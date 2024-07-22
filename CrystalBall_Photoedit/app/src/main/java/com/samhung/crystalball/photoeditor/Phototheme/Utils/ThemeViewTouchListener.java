package com.samhung.crystalball.photoeditor.Phototheme.Utils;

import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import com.math.photostickersdk.ScaleGestureDetector;
import com.math.photostickersdk.Vector2D;
import com.samhung.crystalball.photoeditor.Utilis.MeasureUtil;

public class ThemeViewTouchListener implements View.OnTouchListener {
    public static final String TAG = "MainTouchListener";
    static final int NONE = 0x100;
    static final int DRAG = 0x101;
    static final int ZOOM = 0x102;

    int mode = NONE;
    private static final int INVALID_POINTER_ID = -1;
    private boolean isRotateEnabled = true;
    private boolean isTranslateEnabled = true;
    private boolean isScaleEnabled = true;
    private float minimumScale = 1.0f;
    private float maximumScale = 10.0f;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int[] location = new int[2];

    private float mPrevX, mPrevY, mPrevRawX, mPrevRawY;
    private ScaleGestureDetector mScaleGestureDetector;

    public ThemeViewTouchListener(boolean rotateEnabled) {
        isRotateEnabled = rotateEnabled;
        mScaleGestureDetector = new ScaleGestureDetector(new ScaleGestureListener());
    }

    public ThemeViewTouchListener() {
        this(true);
    }

    private static float adjustAngle(float degrees) {
        if (degrees > 180.0f) {
            degrees -= 360.0f;
        } else if (degrees < -180.0f) {
            degrees += 360.0f;
        }

        return degrees;
    }

    private static void move(View view, TransformInfo info) {
        computeRenderOffset(view, info.pivotX, info.pivotY);
        adjustTranslation(view, info.deltaX, info.deltaY);

        float scale = view.getScaleX() * info.deltaScale;
        scale = Math.max(info.minimumScale, Math.min(info.maximumScale, scale));
        view.setScaleX(scale);
        view.setScaleY(scale);

//        float rotation = adjustAngle(view.getRotation() + info.deltaAngle);
//        view.setRotation(rotation);
    }

    private static void adjustTranslation(View view, float deltaX, float deltaY) {
        float[] deltaVector = {deltaX, deltaY};
        view.getMatrix().mapVectors(deltaVector);
        view.setTranslationX(view.getTranslationX() + deltaVector[0]);
        view.setTranslationY(view.getTranslationY() + deltaVector[1]);
    }

    private static void computeRenderOffset(View view, float pivotX, float pivotY) {
        if (view.getPivotX() == pivotX && view.getPivotY() == pivotY) {
            return;
        }

        float[] prevPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(prevPoint);

        view.setPivotX(pivotX);
        view.setPivotY(pivotY);

        float[] currPoint = {0.0f, 0.0f};
        view.getMatrix().mapPoints(currPoint);

        float offsetX = currPoint[0] - prevPoint[0];
        float offsetY = currPoint[1] - prevPoint[1];

        view.setTranslationX(view.getTranslationX() - offsetX);
        view.setTranslationY(view.getTranslationY() - offsetY);
    }

    private void moveCorrectPosition(View view)
    {
        int[] pos = new int[2];
        view.getLocationOnScreen(pos);
        float scaledWidth = view.getWidth() * view.getScaleX();
        float scaledHeight = view.getHeight() * view.getScaleY();
        float width = view.getWidth();
        float height = view.getHeight();
        DisplayMetrics dm = new DisplayMetrics();
         view.getDisplay().getMetrics(dm);
        float displayWidth = dm.widthPixels;
        float displayHeight = dm.heightPixels - MeasureUtil.convertDpToPixels(150, view.getContext());
        if(scaledWidth <= displayWidth) {
            view.setPivotX(0);
            view.setX((displayWidth - scaledWidth) / 2);
        }
        else
        {
           // Log.e("eeeee----", ""+pos[0]+"    "+width+ "    "+scaledWidth+ "     "+displayWidth);
            if(pos[0]>0) {
                view.setPivotX(0);
                view.setX(0);
            }
            if(pos[0] + scaledWidth < displayWidth ){
                view.setPivotX(0);
                view.setX(displayWidth-scaledWidth );
            }
        }

     //   Log.e("eeeee----", ""+pos[1]+"    "+height+ "    "+scaledHeight+ "     "+displayHeight);
        if(scaledHeight <= displayHeight) {
            view.setPivotY(0);
            view.setY((displayHeight - scaledHeight) / 2);
        }
        else
        {
            if(pos[1]>0) {
                view.setPivotY(0);
                view.setY(0);
            }
            if(pos[1] + scaledHeight < displayHeight){
                view.setPivotY(0);
                view.setY(displayHeight-scaledHeight);
            }
        }

    }
    @Override
    public boolean onTouch(View view, MotionEvent event) {

        mScaleGestureDetector.onTouchEvent(view, event);

        if (!isTranslateEnabled) {
            return true;
        }

        int action = event.getAction();

        int x = (int) event.getRawX();
        int y = (int) event.getRawY();

        switch (action & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mPrevX = event.getX();
                mPrevY = event.getY();
                mPrevRawX = event.getRawX();
                mPrevRawY = event.getRawY();
                mActivePointerId = event.getPointerId(0);
                //view.bringToFront();
                break;
            case MotionEvent.ACTION_MOVE:
                int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                if (pointerIndexMove != -1) {
                    float currX = event.getX(pointerIndexMove);
                    float currY = event.getY(pointerIndexMove);
                    if (!mScaleGestureDetector.isInProgress()) {
                        adjustTranslation(view, currX - mPrevX, currY - mPrevY);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
//                    view.animate().translationY(0).translationY(0);

                moveCorrectPosition(view);
                float mCurrentCancelX = event.getRawX();
                float mCurrentCancelY = event.getRawY();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                int pointerIndexPointerUp = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                int pointerId = event.getPointerId(pointerIndexPointerUp);
                if (pointerId == mActivePointerId) {
                    int newPointerIndex = pointerIndexPointerUp == 0 ? 1 : 0;
                    mPrevX = event.getX(newPointerIndex);
                    mPrevY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
        }

        return true;
    }
    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        private float mPivotX;
        private float mPivotY;
        private Vector2D mPrevSpanVector = new Vector2D();

        @Override
        public boolean onScaleBegin(View view, ScaleGestureDetector detector) {
            mPivotX = detector.getFocusX();
            mPivotY = detector.getFocusY();
            mPrevSpanVector.set(detector.getCurrentSpanVector());
            return true;
        }

        @Override
        public boolean onScale(View view, ScaleGestureDetector detector) {
            TransformInfo info = new TransformInfo();
            info.deltaScale = isScaleEnabled ? detector.getScaleFactor() : 1.0f;
            info.deltaAngle = isRotateEnabled ? Vector2D.getAngle(mPrevSpanVector, detector.getCurrentSpanVector()) : 0.0f;
            info.deltaX = isTranslateEnabled ? detector.getFocusX() - mPivotX : 0.0f;
            info.deltaY = isTranslateEnabled ? detector.getFocusY() - mPivotY : 0.0f;
            info.pivotX = mPivotX;
            info.pivotY = mPivotY;
            info.minimumScale = minimumScale;
            info.maximumScale = maximumScale;
            move(view, info);
            return false;
        }
    }
    private class TransformInfo {
        float deltaX;
        float deltaY;
        float deltaScale;
        float deltaAngle;
        float pivotX;
        float pivotY;
        float minimumScale;
        float maximumScale;
    }
}
