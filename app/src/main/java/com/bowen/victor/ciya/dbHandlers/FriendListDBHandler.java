package com.bowen.victor.ciya.dbHandlers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.bowen.victor.ciya.structures.FriendProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Victor on 6/30/2015.
 *
 * Note: When adding columns remember to: change Database_Version, proper spacing in create Table, and add the column in cursor section
 */
public class FriendListDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 11;
    private static final String DATABASE_NAME = "friendList",
    TABLE_FRIENDS = "friends",
    KEY_ID = "id",
    KEY_OBJECT_ID = "fObjectId",
    KEY_REAL_NAME = "fRealName",
    KEY_NAME = "fname",
    KEY_FRIEND_ID = "fuserId",
    KEY_STATUS = "Friendstatus",
    KEY_IMAGE = "friendPic";


    public FriendListDBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_FRIENDS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_OBJECT_ID + " TEXT, " + KEY_FRIEND_ID + " TEXT, " + KEY_NAME + " TEXT, " + KEY_REAL_NAME + " TEXT, " + KEY_STATUS + " TEXT, " + KEY_IMAGE + " BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("TaskDBAdapter", "Upgrading from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);

    }
    public void dropDatabase(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.close();
    }
    public void deleteDatabase(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_FRIENDS, null, null);
        db.close();
    }

    public void createFriend(String friendObjectId, String friendId, String fname, String realName, String status){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_OBJECT_ID, friendObjectId);
        values.put(KEY_NAME, fname);
        values.put(KEY_REAL_NAME, realName);
        values.put(KEY_STATUS, status);
        values.put(KEY_FRIEND_ID,friendId);

        db.insert(TABLE_FRIENDS, null, values);
        db.close();
    }

    public void createFriend(String friendObjectId, String friendId, String fname, String realName, String status, byte[] imageBytes){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_OBJECT_ID, friendObjectId);
        values.put(KEY_NAME, fname);
        values.put(KEY_REAL_NAME, realName);
        values.put(KEY_STATUS, status);
        values.put(KEY_FRIEND_ID,friendId);
        values.put(KEY_IMAGE, imageBytes);

        db.insert(TABLE_FRIENDS, null, values);
        db.close();
    }

    public void setFriendProfilePic(String friendObjId, byte[] image){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_IMAGE, image);
        db.update(TABLE_FRIENDS, cv, KEY_OBJECT_ID + " = " + "'" + friendObjId + "'", null);
        db.close();
    }

    public void changeFriendStatus(String friendObjId, String status){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_STATUS, status);
        db.update(TABLE_FRIENDS, cv, KEY_OBJECT_ID + " = " + "'" + friendObjId + "'", null);
        db.close();

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
        db.delete(TABLE_FRIENDS, KEY_OBJECT_ID + "=?", new String[]{friendObjectId});
        db.close();

    }

    public List<FriendProfile> getAllFriendProfiles(){
        List<FriendProfile> profiles = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FRIENDS, null);

        if(cursor.moveToFirst()){
            do{
                profiles.add(new FriendProfile(cursor.getString(1),cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getBlob(6)));
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
        Cursor cursor = db.rawQuery("SELECT " + KEY_OBJECT_ID + " FROM " + TABLE_FRIENDS, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();
        return count;

    }

    public int profileExists(String friendId){
        SQLiteDatabase db = getReadableDatabase();
        String[] args= {friendId};
        Cursor cursor = db.rawQuery("SELECT " + KEY_OBJECT_ID + " FROM "+ TABLE_FRIENDS + " WHERE " + KEY_FRIEND_ID + " = ?", args);
        int count = cursor.getCount();
        db.close();
        return count;
    }
}
