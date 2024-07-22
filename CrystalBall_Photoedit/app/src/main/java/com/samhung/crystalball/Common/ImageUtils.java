package com.samhung.crystalball.Common;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Debug;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;

import com.samhung.crystalball.photoeditor.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static java.security.AccessController.getContext;


public class ImageUtils {
    public class CGSize {
        public int width = 1;
        public int height = 1;
        public CGSize(int w, int h)
        {
            width = w;
            height = h;
        }
    }

    public Bitmap m_bm;
    public static int m_bufWid = 0;
    public static int m_bufHei = 0;
    public static final int CAP_IMG_WID = 70;

    public  static void setBrightness(ColorMatrix cm, float brightness) {
        float scale = brightness + 1.f;
        float translate = (-.5f * scale + .5f) * 255.f;
        cm.set(new float[] {
                1, 0, 0, 0, translate,
                0, 1, 0, 0, translate,
                0, 0, 1, 0, translate,
                0, 0, 0, 1, 0 });
    }

    public static Bitmap getBitmap(String path)
    {
        try{
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
            opt.inPurgeable = true;
            opt.inInputShareable = true;
            Bitmap bm = BitmapFactory.decodeFile(path, opt);

            return bm;
        } catch (Exception ex)
        {
            return null;
        }
    }
    public static Bitmap getValidateBitmap(String path, CGSize outSize )
    {
//		System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        try
        {
            BitmapFactory.decodeFile(path, options);
        } catch (OutOfMemoryError e) {
            return null;
        }

        int size = options.outWidth * options.outHeight * 4;

        long availablesize = Debug.getNativeHeapAllocatedSize();
        float scale = (float)size / availablesize;

        if( outSize != null )
        {
            float scale1 = (float)options.outWidth * options.outHeight / ( outSize.width * outSize.height );

            if( scale1 > scale )
                scale = scale1;
        }

        int s = 1;
        while(true)
        {
            if( s * s > scale )
                break;
            s++;
        }

        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inSampleSize = s;
        opt.inPurgeable = true;
        opt.inInputShareable = true;

        Bitmap bm = null;
        ExifInterface exif;
        try {
            exif = new ExifInterface(path);
            int exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90);
            int exifDegree = exifOrientationToDegrees(exifOrientation);
            try
            {
                bm = BitmapFactory.decodeFile(path, opt);
            } catch (OutOfMemoryError e) {
                return null;
            }

            bm = rotate(bm, exifDegree);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        if( bm == null )
            bm = null;
        return bm;
    }


    public static int exifOrientationToDegrees(int exifOrientation)
    {
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90)
        {
            return 90;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180)
        {
            return 180;
        }
        else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270)
        {
            return 270;
        }
        return 0;
    }

    /**
     * �대�吏�� �뚯쟾�쒗궢�덈떎.
     *
     * @param bitmap 鍮꾪듃留��대�吏�
     * @param degrees �뚯쟾 媛곷룄
     * @return �뚯쟾���대�吏�
     */
    public static Bitmap rotate(Bitmap bitmap, int degrees)
    {
        if(degrees != 0 && bitmap != null)
        {
            Matrix m = new Matrix();
            m.setRotate(degrees, (float) bitmap.getWidth() / 2,
                    (float) bitmap.getHeight() / 2);

            try
            {
                Bitmap converted = Bitmap.createBitmap(bitmap, 0, 0,
                        bitmap.getWidth(), bitmap.getHeight(), m, true);
                if(bitmap != converted)
                {
                    bitmap.recycle();
                    bitmap = converted;
                }
            }
            catch(OutOfMemoryError ex)
            {
                // 硫붾え由ш� 遺�”�섏뿬 �뚯쟾���쒗궎吏�紐삵븷 寃쎌슦 洹몃깷 �먮낯��諛섑솚�⑸땲��
            }
        }
        return bitmap;
    }

    public static Bitmap getBitmapFrom32BitArray(byte[] buff, int width)
    {
        if (buff == null)
            return null;

        //Now put these nice RGBA pixels into a Bitmap object
        int height = buff.length / (width * 4);

        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ret.copyPixelsFromBuffer(ByteBuffer.wrap(buff));

        return ret;
    }

    public static Bitmap getBitmapFrom32BitArray(byte[] buff, int width, int height) {
        if (buff == null)
            return null;

        //Now put these nice RGBA pixels into a Bitmap object
        //  int height = buff.length / (width * 4);

        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        byte[] buffTmp = Arrays.copyOf(buff, width * height * 4);
        ret.copyPixelsFromBuffer(ByteBuffer.wrap(buffTmp));//, 0, width * height * 4));

        return ret;
    }
