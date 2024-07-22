package com.samhung.crystalball.photoeditor.VisionMix.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.SharedPref;
import com.samhung.crystalball.photoeditor.Utilis.WaitWindow;
import com.samhung.crystalball.photoeditor.VisionMix.Views.BrushDrawingView;
import com.samhung.crystalball.photoeditor.Widget.NavigatorView;
import com.samhung.crystalball.widgets.Slider;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.Calendar;

import org.CrystalBall.Engine;

public class VisionmaskActivity extends BaseActivity  implements View.OnTouchListener{
    private final String TAG = "VISION_MASK";
    BrushDrawingView brushDrawingView;
    NavigatorView navigatorView;
    RelativeLayout navigatorContainer;
    ImageView ivPhoto;
    Slider sbStrokeWidth;
    Slider sbStrokeHardness;
    RadioGroup rgBrushType;
    int brushType = R.id.rd_drawer_hard;

    int wSrc = 0;
    int hSrc = 0;
    int wDrawingPane = 0;
    int hDrawingPane = 0;
    int xInDrawingPane =0;
    int yInDrawingPane =0;
    float scale0 =1;

    String imagePath;

    Bitmap bitmap;
    Bitmap bitmapMask;
    Bitmap bitmapMaskEdited;
    Bitmap bitmapTrimap;
    Matrix matrix = new Matrix();
    Matrix matrix0 = new Matrix();

    AsyncTask_getMask asyncTaskMasking;

