package com.bowen.victor.ciya.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bowen.victor.ciya.structures.Events;
import com.bowen.victor.ciya.R;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Victor on 3/18/2015.
 */
public class EventListAdapter extends ArrayAdapter<Events> {
    int viewListXML;
    ArrayList<Events> eventArray;
    Context context;
    private final double BITMAP_SCALE = 9;

    public EventListAdapter(Context context, int viewListXML, ArrayList<Events> eventArray){//Example R.layout.event_list_item, events
        super(context, viewListXML, eventArray);
        this.viewListXML = viewListXML;
        this.eventArray = eventArray;
        this.context = context;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        ViewHolder holder;

        if(view == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(viewListXML, parent, false);
            holder = new ViewHolder();

            holder.capacity = (TextView) view.findViewById(R.id.event_item_capacity);
            holder.description = (TextView) view.findViewById(R.id.event_item_title);
            //holder.location = (TextView) view.findViewById(R.id.eventLocationView);
            holder.date = (TextView) view.findViewById(R.id.event_item_time);
            holder.imageView = (ImageView) view.findViewById(R.id.event_item_image);

            view.setTag(holder);

        }else{
            holder = (ViewHolder) view.getTag();
        }
        Events currentEvent = eventArray.get(position);
        holder.capacity.setText(currentEvent.getMax()+"");
        holder.description.setText(currentEvent.getDescr());
        //Depending on layout
        if(holder.location != null)
            holder.location.setText("Honolulu");


        SimpleDateFormat sdf = new SimpleDateFormat();
        long eventTime = currentEvent.getDate().getTime();
        sdf.applyLocalizedPattern("M/d/yy");
        String date = sdf.format(eventTime);
        sdf.applyLocalizedPattern("h:mm a");
        String time = sdf.format(eventTime);

        //Check if time is today
        if(DateUtils.isToday(eventTime)){
            holder.date.setText("Today at "+ time);
        }else {

            holder.date.setText(date +" at "+time);
        }

        if(holder.imageView != null){
            new ImageDownloaderTask(holder.imageView).execute(currentEvent);
        }
        return view;

    }

    static class ViewHolder {
        TextView capacity;
        TextView description;
        TextView location;
        TextView date;
        ImageView imageView;

    }

    class ImageDownloaderTask extends AsyncTask<Events, Void, Bitmap>{
        private final WeakReference<ImageView> imageViewWeakReference;

        public ImageDownloaderTask(ImageView imageView){
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
        }


        @Override
        protected Bitmap doInBackground(Events... params) {

            ParseFile thumbnail = null;
            if((thumbnail = params[0].getCat().getImage()) != null){
                try {
                    byte[] data = thumbnail.getData();
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    if (bmp != null) {
                        //Scale bitmaps based on Device width
                        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                        Display display = wm.getDefaultDisplay();
                        Point size = new Point();
                        display.getSize(size);

                        int width = size.x;
                        int bitmapScale = (int) (width / BITMAP_SCALE);
                        Bitmap resizedbitmap = Bitmap.createScaledBitmap(bmp, bitmapScale, bitmapScale, true);
                        return resizedbitmap;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }



            }else {
                Log.e("parse file", " null");
                //imageView.setPadding(10,10,10,10);

            }
            return null;
        }
        @Override
        protected void onPostExecute(Bitmap bitmap){
            if(isCancelled()){
                bitmap = null;
            }
            if(imageViewWeakReference != null){
                ImageView imageView = imageViewWeakReference.get();
                if(imageView != null){
                    if(bitmap != null){
                        imageView.setImageBitmap(bitmap);
                    }else{
                        Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.reload);
                        imageView.setImageDrawable(placeholder);
                        imageView.setPadding(10,10,10,10);
                    }
                }
            }
        }

    }


}