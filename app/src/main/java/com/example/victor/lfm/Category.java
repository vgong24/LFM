package com.example.victor.lfm;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;

@ParseClassName("Category")
public class Category extends ParseObject {
    public void setName(String activity) {
        put("Name", activity);
    }
    public String getName() {
        try {
            this.fetchIfNeeded();
        } catch (ParseException e) {

        }
        return getString("Name");
    }
    public ParseFile getImage() {
        try {
            this.fetchIfNeeded();
        } catch (ParseException e) {

        }
        return getParseFile("Logo");
    }
    public void setImage(byte [] image) {
        put("Logo", image);
    }
    public static ParseQuery<Category> getQuery() {
        return ParseQuery.getQuery(Category.class);
    }

}
