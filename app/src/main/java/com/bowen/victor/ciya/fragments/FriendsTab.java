package com.bowen.victor.ciya.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bowen.victor.ciya.dbHandlers.FriendListDBHandler;
import com.bowen.victor.ciya.structures.FriendRequest;
import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.adapters.FriendListAdapter;
import com.bowen.victor.ciya.structures.FriendProfile;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
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

    List<FriendProfile> friendNames;
    FriendListDBHandler dbhandler;
    FriendListAdapter profileAdapter;

    String currentUser;
    String reqType;

    List<FriendRequest> friendRequestList;

    //Regular friends tab
    public static FriendsTab newInstance(Context context){
        FriendsTab friendsTab = new FriendsTab();

        friendsTab.context = context;
        friendsTab.activity = (Activity) context;
        return friendsTab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.friends_tab, container, false);
        initialize();
        //populateFriendList();
        setupSearchView();
        return v;
    }

    public void initialize(){
        currentUser = ParseUser.getCurrentUser().getUsername();
        searchedFriend = (TextView) v.findViewById(R.id.searchFriendView);
        searchedFriend.setVisibility(View.GONE); //Hide the search results to not mess up FriendList

        friendlv = (ListView) v.findViewById(R.id.friendListView);
        if(friendNames == null) {
            friendNames = new ArrayList<>();
        }else{
            friendNames.clear();
        }

        dbhandler = new FriendListDBHandler(context);

        if(dbhandler.getFriendCount() != 0){
            friendNames.addAll(dbhandler.getAllFriendProfiles());
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
    private int profileExists(String profileName){
        int profileCount = friendNames.size();

        for (int i = 0; i < profileCount; i++){
            if(profileName.compareToIgnoreCase(friendNames.get(i).getUserName()) == 0)
                return i;
        }

        return -1;

    }

    //Display Friendlist then set up onclick listeners
    public void populateFriendList(){
        profileAdapter = new FriendListAdapter(context, R.layout.friend_request_item, friendNames, new FriendListAdapter.BtnClickListener() {

            @Override
            public void onBtnClick(int position) {
                FriendProfile friendProfile = friendNames.get(position);
                String friendReqId = friendProfile.getFriendRequestId();
                String friendProfileStatus = friendProfile.getStatus();

                switch(friendProfileStatus){
                    case REMOVE:
                        Toast.makeText(context, "Removed Friend Request", Toast.LENGTH_SHORT).show();
                        break;
                    case ACCEPT:
                        FriendRequest.approveFriendRequest(friendReqId);
                        Toast.makeText(context, "Accepted Friend Request", Toast.LENGTH_SHORT).show();
                        friendProfile.setStatus(REMOVE);
                        break;
                    case PENDING:
                        break;
                }
                populateFriendList();
            }
        });
        friendlv.setAdapter(profileAdapter);
        onFriendClick();
    }

    //send a friend requests using usernames rather than object ids
    //If username matches current user, dont send. duh~
    public void sendRequest(String friendname){
        String currentUserName = ParseUser.getCurrentUser().getUsername();
        if(!friendname.equalsIgnoreCase(currentUserName)) {
            FriendRequest.sendFriendRequest(currentUserName, friendname);
            Toast.makeText(context, "Friend Request Sent", Toast.LENGTH_SHORT).show();
            new UpdateFriendList().execute(currentUser);
        }else{
            Toast.makeText(context, "That's you, idiot.", Toast.LENGTH_SHORT).show();
        }

    }


    //Onclick listeners for approve (remove), request(accept), and pending (pending)
    private final String REMOVE = "approve";
    private final String ACCEPT = "request";
    private final String PENDING = "pending";
    private int focus = -1;

    public void onFriendClick(){
        friendlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                for (int i = 0; i <= parent.getLastVisiblePosition(); i++) {
                    if (i != position) {
                        friendlv.getChildAt(i).findViewById(R.id.friend_status_text).setVisibility(View.INVISIBLE);
                        friendlv.getChildAt(i).findViewById(R.id.friend_status_img).setVisibility(View.INVISIBLE);
                    }
                }
                TextView friendStatusText = (TextView) view.findViewById(R.id.friend_status_text);
                ImageView friendStatusImg = (ImageView) view.findViewById(R.id.friend_status_img);
                if (friendStatusText.getVisibility() == View.INVISIBLE) {
                    friendStatusText.setVisibility(View.VISIBLE);
                    friendStatusImg.setVisibility(View.VISIBLE);
                } else {
                    friendStatusText.setVisibility(View.INVISIBLE);
                    friendStatusImg.setVisibility(View.INVISIBLE);
                }
                //Toast.makeText(context, view + "/" + friendlv.getChildAt(0), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        populateFriendList();
        //initialize();
    }


    //Find any new friends to add to local database
    //Check for any requests for the current user

    class UpdateFriendList extends AsyncTask<String, Void, Boolean>{

        @Override
        protected void onPreExecute(){

        }

        //Check for friends that were sent requests by user if they approved it
        @Override
        protected Boolean doInBackground(String... params) {
            String username = params[0];
            if(friendRequestList == null){
                friendRequestList = new ArrayList<>();
            }else{
                friendRequestList.clear();
            }

            boolean newFriends = false;

            List<ParseQuery<FriendRequest>> queries = new ArrayList<>();
            //find rows where you sent request
            ParseQuery<FriendRequest> query1 = ParseQuery.getQuery("FriendRequest");
            query1.whereEqualTo("reqFrom",username);
            //find rows where requests were sent to you ReqTo
            ParseQuery<FriendRequest> query2 = ParseQuery.getQuery("FriendRequest");
            query2.whereEqualTo("reqTo", username);

            queries.add(query1);
            queries.add(query2);

            ParseQuery<FriendRequest> finalQuery = ParseQuery.or(queries);


            try {
                friendRequestList = finalQuery.find();
                for(FriendRequest fr : friendRequestList){
                    boolean newFriendbool = false;

                    String reqFrom, reqTo, fstatus, fObjectId, friendName = "";

                    fObjectId = fr.getObjectId();
                    fstatus = fr.getString("status");
                    reqTo = fr.getString("reqTo");
                    reqFrom = fr.getString("reqFrom");

                    //profile check
                    int pTo = profileExists(reqTo);
                    int pFrom = profileExists(reqFrom);

                    Log.v("CheckStatus", "From: " + reqFrom + ", To: "+ reqTo + ", stat: "+ fstatus + " objectID: " + fObjectId);

                    //Add to database if the sender is not from the user or if the user sent
                    //If reqfrom = current user and status is request, show pending
                    //Add usernames that do not match current user

                    if( pTo < 0 && !currentUser.equalsIgnoreCase(reqTo)){
                        reqType = reqTo;
                        newFriends = true;
                        newFriendbool = true;
                        friendName = reqTo;

                        //if fstatus is request, make it pending (since reqFrom is current user)
                        if (fstatus.equalsIgnoreCase("request")) {
                            fstatus = "pending";
                        }

                    }else if(pFrom < 0 && !currentUser.equalsIgnoreCase(reqFrom)) {
                        reqType = reqFrom;
                        newFriends = true;
                        newFriendbool = true;
                        friendName = reqFrom;
                    }else {
                        int pos = ((pTo > pFrom) ? pTo : pFrom);

                        FriendProfile temprofile = friendNames.get(pos);
                        String tempStat = temprofile.getStatus();
                        if( !(fstatus.equalsIgnoreCase("request") && (tempStat.equalsIgnoreCase("pending") || tempStat.equalsIgnoreCase("request") ))){

                            temprofile.setStatus(fstatus);
                            //Update status in db
                            dbhandler.changeFriendStatus(temprofile.getFriendRequestId(), fstatus);
                            newFriends = true;

                        }

                    }

                    if(newFriendbool){//if new friend, add to db
                        ParseQuery query = ParseUser.getQuery();
                        query.whereEqualTo("username", friendName);
                        List<ParseUser> list = query.find();
                        for(ParseUser friendObject: list){

                            dbhandler.createFriend(fObjectId, friendObject.getObjectId(), reqType, "", fstatus);
                            FriendProfile fp = new FriendProfile(fObjectId, friendObject.getObjectId(), reqType, "", fstatus);
                            Log.v("AddingFriend", "Adding friend");
                            friendNames.add(fp);
                        }

                    }

                }


            } catch (ParseException e) {
                e.printStackTrace();
            }

            return newFriends;
        }

        @Override
        protected void onPostExecute(Boolean newFriends){
            //Add new friends to db
            if(newFriends){
                Log.v("Add", "populating list again");
                populateFriendList();
            }

        }
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
                Toast.makeText(context, "Friend not found", Toast.LENGTH_SHORT).show();

            }else{
                final String friendUserName = puser.getUsername();
                Log.v("searchFriend", "Found friend: "+friendUserName);
                searchedFriend.setVisibility(View.VISIBLE);
                searchedFriend.setText(friendUserName);
                searchedFriend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchedFriend.setText("");
                        searchedFriend.setVisibility(View.GONE);
                        //Check to see if already exists in database
                        if(profileExists(friendUserName) >= 0){
                            Toast.makeText(context, "Already added", Toast.LENGTH_SHORT).show();
                        }else{
                            //Send Friend request
                            sendRequest(friendUserName);
                        }
                    }
                });
            }

        }
    }




}
