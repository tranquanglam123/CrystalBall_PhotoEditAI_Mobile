package com.samhung.crystalball.photoeditor.Photoshop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.CustomAlertDialog;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoAcne;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoCartoon;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoClarity;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoCrop;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoEnhance;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoFaceBeauty;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoHeight;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoRotate;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoBeauty;
import com.samhung.crystalball.photoeditor.Photoshop.Dialogs.Dialog_PhotoSoftfocus;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.VisionMix.Activities.VisionmixActivity;
import com.samhung.crystalball.photoeditor.VisionMix.Dialogs.VisionPreviewDialog;

import org.CrystalBall.Engine;

import java.io.File;
import java.io.FileDescriptor;

public class PhotoeditActivity extends BaseActivity  implements View.OnTouchListener{

    HorizontalScrollView hsv_toolbar_edit = null;
    HorizontalScrollView hsv_toolbar_beauty = null;
    ImageView iv_photo = null;

    Bitmap bitmap = null;
    Matrix matrix = new Matrix();
    int wSrc = 0;
    int hSrc = 0;
    int wDrawingPane = 0;
    int hDrawingPane = 0;
    int xInDrawingPane =0;
    int yInDrawingPane =0;
    int layout_Left = 0;
    int layout_Top = 0;
    float scale0 =1;

    Rect m_faceRect = null;
//    Matrix matrix0 = new Matrix();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photoedit);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String imagePath = getIntent().getExtras().getString("selectedImagePath");
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 1;
//        options.inDither = false;                     //Disable Dithering mode
//        bitmap = BitmapFactory.decodeFile(imagePath, options);
        bitmap = ImageUtils.decodeFile(imagePath);

        landmark_task = new AsyncTask_getLandmark();
        landmark_task.execute();

        hsv_toolbar_beauty = (HorizontalScrollView)findViewById(R.id.hsv_sub_toolbar_beauty);
        hsv_toolbar_edit = (HorizontalScrollView)findViewById(R.id.hsv_sub_toolbar_edit);
        hsv_toolbar_beauty.setVisibility(View.INVISIBLE);
        hsv_toolbar_edit.setVisibility(View.VISIBLE);
        iv_photo = (ImageView)findViewById(R.id.imageview_photo);

