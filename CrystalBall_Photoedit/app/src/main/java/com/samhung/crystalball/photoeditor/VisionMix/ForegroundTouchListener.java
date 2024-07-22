package com.samhung.crystalball.photoeditor.VisionMix;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.MeasureUtil;
import com.samhung.crystalball.photoeditor.VisionMix.Activities.VisionmixActivity;
import com.samhung.crystalball.photoeditor.VisionMix.Views.ForegroundView;
import com.samhung.crystalball.photoeditor.Widget.NavigatorView;

import org.CrystalBall.Engine;

public class ForegroundTouchListener implements View.OnTouchListener {
    public static final String TAG = "ForegroundTouchListener";
    final int NONE = 0x100;
    final int DRAG = 0x101;
    final int ZOOM = 0x102;
    final int DRAW = 0x103;

    ForegroundView fvPhoto = null;
    Matrix savedMatrix = new Matrix();
    Matrix matrix = new Matrix();
    int mode = NONE;
    boolean isRotationEnabled = true;

    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 0;

    final private float minimumScale = 0.5f;
    final private float maximumScale = 5.0f;
    private float scale = 1;

    Vector2D prevVec = new Vector2D();
    VisionmixActivity mainActivity = null;

    NavigatorView navigatorView = null;

    public ForegroundTouchListener(VisionmixActivity activity, ForegroundView fvPhoto, NavigatorView navigatorView, float scale, Matrix matrix, boolean rotateEnabled) {
        this.fvPhoto = fvPhoto;
        this.scale = scale;
        this.matrix.set(matrix);
        this.isRotationEnabled = rotateEnabled;
        mainActivity = activity;
        this.navigatorView = navigatorView;
    }

    public ForegroundTouchListener(VisionmixActivity activity, ForegroundView fvPhoto, NavigatorView navigatorView,  float scale, Matrix matrix) {
        this(activity, fvPhoto, navigatorView, scale, matrix, true);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {

            case MotionEvent.ACTION_DOWN:
                if (!mainActivity.m_bEraseMode) {
                    if(isValidPoint(event)) {
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        Log.d(TAG, "mode=DRAG");
                        mode = DRAG;
                    }
                }
                else
                {
                    Log.d(TAG, "mode=DRAW");
                    mode = DRAW;
                    mainActivity.rl_navilayout.setVisibility(View.VISIBLE);
                    navigatorView.setBitmapMask_for_VISIONMIX(fvPhoto.tempBitmap);
                    navigatorView.translate(0, -event.getX(), -event.getY());
                    return fvPhoto.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                mainActivity.rl_navilayout.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mainActivity.rl_navilayout.getLayoutParams();
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                midPoint(mid, event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 10f && isValidPoint((int)mid.x, (int)mid.y) ){

                    getVecor2D(prevVec, event);
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mainActivity.rl_navilayout.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)mainActivity.rl_navilayout.getLayoutParams();
                params1.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                if (mode == DRAW) {
                        mode = NONE;
                        return fvPhoto.onTouchEvent(event);
                }
                mode = NONE;
//                fvPhoto.invalidate();
                fvPhoto.RefreshView();
                savedMatrix.set(matrix);
                Log.d(TAG, "mode=NONE");
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == ZOOM) {
                    float newDist = spacing(event);

                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float deltaScale = newDist / oldDist;

                        Vector2D vec = new Vector2D();
                        getVecor2D(vec, event);
                        float deltaAngle = adjustAngle(getAngle(prevVec, vec));
                        Log.d(TAG, "deltaAngle=" + deltaAngle +" "+deltaScale);

                        deltaScale = correctDeltaScale(deltaScale, savedMatrix);
                        matrix.postScale(deltaScale, deltaScale, mid.x, mid.y);
                        matrix.postTranslate(event.getX() - start.x,event.getY() - start.y);
                        if (isRotationEnabled) {
                            if(deltaAngle!=0)
                                matrix.postRotate(deltaAngle, event.getX(0), event.getY(0));
//                            matrix.postRotate(deltaAngle, mid.x, mid.y);
                            //fvPhoto.setRotateValues(deltaAngle, event.getX(0), event.getY(0));
                        }
                        fvPhoto.setMatrix(matrix);
                    }
                }
                else if(mode == DRAG)
                {
                    if(event.getY()+30 < mainActivity.getApplicationContext().getResources().getDisplayMetrics().heightPixels-MeasureUtil.convertDpToPixels(150, mainActivity)) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x,
                                event.getY() - start.y);
                        fvPhoto.setMatrix(matrix);
                    }
                }
                else if(mode == DRAW)
                {
                    navigatorView.translate(0, -event.getX(), -event.getY());
                    navigatorView.PositionRefresh(mainActivity.rl_navilayout,(int)event.getX(), (int)event.getY());
                    return fvPhoto.onTouchEvent(event);
                }
                break;
        }
