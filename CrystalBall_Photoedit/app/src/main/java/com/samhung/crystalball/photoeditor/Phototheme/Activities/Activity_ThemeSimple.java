package com.samhung.crystalball.photoeditor.Phototheme.Activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.GalleryUtils;

import org.CrystalBall.Engine;

public class Activity_ThemeSimple extends BaseActivity   implements View.OnTouchListener{

    public enum THEME_TYPE {
            ROMANTIC,
            SELFIE,
            MULTI,
            ANIMAL,
            MAT_EFFECT
    }

    ImageView ivTheme = null;

    Bitmap bitmap_Theme = null;
    Bitmap bitmap_photo = null;
    Bitmap[] selected_bitmps = new Bitmap[2];

    Matrix matrix = new Matrix();
    int wSrc = 0;
    int hSrc = 0;
    int wDrawingPane = 0;
    int hDrawingPane = 0;
    int xInDrawingPane =0;
    int yInDrawingPane =0;
    int layout_Left = 0;
    int layout_Top = 0;
    float scale0 =1;

    int m_nImageIndex = 0;
    int m_themeIndex = 1;
    THEME_TYPE m_themeType = THEME_TYPE.ROMANTIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity__theme_simple);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        m_themeIndex = getIntent().getIntExtra("theme_index", 0);
        InitThemeBitmap(m_themeIndex);
        initControls();
    }

    Bitmap loadImage(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inDither = false;                     //Disable Dithering mode
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    void InitThemeBitmap (int index) {

        String strfileName = String.format("theme/theme_0%d.png", index + 1);
        if(index == 1)
            m_themeType = THEME_TYPE.ROMANTIC;
        else if(index == 3 || index ==5 || index == 6)
            m_themeType = THEME_TYPE.SELFIE;

        Log.d("--------", strfileName);
        try {
            bitmap_Theme = BitmapFactory.decodeStream(getAssets().open(strfileName));
            bitmap_photo = Bitmap.createBitmap(bitmap_Theme);
        }catch (Exception ex) {}
    }
    private void initControls()
    {
        if(m_themeType != THEME_TYPE.ROMANTIC) {
            findViewById(R.id.btn_img2).setVisibility(View.GONE);
        }

        selected_bitmps[0] = Bitmap.createBitmap(10,10, Bitmap.Config.ARGB_8888);
        selected_bitmps[1] = Bitmap.createBitmap(10,10, Bitmap.Config.ARGB_8888);
        ivTheme = (ImageView)findViewById(R.id.iv_foreground);

        ivTheme.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (wDrawingPane == 0 || hDrawingPane == 0 || (wDrawingPane != right-left) || (hDrawingPane !=bottom-top)) {
                    wDrawingPane = right - left;
                    hDrawingPane = bottom - top;
                    layout_Left = left;
                    layout_Top = top;



                    refresh_ImageView();
                }
            }
        });
    }

    public void refresh_ImageView()
    {
        wSrc = bitmap_Theme.getWidth();
        hSrc = bitmap_Theme.getHeight();
        scale0 = Math.min(((float) hDrawingPane) /hSrc, ((float) wDrawingPane) /wSrc);

        matrix.reset();
        matrix.postScale(scale0, scale0);
        xInDrawingPane = (int)Math.abs(wSrc* scale0 - wDrawingPane)/2;
        yInDrawingPane = (int)Math.abs(hSrc* scale0 - hDrawingPane)/2;
        matrix.postTranslate(xInDrawingPane, yInDrawingPane);

        ivTheme.setImageBitmap(bitmap_Theme);
        ivTheme.setImageMatrix(matrix);
        ivTheme.setOnTouchListener(this);

        makeTheme();
    }

    public void onButtonClick(View view) {
        if(view.getId() == R.id.btn_img1) {
            m_nImageIndex = 0;
            GalleryUtils.openGallery(this);
        } else if(view.getId() == R.id.btn_img2) {
            m_nImageIndex = 1;
            GalleryUtils.openGallery(this);
        } else if(view.getId() == R.id.button_close) {
            finish();
        }
    }

    Matrix savedMatrix = new Matrix();
    int mode = NONE;

    PointF start=new PointF();
    PointF mid=new PointF();
    float oldDist=0;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ivTheme.setScaleType(ImageView.ScaleType.MATRIX);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);

                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    start.set(event.getX(), event.getY());
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                RefreshLayoutPos();
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x,
                            event.getY() - start.y);
                }
                else if (mode == ZOOM) {
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
        }
        ivTheme.setImageMatrix(matrix);
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

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case RESULT_CANCELED:

                break;
            case RESULT_OK:
                if (requestCode == GalleryUtils.GALLERY_INTENT_CALLED || requestCode == GalleryUtils.CAMERA_CODE
                        || requestCode == GalleryUtils.GALLERY_KITKAT_INTENT_CALLED) {

                    String path = "";
                    int back_idx = 0;

                    if (requestCode == GalleryUtils.GALLERY_INTENT_CALLED) {
                        Uri uri = data.getData();
                        path = GalleryUtils.getFilePathFromUri(this, uri);
                    } else if (requestCode == GalleryUtils.GALLERY_KITKAT_INTENT_CALLED) {
                        Uri uri = data.getData();
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        // Check for the freshest data.
                        if (uri != null) {
//                            getContentResolver().takePersistableUriPermission(
//                                    uri, takeFlags);
                            path = GalleryUtils.getFilePathFromUri(this, uri);
                        }
                    }

                   selected_bitmps[m_nImageIndex] = loadImage(path);
                    makeTheme();
                    break;
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void makeTheme()
    {
        bitmap_photo = Bitmap.createBitmap(bitmap_Theme.getWidth(), bitmap_Theme.getHeight(), Bitmap.Config.ARGB_8888);

        if(m_themeType == THEME_TYPE.ROMANTIC ) {
            int[] pPoints = new int[] {46, 407, 74, 957,  548, 913,  510, 400 , 638, 498, 814, 998 , 1258, 838 , 1097, 364};
            Engine.getIntance().ThemeRomantic(selected_bitmps[0], selected_bitmps[1], bitmap_photo, pPoints);
        } else if(m_themeType == THEME_TYPE.SELFIE) {
            int[] pPoints = null;
            if(m_themeIndex == 3)
                pPoints = new int[]{27, 578, 180, 1200, 636,1080,489, 476,582, 81, 411,928, 1050, 1110, 1245, 223};
            else if(m_themeIndex == 5)
                pPoints = new int[] {144,287,144, 998, 920,999, 930,284,144,287,144, 998, 920,999, 930,284};
            else if(m_themeIndex == 6)
                pPoints = new int[] { 190, 220, 190, 870, 560, 870, 560, 220, 10,10, 10, 1270, 1270, 1270, 1270, 10};
            selected_bitmps[1] = Bitmap.createBitmap(selected_bitmps[0]);
            Engine.getIntance().ThemeRomantic(selected_bitmps[0], selected_bitmps[1], bitmap_photo, pPoints);
        }

        Canvas tmpCanvas = new Canvas(bitmap_photo);

        tmpCanvas.drawBitmap(bitmap_Theme, 0, 0, new Paint());
        ivTheme.setImageBitmap(bitmap_photo);
    }
}

