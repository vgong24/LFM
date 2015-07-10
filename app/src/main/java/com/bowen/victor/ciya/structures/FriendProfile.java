package com.bowen.victor.ciya.structures;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendProfile {
    private String friendRequestid, fusername, realName, status, userId;

    public FriendProfile(String requestId,String userid, String username, String realName, String status){
        friendRequestid = requestId;
        userId = userid;
        fusername = username;
        this.realName = realName;
        this.status = status;
    }

    public String getFriendRequestId(){
        return friendRequestid;
    } //NOT USERID but FRIEND REQUEST ID
    public String getUserId(){
        return userId;
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
    public void setStatus(String status){
        this.status = status;
    }

}