//        fvPhoto.setMatrix(matrix);
//        fvPhoto.invalidate();
        return true; // indicate event was handled
    }

    boolean isValidPoint(MotionEvent event) {
//        fvPhoto.setDrawingCacheEnabled(true);
//        Bitmap bm = fvPhoto.getDrawingCache();
        Bitmap bm = fvPhoto.tempBitmap;
        if(event.getY() >= bm.getHeight())
            return false;
        int color = 0;
        if(event.getPointerCount() == 1) {
            color = bm.getPixel((int)event.getX(), (int)event.getY());
        } else {
            color = bm.getPixel((int)event.getX(0), (int)event.getY(0));
            for(int i=1; i<event.getPointerCount(); i++) {
                if(event.getY(i)<bm.getHeight())
                    color |= bm.getPixel((int) event.getX(i), (int) event.getY(i));
                else
                    color |=0;
            }
        }
//        fvPhoto.setDrawingCacheEnabled(false);
        if(color == 0) return false;
        return true;
    }

    boolean isValidPoint(int x, int y) {
        Bitmap bm = fvPhoto.tempBitmap;
        if(y >= bm.getHeight())
            return false;
        int color = bm.getPixel(x, y);
        if(color == 0) return false;
        return true;

    }

    float correctDeltaScale(float deltaScale, Matrix matrix){
        float[] x=new float[9];
        matrix.getValues(x);
// calculate real scale
        float scalex = x[Matrix.MSCALE_X];
        float skewy = x[Matrix.MSKEW_Y];
        float rScale = (float) Math.sqrt(scalex * scalex + skewy * skewy);

// calculate the degree of rotation
//        float rAngle = Math.round(Math.atan2(x[Matrix.MSKEW_X], x[Matrix.MSCALE_X]) * (180 / Math.PI));

        float newScale = rScale * deltaScale;
        float retScale = deltaScale;
        if(Math.abs(newScale) * 10 <mainActivity.scale0) {
            retScale = mainActivity.scale0 / 10 / rScale;

        } else if(Math.abs(newScale) / 10 >mainActivity.scale0) {
            retScale = mainActivity.scale0 * 10/ rScale;

        }else {
            retScale = deltaScale;

        }
        return retScale;
    }

    public float rotatedScale(Matrix matrix)
    {
        float[] x = new float[9];
        matrix.getValues(x);
        float rAngle = (float)(Math.atan2(x[Matrix.MSKEW_X], x[Matrix.MSCALE_X]) * (180 / Math.PI));

        rAngle = rAngle % 360;
        // precompute some trig functions
        double radians = Math.toRadians(rAngle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        // figure out total width and height of new bitmap
        int wSrc = Engine.visonmix_fgBitmap.getWidth();
        int hSrc = Engine.visonmix_fgBitmap.getHeight();

        float newWidth = (float)(wSrc * cos + hSrc * sin);
        float newHeight = (float)(wSrc * sin + hSrc * cos);
        float rAngle1 = rAngle % 90;
        radians = Math.toRadians(rAngle1);
        double sin1 = Math.abs(Math.sin(radians ));
        //float scale = (1 - Math.abs(((float)sin1 / 2))) ;//* (hSrc / wSrc);

        float scale = 1;
        if(wSrc > hSrc) {
            if (newWidth > newHeight)
                scale = Math.min(wSrc, hSrc) / Math.min(newWidth, newHeight);
            else
                scale = Math.min(wSrc, hSrc) / Math.max(newWidth, newHeight);
        } else {
            if (newWidth > newHeight)
                scale = Math.min(wSrc, hSrc) / Math.max(newWidth, newHeight);
            else
                scale = Math.min(wSrc, hSrc) / Math.min(newWidth, newHeight);
        }

        return scale;
    }

    private static float adjustAngle(float degrees) {
        if (degrees > 180.0f) {
            degrees -= 360.0f;
        } else if (degrees < -180.0f) {
            degrees += 360.0f;
        }

        return degrees;
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    private void getVecor2D(Vector2D vec, MotionEvent event) {
        float cx0 = event.getX(0);
        float cy0 = event.getY(0);
        float cx1 = event.getX(1);
        float cy1 = event.getY(1);
        float cvx = cx1 - cx0;
        float cvy = cy1 - cy0;
        vec.set(cvx, cvy);
    }

    public static float getAngle(Vector2D vector1, Vector2D vector2) {
        vector1.normalize();
        vector2.normalize();
        double degrees = (180.0 / Math.PI) * (Math.atan2(vector2.y, vector2.x) - Math.atan2(vector1.y, vector1.x));
        return (float) degrees;
    }

    class Vector2D extends PointF {

        public Vector2D() {
            super();
        }

        public Vector2D(float x, float y) {
            super(x, y);
        }

        public void normalize() {
            float length = (float) Math.sqrt(x * x + y * y);
            x /= length;
            y /= length;
        }
    }

}
