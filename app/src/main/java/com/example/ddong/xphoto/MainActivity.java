package com.example.ddong.xphoto;

import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class MainActivity extends TabActivity
        implements OnTabChangeListener {
    private final static String TAG = "MainActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    /** Called when the activity is first created. */
    TabHost mTabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        Context appContext = getApplicationContext();
        XPUtils.Init(appContext);

        // Get TabHost Refference
        mTabHost = getTabHost();

        // Set TabChangeListener called when tab changed
        mTabHost.setOnTabChangedListener(this);

        Intent intent;
        /************setup tabs*********/
        intent = new Intent().setClass(this, LocalGalleryActivity.class);
        setupTab(intent, getString(R.string.tab_text_protected_photos), R.drawable.ic_photo_library_white_24dp);

        intent = new Intent().setClass(this, SharedPhotoManager.class);
        setupTab(intent, getString(R.string.tab_text_shared_photos), R.drawable.ic_share_black_24dp);

        intent = new Intent().setClass(this, ContactsActivity.class);
        setupTab(intent, getString(R.string.tab_text_contacts), R.drawable.ic_people_black_24dp);


        // Set drawable images to tab
        //mTabHost.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.ic_share_black_24dp);
        //mTabHost.getTabWidget().getChildAt(2).setBackgroundResource(R.drawable.ic_people_black_24dp);

        // Set Tab1 as Default tab and change image
        mTabHost.getTabWidget().setCurrentTab(0);
        //mTabHost.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.ic_photo_library_white_24dp);
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void setupTab(final Intent intent, final String tag, final int imageres) {
        View tabview = createTabView(mTabHost.getContext(), tag, imageres);
        TabHost.TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
        mTabHost.addTab(setContent);
    }

    private static View createTabView(final Context context, final String text, final int imageres) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_normal, null);
        ImageView image = (ImageView) view.findViewById(R.id.tabsIcon);
        image.setImageResource(imageres);
        TextView tv = (TextView) view.findViewById(R.id.tabsText);
        tv.setText(text);
        return view;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AccountManager.getInstance().release();
    }

    @Override
    public void onTabChanged(String tabId) {
        /************ Called when tab changed *************/
        int currtab = mTabHost.getCurrentTab();
        Log.i("tabs", "CurrentTab: " + currtab);

        //********* Check current selected tab and change according images *******/

        for(int i=0;i<mTabHost.getTabWidget().getChildCount();i++)
        {
            View tview = mTabHost.getTabWidget().getChildTabViewAt(i);
            ImageView image = (ImageView) tview.findViewById(R.id.tabsIcon);

            if(i==0) {
                if(i==currtab)
                    image.setImageResource(R.drawable.ic_photo_library_white_24dp);
                else
                    image.setImageResource(R.drawable.ic_photo_library_black_24dp);
            } else if(i==1) {
                if(i==currtab)
                    image.setImageResource(R.drawable.ic_share_white_24dp);
                else
                    image.setImageResource(R.drawable.ic_share_black_24dp);
            } else if(i==2) {
                if(i==currtab)
                    image.setImageResource(R.drawable.ic_people_white_24dp);
                else
                    image.setImageResource(R.drawable.ic_people_black_24dp);
            }
        }
    }
    
}
