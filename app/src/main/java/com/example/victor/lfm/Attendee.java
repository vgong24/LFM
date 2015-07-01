package com.example.victor.lfm;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;

@ParseClassName("Attendees")
public class Attendee extends ParseObject {
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
    public void setEventID(String id) {
        put("Event", id);
    }
    public void setEvent(Events e){
        put("Event", e);
    }
    public static ParseQuery<Attendee> getQuery() {
        return ParseQuery.getQuery(Attendee.class);
    }

}
