package com.samhung.crystalball.photoeditor.Photosticker;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.view.View;
import android.widget.ImageView;

import com.math.photostickersdk.Vector2D;

public class AdjustItemPosition {
    public static void adjustposition_glass(PhotostickerActivity activity, View view) {
        Matrix fMatrix = activity.ivPhoto.getImageMatrix();
        float[] m = new float[9];
        fMatrix.getValues(m);
        float nWidth = activity.mBitmap.getWidth() * m[Matrix.MSCALE_X];
        float nHeight = activity.mBitmap.getHeight() * m[Matrix.MSCALE_Y];

        int[] m_landmarkPoints = activity.m_landmarkPoints;
        Bitmap mBitmap = activity.mBitmap;
        PointF ptCenter = activity.EnginePointToAppPoint(m_landmarkPoints[4+27*2], m_landmarkPoints[4+27*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF ptTmp =activity.EnginePointToAppPoint(m_landmarkPoints[4+28*2], m_landmarkPoints[4+28*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        ptCenter.y = (ptCenter.y+ptTmp.y)/2;
        view.setTranslationX(0-nWidth/2 + ptCenter.x);
        view.setTranslationY(0-nHeight/2+ptCenter.y);

        PointF leftEyeEnd = activity.EnginePointToAppPoint(m_landmarkPoints[4+36*2], m_landmarkPoints[4+36*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF rightEyeEnd = activity.EnginePointToAppPoint(m_landmarkPoints[4+45*2], m_landmarkPoints[4+45*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        float eyeLength = (float)Math.sqrt((leftEyeEnd.x-rightEyeEnd.x)*(leftEyeEnd.x-rightEyeEnd.x) + (leftEyeEnd.y-rightEyeEnd.y)*(leftEyeEnd.y-rightEyeEnd.y)) * 1.4f;
        float scale = eyeLength / activity.drawableWidth;

        view.setScaleX(scale);
        view.setScaleY(scale);

        Vector2D vector1 = new Vector2D(rightEyeEnd.x-leftEyeEnd.x, 0);
        Vector2D vector2 = new Vector2D(rightEyeEnd.x-leftEyeEnd.x, rightEyeEnd.y-leftEyeEnd.y);
        vector1.normalize();
        vector2.normalize();
        double degrees = (180.0 / Math.PI) * (Math.atan2(vector2.y, vector2.x) - Math.atan2(vector1.y, vector1.x));
        view.setRotation((float)degrees);
    }


    public static void adjustposition_mouthRose(PhotostickerActivity activity, View view) {
        Matrix fMatrix = activity.ivPhoto.getImageMatrix();
        float[] m = new float[9];
        fMatrix.getValues(m);

        Bitmap mBitmap  = activity.mBitmap;
        int[] m_landmarkPoints = activity.m_landmarkPoints;
        float nWidth = mBitmap.getWidth() * m[Matrix.MSCALE_X];
        float nHeight = mBitmap.getHeight() * m[Matrix.MSCALE_Y];

        PointF pt49 = activity.EnginePointToAppPoint(m_landmarkPoints[4+49*2], m_landmarkPoints[4+49*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF pt59 = activity.EnginePointToAppPoint(m_landmarkPoints[4+59*2], m_landmarkPoints[4+59*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF pt53 = activity.EnginePointToAppPoint(m_landmarkPoints[4+53*2], m_landmarkPoints[4+53*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF pt55 = activity.EnginePointToAppPoint(m_landmarkPoints[4+55*2], m_landmarkPoints[4+55*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF mouthLeft = new PointF((pt49.x+pt59.x)/2, (pt49.y+pt59.y)/2);
        PointF mouthRight = new PointF((pt53.x+pt55.x)/2, (pt53.y+pt55.y)/2);
        PointF mouthCenter = new PointF((mouthLeft.x+mouthRight.x)/2, (mouthLeft.y+mouthRight.y)/2);

        view.setTranslationX(0-nWidth/2+mouthCenter.x);
        view.setTranslationY(0-nHeight/2+mouthCenter.y);

        float mouthLength = (float)Math.sqrt((mouthLeft.x-mouthRight.x)*(mouthLeft.x-mouthRight.x) + (mouthLeft.y-mouthRight.y)*(mouthLeft.y-mouthRight.y));
        float scale = mouthLength / 50;
        if(scale > 10 || scale < 0.2) scale = 1;
        view.setScaleX(scale);
        view.setScaleY(scale);

        Vector2D vector1 = new Vector2D(mouthRight.x-mouthLeft.x, 0);
        Vector2D vector2 = new Vector2D(mouthRight.x-mouthLeft.x, mouthRight.y-mouthLeft.y);
        vector1.normalize();
        vector2.normalize();
        double degrees = (180.0 / Math.PI) * (Math.atan2(vector2.y, vector2.x) - Math.atan2(vector1.y, vector1.x));
        view.setRotation((float)degrees);
    }

    public static void adjustposition_headobject(PhotostickerActivity activity, View view, int index) {
        Matrix fMatrix = activity.ivPhoto.getImageMatrix();
        float[] m = new float[9];
        fMatrix.getValues(m);

        Bitmap mBitmap = activity.mBitmap;
        int[] m_landmarkPoints = activity.m_landmarkPoints;

        float nWidth = mBitmap.getWidth() * m[Matrix.MSCALE_X];
        float nHeight = mBitmap.getHeight() * m[Matrix.MSCALE_Y];
        PointF faceTL = activity.EnginePointToAppPoint(m_landmarkPoints[0], m_landmarkPoints[1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF faceTR = activity.EnginePointToAppPoint(m_landmarkPoints[0]+m_landmarkPoints[2], m_landmarkPoints[1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF faceTC = new PointF((faceTL.x+faceTR.x)/2, faceTL.y);
        PointF pt27 = activity.EnginePointToAppPoint(m_landmarkPoints[4+27*2], m_landmarkPoints[4+27*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF leftEyeEnd = activity.EnginePointToAppPoint(m_landmarkPoints[4+36*2], m_landmarkPoints[4+36*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF rightEyeEnd = activity.EnginePointToAppPoint(m_landmarkPoints[4+45*2], m_landmarkPoints[4+45*2+1], mBitmap.getWidth(), mBitmap.getHeight());

        PointF ptCenter = new PointF(pt27.x, faceTL.y);
        float scale = 1;
        if(index == 10 || index == 23 || index == 24 || index==27) {
            scale = getDistance(leftEyeEnd, rightEyeEnd) * 1f / activity.drawableWidth;
            ptCenter.y -= (pt27.y-faceTL.y) * scale;
        } else if(index == 12) {
            scale = getDistance(leftEyeEnd, rightEyeEnd) * 1.6f * 1.7f / activity.drawableWidth;
            ptCenter.y = pt27.y-activity.drawableHeight / 3 * 2 * scale;
        }

        double angle = getAngle(leftEyeEnd, rightEyeEnd);
        PointF ptCenterD = rotatePoint(pt27, ptCenter, angle / 180 * Math.PI);
        view.setTranslationX(0-nWidth/2+ptCenterD.x);
        view.setTranslationY(0 - nHeight / 2 + ptCenterD.y);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setRotation((float)angle);
    }

    public static void adjustposition_noseobject(PhotostickerActivity activity, View view, int imgIndex) {
        Matrix fMatrix = activity.ivPhoto.getImageMatrix();
        float[] m = new float[9];
        fMatrix.getValues(m);

        Bitmap mBitmap = activity.mBitmap;
        int[] m_landmarkPoints = activity.m_landmarkPoints;

        float nWidth = mBitmap.getWidth() * m[Matrix.MSCALE_X];
        float nHeight = mBitmap.getHeight() * m[Matrix.MSCALE_Y];

        PointF pt30 = activity.EnginePointToAppPoint(m_landmarkPoints[4+30*2], m_landmarkPoints[4+30*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF pt33 = activity.EnginePointToAppPoint(m_landmarkPoints[4+33*2], m_landmarkPoints[4+33*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF pt51 = activity.EnginePointToAppPoint(m_landmarkPoints[4+51*2], m_landmarkPoints[4+51*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF leftEyeEnd = activity.EnginePointToAppPoint(m_landmarkPoints[4+36*2], m_landmarkPoints[4+36*2+1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF rightEyeEnd = activity.EnginePointToAppPoint(m_landmarkPoints[4+45*2], m_landmarkPoints[4+45*2+1], mBitmap.getWidth(), mBitmap.getHeight());

        PointF ptCenter = new PointF();
        float scale = 1;
        if(imgIndex == 9) {
            ptCenter.x = (pt33.x + pt51.x)/2;
            ptCenter.y = (pt33.y + pt51.y)/2;
            scale = getDistance(pt33, pt51) * 0.9f / activity.drawableHeight;
        } else  {
            ptCenter.x = pt30.x;
            ptCenter.y = pt30.y;
            scale = getDistance(leftEyeEnd, rightEyeEnd) / activity.drawableWidth;
        }
        float angle = (float)getAngle(leftEyeEnd, rightEyeEnd);
        view.setTranslationX(0-nWidth/2+ptCenter.x);
        view.setTranslationY(0-nHeight/2+ptCenter.y);
        view.setScaleX(scale);
        view.setScaleY(scale);
        view.setRotation(angle);
    }

    public static void adjust_FaceFocusPosition(PhotostickerActivity activity) {
        if(activity.mBitmap == null) return;
        Matrix fMatrix = activity.ivPhoto.getImageMatrix();
        float[] m = new float[9];
        fMatrix.getValues(m);

        Bitmap mBitmap = activity.mBitmap;
        int[] m_landmarkPoints = activity.m_landmarkPoints;

        float nWidth = mBitmap.getWidth() * m[Matrix.MSCALE_X];
        float nHeight = mBitmap.getHeight() * m[Matrix.MSCALE_Y];

        ImageView ivFocus = activity.ivFocus;
        PointF faceBL = activity.EnginePointToAppPoint(m_landmarkPoints[0], m_landmarkPoints[1]+m_landmarkPoints[3], mBitmap.getWidth(), mBitmap.getHeight());
        PointF faceBR = activity.EnginePointToAppPoint(m_landmarkPoints[0]+m_landmarkPoints[2], m_landmarkPoints[1]+m_landmarkPoints[3], mBitmap.getWidth(), mBitmap.getHeight());
        PointF faceBC = new PointF((faceBL.x+faceBR.x)/2, faceBL.y);
        PointF faceTL = activity.EnginePointToAppPoint(m_landmarkPoints[0], m_landmarkPoints[1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF faceTR = activity.EnginePointToAppPoint(m_landmarkPoints[0]+m_landmarkPoints[2], m_landmarkPoints[1], mBitmap.getWidth(), mBitmap.getHeight());
        PointF faceTC = new PointF((faceTL.x+faceTR.x)/2, faceTL.y);

        ivFocus.setTranslationX(0-nWidth/2+faceBC.x);
        ivFocus.setTranslationY(0-nHeight/2+faceBC.y);
        float scale = m[Matrix.MSCALE_X];

        ivFocus.setScaleX(scale);
        ivFocus.setScaleY(scale);

    }

    static float getDistance(PointF p1, PointF p2) {
        return (float)Math.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
    }

    static PointF rotatePoint( PointF o, PointF p, double alpha ) {
        PointF tp = new PointF();
        p.x -= o.x;
        p.y -= o.y;
        tp.x = p.x * (float)Math.cos( alpha ) - p.y * (float)Math.sin( alpha ) + o.x;
        tp.y = p.x * (float)Math.sin( alpha ) + p.y * (float)Math.cos( alpha ) + o.y;
        return tp;
    }

    static double getAngle(PointF p1, PointF p2) {
        Vector2D vector1 = new Vector2D(p2.x-p1.x, 0);
        Vector2D vector2 = new Vector2D(p2.x-p1.x, p2.y-p1.y);
        vector1.normalize();
        vector2.normalize();
        double degrees = (180.0 / Math.PI) * (Math.atan2(vector2.y, vector2.x) - Math.atan2(vector1.y, vector1.x));
        return degrees;
    }
}
