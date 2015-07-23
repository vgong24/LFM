package com.bowen.victor.ciya.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bowen.victor.ciya.R;
import com.bowen.victor.ciya.tools.WorkAround;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Created by Victor on 7/21/2015.
 */
public class ProfileSettings extends Activity {
    private static final int SELECT_PICTURE = 1;
    private String selectedImagePath;
    TextView fullNameTV, usernameTV, saveTV;
    EditText firstNameET, lastNameET, phoneNumET, emailET;
    StringBuilder fullName;
    String username, emailAddr, firstname, lastname;
    private static boolean fnameChanged, lnameChanged, ppicChanged;
    ParseUser parseUser = ParseUser.getCurrentUser();
    ImageView profilePicture;
    ParseFile pPic;
    Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_profile_page);
        WorkAround.setNotificationBarColor(this, R.color.colorPrimaryDark);
        initialize();

    }

    public void initialize(){
        firstNameET = (EditText) findViewById(R.id.firstNameEdit);
        lastNameET = (EditText) findViewById(R.id.lastNameEdit);
        fullName = new StringBuilder("");
        firstname = parseUser.getString("firstName");

        lastname = parseUser.getString("lastName");
        if(lastname == null){
            lastname = "";
        }


        fullNameTV = (TextView) findViewById(R.id.fullname_display);
        usernameTV = (TextView) findViewById(R.id.username_display);

        emailET = (EditText) findViewById(R.id.emailEditText);

        username = parseUser.getUsername();
        emailAddr = parseUser.getEmail();

        usernameTV.setText(username);


        profilePicture = (ImageView) findViewById(R.id.image_display);

        pPic = parseUser.getParseFile("profilePic");

        saveTV = (TextView) findViewById(R.id.saveButton);
        saveTV.setVisibility(View.GONE);

        fnameChanged = false;
        lnameChanged = false;
        ppicChanged = false;

        setUpClickListeners();
        showCurrentUserInfo();
        checkForChange();
    }

    public void showCurrentUserInfo(){
        emailET.setText(emailAddr);
        firstNameET.setText(firstname);
        lastNameET.setText(lastname);
        fullName.append(firstname + " " + lastname);
        fullNameTV.setText(fullName);
        if(pPic != null){
            Bitmap bitmapPic = WorkAround.getResizedBitmap(getApplicationContext(), pPic, 7.2);
            profilePicture.setImageBitmap(WorkAround.getRoundedCornerBitmap(bitmapPic, 20));
        }

    }

    public void checkForChange(){

        firstNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!firstname.equalsIgnoreCase(s.toString())) {
                    fnameChanged = true;
                } else
                    fnameChanged = false;
                showSave();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        lastNameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!lastname.equalsIgnoreCase(s.toString())) {
                    lnameChanged = true;
                } else
                    lnameChanged = false;
                showSave();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }

    /**
     * Set OnClickListeners for Profile picture and Save Button
     * @return
     */
    public void setUpClickListeners(){
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);

            }
        });

        saveTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firstname = firstNameET.getText().toString();
                lastname = lastNameET.getText().toString();
                fnameChanged = false;
                lnameChanged = false;
                ppicChanged = false;
                showSave();
                parseUser.put("firstName", firstname);
                parseUser.put("lastName", lastname);

                if (selectedImageUri != null) {
                    ParseFile file = new ParseFile(username + ".jpg", convertImageToBytes(selectedImageUri));
                    parseUser.put("profilePic", file);
                }

                parseUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        Toast.makeText(getApplicationContext(), "Saved data", Toast.LENGTH_SHORT);


                    }
                });
            }
        });
    }

    //http://stackoverflow.com/questions/24072816/saving-and-retreving-photos-and-videos-in-parse-android
    private byte[] convertImageToBytes(Uri uri){
        byte[] data = null;
        try {
            ContentResolver cr = getBaseContext().getContentResolver();
            InputStream inputStream = cr.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            Bitmap cropImage = cropBitmap(bitmap);
            cropImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);

            data = baos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return data;
    }

    /**
     * Crop Bitmaps
     */
    public Bitmap cropBitmap(Bitmap bitmap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int crop;
        Bitmap cropped;
        if(width > height){
            crop = (width - height) /2;
            cropped = Bitmap.createBitmap(bitmap, crop, 0, height, height);
        }else{
            crop = (height - width) / 2;
            cropped = Bitmap.createBitmap(bitmap, 0, crop, width, width);
        }

        return cropped;
    }

    //http://stackoverflow.com/questions/10773511/how-to-resize-an-image-i-picked-from-the-gallery-in-android
    public static Bitmap decodeUri(Context c, Uri uri, final int requiredSize)
            throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o);

        int width_tmp = o.outWidth
                , height_tmp = o.outHeight;
        int scale = 1;

        while(true) {
            if(width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(c.getContentResolver().openInputStream(uri), null, o2);
    }

    //Display whether or not the save button should be shown based on changed settings
    public boolean showSave(){
        if(fnameChanged || lnameChanged || ppicChanged){
            saveTV.setVisibility(View.VISIBLE);
            return true;
        }
        saveTV.setVisibility(View.GONE);
        return false;
    }



    /**
     * Start activity to get profile picture from user's gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        if(resultCode == RESULT_OK && data != null){
            if(requestCode == SELECT_PICTURE){
                selectedImageUri = data.getData();
                selectedImagePath = getPath(selectedImageUri);

                try {
                    Bitmap bitmap = decodeUri(getApplicationContext(), selectedImageUri, 200);
                    Bitmap cropped = cropBitmap(bitmap);
                    profilePicture.setImageBitmap(WorkAround.getRoundedCornerBitmap(cropped, 20));
                } catch (FileNotFoundException e) {
                    //profilePicture.setImageURI(selectedImageUri);
                    e.printStackTrace();
                }

                ppicChanged = true;
            }
        }else{
            ppicChanged = false;
        }
        showSave();
    }

    public String getPath(Uri uri){
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }


}
