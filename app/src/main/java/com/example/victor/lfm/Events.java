package com.example.victor.lfm;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;


import java.util.Date;
@ParseClassName("Events")
public class Events extends ParseObject  {
    public void setHost(String user) {
        put("host", user);
    }
    public int getHost() {
        return getInt("host");
    }
    public Category getCat() {
        return (Category) getParseObject("category");
    }
    public void setCat(String cat) {
        put("category", cat);
    }
    public Date getDate() {
        return getDate("date");
    }
    public void setDate(Date date) {
        put("date", date);
    }
    public String getDescr() {
        return getString("descr");
    }
    public void setDescr(String descr) {
        put("descr", descr);
    }
    public int getMax() {
        return getInt("max");
    }
    public void setMax(int max) {
        put("max", max);
    }
    public static ParseQuery<Events> getQuery() {
        return ParseQuery.getQuery(Events.class);
    }

}
