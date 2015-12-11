package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Zhuo
 *         2015/12/11
 */
public class NotificationReceiver extends BroadcastReceiver {

    private static final String TAG = "NotificationReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Got a resultCode: " + getResultCode());
        if (Activity.RESULT_OK != getResultCode()) {
            return;
        }

        int requestCode = intent.getIntExtra("REQUEST_CODE", 0);
        Notification notification = intent.getParcelableExtra("NOTIFICATION");
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(requestCode, notification);
    }
}
