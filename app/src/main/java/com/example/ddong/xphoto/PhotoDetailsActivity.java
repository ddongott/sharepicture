package com.example.ddong.xphoto;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;

import java.util.ArrayList;

public class PhotoDetailsActivity extends Activity {
    private String TAG = "PhotoDetailsActivity";
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

 //       TextView titleTextView = (TextView) findViewById(R.id.title);
 //       titleTextView.setText(title);

 //       ImageView imageView = (ImageView) findViewById(R.id.image);
 //       Uri uri = Uri.parse(path);
 //       imageView.setImageURI(uri);
        mDB = new XPDatabaseOperation(getApplicationContext(), LocalGalleryActivity.TABLE_NAME);

        mAdapter = new FullScreenImageAdapter(PhotoDetailsActivity.this, getData());

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
}