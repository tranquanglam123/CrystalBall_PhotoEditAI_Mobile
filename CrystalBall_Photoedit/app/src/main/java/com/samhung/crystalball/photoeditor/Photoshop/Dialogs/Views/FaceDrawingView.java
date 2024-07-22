package com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FaceDrawingView extends View {

    private float xOff;
    private float yOff;
    private float scale = 1;

    private int wDst;
    private int hDst;

    public FaceDrawingView(Context context) {
        this(context, null);
    }

    public FaceDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupBrushDrawing();
    }

    public FaceDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupBrushDrawing();
    }

    void setupBrushDrawing() {
    }

    public void setDestCacheSize(int w, int h, int pLeft, int pTop, int pRight, int pBottom) {
        wDst = w;
        hDst = h;
//        tempBitmap = Bitmap.createBitmap(pRight-pLeft, pBottom - pTop,  Bitmap.Config.ARGB_8888);
//        tempCanvas = new Canvas(tempBitmap);
    }

    public void setMatrix(Matrix matrix) {
        float[] x = new float[9];
        matrix.getValues(x);
        scale = x[Matrix.MSCALE_X];
        xOff = x[Matrix.MTRANS_X];
        yOff =  x[Matrix.MTRANS_Y];
        invalidate();
    }

    public int[] m_landmarkPoints = null;
    public void draw_Landmarks(int[] landmark)
    {
        m_landmarkPoints = landmark;
//        invalidate();
    }

    public Rect EngineRectToAppRect(int x, int y, int width, int height, int ImgWidth, int ImgHeight)
    {
        Rect rect = new Rect();
        int nWidth = getWidth();
        int nHeight = getHeight();

        rect.right = (x+width)*nWidth/ImgWidth;
        rect.left =   nWidth - rect.right - 1;
        rect.right = rect.left + width*nWidth/ImgWidth;

        //flip start
//        int tmpLeft = rect.left;
//        int tmpRight = rect.right;
//
//        rect.right = nWidth - tmpLeft - 1;
//        rect.left = rect.right - (tmpRight-tmpLeft);
        //flip end

        rect.top = y*nHeight/ImgHeight;
        rect.bottom=(y+height)*nHeight/ImgHeight;
        return rect;
    }

    public PointF EnginePointToAppPoint(int x, int y,int ImgWidth, int ImgHeight)
    {
        PointF pt = new PointF();
        int nWidth = getWidth();
        int nHeight = getHeight();

        pt.x = x * nWidth / ImgWidth;
        pt.y = y * nHeight / ImgHeight;

        return pt;
    }
    @Override
    protected void onDraw(Canvas canvas) {
//        if(tempBitmap == null) return;

//        Matrix matrix = new Matrix();
//        matrix.postScale(scale, scale);
//        matrix.postTranslate(xOff, yOff);
//        tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if(m_landmarkPoints!=null)
        {
            RectF rtt = new RectF();
            rtt.left = xOff +(m_landmarkPoints[0] * scale);
            rtt.top = yOff +m_landmarkPoints[1] * scale;
            rtt.right = xOff+(m_landmarkPoints[0] + m_landmarkPoints[2]) * scale;
            rtt.bottom = yOff+(m_landmarkPoints[1] + m_landmarkPoints[3]) * scale;

           // Rect rtt = EngineRectToAppRect(rt.left, rt.top, rt.width(), rt.height(), wDst, hDst);
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
            //tempCanvas.drawRect(rtt, paint);
            canvas.drawRect(rtt, paint);
//            tempCanvas.drawLine(rtt.left ,rtt.top,rtt.right,rtt.top,paint);
//            tempCanvas.drawLine(rtt.right,rtt.top,rtt.right,rtt.bottom,paint);
//            tempCanvas.drawLine(rtt.right,rtt.bottom,rtt.left,rtt.bottom,paint);
//            tempCanvas.drawLine(rtt.left,rtt.top,rtt.left,rtt.bottom,paint);
//            for(int i=0; i<70; i++)
//            {
//                PointF pt = EnginePointToAppPoint(m_landmarkPoints[4+i*2], m_landmarkPoints[4+i*2+1], wDst, hDst);
//                RectF rt = new RectF();
//                rt.left = xOff +(m_landmarkPoints[4 + i*2]  - 1)* scale;
//                rt.right = xOff +(m_landmarkPoints[4 + i*2]  + 1)* scale;
//                rt.top = yOff +(m_landmarkPoints[4 + i*2 + 1]  - 1)* scale;
//                rt.bottom = yOff +(m_landmarkPoints[4 + i*2 +1]  + 1)* scale;
//                canvas.drawOval(rt, paint);
//                canvas.drawText(""+i, rt.centerX(), rt.centerY(), paint);
//            }
        }
//        canvas.drawBitmap(tempBitmap, 0, 0, copyingPaint);
//        super.onDraw(canvas);

    }
}
