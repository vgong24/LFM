package com.bowen.victor.ciya.uncategorized;

import android.content.Context;
import android.content.Intent;

import com.bowen.victor.ciya.activities.MainActivity_v2;
import com.parse.ParsePushBroadcastReceiver;

/**
 * Created by Victor on 7/16/2015.
 */
public class ParsePushReceiver extends ParsePushBroadcastReceiver{

    @Override
    public void onPushOpen(Context context, Intent intent){
        Intent i = new Intent(context, MainActivity_v2.class);
        i.putExtras(intent.getExtras());
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
