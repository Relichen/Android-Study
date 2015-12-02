package com.bignerdranch.android.nerdlauncher;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * @author Zhuo
 *         2015/11/29
 */
public class RunningTask extends ListFragment {
    private List<ActivityManager.RunningTaskInfo> mRunningApps;
    private ActivityManager mActivityManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivityManager = (ActivityManager) getActivity().getSystemService(Activity
                .ACTIVITY_SERVICE);

        try {
            mRunningApps = mActivityManager.getRunningTasks(Integer.MAX_VALUE);
        } catch (Exception e) {
            Toast.makeText(getActivity(), "no running processes1", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        if (mRunningApps != null) {
            setListAdapter(new MyAdapter(mRunningApps));
        } else {
            Toast.makeText(getActivity(), "no running processes2", Toast.LENGTH_LONG).show();
        }
        return super.onCreateView(inflater, container, savedInstanceState);

    }

    private class MyAdapter extends ArrayAdapter<ActivityManager.RunningTaskInfo> {

        public MyAdapter(List<ActivityManager.RunningTaskInfo> objects) {
            super(getActivity(), 0, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().inflate(R.layout
                        .fragment_activity_item_list, null);
            }

            ActivityManager.RunningTaskInfo runningTaskInfo = getItem(position);
            PackageManager packageManager = getActivity().getPackageManager();
            ApplicationInfo appInfo;
            try {
                appInfo = packageManager.getApplicationInfo(runningTaskInfo.baseActivity
                        .getPackageName(), PackageManager.GET_META_DATA);

                TextView textView = (TextView) convertView.findViewById(R.id.activity_item_name);
                ImageView imageView = (ImageView) convertView.findViewById(R.id.activity_item_image);
                textView.setText(appInfo.loadLabel(packageManager));
                imageView.setImageDrawable(appInfo.loadIcon(packageManager));

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            return convertView;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ActivityManager.RunningTaskInfo runningTaskInfo = (ActivityManager.RunningTaskInfo)
                getListAdapter().getItem(position);
//        PackageManager packageManager = getActivity().getPackageManager();
//        ApplicationInfo appInfo;
        try {
//            appInfo = packageManager.getApplicationInfo(runningTaskInfo.baseActivity
//                    .getPackageName(), PackageManager.GET_META_DATA);
            mActivityManager.moveTaskToFront(runningTaskInfo.id, ActivityManager
                    .MOVE_TASK_WITH_HOME);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
