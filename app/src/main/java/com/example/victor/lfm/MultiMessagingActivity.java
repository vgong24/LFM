package com.example.victor.lfm;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultiMessagingActivity extends ActionBarActivity {

    private String groupID;
    private List<String> recipientIDs;
    private int recipientSize;
    private EditText messageBodyField;
    private String messageBody;
    private MessageServiceV2.MessageServiceInterface messageService;
    private MessageAdapter messageAdapter;
    private ListView messagesList;
    private String currentUserId;
    private String currentName;
    private Toolbar toolbar;
    private ActionBar ab;

    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MessageClientListener messageClientListener = new MyMessageClientListener();
    private boolean isSent;

    private ArrayList<Pair<String, String>> recipientInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        initFields();
        initialize();
    }
    public void initFields(){
        toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();

        recipientIDs = new ArrayList<>();
        Intent intent = getIntent();
        //ChatRoomID
        groupID = intent.getStringExtra("GROUP_ID");
        recipientSize = intent.getIntExtra("RECIPIENT_SIZE", 0);

        //Add all recipients
        for(int i = 0 ; i < recipientSize; i++){
            String rid = intent.getStringExtra("RECIPIENT_ID" + i);
            recipientIDs.add(rid);
        }
        currentUserId = ParseUser.getCurrentUser().getObjectId();
        currentName = ParseUser.getCurrentUser().getUsername();
        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
    }

    public void initialize(){

        bindService(new Intent(this, MessageServiceV2.class), serviceConnection, BIND_AUTO_CREATE);

        populateMessageHistory();

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.chat_room_toolbar, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_switch_details) {
            Toast.makeText(getApplicationContext(), "meep", Toast.LENGTH_SHORT).show();
            //Activity EventDetails
            startEventDetailActivity(groupID);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startEventDetailActivity(String eventId){

        ParseQuery<Events> query = ParseQuery.getQuery("Events");
        query.getInBackground(eventId, new GetCallback<Events>() {
            @Override
            public void done(Events events, ParseException e) {
                Intent i = new Intent(getApplicationContext(), EventDetails.class);
                i.putExtra("EventId", events.getObjectId());
                i.putExtra("EventDate", events.getDate().getTime());
                i.putExtra("EventTitle", events.getDescr());
                i.putExtra("EventLat", events.getLocation().getLatitude());
                i.putExtra("EventLong", events.getLocation().getLongitude());
                startActivity(i);
            }
        });


    }

    //get previous messages from parse & display
    private void populateMessageHistory() {
        String[] userIds = {currentUserId, groupID};
        ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
        query.whereEqualTo("recipientId", groupID);
        query.orderByAscending("createdAt");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                if (e == null) {
                    for (int i = 0; i < messageList.size(); i++) {
                        WritableMessage message = new WritableMessage(messageList.get(i).get("recipientId").toString(), messageList.get(i).get("messageText").toString());
                        String username = messageList.get(i).getString("senderName");
                        //Check which direction the message came from. currentUser or other recipients
                        if (messageList.get(i).get("senderId").toString().equals(currentUserId)) {
                            //Pass current username
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, username);
                        } else {
                            //Pass sender username
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING, username);
                        }
                    }
                }
            }
        });
    }

    /*
    Sends the message through Sinch services
    Followed by onMessageSent in MyMessageClientListener
     */

    private void sendMessage() {
        messageBody = messageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG).show();
            return;
        }
        //Store message to parse first
        //Send to all recipients (use list)
        //PROTOCOL: ChatRoom + " "  + senderName +" " + restOfMessage

        ParseObject parseMessage = new ParseObject("ParseMessage");
        parseMessage.put("senderId", currentUserId);
        parseMessage.put("senderName", currentName);
        //Make recipientID the EventID so chats can stay with their respective rooms
        parseMessage.put("recipientId", groupID);
        parseMessage.put("messageText", messageBody);
        //parseMessage.put("sinchId", writableMessage.getMessageId());
        parseMessage.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    //If message was properly saved, send message to everyone else using sinch.
                    final WritableMessage writableMessage = new WritableMessage(recipientIDs, messageBody);

                    messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING, currentName);

                    messageService.sendMessage(recipientIDs, groupID + " " + currentName + " " + messageBody);
                    messageBodyField.setText("");

                }else{
                    Toast.makeText(getApplicationContext(), "Text not sent", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onDestroy() {
        messageService.removeMessageClientListener(messageClientListener);
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageService = (MessageServiceV2.MessageServiceInterface) iBinder;
            messageService.addMessageClientListener(messageClientListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            messageService = null;
        }
    }

    private class MyMessageClientListener implements MessageClientListener {
        @Override
        public void onMessageFailed(MessageClient client, Message message,
                                    MessageFailureInfo failureInfo) {
            Toast.makeText(MultiMessagingActivity.this, "Message failed to send.", Toast.LENGTH_LONG).show();
            Log.d("FAILURE INFO", failureInfo.getSinchError().getMessage());
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            Log.v("onIncomingMessage", "Message Received from " + message.getSenderId());
            //Break down message
            //Check first token to see which recipient it is for
            //If it is for currently opened Event activity, then post it
            //PROTOCOL TECHNIQUE
            String messageTextBody = message.getTextBody();
            String arr[] = messageTextBody.split(" ", 3);
            String chatRoomID = arr[0];
            String senderName = arr[1];
            String msgBody = arr[2];

            if (groupID.equals(chatRoomID)) {
                WritableMessage writableMessage = new WritableMessage(message.getRecipientIds(), msgBody);
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING, senderName);
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, final String recipientIdextra) {

        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {}

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }
}