    boolean isEditing = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visionmask);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));

        brushDrawingView = (BrushDrawingView)findViewById(R.id.brushDrawingView);
        navigatorContainer = (RelativeLayout)findViewById(R.id.rl_naviview);
        navigatorView = (NavigatorView)findViewById(R.id.naviView);
        navigatorContainer.setVisibility(View.INVISIBLE);

        isEditing = getIntent().getExtras().getBoolean("editing");
        if (!isEditing) {
            imagePath = getIntent().getExtras().getString("selectedImagePath");
            String extension = imagePath.substring(imagePath.length()-4).toLowerCase();
            if(extension.contains("png")) {
                Engine.visonmix_fgBitmap = ImageUtils.decodeFile(imagePath);
                finish_visionmaskActivity(true);
                return;
            }
            bitmap = ImageUtils.decodeFile(imagePath);

            if(savedInstanceState == null) {
                asyncTaskMasking = new AsyncTask_getMask();
                asyncTaskMasking.execute();
            }
        } else {
            loadStatus();
        }

        brushDrawingView.setBrushSize(BrushDrawingView.BRUSH_MAX / 2);
        brushDrawingView.setHardnessSize(BrushDrawingView.BRUSH_MAX/2);

        navigatorView.setBrushSize((int)BrushDrawingView.BRUSH_MAX / 2);

        ivPhoto = (ImageView) findViewById(R.id.iv_photo);
        rgBrushType = (RadioGroup)findViewById(R.id.rg_brush_type);
        sbStrokeWidth = (Slider)findViewById(R.id.sb_stroke_width);
        sbStrokeHardness = (Slider)findViewById(R.id.sb_stroke_hardness);

        rgBrushType.check(brushType);
        brushDrawingView.setMode(BrushDrawingView.DRAWING_MODE_HARD);

        ivPhoto.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (wDrawingPane == 0 || hDrawingPane == 0 || (wDrawingPane != right-left) || (hDrawingPane !=bottom-top)) {
                    wDrawingPane = right - left;
                    hDrawingPane = bottom - top;
                    wSrc = bitmap.getWidth();
                    hSrc = bitmap.getHeight();
                    scale0 = Math.min(((float) hDrawingPane) / bitmap.getHeight(), ((float) wDrawingPane) / bitmap.getWidth());

                    matrix.reset();
                    matrix.postScale(scale0, scale0);
                    xInDrawingPane = (int)Math.abs(wSrc* scale0 - wDrawingPane)/2;
                    yInDrawingPane = (int)Math.abs(hSrc* scale0 - hDrawingPane)/2;
                    matrix.postTranslate(xInDrawingPane, yInDrawingPane);

                    brushDrawingView.setDestCacheSize(wSrc, hSrc, left, top, right, bottom);
                    brushDrawingView.setMatrix(matrix);
                    navigatorView.setParentSize(left, top, right, bottom);
                    navigatorView.setBitmap(bitmap, matrix);


                    if(bitmapMask != null)
                        brushDrawingView.setBitmapMask(bitmapMask);

                    ivPhoto.setImageBitmap(bitmap);
                    ivPhoto.setImageMatrix(matrix);

                    matrix0.set(matrix);

                    brushDrawingView.setOnTouchListener(VisionmaskActivity.this);
                }
            }
        });

        sbStrokeWidth.setOnValueChangeListener(sliderChangeListener);
        sbStrokeHardness.setOnValueChangeListener(sliderChangeListener);
    }

    Slider.OnValueChangeListener sliderChangeListener = new Slider.OnValueChangeListener() {
        @Override
        public void onProgressChanged(Slider slider, int progress, boolean fromUser) {
            if(slider.getId() == R.id.sb_stroke_width)
            {
                brushDrawingView.setBrushSize(progress);
                navigatorView.setBrushSize(progress);
            } else if(slider.getId() == R.id.sb_stroke_hardness){
                brushDrawingView.setHardnessSize(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(Slider slider) {

        }

        @Override
        public void onStopTrackingTouch(Slider seekBar) {

        }
    };

    public void onClickBrushType(View v) {
        brushType = v.getId();
        if (v.getId() == R.id.rd_drawer) {
            brushDrawingView.setMode(BrushDrawingView.DRAWING_MODE_LIGHT);
            sbStrokeWidth.setProgress((int) brushDrawingView.getBrushSize());
        }else if(v.getId() == R.id.rd_drawer_hard) {
            brushDrawingView.setMode(BrushDrawingView.DRAWING_MODE_HARD);
            sbStrokeWidth.setProgress((int) brushDrawingView.getBrushSize());
        } else if (v.getId() == R.id.rd_eraser) {
            brushDrawingView.setMode(BrushDrawingView.ERASING_MODE);
            sbStrokeWidth.setProgress((int) brushDrawingView.getBrushSize());
        }
    }

    public void onButtonClick(View v)
    {
        switch (v.getId())
        {
            case R.id.button_accept:
                v.setEnabled(false);
                AsyncTask_Matting mattingTask = new AsyncTask_Matting();
                mattingTask.execute();
                break;
            case R.id.button_close:
                bitmap.recycle();
                bitmapMask.recycle();
                finish();
                break;
            case R.id.button_undo:
                brushDrawingView.undo();
                break;
            case R.id.button_reset:
                brushDrawingView.clearAll();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }

    public String saveMaskImage(Bitmap bitmap, String folderName, String imageName) {
        String selectedOutputPath = "";
        if (MainActivity.isSDCARDMounted()) {
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "Failed to create directory");
                }
            }
            // Create a media file name
            selectedOutputPath = mediaStorageDir.getPath() + File.separator + imageName;
            Log.d(TAG, "selected camera path " + selectedOutputPath);
            File file = new File(selectedOutputPath);
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();

                //227save
                com.l.l.l.f.saveImage(selectedOutputPath);
                /////////
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return selectedOutputPath;
    }

    public void saveStatus() {
        SharedPref.putString(this, "imagePath", imagePath);

        String imageName = "maskimg_temp.png";
        String maskPath = saveMaskImage(bitmapTrimap, MainActivity.WORK_PHOTO_DIR, imageName);

        SharedPref.putString(this, "maskPath", maskPath);
        SharedPref.putString(this, "strokes", brushDrawingView.toString());
    }

    public void finish_visionmaskActivity(boolean bPNGLoaded)
    {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("status", "OK");
        returnIntent.putExtra("bPNGLoaded", bPNGLoaded);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
    public boolean loadStatus() {
        imagePath = SharedPref.getString(this, "imagePath", "");
        if (imagePath == "") return false;

        bitmap = ImageUtils.decodeFile(imagePath);
        String maskPath = SharedPref.getString(this, "maskPath", "");

        BitmapFactory.Options options1 = new BitmapFactory.Options();
        options1.inSampleSize = 1;
        options1.inDither=false;                     //Disable Dithering mode
//        options1.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared

        bitmapTrimap = ImageUtils.decodeFile(maskPath);
//        bitmapMask = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        bitmapMask = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ALPHA_8);
        Engine.getIntance().magicResizeTrimap(bitmapTrimap, bitmapMask, false, false);

        String jstr = SharedPref.getString(this, "strokes", "");
        brushDrawingView.fromString(jstr);

        return true;
    }

    ////////////////////
    ///////////////////
//    public void showWaitDialog(String strMsg) {
////        //new ProgDialog().show(getFragmentManager(), "sample");
////        progressDialog=new ProgressDialog(VisionmaskActivity.this);
////
////        progressDialog.setMessage(strMsg);
////        progressDialog.setIndeterminate(true);
////        progressDialog.setCancelable(false);
////        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
////        progressDialog.show();
//        WaitWindow.ShowWindow(this);
//
//
//    }
//
//    public void hideWaitDialog() {
//        WaitWindow.HideWindow();
//    }
    private class AsyncTask_getMask extends AsyncTask<Void, Void, Void> {
        Boolean bSuccess = true;
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HideWaitDialog();
            if(!bSuccess)
            {
                brushDrawingView.setBitmapMask(bitmapMask);
                brushDrawingView.invalidate();
            }
            else {
                brushDrawingView.setBitmapMask(bitmapMask);
                brushDrawingView.invalidate();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            bitmapTrimap = Bitmap.createBitmap(224,224, Bitmap.Config.ARGB_8888);
//            int nRet = Engine.getIntance().magicTrimapMask(bitmap, bitmapTrimap);
            int nRet = Engine.getIntance().magicTrimapMaskAndInfoflow(bitmap, bitmapTrimap);
            if(nRet < 0) {
                bSuccess = false;
                bitmapMask = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ALPHA_8);
                return null;
            }

            bitmapMask = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ALPHA_8);
            Engine.getIntance().magicResizeTrimap(bitmapTrimap, bitmapMask, false, false);
//            bitmapMask = ImageUtils.getBitmapFromGrayMaskBytes(imgMasked, bitmap.getWidth(), bitmap.getHeight());
            return null;
        }
    };

    private class AsyncTask_Matting extends AsyncTask<String, String, String>{
        boolean bSuccess = true;
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();

        }

        @Override
        protected void onPostExecute(String aVoid) {
            HideWaitDialog();

            if(!bSuccess)
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("Status", -1);
                setResult(Activity.RESULT_CANCELED, returnIntent);
                VisionmaskActivity.this.finish();
            }
            else {
                finish_visionmaskActivity(false);
            }
        }

        @Override
        protected String doInBackground(String... voids) {
            saveStatus();
            bitmapMaskEdited = brushDrawingView.mergeToCache();

            if (Engine.visonmix_fgBitmap!= null) {
                Engine.visonmix_fgBitmap.recycle();
            }

//            Engine.visonmix_fgBitmap  = Engine.getIntance().magicGetMattingImage(bitmap, bitmapMaskEdited);

//            bitmapMaskEdited = Engine.getIntance().magicGetOneChannel(bitmapMaskEdited, 3);
//            publishProgress("");
            Engine.visonmix_fgBitmap  = Engine.getIntance().magicGetMattingImageGuidefilterOnly(bitmap, bitmapMaskEdited);
            bitmapMaskEdited.recycle();
            bitmap.recycle();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            bitmapMask.recycle();
            bitmap.recycle();
         //   System.gc();
        }
    };


    Matrix savedMatrix = new Matrix();
    int mode = NONE;

    PointF start=new PointF();
    PointF mid=new PointF();
    float oldDist=0;

    final private float minimumScale = 0.5f;
    final private float maximumScale = 5.0f;
    private float scale = scale0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        ivPhoto.setScaleType(ImageView.ScaleType.MATRIX);
        //llSeekbarContainer.setVisibility(View.GONE);

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                    mode = DRAW;
                navigatorContainer.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)navigatorContainer.getLayoutParams();
                params.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                start.set(event.getX(), event.getY());

                navigatorView.setBitmapMask(brushDrawingView.tempBitmap);
                navigatorView.translate(0, -event.getX(), -event.getY());
                return brushDrawingView.onTouchEvent(event);
            case MotionEvent.ACTION_POINTER_DOWN:
                navigatorContainer.setVisibility(View.INVISIBLE);
                brushDrawingView.ResetPath();
                oldDist = spacing(event);

                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    start.set(event.getX(), event.getY());
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                navigatorContainer.setVisibility(View.INVISIBLE);
                if (mode == DRAW) {
                        mode = NONE;
                    return brushDrawingView.onTouchEvent(event);
                } else {
                    RefreshLayoutPos();
                }
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                        matrix.set(savedMatrix);
                        matrix.postTranslate(event.getX() - start.x,
                                event.getY() - start.y);
                }
                else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float deltaScale = newDist / oldDist;
                        matrix.postScale(deltaScale, deltaScale, mid.x, mid.y);
                        matrix.postTranslate(event.getX() - start.x,
                                event.getY() - start.y);
                    }

                }
                else if(mode == DRAW){
                    navigatorView.translate(0, -event.getX(), -event.getY());
                    navigatorView.PositionRefresh(navigatorContainer, (int)event.getX(), (int)event.getY());
                    return brushDrawingView.onTouchEvent(event);
                }
                break;
        }
        ivPhoto.setImageMatrix(matrix);
        brushDrawingView.setMatrix(matrix);
        navigatorView.setBitmap(null, matrix);
        return true; // indicate event was handled
    }

    public void RefreshLayoutPos()
    {
        float x[] = new float[9];
        matrix.getValues(x);
        float scale = x[Matrix.MSCALE_X];
        float transX = x[Matrix.MTRANS_X];
        float transY = x[Matrix.MTRANS_Y];

        if(scale > 3) {
            matrix.postScale(3/scale, 3/scale, mid.x, mid.y);
            matrix.getValues(x);
            scale = x[Matrix.MSCALE_X];
            transX = x[Matrix.MTRANS_X];
            transY = x[Matrix.MTRANS_Y];
        }
        if(scale < scale0)
            scale = scale0;

        xInDrawingPane = (int)Math.abs(wSrc* scale - wDrawingPane)/2;
        yInDrawingPane = (int)Math.abs(hSrc* scale - hDrawingPane)/2;
        float newTransX = transX;
        float newTransY = transY;
        if( wSrc * scale <= wDrawingPane && hSrc * scale <= hDrawingPane )
        {
            newTransX = xInDrawingPane;
            newTransY = yInDrawingPane;
        }else if(wSrc * scale > wDrawingPane && hSrc * scale <= hDrawingPane )
        {
            newTransY = yInDrawingPane;
            if(transX > 0)
                newTransX = 0;
            if(transX < wDrawingPane-(wSrc * scale))
                newTransX = wDrawingPane-(wSrc * scale);

        }else if(wSrc * scale <= wDrawingPane && hSrc * scale >  hDrawingPane){
            newTransX = xInDrawingPane;
            if(transY > 0)
                newTransY = 0;
            if(transY < hDrawingPane-(hSrc * scale))
                newTransY = hDrawingPane-(hSrc * scale);
        }else{
            if(transX > 0)
                newTransX = 0;
            if(transX < wDrawingPane-(wSrc * scale))
                newTransX = wDrawingPane-(wSrc * scale);
            if(transY > 0)
                newTransY = 0;
            if(transY < hDrawingPane-(hSrc * scale))
                newTransY = hDrawingPane-(hSrc * scale);
        }

        matrix.reset();
        matrix.postScale(scale, scale);
        matrix.postTranslate(newTransX, newTransY);
    }
}
