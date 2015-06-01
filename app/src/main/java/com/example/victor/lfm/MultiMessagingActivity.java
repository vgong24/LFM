package com.example.victor.lfm;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
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

public class MultiMessagingActivity extends Activity {

    private String groupID;
    private List<String> recipientIDs;
    private int recipientSize;
    private EditText messageBodyField;
    private String messageBody;
    private MessageServiceV2.MessageServiceInterface messageService;
    private MessageAdapter messageAdapter;
    private ListView messagesList;
    private String currentUserId;
    private ServiceConnection serviceConnection = new MyServiceConnection();
    private MessageClientListener messageClientListener = new MyMessageClientListener();
    private boolean isSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.messaging);
        recipientIDs = new ArrayList<>();

        bindService(new Intent(this, MessageServiceV2.class), serviceConnection, BIND_AUTO_CREATE);

        Intent intent = getIntent();
        //Actually is group message id
        groupID = intent.getStringExtra("GROUP_ID");
        recipientSize = intent.getIntExtra("RECIPIENT_SIZE", 0);
        for(int i = 0 ; i < recipientSize; i++){
            String rid = intent.getStringExtra("RECIPIENT_ID" + i);
            Log.v("RECIPIENT_IDDDDDDDDD", "RECIPIENT ID: "+rid);
            recipientIDs.add(rid);
        }



        Log.d("AAAAAAAAAAAAAAAAAA", "Group ID: "+groupID);

        currentUserId = ParseUser.getCurrentUser().getObjectId();

        messagesList = (ListView) findViewById(R.id.listMessages);
        messageAdapter = new MessageAdapter(this);
        messagesList.setAdapter(messageAdapter);
        populateMessageHistory();

        messageBodyField = (EditText) findViewById(R.id.messageBodyField);

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
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
                        if (messageList.get(i).get("senderId").toString().equals(currentUserId)) {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_OUTGOING);
                        } else {
                            messageAdapter.addMessage(message, MessageAdapter.DIRECTION_INCOMING);
                        }
                    }
                }
            }
        });
    }

    private void sendMessage() {
        messageBody = messageBodyField.getText().toString();
        if (messageBody.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_LONG).show();
            return;
        }
        //CHANGE
        Log.d("BBBBBBBBBBBBBBB", "Sending message");
        Log.v("Check ID", "current: " + currentUserId + " sending " + recipientIDs.get(0));
        //Before the message has been sent
        //Send to all recipients (use list)
        //But only post one time to parse
        isSent = false;
        //If current user is the host
        /*
        if(currentUserId.equalsIgnoreCase(recipientIDs.get(0))) {
            //Using protocols within the message to check later
            messageService.sendMessage(recipientIDs.get(recipientSize - 1), groupID+" "+messageBody);
        }else{
            messageService.sendMessage(recipientIDs.get(0), groupID+" "+messageBody);
        }
        //messageService.sendMessage(currentUserId, messageBody);
        */
        messageService.sendMessage(recipientIDs, groupID + " " + messageBody);
        messageBodyField.setText("");
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
            Log.d("DDDDDDDDDDDDDDD", message.getRecipientIds().get(0));
            Log.d("FAILURE INFO", failureInfo.getSinchError().getMessage());
        }

        @Override
        public void onIncomingMessage(MessageClient client, Message message) {
            Log.v("onIncomingMessage", "Message Received from " + message.getSenderId());
            //Break down message
            //Check first token to see which recipient it is for
            //If it is for currently opened Event activity, then post it

            //if (message.getSenderId().equals(groupId)) {
                WritableMessage writableMessage = new WritableMessage(message.getRecipientIds().get(0), message.getTextBody());
                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_INCOMING);
            //}
        }

        @Override
        public void onMessageSent(MessageClient client, Message message, final String recipientIdextra) {
            Toast.makeText(getApplicationContext(),"On sent message", Toast.LENGTH_SHORT).show();
            //CHANGE
            final WritableMessage writableMessage = new WritableMessage(message.getRecipientIds(), message.getTextBody());
            //final WritableMessage writableMessage2 = new WritableMessage(message.getRecipientIds().get(recipientSize-1), message.getTextBody());



            //If message has not been sent to parse yet
            if(!isSent){
                //only add message to parse database if it doesn't already exist there
                ParseQuery<ParseObject> query = ParseQuery.getQuery("ParseMessage");
                query.whereEqualTo("sinchId", message.getMessageId());
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> messageList, com.parse.ParseException e) {
                        if (e == null) {
                            if (messageList.size() == 0 && !isSent) {
                                ParseObject parseMessage = new ParseObject("ParseMessage");
                                parseMessage.put("senderId", currentUserId);
                                //CHANGE
                                //parseMessage.put("recipientId", writableMessage.getRecipientIds().get(0));
                                parseMessage.put("recipientId", groupID);
                                parseMessage.put("messageText", writableMessage.getTextBody());
                                parseMessage.put("sinchId", writableMessage.getMessageId());
                                parseMessage.saveInBackground();

                                messageAdapter.addMessage(writableMessage, MessageAdapter.DIRECTION_OUTGOING);
                                //messageAdapter.addMessage(writableMessage2, MessageAdapter.DIRECTION_OUTGOING);
                                isSent = true;
                            }
                        }
                    }
                });
            }
        }

        @Override
        public void onMessageDelivered(MessageClient client, MessageDeliveryInfo deliveryInfo) {}

        @Override
        public void onShouldSendPushData(MessageClient client, Message message, List<PushPair> pushPairs) {}
    }
}
