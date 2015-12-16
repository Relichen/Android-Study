package com.bignerdranch.android.runtracker;

import android.support.v4.app.Fragment;

/**
 * @author Zhuo
 *         2015/12/15
 */
public class RunListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new RunListFragment();
    }
}
