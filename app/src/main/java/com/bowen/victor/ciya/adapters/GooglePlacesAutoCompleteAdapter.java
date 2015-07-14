package com.bowen.victor.ciya.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bowen.victor.ciya.structures.PlaceDetails;
import com.bowen.victor.ciya.tools.PlacesAPI;
import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.tools.PlacesAPI_v2;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

/**
 * Created by Victor on 6/14/2015.
 */
public class GooglePlacesAutoCompleteAdapter extends ArrayAdapter<PlaceDetails> implements Filterable{
    private ArrayList<PlaceDetails> resultList;
    //PlacesAPI mPlaceAPI= new PlacesAPI();
    PlacesAPI_v2 mPlaceAPI;
    Context mContext;
    int mResource;
    GoogleApiClient mGoogleApiClient;

    public GooglePlacesAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mResource = resource;
        //mPlaceAPI = new PlacesAPI_v2(mContext);
    }
    public GooglePlacesAutoCompleteAdapter(Context context, int resource, GoogleApiClient client) {
        super(context, resource);
        mContext = context;
        mResource = resource;
        mGoogleApiClient = client;
        mPlaceAPI = new PlacesAPI_v2(mContext, mGoogleApiClient);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        //Get data from position
        PlaceDetails place = resultList.get(position);
        //Check if existing view is being reused
        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false);
        }
        //Lookup view for data population
        TextView autocompleteView = (TextView) convertView.findViewById(R.id.autocompleteText);
        autocompleteView.setText(place.getName());

        return convertView;
    }

    @Override
    public int getCount(){
        return resultList.size();
    }
    //
    @Override
    public PlaceDetails getItem(int index){
        if(resultList.isEmpty()){
            return null;
        }
        return resultList.get(index);
    }

    @Override
    public Filter getFilter(){
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){
                    Log.v("Constraint", "input: " + constraint.toString());
                    //Retrieve the autocomplete results
                    resultList = mPlaceAPI.autocomplete(constraint.toString());

                    /*
                    ArrayList<String> resultNames = new ArrayList<>();
                    //Grab the name of the result rather than object reference
                    for(int i = 0 ; i < resultList.size(); i++){
                        resultNames.add(resultList.get(i).getName().toString());
                    }*/

                    //Assign the data to the filterresults
                    filterResults.values = resultList;
                    filterResults.count = resultList.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0){
                    notifyDataSetChanged();
                }else{
                    notifyDataSetInvalidated();
                }
            }
        };
        return filter;
    }

    //Disconnect Places API v 21


}
