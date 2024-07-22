package com.samhung.crystalball.photoeditor.Widget;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samhung.crystalball.photoeditor.Utilis.MeasureUtil;

import org.CrystalBall.Engine;

public class NavigatorView extends View{

    private float xOff = 0;
    private float yOff = 0;
    private float scale = 1;

    private Paint copyingPaint;
    private Paint canvasPaint;
    private Bitmap bitmapSource = null;
    private Bitmap bitmapMask = null;

    private Matrix  matrix0 = new Matrix();
    private int         brushSize = 0;

    private int viewWidth = 0;
    private int viewHeight = 0;

    private int parentLeft = 0;
    private int parentTop = 0;
    private int parentRight = 0;
    private int parentBottom = 0;

    public NavigatorView(Context context)
    {
        this(context, null);

    }
    public NavigatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupBrushDrawing();
    }

    public NavigatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupBrushDrawing();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        viewWidth = right-left;
        viewHeight = bottom - top;
    }


    void setupBrushDrawing() {


        copyingPaint = new Paint();
//        copyingPaint.setAlpha(0xAA);
        copyingPaint.setColor(Color.argb(0x90, 255, 90, 90));

        canvasPaint = new Paint(Paint.DITHER_FLAG);
    }

    public void setParentSize( int pLeft, int pTop, int pRight, int pBottom) {
        parentLeft = pLeft;
        parentRight = pRight;
        parentTop = pTop;
        parentBottom = pBottom;
    }

    public void setBitmap(Bitmap bm, Matrix matrix) {
        if(bm != null) {
            //bitmapSource = bm;
            bitmapSource = Bitmap.createScaledBitmap(bm, bm.getWidth()/2, bm.getHeight()/2, true);
        }
        matrix0.set(matrix);

        float x[] = new float[9];
        matrix0.getValues(x);
        scale = x[Matrix.MSCALE_X];
        invalidate();
    }

    public void setBitmapMask(Bitmap bmMask)
    {
        bitmapMask = bmMask;
        invalidate();
    }

    public void setBitmapMask_for_VISIONMIX(Bitmap bmMask)
    {
        bitmapMask = bmMask;
        copyingPaint = new Paint();
        invalidate();
    }

    public void translate(float scale, float xOff, float yOff) {
        if(scale != 0)
            this.scale = scale;
        this.xOff = xOff;
        this.yOff = yOff;
        invalidate();
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = brushSize;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        Matrix matrix = new Matrix();
        matrix.set(matrix0);
        matrix.postTranslate((xOff + viewWidth / 2) , (yOff + viewHeight / 2) );
        float[] x = new float[9];
        matrix.getValues(x);
        x[Matrix.MSCALE_X] *=2;
        x[Matrix.MSCALE_Y] *=2;
        matrix.setValues(x);
        if(bitmapSource!=null)
            canvas.drawBitmap(bitmapSource, matrix, canvasPaint);

        Matrix matrix1 = new Matrix();
        matrix1.postTranslate((xOff + viewWidth / 2) , (yOff + viewHeight / 2) );
        if(bitmapMask !=null)
            canvas.drawBitmap(bitmapMask, matrix1, copyingPaint);

        if(brushSize > 0) {
            Paint pointPaint = new Paint();
            pointPaint.setColor(Color.WHITE);
//            pointPaint.setAlpha(0x80);
            RectF rect = new RectF((viewWidth - brushSize) / 2, (viewHeight - brushSize) / 2,(viewWidth + brushSize) / 2,(viewHeight + brushSize) / 2);
            canvas.drawOval(rect,pointPaint);
            pointPaint = new Paint();
            pointPaint.setStrokeWidth(4);
            pointPaint.setStyle(Paint.Style.STROKE);
            pointPaint.setColor(Color.WHITE);
            canvas.drawCircle(rect.centerX(), rect.centerY(), rect.width()/2, pointPaint);
        }
    }

    public void PositionRefresh(final ViewGroup view, int touch_x, int touch_y) {
        int left = view.getLeft();
        int right = view.getRight();
        int top = view.getTop();
        int bottom = view.getBottom();

        Rect bounds = new Rect(left, top, right, bottom);

        if(bounds.contains(touch_x, touch_y)) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)view.getLayoutParams();
            if(left < MeasureUtil.convertDpToPixels(40, getContext())) {
                params.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            } else {
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            }
            view.setLayoutParams(params);
        }
    }
}
