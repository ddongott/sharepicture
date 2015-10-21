package com.example.ddong.xphoto;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;

import org.json.JSONObject;

public class AccountsInfoActivity extends AppCompatActivity {
    private final static String TAG = "AccountInfo";
    private String mFBUserId = "";
    CallbackManager mCallbackManager;
    ProfilePictureView mFbAvatar;
    TextView mAccoutName;
    SharePrefHelper mSharePrefHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mSharePrefHelper = SharePrefHelper.getInstance();
        
        mFBUserId = mSharePrefHelper.getFacebookId();

        setContentView(R.layout.activity_accounts_info);
        mAccoutName = (TextView)findViewById(R.id.user_info_text);
        mFbAvatar = (ProfilePictureView) findViewById(R.id.fbavatar);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);

        if (mFBUserId.isEmpty()) {
            //set default avatar and user name
            mAccoutName.setText(R.string.user_info_no);

            mCallbackManager = CallbackManager.Factory.create();
            loginButton.setReadPermissions("user_friends");
            loginButton.setReadPermissions("email");
            //loginButton.setReadPermissions("read_custom_friendlists");

            loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    Log.d(TAG, "Facebook login success");
                    final AccessToken fbtoken = loginResult.getAccessToken();

                    // App code
                    GraphRequest request = GraphRequest.newMeRequest(
                            fbtoken,
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(
                                        JSONObject object,
                                        GraphResponse response) {
                                    // Application code
                                    Log.d(TAG,"new me request complete: " + object.toString());
                                    String jvalue;

                                    try {
                                        jvalue = (String) object.getString("id");
                                        mSharePrefHelper.setFacebookId(jvalue);
                                        mFbAvatar.setProfileId(jvalue);
                                        Log.d(TAG, "new me request complete: " + jvalue);

                                        jvalue = (String) object.getString("name");
                                        mSharePrefHelper.setUserName(jvalue);
                                        mAccoutName.setText(jvalue);
                                        Log.d(TAG, "new me request complete: " + jvalue);

                                        jvalue = (String) object.getString("email");
                                        mSharePrefHelper.setEmail(jvalue);
                                        Log.d(TAG, "new me request complete: " + jvalue);

                                        //write user info to server
                                        HttpHelper.getInstance().facebookLogin(fbtoken);
                                    }
                                    catch (Exception e) {
                                        Log.d(TAG, "Exception when getting Facebook info");
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email,link");
                    request.setParameters(parameters);
                    request.executeAsync();

                }

                @Override
                public void onCancel() {
                    // App code
                }

                @Override
                public void onError(FacebookException exception) {
                    // App code
                }
            });
        }
        else {
            String v = mSharePrefHelper.getFacebookId();
            mFbAvatar.setProfileId(v);
            mAccoutName.setText(mSharePrefHelper.getUserName());
            //loginButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_accounts_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
