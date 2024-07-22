package com.samhung.crystalball.photoeditor.Photoshop.Dialogs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views.BrushDrawingView;
import com.samhung.crystalball.photoeditor.Utilis.WaitWindow;
import com.samhung.crystalball.photoeditor.Widget.NavigatorView;
import com.samhung.crystalball.widgets.PressableButton;
import com.samhung.crystalball.widgets.Slider;

import org.CrystalBall.Engine;

public class Dialog_PhotoSoftfocus extends FullScreenDialog implements View.OnTouchListener{
    OnClickListener m_onClickListener = null;
    Context mContext = null;

    BrushDrawingView brushDrawingView;
    NavigatorView navigatorView;
    RelativeLayout navigatorContainer;
    ImageView ivPhoto = null;
    ImageView ivPreview = null;

    SeekBar sbStrokeWidth= null;
    RadioGroup rgBrushType;
    int brushType = R.id.rd_drawer;

    AsyncTask_getMask asyncTaskMasking;

    Bitmap bitmapMask;
    Bitmap bitmapMaskEdited;
    Bitmap bitmapTrimap;
    Bitmap bitmapMatting = null;
    int[] m_mattedPos = new int[2];
    Canvas mCanvasResult = null;

    LinearLayout ll_effectPanel = null;
    RelativeLayout rl_hideEffectPanel = null;
    Slider sbStrokeEffect= null;
    RadioGroup rgEffectType;

