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

import java.util.Calendar;
import java.util.Date;

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

    /** isSameDay - CODE USED FROM https://github.com/exabakr/android-app/blob/master/YourAppIdea/src/main/java/org/michenux/android/lang/DateUtils.java
     *
     * <p>Checks if two dates are on the same day ignoring time.</p>
     * @param date1  the first date, not altered, not null
     * @param date2  the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either date is <code>null</code>
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    /**
     * <p>Checks if two calendars represent the same day ignoring time.</p>
     * @param cal1  the first calendar, not altered, not null
     * @param cal2  the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

}
