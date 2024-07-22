package com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.samhung.crystalball.photoeditor.R;

public class PhotoRotateView extends View {
    private Canvas tempCanvas;
    private Bitmap tempBitmap;

    public PhotoRotateView(Context context) {
        this(context, null);
    }

    public PhotoRotateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoRotateView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    RectF rect = new RectF(30, 30, 100, 100);
    RectF rect0 = new RectF();
    RectF original_rect = new RectF();
    RectF rotateRect = new RectF();

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
    }

    public void setRotatedRect(RectF rt) {
        rt.offset(rect.left + (rect.width() - rt.width()) / 2, rect.top + (rect.height() - rt.height()) / 2);
        rotateRect.set(rt);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        tempBitmap.eraseColor(Color.TRANSPARENT);
        tempCanvas.drawColor(Color.parseColor("#AA000000"));

        Paint paint2 = new Paint();
        paint2.setStyle(Paint.Style.FILL);
        paint2.setColor(Color.parseColor("#AA000000"));
        paint2.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        paint2.setAntiAlias(true);
        tempCanvas.drawRect(rotateRect, paint2);
       // tempCanvas.drawRect(original_rect, paint2);


        Paint paint1 = new Paint();
        paint1.setStrokeWidth(getContext().getResources().getDimension(R.dimen.cropview_padding_dim));
        paint1.setColor(Color.WHITE);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setAntiAlias(true);
        tempCanvas.drawRect(rotateRect, paint1);

//        Paint paint3 = new Paint();
//        paint3.setStyle(Paint.Style.FILL);
//        paint3.setColor(Color.TRANSPARENT);
//        paint3.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
//        paint3.setAntiAlias(true);
//        tempCanvas.drawRect(rotateRect, paint3);
        for(int i=1; i<3; i++)
        {
            tempCanvas.drawLine(rotateRect.left, rotateRect.top + rotateRect.height() / 3 * i, rotateRect.right, rotateRect.top + rotateRect.height() / 3 * i, paint1);
            tempCanvas.drawLine(rotateRect.left + rotateRect.width() / 3 * i, rotateRect.top, rotateRect.left + rotateRect.width() / 3 * i, rotateRect.bottom, paint1);
        }
        canvas.drawBitmap(tempBitmap, 0, 0, new Paint());
    }
}