        iv_photo.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (wDrawingPane == 0 || hDrawingPane == 0 || (wDrawingPane != right-left) || (hDrawingPane !=bottom-top)) {
                    wDrawingPane = right - left;
                    hDrawingPane = bottom - top;
                    layout_Left = left;
                    layout_Top = top;
                    refresh_ImageView();
                }
            }
        });
    }

    public void refresh_ImageView()
    {
        wSrc = bitmap.getWidth();
        hSrc = bitmap.getHeight();
        scale0 = Math.min(((float) hDrawingPane) /hSrc, ((float) wDrawingPane) /wSrc);

        matrix.reset();
        matrix.postScale(scale0, scale0);
        xInDrawingPane = (int)Math.abs(wSrc* scale0 - wDrawingPane)/2;
        yInDrawingPane = (int)Math.abs(hSrc* scale0 - hDrawingPane)/2;
        matrix.postTranslate(xInDrawingPane, yInDrawingPane);

        iv_photo.setImageBitmap(bitmap);
        iv_photo.setImageMatrix(matrix);
        iv_photo.setOnTouchListener(this);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        quit_Activity();
    }

    public  void quit_Activity()
    {
        CustomAlertDialog.Builder alert_builder = new CustomAlertDialog.Builder(this);
        alert_builder.setMessage("작업을 중지하겠습니까?\n작업하던 내용이 모두 삭제됩니다.").setCancelable(true).setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                File mediaStorageDir = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MainActivity.WORK_PHOTO_DIR);
                if(mediaStorageDir.exists())
                {
                    File[] tmpFiles = mediaStorageDir.listFiles();
                    for (File file:tmpFiles
                            ) {
                        file.delete();
                    }
                    mediaStorageDir.delete();
                }
                finish();
            }
        }).setNegativeButton("아니", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        CustomAlertDialog alertDialog = alert_builder.create();
        alertDialog.show();
    }

    public  void quit_Activity_old()
    {
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
        alert_confirm.setMessage("작업을 중지하겠습니까?\n작업하던 내용이 모두 삭제됩니다.").setCancelable(false).setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        File mediaStorageDir = new File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MainActivity.WORK_PHOTO_DIR);
                        if(mediaStorageDir.exists())
                        {
                            File[] tmpFiles = mediaStorageDir.listFiles();
                            for (File file:tmpFiles
                                    ) {
                                file.delete();
                            }
                            mediaStorageDir.delete();
                        }
                        finish();
                    }
                }).setNegativeButton("아니",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        AlertDialog alert = alert_confirm.create();
        alert.show();
    }
    public void onClickMainMenuItem(View v)
    {
        if(v.getId() == R.id.rb_photo_edit) {
            hsv_toolbar_beauty.setVisibility(View.INVISIBLE);
            hsv_toolbar_edit.setVisibility(View.VISIBLE);
        }else if(v.getId() == R.id.rb_photo_beauty) {
            hsv_toolbar_beauty.setVisibility(View.VISIBLE);
            hsv_toolbar_edit.setVisibility(View.INVISIBLE);
        }else if(v.getId() == R.id.iv_save) {
//            String fileName = ImageUtils.saveImage(bitmap, "", false);
//            Toast.makeText(PhotoeditActivity.this, fileName, Toast.LENGTH_SHORT).show();
//            finish();
            VisionPreviewDialog vpd = new VisionPreviewDialog(this, bitmap, previewDialogClickListener,1);
            vpd.show();
        }else if(v.getId() == R.id.iv_back) {
            quit_Activity();
        }

    }

    public boolean checkFaceDetectSucced()
    {
        if(m_nFaceCount < 1) {
            Toast.makeText(PhotoeditActivity.this, "얼굴검출이 실패하였습니다. ", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    public void onClickSubMenuItem(View v)
    {
        switch(v.getId())
        {
            case R.id.button_crop:
                Dialog_PhotoCrop dlg_crop = new Dialog_PhotoCrop(this, bitmap, dlgClickListener);
                dlg_crop.show();
                break;
            case R.id.button_rotate:
                Dialog_PhotoRotate dlg_rotate= new Dialog_PhotoRotate(this, bitmap, dlgClickListener);
                dlg_rotate.show();
                break;
            case R.id.button_eye_size:
                if(!checkFaceDetectSucced()) return;
                Dialog_PhotoBeauty dlg_eye = new Dialog_PhotoBeauty(this, "eye",  bitmap,m_nFaceCount, dlgClickListener);
                dlg_eye.show();
                break;
            case R.id.button_faceslim:
                if(!checkFaceDetectSucced()) return;
                Dialog_PhotoBeauty dlg_face = new Dialog_PhotoBeauty(this, "face",  bitmap,m_nFaceCount, dlgClickListener);
                dlg_face.show();
                break;
            case R.id.button_jaw:
                if(!checkFaceDetectSucced()) return;
                Dialog_PhotoBeauty dlg_jaw = new Dialog_PhotoBeauty(this, "jaw",  bitmap,m_nFaceCount, dlgClickListener);
                dlg_jaw.show();
                break;
            case R.id.button_lips:
                if(!checkFaceDetectSucced()) return;
                Dialog_PhotoBeauty dlg_lips = new Dialog_PhotoBeauty(this, "lip",  bitmap,m_nFaceCount, dlgClickListener);
                dlg_lips.show();
                break;
            case R.id.button_norse:
                if(!checkFaceDetectSucced()) return;
                Dialog_PhotoBeauty dlg_norse = new Dialog_PhotoBeauty(this, "norse",  bitmap,m_nFaceCount, dlgClickListener);
                dlg_norse.show();
                break;
            case R.id.button_forehead:
                if(!checkFaceDetectSucced()) return;
                Dialog_PhotoBeauty dlg_forehead = new Dialog_PhotoBeauty(this, "forehead",  bitmap,m_nFaceCount, dlgClickListener);
                dlg_forehead.show();
                break;
            case R.id.button_height:
                Dialog_PhotoHeight dlg_height = new Dialog_PhotoHeight(this, bitmap, dlgClickListener);
                dlg_height.show();
                break;
            case R.id.button_beauty:
//                Dialog_PhotoBeauty dlg_faceBeauty = new Dialog_PhotoBeauty(this,"beauty", bitmap, 1, dlgClickListener);
                Dialog_PhotoFaceBeauty dlg_faceBeauty = new Dialog_PhotoFaceBeauty(this, bitmap, dlgClickListener);
                dlg_faceBeauty.show();
                break;
            case R.id.button_acne:
                Dialog_PhotoAcne dlg_faceAcne = new Dialog_PhotoAcne(this, bitmap, dlgClickListener);
                dlg_faceAcne.show();
                break;
            case R.id.button_enhance:
                Dialog_PhotoEnhance dlg_enhance = new Dialog_PhotoEnhance(this, bitmap, dlgClickListener);
                dlg_enhance.show();
                break;
            case R.id.button_clarity:
                Dialog_PhotoClarity dlg_clarity = new Dialog_PhotoClarity(this, bitmap, dlgClickListener);
                dlg_clarity.show();
                break;
            case R.id.button_cartoon:
                Dialog_PhotoCartoon dlg_cartoon = new Dialog_PhotoCartoon(this, bitmap, dlgClickListener);
                dlg_cartoon.show();
                break;
            case R.id.button_softfocus:
                Dialog_PhotoSoftfocus dlg_softFocus = new Dialog_PhotoSoftfocus(this, bitmap, dlgClickListener);
                dlg_softFocus.show();
                break;
        }
    }

    DialogInterface.OnClickListener dlgClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == DialogInterface.BUTTON_NEGATIVE)
            {
                dialog.dismiss();
            }else if(which == DialogInterface.BUTTON_POSITIVE) {

                if(((FullScreenDialog)dialog).mResultBitmap!=null)
                    bitmap = Bitmap.createBitmap(((FullScreenDialog)dialog).mResultBitmap);
                dialog.dismiss();
                    landmark_task = new AsyncTask_getLandmark();
                    landmark_task.execute();

            }
            refresh_ImageView();
        }
    };

    Matrix savedMatrix = new Matrix();
    int mode = NONE;

    PointF start=new PointF();
    PointF mid=new PointF();
    float oldDist=0;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        iv_photo.setScaleType(ImageView.ScaleType.MATRIX);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = DRAG;
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
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
                mode = NONE;
                RefreshLayoutPos();
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
                break;
        }
        iv_photo.setImageMatrix(matrix);
        return true;
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

    DialogInterface.OnClickListener previewDialogClickListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == VisionPreviewDialog.BTN_BACK)
            {
                dialog.dismiss();
            }
            else if(which == VisionPreviewDialog.BTN_SAVE || which == VisionPreviewDialog.BTN_PHEDIT || which == VisionPreviewDialog.BTN_STICKER)
            {
                AsyncTask_saveResult asyncTask_saveResult = new AsyncTask_saveResult();
                asyncTask_saveResult.execute(dialog);
            }

        }
    };

    int[] m_landmarkPoints = null;

    AsyncTask_getLandmark landmark_task;
    int m_nFaceCount = 0;
    private class AsyncTask_getLandmark extends AsyncTask<Void, Void, Void> {
        Boolean bSuccess = true;
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HideWaitDialog();
//            if(m_nFaceCount < 1)
//            {
//                Toast.makeText(PhotoeditActivity.this, "얼굴검출이 실패하였습니다. ", Toast.LENGTH_SHORT).show();
//            }
            //drawingView.draw_Landmarks(m_landmarkPoints);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int[] ret = Engine.getIntance().FaceDetect(bitmap);
            if(ret == null) {m_nFaceCount = 0; return null;}
            m_nFaceCount = ret[0];
            if(m_nFaceCount == 0) return null;
            m_nFaceCount = Engine.getIntance().FaceLandmark(bitmap);
            return null;
        }
    }

    private class AsyncTask_saveResult extends AsyncTask<DialogInterface, Void, String> {
        Boolean bSuccess = true;
        DialogInterface mDialog = null;
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();
        }

        @Override
        protected void onPostExecute(String fileName) {
            HideWaitDialog();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("path", fileName);
            returnIntent.putExtra("sendto", ((VisionPreviewDialog)mDialog).m_btnWhich);
            setResult(Activity.RESULT_OK, returnIntent);

            mDialog.dismiss();

            finish();
        }

        @Override
        protected String doInBackground(DialogInterface... voids) {
            DialogInterface dialog = (DialogInterface)(voids[0]);
            mDialog = dialog;
            String fileName = ImageUtils.saveImage(((VisionPreviewDialog) dialog).mergeBitmap, "", false);
            return fileName;
        }
    };
}
