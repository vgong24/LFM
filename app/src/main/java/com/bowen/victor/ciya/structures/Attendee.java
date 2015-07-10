package com.bowen.victor.ciya.structures;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;

@ParseClassName("Attendees")
public class Attendee extends ParseObject {
    public final static String JOINED = "joined";
    public final static String KICKED = "kicked";
    public final static String INVITED = "invited";

    public void setAttend(boolean here) {
        put("Attended", here);
    }
    public boolean getAttend() {
        return getBoolean("Attended");
    }
    public String getUserString() {
        return getString("User");
    }
    public String getUserFirstName(){
        return getUserString();
    }
    public _User getUserObject(){
        try {
            this.fetchIfNeeded();
        } catch (ParseException e) {

        }
        return (_User) getParseObject("User");
    }
    public ParseUser getUserID(){
        return (ParseUser) getParseUser("User");
    }
    public String getInviteStatus(){
        return getString("inviteStatus");
    }
    public void setAttendeeStatus(String status){
        put("inviteStatus", status);
    }
    public void setInviteStatus(String status){
        setAttendeeStatus(status);
    }

    public void setUser(String id) {
        put("User", ParseObject.createWithoutData("_User",id));
    }
    public void setUser(_User user){
        put("User", user);
    }

    public String getEventID() {
        return getString("Event");
    }
    public Events getEventObject(){return (Events) getParseObject("Event");}
    public void setEvent(String id) {
        put("Event", ParseObject.createWithoutData("Events", id));
    }
    public void setEvent(Events e){
        put("Event", e);
    }

    public static ParseQuery<Attendee> getQuery() {
        return ParseQuery.getQuery(Attendee.class);
    }

}
