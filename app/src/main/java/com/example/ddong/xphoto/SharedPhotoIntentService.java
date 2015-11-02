package com.example.ddong.xphoto;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;

/**
 * Created by ddong on 30/10/15.
 */
public class SharedPhotoIntentService extends IntentService {
    private static final String TAG = "SPIntentService";
    private static final String[] TOPICS = {"global"};

    public SharedPhotoIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String msg = intent.getStringExtra("message");
        final XPDatabaseOperation hDb = new XPDatabaseOperation(getApplicationContext(),SharedPhotoManager.TABLE_NAME);
        HttpHelper.HttpDownloadCallback callback = new HttpHelper.HttpDownloadCallback() {
            @Override
            public void onTaskCompleted(JSONObject results) {
                JSONObject jobj = new JSONObject();
                String photourl = null;
                try {
                    String id = results.getString("id");
                    jobj.put("id",id);
                    String owner = results.getString("owner");
                    jobj.put("owner", owner);
                    photourl = results.getString("data");
                    if (! photourl.isEmpty()) {
                        HttpHelper.getInstance().downloadData(photourl);
                        String fileName = photourl.substring(photourl.lastIndexOf("/") + 1,
                                photourl.length());
                        jobj.put("data", fileName);
                    }
                    Log.d(TAG, jobj.toString());
                    hDb.createRecords(jobj);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        HttpHelper.getInstance().downloadMessage(msg, callback);
    }
}
