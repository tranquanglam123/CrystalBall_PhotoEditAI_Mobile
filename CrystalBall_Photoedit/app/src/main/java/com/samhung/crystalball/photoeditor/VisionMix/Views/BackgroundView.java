package com.samhung.crystalball.photoeditor.VisionMix.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.samhung.crystalball.photoeditor.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class BackgroundView extends SurfaceView implements SurfaceHolder.Callback{
    public static final float BRUSH_MIN = 1;
    public static final int BRUSH_MAX = 100;

    private float eraserSize = 50;

    private Path drawPath;
    private Paint drawPaint;
    private Paint canvasPaint;
    private Paint copyingPaint;
    private Paint pointPaint;
    private Paint backPaint;

    private Canvas tempCanvas;
    public  Bitmap tempBitmap;
    private Bitmap bitmapSource;

    private Xfermode xferModeEraser = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    private float xOff;
    private float yOff;
    private float scale0 = 1;
    private float scale = 1;

    private float deltaAngle = 0;
    private float centerX = 0;
    private float centerY = 0;

    private int wDst;
    private int hDst;

    ArrayList<StrokeInfo> pathList = new ArrayList<>();

    public class StrokeInfo {
        public float strokeWidth;
        public int mode;
        public float[] matrix_values = new float[9];
        public ArrayList<PointF> points = new ArrayList<>();
//        public Path path = new Path();

        public StrokeInfo() {
        }

        public Xfermode xferMode() {
            return xferModeEraser;
        }
        public String toString() {
            JSONObject j = toJSON();
            if (j == null) return null;
            return j.toString();
        }
        public JSONObject toJSON() {
            JSONObject jsonObj = new JSONObject();
            try {
                jsonObj.put("strokeWidth", strokeWidth);
                jsonObj.put("mode", mode);
                JSONArray jsonMat = new JSONArray();
                for (int i = 0; i < 9; i ++) {
                    jsonMat.put(i,  matrix_values[i]);
                }
                jsonObj.put("matrix", jsonMat);
                JSONArray jsonPoints = new JSONArray();
                for (int i = 0; i < points.size(); i ++) {
                    JSONObject p = new JSONObject();
                    p.put("x", points.get(i).x);
                    p.put("y", points.get(i).y);
                    jsonPoints.put(i, p);
                }
                jsonObj.put("points", jsonPoints);
                return jsonObj;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public BackgroundView(Context context) {
        this(context, null);
    }

    public BackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setupBrushDrawing();

    }

    public BackgroundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
//        super(context, attrs, defStyle);
        setupBrushDrawing();

    }

    void setupBrushDrawing() {
        drawPath = new Path();

        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setColor(Color.BLACK);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setXfermode(xferModeEraser);
        drawPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

        canvasPaint = new Paint();  //Paint.DITHER_FLAG
        copyingPaint = new Paint();

        pointPaint = new Paint();
        pointPaint.setColor(Color.WHITE);
        pointPaint.setAlpha(0x80);
        pointPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        backPaint = new Paint();
        backPaint.setAntiAlias(true);
        backPaint.setStyle(Paint.Style.FILL);
        Bitmap back_tile = BitmapFactory.decodeResource(getResources(), R.drawable.checks_light);
        BitmapShader bmShader = new BitmapShader(back_tile, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
        backPaint.setShader(bmShader);

//        mThread = new DrawingThread();
        setZOrderOnTop(false);
//        setZOrderOnTop(true);
//        setZOrderMediaOverlay(true);


    }

    public void setBitmap(Bitmap bm) {
        bitmapSource = bm;
        wDst = bitmapSource.getWidth();
        hDst = bitmapSource.getHeight();
    }

    public void setEraserSize(float brushEraserSize) {
        this.eraserSize = brushEraserSize;
    }

    public float getEraserSize() {
        return eraserSize;
    }

    public void clearAll() {
//        for (StrokeInfo si:pathList) {
//            si.path.reset();
//        }
        pathList.clear();
        if (tempCanvas != null) {
            tempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
//            invalidate();
            RefreshView();
        }
    }

    public void undo() {
        if (pathList.size() <= 0) {
            return;
        }
        pathList.remove(pathList.size() - 1);
//        invalidate();
        RefreshView();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        tempBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);
    }
    public void setDestCacheSize(int w, int h) {
        wDst = w;
        hDst = h;
    }

    public void initDrawingPos(float scale, int x, int y) {
        this.scale0 = scale;
        this.scale = scale;
        xOff = x;
        yOff = y;
    }

    private Matrix main_matrix = new Matrix();
    public void setMatrix(Matrix matrix)
    {
        drawPath.reset();
        main_matrix.set(matrix);
        float[] x = new float[9];
        main_matrix.getValues(x);

//        scale = x[Matrix.MSCALE_X];
        xOff = x[Matrix.MTRANS_X];
        yOff = x[Matrix.MTRANS_Y];

        // calculate real scale
        float scalex = x[Matrix.MSCALE_X];
        float skewy = x[Matrix.MSKEW_Y];
        float rScale = (float) Math.sqrt(scalex * scalex + skewy * skewy);
        scale = rScale;
// calculate the degree of rotation
        float rAngle = (float)(Math.atan2(x[Matrix.MSKEW_X], x[Matrix.MSCALE_X]) * (180 / Math.PI));
        deltaAngle = rAngle;
//        invalidate();

        RefreshView();
    }

    public void setRotateValues(float angle, float x, float y)
    {
     //   deltaAngle = angle;
        centerX = x;
        centerY = y;
    }

    public Matrix getMatrix()
    {
        return main_matrix;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        draw_my(canvas);
    }

    //@Override
    public void draw_my(Canvas canvas) {
   //     tempBitmap.eraseColor(Color.argb(0xff, 230,229,229));
   //     canvas.drawColor( Color.argb(0xff, 230,229,229), PorterDuff.Mode.CLEAR );
        tempBitmap.eraseColor(Color.TRANSPARENT);
        //canvas.drawColor(Color.TRANSPARENT);
        canvas.drawRect(0,0,tempBitmap.getWidth(), tempBitmap.getHeight(), backPaint);
        if(bitmapSource != null)
            tempCanvas.drawBitmap(bitmapSource, main_matrix, canvasPaint);
        else
            tempCanvas.drawColor(Color.TRANSPARENT);
//            tempCanvas.drawColor(Color.argb(0xff, 230,229,229));
        canvas.drawBitmap(tempBitmap, 0, 0, copyingPaint);
    }

//    public void draw_my(Canvas canvas) {
//        tempBitmap.eraseColor(Color.argb(0xff, 230,229,229));
//        canvas.drawColor( Color.argb(0xff, 230,229,229), PorterDuff.Mode.CLEAR );
//        if(bitmapSource != null)
//            tempCanvas.drawBitmap(bitmapSource, main_matrix, canvasPaint);
//        else
//            tempCanvas.drawColor(Color.argb(0xff, 230,229,229));
//        canvas.drawBitmap(tempBitmap, 0, 0, copyingPaint);
//    }
    public void RefreshView(){
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if(canvas == null) return;
        synchronized (mSurfaceHolder) {
            draw_my(canvas);
        }
        mSurfaceHolder.unlockCanvasAndPost(canvas);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        tempCanvas = new Canvas(tempBitmap);
        RefreshView();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        mThread.keepRunning = true;
//        if(!mThread.isAlive())
//            mThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        mThread.keepRunning = false;
//        boolean retry = true;
//        while (retry) {
//            try {
//                mThread.join();
//                retry = false;
//            } catch (InterruptedException e) {}
//        }
    }

//    DrawingThread mThread;
    SurfaceHolder mSurfaceHolder;
//    private class DrawingThread extends Thread {
//        boolean keepRunning = true;
//
//        @Override
//        public void run() {
//            Canvas c;
//            while (keepRunning) {
//                c = null;
//
//                try {
//                    c = mSurfaceHolder.lockCanvas();
//                    synchronized (mSurfaceHolder) {
////                        onDraw(c);
//                        draw(c);
//
//                    }
//                } finally {
//                    if (c != null)
//                        mSurfaceHolder.unlockCanvasAndPost(c);
//                }
//
//                // Run the draw loop at 50FPS
//                try {
//                    Thread.sleep(40);
//                } catch (InterruptedException e) {}
//            }
//        }
//    }
}
