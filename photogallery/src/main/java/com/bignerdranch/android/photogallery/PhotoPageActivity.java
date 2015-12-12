package com.bignerdranch.android.photogallery;

import android.support.v4.app.Fragment;

/**
 * @author Zhuo
 *         2015/12/12
 */
public class PhotoPageActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new PhotoPageFragment();
    }
}
