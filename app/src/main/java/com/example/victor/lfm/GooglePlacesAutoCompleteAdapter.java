package com.example.victor.lfm;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

/**
 * Created by Victor on 6/14/2015.
 */
public class GooglePlacesAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable{
    private ArrayList<String> resultList;
    PlacesAPI mPlaceAPI= new PlacesAPI();
    Context mContext;
    int mResource;

    public GooglePlacesAutoCompleteAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mResource = resource;
    }

    @Override
    public int getCount(){
        return resultList.size();
    }

    @Override
    public String getItem(int index){
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


}
