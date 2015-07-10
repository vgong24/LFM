package com.bowen.victor.ciya.fragments;

import android.app.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.bowen.victor.ciya.dbHandlers.FriendListDBHandler;
import com.bowen.victor.ciya.structures.FriendRequest;
import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.adapters.InviteListAdapter;
import com.bowen.victor.ciya.structures.FriendProfile;
import com.parse.ParseUser;

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

    public static InviteFragment newInstance(Context context){
        InviteFragment fragment = new InviteFragment();

        fragment.context = context;
        fragment.activity = (Activity) context;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //Use the layout of Friends tab because its pretty much the same
        //Only difference is the items
        v = inflater.inflate(R.layout.friends_tab, container, false);
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

    }

    //Display Friendlist then set up onclick listeners
    public void populateFriendList(){
        profileAdapter = new InviteListAdapter(context, R.layout.invite_item, friendNames);
        friendlv.setAdapter(profileAdapter);
        //onFriendClick();
    }


}
