package com.bowen.victor.ciya.uncategorized;

import android.util.Log;

import com.bowen.victor.ciya.structures.Assets;
import com.bowen.victor.ciya.structures.Attendee;
import com.bowen.victor.ciya.structures.Category;
import com.bowen.victor.ciya.structures.Events;
import com.bowen.victor.ciya.structures.FriendRequest;
import com.bowen.victor.ciya.structures.Interests;
import com.bowen.victor.ciya.structures.Reviews;
import com.bowen.victor.ciya.structures._User;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.SaveCallback;

public class Application extends android.app.Application {
  // Debugging switch
  public static final boolean APPDEBUG = false;

  // Debugging tag for the application
  public static final String APPTAG = "AnyWall";




  public Application() {
  }

  @Override
  public void onCreate() {
    super.onCreate();
    ParseObject.registerSubclass(Category.class);
    ParseObject.registerSubclass(Events.class);
    ParseObject.registerSubclass(Attendee.class);
    ParseObject.registerSubclass(Interests.class);
    ParseObject.registerSubclass(Reviews.class);
    ParseObject.registerSubclass(_User.class);
    ParseObject.registerSubclass(Assets.class);
    ParseObject.registerSubclass(FriendRequest.class);
    Parse.initialize(this, "aTeqCcZ5KEMR72fG8kiZED2Rxeb2r7ruWtqBSVa6",
            "JPdPZXMcR4ESbXNly1vuP19EFoYK3upi8sopk282");


    ParsePush.subscribeInBackground("", new SaveCallback() {
      @Override
      public void done(ParseException e) {
        if (e == null) {
          Log.d("com.parse.push", "successfully subscribed to the broadcast channel.");
        } else {
          Log.e("com.parse.push", "failed to subscribe for push", e);
        }
      }
    });

  }



}
