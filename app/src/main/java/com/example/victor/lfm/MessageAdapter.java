package com.example.victor.lfm;

import android.app.Activity;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private List<Triple<WritableMessage, Integer, String>> messages;
    private List<String> senderNames;
    private LayoutInflater layoutInflater;

    public MessageAdapter(Activity activity) {
        layoutInflater = activity.getLayoutInflater();
        //messages = new ArrayList<Pair<WritableMessage, Integer>>();
        messages = new ArrayList<Triple<WritableMessage, Integer, String>>();

    }

    public void addMessage(WritableMessage message, int direction, String senderName) {
        messages.add(new Triple(message, direction, senderName));
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int i) {
        return messages.get(i).second;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        int direction = getItemViewType(i);

        //show message on left or right, depending on if
        //it's incoming or outgoing
        if (convertView == null) {
            int res = 0;
            if (direction == DIRECTION_INCOMING) {
                res = R.layout.message_right;
            } else if (direction == DIRECTION_OUTGOING) {
                res = R.layout.message_left;
            }
            convertView = layoutInflater.inflate(res, viewGroup, false);
        }

        //Display the sender and message in the Views

        WritableMessage message = messages.get(i).first;

        TextView txtMessage = (TextView) convertView.findViewById(R.id.txtMessage);
        txtMessage.setText(message.getTextBody());

        TextView txtSender = (TextView) convertView.findViewById(R.id.txtSender);
        txtSender.setText(messages.get(i).third);

        return convertView;
    }

    private class Triple <F, S, T>{
        private F first;
        private S second;
        private T third;

        public Triple(F first, S second, T third){
            this.first = first;
            this.second = second;
            this.third = third;
        }


    }
}