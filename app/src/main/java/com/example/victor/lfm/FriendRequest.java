package com.example.victor.lfm;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

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
    public static void approveFriendRequest(String requester){

    }
}
