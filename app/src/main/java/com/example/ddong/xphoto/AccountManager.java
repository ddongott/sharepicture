package com.example.ddong.xphoto;

import android.content.Context;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ddong on 2015-10-14.
 */
public class AccountManager {
    private final static String TAG = "AccountManager";
    private Context mContext;
    private boolean mLogin = false;
    private AccessTokenTracker mAccessTokenTracker = null;
    AccessToken mAccessToken = null;
    private static final AccountManager instance = new AccountManager();

    // Private constructor prevents instantiation from other classes
    private AccountManager() { }

    public static AccountManager getInstance() {
        return instance;
    }

    public void setAppContext(Context context) {
        mContext = context;
    }

    public void login() {
        if (mLogin) {
            return;
        }

        String fbid = SharePrefHelper.getInstance().getFacebookId();
        if (!fbid.isEmpty()) {
            facebookLogin();
        }
    }

    private void facebookLogin() {
        FacebookSdk.sdkInitialize(mContext);
        mAccessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                Log.d(TAG,"accetoken changed: " + currentAccessToken );

                // Set the access token using
                // currentAccessToken when it's loaded or set.
                mAccessToken = currentAccessToken;
                HttpHelper.getInstance().facebookLogin(mAccessToken);
            }
        };

        // If the access token is available already assign it.
        mAccessToken = AccessToken.getCurrentAccessToken();
        Log.d(TAG,"get fb accetoken: " + mAccessToken );
        if(mAccessToken != null) {
            HttpHelper.getInstance().facebookLogin(mAccessToken);
        }
    }

    public AccessToken getAccessToken() {
        if (mAccessToken == null) {
            mAccessToken = AccessToken.getCurrentAccessToken();
        }
        return mAccessToken;
    }

    public void release() {
        if (mAccessTokenTracker != null) {
            mAccessTokenTracker.stopTracking();
        }
    }

    public void loginSuccess(JSONObject jobj) {
        SharePrefHelper sharepreference = SharePrefHelper.getInstance();
        try {
            mLogin = jobj.getBoolean("success");
            if(mLogin) {
                String username = jobj.getString("username");
                sharepreference.setUserName(username);
                String user_id = jobj.getString("user_id");
                sharepreference.setUserID(user_id);
                String email = jobj.getString("email");
                sharepreference.setEmail(email);
                String first_name = jobj.getString("first_name");
                sharepreference.setFirstName(first_name);
                String last_name = jobj.getString("last_name");
                sharepreference.setLastName(last_name);
            }
        }
        catch (JSONException e) {
            Log.d(TAG,e.toString());
        }
        finally {
            String gcmtoken = sharepreference.getGcmToken();
            if(!gcmtoken.isEmpty()) {
                HttpHelper.getInstance().updateGCMToken(gcmtoken);
                Log.d(TAG, "updateGCMToken: " + sharepreference.getGcmToken());
            }
        }
    }

    public boolean getLoginStatus() {
        return mLogin;
    }
}
