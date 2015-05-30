package com.example.victor.lfm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 5/15/2015.
 */
public class ChatTab extends Fragment{
    Context context;
    Activity activity;
    private String currentUserId;
    private ArrayAdapter<String> namesArrayAdapter;
    private ChatListAdapter eventsArrayAdapter;

    private ArrayList<String> names;
    private ArrayList<Events> events;
    private ListView usersListView;
    private ProgressDialog progressDialog;
    private BroadcastReceiver receiver = null;

    public ChatTab(Context context){
        this.context = context;
        this.activity = (Activity) context;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.chat_room_list,container,false);
        //showSpinner();
        //setConversationsList();
        return v;
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
                if(e == null) {
                    for (Attendee attend : list) {
                        try {
                            attend.getEventObject().fetchIfNeeded();
                        } catch (ParseException e1) {
                            e1.printStackTrace();
                        }
                        events.add(attend.getEventObject());
                    }

                    usersListView = (ListView) activity.findViewById(R.id.usersListView);
                    eventsArrayAdapter = new ChatListAdapter(context, R.layout.chat_list_item, events);
                    usersListView.setAdapter(eventsArrayAdapter);

                }
            }
        });


    }

    //display clickable list of all users
    private void setConversationsList(){
        currentUserId = ParseUser.getCurrentUser().getObjectId();
        names = new ArrayList<String>();
        //names.clear();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", currentUserId);
        query.findInBackground(new FindCallback<ParseUser>(){

            @Override
            public void done(List<ParseUser> userList, ParseException e) {
                if(e == null){

                    for(int i = 0; i < userList.size(); i++){
                        names.add(userList.get(i).getUsername().toString());
                    }
                    usersListView = (ListView) activity.findViewById(R.id.usersListView);
                    namesArrayAdapter = new ArrayAdapter<String>(context, R.layout.chat_list_item, names);
                    usersListView.setAdapter(namesArrayAdapter);

                    usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            //openConversation(names, position);
                        }
                    });


                }else{
                    Toast.makeText(context, "Error loading user list", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //open a conversation with one person
    public void openConversation(ArrayList<String> names, int pos) {
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", names.get(pos));
        query.findInBackground(new FindCallback<ParseUser>() {
            public void done(List<ParseUser> user, com.parse.ParseException e) {
                if (e == null) {
                    Intent intent = new Intent(context, MessagingActivity.class);
                    for(int i = 0; i < user.size(); i++){
                        intent.putExtra("RECIPIENT_ID" + i, user.get(i).getObjectId());
                    }

                    intent.putExtra("NUM_OF_RECIPIENT", user.size());
                    startActivity(intent);
                } else {
                    Toast.makeText(context,
                            "Error finding that user",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //show a loading spinner while the sinch client starts
    private void showSpinner() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Boolean success = intent.getBooleanExtra("success", false);
                progressDialog.dismiss();
                if (!success) {
                    Toast.makeText(context, "Messaging service failed to start", Toast.LENGTH_LONG).show();
                }
            }
        };

        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, new IntentFilter("com.example.victor.lfm.MainActivity_v2"));
    }

    @Override
    public void onResume() {
        //setConversationsList();
        setChatList();
        super.onResume();
    }



}
