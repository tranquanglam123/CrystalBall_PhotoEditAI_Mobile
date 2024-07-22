package com.samhung.crystalball.photoeditor.Photoshop.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views.FaceDrawingView;
import com.samhung.crystalball.photoeditor.Photoshop.PhotoeditActivity;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.widgets.PressableButton;
import com.samhung.crystalball.widgets.Slider;

import org.CrystalBall.Engine;

public class Dialog_PhotoBeauty extends FullScreenDialog implements View.OnTouchListener{
    OnClickListener m_onClickListener = null;
    public Bitmap mBitmap = null;
    int m_nFaceCount = 0;
    int m_nFaceIndex=0;
    Rect[] faceRects = null;

    Context mContext = null;

    ImageView ivPhoto = null;
    ImageView ivPhotoOriginal = null;
    Slider seekBar_main = null;
    FaceDrawingView faceDrawingView = null;
    TextView txSeekValue = null;

    ImageView btnFaceChange = null;
    PressableButton btnPreview = null;

    Canvas resultCanvas;


    int[] m_progressValues = null;

    public Dialog_PhotoBeauty(Context context , String type, Bitmap bitmap,int faceCount, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mBitmap = bitmap;
        m_strCommand = type;
        mResultBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);

        m_nFaceCount = faceCount;
        m_progressValues = new int[faceCount];
        faceRects = new Rect[faceCount];
        //mFaceBitmaps = new Bitmap[faceCount];

