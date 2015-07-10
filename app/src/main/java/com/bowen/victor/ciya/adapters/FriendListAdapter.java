package com.bowen.victor.ciya.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bowen.victor.ciya.structures.FriendProfile;
import com.bowen.victor.ciya.R;

import java.util.List;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendListAdapter extends ArrayAdapter<FriendProfile> {
    Context context;
    int resourcexml;
    List<FriendProfile> friendProfiles;

    public interface BtnClickListener {
        public abstract void onBtnClick(int position);
    }

    private BtnClickListener mClickListener = null;

    public FriendListAdapter(Context context, int resource, List<FriendProfile> friendList, BtnClickListener listener) {
        super(context, resource, friendList);
        this.context = context;
        resourcexml = resource;
        friendProfiles = friendList;
        mClickListener = listener;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){


        ViewHolder holder;

        if(view == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(resourcexml, parent, false);
            holder = new ViewHolder();

            holder.friendName = (TextView) view.findViewById(R.id.friend_username);
            holder.friendStatusText = (TextView) view.findViewById(R.id.friend_status_text);
            holder.friendStatusImg = (ImageView) view.findViewById(R.id.friend_status_img);
            holder.friendStatusImg.setTag(position);
            holder.friendStatusImg.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mClickListener != null)
                        mClickListener.onBtnClick((Integer) v.getTag());
                }
            });


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
                holder.friendStatusText.setText("Pending");
                break;
            case "request": //if someone sent you a request, you can approve it
                holder.friendStatusText.setText("Accept");
                holder.friendStatusImg.setImageResource(R.drawable.greenplus);
                break;
            case "approve": //if you are already friends, you can choose to remove friend
                holder.friendStatusText.setText("Remove");
                holder.friendStatusImg.setImageResource(R.drawable.redx);
                break;
            default:
                holder.friendStatusText.setVisibility(View.GONE);
                break;
        }

        return view;
    }

    static class ViewHolder {
        TextView friendName;
        TextView friendStatusText;
        ImageView friendStatusImg;

    }
}
