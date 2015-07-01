package com.example.victor.lfm;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendProfile {
    private String _userObjectId, fusername, realName, status;

    public FriendProfile(String userid, String username, String realName, String status){
        _userObjectId = userid;
        fusername = username;
        this.realName = realName;
        this.status = status;
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
    public String getStatus(){
        return status;
    }

}
