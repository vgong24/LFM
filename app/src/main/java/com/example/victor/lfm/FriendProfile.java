package com.example.victor.lfm;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendProfile {
    private String userObjectId, fusername, realName, status;

    public FriendProfile(String userid, String username, String realName, String status){
        userObjectId = userid;
        fusername = username;
        this.realName = realName;
        this.status = status;
    }

    public String getUserId(){
        return userObjectId;
    } //NOT USERID but FRIEND REQUEST ID
    public String getUserName(){
        return fusername;
    }
    public String getRealName(){
        return realName;
    }
    public String getStatus(){
        return status;
    }
    public void setStatus(String status){
        this.status = status;
    }

}
