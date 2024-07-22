package com.samhung.crystalball.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.SeekBar;

import com.samhung.crystalball.crystalball_widgets.R;

public class Slider extends View {

    private static final String TAG = Slider.class.getSimpleName();
    private static final int MAX = 100;
    private static final int MIN = 0;
    private static final int StateNormal = 0;
    private static final int StateDragging = 1;

    public static interface OnValueChangeListener {
        void onProgressChanged(Slider slider, int progress, boolean fromUser);
        void onStartTrackingTouch(Slider slider);
        void onStopTrackingTouch(Slider seekBar);
    }

    private Paint mPaint;
    private int mColor;
    private int mTintColor;
    private int mThumbRadius;
    private int mRippleRadius;
    private int mBarHeight;
    private int mMax;
    private int mProgress;
    private int mMiddle;
    private int mThumbBorderWidth;
    private RectF mUncoveredBarRectF = new RectF();
    private RectF mCoveredBarRectF = new RectF();
    private Point mThumbCenter = new Point();
    private Canvas mMinCanvas;
    private Paint mClearPaint;
    private PorterDuffXfermode mPorterDuffXFerMode;
    private float mCoordinateX;
    private int mState = StateNormal;
    private OnValueChangeListener mOnValueChangeListener;
    public void setOnValueChangeListener(OnValueChangeListener listener) {
        this.mOnValueChangeListener = listener;
    }

    public Slider(Context context) {
        this(context, null);
    }

    public Slider(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Slider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.Slider);
        mColor = attributes.getColor(R.styleable.Slider_slider_color,
                getResources().getColor(R.color.slider_color));
        mTintColor = attributes.getColor(R.styleable.Slider_slider_tint_color,
                getResources().getColor(R.color.slider_tint_color));
        mThumbRadius = attributes.getDimensionPixelSize(R.styleable.Slider_slider_thumb_radius,
                getResources().getDimensionPixelSize(R.dimen.slider_thumb_radius));
        mRippleRadius = attributes.getDimensionPixelSize(R.styleable.Slider_slider_ripple_radius,
                getResources().getDimensionPixelSize(R.dimen.slider_thumb_ripple_radius));
        mBarHeight = attributes.getDimensionPixelSize(R.styleable.Slider_slider_bar_height,
                getResources().getDimensionPixelSize(R.dimen.slider_bar_height));
        mThumbBorderWidth = attributes.getDimensionPixelSize(R.styleable.Slider_slider_thumb_border_width,
                getResources().getDimensionPixelSize(R.dimen.slider_thumb_border_width));
        mMax = attributes.getInteger(R.styleable.Slider_slider_max, MAX);
        mProgress = attributes.getInteger(R.styleable.Slider_slider_progress, MIN);
        mMiddle = attributes.getInteger(R.styleable.Slider_slider_middle_value, MIN);
        mCoordinateX = 0.f;

        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mClearPaint = new Paint();
        mClearPaint.setAntiAlias(true);

        mMinCanvas = new Canvas();
        mPorterDuffXFerMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    }

    public int getProgress() {return mProgress; }
    public void setProgress(int value) {
        mProgress = value;
        invalidate();
        if(mOnValueChangeListener!=null)
            mOnValueChangeListener.onProgressChanged(Slider.this, mProgress, false);
    }

    public void setMiddleValue(int value)
    {
        mMiddle = value;
        invalidate();
    }

    public int getMiddleValue(){
        return mMiddle;
    }
    public int getMax() {return mMax; }
    public void setMax(int value) {
        mMax = value;
        invalidate();
    }
    @Override
    protected int getSuggestedMinimumWidth() {
        return mRippleRadius * 4;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return mRippleRadius * 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec, true), measure(heightMeasureSpec, false));
    }

    private int measure(int measureSpec, boolean isWidth) {
        int result;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        int padding = isWidth ? getPaddingLeft() + getPaddingRight() : getPaddingTop() + getPaddingBottom();
        if (mode == MeasureSpec.EXACTLY) {
            result = size;
        } else {
            result = isWidth ? getSuggestedMinimumWidth() : getSuggestedMinimumHeight();
            result += padding;
            if (mode == MeasureSpec.AT_MOST) {
                if (isWidth) {
                    result = Math.max(result, size);
                } else {
                    result = Math.min(result, size);
                }
            }
        }
        return result;
    }

    boolean m_bStartTracking = false;
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
        ViewParent parent = getParent();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                if (event.getX() >= 0 && event.getX() <= getWidth() &&
                        event.getY() >= 0 && event.getY() <= getHeight()) {
                    m_bStartTracking = true;
                    mCoordinateX = getCoordinateX(event);
                    calculateProgress();
                    if(mOnValueChangeListener!=null)
                        mOnValueChangeListener.onStartTrackingTouch(Slider.this);
                }
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(true);
                }
                mState = StateDragging;