//    void ImageCut(BYTE *pImage, int w, int h, int nBitCount, BYTE *pNewImg, int left, int top, int right, int bottom)
//    {
//        int i, j, n = 0, nBytes = nBitCount/8;
//
//        for (i = top; i < bottom; i++) {
//            for (j = left; j < right; j++) {
//                pNewImg[n++] = pImage[(i*w+j)*nBytes+0];
//                pNewImg[n++] = pImage[(i*w+j)*nBytes+1];
//                pNewImg[n++] = pImage[(i*w+j)*nBytes+2];
//            }
//        }
//    }

    public static Bitmap GetSizeedBitmap(Bitmap bm, int ori_wid, int ori_hei, int new_x, int new_y, int new_wid, int new_hei)
    {
//        int[] pixels = new int[new_wid* new_hei];
//        bm.getPixels(pixels, 0,new_wid,new_x, new_y, new_wid, new_hei);
//        bm.setPixels(pixels, 0, new_wid, new_x, new_y, new_wid, new_hei);

//        return         Bitmap.createScaledBitmap(bm, 140, 140, true);

        byte[] RawImg = ImageUtils.getByteArrayFromBitmap(bm);

        int i, j, n = 0, nBytes = 4;

        if((new_wid +new_x) > ori_wid ) new_wid = ori_wid - new_x;
        if((new_hei +new_y )> ori_hei ) new_hei = ori_hei - new_y;

        byte [] CutImg = new byte[new_wid*new_hei*4];

        for (i = new_y; i < (new_y+new_hei); i++) {
            for (j = new_x; j <(new_x+new_wid); j++) {
                CutImg[n++] = RawImg[(i*ori_wid+j)*nBytes+0];
                CutImg[n++] = RawImg[(i*ori_wid+j)*nBytes+1];
                CutImg[n++] = RawImg[(i*ori_wid+j)*nBytes+2];
                CutImg[n++] = RawImg[(i*ori_wid+j)*nBytes+3];
            }
        }
        Bitmap bm1 =getBitmapFrom32BitArray(CutImg, new_wid);
        return Bitmap.createScaledBitmap(bm1, ImageUtils.CAP_IMG_WID, ImageUtils.CAP_IMG_WID, true);
    }

    public static Rect EngineRectToAppRect(int x, int y, int width, int height, int PreviewWid, int PreviewHei)
    {
        Rect rect = new Rect();
        int nWidth = PreviewWid;
        int nHeight = PreviewHei;

        rect.right = (x+width)*nWidth/m_bufWid;
        rect.left =   nWidth - rect.right - 1;
        rect.right = rect.left + width*nWidth/m_bufWid;

        //flip start
        int tmpLeft = rect.left;
        int tmpRight = rect.right;
        rect.right = nWidth - tmpLeft - 1;
        rect.left = rect.right - (tmpRight-tmpLeft);
        //flip end

        rect.top = y*nHeight/m_bufHei;
        rect.bottom=(y+height)*nHeight/m_bufHei;
        return rect;
    }

    public static Bitmap getBitmapFromJpeg(byte[] data)
    {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public static Bitmap getBitmapFromByteArray(byte[] buff, int width)
    {
        if (buff == null)
            return null;

        byte [] Bits = new byte[buff.length*4]; //That's where the RGBA array goes.

        for (int i = 0; i < buff.length; i++)
        {
            Bits[i*4] =
                    Bits[i*4+1] =
                            Bits[i*4+2] = buff[i]; //Invert the source bits
            Bits[i*4+3] = -1;//0xff, that's the alpha.
        }

        //Now put these nice RGBA pixels into a Bitmap object
        int height = buff.length / width ;

        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ret.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));

        return ret;
    }

    public static Bitmap getBitmapFrom24ByteArray(byte[] buff, int width)
    {
        if (buff == null)
            return null;

        byte [] Bits = new byte[buff.length/3*4]; //That's where the RGBA array goes.

        for (int i = 0; i < buff.length / 3; i++)
        {
            Bits[i*4] = buff[i*3+2];
            Bits[i*4+1] = buff[i*3+1];
            Bits[i*4+2] = buff[i*3];
            Bits[i*4+3] = -1;//0xff, that's the alpha.
        }

        //Now put these nice RGBA pixels into a Bitmap object
        int height = buff.length / 3 / width;

        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ret.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));

        return ret;
    }

    public static Bitmap getBitmapFromByteArray1(byte[] buff, int width)
    {
        if (buff == null)
            return null;

        byte [] Bits = new byte[buff.length/3*4]; //That's where the RGBA array goes.

        for (int i = 0; i < buff.length / 3; i++)
        {
            Bits[i*4] = buff[i*3+2];
            Bits[i*4+1] = buff[i*3+1];
            Bits[i*4+2] = buff[i*3];
            Bits[i*4+3] = -1;//0xff, that's the alpha.
        }

        //Now put these nice RGBA pixels into a Bitmap object
        int height = buff.length / 3 / width;

        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ret.copyPixelsFromBuffer(ByteBuffer.wrap(Bits));

        return ret;
    }


    public static byte[] getByteArrayFromBitmap(Bitmap bm)
    {
        // 이미지를 상황에 맞게 회전시킨다

        if (bm == null)
            return null;

        int cx = bm.getWidth();
        int cy = bm.getHeight();
        int nBufferSize = cx * cy * 4;
        try
        {
            long availablesize = Debug.getNativeHeapAllocatedSize();

            if( availablesize < nBufferSize )
            {
                return null;
            }

            ByteBuffer byBuff = null;
            try {
                byBuff = ByteBuffer.allocate(nBufferSize);
            } catch (OutOfMemoryError e) {
                byBuff = null;
                System.gc();
                return null;
            }

            if( byBuff == null )
                return null;

            bm.copyPixelsToBuffer(byBuff);



            return byBuff.array();

        }
        catch(IllegalArgumentException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean checkBitmapFitsInMemory(long bmpwidth,long bmpheight, int bmpdensity ){
        long reqsize=bmpwidth*bmpheight*bmpdensity;
        long allocNativeHeap = Debug.getNativeHeapAllocatedSize();


        final long heapPad=(long) Math.max(4*1024*1024,Runtime.getRuntime().maxMemory()*0.1);
        if ((reqsize + allocNativeHeap + heapPad) >= Runtime.getRuntime().maxMemory())
        {
            return false;
        }
        return true;

    }

    public static ByteBuffer getByteBufferFromBitmap(Bitmap bm)
    {
        // 이미지를 상황에 맞게 회전시킨다

        if (bm == null)
            return null;

        int cx = bm.getWidth();
        int cy = bm.getHeight();
        int nBufferSize = cx * cy * 4;
        try
        {
//            long availablesize = android.os.Debug.getNativeHeapAllocatedSize();

//            if( availablesize < nBufferSize )
//            {
//                return null;
//            }
//            if(!checkBitmapFitsInMemory(cx,cy,4))
//            {
//                return null;
//            }

            ByteBuffer byBuff = null;
            try {
                byBuff = ByteBuffer.allocate(nBufferSize);
            } catch (OutOfMemoryError e) {
                byBuff = null;
                System.gc();
                return null;
            }

            if( byBuff == null )
                return null;

            bm.copyPixelsToBuffer(byBuff);

            return byBuff;

        }
        catch(IllegalArgumentException e )
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getBitmapFromGrayMaskBytes(byte[] buff, int width, int height)
    {
        if (buff == null)
            return null;

        ByteBuffer bb = ByteBuffer.allocate(buff.length*4);
        byte [] Bits =bb.array();// new byte[buff.length*4]; //That's where the RGBA array goes.

        for (int i = 0; i < buff.length; i++) {
            if (buff[i] ==0) {
                Bits[i * 4] =
                        Bits[i * 4 + 1] =
                                Bits[i * 4 + 2] = 0;//buff[i]; //Invert the source bits
                Bits[i * 4 + 3] = 0;//0xff, that's the alpha.
            } else if (buff[i] == (byte)0x80) {
                Bits[i * 4] = (byte) 0;
                Bits[i * 4 + 1] = (byte)255;
                Bits[i * 4 + 2] = 0;
                Bits[i * 4 + 3] = (byte) buff[i];//buff[i]; //Invert the source bits
            }else {
                Bits[i * 4] = (byte) 255;
                Bits[i * 4 + 1] =
                        Bits[i * 4 + 2] = 0;
                Bits[i * 4 + 3] = (byte) buff[i];//buff[i]; //Invert the source bits
            }

        }

        //Now put these nice RGBA pixels into a Bitmap object
        Bitmap ret = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        ret.copyPixelsFromBuffer(bb);

        bb.clear();
        return ret;
    }

    public static String saveImage(Bitmap bitmap, String fileName, boolean bPNG) {
        if(fileName == "") fileName = "IMG";
        String selectedOutputPath = "";
        if (MainActivity.isSDCARDMounted()) {
//            File mediaStorageDir = new File(
//                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MainActivity.DONE_PHOTO_DIR);
            File mediaStorageDir = new File(
                    Environment.getExternalStorageDirectory(), MainActivity.DONE_PHOTO_DIR);
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    return null;
                }
            }
            // Create a media file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            selectedOutputPath = mediaStorageDir.getPath() + File.separator
                    + fileName+"_" + timeStamp + (bPNG?".png":".jpg");

            File file = new File(selectedOutputPath);
            try {
                FileOutputStream out = new FileOutputStream(file);

                if(bPNG)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                else
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                //227save
                com.l.l.l.f.saveImage(selectedOutputPath);
                /////////

                //new SingleMediaScanner(MainActivity.mainActivity, mediaStorageDir);
                SingleMediaScanner.RefreshScan(MainActivity.mainActivity, selectedOutputPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return selectedOutputPath;
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeFile(String strFileName, int reqWid, int reqHei) {
        try {
            //227load
            boolean res = com.t.t.t.d.isLegalFile(strFileName);
//            if(res == false)
//                return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            /////////
            // Decode image size
            File f = new File(strFileName);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = calculateInSampleSize(o, reqWid, reqHei);
            o2.inDither = false;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    public static Bitmap decodeFile(String strFileName) {
        try {
            //227load
//            boolean res = com.t.t.t.d.isLegalFile(strFileName);
//            if(res == false)
//                return Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            /////////
            // Decode image size
            File f = new File(strFileName);
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            long imageSize = o.outWidth * o.outHeight * 4;
            long maxMem = Runtime.getRuntime().maxMemory();

            int reqWidth = o.outWidth;
            int reqHeight = o.outHeight;
            int scale = 1;
            while(imageSize > (maxMem / 7)) {
                reqWidth /= 2;
                reqHeight /=2;
                scale *=2;
                imageSize = reqWidth * reqHeight * 4;
            }
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inDither = false;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    public static void Blur_Effect(Context context, Bitmap bmIn, Bitmap bmOut, float radius) {
        final RenderScript rs = RenderScript.create( context );
        final Allocation input = Allocation.createFromBitmap( rs, bmIn, Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT );
        final Allocation output = Allocation.createTyped( rs, input.getType() );
        final ScriptIntrinsicBlur script = ScriptIntrinsicBlur.create( rs, Element.U8_4( rs ) );
        script.setRadius( radius /* e.g. 3.f */ );
        script.setInput( input );
        script.forEach( output );
        output.copyTo( bmOut );
    }
}