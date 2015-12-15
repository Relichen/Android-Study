package com.bignerdranch.android.runtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Zhuo
 *         2015/12/14
 */
public class RunFragment extends Fragment {
    private RunManager mRunManager;
    private Location mLastLocation;
    private Run mRun;
    private Button mStartButton, mStopButton;
    private TextView mStartedTextView, mLatitudeTextView,
                mLongitudeTextView, mAltitudeTextView, mDurationTextView;

    private BroadcastReceiver mLocationReceiver = new LocationReceiver(){
        @Override
        protected void onLocationReceived(Context context, Location location) {
            mLastLocation = location;
            if (isVisible()) {
                updateUI();
            }
        }

        @Override
        protected void onProviderEnabledChanged(boolean enabled) {
            int toastText = enabled ? R.string.gps_enabled : R.string.gps_disabled;
            Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mRunManager = RunManager.get(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_run, container, false);

        mStartedTextView = (TextView) v.findViewById(R.id.run_startedTextView);
        mLatitudeTextView = (TextView) v.findViewById(R.id.run_latitudeTextView);
        mLongitudeTextView = (TextView) v.findViewById(R.id.run_longitudeTextView);
        mAltitudeTextView = (TextView) v.findViewById(R.id.run_altitudeTextView);
        mDurationTextView = (TextView) v.findViewById(R.id.run_durationTextView);

        mStartButton = (Button) v.findViewById(R.id.run_startButton);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRunManager.startLocationUpdates();
                mRun = new Run();
                updateUI();
            }
        });

        mStopButton = (Button) v.findViewById(R.id.run_stopButton);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRunManager.stopLocationUpdates();
                updateUI();
            }
        });

        updateUI();

        return v;
    }

    private void updateUI() {
        boolean started = mRunManager.isTrackingRun();

        if (mRun != null) {
            mStartedTextView.setText(DateFormat.format("yyyy年MM月dd日 EEEE kk:mm", mRun.getStartDate()));
        }

        int durationSeconds = 0;
        if (mRun != null && mLastLocation != null) {
            durationSeconds = mRun.getDurationSeconds(mLastLocation.getTime());
            mLatitudeTextView.setText(Double.toString(mLastLocation.getLatitude()));
            mLongitudeTextView.setText(Double.toString(mLastLocation.getLongitude()));
            mAltitudeTextView.setText(Double.toString(mLastLocation.getAltitude()));
        }
        mDurationTextView.setText(Run.formatDuration(durationSeconds));

        mStartButton.setEnabled(!started);
        mStopButton.setEnabled(started);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(RunManager.ACTION_LOCATION);
        getActivity().registerReceiver(mLocationReceiver, intentFilter);
    }

    @Override
    public void onStop() {
        getActivity().unregisterReceiver(mLocationReceiver);
        super.onStop();
    }
}
