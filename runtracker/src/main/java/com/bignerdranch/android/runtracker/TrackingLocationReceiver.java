package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.location.Location;

/**
 * @author Zhuo
 *         2015/12/15
 */
public class TrackingLocationReceiver extends LocationReceiver {

    @Override
    protected void onLocationReceived(Context context, Location location) {
        RunManager.get(context).insertLocation(location);
    }
}
