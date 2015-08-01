package com.bowen.victor.ciya.adapters;

import android.content.Context;
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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.activities.EventDetails;
import com.bowen.victor.ciya.fragments.FriendsTab;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.structures.Events;
import com.bowen.victor.ciya.structures.FriendProfile;
import com.bowen.victor.ciya.structures.FriendRequest;
import com.bowen.victor.ciya.structures._User;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendRecyclerAdapter extends RecyclerView.Adapter<FriendRecyclerAdapter.ViewHolder> {
    Context context;
    int resourcexml;
    RecyclerView.LayoutManager mLayoutManager;
    List<FriendProfile> friendProfiles;
    BtnClickListener mClickListener = null;
    private int lastPosition = -1;
    private final double BITMAP_SCALE = 7.2;

    public interface BtnClickListener {
        public abstract void onBtnClick(int position);
    }

    public FriendRecyclerAdapter(Context context, int resource, List<FriendProfile> friendList, RecyclerView.LayoutManager layoutManager, BtnClickListener listener) {
        //super(context, resource, friendList);
        this.context = context;
        resourcexml = resource;
        friendProfiles = friendList;
        mClickListener = listener;
        mLayoutManager = layoutManager;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FriendRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                              int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v, context);
        return vh;
    }

    private final String REMOVE = "approve";
    private final String ACCEPT = "request";
    private final String PENDING = "pending";
    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.friendStatusImg.setTag(position);
        FriendProfile friendProfile = friendProfiles.get(position);
        String fName = friendProfile.getUserName();
        holder.friendName.setText(fName);
        String statusBox = friendProfile.getStatus();


        if(holder.friendProfileImg != null){
            ParseQuery query = ParseQuery.getQuery("_User");
            query.whereEqualTo("username", fName);
            query.getFirstInBackground(new GetCallback() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    if (e == null) {
                        ParseUser user = (ParseUser) parseObject;
                        Toast.makeText(context, user.getUsername(), Toast.LENGTH_SHORT).show();
                        new ImageDownloaderTask(holder.friendProfileImg).execute(user);
                    }
                }
            });

        }


        switch(statusBox){
            case "pending":
                holder.friendStatusText.setText("Pending");
                break;
            case "request": //if someone sent you a request, you can approve it
                holder.friendStatusText.setText("Accept");
                holder.friendStatusImg.setImageResource(R.drawable.greenplus);
                break;
            case "approve": //if you are already friends, you can choose to remove friend
                holder.friendStatusText.setText("Remove");
                holder.friendStatusImg.setImageResource(R.drawable.redx);
                break;
            default:
                holder.friendStatusText.setVisibility(View.GONE);
                break;
        }


        //Set onclicks
        holder.setClickListener(new ViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int position, boolean isLongClick) {

                for (int i = 0; i < getItemCount(); i++) {
                    //Toast.makeText(context, i + " " + mLayoutManager.getChildAt(i).findViewById(R.id.friend_status_img).getVisibility() , Toast.LENGTH_SHORT).show();
                    if (i != position) {
                        mLayoutManager.getChildAt(i).findViewById(R.id.friend_status_text).setVisibility(View.INVISIBLE);
                        mLayoutManager.getChildAt(i).findViewById(R.id.friend_status_img).setVisibility(View.INVISIBLE);
                    }
                }

                TextView friendStatusText = (TextView) v.findViewById(R.id.friend_status_text);
                ImageView friendStatusImg = (ImageView) v.findViewById(R.id.friend_status_img);
                if (friendStatusText.getVisibility() == View.INVISIBLE) {
                    friendStatusText.setVisibility(View.VISIBLE);
                    friendStatusImg.setVisibility(View.VISIBLE);
                } else {
                    friendStatusText.setVisibility(View.INVISIBLE);
                    friendStatusImg.setVisibility(View.INVISIBLE);
                }

                //Toast.makeText(context, "" + mLayoutManager.getChildAt(0).findViewById(R.id.friend_status_text).get, Toast.LENGTH_SHORT).show();
            }
        });

        holder.friendStatusImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null)
                    mClickListener.onBtnClick((Integer) v.getTag());
            }
        });

        //Set up animation
        setAnimation(holder.mView, position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return friendProfiles.size();
    }

    /**
     * Animate list population
     */
    private void setAnimation(View viewToAnimate, int position){
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
            animation.setDuration(1000);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }

    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView friendName;
        public TextView friendStatusText;
        public ImageView friendStatusImg;
        public ImageView friendProfileImg;

        public View mView;
        public Context context;
        private ClickListener clickListener;

        public ViewHolder(View v, final Context context) {
            super(v);
            mView = v;
            friendProfileImg = (ImageView) v.findViewById(R.id.friend_pro_pic);
            friendName = (TextView) v.findViewById(R.id.friend_username);
            friendStatusText = (TextView) v.findViewById(R.id.friend_status_text);
            friendStatusImg = (ImageView) v.findViewById(R.id.friend_status_img);
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

    /*
    @Override
    public View getView(int position, View view, ViewGroup parent){


        ViewHolder holder;

        if(view == null) {
            LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(resourcexml, parent, false);
            holder = new ViewHolder();

            holder.friendName = (TextView) view.findViewById(R.id.friend_username);
            holder.friendStatusText = (TextView) view.findViewById(R.id.friend_status_text);
            holder.friendStatusImg = (ImageView) view.findViewById(R.id.friend_status_img);
            holder.friendStatusImg.setTag(position);
            holder.friendStatusImg.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mClickListener != null)
                        mClickListener.onBtnClick((Integer) v.getTag());
                }
            });


            view.setTag(holder);

        }else{
            holder = (ViewHolder) view.getTag();
        }
        FriendProfile friendProfile = friendProfiles.get(position);
        String fName = friendProfile.getUserName();
        holder.friendName.setText(fName);
        String statusBox = friendProfile.getStatus();


        switch(statusBox){
            case "pending":
                holder.friendStatusText.setText("Pending");
                break;
            case "request": //if someone sent you a request, you can approve it
                holder.friendStatusText.setText("Accept");
                holder.friendStatusImg.setImageResource(R.drawable.greenplus);
                break;
            case "approve": //if you are already friends, you can choose to remove friend
                holder.friendStatusText.setText("Remove");
                holder.friendStatusImg.setImageResource(R.drawable.redx);
                break;
            default:
                holder.friendStatusText.setVisibility(View.GONE);
                break;
        }

        return view;
    }
    */

    class ImageDownloaderTask extends AsyncTask<ParseUser, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewWeakReference;

        public ImageDownloaderTask(ImageView imageView) {
            imageViewWeakReference = new WeakReference<ImageView>(imageView);
        }


        @Override
        protected Bitmap doInBackground(ParseUser... params) {

            ParseFile thumbnail = null;

            if ((thumbnail = params[0].getParseFile("profilePic")) == null) {
                if((thumbnail = params[0].getParseFile("profilePicture")) == null){

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
