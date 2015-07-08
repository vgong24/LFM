package com.bowen.victor.ciya;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;

@ParseClassName("Reviews")
public class Reviews extends ParseObject {
    public void setRating(int rate) {
        put("Rating", rate);
    }
    public int getRating() {
        return getInt("rating");
    }
    public String getUser() {
        return getString("User");
    }
    public void setUser(String id) {
        put("User", id);
    }
    public String getReviewee() {
        return getString("Reviewee");
    }
    public void setReviewee(String id) {
        put("Reviewee", id);
    }
    public String getEventID() {
        return getString("event");
    }
    public void setEventID(String id) {
        put("event", id);
    }
    public static ParseQuery<Reviews> getQuery() {
        return ParseQuery.getQuery(Reviews.class);
    }

}
