package com.samhung.crystalball.photoeditor.Photoshop.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoRotate;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.widgets.PressableButton;
import com.samhung.crystalball.widgets.Slider;

import org.CrystalBall.Engine;

public class Dialog_PhotoClarity extends FullScreenDialog implements View.OnTouchListener{
    OnClickListener m_onClickListener = null;
    Context mContext = null;

    ImageView ivPhoto = null;
    ImageView ivPhotoOriginal = null;
    Slider seekBar_clarity = null;
    PressableButton btnPreview = null;
    public Dialog_PhotoClarity(Context context , Bitmap bitmap, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mBitmap = bitmap;
        //mResultBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo_clarity);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));
        seekBar_clarity = (Slider) findViewById(R.id.seekBar_clarity);
        seekBar_clarity.setOnValueChangeListener(onSliderChangeListener);
        btnPreview = (PressableButton)findViewById(R.id.imv_preview);
        btnPreview.SetPressedListener(previewPressedListener);
        ivPhotoOriginal = (ImageView)findViewById(R.id.imageview_photo);
        ivPhoto = (ImageView)findViewById(R.id.imageview_photo2);
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
    }

    void Refresh_PhotoView()
    {
        wSrc = mBitmap.getWidth();
        hSrc = mBitmap.getHeight();
        scale0 = Math.min(((float) hDrawingPane) / hSrc, ((float) wDrawingPane) / wSrc);
        matrix.reset();
        matrix.postScale(scale0, scale0);
        xInDrawingPane = (int) Math.abs(wSrc * scale0 - wDrawingPane) / 2;
        yInDrawingPane = (int) Math.abs(hSrc * scale0 - hDrawingPane) / 2;
        matrix.postTranslate(xInDrawingPane, yInDrawingPane);
        ivPhotoOriginal.setImageBitmap(mBitmap);
        ivPhotoOriginal.setImageMatrix(matrix);
        ivPhoto.setImageBitmap(mBitmap);
        ivPhoto.setImageMatrix(matrix);
        ivPhoto.setOnTouchListener(this);
    }

    Slider.OnValueChangeListener onSliderChangeListener = new Slider.OnValueChangeListener() {
        @Override
        public void onProgressChanged(Slider slider, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(Slider slider) {

        }

        @Override
        public void onStopTrackingTouch(Slider seekBar) {
            AsyncTask_ProcessEffect task = new AsyncTask_ProcessEffect();
            task.execute();
        }
    };
    int mode = BaseActivity.NONE;
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 0;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        super.onTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = BaseActivity.DRAG;
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);

                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    start.set(event.getX(), event.getY());
                    mode = BaseActivity.ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == BaseActivity.DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x,
                            event.getY() - start.y);
                }
                else if (mode == BaseActivity.ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float deltaScale = newDist / oldDist;
                        matrix.postScale(deltaScale, deltaScale, mid.x, mid.y);
                        matrix.postTranslate(event.getX() - start.x,
                                event.getY() - start.y);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = BaseActivity.NONE;
                RefreshLayoutPos();
                break;
        }
        ivPhotoOriginal.setImageMatrix(matrix);
        ivPhoto.setImageMatrix(matrix);
        return true;
    }

    public void RefreshLayoutPos()
    {
        float x[] = new float[9];
        matrix.getValues(x);
        float scale = x[Matrix.MSCALE_X];
        float transX = x[Matrix.MTRANS_X];
        float transY = x[Matrix.MTRANS_Y];
        if(scale > 3) {
            matrix.postScale(3/scale, 3/scale, mid.x, mid.y);
            matrix.getValues(x);
            scale = x[Matrix.MSCALE_X];
            transX = x[Matrix.MTRANS_X];
            transY = x[Matrix.MTRANS_Y];
        }
        if(scale < scale0)
            scale = scale0;
        xInDrawingPane = (int)Math.abs(wSrc* scale - wDrawingPane)/2;
        yInDrawingPane = (int)Math.abs(hSrc* scale - hDrawingPane)/2;
        float newTransX = transX;
        float newTransY = transY;
        if( wSrc * scale <= wDrawingPane && hSrc * scale <= hDrawingPane )
        {
            newTransX = xInDrawingPane;
            newTransY = yInDrawingPane;
        }else if(wSrc * scale > wDrawingPane && hSrc * scale <= hDrawingPane )
        {
            newTransY = yInDrawingPane;
            if(transX > 0)
                newTransX = 0;
            if(transX < wDrawingPane-(wSrc * scale))
                newTransX = wDrawingPane-(wSrc * scale);

        }else if(wSrc * scale <= wDrawingPane && hSrc * scale >  hDrawingPane){
            newTransX = xInDrawingPane;
            if(transY > 0)
                newTransY = 0;
            if(transY < hDrawingPane-(hSrc * scale))
                newTransY = hDrawingPane-(hSrc * scale);
        }else{
            if(transX > 0)
                newTransX = 0;
            if(transX < wDrawingPane-(wSrc * scale))
                newTransX = wDrawingPane-(wSrc * scale);
            if(transY > 0)
                newTransY = 0;
            if(transY < hDrawingPane-(hSrc * scale))
                newTransY = hDrawingPane-(hSrc * scale);
        }

        matrix.reset();
        matrix.postScale(scale, scale);
        matrix.postTranslate(newTransX, newTransY);
    }

    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.button_accept)
                m_onClickListener.onClick(Dialog_PhotoClarity.this, DialogInterface.BUTTON_POSITIVE);
            else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_PhotoClarity.this, BUTTON_NEGATIVE);
        }
    };

    PressableButton.OnPressedListener previewPressedListener = new PressableButton.OnPressedListener() {
        @Override
        public void OnPressed(View view) {
            ivPhoto.setVisibility(View.INVISIBLE);
        }

        @Override
        public void OnReleased(View view) {
            ivPhoto.setVisibility(View.VISIBLE);
        }
    };

    public class AsyncTask_ProcessEffect extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();
            if(mResultBitmap != null) {
                mResultBitmap.recycle();
                mResultBitmap = null;
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int prog = seekBar_clarity.getProgress();

            mResultBitmap = Engine.getIntance().ImageClarity(mBitmap, prog);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HideWaitDialog();
            ivPhoto.setImageBitmap(mResultBitmap);
        }
    };
}