        resultCanvas = new Canvas(mResultBitmap);
        resultCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        initFacesValues();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_photo_beauty);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));

        seekBar_main = (Slider) findViewById(R.id.seekBar_main);
        txSeekValue = (TextView)findViewById(R.id.textView_seekBar);
        InitSlider();
        faceDrawingView = (FaceDrawingView)findViewById(R.id.faceDrawView);
        btnFaceChange = (ImageView)findViewById(R.id.imv_facechange);
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

                    if(m_strCommand == "beauty") {
                        btnFaceChange.setVisibility(View.INVISIBLE);
                        faceDrawingView.setVisibility(View.INVISIBLE);
                    }
                    faceDrawingView.setDestCacheSize(wSrc, hSrc, left, top, right, bottom);
                    faceDrawingView.draw_Landmarks(Engine.getIntance().getLandmarkInfo(m_nFaceIndex));
                    faceDrawingView.setMatrix(matrix);
                }
            }
        });

        ImageView btnAccept = (ImageView)findViewById(R.id.button_accept);
        ImageView btnBack = (ImageView)findViewById(R.id.button_close);
        btnAccept.setOnClickListener(btnClickListener);
        btnBack.setOnClickListener(btnClickListener);
        btnFaceChange.setOnClickListener(btnClickListener);

        if(m_nFaceCount < 2) {
            btnFaceChange.setVisibility(View.INVISIBLE);
//            faceDrawingView.setVisibility(View.INVISIBLE);
        }
    }

    void Refresh_PhotoView()
    {
        wSrc = mResultBitmap.getWidth();
        hSrc = mResultBitmap.getHeight();
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
    }

    int paramValue = 0;
    void InitSlider()
    {
        seekBar_main.setMax(100);
        if(m_strCommand == "beauty")
            seekBar_main.setMiddleValue(0);
        else if(m_strCommand=="jaw")
            seekBar_main.setMiddleValue(30);
        else
            seekBar_main.setMiddleValue(50);

        seekBar_main.setProgress(m_progressValues[m_nFaceIndex] );

        seekBar_main.setOnValueChangeListener(sliderChangeListener);
    }
    Slider.OnValueChangeListener sliderChangeListener = new Slider.OnValueChangeListener() {
        @Override
        public void onProgressChanged(Slider slider, int progress, boolean fromUser) {
            if(m_strCommand == "face" || m_strCommand == "eye")
            {
                paramValue = (progress - 50) * 2;
                txSeekValue.setText(""+paramValue);
            }
            else if(m_strCommand == "jaw") {
                paramValue = progress;
                txSeekValue.setText(""+(paramValue - 30));
            } else if (m_strCommand == "beauty") {
                paramValue = progress;
                txSeekValue.setText(""+paramValue);
            }
            else {
                paramValue = progress;
                txSeekValue.setText(""+(paramValue - 50));
            }
        }

        @Override
        public void onStartTrackingTouch(Slider slider) {
            txSeekValue.setText(""+slider.getProgress());
            int progress = slider.getProgress();
            if(m_strCommand == "face" || m_strCommand == "eye")
            {
                paramValue = (progress - 50) * 2;
                txSeekValue.setText(""+paramValue);
            }
            else if(m_strCommand == "jaw") {
                paramValue = progress;
                txSeekValue.setText(""+(paramValue - 30));
            } else if (m_strCommand == "beauty") {
                paramValue = progress;
                txSeekValue.setText(""+paramValue);
            }
            else {
                paramValue = progress;
                txSeekValue.setText(""+(paramValue - 50));
            }
            txSeekValue.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(Slider seekBar) {
            txSeekValue.setVisibility(View.INVISIBLE);
            m_progressValues[m_nFaceIndex] = seekBar.getProgress();
            //ProcessPhoto(m_strCommand, paramValue);
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
        faceDrawingView.setMatrix(matrix);
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
            if(v.getId() == R.id.button_accept) {
                makeRealResultBitamp();
                m_onClickListener.onClick(Dialog_PhotoBeauty.this, DialogInterface.BUTTON_POSITIVE);
            } else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_PhotoBeauty.this, BUTTON_NEGATIVE);
            else if(v.getId() == R.id.imv_facechange) {
                NextFace();
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
    void initFacesValues() {
        int defaultProgressValue = 50;
        switch (m_strCommand)
        {
            case "face":
                defaultProgressValue = 50;
                break;
            case "eye":
                defaultProgressValue = 50;
                break;
            case "jaw":
                defaultProgressValue = 30;
                break;
            case "lip":

                break;
            case "norse":

                break;
            case "forehead":

                break;
            case "beauty":
                defaultProgressValue = 0;
                break;
        }

        for(int i=0; i<m_nFaceCount; i++)
        {
            m_progressValues[i] = defaultProgressValue;
        }
    }

    void ProcessPhoto(String strCommand, int paramValue)
    {
        byte[] retImage = null;
        Bitmap bmFace = null;
        switch (strCommand)
        {
            case "face":
                bmFace = getFaceBitmap();
                retImage = Engine.getIntance().FaceMorphing(bmFace,m_nFaceIndex,0, paramValue);
                //retImage = Engine.getIntance().FaceMorphing(mBitmap,m_nFaceIndex,paramValue, 0);
                if(retImage == null) return;
                break;
            case "eye":
                bmFace = getFaceBitmap();
                retImage = Engine.getIntance().FaceMorphing(bmFace,m_nFaceIndex,paramValue, 0);
                //retImage = Engine.getIntance().FaceMorphing(mBitmap,m_nFaceIndex,paramValue, 0);
                break;
            case "jaw":
                bmFace = getFaceBitmap();
                retImage = Engine.getIntance().FaceJaw(bmFace, m_nFaceIndex, paramValue);
                break;
            case "lip":
                bmFace = getFaceBitmap();
                retImage = Engine.getIntance().FaceLip(bmFace, m_nFaceIndex, paramValue);
                break;
            case "norse":
                bmFace = getFaceBitmap();
                retImage = Engine.getIntance().FaceNose(bmFace, m_nFaceIndex, paramValue);
                break;
            case "forehead":
                bmFace = getFaceBitmap();
                retImage = Engine.getIntance().FaceForehead(bmFace, m_nFaceIndex, paramValue);
                break;
            case "beauty":
              //  mResultBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
                Engine.getIntance().FaceBeauty(mBitmap, mResultBitmap, paramValue);
                break;
        }

        if(strCommand != "beauty") {
            if(retImage == null) return;
            Bitmap edited = ImageUtils.getBitmapFrom24ByteArray(retImage, faceRects[m_nFaceIndex].width());
            makeResultBitmap(edited);
        }
        //mResultBitmap = ImageUtils.getBitmapFrom24ByteArray(retImage, mBitmap.getWidth());
//        ivPhoto.setImageBitmap(mResultBitmap);
//        ivPhoto.setImageMatrix(matrix);
    }

    Bitmap getFaceBitmap()
    {
        int[] landmark = Engine.getIntance().getLandmarkInfo(m_nFaceIndex);
        faceRects[m_nFaceIndex] = Engine.getFaceRectLandmark(landmark);

        Rect faceRect = faceRects[m_nFaceIndex];
        int left = faceRect.left;
        int top = faceRect.top;
        int faceWid = faceRect.width();
        int faceHei = faceRect.height();
        int cx = 0; int cy=0;
        if(faceRect.bottom > mBitmap.getHeight())
            faceHei = mBitmap.getHeight() - faceRect.top;
        if(faceRect.right > mBitmap.getWidth())
            faceWid = mBitmap.getWidth() - faceRect.left;
        if(faceRect.left < 0)
        { left = 0; cx = -faceRect.left;}
        if(faceRect.top < 0)
        { top = 0; cy = -faceRect.top;}

        Bitmap tmp = Bitmap.createBitmap(mBitmap, left, top, faceWid, faceHei);
        Bitmap faceBitmap = Bitmap.createBitmap(faceRect.width(), faceRect.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(faceBitmap);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(tmp, cx, cy, new Paint());
        return faceBitmap;
    }

    void makeResultBitmap(Bitmap editedFaceBitmap){
//        mResultBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas resultCanvas = new Canvas(mResultBitmap);
//        resultCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
   //     resultCanvas.drawBitmap(mBitmap, 0, 0, new Paint());
        resultCanvas.drawBitmap(editedFaceBitmap, faceRects[m_nFaceIndex].left,faceRects[m_nFaceIndex].top, new Paint());
    }

    void makeRealResultBitamp()
    {
        Bitmap bm = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas bmCanvas = new Canvas(bm);
        bmCanvas.drawBitmap(mBitmap, 0, 0, new Paint());
        bmCanvas.drawBitmap(mResultBitmap, 0, 0, new Paint());
        mResultBitmap = Bitmap.createBitmap(bm);
    }
    void NextFace(){
        if( m_nFaceCount < 1) return;
        m_nFaceIndex = (m_nFaceIndex + 1) % m_nFaceCount;
        //mFaceBitmaps[m_nFaceCount] = getFaceBitmap();
        faceDrawingView.draw_Landmarks(Engine.getIntance().getLandmarkInfo(m_nFaceIndex));
        faceDrawingView.invalidate();

        seekBar_main.setProgress(m_progressValues[m_nFaceIndex]);
    }

    public class AsyncTask_ProcessEffect extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            ProcessPhoto(m_strCommand, paramValue);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HideWaitDialog();
            ivPhoto.setImageBitmap(mResultBitmap);
            ivPhoto.setImageMatrix(matrix);
        }
    };

}
