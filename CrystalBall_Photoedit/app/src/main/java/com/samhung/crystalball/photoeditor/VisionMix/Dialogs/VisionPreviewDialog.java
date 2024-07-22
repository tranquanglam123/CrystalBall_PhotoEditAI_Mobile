package com.samhung.crystalball.photoeditor.VisionMix.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.Common.GifAnimationDrawable;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.VisionMix.Activities.VisionmixActivity;
import com.samhung.crystalball.photoeditor.Widget.GifView;

public class VisionPreviewDialog extends FullScreenDialog {
    OnClickListener m_onClickListener = null;
    public Bitmap mergeBitmap = null;
    Context mContext = null;
    public int m_btnWhich = 0;

    int nCallerType = 0;
    public VisionPreviewDialog(Context context , Bitmap bitmap, OnClickListener onClickListener, int nCallerType){
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mergeBitmap = bitmap;
        this.nCallerType = nCallerType;
    }

    ImageView ivPhoto = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_vision_preview);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));
        ivPhoto = (ImageView)findViewById(R.id.iv_photo);
        ivPhoto.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (wDrawingPane == 0 || hDrawingPane == 0 || (wDrawingPane != right-left) || (hDrawingPane !=bottom-top)) {
                    wDrawingPane = right - left;
                    hDrawingPane = bottom - top;
                    layout_Left = left;
                    layout_Top = top;

                    wSrc = mergeBitmap.getWidth();
                    hSrc = mergeBitmap.getHeight();
    //                   Toast.makeText(mContext, "길이:" + (right - left) + ",  너비:" + (bottom - top), Toast.LENGTH_LONG).show();
                    scale0 = Math.min(((float) hDrawingPane) / hSrc, ((float) wDrawingPane) / wSrc);

                    matrix.reset();
                    matrix.postScale(scale0, scale0);
                    xInDrawingPane = (int) Math.abs(wSrc * scale0 - wDrawingPane) / 2;
                    yInDrawingPane = (int) Math.abs(hSrc * scale0 - hDrawingPane) / 2;
                    matrix.postTranslate(xInDrawingPane, yInDrawingPane);

                    ivPhoto.setImageBitmap(mergeBitmap);
                    ivPhoto.setImageMatrix(matrix);

//                    ImageView ivGif = (ImageView)findViewById(R.id.imageView1);
//                    GifAnimationDrawable gif = null;
//                    try {
//                        int id = R.drawable.gif_rose;
//                        gif = new GifAnimationDrawable(getContext().getResources().openRawResource(id));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                    gif.setOneShot(false);
//                    ivGif.setImageDrawable(gif);
//                    gif.setVisible(true, true);

                }
            }
        });


        ImageView btnSave = (ImageView)findViewById(R.id.iv_save);
        ImageView btnBack = (ImageView)findViewById(R.id.iv_back);
        btnSave.setOnClickListener(btnClickListener);
        btnBack.setOnClickListener(btnClickListener);
        findViewById(R.id.iv_phedit).setOnClickListener(btnClickListener);
        findViewById(R.id.iv_phsticker).setOnClickListener(btnClickListener);
        if(nCallerType == 1)
            findViewById(R.id.rl_phedit).setVisibility(View.GONE);
        if(nCallerType == 2)
            findViewById(R.id.rl_phsticker).setVisibility(View.GONE);
//        GifView gifView = (GifView)findViewById(R.id.gifView3);
//        gifView.setImageResource(R.drawable.gif_rose);

    }

    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.iv_back)
                m_onClickListener.onClick(VisionPreviewDialog.this, BTN_BACK);
            else if(v.getId() == R.id.iv_save)
                m_onClickListener.onClick(VisionPreviewDialog.this, BTN_SAVE);
            else if(v.getId() == R.id.iv_phedit) {
                m_btnWhich = BTN_PHEDIT;
                m_onClickListener.onClick(VisionPreviewDialog.this, BTN_PHEDIT);
            }
            else if(v.getId() == R.id.iv_phsticker) {
                m_btnWhich = BTN_STICKER;
                m_onClickListener.onClick(VisionPreviewDialog.this, BTN_STICKER);
            }
        }
    };
    public static final int BTN_BACK = 111;
    public static final int BTN_SAVE= 222;
    public static final int BTN_PHEDIT = 333;
    public static final int BTN_STICKER=444;

}
