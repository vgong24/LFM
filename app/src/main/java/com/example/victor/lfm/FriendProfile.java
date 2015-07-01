package com.example.victor.lfm;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendProfile {
    private String _userObjectId, fusername, realName;

    public FriendProfile(String userid, String username, String realName){
        _userObjectId = userid;
        fusername = username;
        this.realName = realName;
    }

    public String getUserId(){
        return _userObjectId;
    }
    public String getUserName(){
        return fusername;
    }
    public String getRealName(){
        return realName;
    }

}
