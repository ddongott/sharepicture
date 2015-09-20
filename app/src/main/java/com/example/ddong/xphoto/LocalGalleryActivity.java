package com.example.ddong.xphoto;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class LocalGalleryActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "XPhotoSettings";
    public final static String TABLE_NAME = "ProtectPhotos";
    private String TAG = "LocalGalleryActivity";
    private static int RESULT_LOAD_IMG = 1;
    private static int RESULT_SET_PASSWORD = 2;
    private static int RESULT_VALIDATE_PASSWORD = 3;
    private GridView mGridView;
    private LocalGridViewAdapter mGridAdapter;
    private XPDatabaseOperation mDB;
    final ArrayList<ImageItem> mImageItems = new ArrayList<>();
    private SharedPreferences mSettings;
    private String mPassword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(PREFS_NAME, 0);
        mPassword = mSettings.getString("password", "");
        if(mPassword.isEmpty()) {
            setLocalGalleryView();
        } else {
            //validate password first
            Intent pwintent = new Intent(LocalGalleryActivity.this, SetPasswordActivity.class);
            pwintent.putExtra("password",mPassword);
            // Start the Intent
            startActivityForResult(pwintent, RESULT_VALIDATE_PASSWORD);
        }

    }

    private void setLocalGalleryView() {
        setContentView(R.layout.activity_photo_manager);
        mDB = new XPDatabaseOperation(getApplicationContext(),TABLE_NAME);

        mGridView = (GridView) findViewById(R.id.gridView);
        mGridAdapter = new LocalGridViewAdapter(this, R.layout.grid_item_layout, getData());
        mGridView.setAdapter(mGridAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if(position == 0) {
                    if (mImageItems.size() == 1 && mPassword.isEmpty()) {
                        //set password first
                        Intent pwintent = new Intent(LocalGalleryActivity.this, SetPasswordActivity.class);
                        pwintent.putExtra("password",mPassword);
                        // Start the Intent
                        startActivityForResult(pwintent, RESULT_SET_PASSWORD);
                    }
                    else {
                        startGalleryIntent();
                    }
                }
                else {
                    ImageItem item = (ImageItem) parent.getItemAtPosition(position);
                    //Create intent
                    Intent intent = new Intent(LocalGalleryActivity.this, PhotoDetailsActivity.class);
                    intent.putExtra("position", position);
                    //Start details activity
                    startActivity(intent);
                }
            }
        });
    }

    private void startGalleryIntent() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }
    // Prepare some dummy data for gridview
    private ArrayList<ImageItem> getData() {

        Cursor cursor = null;
        Log.d(TAG, "getData");
        Bitmap bp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_add_black_48dp);
        mImageItems.add(new ImageItem(bp, getString(R.string.add_photo), null));
        try {
            cursor = mDB.selectRecords();
            int row = cursor.getCount();
            for(int i = 0; i < row; i++){
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(XPDatabaseOperation.PHOTO_ID));
                String thumbpath = cursor.getString(cursor.getColumnIndexOrThrow(XPDatabaseOperation.PHOTO_THUMB));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(XPDatabaseOperation.PHOTO_PATH));
                Log.d(TAG,"thumbnail path: " + thumbpath);
                Bitmap bitmap = BitmapFactory.decodeFile(thumbpath);
                mImageItems.add(new ImageItem(bitmap, "Image#" + id, path));
                cursor.moveToNext();
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Log.d(TAG, "getData" + mImageItems.toString());
        return mImageItems;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_manager, menu);
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

    private void copyFile(String sourcePath, String destPath) throws IOException {
        File sourceFile = new File(sourcePath);
        File destFile  = new File(destPath);
        if (!destFile.exists())
        {
            try {
                destFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if (!sourceFile.exists()) {
            return;
        }

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }


    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private String getThumbnail(String picPath) {
        ExifInterface exif = null;
        Bitmap thumbnail;
        int THUMBSIZE = 128;
        try {
            exif = new ExifInterface(picPath);
        }
        catch (IOException e) {
            Log.w(TAG, "No exif data for: " + picPath);
        }

        if (exif != null) {
            byte[] imageData = exif.getThumbnail();
            thumbnail = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
        }
        else {
            thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(picPath), THUMBSIZE, THUMBSIZE);

        }
        mImageItems.add(new ImageItem(thumbnail, "", picPath));

        String thumbfilename = picPath.substring(picPath.lastIndexOf("/") + 1);
        File thumbfolder = Environment.getExternalStoragePublicDirectory(getString(R.string.data_storage_folder) + "/.thumbnail");
        if (!thumbfolder.exists()) {
            thumbfolder.mkdirs();
        }
        File fThumb = new File(thumbfolder,thumbfilename);
        FileOutputStream outputStream;

        try {
            outputStream = new FileOutputStream (fThumb);
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fThumb.getPath();
    }

    private void saveData(Context context, Uri contentUri) {
        Cursor cursor = null;
        Cursor thumbcursor = null;
        String path = "";
        String thumbnail = "";
        if (!isExternalStorageWritable()) {
            Log.e(TAG,"External storage not writable");
            return;
        }
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            path = cursor.getString(column_index);
            String filename = path.substring(path.lastIndexOf("/")+1);
            File datafolder = Environment.getExternalStoragePublicDirectory(getString(R.string.data_storage_folder));
            if (!datafolder.exists()) {
                datafolder.mkdirs();
                File nomediaFile = new File(datafolder, ".nomedia");
                try {
                    nomediaFile.createNewFile();
                }
                catch (IOException e) {
                    Log.w(TAG, "Failed to create nomedia");
                }
            }
            String dstpath = datafolder + "/" + filename;
            try {
                copyFile(path, dstpath);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String thumbnailpath = getThumbnail(path);

            mDB.createRecords(dstpath, thumbnailpath);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (thumbcursor != null) {
                thumbcursor.close();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // When an Image is picked
            if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri uri = item.getUri();
                        //In case you need image's absolute path
                        saveData(getApplicationContext(), uri);
                    }
                    //mGridAdapter.updateView(mImageItems);
                    mGridView.invalidateViews();
                }

            } else if (requestCode == RESULT_SET_PASSWORD) {
                if (resultCode == RESULT_OK
                    && null != data) {
                    Bundle bundle = data.getExtras();
                    mPassword = bundle.getString("password");
                    SharedPreferences.Editor editor = mSettings.edit();
                    editor.putString("password", mPassword);

                    // Commit the edits!
                    editor.commit();
                    startGalleryIntent();
                } else {
                    Toast.makeText(this,getString(R.string.password_not_set),Toast.LENGTH_LONG)
                            .show();
                }
            } else if (requestCode == RESULT_VALIDATE_PASSWORD) {
                if (resultCode == RESULT_OK) {
                    setLocalGalleryView();
                }
                else {
                    finish();
                }
            } else {
                    Toast.makeText(this, "Unexpected result",
                            Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

    }
}
