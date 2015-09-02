package com.bowen.victor.ciya.adapters;

/**
 * Created by Victor on 7/11/2015.
 */

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.activities.AboutActivity;
import com.bowen.victor.ciya.activities.MainActivity_v2;
import com.bowen.victor.ciya.activities.ProfileSettings;
import com.bowen.victor.ciya.fragments.FragmentDrawer;
import com.bowen.victor.ciya.model.NavDrawerItem;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.Collections;
import java.util.List;

/**
 * Created by Ravi Tamada on 12-03-2015.
 */
public class NavigationDrawerAdapter extends RecyclerView.Adapter<NavigationDrawerAdapter.MyViewHolder> {
    List<NavDrawerItem> data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;


    public NavigationDrawerAdapter(Context context, List<NavDrawerItem> data) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.data = data;

    }

    public void delete(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.nav_drawer_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        NavDrawerItem current = data.get(position);
        holder.title.setText(current.getTitle());
        switch (position){
            case 0: //profilePage
                holder.icon.setImageResource(R.drawable.ic_action_person);
                break;
            case 1: //Filter
                holder.icon.setImageResource(R.drawable.ic_checkbox_multiple_marked_outline);
                break;
            case 2: //Settings
                holder.icon.setImageResource(R.drawable.ic_action_settings);
                break;
            case 3: //About
                holder.icon.setImageResource(R.drawable.ic_action_about);
                break;
            case 4: //Log out Ask to be sure
                holder.icon.setImageResource(R.drawable.ic_logout);
                break;

        }

        holder.setClickListener(new MyViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position, boolean isLongClick) {
                String itemClickedText = holder.title.getText().toString();

                Log.v("NAVCLICK", "Position: "+ position);
                /**
                 * Edit Profile 0
                 * Event Filter 1
                 * Settings     2
                 * About        3
                 * Logout       4
                 */
                switch (position){
                    case 0: //profilePage
                        ProfileSettings.startProfileSettingsActivity(context);
                        break;
                    case 1: //Filter
                        break;
                    case 2: //Settings
                        break;
                    case 3: //About
                        Intent i = new Intent(context, AboutActivity.class);
                        context.startActivity(i);
                        break;
                    case 4: //Log out Ask to be sure
                        MainActivity_v2.logOutConfirm(context);
                        break;

                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        ImageView icon;
        private ClickListener clickListener;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            itemView.setOnClickListener(this);
        }


        public interface ClickListener {

            /**
             * Called when the view is clicked.
             *
             * @param v           view that is clicked
             * @param position    of the clicked item
             * @param isLongClick true if long click, false otherwise
             */
            public void onClick(View v, int position, boolean isLongClick);

        }

        /* Setter for listener. */
        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }
        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getPosition(), false);
        }
    }
}