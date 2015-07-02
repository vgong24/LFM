package com.example.victor.lfm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendListAdapter extends ArrayAdapter<FriendProfile> {
    Context context;
    int resourcexml;
    List<FriendProfile> friendProfiles;

    public FriendListAdapter(Context context, int resource, List<FriendProfile> friendList) {
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
            holder.friendStatus = (TextView) view.findViewById(R.id.friend_item_status);


            view.setTag(holder);

        }else{
            holder = (ViewHolder) view.getTag();
        }
        FriendProfile friendProfile = friendProfiles.get(position);
        String fName = friendProfile.getUserName();
        holder.friendName.setText(fName);
        String statusBox = friendProfile.getStatus();


        switch(statusBox){
            case "pending":
                holder.friendStatus.setText("Pending");
                break;

            case "request": //if someone sent you a request, you can approve it
                holder.friendStatus.setText("Accept");
                break;
            case "approve": //if you are already friends, you can choose to remove friend
                holder.friendStatus.setText("Remove");
                break;
            default:
                holder.friendStatus.setVisibility(View.GONE);
                break;
        }

        return view;
    }

    static class ViewHolder {
        TextView friendName;
        TextView friendStatus;


    }
}