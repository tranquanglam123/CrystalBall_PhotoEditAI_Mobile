package com.samhung.crystalball.photoeditor.Photoshop.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
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
import com.samhung.crystalball.photoeditor.Widget.NavigatorView;
import com.samhung.crystalball.widgets.PressableButton;
import com.samhung.crystalball.widgets.Slider;

import org.CrystalBall.Engine;

import java.util.ArrayList;

public class Dialog_PhotoAcne extends FullScreenDialog implements View.OnTouchListener{
    OnClickListener m_onClickListener = null;
    Context mContext = null;

    ImageView ivPhoto = null;
    ImageView ivPhotoOriginal = null;

    RelativeLayout rl_navilayout = null;
    NavigatorView navigatorView = null;

    Slider seekBar_size = null;

    Canvas m_resultCanvas = null;

    Bitmap mTmpBitmap = null;
    Canvas m_tmpCanvas = null;

    PressableButton btnPreview = null;
    ImageView btnUndo = null;

    public Dialog_PhotoAcne(Context context , Bitmap bitmap, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mBitmap = bitmap;

        mTmpBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
        m_tmpCanvas= new Canvas(mTmpBitmap);

        mResultBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        m_resultCanvas = new Canvas(mResultBitmap);
        m_resultCanvas.drawBitmap(mBitmap, 0, 0, new Paint());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo_acne);
        ivPhotoOriginal = (ImageView)findViewById(R.id.imageview_photo);
        ivPhoto = (ImageView)findViewById(R.id.imageview_photo2);
        navigatorView = (NavigatorView)findViewById(R.id.naviView);
        navigatorView.setBrushSize(brush_Size);
        rl_navilayout = (RelativeLayout)findViewById(R.id.rl_naviview);
        rl_navilayout.setVisibility(View.INVISIBLE);
        seekBar_size = (Slider) findViewById(R.id.seekBar_size);
        seekBar_size.setOnValueChangeListener(onSliderChangeListener);
        btnUndo = (ImageView)findViewById(R.id.button_undo);
        btnUndo.setOnClickListener(btnClickListener);
        btnUndo.setVisibility(View.INVISIBLE);
        btnPreview = (PressableButton)findViewById(R.id.imv_preview);
        btnPreview.SetPressedListener(previewPressedListener);

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

        ivPhoto.setImageBitmap(mResultBitmap);
        ivPhoto.setImageMatrix(matrix);
        ivPhoto.setOnTouchListener(this);
        ivPhotoOriginal.setImageBitmap(mBitmap);
        ivPhotoOriginal.setImageMatrix(matrix);

        navigatorView.setBitmap(mResultBitmap, matrix);

    }

    int brush_Size = 50;

    Slider.OnValueChangeListener onSliderChangeListener = new Slider.OnValueChangeListener() {
        @Override
        public void onProgressChanged(Slider slider, int progress, boolean fromUser) {
            int tmp = progress + 2;
            brush_Size = tmp * 10;
            navigatorView.setBrushSize(brush_Size);
        }

        @Override
        public void onStartTrackingTouch(Slider slider) {

        }

        @Override
        public void onStopTrackingTouch(Slider seekBar) {

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

    public ArrayList<AcneInfo> m_listAcnes = new ArrayList<AcneInfo>();
    private void ProcessAcne(int x, int y) {
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

        AcneInfo acne = new AcneInfo((int)cx, (int)cy, mask_width);
        acne.Process();
        m_listAcnes.add(acne);

        if(m_listAcnes.size() > 10) {
            RemoveAcne(m_tmpCanvas, m_listAcnes.get(0));
            m_listAcnes.remove(0);
        }

        m_resultCanvas.drawBitmap(mTmpBitmap, 0, 0, new Paint());
        for (AcneInfo ac: m_listAcnes) {
            RemoveAcne(m_resultCanvas, ac);
        }

        ivPhoto.setImageBitmap(mResultBitmap);
//        Bitmap bm = Bitmap.createBitmap(mBitmap, (int)(cx-mask_width/2), (int)(cy-mask_width/2), mask_width, mask_width);
//        ImageView iv = (ImageView)findViewById(R.id.imageview_photo3);
//        iv.setImageBitmap(bm);

        if(m_listAcnes.size() > 0) btnUndo.setVisibility(View.VISIBLE);
    }


    private void RemoveAcne(Canvas canvas, AcneInfo acneInfo) {
        canvas.drawBitmap(acneInfo.area_processed, acneInfo.acneRect_Large.left, acneInfo.acneRect_Large.top, new Paint());
    }

    private void ProcessUndo() {
        if(m_listAcnes.size() == 0) return;

        m_listAcnes.remove(m_listAcnes.size()-1);
        m_resultCanvas.drawBitmap(mTmpBitmap, 0, 0, new Paint());
        for (AcneInfo ac: m_listAcnes) {
            RemoveAcne(m_resultCanvas, ac);
        }

        ivPhoto.setImageBitmap(mResultBitmap);

        if(m_listAcnes.size() == 0) {
            btnUndo.setVisibility(View.INVISIBLE);
        }
    }

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
                mode = BaseActivity.DRAW;
                start.set(event.getX(), event.getY());
                navigatorView.translate(0, -event.getX(), -event.getY());

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)rl_navilayout.getLayoutParams();
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                rl_navilayout.setVisibility(View.VISIBLE);
                return true;
                //break;
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
                if (mode == BaseActivity.DRAW) {
  //                  matrix.set(savedMatrix);
//                    matrix.postTranslate(event.getX() - start.x,
//                            event.getY() - start.y);
                    navigatorView.translate(0, -event.getX(), -event.getY());
                    navigatorView.PositionRefresh(rl_navilayout, (int)event.getX(), (int)event.getY());
                    return true;
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
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                rl_navilayout.setVisibility(View.INVISIBLE);
                if(mode == BaseActivity.ZOOM) {
                    mode = BaseActivity.NONE;
                        RefreshLayoutPos();
                } else if(mode == BaseActivity.DRAW) {
                    ProcessAcne((int)event.getX(), (int)event.getY());
                }
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
            if(v.getId() == R.id.button_accept)
                m_onClickListener.onClick(Dialog_PhotoAcne.this, DialogInterface.BUTTON_POSITIVE);
            else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_PhotoAcne.this, BUTTON_NEGATIVE);
            else if(v.getId() == R.id.button_undo)
                ProcessUndo();
        }
    };

    public class AcneInfo {
        Rect acneRect = null;
        Rect acneRect_Large = null;
        int cx = 0; int cy=0;
        int radius = 0;
        public Bitmap area_processed = null;
        public AcneInfo(int cx, int cy, int radius) {
            this.cx = cx;
            this.cy = cy;
            this.radius = radius;
            this.acneRect = new Rect(cx-radius/2,cy-radius/2, cx+radius/2, cy+radius/2);
            this.acneRect_Large = new Rect(cx-radius,cy-radius, cx+radius, cy+radius);

        }

        public void Process() {
            Bitmap area = Bitmap.createBitmap(mResultBitmap, acneRect_Large.left, acneRect_Large.top, radius * 2, radius*2);
            area_processed = Bitmap.createBitmap(area.getWidth(), area.getHeight(), Bitmap.Config.ARGB_8888);
            Engine.getIntance().removeAcneOne(area, area_processed);
        }
    }
}
