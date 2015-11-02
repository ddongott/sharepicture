package com.example.ddong.xphoto;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class SharedPhotoManager extends AppCompatActivity {
    private final static String TAG = "SharedPhotoManager";
    public final static String TABLE_NAME = "ReceivedPhoto";

    private XPDatabaseOperation mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_photo_manager);
        mDB = new XPDatabaseOperation(getApplicationContext(),TABLE_NAME);

        //Intent intent = new Intent(this, SharedPhotoIntentService.class);
        //intent.putExtra("message", "19");
        //startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_shared_photo_manager, menu);
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
}