    PressableButton btnPreview = null;
    ImageView btnUndo = null;
    int m_EffectIndex = 0;
    public Dialog_PhotoSoftfocus(Context context , Bitmap bitmap, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mBitmap = bitmap;
        mResultBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        mCanvasResult = new Canvas(mResultBitmap);
        mCanvasResult.drawBitmap(mBitmap, 0, 0, new Paint());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo_softfocus);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));

        brushDrawingView = (BrushDrawingView)findViewById(R.id.drawingView);
        brushDrawingView.setBrushSize(BrushDrawingView.BRUSH_MAX / 2);

        navigatorContainer = (RelativeLayout)findViewById(R.id.rl_naviview);
        navigatorView = (NavigatorView)findViewById(R.id.naviView1);
        navigatorContainer.setVisibility(View.INVISIBLE);
        navigatorView.setBrushSize((int)BrushDrawingView.BRUSH_MAX / 2);

        rgBrushType = (RadioGroup)findViewById(R.id.rg_brush_type);
        sbStrokeWidth = (SeekBar)findViewById(R.id.sb_stroke_width);
        rgBrushType.check(brushType);

        rgBrushType.setOnCheckedChangeListener(radioCheckedChangeListener);

        rgEffectType = (RadioGroup)findViewById(R.id.rg_effect_type);
        rgEffectType.setOnCheckedChangeListener(radioEffectChangeListener);
        sbStrokeEffect = (Slider) findViewById(R.id.sb_stroke_effect);
        sbStrokeEffect.setOnValueChangeListener(effectSliderListener);
        ll_effectPanel = (LinearLayout) findViewById(R.id.LinearLayout_Effect);
        ll_effectPanel.setVisibility(View.INVISIBLE);
        rl_hideEffectPanel = (RelativeLayout) findViewById(R.id.RelativeLayout_HideEffect);
        rl_hideEffectPanel.setOnClickListener(btnClickListener);

        ivPreview = (ImageView)findViewById(R.id.imageview_preview);
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

        btnPreview = (PressableButton)findViewById(R.id.imv_preview);
        btnPreview.SetPressedListener(previewPressedListener);
        btnPreview.setVisibility(View.INVISIBLE);

        ImageView btnAccept = (ImageView)findViewById(R.id.button_accept);
        ImageView btnBack = (ImageView)findViewById(R.id.button_close);
        btnAccept.setOnClickListener(btnClickListener);
        btnBack.setOnClickListener(btnClickListener);
        btnUndo = ((ImageView)findViewById(R.id.button_undo));
        btnUndo.setOnClickListener(btnClickListener);
     //   ((ImageView)findViewById(R.id.imageView_waitBack)).setOnClickListener( btnClickListener);

        sbStrokeWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brushDrawingView.setBrushSize(progress);
                navigatorView.setBrushSize(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        asyncTaskMasking = new AsyncTask_getMask();
        asyncTaskMasking.execute();
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

        brushDrawingView.setDestCacheSize(wSrc, hSrc, layout_Left, layout_Top, layout_Left + wDrawingPane, layout_Top + hDrawingPane);
        brushDrawingView.setMatrix(matrix);
        navigatorView.setParentSize(layout_Left, layout_Top, layout_Left + wDrawingPane, layout_Top + hDrawingPane);
        navigatorView.setBitmap(mBitmap, matrix);


        if(bitmapMask != null)
            brushDrawingView.setBitmapMask(bitmapMask);

        ivPhoto.setImageBitmap(mBitmap);
        ivPhoto.setImageMatrix(matrix);
        ivPreview.setImageBitmap(mResultBitmap);
        ivPreview.setImageMatrix(matrix);
       // ivPhoto.setOnTouchListener(this);
        brushDrawingView.setOnTouchListener(this);
        ivPreview.setOnTouchListener(this);
    }

    RadioGroup.OnCheckedChangeListener radioCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            brushType = checkedId;
            btnPreview.setVisibility(View.INVISIBLE);
            btnUndo.setVisibility(View.INVISIBLE);
            if (checkedId == R.id.rd_drawer) {
                btnUndo.setVisibility(View.VISIBLE);
                brushDrawingView.setMode(BrushDrawingView.DRAWING_MODE_HARD);
                brushDrawingView.setVisibility(View.VISIBLE);
                sbStrokeWidth.setProgress((int) brushDrawingView.getBrushSize());
                ivPreview.setVisibility(View.INVISIBLE);
            } else if (checkedId== R.id.rd_eraser) {
                btnUndo.setVisibility(View.VISIBLE);
                brushDrawingView.setVisibility(View.VISIBLE);
                brushDrawingView.setMode(BrushDrawingView.ERASING_MODE);
                sbStrokeWidth.setProgress((int) brushDrawingView.getBrushSize());
                ivPreview.setVisibility(View.INVISIBLE);
            } else if(checkedId == R.id.rd_effect) {
                brushDrawingView.setVisibility(View.INVISIBLE);
                btnPreview.setVisibility(View.VISIBLE);
                ll_effectPanel.setVisibility(View.VISIBLE);
                ((RadioButton)findViewById(R.id.rd_preview)).setChecked(true);
            } else if (checkedId == R.id.rd_preview ) {
                brushDrawingView.setVisibility(View.INVISIBLE);
                btnPreview.setVisibility(View.VISIBLE);
                Refresh_PhotoView();
                if(brushDrawingView.isMaskEdited())
                    ProcessEffect();
                else
                    ivPreview.setVisibility(View.VISIBLE);
            }
        }
    };

    RadioGroup.OnCheckedChangeListener radioEffectChangeListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId) {
                case R.id.rd_effect1:
                    m_EffectIndex = 0;
                    break;
                case R.id.rd_effect2:
                    m_EffectIndex = 1;
                    break;
                case R.id.rd_effect3:
                    m_EffectIndex = 2;
                    break;
                case R.id.rd_effect4:
                    m_EffectIndex = 3;
                    break;
            }
            sbStrokeEffect.setProgress(50);
            ProcessEffect();
        }
    };

    Slider.OnValueChangeListener effectSliderListener = new Slider.OnValueChangeListener() {
        @Override
        public void onProgressChanged(Slider slider, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(Slider slider) {

        }

        @Override
        public void onStopTrackingTouch(Slider seekBar) {
            ProcessEffect();
        }
    };

    public void ProcessEffect() {
        AsyncTask_SoftFocusEffect task = new AsyncTask_SoftFocusEffect();
        task.execute();
    }

    int mode = BaseActivity.NONE;
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 0;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d("-------", event.toString());
        super.onTouchEvent(event);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                start.set(event.getX(), event.getY());
                if(brushType == R.id.rd_drawer || brushType == R.id.rd_eraser) {
                    mode = BaseActivity.DRAW;

                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)navigatorContainer.getLayoutParams();
                    params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                    navigatorContainer.setVisibility(View.VISIBLE);
                    navigatorView.setBitmapMask(brushDrawingView.tempBitmap);
                    navigatorView.translate(0, -event.getX(), -event.getY());
                    return brushDrawingView.onTouchEvent(event);
                } else {
                    mode = BaseActivity.NONE;
                    navigatorContainer.setVisibility(View.INVISIBLE);
                    break;
                }

            case MotionEvent.ACTION_POINTER_DOWN:
                navigatorContainer.setVisibility(View.INVISIBLE);
                brushDrawingView.ResetPath();
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
                } else if(mode == BaseActivity.DRAW){
                    navigatorView.translate(0, -event.getX(), -event.getY());
                    navigatorView.PositionRefresh(navigatorContainer, (int)event.getX(), (int)event.getY());
                    return brushDrawingView.onTouchEvent(event);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                navigatorContainer.setVisibility(View.INVISIBLE);
                if (mode == BaseActivity.DRAW) {
                    mode = BaseActivity.NONE;
                    return brushDrawingView.onTouchEvent(event);
                } else {
                    RefreshLayoutPos();
                }
                mode = BaseActivity.NONE;
                break;
        }
        ivPhoto.setImageMatrix(matrix);
        ivPreview.setImageMatrix(matrix);
        navigatorView.setBitmap(null, matrix);
        brushDrawingView.setMatrix(matrix);
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
                m_onClickListener.onClick(Dialog_PhotoSoftfocus.this, DialogInterface.BUTTON_POSITIVE);
            else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_PhotoSoftfocus.this, BUTTON_NEGATIVE);
            else if(v.getId() == R.id.button_undo)
                brushDrawingView.undo();
            else if(v.getId() == R.id.RelativeLayout_HideEffect) {
                ll_effectPanel.setVisibility(View.INVISIBLE);
            }
        }
    };

    PressableButton.OnPressedListener previewPressedListener = new PressableButton.OnPressedListener() {
        @Override
        public void OnPressed(View view) {
            ivPreview.setVisibility(View.INVISIBLE);
            brushDrawingView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void OnReleased(View view) {
            ivPreview.setVisibility(View.VISIBLE);
            brushDrawingView.setVisibility(View.VISIBLE);
        }
    };
    ////////////////////
    ///////////////////

    private class AsyncTask_getMask extends AsyncTask<Void, Void, Void> {
        Boolean bSuccess = true;
        @Override
        protected void onPreExecute() {
            Dialog_PhotoSoftfocus.this.ShowWaitDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
          HideWaitDialog();
            if(!bSuccess)
            {
                m_onClickListener.onClick(Dialog_PhotoSoftfocus.this, BUTTON_NEGATIVE);
            }
            else {
                brushDrawingView.setBitmapMask(bitmapMask);
                brushDrawingView.invalidate();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            bitmapTrimap = Bitmap.createBitmap(224,224, Bitmap.Config.ARGB_8888);
            int nRet = Engine.getIntance().magicTrimapMask(mBitmap, bitmapTrimap);
            bitmapMask = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ALPHA_8);
            Engine.getIntance().magicResizeTrimap(bitmapTrimap, bitmapMask, true, true);
//            bitmapMask = ImageUtils.getBitmapFromGrayMaskBytes(imgMasked, bitmap.getWidth(), bitmap.getHeight());
            return null;
        }
    };

    private class AsyncTask_SoftFocusEffect extends AsyncTask<String, String, String>{
        boolean bSuccess = true;
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();

        }

        @Override
        protected void onPostExecute(String aVoid) {
            HideWaitDialog();
            if(!bSuccess)
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Status", -1);
                Dialog_PhotoSoftfocus.this.dismiss();
            }
            else {
                Refresh_PhotoView();
                brushDrawingView.setVisibility(View.INVISIBLE);
                ivPreview.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected String doInBackground(String... voids) {
            if(bitmapMatting == null || brushDrawingView.isMaskEdited()) {
                bitmapMaskEdited = brushDrawingView.mergeToCache();

                bitmapMatting = Engine.getIntance().magicGetMattingImageForEffect(mBitmap, bitmapMaskEdited, m_mattedPos);
            }
            mCanvasResult.drawBitmap(mBitmap, 0, 0, new Paint());
//            if(m_EffectIndex == 0) {
//                int rad = sbStrokeEffect.getProgress() / 4;
//                if(rad==0) rad = 1;
//                ImageUtils.Blur_Effect(Dialog_PhotoSoftfocus.this.getContext(), mResultBitmap, mResultBitmap, rad);
//            }else
                Engine.getIntance().SoftFocusBackgroundFilter(mResultBitmap, m_EffectIndex, sbStrokeEffect.getProgress());
            mCanvasResult.drawBitmap(bitmapMatting, (float)m_mattedPos[0], (float)m_mattedPos[1], new Paint());
            return null;
        }

    };
}
