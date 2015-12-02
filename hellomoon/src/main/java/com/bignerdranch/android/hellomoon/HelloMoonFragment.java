package com.bignerdranch.android.hellomoon;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * @author Zhuo
 */
public class HelloMoonFragment extends Fragment{

    private Button mPlayButton;
    private Button mStopButton;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private AudioPlayer mAudioPlayer = new AudioPlayer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_hello_moon, container, false);

        mSurfaceView = (SurfaceView) v.findViewById(R.id.hellomoon_surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mAudioPlayer.setSurfaceHolder(mSurfaceHolder);

        mPlayButton = (Button) v.findViewById(R.id.hellomoon_playButton);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAudioPlayer.isPlaying()) {
                    mAudioPlayer.pause();
                    mPlayButton.setText(R.string.hellomoon_play);
                } else {
                    mAudioPlayer.play(getActivity());
                    mPlayButton.setText(R.string.hellomoon_pause);
                }
            }
        });
        mStopButton = (Button) v.findViewById(R.id.hellomoon_stopButton);
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAudioPlayer.stop();
                mPlayButton.setText(R.string.hellomoon_play);
            }
        });

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioPlayer.stop();
    }
}
