package com.samhung.crystalball.photoeditor.Photoshop.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Widget.NavigatorView;
import com.samhung.crystalball.widgets.PressableButton;
import com.samhung.crystalball.widgets.Slider;

import org.CrystalBall.Engine;

public class Dialog_PhotoFaceBeauty extends FullScreenDialog implements View.OnTouchListener{
    OnClickListener m_onClickListener = null;
    Context mContext = null;

    ImageView ivPhoto = null;
    ImageView ivPhotoOriginal = null;
    RelativeLayout rl_navilayout = null;
    NavigatorView navigatorView = null;
    Slider seekBar_beauty = null;
    Slider seekBar_brushSize = null;
    PressableButton btnPreview = null;
    boolean m_bModeManual = false;
    int brush_Size = 50;
    Canvas mResultCanvas = null;
    Bitmap mTmpBitmap = null;

    public Dialog_PhotoFaceBeauty(Context context , Bitmap bitmap, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mBitmap = bitmap;
        mResultBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mResultCanvas = new Canvas(mResultBitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo_facebeauty);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));
        seekBar_beauty = (Slider) findViewById(R.id.seekBar_main);
        seekBar_beauty.setOnValueChangeListener(onSliderChangeListener);
        seekBar_brushSize = (Slider)findViewById(R.id.seekBar_sub);
        seekBar_brushSize.setOnValueChangeListener(onSliderChangeListener);

        btnPreview = (PressableButton)findViewById(R.id.imv_preview);
        btnPreview.SetPressedListener(previewPressedListener);
        rl_navilayout = (RelativeLayout)findViewById(R.id.rl_naviview);
        rl_navilayout.setVisibility(View.INVISIBLE);
        navigatorView = (NavigatorView)findViewById(R.id.naviView);
        navigatorView.setBrushSize(brush_Size);
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
                    navigatorView.setParentSize(left, top, right, bottom);
                    Refresh_PhotoView();
                }
            }
        });


        ImageView btnAccept = (ImageView)findViewById(R.id.button_accept);
        ImageView btnBack = (ImageView)findViewById(R.id.button_close);
        btnAccept.setOnClickListener(btnClickListener);
        btnBack.setOnClickListener(btnClickListener);

        findViewById(R.id.bt_manual).setOnClickListener(btnClickListener);
        findViewById(R.id.bt_auto).setOnClickListener(btnClickListener);
        findViewById(R.id.button_sub_accept).setOnClickListener(btnClickListener);
        findViewById(R.id.button_sub_close).setOnClickListener(btnClickListener);
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
        ivPhoto.setImageBitmap(mResultBitmap);
        ivPhoto.setImageMatrix(matrix);
        ivPhoto.setOnTouchListener(this);

        navigatorView.setBitmap(mResultBitmap, matrix);
    }

    Slider.OnValueChangeListener onSliderChangeListener = new Slider.OnValueChangeListener() {
        @Override
        public void onProgressChanged(Slider slider, int progress, boolean fromUser) {
            if(slider.getId() == R.id.seekBar_sub) {
                int tmp = progress + 2;
                brush_Size = tmp * 10;
                navigatorView.setBrushSize(brush_Size);
            }
        }

        @Override
        public void onStartTrackingTouch(Slider slider) {

        }

        @Override
        public void onStopTrackingTouch(Slider seekBar) {
            if(seekBar.getId() == R.id.seekBar_main) {
                AsyncTask_ProcessEffect task = new AsyncTask_ProcessEffect();
                task.execute();
            } else if(seekBar.getId() == R.id.seekBar_sub) {

            }
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
                if(m_bModeManual) {
                    mode = BaseActivity.DRAW;
                    start.set(event.getX(), event.getY());
                    navigatorView.translate(0, -event.getX(), -event.getY());

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)rl_navilayout.getLayoutParams();
                    params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rl_navilayout.setVisibility(View.VISIBLE);
                } else {
                    mode = BaseActivity.DRAG;
                    savedMatrix.set(matrix);
                    start.set(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                rl_navilayout.setVisibility(View.INVISIBLE);
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

                        navigatorView.setBitmap(null, matrix);
                    }
                } else if(mode == BaseActivity.DRAW) {
                    ProcessManualBeauty((int)event.getX(), (int)event.getY());
                    navigatorView.translate(0, -event.getX(), -event.getY());
                    navigatorView.PositionRefresh(rl_navilayout, (int)event.getX(), (int)event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = BaseActivity.NONE;
                RefreshLayoutPos();
                rl_navilayout.setVisibility(View.INVISIBLE);
                break;
        }
        ivPhotoOriginal.setImageMatrix(matrix);
        ivPhoto.setImageMatrix(matrix);
        navigatorView.setBitmap(null, matrix);
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
            if(v.getId() == R.id.button_accept || v.getId() == R.id.button_sub_accept)
                m_onClickListener.onClick(Dialog_PhotoFaceBeauty.this, DialogInterface.BUTTON_POSITIVE);
            else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_PhotoFaceBeauty.this, BUTTON_NEGATIVE);
            else if(v.getId() == R.id.bt_manual) {
                m_bModeManual = true;
                mTmpBitmap = mResultBitmap.copy(Bitmap.Config.ARGB_8888, true);
                findViewById(R.id.ll_ManualPad).setVisibility(View.VISIBLE);
            } else if(v.getId() == R.id.button_sub_close) {
                m_bModeManual = false;
                findViewById(R.id.ll_ManualPad).setVisibility(View.GONE);
            } else if(v.getId() == R.id.bt_auto) {
                m_bModeManual = false;
                findViewById(R.id.ll_ManualPad).setVisibility(View.GONE);
            }
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
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int prog = seekBar_beauty.getProgress();

//            mResultBitmap = Engine.getIntance().ImageClarity(mBitmap, prog);
            Engine.getIntance().FaceBeauty(mBitmap, mResultBitmap, prog);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HideWaitDialog();
            ivPhoto.setImageBitmap(mResultBitmap);
        }
    };

    public void ProcessManualBeauty(int x, int y) {
        float[] matrix_value = new float[9];
        matrix.getValues(matrix_value);
        float scale = matrix_value[Matrix.MSCALE_X];
        float transX = matrix_value[Matrix.MTRANS_X];
        float transY = matrix_value[Matrix.MTRANS_Y];

        float cx = 0-transX/scale  + (x - layout_Left)/ scale;
        float cy = 0-transY/scale + (y-layout_Top) / scale;
        int mask_width = (int)(brush_Size / scale);

        if(cx - mask_width * 1 < 0 || cy- mask_width * 1 < 0 || cx + mask_width*1 > mBitmap.getWidth() || cy + mask_width * 1 > mBitmap.getHeight())
            return;

        BeautyRectInfo beautyPos = new BeautyRectInfo((int)cx, (int)cy, mask_width);
        beautyPos.Process();

        mResultCanvas.drawBitmap(beautyPos.area_processed, beautyPos.brushRect.left, beautyPos.brushRect.top, new Paint());

//        m_listAcnes.add(acne);

//        if(m_listAcnes.size() > 10) {
//            RemoveAcne(m_tmpCanvas, m_listAcnes.get(0));
//            m_listAcnes.remove(0);
//        }

//        m_resultCanvas.drawBitmap(mTmpBitmap, 0, 0, new Paint());
//        for (AcneInfo ac: m_listAcnes) {
//            RemoveAcne(m_resultCanvas, ac);
//        }

        ivPhoto.setImageBitmap(mResultBitmap);
//        Bitmap bm = Bitmap.createBitmap(mBitmap, (int)(cx-mask_width/2), (int)(cy-mask_width/2), mask_width, mask_width);
//        ImageView iv = (ImageView)findViewById(R.id.imageview_photo3);
//        iv.setImageBitmap(bm);

//        if(m_listAcnes.size() > 0) btnUndo.setVisibility(View.VISIBLE);
    }

    public class BeautyRectInfo {
        Rect brushRect = null;
        Rect brushRect_Large = null;
        int cx = 0; int cy=0;
        int radius = 0;
        public Bitmap area_processed = null;
        public BeautyRectInfo(int cx, int cy, int radius) {
            this.cx = cx;
            this.cy = cy;
            this.radius = radius;
            this.brushRect = new Rect(cx-radius/2,cy-radius/2, cx+radius/2, cy+radius/2);
            this.brushRect_Large = new Rect(cx-radius,cy-radius, cx+radius, cy+radius);

        }

        public void Process() {
            Bitmap area = Bitmap.createBitmap(mTmpBitmap, brushRect.left, brushRect.top, radius , radius);
            area_processed = Bitmap.createBitmap(area.getWidth(), area.getHeight(), Bitmap.Config.ARGB_8888);
            Engine.getIntance().FaceBeauty(area, area_processed, 100);
        }
    }
}
