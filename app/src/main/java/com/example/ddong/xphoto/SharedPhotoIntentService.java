package com.example.ddong.xphoto;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


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
        Context appContext = getApplicationContext();
        XPUtils.Init(appContext);
        final XPDatabaseOperation hDb = new XPDatabaseOperation(appContext,SharedPhotoManager.TABLE_NAME);
        HttpHelper.HttpTaskCallback callback = new HttpHelper.HttpTaskCallback() {
            @Override
            public void onTaskCompleted(JSONObject results) {
                JSONObject jobj = new JSONObject();
                String photourl = null;
                try {
                    String id = results.getString("id");
                    jobj.put("serverid",id);
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

                    String message = owner + " " + getString(R.string.gcm_notification);
                    sendNotification(message);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        HttpHelper.getInstance().downloadMessage(msg, callback);
    }

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
