package com.bignerdranch.android.draganddraw;

import android.graphics.PointF;

/**
 * @author Zhuo
 *         2015/12/13
 */
public class Box {
    private PointF mOrigin;
    private PointF mCurrent;
    private float mDegrees;

    public Box(PointF origin) {
        mOrigin = mCurrent = origin;
    }

    public PointF getCurrent() {
        return mCurrent;
    }

    public void setCurrent(PointF current) {
        mCurrent = current;
    }

    public PointF getOrigin() {
        return mOrigin;
    }

    public float getDegrees() {
        return mDegrees;
    }

    public void setDegrees(float degrees) {
        mDegrees = degrees;
    }

    public PointF getCenter() {
        float middleX = (mCurrent.x + mOrigin.x) / 2.0f;
        float middleY = (mCurrent.y + mOrigin.y) / 2.0f;
        return new PointF(middleX, middleY);
    }

}
