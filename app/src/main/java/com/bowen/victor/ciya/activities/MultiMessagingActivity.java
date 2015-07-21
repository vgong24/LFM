package com.bowen.victor.ciya.activities;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
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

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.adapters.MessageAdapter;
import com.bowen.victor.ciya.services.MessageServiceV2;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.structures.Events;
import com.bowen.victor.ciya.tools.WorkAround;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MultiMessagingActivity extends ActionBarActivity {

    private String groupID;
    private String groupName;
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

    private ServiceConnection serviceConnection;
    private MessageClientListener messageClientListener;
    public static MultiMessagingActivity mma;

    private ArrayList<Pair<String, String>> recipientInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        mma = this;
        WorkAround.setNotificationBarColor(this, R.color.colorPrimaryDark);
        initFields();
        initialize();
    }
    public void initFields(){
        if(serviceConnection == null){
            serviceConnection = new MyServiceConnection();
        }
        if(messageClientListener == null){
            messageClientListener = new MyMessageClientListener();
        }
        toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        ab = getSupportActionBar();

        recipientIDs = new ArrayList<>();
        Intent intent = getIntent();
        //ChatRoomID and name
        groupID = intent.getStringExtra("GROUP_ID");
        groupName = intent.getStringExtra("TITLE");
        ab.setTitle(groupName);

        ParseUser pu = ParseUser.getCurrentUser();
        currentUserId = pu.getObjectId();
        currentName = pu.getUsername();
        new SetUpRecipientsInBackGround().execute(groupID);
    }

    public void initialize(){

        bindService(new Intent(this, MessageServiceV2.class), serviceConnection, BIND_AUTO_CREATE);

        //Populates Message History
        new SetupHistory().execute();

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);
        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);

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
        MenuItem menuItem = menu.findItem(R.id.action_settings);
        menuItem.setVisible(false);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_switch_details) {
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

                EventDetails.startEventDetails(getApplicationContext(), events);

            }
        });


    }

    /**
     * Retrieves chatroom messages from parse and displays in background thread
     *
     */
    class SetupHistory extends AsyncTask<Void, Void, List<ParseObject>>{

        @Override
        protected List<ParseObject> doInBackground(Void... params) {
            String[] userIds = {currentUserId, groupID};
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
            ParseObject eventObject = ParseObject.createWithoutData("Events", groupID);
            query.whereEqualTo("recipientId", eventObject);
            query.orderByAscending("createdAt");
            try{
                List<ParseObject> messageList = query.find();
                return messageList;

            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<ParseObject> messageList){
            if(messageList == null){
                return;
            }
            for (int i = 0; i < messageList.size(); i++) {
                WritableMessage message = new WritableMessage(messageList.get(i).get("recipientId").toString(), messageList.get(i).get("messageText").toString());
                String username = messageList.get(i).getString("senderName");
                //Get time HH:MM ap of message sent
                Date time = messageList.get(i).getCreatedAt();

                //Check which direction the message came from. currentUser or other recipients
                if (messageList.get(i).get("senderId").toString().equals(currentUserId)) {
                    //Pass current username
                    messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING, username, time);
                } else {
                    //Pass sender username
                    messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING, username, time);
                }
            }
        }
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
        //PROTOCOL: ChatRoom + " "  + senderName + " " + restOfMessage

        ParseObject parseMessage = new ParseObject("ParseMessage");
        ParseObject eventObject = ParseObject.createWithoutData("Events", groupID);
        parseMessage.put("senderId", currentUserId);
        parseMessage.put("senderName", currentName);
        //Make recipientID the EventID so chats can stay with their respective rooms
        parseMessage.put("recipientId", eventObject);
        parseMessage.put("messageText", messageBody);
        parseMessage.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    //If message was properly saved, send message to everyone else using sinch.
                    final WritableMessage writableMessage = new WritableMessage(recipientIDs, messageBody);

                    //add current time

                    messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING, currentName, Calendar.getInstance().getTime());
                    //Try sending messages individually
                    for(int i = 0; i < recipientIDs.size(); i++){
                        if(!recipientIDs.get(i).equalsIgnoreCase(currentUserId)){
                            messageService.sendMessage(recipientIDs.get(i), groupID + " " + currentName + " " + messageBody);
                            //Test push
                        }

                    }

                    /*
                    messageService.sendMessage(recipientIDs, groupID + " " + currentName + " " + messageBody);
                    */
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
            /**
             * PROTOCOL:
             * 1. ChatRoomId represented by Events ObjectId
             * 2. SenderName
             * 3. MsgBody
             */
            String messageTextBody = message.getTextBody();
            String arr[] = messageTextBody.split(" ", 3);
            String chatRoomID = arr[0];
            String senderName = arr[1];
            String msgBody = arr[2];

            if (groupID.equals(chatRoomID)) {
                WritableMessage writableMessage = new WritableMessage(message.getRecipientIds(), msgBody);
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING, senderName, Calendar.getInstance().getTime());
            }
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, final String recipientIdextra) {
            Log.v("DELIVERED", "Message sent: " + message.getRecipientIds().get(0));
        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {
            Log.v("DELIVERED", "Message delivered: " + deliveryInfo.getRecipientId());
        }

        @Override
        public void onShouldSendPushData(MessageClient client, final Message message, List<PushPair> pushPairs) {
            Log.v("SEND PUSH", "Sending push to: " + message.getRecipientIds().get(0));

                //Async send
                //WorkAround.pushToRecipient(currentUserId, "received a message");
        }
    }



    /**AsyncTask Send Push Data
     *
     *
     */
    class SetUpRecipientsInBackGround extends AsyncTask<String, Void, Void>{
        @Override
        protected void onPreExecute(){
            if(recipientIDs != null){
                recipientIDs.clear();
            }
        }

        @Override
        protected Void doInBackground(String... params) {
            Events eventItem = (Events) ParseObject.createWithoutData("Events", params[0]);
            ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
            query.whereEqualTo("Event", eventItem);
            try {
                List<Attendee> results = query.find();

                Log.v("SIZE", "SIZE : " + results.size());
                for (int i = 0; i < results.size(); i++) {
                    //add all the recipients to arraylist
                    try {
                        results.get(i).getUserID().fetchIfNeeded();
                        ParseUser userObj = results.get(i).getUserID();
                        recipientIDs.add(userObj.getObjectId());

                    } catch (ParseException e1) {
                        e1.printStackTrace();
                    }

                }

            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void nothing){

        }
    }
}
