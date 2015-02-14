package com.example.victor.lfm;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;

@ParseClassName("Category")
public class Category extends ParseObject {
    public void setCat(String activity) {
        put("name", activity);
    }
    public String getCat() {
        return getString("name");
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
