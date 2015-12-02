package com.bignerdranch.android.hellomoon;

import android.content.Context;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;

/**
 * @author Zhuo
 */
public class AudioPlayer {

    private MediaPlayer mPlayer;
    private boolean isPause;
    private SurfaceHolder mSurfaceHolder;

    public void stop() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
            isPause = false;
        }
    }

    public void play(Context c) {
        if (isPause == true) {
            mPlayer.start();
        } else {
            stop();
            mPlayer = MediaPlayer.create(c, R.raw.apollo_17_stroll);
            mPlayer.setDisplay(mSurfaceHolder);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });
            mPlayer.start();
        }
        isPause = false;
    }

    public void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
            isPause = true;
        }
    }

    public boolean isPlaying() {
        if (mPlayer != null) {
            return mPlayer.isPlaying();
        }
        return false;
    }

    public void setSurfaceHolder(SurfaceHolder surfaceHolder) {
        this.mSurfaceHolder = surfaceHolder;
    }
}
