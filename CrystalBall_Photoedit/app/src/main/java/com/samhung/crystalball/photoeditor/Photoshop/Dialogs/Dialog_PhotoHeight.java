package com.samhung.crystalball.photoeditor.Photoshop.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoRotate;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views.PhotoHeightView;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views.PhotoRotateView;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.widgets.PressableButton;
import com.samhung.crystalball.widgets.Slider;

public class Dialog_PhotoHeight extends FullScreenDialog {
    OnClickListener m_onClickListener = null;
    public Bitmap previewBitmap = null;

    Context mContext = null;

    ImageView ivPhoto = null;
    ImageView ivPhotoOriginal = null;
    Slider seekBar_height = null;
    TextView txtView_seekValue = null;
    PressableButton btnPreview = null;
    PhotoHeightView heightView = null;

    public Dialog_PhotoHeight(Context context , Bitmap bitmap, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mBitmap = bitmap;
        mResultBitmap = Bitmap.createBitmap(mBitmap);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo_height);

        seekBar_height = (Slider) findViewById(R.id.slider_height);
        txtView_seekValue = (TextView)findViewById(R.id.textView_seekBar);
        btnPreview = (PressableButton)findViewById(R.id.imv_preview);
        btnPreview.SetPressedListener(previewPressedListener);
        heightView = (PhotoHeightView)findViewById(R.id.photo_heightview);
        heightView.setParent(this);
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
                    heightView.setDestCacheSize(layout_Left, layout_Top, layout_Left+wDrawingPane, layout_Top+hDrawingPane);
                    Refresh_PhotoView();

                }
            }
        });


        ImageView btnAccept = (ImageView)findViewById(R.id.button_accept);
        ImageView btnBack = (ImageView)findViewById(R.id.button_close);
        btnAccept.setOnClickListener(btnClickListener);
        btnBack.setOnClickListener(btnClickListener);
        seekBar_height.setOnValueChangeListener(sliderChangeListener);
        ((Button)findViewById(R.id.bt_reset)).setOnClickListener(btnClickListener);
    }

    void Refresh_PhotoView()
    {
        wSrc = mBitmap.getWidth();
        hSrc = mBitmap.getHeight();
        scale0 = Math.min(((float) hDrawingPane) / hSrc, ((float) wDrawingPane) / wSrc);
        scale0 = scale0 * (float)0.8;
        matrix.reset();
        matrix.postScale(scale0, scale0);
        xInDrawingPane = (int) Math.abs(wSrc * scale0 - wDrawingPane) / 2;
        yInDrawingPane = (int) Math.abs(hSrc * scale0 - hDrawingPane) / 2;
        matrix.postTranslate(xInDrawingPane, yInDrawingPane);

        ivPhotoOriginal.setImageBitmap(mBitmap);
        ivPhotoOriginal.setImageMatrix(matrix);
        previewBitmap = Bitmap.createScaledBitmap(mBitmap, (int)(wSrc *scale0), (int)(hSrc *scale0), true);
        ivPhoto.setImageBitmap(previewBitmap);
        Matrix matrix1 = new Matrix();
        matrix1.postTranslate(xInDrawingPane, yInDrawingPane);
        ivPhoto.setImageMatrix(matrix1);
        ivPhotoOriginal.setVisibility(View.INVISIBLE);
        heightView.setDrawCacheSize(xInDrawingPane, yInDrawingPane, (int)(wSrc * scale0), (int)(hSrc * scale0));
        heightView.initStretchRect();
    }

    void RefreshPreview()
    {
        ivPhoto.setImageBitmap(previewBitmap);
        Matrix matrix1 = new Matrix();
        int yTrans = (int) Math.abs(previewBitmap.getHeight() - hDrawingPane) / 2;
        matrix1.postTranslate(xInDrawingPane, yTrans);
        ivPhoto.setImageMatrix(matrix1);
        heightView.setDrawCacheSize(xInDrawingPane, yTrans, previewBitmap.getWidth(), previewBitmap.getHeight());
    }

    PressableButton.OnPressedListener previewPressedListener = new PressableButton.OnPressedListener() {
        @Override
        public void OnPressed(View view) {
            ivPhoto.setVisibility(View.INVISIBLE);
            heightView.setVisibility(View.INVISIBLE);
            ivPhotoOriginal.setVisibility(View.VISIBLE);
        }

        @Override
        public void OnReleased(View view) {
            ivPhoto.setVisibility(View.VISIBLE);
            heightView.setVisibility(View.VISIBLE);
            ivPhotoOriginal.setVisibility(View.INVISIBLE);
        }
    };

    Bitmap m_tmpBitmap = null;

    int topAreaHeight=0;
    int bottomAreaHeight=0;
    int areaHeight=0;
    int stretchedHeight = 0;

    void ResizeHeight()
    {
        RectF stRect = heightView.getStretchArea();

        int param = seekParam - oldSeekParam;
        stretchedHeight = (int)stRect.height() + (int)((float)param / 5);
        int realStretchedHeight = (int)(stretchedHeight/scale0);
        if(realStretchedHeight < 2) {
            realStretchedHeight = 2;
            stretchedHeight = (int)(realStretchedHeight * scale0);
        }

        int yTrans = (int) Math.abs(m_tmpBitmap.getHeight() - hDrawingPane) / 2;

        topAreaHeight = (int)stRect.top - yTrans;
        if(topAreaHeight == 0) topAreaHeight = 1;
        bottomAreaHeight = m_tmpBitmap.getHeight() - topAreaHeight-(int)stRect.height();
        if(bottomAreaHeight == 0) bottomAreaHeight = 1;
        areaHeight = m_tmpBitmap.getHeight() - topAreaHeight - bottomAreaHeight;
        if(areaHeight == 0) areaHeight = 1;

        Bitmap bm = Bitmap.createBitmap(m_tmpBitmap.getWidth(), (int)(m_tmpBitmap.getHeight() + (stretchedHeight - stRect.height())), Bitmap.Config.ARGB_8888);
        Bitmap bmTop = Bitmap.createBitmap(m_tmpBitmap, 0,0, m_tmpBitmap.getWidth(), topAreaHeight);
        Bitmap bmBottom = Bitmap.createBitmap(m_tmpBitmap, 0,  m_tmpBitmap.getHeight()-bottomAreaHeight, m_tmpBitmap.getWidth(), bottomAreaHeight);
        Bitmap bmArea = Bitmap.createBitmap(m_tmpBitmap, 0, topAreaHeight, m_tmpBitmap.getWidth(), areaHeight);
        bmArea = Bitmap.createScaledBitmap(bmArea, m_tmpBitmap.getWidth(),stretchedHeight, true);

        Canvas bmCanvas = new Canvas(bm);
        bmCanvas.drawBitmap(bmTop, 0,0, new Paint());
//        bmCanvas.drawBitmap(bmBottom, 0, topAreaHeight + stretchedHeight, new Paint());
        bmCanvas.drawBitmap(bmBottom, 0, bm.getHeight() -bottomAreaHeight, new Paint());
        bmCanvas.drawBitmap(bmArea, 0, topAreaHeight, new Paint());
        previewBitmap = Bitmap.createBitmap(bm);
    }

    void MakeResultBitmap()
    {
        int realStHeight = (int)(stretchedHeight / scale0);
        int realtopAreaHeight=(int) (topAreaHeight /scale0);
        int realbottomAreaHeight=(int)(bottomAreaHeight/scale0);
       // int realareaHeight=(int)(areaHeight/scale0);
        int realareaHeight = mResultBitmap.getHeight() - realbottomAreaHeight - realtopAreaHeight;

        Bitmap bmTop = Bitmap.createBitmap(mResultBitmap, 0,0, mResultBitmap.getWidth(), realtopAreaHeight);
        Bitmap bmBottom = Bitmap.createBitmap(mResultBitmap, 0,  mResultBitmap.getHeight()-realbottomAreaHeight, mResultBitmap.getWidth(), realbottomAreaHeight);
        Bitmap bmArea = Bitmap.createBitmap(mResultBitmap, 0, realtopAreaHeight, mResultBitmap.getWidth(), realareaHeight  );
        bmArea = Bitmap.createScaledBitmap(bmArea, mResultBitmap.getWidth(),realStHeight, true);

        mResultBitmap = Bitmap.createBitmap(wSrc, mResultBitmap.getHeight() + (realStHeight-realareaHeight), Bitmap.Config.ARGB_8888);
        Canvas bmCanvas = new Canvas(mResultBitmap);
        bmCanvas.drawBitmap(bmTop, 0,0, new Paint());
        bmCanvas.drawBitmap(bmBottom, 0, realtopAreaHeight + realStHeight, new Paint());
 //       bmCanvas.drawBitmap(bmBottom, 0, mResultBitmap.getHeight() -realbottomAreaHeight, new Paint());
        bmCanvas.drawBitmap(bmArea, 0, realtopAreaHeight, new Paint());
    }

    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.button_accept)
                m_onClickListener.onClick(Dialog_PhotoHeight.this, DialogInterface.BUTTON_POSITIVE);
            else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_PhotoHeight.this, BUTTON_NEGATIVE);
            else if(v.getId() == R.id.bt_reset) {
                Refresh_PhotoView();
                ResetSeekBar();
            }
        }
    };

    public void ResetSeekBar()
    {
        oldSeekParam = 0;
        seekParam = 0;
        seekBar_height.setOnValueChangeListener(null);
        seekBar_height.setProgress(100);
        seekBar_height.setOnValueChangeListener(sliderChangeListener);
    }

    int seekParam = 0;
    int oldSeekParam = 0;
    Slider.OnValueChangeListener sliderChangeListener = new Slider.OnValueChangeListener() {
        @Override
        public void onProgressChanged(Slider slider, int progress, boolean fromUser) {
            txtView_seekValue.setText(""+(progress-100));
            seekParam = progress - 100;

            RectF rt = heightView.getStretchArea();
            int stHeight = (int)rt.height() + (int)((float)(seekParam - oldSeekParam) / 5);
            if(stHeight < 2) {
                slider.setProgress(progress + 1);
                seekParam += 1;
                return;
            }
            ResizeHeight();
            RefreshPreview();
        }

        @Override
        public void onStartTrackingTouch(Slider slider) {
            txtView_seekValue.setText(""+(slider.getProgress()-100));
            txtView_seekValue.setVisibility(View.VISIBLE);
            seekParam = slider.getProgress() - 100;
            m_tmpBitmap = Bitmap.createBitmap(previewBitmap);
            oldSeekParam = seekParam;

            heightView.setVisibility(View.INVISIBLE);

            ResizeHeight();
            RefreshPreview();
        }

        @Override
        public void onStopTrackingTouch(Slider seekBar) {
            txtView_seekValue.setVisibility(View.INVISIBLE);
            txtView_seekValue.setText(""+(seekBar.getProgress()-100));
            seekParam = seekBar.getProgress() - 100;
            oldSeekParam = seekParam;
            RefreshPreview();
            heightView.resizeStretchRect(topAreaHeight, stretchedHeight);
            heightView.setVisibility(View.VISIBLE);
            MakeResultBitmap();
        }
    };

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            txtView_seekValue.setText(""+(progress-100));
            seekParam = progress - 100;

            RectF rt = heightView.getStretchArea();
            int stHeight = (int)rt.height() + (int)((float)(seekParam - oldSeekParam) / 5);
            if(stHeight < 2) {
                seekBar.setProgress(progress + 1);
                seekParam += 1;
                return;
            }
            ResizeHeight();
            RefreshPreview();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            txtView_seekValue.setText(""+(seekBar.getProgress()-100));
            txtView_seekValue.setVisibility(View.VISIBLE);
            seekParam = seekBar.getProgress() - 100;
            m_tmpBitmap = Bitmap.createBitmap(previewBitmap);
            oldSeekParam = seekParam;

            heightView.setVisibility(View.INVISIBLE);

            ResizeHeight();
            RefreshPreview();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            txtView_seekValue.setVisibility(View.INVISIBLE);
            txtView_seekValue.setText(""+(seekBar.getProgress()-100));
            seekParam = seekBar.getProgress() - 100;
            oldSeekParam = seekParam;
            RefreshPreview();
            heightView.resizeStretchRect(topAreaHeight, stretchedHeight);
            heightView.setVisibility(View.VISIBLE);
            MakeResultBitmap();

        }
    };
}
