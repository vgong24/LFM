package com.bowen.victor.ciya.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.bowen.victor.ciya.activities.MultiMessagingActivity;
import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.adapters.ChatListAdapter;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.structures.Events;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 5/15/2015.
 */
public class ChatTab extends Fragment {
    Context context;
    Activity activity;
    private ChatListAdapter eventsArrayAdapter;

    private ArrayList<String> names;
    private ArrayList<Events> events;
    private ListView usersListView;
    private ProgressBar progressBar;
    private View v;


    public static ChatTab newInstance(Context context){
        ChatTab chatTab = new ChatTab();
        chatTab.context = context;
        chatTab.activity = (Activity) context;
        return chatTab;
    }
    public ChatTab(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.chat_room_list, container, false);
        //initialize();
        //new SetUpChatList().execute();

        return v;
    }

    public void initialize(){
        progressBar = (ProgressBar) v.findViewById(R.id.chatRoomProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        if(events == null){
            events = new ArrayList<Events>();
        }
        if(usersListView == null){
            usersListView = (ListView) v.findViewById(R.id.usersListView);
        }

    }

    /**
     * Starts the MultiMessagingActivity with GroupID (Events ObjectID)
     * @param eventsArrayList
     * @param pos
     */
    public void openChatRoom(ArrayList<Events> eventsArrayList, int pos){

        final String eventIDString = eventsArrayList.get(pos).getObjectId();

        Intent intent = new Intent(context, MultiMessagingActivity.class);
        intent.putExtra("GROUP_ID", eventIDString);
        intent.putExtra("TITLE", eventsArrayList.get(pos).getDescr());

        context.startActivity(intent);

    }

    /**
     * Displays list of Events joined
     * TODO: Change variable name userListView to avoid confusion
     * @param eventsArrayList
     */
    public void populateList(ArrayList<Events> eventsArrayList){
        progressBar.setVisibility(View.GONE);
        eventsArrayAdapter = new ChatListAdapter(context, R.layout.chat_list_item, eventsArrayList);
        usersListView.setAdapter(eventsArrayAdapter);
        usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openChatRoom(events, position);

            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        initialize();
        new SetUpChatList().execute();

    }

    @Override
    public void onPause(){
        super.onPause();


    }

    private class SetUpChatList extends AsyncTask<Void, Void, ArrayList<Events>> {

        @Override
        protected void onPreExecute(){
            if(events != null)
            events.clear();

        }

        @Override
        protected ArrayList<Events> doInBackground(Void... params) {

            ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
            query.whereEqualTo("User", ParseUser.getCurrentUser());
            query.whereEqualTo("inviteStatus", Attendee.JOINED);
            try {

                List<Attendee> tempList = query.find();
                for (Attendee attend : tempList) {
                    try{
                        Events ev = attend.getEventObject().fetchIfNeeded();
                        events.add(ev);
                        Log.v("Attendee", "found: " + ev.getObjectId());
                    }catch (ParseException e){
                        e.printStackTrace();
                    }

                }

                Log.v("Array Length", "Array length in backgroudn: "+ events.size());
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return events;
        }

        @Override
        protected void onPostExecute(ArrayList<Events> eventsArr) {
            Log.v("Array Length", "Array length: " + eventsArr.size());
            populateList(eventsArr);


        }

    }

}
