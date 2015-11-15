package com.bowen.victor.ciya.structures;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendProfile implements Comparable<FriendProfile>{
    private String friendRequestid, fusername, realName, status, userId;
    private byte[] profilePic;
    boolean clicked;


    public FriendProfile(String requestId,String userid, String username, String realName, String status){
        friendRequestid = requestId;
        userId = userid;
        fusername = username;
        this.realName = realName;
        this.status = status;
    }
    public FriendProfile(String requestId,String userid, String username, String realName, String status, byte[] imageBytes){
        friendRequestid = requestId;
        userId = userid;
        fusername = username;
        this.realName = realName;
        this.status = status;
        this.profilePic = imageBytes;
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
    public byte[] getImageBytes(){
        return profilePic;
    }

    /**
     * TODO: Compare the displaying name for the friends list
     * If user discloses full names, dynamically compare displaying usernames with displaying fullnames
     * @param another
     * @return
     */
    @Override
    public int compareTo(FriendProfile another) {

        int result = this.getUserName().compareToIgnoreCase(another.getUserName());

        if(result > 0 ){
            return 1;
        }else if(result < 0){
            return -1;
        }
        return 0;

    }
}
