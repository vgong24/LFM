package com.bowen.victor.ciya.tools;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SendCallback;

/**
 * Created by Victor on 7/11/2015.
 */
public class WorkAround {
    public static void setNotificationBarColor(Activity activity, int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(color));
        }

    }

    public static void pushToRecipient(final String userId, final String message){
        ParseQuery userQuery = ParseUser.getQuery();
        userQuery.whereEqualTo("objectId", userId);

        ParseQuery pushQuery = ParseInstallation.getQuery();
        pushQuery.whereMatchesQuery("pUser", userQuery);

        // Send push notification to query
        ParsePush push = new ParsePush();
        push.setQuery(pushQuery); // Set our Installation query
        push.setMessage(message);
        push.sendInBackground(new SendCallback() {
            @Override
            public void done(ParseException e) {
                Log.v("SEND PUSH", "Sending push to: " + userId);
                if (e == null) {
                    Log.v("Pushed", "Worked");

                } else {
                    e.printStackTrace();
                }


            }
        });
    }

}
