package com.example.victor.lfm;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by Victor on 3/19/2015.
 */

@ParseClassName("Assets")
public class Assets extends ParseObject {
    public void setImgRef(String imgName) {
        put("imageRef", imgName);
    }
    public String getImgRef() {
        try {
            this.fetchIfNeeded();
        } catch (ParseException e) {

        }
        return getString("imageRef");
    }
    public ParseFile getImage() {
        try {
            this.fetchIfNeeded();
        } catch (ParseException e) {

        }
        return getParseFile("image");
    }
    public void setImage(byte [] image) {
        put("image", image);
    }


    public static ParseQuery<Assets> getQuery() {
        return ParseQuery.getQuery(Assets.class);
    }

}
