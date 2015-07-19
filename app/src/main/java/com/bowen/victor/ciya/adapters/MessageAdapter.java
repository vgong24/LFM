package com.bowen.victor.ciya.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.tools.WorkAround;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;

    private List<Quadruple<WritableMessage, Integer, String, Date>> messages;
    private List<String> senderNames;
    private LayoutInflater layoutInflater;

    SimpleDateFormat timeFormat = new SimpleDateFormat();
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    Calendar firstDate = null;
    Calendar checkDate = Calendar.getInstance();

    public MessageAdapter(Activity activity) {
        layoutInflater = activity.getLayoutInflater();
        //messages = new ArrayList<Pair<WritableMessage, Integer>>();
        messages = new ArrayList<Quadruple<WritableMessage, Integer, String, Date>>();

    }

    public void addMessage(WritableMessage message, int direction, String senderName) {
        messages.add(new Quadruple(message, direction, senderName, null));
        notifyDataSetChanged();
    }
    public void addMessage(WritableMessage message, int direction, String senderName, Date time) {
        messages.add(new Quadruple(message, direction, senderName, time));
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

        //Include date
        TextView txtdate = (TextView) convertView.findViewById(R.id.txtDate);
        Date date = messages.get(i).fourth;
        Date date2;
        if(date != null){
            //If first messege
            String formattedDate = "";
            if(i == 0){
                dateFormat.applyLocalizedPattern("M/d/yy h:mm a");
                formattedDate = dateFormat.format(date);
            }else{
                //compare previous message to see if same day
                date2 = messages.get(i-1).fourth;
                if(WorkAround.isSameDay(date, date2)){
                    timeFormat.applyLocalizedPattern("h:mm a");
                    formattedDate = timeFormat.format(date);
                }else{
                    dateFormat.applyLocalizedPattern("M/d/yy h:mm a");
                    formattedDate = dateFormat.format(date);

                }

            }
            txtdate.setText(formattedDate);

        }


        return convertView;
    }

    private class Quadruple <F, S, T, D>{
        private F first;
        private S second;
        private T third;
        private D fourth;

        public Quadruple(F first, S second, T third, D fourth){
            this.first = first;
            this.second = second;
            this.third = third;
            this.fourth = fourth;
        }


    }
}