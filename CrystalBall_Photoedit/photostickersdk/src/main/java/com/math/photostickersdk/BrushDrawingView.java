package com.math.photostickersdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class BrushDrawingView extends View {

    private float brushSize = 10;
    private float brushEraserSize = 100;

    private DrawPathInfo pathInfo = new DrawPathInfo();
    private ArrayList<DrawPathInfo> m_listPathInfo = new ArrayList<DrawPathInfo>();

    //    private Path drawPath;
//    private Paint drawPaint;
    private Paint canvasPaint;

    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private boolean brushDrawMode;

    private OnPhotoStickerSDKListener onPhotoStickerSDKListener;

    public BrushDrawingView(Context context) {
        this(context, null);
    }

    public BrushDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupBrushDrawing();
    }

    public BrushDrawingView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setupBrushDrawing();
    }

    void setupBrushDrawing() {
//        drawPaint = new Paint();
        pathInfo.drawPaint.setAntiAlias(true);
        pathInfo.drawPaint.setDither(true);
        pathInfo.drawPaint.setColor(Color.BLACK);
        pathInfo.drawPaint.setStyle(Paint.Style.STROKE);
        pathInfo.drawPaint.setStrokeJoin(Paint.Join.ROUND);
        pathInfo.drawPaint.setStrokeCap(Paint.Cap.ROUND);
        pathInfo.drawPaint.setStrokeWidth(brushSize);
        pathInfo.drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        canvasPaint = new Paint(Paint.DITHER_FLAG);
        this.setVisibility(View.GONE);
    }

    private void refreshBrushDrawing() {
        brushDrawMode = true;
        pathInfo.drawPaint.setAntiAlias(true);
        pathInfo.drawPaint.setDither(true);
        pathInfo.drawPaint.setStyle(Paint.Style.STROKE);
        pathInfo.drawPaint.setStrokeJoin(Paint.Join.ROUND);
        pathInfo.drawPaint.setStrokeCap(Paint.Cap.ROUND);
        pathInfo.drawPaint.setStrokeWidth(brushSize);
//        Bitmap bm = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.vy);
//        BitmapShader fillBMPshader = new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
//        drawPaint.setShader(fillBMPshader);
        pathInfo.drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
    }

    void brushEraser() {
        pathInfo.drawPaint.setStrokeWidth(brushEraserSize);
        pathInfo.drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    void setBrushDrawingMode(boolean brushDrawMode) {
        this.brushDrawMode = brushDrawMode;
        if (brushDrawMode) {
            this.setVisibility(View.VISIBLE);
            refreshBrushDrawing();
        }
    }

    void setBrushSize(float size) {
        brushSize = size;
        refreshBrushDrawing();
    }

    void setBrushColor(@ColorInt int color) {
        pathInfo.drawPaint.setColor(color);
        refreshBrushDrawing();
    }

    void setBrushEraserSize(float brushEraserSize) {
        this.brushEraserSize = brushEraserSize;
    }

    void setBrushEraserColor(@ColorInt int color){
        pathInfo.BrushColor = color;
        pathInfo.drawPaint.setColor(color);
        refreshBrushDrawing();
    }

    void setBrushType(int nType) {
        pathInfo.BrushType = nType;
        pathInfo.brushBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.brush_type_2);
    }

    float getEraserSize() {
        return brushEraserSize;
    }

    float getBrushSize() {
        return brushSize;
    }

    int getBrushColor() {
        return pathInfo.drawPaint.getColor();
    }

    void clearAll() {
        if(drawCanvas!=null){
            drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            invalidate();
        }

    }

    public void setOnPhotoEditorSDKListener(OnPhotoStickerSDKListener onPhotoStickerSDKListener) {
        this.onPhotoStickerSDKListener = onPhotoStickerSDKListener;
    }

    private Rect mBounds = null;
    public void setBoundsRect(Rect rect){
        mBounds = rect;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mBounds!=null)
            canvas.clipRect(mBounds);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);

        if(pathInfo.BrushType == 0)
            canvas.drawPath(pathInfo.drawPath, pathInfo.drawPaint);
        else
            for(Vector2D pos:pathInfo.pathPoints)
                canvas.drawBitmap(pathInfo.brushBitmap, pos.x, pos.y, pathInfo.drawPaint);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (brushDrawMode) {
            float touchX = event.getX();
            float touchY = event.getY();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    pathInfo.drawPath.moveTo(touchX, touchY);
                    pathInfo.pathPoints.add(new Vector2D(touchX, touchY));
                    if (onPhotoStickerSDKListener != null)
                        onPhotoStickerSDKListener.onStartViewChangeListener(ViewType.BRUSH_DRAWING);
                    break;
                case MotionEvent.ACTION_MOVE:
                    pathInfo.drawPath.lineTo(touchX, touchY);
                    pathInfo.pathPoints.add(new Vector2D(touchX, touchY));
                    break;
                case MotionEvent.ACTION_UP:
                    drawCanvas.drawPath(pathInfo.drawPath, pathInfo.drawPaint);
                    pathInfo.pathPoints.add(new Vector2D(touchX, touchY));
                    m_listPathInfo.add(pathInfo);
                    pathInfo.reset();
                    if (onPhotoStickerSDKListener != null)
                        onPhotoStickerSDKListener.onStopViewChangeListener(ViewType.BRUSH_DRAWING);
                    break;
                default:
                    return false;
            }
            invalidate();
            return true;
        } else {
            return false;
        }
    }

    class DrawPathInfo {
        public Path drawPath = new Path();
        public ArrayList<Vector2D> pathPoints = new ArrayList<Vector2D>();
        public int BrushType = 0;
        public int BrushColor = Color.WHITE;
        public Paint drawPaint = new Paint();
        public Bitmap brushBitmap = null;
        public DrawPathInfo() {
        }

        public void reset() {
            drawPath.reset();
            pathPoints.clear();
        }
    }
}
