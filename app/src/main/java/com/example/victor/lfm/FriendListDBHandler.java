package com.example.victor.lfm;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 6/30/2015.
 */
public class FriendListDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "friendList",
    TABLE_FRIENDS = "friends",
    KEY_ID = "id",
    KEY_FRIEND_ID = "fObjectId",
    KEY_REAL_NAME = "fRealName",
    KEY_NAME = "fname",
    KEY_STATUS = "Friendstatus";


    public FriendListDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FRIENDS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_FRIEND_ID + " TEXT, " + KEY_NAME + " TEXT, " + KEY_REAL_NAME + " TEXT, " + KEY_STATUS + " TEXT )");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        onCreate(db);
    }
    public void dropDatabase(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
    }
    public void deleteDatabase(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FRIENDS, null, null);
    }

    public void createFriend(String friendObjectId, String fname, String realName, String status){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_FRIEND_ID, friendObjectId);
        values.put(KEY_NAME, fname);
        values.put(KEY_REAL_NAME, realName);
        values.put(KEY_STATUS, status);

        db.insert(TABLE_FRIENDS, null, values);
        db.close();
    }

    public void changeFriendStatus(String friendObjId, String status){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_STATUS, status);
        db.update(TABLE_FRIENDS, cv, KEY_FRIEND_ID + " = " + "'" + friendObjId + "'", null);

    }

    //Create friends based on data from FriendRequest
    public void createFriend(String friendName, String status){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, friendName);
        values.put(KEY_STATUS, status);

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
                profiles.add(new FriendProfile(cursor.getString(1),cursor.getString(2), cursor.getString(3), cursor.getString(4)));
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
