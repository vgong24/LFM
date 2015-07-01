package com.example.victor.lfm;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendListDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "friendList",
    TABLE_FRIENDS = "friends",
    KEY_ID = "id",
    KEY_FRIEND_ID = "fObjectId",
    KEY_REAL_NAME = "fRealName",
    KEY_NAME = "fname"
    ;


    public FriendListDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FRIENDS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_FRIEND_ID + " TEXT, " + KEY_NAME + " TEXT, " + KEY_REAL_NAME + " TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        onCreate(db);
    }

    public void createFriend(String friendObjectId, String fname, String realName){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FRIEND_ID, friendObjectId);
        values.put(KEY_NAME, fname);
        values.put(KEY_REAL_NAME, realName);

        db.insert(TABLE_FRIENDS, null, values);
        db.close();
    }

    public void deleteFriend(String friendObjectId){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FRIENDS, KEY_FRIEND_ID + "=?", new String[]{friendObjectId});
        db.close();

    }

    public List<FriendProfile> getAllFriendProfiles(){
        List<FriendProfile> profiles = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FRIENDS, null);

        if(cursor.moveToFirst()){
            do{
                profiles.add(new FriendProfile(cursor.getString(1),cursor.getString(2), cursor.getString(3)));
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return profiles;

    }
    public List<String> getAllFriendProfilesToString(){
        List<String> profiles = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT "+ KEY_NAME +" FROM "+ TABLE_FRIENDS, null);

        if(cursor.moveToFirst()){
            do{
                profiles.add(cursor.getString(0));
            }
            while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return profiles;
    }

    public int getFriendCount(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + KEY_FRIEND_ID + " FROM "+ TABLE_FRIENDS, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();
        return count;

    }
}
