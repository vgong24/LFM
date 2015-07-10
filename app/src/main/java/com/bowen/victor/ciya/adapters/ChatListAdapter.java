package com.bowen.victor.ciya.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bowen.victor.ciya.structures.Events;
import com.bowen.victor.ciya.R;

import java.util.ArrayList;

/**
 * Created by Victor on 5/29/2015.
 * Usage: ChatTab
 */
public class ChatListAdapter extends ArrayAdapter<Events> {
    Context context;
    int viewList;
    ArrayList<Events> eventses;

    public ChatListAdapter(Context context, int viewlist, ArrayList<Events> eventsArrayList) {
        super(context, viewlist, eventsArrayList);
        this.context = context;
        viewList = viewlist;
        eventses = eventsArrayList;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        ViewHolderChat holder;
        if(view == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(viewList, parent, false);

            holder = new ViewHolderChat();
            holder.chatRoomName = (TextView) view.findViewById(R.id.userListItem);
            view.setTag(holder);
        }else{
            holder = (ViewHolderChat) view.getTag();
        }
        Events event = eventses.get(position);
        String eventName = event.getDescr();
        holder.chatRoomName.setText(eventName);

        return view;
    }

    static class ViewHolderChat {
        TextView chatRoomName;


    }
}
