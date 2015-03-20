package com.example.victor.lfm;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;
import com.parse.ParseObject;

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
    Parse.initialize(this, "aTeqCcZ5KEMR72fG8kiZED2Rxeb2r7ruWtqBSVa6",
        "JPdPZXMcR4ESbXNly1vuP19EFoYK3upi8sopk282");




  }



}
