package com.example.victor.lfm;

import android.net.Uri;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Victor on 6/14/2015.
 * Resource: http://stackoverflow.com/questions/12460471/how-to-send-a-google-places-search-request-with-java
 */
public class PlacesAPI {
    private static final String LOG_TAG = "Autocomplete";

    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String TYPE_DETAILS = "/details";
    private static final String TYPE_SEARCH = "/search";

    private static final String OUT_JSON = "/json";
    private static final String API_KEY = "AIzaSyCP6IKn015BJ-pwpuGayJtuDGwJuT5oi9I";

    public static ArrayList<PlaceDetails> autocomplete(String input){
        ArrayList<PlaceDetails> resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try{
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_AUTOCOMPLETE);
            sb.append(OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            sb.append("&sensor=false");

            URL url = new URL(sb.toString());
            Log.v("autoComplete", "url: "+ sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            //Load results into StringBuilder
            int read;
            char[] buff = new char[1024];
            while((read = in.read(buff)) != -1){
                jsonResults.append(buff, 0, read);
            }

        }catch (MalformedURLException e){
            Log.e(LOG_TAG, "Error processing Places API URL", e);
            return resultList;
        }catch(IOException e){
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return resultList;

        } finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        PlaceDetails place = null;
        try{
            //Create a JSON object heiarchy from results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            //Extract the places descriptions from the results
            resultList = new ArrayList<PlaceDetails>(predsJsonArray.length());
            for(int i = 0; i < predsJsonArray.length(); i++){
                place = new PlaceDetails();

                place.name = predsJsonArray.getJSONObject(i).getString("description");
                Log.v("Location", "Description: " + predsJsonArray.getJSONObject(i).getString("description"));

                resultList.add(place);
            }

        }catch(JSONException e){
            Log.e(LOG_TAG, "Cannot process JSon results", e);
        }
        return resultList;
    }
}
