package com.bowen.victor.ciya;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.List;

public class MessageServiceV2  extends Service implements SinchClientListener {

    /*
    private static final String APP_KEY = "77e0f813-7d7c-49a7-8c8d-54ef1150a93d";
    private static final String APP_SECRET = "HpVNwp8epkGfsSYmANOgTA==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    */

    /* production */
    private static final String APP_KEY = "15fa33c9-20d1-498b-8fec-5759f41a8039";
    private static final String APP_SECRET = "KoOg+I0v6UqCFI9wVogcbA==";
    private static final String ENVIRONMENT = "clientapi.sinch.com";


    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
    private SinchClient sinchClient = null;
    private MessageClient messageClient = null;
    private String currentUserId;
    private LocalBroadcastManager broadcaster;
    private Intent broadcastIntent = new Intent("com.bowen.victor.ciya.MainActivity_v2");

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        currentUserId = ParseUser.getCurrentUser().getObjectId();

        if (currentUserId != null && !isSinchClientStarted()) {
            startSinchClient(currentUserId);
        }

        broadcaster = LocalBroadcastManager.getInstance(this);

        return super.onStartCommand(intent, flags, startId);
    }

    public void startSinchClient(String username) {
        //Toast.makeText(getApplicationContext(), "Starting sinch client", Toast.LENGTH_SHORT).show();
        sinchClient = Sinch.getSinchClientBuilder().context(this).userId(username).applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET).environmentHost(ENVIRONMENT).build();

        sinchClient.addSinchClientListener(this);

        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportActiveConnectionInBackground(true);

        sinchClient.checkManifest();
        sinchClient.start();
    }

    private boolean isSinchClientStarted() {
        return sinchClient != null && sinchClient.isStarted();
    }

    @Override
    public void onClientFailed(SinchClient client, SinchError error) {
        broadcastIntent.putExtra("success", false);
        broadcaster.sendBroadcast(broadcastIntent);

        sinchClient = null;
    }

    @Override
    public void onClientStarted(SinchClient client) {
        //Toast.makeText(getApplicationContext(), "Broadcasting sinchClient", Toast.LENGTH_SHORT).show();
        broadcastIntent.putExtra("success", true);
        broadcaster.sendBroadcast(broadcastIntent);

        client.startListeningOnActiveConnection();
        messageClient = client.getMessageClient();
    }

    @Override
    public void onClientStopped(SinchClient client) {
        sinchClient = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceInterface;
    }

    @Override
    public void onLogMessage(int level, String area, String message) {
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration clientRegistration) {
    }

    public void sendMessage(List<String> recipientUserId, String textBody) {
        if (messageClient != null) {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            messageClient.send(message);

        }
    }

    public void addMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.addMessageClientListener(listener);
        }
    }

    public void removeMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.removeMessageClientListener(listener);
        }
    }

    @Override
    public void onDestroy() {
        if(sinchClient != null) {
            //Toast.makeText(getApplicationContext(), "Stopping SinchClient", Toast.LENGTH_SHORT).show();
            sinchClient.stopListeningOnActiveConnection();
            sinchClient.terminate();
        }
    }

    public class MessageServiceInterface extends Binder {
        public void sendMessage(List<String> recipientUserId, String textBody) {
            MessageServiceV2.this.sendMessage(recipientUserId, textBody);
        }

        public void addMessageClientListener(MessageClientListener listener) {
            MessageServiceV2.this.addMessageClientListener(listener);
        }

        public void removeMessageClientListener(MessageClientListener listener) {
            MessageServiceV2.this.removeMessageClientListener(listener);
        }

        public boolean isSinchClientStarted() {
            return MessageServiceV2.this.isSinchClientStarted();
        }
    }
}