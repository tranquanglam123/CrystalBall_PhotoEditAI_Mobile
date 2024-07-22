package org.CrystalBall;

import android.graphics.Bitmap;
import android.graphics.Rect;

public class Engine {
    static {
        System.loadLibrary("CrystalBall");
    }

    private static  Engine s_Instance = null;
    public static Engine getIntance()
    {
        if (s_Instance == null)
        {
            s_Instance = new Engine();
        }

        return s_Instance;
    }

    public static Bitmap visonmix_fgBitmap =null;
    public static Bitmap visonmix_bgBitmap =null;

    public static Rect[] getFaceRects(int[] detetArray)
    {
        int faceCount = detetArray[0];
        Rect[] ret = new Rect[faceCount];
        for(int i=0; i<faceCount; i++) {
            ret[i].left = detetArray[i * 14 + 1];
            ret[i].top = detetArray[i * 14 + 2];
            ret[i].right = detetArray[i * 14 + 3];
            ret[i].bottom = detetArray[i * 14 + 4];
        }
        return ret;
    }

    public static Rect getFaceRectLandmark(int[] landmark) {
        if(landmark == null) return null;
        Rect rect = new Rect();
        rect.left = landmark[0];
        rect.top = landmark[1];
        rect.right = landmark[0] + landmark[2];
        rect.bottom = landmark[1] + landmark[3];
        return rect;
    }

    public native boolean EngineInit(String faceDetectionModelPath,String lmDBPath, Bitmap sketchTexture);
    public native int[] FaceDetect(Bitmap bitmapIn);
    public native int FaceLandmark(Bitmap bitmapIn);
    public native  int[] getLandmarkInfo(int faceIndex);
    //param value -100~100, default 0
    public native byte[] FaceMorphing(Bitmap facebitmapIn,  int faceIndex, int eyeParam, int faceParam);
    //param value 0~100, default 30
    public native byte[] FaceJaw(Bitmap facebitmapIn, int faceIndex, int param);
    //param value 0~100, default 50
    public native byte[] FaceLip(Bitmap facebitmapIn, int faceIndex, int param);
    //param value 0~100, default 50
    public native byte[] FaceNose(Bitmap facebitmapIn, int faceIndex, int param);
    //param value 0~100, default 50
    public native byte[] FaceForehead(Bitmap facebitmapIn, int faceIndex, int param);

    public native int FaceBeauty(Bitmap bitmapIn, Bitmap bitmapOut, int param);  //default param 0~100, default 0
//    public native int ImageClarity(Bitmap bitmapIn, Bitmap bitmapOut, int param);
    public native Bitmap ImageClarity(Bitmap bitmapIn,  int param);
    public native Bitmap ImageCartoon(Bitmap bitmapIn,  int param);
    public native int removeAcneOne(Bitmap bitmapIn, Bitmap bitmapOut);
    //////////////////////////
    public native int ThemeRomantic(Bitmap bitmapIn1, Bitmap bitmapIn2, Bitmap bitmapOut, int[] points);
    //////////////////////////
    public native int SoftFocusBackgroundFilter(Bitmap bitmap, int filterIndex, int filterParam);
    //////////////////////////
    public native boolean FaceModelUnInit();

    public native int magicTrimapMask(Bitmap bitmapIn, Bitmap bitmapOut); //output is 224*224 bitmap
    public native int magicTrimapMaskAndInfoflow(Bitmap bitmapIn, Bitmap bitmapOut); //output is 224*224 bitmap
    public native int magicResizeTrimap(Bitmap trimapIn, Bitmap bitmapOut, boolean bMono, boolean bPreproc);  //input trimap argb, output mask 8bit alpha bitmap
    public native Bitmap magicGetOneChannel(Bitmap bitmapIn, int channelIndex); //channelIndex = 0-R, 1-G, 2-B, 3-A
  //  public native void getMaskImage(byte[] imageDate, int imageWidth , int imageHeight, byte[] mask);
    public native byte[] getAlphaByteArray(Bitmap bitmapIn);
    public native Bitmap magicGetMattingImage(Bitmap bitmapIn, Bitmap maskImage); //input image argb, maskImage alpha_8 bitmap
    public native Bitmap magicGetMattingImageGuidefilterOnly(Bitmap bitmapIn, Bitmap maskImage); //input image argb, maskImage alpha_8 bitmap
    public native Bitmap magicGetMattingImageForEffect(Bitmap bitmapIn, Bitmap maskImage, int[] position);
    public native Bitmap makeBitmapToGray(Bitmap bitmapIn);
}
