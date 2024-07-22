package com.samhung.crystalball.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.samhung.crystalball.crystalball_widgets.R;

public class PressableButton extends android.support.v7.widget.AppCompatImageView {

    public static interface OnPressedListener {
        public void OnPressed(View view);
        public void OnReleased(View view);
    }

    private OnPressedListener onPressedListener = null;
    public PressableButton(Context context) {
        this(context, null);
    }

    public PressableButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PressableButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
    }

    public void SetPressedListener(OnPressedListener onPressedListener){
        this.onPressedListener = onPressedListener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(onPressedListener!=null){
            if(event.getAction()==MotionEvent.ACTION_DOWN){
                onPressedListener.OnPressed(this);
            }
            if(event.getAction() == MotionEvent.ACTION_UP) {
                onPressedListener.OnReleased(this);
            }
        }

        return super.onTouchEvent(event);
    }
}
