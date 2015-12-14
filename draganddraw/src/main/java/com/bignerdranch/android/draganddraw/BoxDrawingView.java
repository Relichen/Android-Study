package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * @author Zhuo
 *         2015/12/13
 */
public class BoxDrawingView extends View {

    private static final String TAG = "BoxDrawingView";
    private static final String KEY_BOXES = "key_boxes";
    private static final String KEY = "key";

    private ArrayList<Box> mBoxes = new ArrayList<>();
    private Box mCurrentBox;
    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    private float originRotation;
    private float rotation;

    private static final int NONE = 0;
    private static final int DRAW = 1;
    private static final int ROTATION = 2;
    int mode = NONE;

    public BoxDrawingView(Context context) {
        this(context, null);
    }

    //    从xml生成视图时使用
    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setId(R.id.boxDrawingView);

        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xff8efe0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF curr = new PointF(event.getX(), event.getY());

        Log.i(TAG, "Received event at x=" + curr.x + " , y=" + curr.y + ":");

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                Log.i(TAG, " ACTION_DOWN");
                mode = DRAW;
                mCurrentBox = new Box(curr);
                mBoxes.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.i(TAG, " ACTION_POINTER_DOWN");

                mode = ROTATION;
//                当双指都放在屏幕上时，将之前新建的box移除，并将当前box设为上一个画出来的box
                mBoxes.remove(mBoxes.size() - 1);
                if (mBoxes.size() == 0) {
                    break;
                }
                mCurrentBox = mBoxes.get(mBoxes.size() - 1);
                Toast.makeText(getContext(), "mCurrentBox.degrees()=" + mCurrentBox.getDegrees(), Toast
                        .LENGTH_SHORT).show();

                double delta_x1 = (event.getX(event.findPointerIndex(0)) - event.getX(event.findPointerIndex(1)));
                double delta_y1 = (event.getY(event.findPointerIndex(0)) - event.getY(event.findPointerIndex(1)));
                double radians1 = Math.atan2(delta_y1, delta_x1);
                originRotation = (float) Math.toDegrees(radians1);

                break;

            case MotionEvent.ACTION_MOVE:
                Log.i(TAG, " ACTION_MOVE");

                if (mCurrentBox == null) {
                    Log.i(TAG, "mCurrentBox == null ");
                    break;
                }

                if (mode == DRAW) {
                    mCurrentBox.setCurrent(curr);

                } else if (mode == ROTATION) {

//              取旋转角度
                    double delta_x = (event.getX(event.findPointerIndex(0)) - event.getX(event.findPointerIndex(1)));
                    double delta_y = (event.getY(event.findPointerIndex(0)) - event.getY(event.findPointerIndex(1)));
                    double radians = Math.atan2(delta_y, delta_x);
                    rotation = (float) Math.toDegrees(radians);

                    mCurrentBox.setDegrees(rotation - originRotation);

                    Log.i(TAG, "rotation: " + rotation);
                }
                invalidate();

                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i(TAG, " ACTION_CANCEL");
                mCurrentBox = null;
                break;

            case MotionEvent.ACTION_POINTER_UP:
                Log.i(TAG, " ACTION_POINTER_UP");
                mCurrentBox.setDegrees(rotation - originRotation);
                mode = NONE;
                break;
            case MotionEvent.ACTION_UP:
                Log.i(TAG, " ACTION_UP");
                mCurrentBox = null;
                mode = NONE;
                originRotation = rotation = 0;
                break;
        }
        return true;
    }

    /*
        有一个极其严重的问题：第一次旋转方块好使，想接着上次的角度继续旋转时
        将双指放在屏幕上后，这个方块就变回第一次旋转前的状态重新旋转

        我的猜测是：由于在旋转前调用了canvas.save()方法保存旋转前的状态(即旋转前的坐标轴)
        且在旋转后用restore()恢复，因此第二次旋转时的坐标轴是正常的
        而保存的box的Current和Origin点都是未旋转时的位置
        所以在onDraw()方法里取到的left,right,top,bottom都是相对正常坐标系的位置
        再次绘制时又从正常的位置开始绘制了，而且在ACTION_MOVE中设置box的角度值
        ACTION_MOVE动一点点也算动...又把角度值设回去了...
    */


    @Override
    protected void onDraw(Canvas canvas) {

        canvas.drawPaint(mBackgroundPaint);

        for (Box box : mBoxes) {

            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y, box.getCurrent().y);

            canvas.save();
            Log.i(TAG, "mCurrentBox is rotation " + box.getDegrees());
            canvas.rotate(box.getDegrees(), box.getCenter().x, box.getCenter().y);
            canvas.drawRect(left, top, right, bottom, mBoxPaint);
            canvas.restore();

        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Log.i(TAG, "onSaveInstanceState");
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY, super.onSaveInstanceState());
        bundle.putSerializable(KEY_BOXES, mBoxes);
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Log.i(TAG, "onRestoreInstanceState");
        Bundle bundle = (Bundle) state;
        //noinspection unchecked
        mBoxes = (ArrayList<Box>) bundle.getSerializable(KEY_BOXES);
        super.onRestoreInstanceState(bundle.getParcelable(KEY));
    }
}
