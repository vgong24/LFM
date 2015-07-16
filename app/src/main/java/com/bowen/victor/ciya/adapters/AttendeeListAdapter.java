package com.bowen.victor.ciya.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bowen.victor.ciya.activities.EventDetails;
import com.bowen.victor.ciya.dbHandlers.FriendListDBHandler;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.structures.Events;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Victor on 5/28/2015.
 * Usage: EventDetails page listview
 * Display User information Picture, Name, Options(add friend, or kick if host)
 *
 */
public class AttendeeListAdapter extends ArrayAdapter<Attendee> {
    TextView attenderName;
    int viewListXML;
    ArrayList<Attendee> attendeeArrayList;
    boolean isHost;
    String hostId;
    String eventId;
    EventDetails eventDetails;
    ParseUser currentUser = ParseUser.getCurrentUser();
    String currentUserId = currentUser.getObjectId();
    FriendListDBHandler dbHandler;

    Context context;
    //Using ratio of 7.2 to get the correct size. 720 : 100
    private final double BITMAP_SCALE = 7.2;

    /**
     * Needs to fact check and call parent methods after buttons are pressed
     * @param context
     * @param viewListXML
     * @param attendeesArr
     * @param eventID
     * @param isHost
     * @param hostId
     * @param eventDetails
     */
    public AttendeeListAdapter(Context context, int viewListXML, ArrayList<Attendee> attendeesArr, String eventID, boolean isHost, String hostId, EventDetails eventDetails) {//Example R.layout.event_list_item, events
        super(context, viewListXML, attendeesArr);
        this.viewListXML = viewListXML;
        this.attendeeArrayList = attendeesArr;
        this.context = context;
        this.hostId = hostId;
        this.isHost = isHost;
        this.eventId = eventID;
        this.eventDetails = eventDetails;
        dbHandler = new FriendListDBHandler(context);

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
            holder.attendeeKick = (Button) view.findViewById(R.id.attendeeKick);
            holder.attendeeAddFriend = (Button) view.findViewById(R.id.attendeeAddFriend);


            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        //ParseUser player = attendeeUsers.get(position);
        final Attendee attendee = attendeeArrayList.get(position);
        final ParseUser player = attendee.getUserID();

        String playerName = player.getString("username");
        holder.attenderName.setText(playerName);
        if(holder.attendeePic != null){
            new ImageDownloaderTask(holder.attendeePic).execute(attendee);
        }

        setButtons(view, holder, player, attendee);

        return view;

    }

    public void setButtons(View view, ViewHolder holder, ParseUser player, final Attendee attendee){
        holder.attendeeAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Request Friend(Not implemented)", Toast.LENGTH_SHORT).show();
            }
        });

        if(hostId.equalsIgnoreCase(player.getObjectId())){//If host, make different color
            view.setBackgroundResource(R.color.HostRow);
            holder.attendeeKick.setVisibility(View.INVISIBLE);
            if(isHost){
                holder.attendeeAddFriend.setVisibility(View.GONE);
            }

        }else if(isHost){//If you are the host, set up options for others
            holder.attendeeKick.setText("KICK");
            holder.attendeeKick.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Events eventObject = (Events) ParseObject.createWithoutData("Events", eventId);
                    onKickDialog(eventObject, attendee.getObjectId());
                }
            });

        }else{
            holder.attendeeKick.setVisibility(View.INVISIBLE);
        }

        if(player.getObjectId().equalsIgnoreCase(currentUserId) || dbHandler.profileExists(player.getObjectId()) > 0){ //If you are this player, or if the player is your friend, hide add button
            holder.attendeeAddFriend.setVisibility(View.GONE);
        }


    }

    public void onKickDialog(final Events eventObj, final String attendeeId){
        AlertDialog.Builder builder = new AlertDialog.Builder(eventDetails);
        builder.setTitle("Kick Player?");
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                eventDetails.leaveEventAsAttendee(eventObj, attendeeId);
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }

    static class ViewHolder {
        TextView attenderName;
        ImageView attendeePic;
        Button attendeeAddFriend;
        Button attendeeKick;

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