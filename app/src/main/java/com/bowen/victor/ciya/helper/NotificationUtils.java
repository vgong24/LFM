package com.bowen.victor.ciya.helper;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.uncategorized.AppConfig;

import java.util.List;

/**
 * Created by Ravi on 01/06/15.
 */
public class NotificationUtils {

    private String TAG = NotificationUtils.class.getSimpleName();

    private Context mContext;
    private NotificationCompat.InboxStyle inboxStyle;
    private static NotificationCompat.Builder mBuilder;
    private static boolean firstTime;
    private static NotificationManager notificationManager;




    public NotificationUtils() {
    }

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
        firstTime = true;
    }

    public void showNotificationMessage(String title, String message, Intent intent) {
        // Check for empty push message
        if (TextUtils.isEmpty(message))
            return;
        if (isAppIsInBackground(mContext)) {
            // notification icon
            int icon = R.drawable.ic_launcher;

            int mNotificationId = AppConfig.NOTIFICATION_ID;

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );
            Log.v(TAG, "Forth Step");
            //First Time
            if(firstTime) {
                Log.v(TAG, "First time set up");
                NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                mBuilder = new NotificationCompat.Builder(
                        mContext);
                mBuilder.setSmallIcon(R.drawable.ic_launcher).setTicker(message).setWhen(0)
                        .setAutoCancel(true)
                        .setContentTitle(title)
                        .setOnlyAlertOnce(true)
                        .setStyle(inboxStyle)
                        .setContentIntent(resultPendingIntent)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setContentText(message);

                notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                firstTime = false;
            }

            Log.v(TAG, "second time");
            mBuilder.setContentTitle(title)
                    .setContentText(message)
                    .setStyle(new NotificationCompat.InboxStyle()
                            .addLine(message))
                    .setTicker(message);
            notificationManager.notify(mNotificationId, mBuilder.build());



        } else {
            Log.v(TAG, "Else...");
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
        }
    }

    /**
     * Method checks if the app is in background or not
     *
     * @param context
     * @return
     */
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

}