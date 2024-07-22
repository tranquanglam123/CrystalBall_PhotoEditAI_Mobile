package com.samhung.crystalball.photoeditor.Photoshop.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views.PhotoCroppingView;
import com.samhung.crystalball.photoeditor.R;

public class Dialog_PhotoCrop extends FullScreenDialog {
    OnClickListener m_onClickListener = null;

    Context mContext = null;

    ImageView ivPhoto = null;
    PhotoCroppingView croppingView = null;
    RadioGroup rdg_main = null;

    int m_selectedToolType = 1;

    FullScreenDialog me = null;

    public Dialog_PhotoCrop(Context context , Bitmap bitmap, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mBitmap = bitmap;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo_crop);
        me = this;
        rdg_main = (RadioGroup)findViewById(R.id.rdg_crop);
        croppingView = (PhotoCroppingView)findViewById(R.id.photo_cropview);
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
        rdg_main.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId)
                {
                    case R.id.rb_crop_origin:
                        m_selectedToolType = PhotoCroppingView.CROP_ORIGIN;
                        break;
                    case R.id.rb_crop_custom:
                        m_selectedToolType = PhotoCroppingView.CROP_CUSTOM;
                        break;
                    case R.id.rb_crop_1_1:
                        m_selectedToolType = PhotoCroppingView.CROP_1_1;
                        break;
                    case R.id.rb_crop_3_4:
                        m_selectedToolType = PhotoCroppingView.CROP_3_4;
                        break;
                    case R.id.rb_crop_4_3:
                        m_selectedToolType = PhotoCroppingView.CROP_4_3;
                        break;
                    case R.id.rb_crop_9_16:
                        m_selectedToolType = PhotoCroppingView.CROP_9_16;
                        break;
                    case R.id.rb_crop_16_9:
                        m_selectedToolType = PhotoCroppingView.CROP_16_9;
                        break;
                }
                Dialog_PhotoCrop.this.croppingView.SetCropType(m_selectedToolType);
                croppingView.invalidate();
            }
        });
    }

    void Refresh_PhotoView()
    {
        wSrc = mBitmap.getWidth();
        hSrc = mBitmap.getHeight();
        scale0 = Math.min(((float) hDrawingPane) / hSrc, ((float) wDrawingPane) / wSrc);
        scale0 = scale0 * (float)0.9;
        matrix.reset();
        matrix.postScale(scale0, scale0);
        xInDrawingPane = (int) Math.abs(wSrc * scale0 - wDrawingPane) / 2;
        yInDrawingPane = (int) Math.abs(hSrc * scale0 - hDrawingPane) / 2;
        matrix.postTranslate(xInDrawingPane, yInDrawingPane);
        croppingView.initRect(xInDrawingPane, yInDrawingPane, wSrc*scale0, hSrc*scale0, scale0);

        ivPhoto.setImageBitmap(mBitmap);
        ivPhoto.setImageMatrix(matrix);
    }

    public void MakeResultBitmap()
    {
        RectF rect = new RectF(Dialog_PhotoCrop.this.croppingView.getRect());

        mResultBitmap = Bitmap.createBitmap((int)(rect.width()/scale0), (int)(rect.height()/scale0), mBitmap.getConfig());
        Canvas canvas = new Canvas(mResultBitmap);
        Matrix mat = new Matrix();
        mat.postTranslate(-(rect.left-xInDrawingPane)/scale0, -(rect.top-yInDrawingPane)/scale0);
        canvas.drawBitmap(mBitmap, mat, new Paint());
    }

    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.button_accept) {
                MakeResultBitmap();
                m_onClickListener.onClick(Dialog_PhotoCrop.this, DialogInterface.BUTTON_POSITIVE);
            }else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_PhotoCrop.this, BUTTON_NEGATIVE);
        }
    };
}
