package com.example.ddong.xphoto;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

import java.util.ArrayList;

public class ReceivedPhotoActivity extends AppCompatActivity {
    private final static String TAG = "ReceivedPhotoActivity";

    ListView mList;
    private XPDatabaseOperation mDB;
    private ArrayList<ImageItem> mData;
    Cursor mCursor = null;
    private ProgressDialog mWaitingDialog;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_photo_manager);

        mList = (ListView)findViewById(R.id.received_photo_list);
        mList.setOnScrollListener(new RPScrollListener());

        mWaitingDialog = new ProgressDialog(this);

        mContext = getApplicationContext();
        mDB = new XPDatabaseOperation(mContext,XPDatabaseHelper.RECEIVED_TABLE_NAME);
        String[] cols = new String[] {"_id", "serverid", "owner","data"};
        mCursor = mDB.selectRecords(cols);
        mData = new ArrayList<>();
        new LoadDataTask().execute(0);
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

    private class LoadDataTask extends AsyncTask<Integer, Integer, ArrayList<ImageItem>> {

        @Override
        protected ArrayList<ImageItem> doInBackground(Integer... params) {
            int position = params[0];

            // params comes from the execute() call: params[0] is the url.
            Log.d(TAG, "LoadDataTask position: " + position);
            try {
                int count = mCursor.getCount();
                for (int i = 0; i < count; i++) {
                    if (isCancelled()) break;
                    mCursor.moveToPosition(i);
                    int id = mCursor.getInt(mCursor.getColumnIndexOrThrow("_id"));
                    String serverid = mCursor.getString(mCursor.getColumnIndexOrThrow("serverid"));
                    String owner = mCursor.getString(mCursor.getColumnIndexOrThrow("owner"));
                    String filename = mCursor.getString(mCursor.getColumnIndexOrThrow("data"));
                    String datapath = mContext.getFilesDir() + "/" + filename;
                    //BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inJustDecodeBounds = true;
                    //Bitmap bitmap = BitmapFactory.decodeFile(datapath, options);
                    //options.inSampleSize = calculateInSampleSize(options, R.dimen.received_thumb_width, R.dimen.received_thumb_height);
                    //options.inJustDecodeBounds = false;
                    //bitmap = BitmapFactory.decodeFile(datapath, options);
                    ImageItem item = new ImageItem(null, datapath, id);
                    item.setServerid(serverid);
                    item.setOwner(owner);
                    mData.add(item);
                    publishProgress(i);
                    Log.d(TAG, "Add photo from " + owner + " to "+mData.size());
                }
            }
            catch (IllegalArgumentException e){
                Log.e(TAG,e.toString());
            }
            return mData;
        }

        @Override
        protected void onCancelled(ArrayList<ImageItem> result) {
            Log.d(TAG, "onCancelled");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<ImageItem> result)
        {
            /**************** Create Custom Adapter *********/
            mList.setAdapter( new ReceivedPhotoAdapter( mContext, result ) );
            mWaitingDialog.cancel();
        }
    }

    class RPScrollListener implements AbsListView.OnScrollListener {
        private boolean loading = true;

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,int visibleItemCount, int totalItemCount) {
            if (!(loading) && (totalItemCount - visibleItemCount) <= (firstVisibleItem)) {
                Log.d(TAG, "Load Next Page!");
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {}

        public boolean isLoading() {
            return loading;
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
        }

    }
}
