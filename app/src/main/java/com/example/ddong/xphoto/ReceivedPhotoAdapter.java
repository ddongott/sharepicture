package com.example.ddong.xphoto;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;

import 	android.app.ProgressDialog;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ddong on 04/11/15.
 */
public class ReceivedPhotoAdapter extends BaseAdapter implements View.OnClickListener {
    private final static String TAG = "ReceivedPhotoAdapter";
    private final static int MAX_ARRAY_SIZE = 20;
    /*********** Declare Used Variables *********/
    private ArrayList<ImageItem> mData;
    private XPDatabaseOperation mDB;
    Cursor mCursor = null;
    private static LayoutInflater inflater=null;
    private LoadDataTask mPendingTask = null;
    private ProgressDialog mWaitingDialog;
    private int mDataOffset;

    /*************  CustomAdapter Constructor *****************/
    public ReceivedPhotoAdapter(Context context, ListView list) {

        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        list.setOnScrollListener(new RPScrollListener());
        mWaitingDialog = new ProgressDialog(context);
        mDB = new XPDatabaseOperation(context,XPDatabaseHelper.RECEIVED_TABLE_NAME);
        String[] cols = new String[] {"_id", "serverid", "owner","data"};
        mCursor = mDB.selectRecords(cols);
        mData = new ArrayList<>();
        mDataOffset = 0;
        new LoadDataTask().execute(0);
    }

    /******** What is the size of Passed Arraylist Size ************/
    public int getCount() {
        return mCursor.getCount();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{

        public TextView textOwner;
        public TextClock expireTimer;
        public ImageView image;

    }

    /****** Depends upon data size called for each row , Create each ListView row *****/
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ViewHolder holder;

        if(convertView==null){

            /****** Inflate received_photo_item_layout.xml file for each row ( Defined below ) *******/
            vi = inflater.inflate(R.layout.received_photo_item_layout, null);

            /****** View Holder Object to contain received_photo_item_layout.xml file elements ******/

            holder = new ViewHolder();
            holder.textOwner = (TextView) vi.findViewById(R.id.received_photo_owner_txt);
            holder.expireTimer = (TextClock) vi.findViewById(R.id.expire_count_down_txt);
            holder.image=(ImageView)vi.findViewById(R.id.received_photo_thumb);

            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        }
        else
            holder=(ViewHolder)vi.getTag();

        if(mPendingTask != null) {
            mWaitingDialog.show();
        }
        else if(position > mData.size() + mDataOffset || position < mDataOffset)
        {
            if(position > mData.size() + mDataOffset) {
                mDataOffset = position;
                new LoadDataTask().execute(position);
                mWaitingDialog.show();
            }
        }
        else
        {
            /***** Get each Model object from Arraylist ********/
            ImageItem item = ( ImageItem ) mData.get(position);

            /************  Set Model values in Holder elements ***********/

            holder.textOwner.setText(item.getOwner() );
            //holder.expireTimer.setFormat24Hour("");
            holder.image.setImageBitmap(item.getImage());

            /******** Set Item Click Listner for LayoutInflater for each row *******/
            vi.setOnClickListener(new OnItemClickListener( position ));
        }
        return vi;
    }

    @Override
    public void onClick(View v) {
        Log.v(TAG, "=====Row button clicked=====");
    }

    /********* Called when Item click in ListView ************/
    private class OnItemClickListener  implements View.OnClickListener{
        private int mPosition;

        OnItemClickListener(int position){
            mPosition = position;
        }

        @Override
        public void onClick(View arg0) {


            //CustomListViewAndroidExample sct = (CustomListViewAndroidExample)activity;

            /****  Call  onItemClick Method inside CustomListViewAndroidExample Class ( See Below )****/

            //sct.onItemClick(mPosition);
        }
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

    private class LoadDataTask extends AsyncTask<Integer, Integer, ArrayList<ImageItem>> {
        public void LoadDataTask() {
            mPendingTask = this;
        }

        @Override
        protected ArrayList<ImageItem> doInBackground(Integer... params) {
            int position = params[0];

            // params comes from the execute() call: params[0] is the url.
            Log.d(TAG, "LoadDataTask position: " + position);
            try {
                int count = mCursor.getCount() - position;
                int row = count > MAX_ARRAY_SIZE ? MAX_ARRAY_SIZE : count;
                for (int i = position; i < row; i++) {
                    if (isCancelled()) break;
                    mCursor.moveToPosition(i);
                    int id = mCursor.getInt(mCursor.getColumnIndexOrThrow("_id"));
                    String serverid = mCursor.getString(mCursor.getColumnIndexOrThrow("serverid"));
                    String owner = mCursor.getString(mCursor.getColumnIndexOrThrow("owner"));
                    String datapath = mCursor.getString(mCursor.getColumnIndexOrThrow("data"));
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    Bitmap bitmap = BitmapFactory.decodeFile(datapath, options);
                    options.inSampleSize = calculateInSampleSize(options, R.dimen.received_thumb_width, R.dimen.received_thumb_height);
                    options.inJustDecodeBounds = false;
                    bitmap = BitmapFactory.decodeFile(datapath, options);
                    ImageItem item = new ImageItem(bitmap, datapath, id);
                    item.setServerid(serverid);
                    item.setOwner(owner);
                    mData.add(item);
                    publishProgress(i);
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
            mPendingTask = null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<ImageItem> result)
        {
            mPendingTask = null;
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
