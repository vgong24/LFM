package com.example.victor.lfm;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 6/13/2015.
 */
public class FriendsTab extends Fragment {
    View v;
    Context context;
    Activity activity;

    TextView searchedFriend;
    ListView friendlv;

    List<String> friendNames;
    FriendListDBHandler dbhandler;
    ArrayAdapter<String> profileAdapter;

    String currentUser;


    public FriendsTab(Context context){
        this.context = context;
        activity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.friends_tab, container, false);
        initialize();
        populateFriendList();
        setupSearchView();
        return v;
    }

    public void initialize(){
        currentUser = ParseUser.getCurrentUser().getUsername();
        searchedFriend = (TextView) v.findViewById(R.id.searchFriendView);
        friendlv = (ListView) v.findViewById(R.id.friendListView);
        friendNames = new ArrayList<>();

        dbhandler = new FriendListDBHandler(context);
        if(dbhandler.getFriendCount() != 0){
            friendNames.addAll(dbhandler.getAllFriendProfilesToString());
        }

        new UpdateFriendList().execute(currentUser);
    }
    //Set up Search functionality
    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) v.findViewById(R.id.searchView);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(activity.getComponentName());
        searchView.setSearchableInfo(searchableInfo);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("setupSearch", "query: " + query);
                new SearchForFriend().execute(query);
                //Display result
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    //Given username, check the friends db for any usernames that matches
    private boolean profileExists(String profileName){
        int profileCount = friendNames.size();

        for (int i = 0; i < profileCount; i++){
            if(profileName.compareToIgnoreCase(friendNames.get(i)) == 0)
                return true;
        }

        return false;

    }

    //Display Friendlist
    public void populateFriendList(){
        profileAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, friendNames);
        friendlv.setAdapter(profileAdapter);
    }

    //send a friend requests using usernames rather than object ids
    public void sendRequest(String friendname){
        String currentUserName = ParseUser.getCurrentUser().getUsername();
        FriendRequest.sendFriendRequest(currentUserName, friendname);

    }

    //Find any new friends to add to local database
    //Check for any requests for the current user

    class UpdateFriendList extends AsyncTask<String, Void, Boolean>{

        //Check for friends that were sent requests by user if they approved it
        @Override
        protected Boolean doInBackground(String... params) {
            final String STATUS = "approve";
            String username = params[0];
            List<FriendRequest> friendRequestList = new ArrayList<>();
            ParseQuery<FriendRequest> query = ParseQuery.getQuery("FriendRequest");
            query.whereEqualTo("reqFrom",username);
            query.whereEqualTo("status", STATUS);

            try {
                friendRequestList = query.find();
                for(FriendRequest fr : friendRequestList){
                    String friendName, fstatus = STATUS;
                    friendName = fr.getString("reqTo");

                    if(!profileExists(friendName)){
                        dbhandler.createFriend("", friendName,"", fstatus);
                        friendNames.add(friendName);
                        return true;
                    }
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }


            return false;
        }

        @Override
        protected void onPostExecute(Boolean newFriends){
            //Add new friends to db
            if(newFriends){
                populateFriendList();
            }

        }
    }

    @Override
    public void onResume(){
        super.onResume();
        populateFriendList();
    }


    //Async display username results
    //Searches for a friend then will display friend name in listview
    class SearchForFriend extends AsyncTask<String, Void, ParseUser>{

        @Override
        protected ParseUser doInBackground(String... params) {
            String username = params[0];
            //Find user with matching username
            ParseQuery query = ParseQuery.getQuery("_User");
            query.whereEqualTo("username", username);
            try {
                List<ParseUser> userObjs = query.find();
                for(ParseUser parseUser: userObjs) {
                    return parseUser;
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(final ParseUser puser){
            if(puser == null){
                Log.v("searchFriend", "Friend not found");
                searchedFriend.setText("");
                Toast.makeText(context, "Friend not found", Toast.LENGTH_SHORT).show();

            }else{
                final String friendUserName = puser.getUsername();
                Log.v("searchFriend", "Found friend: "+friendUserName);
                searchedFriend.setText(friendUserName);
                searchedFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Send Friend request
                        sendRequest(friendUserName);
                        Toast.makeText(context, "Friend Request Sent", Toast.LENGTH_SHORT).show();
                        //searchedFriend.setText("");
                    }
                });
            }

        }
    }




}
