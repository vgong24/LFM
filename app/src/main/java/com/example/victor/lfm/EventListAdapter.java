package com.example.victor.lfm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 3/18/2015.
 */
public class EventListAdapter extends ArrayAdapter<Events> {
    int viewListXML;
    ArrayList<Events> eventArray;
    Context context;

    public EventListAdapter(Context context, int viewListXML, ArrayList<Events> eventArray){//Example R.layout.event_list_item, events
        super(context, viewListXML, eventArray);
        this.viewListXML = viewListXML;
        this.eventArray = eventArray;
        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        if(view == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(viewListXML, parent, false);
        }
        Events currentEvent = eventArray.get(position);

        TextView capacity = (TextView) view.findViewById(R.id.eventCapacityView);
        capacity.setText(currentEvent.getMax()+"");

        TextView activity = (TextView) view.findViewById(R.id.eventActivityView);
        activity.setText(currentEvent.getDescr());

        TextView location = (TextView) view.findViewById(R.id.eventLocationView);
        location.setText("Honolulu");

        TextView date = (TextView) view.findViewById(R.id.eventTimeView);
        SimpleDateFormat sdf = new SimpleDateFormat();
        date.setText(sdf.format(currentEvent.getDate().getTime()));

        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        ParseFile thumbnail = null;
        if((thumbnail = currentEvent.getCat().getImage()) != null){
            thumbnail.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                        if (e == null) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                            if (bmp != null) {
                                Bitmap resizedbitmap = Bitmap.createScaledBitmap(bmp, 100, 100, true);
                                imageView.setImageBitmap(resizedbitmap);
                            }
                        } else {
                            Log.e("paser after download", "null");

                        }
                }
            });

        }else {
            Log.e("parse file", " null");
            imageView.setPadding(10,10,10,10);

        }

        return view;

    }

}