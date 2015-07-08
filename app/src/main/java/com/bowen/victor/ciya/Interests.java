package com.bowen.victor.ciya;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;

@ParseClassName("Interests")
public class Interests extends ParseObject {
    public void setCat(String cat) {
        put("Category", cat);
    }
    public int getCat() {
        return getInt("Category");
    }
    public String getUser() {
        return getString("User");
    }
    public void setUser(String id) {
        put("User", id);
    }
    public static ParseQuery<Interests> getQuery() {
        return ParseQuery.getQuery(Interests.class);
    }

}
