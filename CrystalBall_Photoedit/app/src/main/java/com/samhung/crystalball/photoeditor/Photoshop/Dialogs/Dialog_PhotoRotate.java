package com.samhung.crystalball.photoeditor.Photoshop.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views.PhotoRotateView;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.VisionMix.ForegroundTouchListener;

public class Dialog_PhotoRotate extends FullScreenDialog  implements View.OnTouchListener{
    OnClickListener m_onClickListener = null;
    public Bitmap mBitmap = null;
    Context mContext = null;

    ImageView ivPhoto = null;
    PhotoRotateView rotateView = null;

    ImageButton imbRotateCW90 = null;
    ImageButton imbRotateACW90 = null;
    ImageButton imbFlipV = null;
    ImageButton imbFlipH = null;

    Matrix matrix_save = new Matrix();

    public Dialog_PhotoRotate(Context context , Bitmap bitmap, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mBitmap = bitmap;
        mResultBitmap = Bitmap.createBitmap(mBitmap);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo_rotate);
        rotateView = (PhotoRotateView)findViewById(R.id.photo_rotateview);
        ivPhoto = (ImageView)findViewById(R.id.imageview_photo);
        ivPhoto.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (wDrawingPane == 0 || hDrawingPane == 0 || (wDrawingPane != right-left) || (hDrawingPane !=bottom-top)) {
                    wDrawingPane = right - left;
                    hDrawingPane = bottom - top;
                    layout_Left = left;
                    layout_Top = top;
                    Refresh_PhotoView();
                }
            }
        });


        ImageView btnAccept = (ImageView)findViewById(R.id.button_accept);
        ImageView btnBack = (ImageView)findViewById(R.id.button_close);
        btnAccept.setOnClickListener(btnClickListener);
        btnBack.setOnClickListener(btnClickListener);

        imbRotateACW90 = (ImageButton)findViewById(R.id.imb_rotate_acw90);
        imbRotateCW90 = (ImageButton)findViewById(R.id.imb_rotate_cw90);
        imbFlipH = (ImageButton)findViewById(R.id.imb_flip_h);
        imbFlipV = (ImageButton)findViewById(R.id.imb_flip_v);
        imbRotateACW90.setOnClickListener(btnClickListener);
        imbRotateCW90.setOnClickListener(btnClickListener);
        imbFlipH.setOnClickListener(btnClickListener);
        imbFlipV.setOnClickListener(btnClickListener);
    }

    void Refresh_PhotoView()
    {
        wSrc = mResultBitmap.getWidth();
        hSrc = mResultBitmap.getHeight();
        scale0 = Math.min(((float) hDrawingPane) / hSrc, ((float) wDrawingPane) / wSrc);
        scale0 = scale0 * (float)0.9;
        matrix.reset();
        matrix.postScale(scale0, scale0);
        xInDrawingPane = (int) Math.abs(wSrc * scale0 - wDrawingPane) / 2;
        yInDrawingPane = (int) Math.abs(hSrc * scale0 - hDrawingPane) / 2;
        matrix.postTranslate(xInDrawingPane, yInDrawingPane);

        rotateView.initRect(xInDrawingPane, yInDrawingPane, wSrc*scale0, hSrc*scale0, scale0);
        rotateView.setRotatedRect(new RectF(0, 0, wSrc*scale0, hSrc * scale0));
        rotateView.invalidate();

        ivPhoto.setImageBitmap(mResultBitmap);
        ivPhoto.setImageMatrix(matrix);
        ivPhoto.setOnTouchListener(this);
    }

    float rAngle = 0;
    void Refresh_PhotoView_1()
    {
        mResultBitmap = Bitmap.createBitmap(mBitmap);
        wSrc = mResultBitmap.getWidth();
        hSrc = mResultBitmap.getHeight();
        scale0 = Math.min(((float) hDrawingPane) / hSrc, ((float) wDrawingPane) / wSrc);
        scale0 = scale0 * (float)0.9;
        matrix.reset();
        matrix.postScale(scale0, scale0);
        xInDrawingPane = (int) Math.abs(wSrc * scale0 - wDrawingPane) / 2;
        yInDrawingPane = (int) Math.abs(hSrc * scale0 - hDrawingPane) / 2;
        matrix.postTranslate(xInDrawingPane, yInDrawingPane);
        matrix.postRotate(-rAngle, layout_Left+wDrawingPane / 2, layout_Left+hDrawingPane / 2);

        rotateView.initRect(xInDrawingPane, yInDrawingPane, wSrc*scale0, hSrc*scale0, scale0);
        rotatedRectF = rotatedSize();
        rotateView.setRotatedRect(rotatedRectF);
        rotateView.invalidate();
        ivPhoto.setImageBitmap(mResultBitmap);
        ivPhoto.setImageMatrix(matrix);
    }

    Matrix savedMatrix = new Matrix();
    Vector2D prevVec = new Vector2D();
    RectF rotatedRectF = new RectF();
    float deltaAngle = 0;
    Matrix matrix_tmp = new Matrix();
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      //  super.onTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                Refresh_PhotoView_1();
                savedMatrix.set(matrix);
                matrix_tmp.set(matrix_save);
                getVecor2D(prevVec, event);
               // rotatedRectF = rotatedSize();
                break;
            case MotionEvent.ACTION_MOVE:
                matrix.set(savedMatrix);
                matrix_save.set(matrix_tmp);

                Vector2D vec = new Vector2D();
                getVecor2D(vec, event);
               deltaAngle = adjustAngle(getAngle(prevVec, vec));
                matrix.postRotate(deltaAngle, layout_Left + wDrawingPane/2, layout_Top + hDrawingPane/2);
                matrix_save.postRotate(deltaAngle, mBitmap.getWidth()/2, mBitmap.getHeight()/2);
                rotatedRectF = rotatedSize();
                rotateView.setRotatedRect(rotatedRectF);
                rotateView.invalidate();
                break;
            case MotionEvent.ACTION_UP:

                float[] x = new float[9];
                matrix.getValues(x);
                rAngle = (float)(Math.atan2(x[Matrix.MSKEW_X], x[Matrix.MSCALE_X]) * (180 / Math.PI));
                rAngle = rAngle % 360;

                int left = (int)((mNewWidth - rotatedRectF.width()/scale0) / 2);
                int top = (int)((mNewHeight - rotatedRectF.height() /scale0)/ 2);

                Matrix mat = new Matrix();
                mat.postRotate(-rAngle);
                mResultBitmap = Bitmap.createBitmap(mResultBitmap, 0,0, wSrc, hSrc, matrix_save, true);
                mResultBitmap = Bitmap.createBitmap(mResultBitmap, left, top, (int)(rotatedRectF.width()/ scale0) , (int)(rotatedRectF.height()/scale0));
                Refresh_PhotoView();
                break;
        }
        ivPhoto.setImageMatrix(matrix);
        return true;
    }

    private void getVecor2D(Vector2D vec, MotionEvent event) {
        float cx0 = layout_Left + wDrawingPane / 2;
        float cy0 = layout_Top + hDrawingPane / 2;
        float cx1 = event.getX();//event.getX(0);
        float cy1 = event.getY();//event.getY(0);
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

    private float adjustAngle(float degrees) {
        if (degrees > 180.0f) {
            degrees -= 360.0f;
        } else if (degrees < -180.0f) {
            degrees += 360.0f;
        }
        return degrees;
    }

    public class Vector2D extends PointF {

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

    float mNewWidth = 0;
    float mNewHeight = 0;
    public RectF rotatedSize()
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
        float newWidth = (float)(wSrc * cos + hSrc * sin);
        float newHeight = (float)(wSrc * sin + hSrc * cos);
        mNewHeight = newHeight;
        mNewWidth = newWidth;
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

        RectF ret = new RectF(0, 0, wSrc * scale * scale0, hSrc * scale * scale0 );
        return ret;
    }

    public void rotateBitmap(float degree) {
        Matrix mat = new Matrix();
        mat.postRotate(degree);

        rAngle = 0;
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), mat, true);
        mResultBitmap = Bitmap.createBitmap(mBitmap);
    }

    public void flipBitmap(boolean bVertical)
    {

        Matrix rotateMatrix = new Matrix();
        if(bVertical)
            rotateMatrix.postScale(1.0f, -1.0f);
        else
            rotateMatrix.postScale(-1.0f, 1.0f);
        rAngle = 0;
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), rotateMatrix, true);
        mResultBitmap = Bitmap.createBitmap(mBitmap);
    }

    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.button_accept)
                m_onClickListener.onClick(Dialog_PhotoRotate.this, DialogInterface.BUTTON_POSITIVE);
            else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_PhotoRotate.this, BUTTON_NEGATIVE);
            else if(v.getId() == R.id.imb_rotate_acw90) {
                rotateBitmap( - 90);
                Dialog_PhotoRotate.this.Refresh_PhotoView();
                rotateView.invalidate();
            } else if(v.getId() == R.id.imb_rotate_cw90) {
                rotateBitmap( 90);
                Dialog_PhotoRotate.this.Refresh_PhotoView();
                rotateView.invalidate();
            } else if(v.getId() == R.id.imb_flip_h) {
                flipBitmap(false);
                Dialog_PhotoRotate.this.Refresh_PhotoView();
                rotateView.invalidate();
            } else if(v.getId() == R.id.imb_flip_v) {
                flipBitmap(true);
                Dialog_PhotoRotate.this.Refresh_PhotoView();
                rotateView.invalidate();
            }

        }
    };
}
