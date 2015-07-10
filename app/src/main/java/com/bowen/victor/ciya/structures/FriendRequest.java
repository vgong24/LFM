package com.bowen.victor.ciya.structures;

import android.util.Log;

import com.parse.GetCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Victor on 6/27/2015.
 */
@ParseClassName("FriendRequest")
public class FriendRequest extends ParseObject {
    private final static String REQUEST = "request",
    APPROVED = "approve",
    REMOVED = "removed";


    //Send friend request
    public static void sendFriendRequest(String reqFrom, String reqTo){
        ParseObject friendReq = ParseObject.create("FriendRequest");
        friendReq.put("reqFrom", reqFrom);
        friendReq.put("reqTo", reqTo);
        friendReq.put("status", REQUEST);

        friendReq.saveInBackground();

    }

    //accept friend request
    public static void approveFriendRequest(String requestId){
        ParseQuery query = ParseQuery.getQuery("FriendRequest");
        Log.v("GETID: ", requestId + "");
        query.whereEqualTo("objectId", requestId);
        query.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                parseObject.put("status", APPROVED);
                parseObject.saveInBackground();
            }
        });
    }
}
