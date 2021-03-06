package com.example.ddong.xphoto;

import android.app.FragmentManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class LocalGalleryActivity extends AppCompatActivity {
    public final static String DB_KEY_PHOTO_ID="_id"; // id
    public final static String DB_KEY_PHOTO_PATH="path";  // path of photo
    public final static String DB_KEY_PHOTO_THUMB="thumbnail";  // path of thumbnail

    private String TAG = "LocalGalleryActivity";
    private static int RESULT_LOAD_IMG = 1;
    private static int RESULT_SET_PASSWORD = 2;
    private static int RESULT_VALIDATE_PASSWORD = 3;
    private GridView mGridView;
    private LocalGridViewAdapter mGridAdapter;
    private XPDatabaseOperation mDB;
    final ArrayList<ImageItem> mImageItems = new ArrayList<>();
    private String mPassword = "";
    private EncriptionUtil mEncription = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPassword = SharePrefHelper.getInstance().getPassword();
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
        mDB = new XPDatabaseOperation(getApplicationContext(),XPDatabaseHelper.LOCAL_TABLE_NAME);

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
                    intent.putExtra("password", mPassword);
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
        mImageItems.add(new ImageItem(bp, null, 0));
        byte[] thumbdata;
        try {
            String[] cols = new String[] {DB_KEY_PHOTO_ID, DB_KEY_PHOTO_PATH, DB_KEY_PHOTO_THUMB};
            cursor = mDB.selectRecords(cols);
            int row = cursor.getCount();
            for(int i = 0; i < row; i++){
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DB_KEY_PHOTO_ID));
                String thumbpath = cursor.getString(cursor.getColumnIndexOrThrow(DB_KEY_PHOTO_THUMB));
                String path = cursor.getString(cursor.getColumnIndexOrThrow(DB_KEY_PHOTO_PATH));
                thumbdata = mEncription.decriptFile(thumbpath);
                Bitmap bitmap = BitmapFactory.decodeByteArray(thumbdata, 0, thumbdata.length);
                mImageItems.add(new ImageItem(bitmap, path, 0));
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
        if (id == R.id.action_accounts) {
            launchAccountInfo();
        }
        else if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void launchAccountInfo() {
        Intent intent = new Intent(LocalGalleryActivity.this, AccountsInfoActivity.class);
        // Start the Intent
        startActivity(intent);
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
        int THUMBSIZE = 128;
        ExifInterface exif = null;
        Bitmap thumbnail;
        byte[] imageData;
        int    imageSize;
        try {
            exif = new ExifInterface(picPath);
        }
        catch (IOException e) {
            Log.w(TAG, "No exif data for: " + picPath);
        }

        if (exif != null && exif.hasThumbnail()) {
            imageData = exif.getThumbnail();
            imageSize = imageData.length;
            thumbnail = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            Log.d(TAG,"Thumbnail size(" + imageData.length + "), resolution(" + thumbnail.getWidth() +
            ":" + thumbnail.getHeight() + ")");
        }
        else {
            thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(picPath), THUMBSIZE, THUMBSIZE);
            ByteArrayOutputStream soutput = new ByteArrayOutputStream(thumbnail.getByteCount());
            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, soutput);
            imageData = soutput.toByteArray();
            imageSize = soutput.size();
        }
        mImageItems.add(new ImageItem(thumbnail, picPath, 0));

        String thumbfilename = picPath.substring(picPath.lastIndexOf("/") + 1);
        File thumbfolder = Environment.getExternalStoragePublicDirectory(getString(R.string.data_storage_folder) + "/.thumbnail");
        if (!thumbfolder.exists()) {
            thumbfolder.mkdirs();
        }
        File fThumb = new File(thumbfolder,thumbfilename);
        mEncription.encriptBytes(imageData, imageSize, fThumb);

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
            mEncription.encriptFile(path, dstpath);

            String thumbnailpath = getThumbnail(path);

            JSONObject object = new JSONObject();
            try {
                object.put(DB_KEY_PHOTO_PATH, dstpath);
                object.put(DB_KEY_PHOTO_THUMB, thumbnailpath);
                mDB.createRecords(object);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
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
                    SharePrefHelper.getInstance().setPassword(mPassword);

                    mEncription = new EncriptionUtil(mPassword);
                    startGalleryIntent();
                } else {
                    Toast.makeText(this,getString(R.string.password_not_set),Toast.LENGTH_LONG)
                            .show();
                }
            } else if (requestCode == RESULT_VALIDATE_PASSWORD) {
                if (resultCode == RESULT_OK) {
                    mEncription = new EncriptionUtil(mPassword);
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
