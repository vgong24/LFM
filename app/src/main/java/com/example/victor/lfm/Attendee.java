package com.example.victor.lfm;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;

@ParseClassName("Attendee")
public class Attendee extends ParseObject {
    public void setAttend(boolean here) {
        put("attend", here);
    }
    public boolean getAttend() {
        return getBoolean("here");
    }
    public String getUser() {
        return getString("userid");
    }
    public void setUser(String id) {
        put("userid", id);
    }
    public String getEventID() {
        return getString("event");
    }
    public void setEventID(String id) {
        put("event", id);
    }
    public static ParseQuery<Attendee> getQuery() {
        return ParseQuery.getQuery(Attendee.class);
    }

}
