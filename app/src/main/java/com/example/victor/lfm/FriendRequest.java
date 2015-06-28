package com.example.victor.lfm;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseUser;

/**
 * Created by Victor on 6/27/2015.
 */
@ParseClassName("FriendRequest")
public class FriendRequest extends ParseObject {


    //Send friend request
    public static void sendFriendRequest(String reqFrom, String reqTo){
        ParseObject friendReq = ParseObject.create("FriendRequest");

    }
}
