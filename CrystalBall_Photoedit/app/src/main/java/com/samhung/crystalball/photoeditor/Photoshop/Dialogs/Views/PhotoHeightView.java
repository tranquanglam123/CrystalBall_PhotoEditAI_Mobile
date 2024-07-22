package com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoHeight;
import com.samhung.crystalball.photoeditor.R;

import java.util.ArrayList;

public class PhotoHeightView extends View {

    private Paint copyingPaint;

    private Canvas tempCanvas;
    private Bitmap tempBitmap;

    private Bitmap bmCursor1 = null;
    private Bitmap bmCursor2 = null;
    private Bitmap bmCursorUP = null;
    private Bitmap bmCursorDOWN= null;

    Dialog_PhotoHeight parent = null;

    public PhotoHeightView(Context context) {
        this(context, null);
    }

    public PhotoHeightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupBrushDrawing();
    }

    public PhotoHeightView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupBrushDrawing();
    }

    void setupBrushDrawing() {
        copyingPaint = new Paint();
        copyingPaint.setAlpha(0xAA);
        bmCursorUP = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.height_cursor_up);
        bmCursorDOWN = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.height_cursor_dwn);
        bmCursor1 = bmCursorUP;
        bmCursor2 = bmCursorUP;

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    int dx = 0;
    int dy=0;
    int dw = 100;
    int dh = 100;
    int px = 0;
    int py=0;
    int pr = 100;
    int pb = 100;

    public void setParent(Dialog_PhotoHeight p) {
        parent = p;
    }
    RectF mStretchRect = new RectF();
    public void setDestCacheSize(int pLeft, int pTop, int pRight, int pBottom) {
        px = pLeft; py = pTop; pr = pRight; pb = pBottom;
        tempBitmap = Bitmap.createBitmap(pRight-pLeft, pBottom - pTop,  Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);
    }

    public void setDrawCacheSize(int x,int y,int w, int h)
    {
        dx = x; dy = y; dw = w; dh=h;
    }

    Rect cursorRect1 = new Rect(0,0,30,30);
    Rect cursorRect2 = new Rect(0,100,30,30);
    public void initStretchRect()
    {
        mStretchRect.left = dx;
        mStretchRect.right = dx +dw;
        mStretchRect.top = dy + dh / 2 - dh/4;
        mStretchRect.bottom = dy + dh / 2 + dh/4;

        int cursorWidth = 60;
        cursorRect1.left = cursorRect2.left = (int)mStretchRect.right - cursorWidth;
        cursorRect1.right = cursorRect2.right = (int)mStretchRect.right;
        cursorRect1.top = (int)mStretchRect.top- (int)(cursorWidth/2);
        cursorRect1.bottom = cursorRect1.top + cursorWidth;
        cursorRect2.bottom = (int)mStretchRect.bottom +  (int)(cursorWidth/2);
        cursorRect2.top = cursorRect2.bottom - cursorWidth;
        invalidate();
    }

    public void resizeStretchRect(int top, int height)
    {
        mStretchRect.top = dy + top;
        mStretchRect.bottom = dy +top+height;
        int cursorWidth = 60;
        cursorRect1.left = cursorRect2.left = (int)mStretchRect.right - cursorWidth;
        cursorRect1.right = cursorRect2.right = (int)mStretchRect.right;
        cursorRect1.top = (int)mStretchRect.top- (int)(cursorWidth/2);
        cursorRect1.bottom = cursorRect1.top + cursorWidth;
        cursorRect2.bottom = (int)mStretchRect.bottom +  (int)(cursorWidth/2);
        cursorRect2.top = cursorRect2.bottom - cursorWidth;
        invalidate();
    }

    public RectF getStretchArea() {
        return mStretchRect;
    }
    void adjustStretchRect()
    {
        int cy1 = cursorRect1.centerY();
        int cy2 = cursorRect2.centerY();
        mStretchRect.top = cy1 > cy2 ? cy2 : cy1;
        mStretchRect.bottom = cy1 < cy2 ? cy2 : cy1;
        if(mStretchRect.top == mStretchRect.bottom) {
            mStretchRect.bottom +=1;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(tempBitmap == null) return;

        tempCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        Paint paint1 = new Paint();
        paint1.setStrokeWidth(1);
        paint1.setColor(Color.WHITE);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        tempCanvas.drawRect(mStretchRect, paint1);

        tempCanvas.drawRect(new Rect(dx,dy,dx+dw, dy+dh), paint1);
        Paint paint2 = new Paint();
        paint2.setStyle(Paint.Style.FILL);
        paint2.setColor(Color.parseColor("#CCFF0000"));
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        paint2.setAntiAlias(true);
        tempCanvas.drawRect(mStretchRect, paint2);

        tempCanvas.drawBitmap(bmCursor1, new Rect(0,0,bmCursor1.getWidth(), bmCursor1.getHeight()), cursorRect1, new Paint());
        tempCanvas.drawBitmap(bmCursor2, new Rect(0,0,bmCursor1.getWidth(), bmCursor1.getHeight()), cursorRect2, new Paint());

        canvas.drawBitmap(tempBitmap, 0, 0, copyingPaint);
        super.onDraw(canvas);

    }

    final int CURSOR_1 = 10;
    final int CURSOR_2 = 20;
    Point ptStart = new Point();
    int m_TouchMode = BaseActivity.NONE;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch(event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
                ptStart.x = (int)event.getX();
                ptStart.y = (int)event.getY();
                m_TouchMode = BaseActivity.NONE;
                if( cursorRect1.contains(ptStart.x, ptStart.y) ){
                    bmCursor1 = bmCursorDOWN;
                    m_TouchMode = CURSOR_1;
                } else if(cursorRect2.contains(ptStart.x, ptStart.y)) {
                    m_TouchMode = CURSOR_2;
                    bmCursor2 = bmCursorDOWN;
                } else
                    return false;
                parent.ResetSeekBar();
                break;
            case MotionEvent.ACTION_MOVE:
                if(m_TouchMode == BaseActivity.NONE) return false;
                if(event.getY()-1 <= dy || event.getY()-1 >= dy + dh ) return false;

                if(m_TouchMode == CURSOR_1) {
                    cursorRect1.offsetTo(cursorRect1.left, (int)event.getY() - cursorRect1.height()/2);
                } else if(m_TouchMode == CURSOR_2) {
                    cursorRect2.offsetTo(cursorRect2.left, (int)event.getY() - cursorRect1.height()/2);
                }
                adjustStretchRect();

                break;
            case MotionEvent.ACTION_UP:
                if(m_TouchMode != BaseActivity.NONE)
                {
                    m_TouchMode = BaseActivity.NONE;
                    bmCursor1 = bmCursorUP;
                    bmCursor2 = bmCursorUP;


                }

                break;
        }
        invalidate();
        return true;
    }
}
