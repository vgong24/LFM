package com.bowen.victor.ciya;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Victor on 7/9/2015.
 */
public class InviteListAdapter extends ArrayAdapter<FriendProfile>{
    Context context;
    int resourcexml;
    List<FriendProfile> friendProfiles;

    public InviteListAdapter(Context context, int resource, List<FriendProfile> friendList) {
        super(context, resource, friendList);
        this.context = context;
        resourcexml = resource;
        friendProfiles = friendList;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){


        ViewHolder holder;

        if(view == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(resourcexml, parent, false);
            holder = new ViewHolder();

            holder.friendName = (TextView) view.findViewById(R.id.friend_username);
            holder.inviteStatus = (TextView) view.findViewById(R.id.invite_status);


            view.setTag(holder);

        }else{
            holder = (ViewHolder) view.getTag();
        }
        FriendProfile friendProfile = friendProfiles.get(position);
        String fName = friendProfile.getUserName();
        holder.friendName.setText(fName);
        holder.inviteStatus.setText("Invite");

        return view;
    }

    static class ViewHolder {
        TextView friendName;
        TextView inviteStatus;


    }
}
