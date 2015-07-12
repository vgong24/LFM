package com.bowen.victor.ciya.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.R;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Victor on 5/28/2015.
 * Usage: CreateTab
 * Display User information (pic, name)
 */
public class AttendeeListAdapter extends ArrayAdapter<Attendee> {
    TextView attenderName;
    int viewListXML;
    ArrayList<Attendee> attendeeArrayList;
    Context context;
    //Using ratio of 7.2 to get the correct size. 720 : 100
    private final double BITMAP_SCALE = 7.2;

    public AttendeeListAdapter(Context context, int viewListXML, ArrayList<Attendee> attendeesArr) {//Example R.layout.event_list_item, events
        super(context, viewListXML, attendeesArr);
        this.viewListXML = viewListXML;
        this.attendeeArrayList = attendeesArr;
        this.context = context;

    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view == null) {
            //view = getLayoutInflater().inflate(viewListXML, parent, false);
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(viewListXML, parent, false);

            holder = new ViewHolder();
            holder.attendeePic = (ImageView) view.findViewById(R.id.attendeeProfilePic);
            holder.attenderName = (TextView) view.findViewById(R.id.attendeeListViewName);


            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        //ParseUser player = attendeeUsers.get(position);
        Attendee attendee = attendeeArrayList.get(position);
        ParseUser player = attendee.getUserID();
        String playerName = player.getString("username");
        holder.attenderName.setText(playerName);

        if(holder.attendeePic != null){
            new ImageDownloaderTask(holder.attendeePic).execute(attendee);
        }

        return view;

    }

    static class ViewHolder {
        TextView attenderName;
        ImageView attendeePic;

    }

    class ImageDownloaderTask extends AsyncTask<Attendee, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
        }


        @Override
        protected Bitmap doInBackground(Attendee... params) {

            ParseFile thumbnail = null;
            if ((thumbnail = params[0].getUserID().getParseFile("profilePic")) == null) {
                if((thumbnail = params[0].getUserID().getParseFile("profilePicture")) == null){

                }
            }
            return getResizedBitmap(thumbnail);
        }

        public Bitmap getResizedBitmap(ParseFile thumbnail){
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
                    int height = size.y;
                    //Using ratio of 7.2 to get the correct size. 720 : 100
                    int bitmapScale = (int) (width / BITMAP_SCALE);
                    Log.v("Bitmap", "Bitmap width: " + width + " height: "+ height);


                    Bitmap resizedbitmap = Bitmap.createScaledBitmap(bmp, bitmapScale, bitmapScale, true);
                    return resizedbitmap;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (imageViewWeakReference != null) {
                ImageView imageView = imageViewWeakReference.get();
                if (imageView != null) {
                    if (bitmap != null) {
                        imageView.setImageBitmap(bitmap);
                    } else {
                        Drawable placeholder = imageView.getContext().getResources().getDrawable(R.drawable.reload);
                        imageView.setImageDrawable(placeholder);
                        imageView.setPadding(10, 10, 10, 10);
                    }
                }
            }
        }


    }
}