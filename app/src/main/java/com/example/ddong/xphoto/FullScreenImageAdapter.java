package com.example.ddong.xphoto;


import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;

/**
 * Created by ddong on 2015-09-19.
 */
public class FullScreenImageAdapter extends PagerAdapter {
    private final static String TAG = "FullScreenImageAdapter";
    private Activity mActivity;
    private ArrayList<String> mImagePaths;
    private LayoutInflater mInflater;
    private Point mScreenSize = new Point();
    private EncriptionUtil mEncription;
    private int mPosition;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  ArrayList<String> imagePaths, String password) {
        this.mActivity = activity;
        this.mImagePaths = imagePaths;
        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getRealSize(mScreenSize);
        Log.d(TAG, "screen size: " + mScreenSize.x + ":" + mScreenSize.y);
        mEncription = new EncriptionUtil(password);

    }

    @Override
    public int getCount() {
        return this.mImagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imgDisplay;
        Button btnClose;
        Button btnShare;

        mPosition = position;

        mInflater = (LayoutInflater) mActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = mInflater.inflate(R.layout.layout_fullscreen_image, container,
                false);

        imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);
        btnClose = (Button) viewLayout.findViewById(R.id.btnClose);
        btnShare = (Button) viewLayout.findViewById(R.id.btnShare);

        String path = mImagePaths.get(position);
        byte[] imageData = mEncription.decriptFile(path);
        BitmapFactory.Options options = new BitmapFactory.Options();
        //calculate inSampleSize first
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
        options.inSampleSize = calculateInSampleSize(options, mScreenSize.x, mScreenSize.y);
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);

        imgDisplay.setImageBitmap(bitmap);

        // close button click event
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact();
            }
        });
        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    private void pickContact() {
        Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
        pickContactIntent.setType(ContactsContract.CommonDataKinds.Email.CONTENT_TYPE);
        mActivity.startActivityForResult(pickContactIntent, PhotoDetailsActivity.PICK_CONTACT_REQUEST);
    }

    public void onContactPickResult(int requestCode, Uri pickedEmail) {
        ContentResolver cr = mActivity.getContentResolver();
        Cursor cur = cr.query(pickedEmail,
                null, null, null, null);
        //Email is stored in array, starts from -1;
        cur.moveToNext();
        String email = cur.getString(
                cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
        Log.d(TAG, "get email: " + email);
        sendPhoto(email);
    }

    private void sendPhoto(String email){
        String path = mImagePaths.get(mPosition);

    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    || (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

}