//                if (event.getX() >= 0 && event.getX() <= getWidth())
//                        &&
//                        event.getY() >= 0 && event.getY() <= getHeight())
                if(m_bStartTracking)
                {
                    mCoordinateX = getCoordinateX(event);
                    calculateProgress();
                    if(mOnValueChangeListener!=null)
                        mOnValueChangeListener.onProgressChanged(Slider.this, mProgress, true);
                    invalidate();
                }
            }
            break;
            case MotionEvent.ACTION_UP: {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                m_bStartTracking = false;
                mCoordinateX = getCoordinateX(event);
                calculateProgress();
                if(mOnValueChangeListener!=null)
                    mOnValueChangeListener.onStopTrackingTouch(Slider.this);
                mState = StateNormal;
                invalidate();
            }
            break;
            case MotionEvent.ACTION_CANCEL: {
                if (parent != null) {
                    parent.requestDisallowInterceptTouchEvent(false);
                }
                mCoordinateX = getCoordinateX(event);
                calculateProgress();
                if(mOnValueChangeListener!=null)
                    mOnValueChangeListener.onStopTrackingTouch(Slider.this);
                mState = StateNormal;
                invalidate();
            }
            break;
        }
        return true;
    }

    private float getCoordinateX(MotionEvent event) {
//        return ((getWidth() - getPaddingLeft() - getPaddingRight()) * event.getX()) / getWidth();
        if(event.getX() < getPaddingLeft())
            return 0;
        else if(event.getX() > getWidth()-getPaddingRight())
            return getWidth()-getPaddingLeft()-getPaddingRight();
        else
            return event.getX()-getPaddingLeft();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        RectF barRectF = getBarRect();
        switch (mState) {
            case StateNormal: {
                calculateThumbCenterPoint();
                if (mProgress == MIN) {
                    mPaint.setColor(mColor);
                    canvas.drawRect(barRectF, mPaint);

                    mPaint.setColor(mTintColor);
                    canvas.drawRect(getMinCoveredBarRect(), mPaint);

                    mPaint.setColor(mTintColor);
                    canvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius, mPaint);

                    mClearPaint.setColor(Color.WHITE);
                    canvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius - mThumbBorderWidth, mClearPaint);

                } else if (mProgress == mMax) {
                    mPaint.setColor(mColor);
                    canvas.drawRect(barRectF, mPaint);

                    mPaint.setColor(mTintColor);
                    canvas.drawRect(getMaxCoveredBarRect(), mPaint);

                    mPaint.setColor(mTintColor);
                    canvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius, mPaint);

                    mClearPaint.setColor(Color.WHITE);
//                    mClearPaint.setXfermode(mPorterDuffXFerMode);
                    canvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius - mThumbBorderWidth, mClearPaint);
                } else {
                    mPaint.setColor(mColor);
                    canvas.drawRect(barRectF, mPaint);

                    mPaint.setColor(mTintColor);
                    canvas.drawRect(getCoveredRectF(mProgress), mPaint);

                    mThumbCenter.set(getThumbCenterX(mProgress), getHeight() / 2);
                    mPaint.setColor(mTintColor);
                    canvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius, mPaint);

                    mClearPaint.setColor(Color.WHITE);
                    canvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius - mThumbBorderWidth, mClearPaint);
                }
            }
            break;
            case StateDragging: {
                mUncoveredBarRectF.left = getPaddingLeft() + mThumbRadius;
                mUncoveredBarRectF.right = getWidth() - getPaddingRight() - mThumbRadius;
                mUncoveredBarRectF.top = getHeight() / 2.0f + -mBarHeight / 2.0f;
                mUncoveredBarRectF.bottom = getHeight() / 2.0f + mBarHeight / 2.0f;

                float realX = getThumbCenterX(mCoordinateX);

                mPaint.setColor(mColor);
                canvas.drawRect(barRectF, mPaint);

                mPaint.setColor(mTintColor);
                canvas.drawRect(getCoveredRectF(realX), mPaint);

                mPaint.setColor(mTintColor);

                mThumbCenter.set((int) realX, getHeight() / 2);
                canvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius, mPaint);
            }
            break;
        }

        /*
        canvas.save();
        canvas.getClipBounds(mCanvasRect);
        mCanvasRect.inset(-mRippleRadius, -mRippleRadius);
        canvas.clipRect(mCanvasRect, Region.Op.REPLACE);

        canvas.restore();

        calculateBarDrawRect();

        calculateThumbCenterPoint();

        if (mProgress == MIN) {
            mMinBitmap = getMinBitmap(canvas);
            mMinCanvas.setBitmap(mMinBitmap);

            mPaint.setColor(mColor);
            mMinCanvas.drawRect(mUncoveredBarRectF, mPaint);

            mPaint.setColor(mTintColor);
            mMinCanvas.drawRect(mCoveredBarRectF, mPaint);

            mPaint.setColor(mTintColor);
            mMinCanvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius, mPaint);

            mClearPaint.setColor(Color.TRANSPARENT);
            mClearPaint.setXfermode(mPorterDuffXFerMode);
            mMinCanvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius - mThumbBorderWidth, mClearPaint);

            canvas.drawBitmap(mMinBitmap, 0, 0, null);
        } else if (mProgress == MAX) {
            mPaint.setColor(mColor);
            canvas.drawRect(mUncoveredBarRectF, mPaint);

            mPaint.setColor(mTintColor);
            canvas.drawRect(mCoveredBarRectF, mPaint);

            mPaint.setColor(mTintColor);
            canvas.drawCircle(mThumbCenter.x, mThumbCenter.y, mThumbRadius, mPaint);
        } else {

        }
        */
    }

    private void calculateProgress() {
        float width = getMaxThumbCenterX() - getMinThumbCenterX();
        float passed = getThumbCenterX(mCoordinateX) - getMinThumbCenterX();
        mProgress = Math.round(passed * mMax / width);
    }

    private float getThumbCenterX(float x) {
//        if (x < getPaddingLeft() + mThumbRadius) {
//            return getMinThumbCenterX();
//        } else if (x > getWidth() - getPaddingRight() - mThumbRadius) {
//            return getMaxThumbCenterX();
//        } else {
//            int width = getWidth() - getPaddingLeft() - getPaddingRight() - mThumbRadius * 2;
//            return mThumbRadius + getPaddingLeft() + x * width / getWidth();
//        }
        if(x<mThumbRadius)
            return getMinThumbCenterX();
        else if(x>getWidth()-getPaddingLeft()-getPaddingRight()-mThumbRadius)
            return getMaxThumbCenterX();
        else {
            return getPaddingLeft() + x;
        }
    }

    private int getThumbCenterX(int progress) {
        if (progress == MIN) {
            return getPaddingLeft() + mThumbRadius;
        } else if (progress == mMax) {
            return getWidth() - getPaddingRight() - mThumbRadius;
        } else {
            float width = getMaxThumbCenterX() - getMinThumbCenterX();
            float passed = progress * width / mMax;
            return Math.round(getPaddingLeft() + mThumbRadius + passed);
        }
    }

    private Bitmap getMinBitmap(Canvas canvas) {
        return Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
    }

    private float getMinThumbCenterX() {
        return getPaddingLeft() + mThumbRadius;
    }

    private float getMaxThumbCenterX() {
        return getWidth() - getPaddingRight() - mThumbRadius;
    }

    private RectF getMinCoveredBarRect() {
        if (mCoveredBarRectF == null) {
            mCoveredBarRectF = new RectF();
        }
        mCoveredBarRectF.left = getPaddingLeft() + mThumbRadius;
        mCoveredBarRectF.right = getThumbCenterX(mMiddle);
//        mCoveredBarRectF.right = getPaddingLeft() + mThumbRadius;
        mCoveredBarRectF.top = getHeight() / 2.0f + -mBarHeight / 2.0f;
        mCoveredBarRectF.bottom = getHeight() / 2.0f + mBarHeight / 2.0f;
        if(mCoveredBarRectF.right <= mCoveredBarRectF.left)
            mCoveredBarRectF.right = mCoveredBarRectF.left+1;
        return mCoveredBarRectF;
    }

    private RectF getMaxCoveredBarRect() {
        if (mCoveredBarRectF == null) {
            mCoveredBarRectF = new RectF();
        }
        mCoveredBarRectF.left = getThumbCenterX(mMiddle);
//        mCoveredBarRectF.left = getPaddingLeft() + mThumbRadius;
        mCoveredBarRectF.right = getWidth() - getPaddingRight() - mThumbRadius;
        mCoveredBarRectF.top = getHeight() / 2.0f + -mBarHeight / 2.0f;
        mCoveredBarRectF.bottom = getHeight() / 2.0f + mBarHeight / 2.0f;
        if(mCoveredBarRectF.left >=mCoveredBarRectF.right)
            mCoveredBarRectF.left = mCoveredBarRectF.right-1;
        return mCoveredBarRectF;
    }

    private RectF getBarRect() {
        if (mUncoveredBarRectF == null) {
            mUncoveredBarRectF = new RectF();
        }
        mUncoveredBarRectF.left = getPaddingLeft() + mThumbRadius;
        mUncoveredBarRectF.right = getWidth() - getPaddingRight() - mThumbRadius;
        mUncoveredBarRectF.top = getHeight() / 2.0f + -mBarHeight / 2.0f;
        mUncoveredBarRectF.bottom = getHeight() / 2.0f + mBarHeight / 2.0f;
        return mUncoveredBarRectF;
    }

    private RectF getCoveredRectF(float x) {
        if (mCoveredBarRectF == null) {
            mCoveredBarRectF = new RectF();
        }

        float midPos = getThumbCenterX(mMiddle);
        if(x >= midPos) {
            mCoveredBarRectF.left = midPos;
            mCoveredBarRectF.right = x;
            if(x==midPos)
                mCoveredBarRectF.right = midPos;
        }else {
            mCoveredBarRectF.left = x;
            mCoveredBarRectF.right = midPos;
        }
//        mCoveredBarRectF.left = getPaddingLeft() + mThumbRadius;
//        mCoveredBarRectF.right = x;
        mCoveredBarRectF.top = getHeight() / 2.0f + -mBarHeight / 2.0f;
        mCoveredBarRectF.bottom = getHeight() / 2.0f + mBarHeight / 2.0f;
        return mCoveredBarRectF;
    }

    private RectF getCoveredRectF(int progress) {
        if (mCoveredBarRectF == null) {
            mCoveredBarRectF = new RectF();
        }
        if(progress >= mMiddle) {
            mCoveredBarRectF.left = getThumbCenterX(mMiddle);
            mCoveredBarRectF.right = getThumbCenterX(progress);
            if(progress == mMiddle) {
                mCoveredBarRectF.right = mCoveredBarRectF.left +1;
                }
        } else {
            mCoveredBarRectF.left = getThumbCenterX(progress);
            mCoveredBarRectF.right = getThumbCenterX(mMiddle);
            if(mCoveredBarRectF.left >=mCoveredBarRectF.right)
                mCoveredBarRectF.left = mCoveredBarRectF.right - 1;
        }
//        mCoveredBarRectF.left = getPaddingLeft() + mThumbRadius;
//        mCoveredBarRectF.right = getThumbCenterX(progress);
        mCoveredBarRectF.top = getHeight() / 2.0f + -mBarHeight / 2.0f;
        mCoveredBarRectF.bottom = getHeight() / 2.0f + mBarHeight / 2.0f;
        return mCoveredBarRectF;
    }

    private void calculateThumbCenterPoint() {
        if (mProgress == MIN) {
            mThumbCenter.set(getPaddingLeft()+mThumbRadius, getHeight() / 2);
        } else if (mProgress == mMax) {
            mThumbCenter.set(getWidth() - getPaddingRight() - mThumbRadius, getHeight() / 2);
        }
    }
}
