package com.example.victor.lfm;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
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
    private ArrayList<String> names;
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
        setConversationList();
        return v;
    }

    //display clickable list of all users
    private void setConversationList(){
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

                    usersListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            openConversation(names, position);
                        }
                    });


                }else{
                    Toast.makeText(context, "Error loading user list", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void openConversation(ArrayList<String> names, int pos){


    }




}
