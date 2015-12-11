package com.bignerdranch.android.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Zhuo
 *         2015/12/11
 */
public class VisibleFragment extends Fragment {

    private static final String TAG = "visibleFragment";

    private BroadcastReceiver mOnShowNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "Got a broadcast: " + intent.getAction(), Toast.LENGTH_LONG).show();

//            如果我们在这个界面上，就取消notification
            Log.i(TAG, "canceling notification");
            setResultCode(Activity.RESULT_CANCELED);
        }
    };
    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
        getActivity().registerReceiver(mOnShowNotificationReceiver, intentFilter, PollService
                .PERM_PRIVATE, null);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mOnShowNotificationReceiver);
    }
}
