package com.samhung.crystalball.photoeditor.VisionMix.Activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.VisionMix.Views.CroppingView;

import org.CrystalBall.Engine;


public class BackgroundMagicActivity extends BaseActivity {
    private final String TAG = "BackgroundMagicActivity";

    ImageView ivPhoto;

    int wSrc = 0;
    int hSrc = 0;
    int wDrawingPane = 0;
    int hDrawingPane = 0;
    int xInDrawingPane =0;
    int yInDrawingPane =0;
    float scale0 =1;

    String imagePath;

    Matrix matrix = new Matrix();

    CroppingView croppingView;

    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visionmix_backadd);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 1;
        options.inDither = false;                     //Disable Dithering mode
        options.inPurgeable = true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared

        int nMode = getIntent().getExtras().getInt("mode");
        if(nMode == 0) {
            imagePath = getIntent().getExtras().getString("selectedImagePath");
            //bitmap = BitmapFactory.decodeFile(imagePath, options);
            bitmap = ImageUtils.decodeFile(imagePath);
        }
        else
        {
            imagePath = getIntent().getExtras().getString("selectedImagePath");
            try {
                bitmap = BitmapFactory.decodeStream(getAssets().open(imagePath));
            }catch (Exception ex) {}
        }

        ivPhoto = (ImageView) findViewById(R.id.iv_photo);
        croppingView = (CroppingView)findViewById(R.id.cropping_view);

        ivPhoto.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (wDrawingPane == 0 || hDrawingPane == 0 || (wDrawingPane != right-left) || (hDrawingPane !=bottom-top)) {
                    wDrawingPane = right - left;
                    hDrawingPane = bottom - top;
                    wSrc = bitmap.getWidth();
                    hSrc = bitmap.getHeight();

                    scale0 = Math.min(((float) hDrawingPane) / bitmap.getHeight(), ((float) wDrawingPane) / bitmap.getWidth());
                    scale0 = scale0 * (float)0.9;
                    matrix.reset();
                    matrix.postScale(scale0, scale0);

                    xInDrawingPane = (int)Math.abs(wSrc* scale0 - wDrawingPane)/2;
                    yInDrawingPane = (int)Math.abs(hSrc* scale0 - hDrawingPane)/2;

                    croppingView.initRect(xInDrawingPane, yInDrawingPane, wSrc*scale0, hSrc*scale0);

                    matrix.postTranslate(xInDrawingPane, yInDrawingPane);

                    ivPhoto.setImageBitmap(bitmap);
                    ivPhoto.setImageMatrix(matrix);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onClick(View v) {
        if (v.getId() == R.id.iv_accept) {
            RectF rect = new RectF(croppingView.getRect());

            Engine.visonmix_bgBitmap = Bitmap.createBitmap((int)(rect.width()/scale0), (int)(rect.height()/scale0), bitmap.getConfig());
            Canvas canvas = new Canvas(Engine.visonmix_bgBitmap);
            Matrix mat = new Matrix();
            mat.postTranslate(-(rect.left-xInDrawingPane)/scale0, -(rect.top-yInDrawingPane)/scale0);
            canvas.drawBitmap(bitmap, mat, new Paint());
//            bitmap.recycle();
            Intent returnIntent = new Intent();
            returnIntent.putExtra("status", "OK");
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        } else if (v.getId() == R.id.iv_cancel) {

            finish();
        } else if (v.getId() == R.id.iv_undo) {

        }

    }

}
