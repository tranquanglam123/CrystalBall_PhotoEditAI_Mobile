package com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
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

public class BrushDrawingView extends View {

    public static final float BRUSH_MIN = 1;
    public static final float BRUSH_MAX = 100;

    public static final int DRAWING_MODE_LIGHT = 0;
    public static final int DRAWING_MODE_HARD = 1;
    public static final int ERASING_MODE = 2;

    private int mode = DRAWING_MODE_HARD;
    private float brushSize = BRUSH_MAX / 2;
  //  private float eraserSize = BRUSH_MAX;

    private Path drawPath;
    private Paint drawPaint;
    private Paint canvasPaint;
    private Paint copyingPaint;
    private Paint pointPaint;

    private Canvas tempCanvas;
    public Bitmap tempBitmap=null;

    private Bitmap bitmapMask;

    private Xfermode xferModeBrush = new PorterDuffXfermode(PorterDuff.Mode.SRC);
    private Xfermode xferModeEraser = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
    private Xfermode curXfermode = xferModeBrush;
    private int hardColor = Color.argb(0xff, 0, 0, 0);
    private int lightColor = Color.argb(0x80, 0, 0, 0);
    private int cur_Color = hardColor;

    private float xOff;
    private float yOff;
    private float scale = 1;

    private int wDst;
    private int hDst;

    private int parentLeft = 0;
    private int parentTop = 0;
    private int parentRight = 0;
    private int parentBottom = 0;

    private Matrix matrix0=new Matrix();

    private boolean m_bEdited = true;

    public class StrokeInfo {
        public float strokeWidth;
        public int mode;
        public float[] matrix = new float[9];
        public ArrayList<PointF> points = new ArrayList<>();
//        public Path path = new Path();

        public StrokeInfo() {
        }

        public int PaintColor() {
            if(this.mode == DRAWING_MODE_HARD) return hardColor;
            else if(this.mode == DRAWING_MODE_LIGHT) return lightColor;
            else return hardColor;
        }

