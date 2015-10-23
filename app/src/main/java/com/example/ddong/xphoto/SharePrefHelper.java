package com.example.ddong.xphoto;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/**
 * Created by ddong on 2015-09-22.
 */
public class SharePrefHelper {
    private static final String PREFS_NAME = "XPhotoSettings";
    private static final String PREFS_KEY_FACEBOOK_ID = "fbid";
    private static final String PREFS_KEY_USERNAME = "username";
    private static final String PREFS_KEY_USER_UUID = "useruuid";
    private static final String PREFS_KEY_PASSWORD = "password";
    private static final String PREFS_KEY_EMAILS = "emails";
    private static final String GCM_TOKEN = "gcmToken";


    private Context mContext;
    private SharedPreferences mSettings;

    //////////////////
    //the singleton implementation
    //////////////////

    private static final SharePrefHelper instance = new SharePrefHelper();

    // Private constructor prevents instantiation from other classes
    private SharePrefHelper() { }

    public static SharePrefHelper getInstance() {
        return instance;
    }

    public void setAppContext(Context context) {
        mContext = context;
        mSettings = mContext.getSharedPreferences(PREFS_NAME, 0);

    }

    public String getFacebookId() {
        return mSettings.getString(PREFS_KEY_FACEBOOK_ID, "");
    }

    public void setFacebookId(String id) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(PREFS_KEY_FACEBOOK_ID, id);
        editor.commit();
    }

    public String getUserName() {
        return mSettings.getString(PREFS_KEY_USERNAME, "");
    }

    public void setUserName(String username) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(PREFS_KEY_USERNAME, username);
        editor.commit();
    }

    public void setEmail(String email) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(PREFS_KEY_EMAILS, email);
        editor.commit();
    }

    public String getEmails() {
        return mSettings.getString(PREFS_KEY_EMAILS, "");
    }

    public String getUserUUID() {
        String uuid = mSettings.getString(PREFS_KEY_USER_UUID, "");
        //if uuid of the user is not generated,
        //get a new one.
        if (uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putString(PREFS_KEY_USER_UUID, uuid);
            editor.commit();
        }
        return uuid;
    }

    public String getPassword() {
        return mSettings.getString(PREFS_KEY_PASSWORD, "");
    }

    public void setPassword(String password) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putString(PREFS_KEY_PASSWORD, password);

        // Commit the edits!
        editor.commit();
    }

    public void setGcmToken(String token) {
        mSettings.edit().putString(GCM_TOKEN, token).apply();
    }

    public String getGcmToken() {
        return mSettings.getString(GCM_TOKEN, "");
    }
}
