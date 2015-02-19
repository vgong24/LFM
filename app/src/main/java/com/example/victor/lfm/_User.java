package com.example.victor.lfm;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by Victor on 2/18/2015.
 */
@ParseClassName("_User")
public class _User extends ParseObject {
    public String getFirstName(){
        return getString("firstName");
    }

}
