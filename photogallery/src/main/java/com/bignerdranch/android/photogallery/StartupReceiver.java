package com.bignerdranch.android.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * @author Zhuo
 *         2015/12/10
 */
public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());

//        不能这么写，因为isAlarmServiceOn是判断设置定时器的服务是否开启
//        而不是定时器是否开启，而且每次开机时服务肯定是关的，所以每次开机都不会开启定时器了
//        boolean isAlarmOn = PollService.isAlarmServiceOn(context);

        boolean isOn = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(PollService
                .PREF_IS_ALARM_ON, false);
        PollService.setServiceAlarm(context, isOn);
    }
}
