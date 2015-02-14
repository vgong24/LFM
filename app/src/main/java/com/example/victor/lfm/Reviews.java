package com.example.victor.lfm;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.ParseObject;
import com.parse.ParseQuery;

public class Reviews extends ParseObject {
    public void setRating(int rate) {
        put("rating", rate);
    }
    public int getRating() {
        return getInt("rating");
    }
    public String getUser() {
        return getString("userid");
    }
    public void setUser(String id) {
        put("userid", id);
    }
    public String getReviewee() {
        return getString("reviewee");
    }
    public void setReviewee(String id) {
        put("reviewee", id);
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
