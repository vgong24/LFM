package com.example.victor.lfm;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;

@ParseClassName("Category")
public class Category extends ParseObject {
    public void setName(String activity) {
        put("name", activity);
    }
    public String getName() {
        return getString("Name");
    }
    public byte[] getImage() {
        return getBytes("image");
    }
    public void setImage(byte [] image) {
        put("image", image);
    }
    public static ParseQuery<Category> getQuery() {
        return ParseQuery.getQuery(Category.class);
    }

}
