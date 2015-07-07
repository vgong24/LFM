package com.example.victor.lfm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 5/15/2015.
 */
public class ChatTab extends Fragment {
    Context context;
    Activity activity;
    private String currentUserId;
    private ArrayAdapter<String> namesArrayAdapter;
    private ChatListAdapter eventsArrayAdapter;

    private ArrayList<String> names;
    private ArrayList<Events> events;
    private ListView usersListView;
    private ProgressBar progressBar;
    private BroadcastReceiver receiver = null;
    private View v;

    private SetUpChatList chatList;


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
     * Display clickable list of events you are currently participating
     */
    private void setChatList(){
        currentUserId = ParseUser.getCurrentUser().getObjectId();
        names = new ArrayList<String>();
        events = new ArrayList<Events>();


        ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
        query.whereEqualTo("User", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<Attendee>() {
            @Override
            public void done(List<Attendee> list, ParseException e) {
                if (e == null) {
                    for (Attendee attend : list) {
                        try {
                            attend.getEventObject().fetchIfNeeded();
                            events.add(attend.getEventObject());
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }

                    }

                    usersListView = (ListView) activity.findViewById(R.id.usersListView);
                    eventsArrayAdapter = new ChatListAdapter(context, R.layout.chat_list_item, events);
                    usersListView.setAdapter(eventsArrayAdapter);

                    usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            openChatRoom(events, position);
                        }
                    });
                }
            }
        });


    }

    //open a conversation with multiple people
    //Send the event object ID which will represent the universal recipient
    public void openChatRoom(ArrayList<Events> eventsArrayList, int pos){

        final String eventIDString = eventsArrayList.get(pos).getObjectId();

        //Experiment
        ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
        query.whereEqualTo("Event", eventsArrayList.get(pos));
        query.findInBackground(new FindCallback<Attendee>() {
            @Override
            public void done(List<Attendee> list, ParseException e) {
                if (e == null) {

                    Intent intent = new Intent(context, MultiMessagingActivity.class);
                    intent.putExtra("GROUP_ID", eventIDString);
                    intent.putExtra("RECIPIENT_SIZE", list.size());
                    Log.v("SIZE", "SIZE : " + list.size());
                    for(int i = 0 ; i < list.size(); i++){
                        //add all the recipients to arraylist
                        try {
                            list.get(i).getUserID().fetchIfNeeded();
                            ParseUser userObj = list.get(i).getUserID();
                            intent.putExtra("RECIPIENT_ID" + i, userObj.getObjectId());
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }

                    }

                    context.startActivity(intent);
                }
            }
        });


    }

    public void populateList(ArrayList<Events> eventsArrayList){
        progressBar.setVisibility(View.GONE);
        eventsArrayAdapter = new ChatListAdapter(context, R.layout.chat_list_item, eventsArrayList);
        //usersListView = (ListView) activity.findViewById(R.id.usersListView);
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

        //setChatList();
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
            currentUserId = ParseUser.getCurrentUser().getObjectId();
            //usersListView = (ListView) activity.findViewById(R.id.usersListView);
            //usersListView.setVisibility(View.GONE);
            events.clear();

        }

        @Override
        protected ArrayList<Events> doInBackground(Void... params) {

            ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
            ParseObject userObject = ParseObject.createWithoutData("User", ParseUser.getCurrentUser().getObjectId());
            //CHANGE
            //query.whereEqualTo("User", userObject);
            query.whereEqualTo("User", ParseUser.getCurrentUser());
            //query.setCachePolicy(ParseQuery.CachePolicy.NETWORK_ONLY);
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
 //           Log.v("current USer", "ID: " + ParseUser.getCurrentUser().getObjectId());
            //usersListView.setVisibility(View.VISIBLE);
            populateList(eventsArr);


        }

    }

}
