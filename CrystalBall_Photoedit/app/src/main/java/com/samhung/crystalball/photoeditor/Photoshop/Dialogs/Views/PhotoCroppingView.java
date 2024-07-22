package com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.samhung.crystalball.photoeditor.R;

public class PhotoCroppingView extends View {

    public static final int CROP_ORIGIN = 0;
    public static final int CROP_CUSTOM = 1;
    public static final int CROP_1_1 = 2;
    public static final int CROP_9_16 = 3;
    public static final int CROP_16_9 = 4;
    public static final int CROP_3_4 = 5;
    public static final int CROP_4_3 = 6;

    private Canvas tempCanvas;
    private Bitmap tempBitmap;

    int m_nCropType = 1;

    public PhotoCroppingView(Context context) {
        this(context, null);
    }

    public PhotoCroppingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoCroppingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    RectF rect = new RectF(30, 30, 100, 100);
    RectF rect0 = new RectF();
    RectF original_rect = new RectF();

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        tempBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);

    }

    public void initRect(float x, float y, float width, float height, float parentScale) {
        rect.left = x;
        rect.top = y;
        rect.right = x + width;
        rect.bottom = y + height;
        rect0.set(rect);
        original_rect.set(rect);
        whScale = width/height;
        mParentScale = parentScale;
        invalidate();
    }

    public void SetRect(RectF newRect)
    {
        rect.set(newRect);
//        rect0.set(rect);
        whScale = rect.width()/rect.height();
    //    invalidate();
    }
    public void SetCropType(int nType)
    {
        m_nCropType = nType;
        RectF rectTmp = new RectF();
        rectTmp.set(original_rect);
        switch(m_nCropType)
        {
            case CROP_ORIGIN:
            case CROP_CUSTOM:
                rectTmp.set(original_rect);
                break;
            case CROP_1_1:
                rectTmp = getRatioRect(1,1);
                break;
            case CROP_3_4:
                rectTmp = getRatioRect(3,4);
                break;
            case CROP_4_3:
                rectTmp = getRatioRect(4,3);
                break;
            case CROP_9_16:
                rectTmp = getRatioRect(9,16);
                break;
            case CROP_16_9:
                rectTmp = getRatioRect(16,9);
                break;
        }
        SetRect(rectTmp);
    }

    public RectF getRatioRect(float ratioX, float ratioY)
    {
        float x = original_rect.left;
        float y=original_rect.top;
        float width = original_rect.width();
        float height = original_rect.height();

        if(width / ratioX  * ratioY <= height)
        {
            x = original_rect.left;
            width = original_rect.width();
            height = width / ratioX  * ratioY;
            y = original_rect.top + (original_rect.height() - height) / 2;
        }
        else {
            y = original_rect.top;
            height = original_rect.height();
            width = height / ratioY * ratioX;
            x = original_rect.left + (original_rect.width()-width) / 2;
        }
        RectF rect = new RectF(x,y, x+width, y+height);
        return rect;
    }
    public RectF getRect() {
        return rect;
    }
    final float RADIUS = getResources().getDimension(R.dimen.cropview_cornner_dim);
    @Override
    protected void onDraw(Canvas canvas) {

        tempBitmap.eraseColor(Color.TRANSPARENT);
        tempCanvas.drawColor(Color.parseColor("#AA000000"));

        Paint paint2 = new Paint();
        paint2.setStyle(Paint.Style.FILL);
        paint2.setColor(Color.TRANSPARENT);
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint2.setAntiAlias(true);
        tempCanvas.drawRect(rect, paint2);


        Paint paint1 = new Paint();
        paint1.setStrokeWidth(getContext().getResources().getDimension(R.dimen.cropview_padding_dim));
        paint1.setColor(Color.WHITE);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        tempCanvas.drawRect(rect, paint1);

        Paint paint3 = new Paint();
        paint3.setColor(Color.WHITE);
        paint3.setStyle(Paint.Style.FILL);
        tempCanvas.drawCircle(rect.left, rect.top, RADIUS, paint3);
        tempCanvas.drawCircle(rect.left, rect.bottom, RADIUS, paint3);
        tempCanvas.drawCircle(rect.right, rect.top, RADIUS, paint3);
        tempCanvas.drawCircle(rect.right, rect.bottom, RADIUS, paint3);

        if(m_nCropType == CROP_CUSTOM) {
            tempCanvas.drawCircle(rect.left, rect.top + rect.height() / 2, RADIUS, paint3);
            tempCanvas.drawCircle(rect.right, rect.top + rect.height() / 2, RADIUS, paint3);
            tempCanvas.drawCircle(rect.left + rect.width() / 2, rect.top, RADIUS, paint3);
            tempCanvas.drawCircle(rect.left + rect.width() / 2, rect.bottom, RADIUS, paint3);
        }

        for(int i=1; i<3; i++)
        {
            tempCanvas.drawLine(rect.left, rect.top + rect.height() / 3 * i, rect.right, rect.top + rect.height() / 3 * i, paint1);
            tempCanvas.drawLine(rect.left + rect.width() / 3 * i, rect.top, rect.left + rect.width() / 3 * i, rect.bottom, paint1);
        }

        String size = "" + toNumberFormat((int)(rect.width() / mParentScale)) + " X " +
                toNumberFormat((int)(rect.height()/mParentScale));
        paint1.setTextSize(24);
        tempCanvas.drawText(size, rect.left + rect.width() / 2-50, rect.top + rect.height() / 2, paint1);
        canvas.drawBitmap(tempBitmap, 0, 0, new Paint());
    }

    String toNumberFormat(int n) {
        String ret = "";
        int ll = n / 1000;
        int rr = n % 1000;
        if(ll !=0)
            ret += ll + " ";
        ret += (""+rr).toString();
        return ret;
    }

    final int RESIZE_NONE = 0;
    final int RESIZE_TL = 1;
    final int RESIZE_TR = 2;
    final int RESIZE_BR = 3;
    final int RESIZE_BL = 4;
    final int RESIZE_TM = 5;
    final int RESIZE_RM = 6;
    final int RESIZE_BM = 7;
    final int RESIZE_LM = 8;
    final int DRAG_TYPE_MOVE = 9;
    float sX;
    float sY;
    int dragType = RESIZE_NONE;
    float whScale;
    float mParentScale = (float)1.0;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int rad_scale = 2;
                if (fromTopLeft(touchX, touchY) <= RADIUS * rad_scale ) {
                    dragType = RESIZE_TL;
                } else if (fromTopRight(touchX, touchY) <= RADIUS * rad_scale) {
                    dragType = RESIZE_TR;
                } else if (fromBottomRight(touchX, touchY) <= RADIUS * rad_scale) {
                    dragType = RESIZE_BR;
                } else if (fromBottomLeft(touchX, touchY) <= RADIUS * rad_scale) {
                    dragType = RESIZE_BL;
                } else if (fromTopMid(touchX, touchY) <= RADIUS * rad_scale && m_nCropType == CROP_CUSTOM) {
                    dragType = RESIZE_TM;
                } else if (fromRightMid(touchX, touchY) <= RADIUS * rad_scale && m_nCropType == CROP_CUSTOM) {
                    dragType = RESIZE_RM;
                } else if (fromBottomMid(touchX, touchY) <= RADIUS * rad_scale && m_nCropType == CROP_CUSTOM) {
                    dragType = RESIZE_BM;
                } else if (fromLeftMid(touchX, touchY) <= RADIUS * rad_scale && m_nCropType == CROP_CUSTOM) {
                    dragType = RESIZE_LM;
                } else if (rect.contains(touchX, touchY)) {
                    dragType = DRAG_TYPE_MOVE;
                    sX = touchX;
                    sY = touchY;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (dragType == RESIZE_TL) {
                    if (touchX < rect0.left) {
                        touchX = rect0.left;
                    }
                    if (touchY < rect0.top) {
                        touchY = rect0.top;
                    }
                    float cx = rect.right - touchX;
                    float cy = rect.bottom - touchY;
                    if (cx > RADIUS * 3 && cy > RADIUS * 3) {
                        if (cx < whScale * cy) {
                            rect.left = touchX;
                            rect.top = rect.bottom - cx / whScale;
                        } else {
                            rect.top = touchY;
                            rect.left = rect.right - cy * whScale;
                        }
                    }
                } else if (dragType == RESIZE_TR) {
                    if (touchX > rect0.right) {
                        touchX = rect0.right;
                    }
                    if (touchY < rect0.top) {
                        touchY = rect0.top;
                    }
                    float cx =  touchX-rect.left;
                    float cy = rect.bottom - touchY;
                    if (cx > RADIUS * 3 && cy > RADIUS * 3) {
                        if (cx < whScale * cy) {
                            rect.right= touchX;
                            rect.top = rect.bottom - cx / whScale;
                        } else {
                            rect.top = touchY;
                            rect.right = rect.left +  cy * whScale;
                        }
                    }
                } else if (dragType == RESIZE_BR) {
                    if (touchX > rect0.right) {
                        touchX = rect0.right;
                    }
                    if (touchY > rect0.bottom) {
                        touchY = rect0.bottom;
                    }
                    float cx =  touchX-rect.left;
                    float cy = touchY-rect.top;
                    if (cx > RADIUS * 3 && cy > RADIUS * 3) {
                        if (cx < whScale * cy) {
                            rect.right= touchX;
                            rect.bottom = rect.top + cx / whScale;
                        } else {
                            rect.bottom = touchY;
                            rect.right = rect.left +  cy * whScale;
                        }
                    }
                } else if (dragType == RESIZE_BL) {
                    if (touchX < rect0.left) {
                        touchX = rect0.left;
                    }
                    if (touchY > rect0.bottom) {
                        touchY = rect0.bottom;
                    }
                    float cx =  rect.right-touchX;
                    float cy = touchY-rect.top;
                    if (cx > RADIUS * 3 && cy > RADIUS * 3) {
                        if (cx < whScale * cy) {
                            rect.left= touchX;
                            rect.bottom = rect.top + cx / whScale;
                        } else {
                            rect.bottom = touchY;
                            rect.left = rect.right -  cy * whScale;
                        }
                    }
                } else if (dragType == RESIZE_TM) {
                    if (touchY < rect0.top) {
                        touchY = rect0.top;
                    }
                    float cy = rect.bottom-touchY;
                    if (cy > RADIUS * 3) {
                        rect.top = touchY;
                        whScale = rect.width()/rect.height();
                    }
                } else if (dragType == RESIZE_BM) {
                    if (touchY > rect0.bottom) {
                        touchY = rect0.bottom;
                    }
                    float cy = touchY - rect.top;
                    if (cy > RADIUS * 3) {
                        rect.bottom = touchY;
                        whScale = rect.width()/rect.height();
                    }
                } else if (dragType == RESIZE_LM) {
                    if (touchX < rect0.left) {
                        touchX = rect0.left;
                    }
                    float cx =  rect.right-touchX;
                    if (cx > RADIUS * 3) {
                        rect.left= touchX;
                        whScale = rect.width()/rect.height();
                    }
                } else if (dragType == RESIZE_RM) {
                    if (touchX > rect0.right) {
                        touchX = rect0.right;
                    }
                    float cx =  touchX-rect.left;
                    if (cx > RADIUS * 3) {
                        rect.right= touchX;
                        whScale = rect.width()/rect.height();
                    }
                } else if (dragType == DRAG_TYPE_MOVE) {
                    float dx = touchX - sX;
                    float dy = touchY - sY;
                    if (rect.left+dx >= rect0.left && rect.right+dx <= rect0.right)  {
                        rect.left += dx;
                        rect.right += dx;
                        sX = touchX;
                    }
                    if( rect.top+dy>=rect0.top&&rect.bottom+dy<=rect0.bottom) {
                        rect.top += dy;
                        rect.bottom += dy;
                        sY = touchY;
                    }
                }
                break;
            case MotionEvent.ACTION_UP: {
                dragType = RESIZE_NONE;
                break;
            }
            default:
                return false;
        }
        invalidate();
        return true;
    }
    private float fromTopLeft(float x, float y) {
        x = x - rect.left;
        y = y - rect.top;
        return (float)Math.sqrt(x * x + y * y);
    }

    private float fromTopRight(float x, float y) {
        x = x - rect.right;
        y = y - rect.top;
        return (float)Math.sqrt(x * x + y * y);
    }

    private float fromBottomLeft(float x, float y) {
        x = x - rect.left;
        y = y - rect.bottom;
        return (float)Math.sqrt(x * x + y * y);
    }
    private float fromBottomRight(float x, float y) {
        x = x - rect.right;
        y = y - rect.bottom;
        return (float)Math.sqrt(x * x + y * y);
    }

    private float fromTopMid(float x, float y) {
        x = x - (rect.left + rect.width() / 2);
        y = y - rect.top;
        return (float) Math.sqrt(x * x + y * y);
    }
    private float fromBottomMid(float x, float y) {
        x = x - (rect.left + rect.width() / 2);
        y = y - rect.bottom;
        return (float) Math.sqrt(x * x + y * y);
    }

    private float fromLeftMid(float x, float y) {
        x = x - rect.left;
        y = y - (rect.top + rect.height() / 2);
        return (float) Math.sqrt(x * x + y * y);
    }

    private float fromRightMid(float x, float y) {
        x = x - rect.right;
        y = y - (rect.top + rect.height() / 2);
        return (float) Math.sqrt(x * x + y * y);
    }
}
