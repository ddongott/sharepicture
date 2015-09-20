package com.example.ddong.xphoto;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SetPasswordActivity extends AppCompatActivity {
    private final static int PASSWORD_LENGTH = 4;
    private Keyboard mKeyboard;
    private KeyboardView mKeyboardView;
    private String mPassword = "";
    private String mPassword2 = "";
    private String mSavedPassword = "";
    private Context mContext;
    private TextView mTitleTxtView = null;
    private ArrayList<ImageView> mPasswordView = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        mContext = getApplicationContext();
        mTitleTxtView = (TextView)findViewById(R.id.set_password_title);
        mSavedPassword = getIntent().getStringExtra("password");
        if(mSavedPassword.isEmpty()) {
            mTitleTxtView.setText(R.string.set_password_tile);
        } else {
            mTitleTxtView.setText(R.string.validate_password_tile);
        }

        ImageView passwordhint = (ImageView)findViewById(R.id.passwordHintView0);
        mPasswordView.add(passwordhint);
        passwordhint = (ImageView)findViewById(R.id.passwordHintView1);
        mPasswordView.add(passwordhint);
        passwordhint = (ImageView)findViewById(R.id.passwordHintView2);
        mPasswordView.add(passwordhint);
        passwordhint = (ImageView)findViewById(R.id.passwordHintView3);
        mPasswordView.add(passwordhint);
        // Create the Keyboard
        mKeyboard= new Keyboard(this,R.layout.keyboard);

        // Lookup the KeyboardView
        mKeyboardView= (KeyboardView)findViewById(R.id.keyboardview);
        // Attach the keyboard to the view
        mKeyboardView.setKeyboard( mKeyboard );

        // Do not show the preview balloons
        mKeyboardView.setPreviewEnabled(false);

        // Install the key handler
        mKeyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);

        //mKeyboardView.setVisibility(View.VISIBLE);
        mKeyboardView.setEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_password, menu);
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

    private void resetPasswordView() {
        int i;
        ImageView pwv;
        for(i = 0; i < PASSWORD_LENGTH; i++) {
            pwv = mPasswordView.get(i);
            pwv.setImageResource(R.drawable.ic_panorama_fish_eye_black_24dp);
        }
    }

    private KeyboardView.OnKeyboardActionListener mOnKeyboardActionListener = new KeyboardView.OnKeyboardActionListener() {
        @Override public void onKey(int primaryCode, int[] keyCodes)
        {
            //Here check the primaryCode to see which key is pressed
            //based on the android:codes property
            if(primaryCode >= 0 && primaryCode <= 9)
            {
                mPassword += primaryCode;
                int length = mPassword.length();
                ImageView pwview = mPasswordView.get(length - 1);
                pwview.setImageResource(R.drawable.ic_adjust_black_24dp);
                if (length == PASSWORD_LENGTH) {
                    //get 4 digital password successfully
                    if (!mSavedPassword.isEmpty()) {
                        if (mSavedPassword.equals(mPassword)) {
                            finishWithResult();
                        } else {
                            mPassword = "";
                            resetPasswordView();
                            Toast.makeText(mContext, getString(R.string.invalid_password),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        if (mPassword2.isEmpty()) {
                            mTitleTxtView.setText(R.string.confirm_password_tile);
                            mPassword2 = mPassword;
                            mPassword = "";
                            resetPasswordView();
                        } else if (mPassword.equals(mPassword2)) {
                            finishWithResult();
                        } else {
                            Toast.makeText(mContext, getString(R.string.pass_not_match),
                                    Toast.LENGTH_LONG).show();
                            mPassword = "";
                            mPassword2 = "";
                            mTitleTxtView.setText(R.string.set_password_tile);
                            resetPasswordView();
                        }
                    }
                }
            }
        }

        @Override public void onPress(int arg0) {
        }

        @Override public void onRelease(int primaryCode) {
        }

        @Override public void onText(CharSequence text) {
        }

        @Override public void swipeDown() {
        }

        @Override public void swipeLeft() {
        }

        @Override public void swipeRight() {
        }

        @Override public void swipeUp() {
        }
    };

    private void finishWithResult()
    {
        Bundle conData = new Bundle();
        conData.putString("password", mPassword);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        finish();
    }
}
