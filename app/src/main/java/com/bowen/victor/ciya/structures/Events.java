package com.bowen.victor.ciya.structures;

/**
 * Created by Victor on 2/14/2015.
 */

import com.parse.*;


import java.util.Date;
@ParseClassName("Events")
public class Events extends ParseObject  {
    public void setHost(String user) {
        put("Host", user);
    }
    public ParseUser getHost() {
        try{
            this.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return (ParseUser) getParseObject("Host");

    }

    public Category getCat() {
        try {
            this.fetchIfNeeded();
        } catch (ParseException e) {

        }
        return (Category) getParseObject("Category");
    }
    public String getPrivacy(){
        return getString("privacy");
    }
    public void setCat(String cat) {
        put("Category", cat);
    }
    public Date getDate() {
        try {
            this.fetchIfNeeded();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return(Date) getDate("Date");
    }
    public void setDate(Date date) {
        put("Date", date);
    }
    public String getDescr() {
        return getString("Description");
    }
    public void setDescr(String descr) {
        put("Description", descr);
    }
    public int getMax() {
        return getInt("Max");
    }
    public void setMax(int max) {
        put("Max", max);
    }
    public ParseGeoPoint getLocation(){return getParseGeoPoint("Location");}
    public static ParseQuery<Events> getQuery() {
        return ParseQuery.getQuery(Events.class);
    }

}
