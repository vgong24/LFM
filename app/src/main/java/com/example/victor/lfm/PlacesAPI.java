package com.example.victor.lfm;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

                JSONObject jObject = predsJsonArray.getJSONObject(i);
                place.name = jObject.getString("description");
                place.place_id = jObject.getString("place_id");
                Log.v("Location", "Description: " + predsJsonArray.getJSONObject(i).getString("description"));

                resultList.add(place);
            }

        }catch(JSONException e){
            Log.e(LOG_TAG, "Cannot process JSon results", e);
        }
        return resultList;
    }

    public static LatLng searchLocationById(String placeId){
        LatLng placeLocation = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();

        try{
            StringBuilder sb = new StringBuilder(PLACES_API_BASE);
            sb.append(TYPE_DETAILS);
            sb.append(OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&placeid="+placeId);


            URL url = new URL(sb.toString());
            Log.v("locationSearch", "url: "+ sb.toString());
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
            return placeLocation;
        }catch(IOException e){
            Log.e(LOG_TAG, "Error connecting to Places API", e);
            return placeLocation;

        } finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        try{
            Log.v("LATLNG", "Entering LatLng part");


            //Create a JSON object heiarchy from results

            JSONObject jsonObj = new JSONObject(jsonResults.toString());

            JSONObject jResult = jsonObj.getJSONObject("result");


            JSONObject jGeometry = jResult.getJSONObject("geometry");
            JSONObject jLocation = jGeometry.getJSONObject("location");

            double jlat = jLocation.getDouble("lat");
            double jlng = jLocation.getDouble("lng");
            Log.v("LATLNG", jlat + " " + jlng);

            placeLocation = new LatLng(jlat, jlng);



        }catch(JSONException e){
            Log.e(LOG_TAG, "Cannot process JSon results", e);
        }
        return placeLocation;
    }

    //Uses the place_id to find the Lat Lng position of the Place
    //Then moves the camera to selected location
    public void getLatLngByID(GoogleMap gmap, String pid){
        new GetLatLngTask(gmap).execute(pid);
    }

    class GetLatLngTask extends AsyncTask<String, Void, LatLng>{
        private GoogleMap gmap;
        LatLng placeLocation;
        private final int ZOOM_DISTANCE = 13;

        public GetLatLngTask(GoogleMap map){
            gmap = map;
        }

        @Override
        protected LatLng doInBackground(String... params) {
            String placeid = params[0];
            placeLocation = searchLocationById(placeid);

            return placeLocation;
        }

        @Override
        protected void onPostExecute(LatLng ll){
            gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(ll, ZOOM_DISTANCE));

        }
    }
}
