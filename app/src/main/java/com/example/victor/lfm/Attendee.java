package com.example.victor.lfm;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;

@ParseClassName("Attendee")
public class Attendee extends ParseObject {
    public void setAttend(boolean here) {
        put("Attended", here);
    }
    public boolean getAttend() {
        return getBoolean("Attended");
    }
    public String getUser() {
        return getString("User");
    }
    public void setUser(String id) {
        put("User", id);
    }
    public String getEventID() {
        return getString("Event");
    }
    public void setEventID(String id) {
        put("Event", id);
    }
    public static ParseQuery<Attendee> getQuery() {
        return ParseQuery.getQuery(Attendee.class);
    }

}
