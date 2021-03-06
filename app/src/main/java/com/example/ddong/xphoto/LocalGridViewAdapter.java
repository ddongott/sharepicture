package com.example.ddong.xphoto;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by ddong on 2015-09-13.
 */
public class LocalGridViewAdapter extends ArrayAdapter {
    private final static String TAG = "GridView";
    private Context context;
    private int layoutResourceId;
    private ArrayList data = new ArrayList();
    //private GridView mView = null;

    public LocalGridViewAdapter
            (Context context, int layoutResourceId, ArrayList data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();
            holder.imageTitle = (TextView) row.findViewById(R.id.text);
            holder.image = (ImageView) row.findViewById(R.id.image);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        ImageItem item = (ImageItem) data.get(position);
        holder.imageTitle.setText(item.getTitle());
        holder.image.setImageBitmap(item.getImage());
        //mView = row;
        return row;
    }

    public void updateView(ArrayList data) {
        this.data = data;
        //if(mView != null) {
        //    mView.invalidate();
        //}
    }

    static class ViewHolder {
        TextView imageTitle;
        ImageView image;
    }
}
