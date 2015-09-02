package com.bowen.victor.ciya.tools;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import com.bowen.victor.ciya.structures.PlaceDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.PendingResults;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * Created by Victor on 7/14/2015.
 */
public class PlacesAPI_v2 implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private Context context;
    private ArrayList<PlaceDetails> placesList;
    private LatLngBounds mLatLngBounds;

    public PlacesAPI_v2(Context context){
        this.context = context;
        placesList = new ArrayList<>();
        setUpApiClient();
    }

    public PlacesAPI_v2(Context context, GoogleApiClient mclient, LatLngBounds mLatLngBounds){
        this.context = context;
        mGoogleApiClient = mclient;
        this.mLatLngBounds = mLatLngBounds;
    }

    private void setUpApiClient(){
        if(mGoogleApiClient == null){
            mGoogleApiClient = new GoogleApiClient.Builder(context)
            .addApi(Places.GEO_DATA_API)
            .addApi(Places.PLACE_DETECTION_API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();
        }
    }

    public ArrayList<PlaceDetails> autocomplete(String input){
        if(!mGoogleApiClient.isConnected()){
            Log.e("AUTO ERR", "Google API client is not connected for autocomplete query.");
            return null;
        }
        PlaceDetails details = null;
        PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi.getAutocompletePredictions(mGoogleApiClient, input, mLatLngBounds, null);
        AutocompletePredictionBuffer autocompletePredictions = results.await(60, TimeUnit.SECONDS);

        final Status status = autocompletePredictions.getStatus();
        if(!status.isSuccess()){
            //Error
            Log.e("AUTOCOMPLETE", "Error getting autocomplete prediction API call: " + status.toString());
            autocompletePredictions.release();
            return null;
        }
        Log.i("AUTOCOMPLETE", "Query completed. Received " + autocompletePredictions.getCount()
                + " predictions.");
        placesList = new ArrayList<>(autocompletePredictions.getCount());

        Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
        while(iterator.hasNext()){
            AutocompletePrediction prediction = iterator.next();
            placesList.add(new PlaceDetails(prediction.getDescription(), prediction.getPlaceId()));
        }
        autocompletePredictions.release();

        return placesList;
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void disconnect(){
        mGoogleApiClient.disconnect();
    }
}
