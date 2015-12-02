package com.bignerdranch.android.criminalintent;

import android.content.res.Configuration;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * @author Zhuo
 *         2015/11/20
 */
public class ImageFragment extends DialogFragment {

    public static final String EXTRA_IMAGE_PATH = "com.bignerdranch.android.criminalintent.image_path";
    public static final String EXTRA_IMAGE_ORIENTATION = "com.bignerdranch.android.criminalintent.image_orientation";
    private ImageView mImageView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mImageView = new ImageView(getActivity());
        String path = (String) getArguments().getSerializable(EXTRA_IMAGE_PATH);
        int orientation = getArguments().getInt(EXTRA_IMAGE_ORIENTATION);

        BitmapDrawable image = PictureUtils.getScaledDrawable(getActivity(), path);
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            image = PictureUtils.getPortraitDrawable(mImageView, image);
        }

        mImageView.setImageDrawable(image);
        return mImageView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PictureUtils.cleanImageView(mImageView);
    }

    public static ImageFragment newInstance(String imagePath, int orientation) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_IMAGE_PATH, imagePath);
        args.putInt(EXTRA_IMAGE_ORIENTATION, orientation);

        ImageFragment fragment = new ImageFragment();
        fragment.setArguments(args);
        fragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        return fragment;
    }
}
