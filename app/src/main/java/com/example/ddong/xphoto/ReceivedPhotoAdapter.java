package com.example.ddong.xphoto;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ddong on 04/11/15.
 */
public class ReceivedPhotoAdapter extends BaseAdapter implements View.OnClickListener {
    private final static String TAG = "ReceivedPhotoAdapter";
    private final static int MAX_ARRAY_SIZE = 20;
    /*********** Declare Used Variables *********/

    private static LayoutInflater inflater=null;
    private Context mContext;
    private Activity mActivity;
    private Point mScreenSize = new Point();
    ArrayList<ImageItem> mData;

    /*************  CustomAdapter Constructor *****************/
    public ReceivedPhotoAdapter(Activity activity, ArrayList<ImageItem> data) {
        mActivity = activity;
        mContext = activity.getApplicationContext();
        mData = data;

        Display display = activity.getWindowManager().getDefaultDisplay();
        display.getRealSize(mScreenSize);
        /***********  Layout inflator to call external xml layout () ***********/
        inflater = ( LayoutInflater )mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /******** What is the size of Passed Arraylist Size ************/
    public int getCount() {
        return mData.size();
    }

    public Object getItem(int position) {
        Log.d(TAG,"getItem at: " + position);
        return position;
    }

    public long getItemId(int position) {
        Log.d(TAG, "getItemId at: " + position);
        return position;
    }

    /********* Create a holder Class to contain inflated xml file elements *********/
    public static class ViewHolder{

        public TextView textOwner;
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
            //holder.expireTimer = (TextClock) vi.findViewById(R.id.expire_count_down_txt);
            holder.image=(ImageView)vi.findViewById(R.id.received_photo_thumb);

            /************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
        }
        else
            holder=(ViewHolder)vi.getTag();


        /***** Get each Model object from Arraylist ********/
        ImageItem item = mData.get(position);

        /************  Set Model values in Holder elements ***********/
        Log.d(TAG, "Position: " + position + ", mData owner:  "+item.getOwner());

        holder.textOwner.setText(item.getOwner());
        holder.image.setImageBitmap(decodeBitmap(item.getPath()));
        //holder.expireTimer.setFormat24Hour("");
        //holder.image.setImageBitmap(item.getImage());
        //DecodingTaskParams params = new DecodingTaskParams(item.getPath(), holder.image);
        //new BitmapDecodingTask().execute(params);


        /******** Set Item Click Listner for LayoutInflater for each row *******/
        vi.setOnClickListener(new OnItemClickListener( position ));

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

    private static class DecodingTaskParams {
        String path;
        ImageView view;

        DecodingTaskParams(String path, ImageView view) {
            this.path = path;
            this.view = view;
        }
    }

    private class BitmapDecodingTask extends AsyncTask<DecodingTaskParams, Void, Bitmap> {
        ImageView view;
        String datapath;

        @Override
        protected Bitmap doInBackground(DecodingTaskParams... params) {
            DecodingTaskParams param = params[0];
            view = param.view;
            datapath = param.path;

            // params comes from the execute() call: params[0] is the url.
            Log.d(TAG, "BitmapDecodingTask path: " + datapath);
            return decodeBitmap(datapath);
        }

        @Override
        protected void onPostExecute(Bitmap result)
        {
            /**************** Create Custom Adapter *********/
            view.setImageBitmap(result);
        }
    }

    public int calculateInSampleSize(
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

    public Bitmap decodeBitmap(String datapath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(datapath, options);
        options.inSampleSize = calculateInSampleSize(options, mScreenSize.x, mScreenSize.y);
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(datapath, options);
        Log.d(TAG, "BitmapDecodingTask decoded size: " + options.outWidth + ", " + options.outHeight);
        return bitmap;
    }

}
