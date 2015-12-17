package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.location.Location;

/**
 * @author Zhuo
 *         2015/12/17
 */
public class LocationLoader extends DataLoader<Location> {
    private long mRunId;

    public LocationLoader(Context context, long runId) {
        super(context);
        mRunId = runId;
    }

    @Override
    public Location loadInBackground() {
        return RunManager.get(getContext()).getLastLocationForRun(mRunId);
    }
}
