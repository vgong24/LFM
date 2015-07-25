package com.bowen.victor.ciya.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;


import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.activities.EventDetails;
import com.bowen.victor.ciya.fragments.FragmentDrawer;
import com.bowen.victor.ciya.structures.Events;
import com.parse.ParseException;
import com.parse.ParseFile;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Victor on 7/11/2015.
 */
public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.ViewHolder> {
    private List<Events> mDataset;
    private final double BITMAP_SCALE = 9;
    Context context;
    private int lastPosition = -1;



    // Provide a suitable constructor (depends on the kind of dataset)
    public EventRecyclerAdapter(Context context, List<Events> myDataset) {
        mDataset = myDataset;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public EventRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item_reddit, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v, context);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Events events = mDataset.get(position);
        holder.eventClick = events;
        holder.capacity.setText(events.getMax()+"");
        holder.description.setText(events.getDescr());
        SimpleDateFormat sdf = new SimpleDateFormat();
        long eventTime = events.getDate().getTime();
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
            new ImageDownloaderTask(holder.imageView).execute(events);
        }

        //Set onclicks
        holder.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position, boolean isLongClick) {
                EventDetails.startEventDetails(context, events);
            }
        });

        //Set up animation
        setAnimation(holder.mView, position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    /**
     * Animate list population
     */
    private void setAnimation(View viewToAnimate, int position){
        // If the bound view wasn't previously displayed on screen, it's animated
        /*
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            animation.setDuration(500);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }*/

    }

    /** VIEW HOLDER
     * Provide a reference to the views for each data item
    * Complex data items may need more than one view per item, and
    * you provide access to all the views for a data item in a view holder
    */

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        // each data item is just a string in this case
        public View mView;
        public TextView capacity;
        public TextView description;
        public TextView date;
        public ImageView imageView;
        public Events eventClick;
        public Context context;
        private ClickListener clickListener;

        public ViewHolder(View v, final Context context) {
            super(v);
            mView = v;
            capacity = (TextView) v.findViewById(R.id.event_item_capacity);
            description = (TextView) v.findViewById(R.id.event_item_title);
            date = (TextView) v.findViewById(R.id.event_item_time);
            imageView = (ImageView) v.findViewById(R.id.event_item_image);
            this.context = context;
            mView.setOnClickListener(this);

        }
        /* Interface for handling clicks - both normal and long ones. */
        public interface ClickListener {

            /**
             * Called when the view is clicked.
             *
             * @param v view that is clicked
             * @param position of the clicked item
             * @param isLongClick true if long click, false otherwise
             */
            public void onClick(View v, int position, boolean isLongClick);

        }
        /* Setter for listener. */
        public void setClickListener(ClickListener clickListener) {
            this.clickListener = clickListener;
        }


        @Override
        public void onClick(View v) {
            clickListener.onClick(v, getPosition(), false);
        }
    }

    class ImageDownloaderTask extends AsyncTask<Events, Void, Bitmap> {
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
