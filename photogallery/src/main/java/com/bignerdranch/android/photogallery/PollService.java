package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

/**
 * @author Zhuo
 *         2015/12/8
 */
public class PollService extends IntentService {

    private static final String TAG = "PollService";

    public static final String ACTION_SHOW_NOTIFICATION =
            "com.bignerdranch.android.photogallery.SHOW_NOTIFICATION";

    public static final String PERM_PRIVATE = "com.bignerdranch.android.photogallery.PRIVATE";

    private static final int POLL_INTERVAL = 1000 * 10; // 10 second
    public static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public PollService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressWarnings("deprecation")
        boolean isNetworkAvailable = cm.getBackgroundDataSetting() && cm.getActiveNetworkInfo() != null;

        if (!isNetworkAvailable) {
            return;
        }

        Log.i(TAG, "Received an intent: " + intent);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String query = preferences.getString(FlickrFetchr.PREF_SEARCH_QUERY, null);
        String lastResultId = preferences.getString(FlickrFetchr.PREF_LAST_RESULT_ID, null);

        ArrayList<GalleryItem> items;
        if (query != null) {
            items = new FlickrFetchr().search(query);
        } else {
            items = new FlickrFetchr().fetchItem(0);
        }

        if (items.size() == 0) {
            Log.i(TAG, "接收 ResultId 因得到结果为空返回");
            Resources r = getResources();
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                    PhotoGalleryActivity.class), 0);

            Notification notification = new NotificationCompat.Builder(this)
                    .setTicker(r.getString(R.string.new_pictures_title))
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(r.getString(R.string.new_pictures_title))
                    .setContentText(r.getString(R.string.new_pictures_text))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build();

            showBackgroundNotification(0, notification);

//            sendBroadcast(new Intent(PollService.ACTION_SHOW_NOTIFICATION), PollService.PERM_PRIVATE);

            return;
        }

        String resultId = items.get(0).getId();
        if (!resultId.equals(lastResultId)) {
            Log.i(TAG, "Got a new ResultId: " + resultId);

//            因为在这里没法验证通知栏是否好用，就把他们放到上边了

//            Resources r = getResources();
//            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
//                    PhotoGalleryActivity.class), 0);
//
//            Notification notification = new NotificationCompat.Builder(this)
//                    .setTicker(r.getString(R.string.new_pictures_title))
//                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
//                    .setContentTitle(r.getString(R.string.new_pictures_title))
//                    .setContentText(r.getString(R.string.new_pictures_text))
//                    .setContentIntent(pendingIntent)
//                    .setAutoCancel(true)
//                    .build();

//            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            notificationManager.notify(0, notification);

        } else {
            Log.i(TAG, "Got a old ResultId: " + resultId);
        }

        preferences.edit().putString(FlickrFetchr.PREF_LAST_RESULT_ID, resultId).commit();
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(PollService.ACTION_SHOW_NOTIFICATION);
        i.putExtra("REQUEST_CODE", requestCode);
        i.putExtra("NOTIFICATION", notification);

        sendOrderedBroadcast(i, PollService.PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);

    }

    public static void setServiceAlarm(Context context, boolean isOn) {

        Intent i = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), POLL_INTERVAL, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel();
        }

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(PREF_IS_ALARM_ON, isOn).commit();

    }

    public static boolean isAlarmServiceOn(Context context) {
        Intent i = new Intent(context, PollService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pendingIntent != null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "service destroy");
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "service create");
        super.onCreate();
    }
}
