package com.example.victor.lfm;

import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * Created by Victor on 2/18/2015.
 */
@ParseClassName("_User")
public class _User extends ParseObject {
    public String getFirstName(){
        return getString("firstName");
    }

    public ParseFile getImage() {
        try {
            this.fetchIfNeeded();
        } catch (ParseException e) {

        }
        return getParseFile("profilePicture");
    }

    public static ParseQuery<_User> getQuery() {
        return ParseQuery.getQuery(_User.class);
    }

}