        public Xfermode xferMode() {
            return (this.mode != ERASING_MODE) ? xferModeBrush : xferModeEraser;
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
                    jsonMat.put(i,  matrix[i]);
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

    StrokeInfo strokeInfoFromJSON(JSONObject jsonObject) {
        StrokeInfo si = new StrokeInfo();
        try {
            si.strokeWidth = (float)jsonObject.getDouble("strokeWidth");
            si.mode = jsonObject.getInt("mode");
            JSONArray jsonMat = jsonObject.getJSONArray("matrix");
            for (int i = 0; i < 9; i ++) {
                si.matrix[i] = (float)jsonMat.getDouble(i);
            }
            JSONArray jps = jsonObject.getJSONArray("points");
            for (int i = 0; i < jps.length(); i ++) {
                JSONObject jp = jps.getJSONObject(i);
                si.points.add(new PointF((float)jp.getDouble("x"), (float)jp.getDouble("y")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return si;
    }

    public String toString() {
        JSONArray jsonArray = new JSONArray();

        for (int i = 0; i < pathList.size(); i ++) {
            StrokeInfo si = pathList.get(i);
            try {
                jsonArray.put(i, si.toJSON());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray.toString();
    }
    public void fromString(String str) {
        try {
            JSONArray jsonArray = new JSONArray(str);
            for (int i = 0; i < jsonArray.length(); i ++) {
                pathList.add(strokeInfoFromJSON(jsonArray.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    ArrayList<StrokeInfo> pathList = new ArrayList<>();

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
        drawPath = new Path();

        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setDither(true);
        drawPaint.setColor(Color.argb(0x80, 0, 0, 0));//Color.BLACK);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setXfermode(xferModeBrush);
        //drawPaint.setMaskFilter(new BlurMaskFilter(8, BlurMaskFilter.Blur.NORMAL));

        canvasPaint = new Paint(Paint.DITHER_FLAG);
        copyingPaint = new Paint();
//        copyingPaint.setAlpha(0xAA);
        copyingPaint.setColor(Color.argb(0x90, 255, 90, 90));

        pointPaint = new Paint();
        pointPaint.setColor(Color.WHITE);
        pointPaint.setAlpha(0x80);
        pointPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
    }

    public void setMode(int mode) {
        if (mode == DRAWING_MODE_LIGHT) {
            this.mode = mode;
            curXfermode = xferModeBrush;
            cur_Color = lightColor;
        }else if (mode == DRAWING_MODE_HARD){
            this.mode = mode;
            curXfermode = xferModeBrush;
            cur_Color = hardColor;
        } else {
            this.mode = mode;
            curXfermode = xferModeEraser;
        }
    }

    public boolean isMaskEdited() {
        return m_bEdited;
    }

    public void setBitmapMask(Bitmap mask) {
        bitmapMask = mask;
    }

    public void setBrushSize(float size) {
        curXfermode = xferModeBrush;
        brushSize = size;
    }

    public float getBrushSize() {
        return brushSize;
    }


    public void clearAll() {
//        for (StrokeInfo si:pathList) {
//            si.path.reset();
//        }
        pathList.clear();
        if (tempCanvas != null) {
            tempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            invalidate();
        }

        m_bEdited = true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
       // tempBitmap = Bitmap.createBitmap(w, h,  Bitmap.Config.ARGB_8888);
       // tempCanvas = new Canvas(tempBitmap);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
//        tempBitmap = Bitmap.createBitmap(right-left, bottom - top,  Bitmap.Config.ARGB_8888);
//        tempCanvas = new Canvas(tempBitmap);
    }

    public void setDestCacheSize(int w, int h, int pLeft, int pTop, int pRight, int pBottom) {
        wDst = w;
        hDst = h;
        parentLeft = pLeft;
        parentRight = pRight;
        parentTop = pTop;
        parentBottom = pBottom;
        tempBitmap = Bitmap.createBitmap(parentRight-parentLeft, parentBottom - parentTop,  Bitmap.Config.ALPHA_8);
//        tempBitmap = Bitmap.createBitmap(parentRight-parentLeft, parentBottom - parentTop,  Bitmap.Config.ALPHA_8);
//        tempBitmap = Bitmap.createBitmap(wDst,hDst,  Bitmap.Config.ARGB_8888);
//        tempCanvas = new Canvas(tempBitmap);
    }

    public void setMatrix(Matrix matrix)
    {
        matrix0.set(matrix);
        float x[] = new float[9];
        matrix0.getValues(x);
        scale = x[Matrix.MSCALE_X];
        xOff = x[Matrix.MTRANS_X];
        yOff = x[Matrix.MTRANS_Y];

        if(tempBitmap == null) return;
        tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        float canvasWidth = wDst * scale;
        float canvasHeight = hDst * scale;
        int parentWidth = parentRight - parentLeft;
        int parentHeight = parentBottom - parentTop;

        float drwLeft = parentLeft  + xOff;
        float drwTop = parentTop + yOff ;

        float drwRight = drwLeft + canvasWidth;
        float drwBottom = drwTop + canvasHeight;
        tempCanvas.clipRect(drwLeft ,drwTop, drwRight, drwBottom);
        invalidate();
    }
    public Bitmap mergeToCache() {
        m_bEdited = false;

        Bitmap destBitmap = Bitmap.createBitmap(wDst, hDst, Bitmap.Config.ALPHA_8);
        Canvas destCanvas = new Canvas(destBitmap);
        destCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        destCanvas.drawBitmap(bitmapMask, 0, 0, new Paint());
        for (StrokeInfo si : pathList) {
            drawPaint.setColor(si.PaintColor());
            drawPaint.setXfermode(si.xferMode());
            drawPaint.setStrokeWidth(si.strokeWidth);
            Path path = new Path();
            path.moveTo(si.points.get(0).x, si.points.get(0).y);
            for (int i = 1; i < si.points.size(); i ++) {
                path.lineTo(si.points.get(i).x,si.points.get(i).y);
            }
            Matrix matrix = new Matrix();
            matrix.setValues(si.matrix);
            path.transform(matrix);
            destCanvas.drawPath(path, drawPaint);
        }
        return destBitmap;
    }

    public void initDrawingPos(float scale, int x, int y) {
//        this.scale = scale;
//        xOff = x;
//        yOff = y;
    }

    public void zoom(float scale, float xOff, float yOff) {

        drawPath.reset();
        this.scale = scale;
        this.xOff = xOff;
        this.yOff = yOff;
        tempCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

        //tempCanvas.clipRect(0,100,tempBitmap.getWidth(), 400);

        invalidate();
    }

    public void undo() {
        if (pathList.size() <= 0) {
            return;
        }
        m_bEdited = true;
        pathList.remove(pathList.size() - 1);
        invalidate();
    }

    //Bitmap bitmapEraser = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_eraser);
    //Bitmap bitmapBrush = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.ic_brush);

    @Override
    protected void onDraw(Canvas canvas) {
        if(tempBitmap == null) return;

        tempBitmap.eraseColor(Color.TRANSPARENT);
        Matrix matrix = new Matrix();
        matrix.set(matrix0);
//        matrix.postScale(scale, scale);
//        matrix.postTranslate(xOff, yOff);

        if(bitmapMask!=null)
            tempCanvas.drawBitmap(bitmapMask, matrix, canvasPaint);
        for (StrokeInfo si : pathList) {

            Path path = new Path();
            path.moveTo(si.points.get(0).x, si.points.get(0).y);
            for (int i = 1; i < si.points.size(); i ++) {
                path.lineTo(si.points.get(i).x,si.points.get(i).y);
            }
            Matrix matrix1 = new Matrix();
            matrix1.setValues(si.matrix);
            path.transform(matrix1);
            path.transform(matrix);

            drawPaint.setXfermode(si.xferMode());
            drawPaint.setStrokeWidth(si.strokeWidth * scale);
            drawPaint.setColor(si.PaintColor());
            tempCanvas.drawPath(path, drawPaint);
        }

        if (!drawPath.isEmpty()) {
            float size = 0;

            drawPaint.setStrokeWidth(brushSize/* * scale*/);
            size = brushSize;// * scale;
            drawPaint.setColor(cur_Color);
            drawPaint.setXfermode(curXfermode);
            tempCanvas.drawPath(drawPath, drawPaint);

            RectF rect = new RectF(cx-size / 2, cy-size / 2, cx+size / 2, cy+size / 2);
            tempCanvas.drawOval(rect, pointPaint);
//            if(mode == DRAWING_MODE)
//                tempCanvas.drawBitmap(bitmapBrush, new Rect(0,0, bitmapBrush.getWidth(), bitmapBrush.getHeight()), rect, copyingPaint);
//            else
//                tempCanvas.drawBitmap(bitmapEraser, new Rect(0,0, bitmapEraser.getWidth(), bitmapEraser.getHeight()), rect, copyingPaint);
        }
        canvas.drawBitmap(tempBitmap, 0, 0, copyingPaint);

    }

    float cx = 0;
    float cy = 0;
    ArrayList<PointF> ppsList = new ArrayList<>();
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();
        float size;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                cy = touchY; // - size * scale /2 - MeasureUtil.convertDpToPixels(50, getContext());;
                cx = touchX;
                drawPath.moveTo(touchX, cy);
                ppsList.clear();
                ppsList.add(new PointF(touchX,cy));

                break;
            case MotionEvent.ACTION_MOVE:
                cy = touchY;// - size * scale /2 - MeasureUtil.convertDpToPixels(50, getContext());;
                cx = touchX;
                drawPath.lineTo(touchX, cy);
                ppsList.add(new PointF(touchX, cy));
                m_bEdited = true;
                break;
            case MotionEvent.ACTION_UP: {

                Matrix matrix = new Matrix();
                matrix.postTranslate(-xOff, -yOff);
                matrix.postScale(1.0f / scale, 1.0f / scale);

                StrokeInfo si = new StrokeInfo();

                matrix.getValues(si.matrix);
                si.points.addAll(ppsList);
//                si.path.addPath(drawPath, matrix);

                si.mode = mode;//curXfermode;
                si.strokeWidth =  brushSize;
                si.strokeWidth = si.strokeWidth / scale;
                pathList.add(si);
                drawPath.reset();
                m_bEdited = true;
                break;
            }
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void ResetPath() {
        drawPath.reset();
        ppsList.clear();
    }
}
