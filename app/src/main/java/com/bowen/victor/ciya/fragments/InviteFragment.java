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
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.bowen.victor.ciya.dbHandlers.FriendListDBHandler;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.structures.Events;
import com.bowen.victor.ciya.structures.FriendRequest;
import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.adapters.InviteListAdapter;
import com.bowen.victor.ciya.structures.FriendProfile;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Similar layout to FriendsTab but removed nonessentials
 *
 *
 * Created by Victor on 7/9/2015.
 */
public class InviteFragment extends Fragment {
    Context context;
    Activity activity;
    View v;

    TextView searchedFriend;
    ListView friendlv;

    List<FriendProfile> friendNames;
    FriendListDBHandler dbhandler;
    InviteListAdapter profileAdapter;

    String currentUser;
    String reqType;

    List<FriendRequest> friendRequestList;

    public static InviteFragment newInstance(Context context, String eventID){
        InviteFragment fragment = new InviteFragment();
        Bundle args = new Bundle();
        args.putString("eventId", eventID);
        fragment.setArguments(args);
        fragment.context = context;
        fragment.activity = (Activity) context;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Use the layout of Friends tab because its pretty much the same
        //Only difference is the items
        v = inflater.inflate(R.layout.invite_tab, container, false);
        initialize();
        //populateFriendList();
        //setupSearchView();
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
        populateFriendList();
        setupSearchView();

    }

    //Display Friendlist then set up onclick listeners
    public void populateFriendList(){
        profileAdapter = new InviteListAdapter(context, R.layout.invite_item, friendNames);
        friendlv.setAdapter(profileAdapter);
        onFriendClick();
    }
    public void onFriendClick(){
        friendlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FriendProfile fp = friendNames.get(position);
                final String eventid = getArguments().getString("eventId");
                //Check to see if invite was sent then send it
                checkSendInvite(fp.getUserId(), eventid);
                view.setEnabled(false);
            }
        });
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

    /**
     * Check to see if invitee is already an attendee for the event
     * If so, let the user know. If not, send out an invitation
     * @param userId
     * @param eventid
     */
    public void checkSendInvite(final String userId, final String eventid){
        ParseQuery<Attendee> query = ParseQuery.getQuery("Attendees");
        ParseObject friendUser = ParseObject.createWithoutData("_User", userId);
        ParseObject eventPointer = ParseObject.createWithoutData("Events", eventid);

        query.whereEqualTo("User", friendUser);
        query.whereEqualTo("Event", eventPointer);
        query.findInBackground(new FindCallback<Attendee>() {
            @Override
            public void done(List<Attendee> list, ParseException e) {
                if (list.size() > 0) {
                    Toast.makeText(context, "Already invited", Toast.LENGTH_SHORT).show();
                } else {
                    sendInvite(eventid, userId);
                }
            }
        });

    }

    public void sendInvite(String eventJoining, String invitee){
        Toast.makeText(context, "Invite Sent", Toast.LENGTH_SHORT).show();
        Attendee attend = new Attendee();
        attend.setEvent(eventJoining);
        attend.setUser(invitee);
        attend.setAttendeeStatus(Attendee.INVITED);
        attend.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                //once clicked, refresh page
                //new SetUpBackground().execute(evnt);
            }
        });
    }

    //Async display username results
    //Searches for a friend then will display friend name in listview
    class SearchForFriend extends AsyncTask<String, Void, ParseUser> {

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
                Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();

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
                        String eventid = getArguments().getString("eventId");
                        checkSendInvite(puser.getObjectId(), eventid);
                    }
                });
            }

        }
    }



}
