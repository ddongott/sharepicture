package com.example.ddong.xphoto;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.util.ArrayList;

public class PhotoDetailsActivity extends Activity {
    private String TAG = "PhotoDetailsActivity";
    public final static int PICK_CONTACT_REQUEST = 1001;
    private FullScreenImageAdapter mAdapter;
    private ViewPager mViewPager;
    private XPDatabaseOperation mDB;
    final ArrayList<String> mImagePath = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_details);
        mViewPager = (ViewPager) findViewById(R.id.pager);
        //utils = new Utils(getApplicationContext());

        int position = getIntent().getIntExtra("position", 1);
        String password = getIntent().getStringExtra("password");

 //       TextView titleTextView = (TextView) findViewById(R.id.title);
 //       titleTextView.setText(title);

 //       ImageView imageView = (ImageView) findViewById(R.id.image);
 //       Uri uri = Uri.parse(path);
 //       imageView.setImageURI(uri);
        mDB = new XPDatabaseOperation(getApplicationContext(), LocalGalleryActivity.TABLE_NAME);

        mAdapter = new FullScreenImageAdapter(PhotoDetailsActivity.this, getData(), password);

        mViewPager.setAdapter(mAdapter);

        // displaying selected image first
        mViewPager.setCurrentItem(position);
    }

    // Prepare some dummy data for gridview
    private ArrayList<String> getData() {

        Cursor cursor = null;
        Log.d(TAG, "getData");
        try {
            cursor = mDB.selectRecords();
            int row = cursor.getCount();
            for(int i = 0; i < row; i++){
                String path = cursor.getString(cursor.getColumnIndexOrThrow(XPDatabaseOperation.PHOTO_PATH));
                Log.d(TAG,"path: " + path);
                mImagePath.add(path);
                cursor.moveToNext();
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mImagePath;
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent intent ) {

        super.onActivityResult(requestCode, resultCode, intent);
        if ( requestCode == PICK_CONTACT_REQUEST ) {

            if ( resultCode == RESULT_OK ) {
                Uri pickedEmail = intent.getData();
                Log.d(TAG,"picked email: " + pickedEmail.toString());
                mAdapter.onContactPickResult(requestCode, pickedEmail);
                // handle the picked phone number in here.
            }
        }
    }
}