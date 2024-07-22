package com.samhung.crystalball.photoeditor.VisionMix.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.CustomAlertDialog;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.GalleryUtils;
import com.samhung.crystalball.photoeditor.VisionMix.Dialogs.Dialog_DefaultBackground;
import com.samhung.crystalball.photoeditor.VisionMix.Dialogs.VisionPreviewDialog;
import com.samhung.crystalball.photoeditor.VisionMix.ForegroundTouchListener;
import com.samhung.crystalball.photoeditor.VisionMix.Views.BackgroundView;
import com.samhung.crystalball.photoeditor.VisionMix.Views.ForegroundView;
import com.samhung.crystalball.photoeditor.Widget.NavigatorView;

import org.CrystalBall.Engine;

import java.io.File;
import java.io.InputStream;

public class VisionmixActivity extends BaseActivity {

    static final int MY_REQUEST_VISIONMASK = 0x300;
    static final int MY_REQUEST_BG_MAGIC = 0x301;
    static final int MY_REQUEST_DEFAULT_GALLERY = 0x302;

    static final int SUB_MENU_ERASER = 0x01;
    static final int SUB_MENU_BACK_BLUR = 0x02;

//    ImageView ivBackground;
    BackgroundView ivBackground;
    ForegroundView fvForeground;
    public int scrWidth = 0;
    public int scrHeight = 0;

    float bgScale = 0;
    Matrix bgMatrix = null;

    public boolean m_bEraseMode = false;

    LinearLayout ll_subtoolmenu = null;
    RelativeLayout rl_subtoolbar = null;
    RelativeLayout rl_subBlurMenu = null;
    RelativeLayout rl_subEraserMenu = null;
    LayoutInflater m_inflater = null;

    SeekBar sbStrokeWidth = null;
    public RelativeLayout rl_navilayout = null;
    NavigatorView navigatorView = null;

    public Bitmap bitmapBackground = null;
    public Bitmap efftBitmap_draw = null;
    ImageView ivEffect = null;
    public float scale0 = 1.0f;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visionmix);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));

        boolean bPNGLoaded = getIntent().getExtras().getBoolean("bPNGLoaded");
        InitControls();
        if(bPNGLoaded)
            findViewById(R.id.BTN_fvEdit).setVisibility(View.GONE);
    }

    void InitControls() {
        fvForeground = (ForegroundView)findViewById(R.id.fv_foreground);
        ivBackground = (BackgroundView) findViewById(R.id.iv_background);
        ivEffect = (ImageView)findViewById(R.id.iv_effect);
        ll_subtoolmenu = (LinearLayout)findViewById(R.id.layout_sub_total);
        rl_subtoolbar = (RelativeLayout)findViewById(R.id.layout_sub_toolbar);
        navigatorView = (NavigatorView)findViewById(R.id.naviView);
        rl_navilayout = (RelativeLayout)findViewById(R.id.rl_naviview);
        rl_subtoolbar.setBackgroundColor(Color.WHITE);

        m_inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rl_subBlurMenu = (RelativeLayout)m_inflater.inflate(R.layout.sub_menu_visionmix_blur, null);
        rl_subEraserMenu = (RelativeLayout)m_inflater.inflate(R.layout.sub_menu_visionmix_eraser, null);

        sbStrokeWidth = (SeekBar)rl_subBlurMenu.findViewById(R.id.sb_stroke_width);
        sbStrokeWidth.setOnSeekBarChangeListener(seekBarChangeListener);
        ((SeekBar)rl_subEraserMenu.findViewById(R.id.sb_brush_size)).setOnSeekBarChangeListener(seekBarChangeListener);
        ((SeekBar)rl_subEraserMenu.findViewById(R.id.sb_hardness)).setOnSeekBarChangeListener(seekBarChangeListener);
        hide_subMenu();

        fvForeground.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
//                fvForeground.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
        {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (scrWidth == 0 || scrHeight == 0 || (scrWidth != right-left) || (scrHeight !=bottom-top)) {
                    scrWidth = right - left;
                    scrHeight = bottom - top;

                    if(Engine.visonmix_fgBitmap!=null) {
                        int w = Engine.visonmix_fgBitmap.getWidth();
                        int h = Engine.visonmix_fgBitmap.getHeight();
                        Matrix matrix = new Matrix();
                        scale0 = Math.min(((float) scrHeight) / Engine.visonmix_fgBitmap.getHeight(), ((float) scrWidth) / Engine.visonmix_fgBitmap.getWidth());
                        if (bgMatrix != null) {
                            //matrix.set(bgMatrix);
                            float[] vv = new float[9];
                            bgMatrix.getValues(vv);
                            scale0 = vv[Matrix.MSCALE_X];

                            float scale = Math.min(((float)Engine.visonmix_bgBitmap.getWidth() / Engine.visonmix_fgBitmap.getWidth()),
                                    ((float)Engine.visonmix_bgBitmap.getHeight() / Engine.visonmix_fgBitmap.getHeight()));
                            scale0 *= scale;
                            matrix.postScale(scale0, scale0);
                            matrix.postTranslate(-(int) (w * scale0 - scrWidth) / 2, -(int) (h * scale0 - scrHeight) / 2);
//                            matrix.postScale(scale0, scale0);
//                            matrix.postTranslate(-(int) (w * scale0 - scrWidth) / 2, -(int) (h * scale0 - scrHeight) / 2);

                        } else {
                            matrix.postScale(scale0, scale0);
                            matrix.postTranslate((int) Math.abs(w * scale0 - scrWidth) / 2, (int) Math.abs(h * scale0 - scrHeight) / 2);
                        }
                        fvForeground.clearAll();
                        fvForeground.setBitmap(Engine.visonmix_fgBitmap);

                        fvForeground.setMatrix(matrix);
                        fvForeground.setOnTouchListener(new ForegroundTouchListener(VisionmixActivity.this,fvForeground, navigatorView, scale0, matrix));
                        ivBackground.RefreshView();

                        navigatorView.setParentSize(left, top, right, bottom);
                        navigatorView.setBitmapMask(fvForeground.tempBitmap);
                    }

                    if(Engine.visonmix_bgBitmap!=null) {
                        int w = Engine.visonmix_bgBitmap.getWidth();
                        int h = Engine.visonmix_bgBitmap.getHeight();

                        bgScale = Math.min(((float) scrHeight) / Engine.visonmix_bgBitmap.getHeight(), ((float) scrWidth) / Engine.visonmix_bgBitmap.getWidth());
                        bgMatrix = new Matrix();
                        bgMatrix.reset();
                        bgMatrix.postScale(bgScale, bgScale);
                        bgMatrix.postTranslate((int) Math.abs(w * bgScale - scrWidth) / 2, (int) Math.abs(h * bgScale - scrHeight) / 2);

                        ivBackground.setBitmap(Engine.visonmix_bgBitmap);
                        ivBackground.setMatrix(bgMatrix);
                        navigatorView.setBitmap(Engine.visonmix_bgBitmap, bgMatrix);
                        if (Engine.visonmix_fgBitmap != null) {
                            Matrix fMatrix = new Matrix();
                            fMatrix.setScale(bgScale, bgScale);
                            fMatrix.postTranslate(-(int) (Engine.visonmix_fgBitmap.getWidth() * bgScale - scrWidth) / 2, -(int) (Engine.visonmix_fgBitmap.getHeight() * bgScale - scrHeight) / 2);
                            fvForeground.setMatrix(fMatrix);
                            fvForeground.setOnTouchListener(new ForegroundTouchListener(VisionmixActivity.this, fvForeground, navigatorView, bgScale, fMatrix));
                        }
                    }
                }
            }
        });
    }

    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(m_bEraseMode){
                if(seekBar.getId() == R.id.sb_brush_size)
                    fvForeground.setEraserSize(progress>5?progress:5);
                if(seekBar.getId() == R.id.sb_hardness)
                    fvForeground.setBrushHardness(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(m_SubMenuType == SUB_MENU_BACK_BLUR){
                if(Engine.visonmix_bgBitmap == null) return;
                float rad = (seekBar.getProgress() / 4) >0 ? seekBar.getProgress() / 4 : 1;
                Bitmap tmp = Bitmap.createBitmap(blurTmpBitmap);
                ImageUtils.Blur_Effect(VisionmixActivity.this,blurTmpBitmap, tmp, rad);
                VisionmixActivity.this.bitmapBackground = Bitmap.createScaledBitmap(tmp, Engine.visonmix_bgBitmap.getWidth(), Engine.visonmix_bgBitmap.getHeight(), true);
//                    ivBackground.setImageBitmap(bitmapBackground);
                ivBackground.setBitmap(bitmapBackground);
                ivBackground.RefreshView();
                navigatorView.setBitmap(bitmapBackground, bgMatrix);

                if(m_bIsDefaultBackground) {
                    tmp = Bitmap.createBitmap(blurTmpEffectBitmap);
                    ImageUtils.Blur_Effect(VisionmixActivity.this, blurTmpEffectBitmap, tmp, rad);
                    VisionmixActivity.this.efftBitmap_draw = Bitmap.createScaledBitmap(tmp, effectBitmap.getWidth(), effectBitmap.getHeight(), true);
                    ivEffect.setImageBitmap(efftBitmap_draw);
                }
            }
        }
    };
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        quit_Activity();
    }

    Bitmap blurTmpBitmap = null;
    Bitmap blurTmpEffectBitmap = null;
    public void onButtonClick(View v)
    {
        switch(v.getId())
        {
            case R.id.BTN_fvEdit:
                gotoVisionMaskActivity();
                break;
            case R.id.BTN_bgAdd:
                openMedia_Background("배경화상추가");
                break;
            case R.id.BTN_fvEraser:
                show_subMenu(this, SUB_MENU_ERASER);
                break;
            case R.id.BTN_pngSave:
                AsyncTask_savePNG pngSaver = new AsyncTask_savePNG();
                pngSaver.execute();
//                ImageUtils.saveImage(Engine.visonmix_fgBitmap, "", true);
//                Toast.makeText(this, "전경화상이 PNG화일로 보관되였습니다.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.BTN_bgBlur:
                if(Engine.visonmix_bgBitmap != null) {
                    int w = Engine.visonmix_bgBitmap.getWidth();
                    int h = Engine.visonmix_bgBitmap.getHeight();
                    blurTmpBitmap = Bitmap.createScaledBitmap(Engine.visonmix_bgBitmap, (int)(w * bgScale /2), (int)(h*bgScale/2), false );
                    if(m_bIsDefaultBackground)
                        blurTmpEffectBitmap = Bitmap.createScaledBitmap(effectBitmap, (int)(w * bgScale /2), (int)(h*bgScale/2), false );
                    findViewById(R.id.button_reset).setVisibility(View.INVISIBLE);
                    show_subMenu(this, SUB_MENU_BACK_BLUR);
                }
                else
                    Toast.makeText(this, "배경화상을 추가하십시오.", Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_sub_accept:
                if(m_SubMenuType == SUB_MENU_ERASER) {
                    Engine.visonmix_fgBitmap = fvForeground.mergeToCache();
                    fvForeground.setBitmap(Engine.visonmix_fgBitmap);
                    fvForeground.clearAll();
                } else if(m_SubMenuType == SUB_MENU_BACK_BLUR) {
                    Engine.visonmix_bgBitmap = bitmapBackground;
                    if(m_bIsDefaultBackground)
                        effectBitmap = efftBitmap_draw;
                    blurTmpBitmap.recycle();
                    blurTmpEffectBitmap.recycle();
                }

                hide_subMenu();
                break;
            case R.id.button_sub_close:
                if(m_SubMenuType == SUB_MENU_ERASER) {
                    fvForeground.clearAll();
                } else if(m_SubMenuType == SUB_MENU_BACK_BLUR) {
                    blurTmpBitmap.recycle();
                    blurTmpEffectBitmap.recycle();
                    bitmapBackground = Engine.visonmix_bgBitmap;
                    ivBackground.setBitmap(bitmapBackground);
                    ivBackground.RefreshView();
                    if(m_bIsDefaultBackground) {
                        efftBitmap_draw = effectBitmap;
                        ivEffect.setImageBitmap(efftBitmap_draw);
                    }
                }

                hide_subMenu();
                break;
            case R.id.button_undo:
                if(m_bEraseMode)
                    fvForeground.undo();
                break;
            case R.id.button_reset:
                int w = Engine.visonmix_fgBitmap.getWidth();
                int h = Engine.visonmix_fgBitmap.getHeight();
                Matrix matrix = new Matrix();
                scale0 = Math.min(((float) scrHeight) / Engine.visonmix_fgBitmap.getHeight(), ((float) scrWidth) / Engine.visonmix_fgBitmap.getWidth());
                if(Engine.visonmix_bgBitmap != null ) {
                    float[] x=new float[9];
                    bgMatrix.getValues(x);
                    scale0 = x[Matrix.MSCALE_X];
                    float scale = Math.min(((float) Engine.visonmix_bgBitmap.getWidth() / Engine.visonmix_fgBitmap.getWidth()),
                            ((float) Engine.visonmix_bgBitmap.getHeight() / Engine.visonmix_fgBitmap.getHeight()));
                    scale0 *= scale;
                }
                matrix.postScale(scale0, scale0);
                matrix.postTranslate((int) Math.abs(w * scale0 - scrWidth) / 2, (int) Math.abs(h * scale0 - scrHeight) / 2);
                fvForeground.setMatrix(matrix);
                fvForeground.setOnTouchListener(new ForegroundTouchListener(VisionmixActivity.this,fvForeground, navigatorView, scale0, matrix));
                break;
            case R.id.button_close:
                quit_Activity();
                break;
            case R.id.button_accept:
                show_PreviewDialog();
                break;
        }
    }

    public Bitmap BlendImage()
    {
        float[] bv = new float[9];
        bgMatrix.getValues(bv);

        Matrix matrix1 = new Matrix();
        matrix1.set(fvForeground.getMatrix());

        //matrix1.setValues(fv);
        matrix1.postScale(1.0f / bv[Matrix.MSCALE_X], 1.0f / bv[Matrix.MSCALE_X]);
        matrix1.postTranslate(-bv[Matrix.MTRANS_X] / bv[Matrix.MSCALE_X], -bv[Matrix.MTRANS_Y] / bv[Matrix.MSCALE_X]);


        Bitmap mergedImage = Bitmap.createBitmap(Engine.visonmix_bgBitmap.getWidth(), Engine.visonmix_bgBitmap.getHeight(), Engine.visonmix_bgBitmap.getConfig());
        Canvas canvas = new Canvas(mergedImage);

  //      ColorMatrix cm = new ColorMatrix();
  //      float contrast = (float)(bgContrastValue - 50) / 50;
     //   ImageUtils.setBrightness(cm, contrast);
        Paint paintBack = new Paint();
        Paint paintFore = new Paint();
     //   paintBack.setColorFilter(new ColorMatrixColorFilter(cm));

        canvas.drawBitmap(bitmapBackground, 0, 0, paintBack);
        //   canvas.drawBitmap(fgBitmap, matrix1, new Paint());

//        ColorMatrix cm_f = new ColorMatrix();
//        float contrast_f = (float)(fgContrastValue - 50) / 50;
//        ImageUtils.setBrightness(cm_f, contrast_f);
//        paintFore.setColorFilter(new ColorMatrixColorFilter(cm_f));
//        canvas.drawBitmap(fvForeground.tempBitmap, matrix1,paintFore);
        canvas.drawBitmap(Engine.visonmix_fgBitmap, matrix1, paintFore);

        if(m_bIsDefaultBackground && efftBitmap_draw!=null) {
            canvas.drawBitmap(efftBitmap_draw, 0, bitmapBackground.getHeight()-efftBitmap_draw.getHeight(), paintBack);
        }

        return mergedImage;
    }

    public void show_PreviewDialog()
    {
        if(Engine.visonmix_bgBitmap == null)
        {
            Toast.makeText(this, "배경화상을 추가하십시오.",Toast.LENGTH_SHORT).show();
            return;
        }
        VisionPreviewDialog vpd = new VisionPreviewDialog(this, BlendImage(), previewDialogClickListener, 0);
        vpd.show();
    }

    DialogInterface.OnClickListener previewDialogClickListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == VisionPreviewDialog.BTN_BACK)
            {
                dialog.dismiss();
            }
            else if(which == VisionPreviewDialog.BTN_SAVE)
            {
                AsyncTask_saveResult asyncTask_saveResult = new AsyncTask_saveResult();
                asyncTask_saveResult.execute(dialog);
            }
            else if(which == VisionPreviewDialog.BTN_PHEDIT) {
                AsyncTask_saveResult asyncTask_saveResult = new AsyncTask_saveResult();
                asyncTask_saveResult.execute(dialog);
            } else if(which == VisionPreviewDialog.BTN_STICKER) {
                AsyncTask_saveResult asyncTask_saveResult = new AsyncTask_saveResult();
                asyncTask_saveResult.execute(dialog);
            }

        }
    };
    public  void quit_Activity()
    {
        CustomAlertDialog.Builder alert_confirm = new CustomAlertDialog.Builder(this);
        alert_confirm.setMessage("작업을 중지하겠습니까?\n작업하던 내용이 모두 삭제됩니다.").setCancelable(true).setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Engine.visonmix_fgBitmap != null) {
                            Engine.visonmix_fgBitmap.recycle();
                            Engine.visonmix_fgBitmap = null;
                        }

                        if (Engine.visonmix_bgBitmap != null) {
                            Engine.visonmix_bgBitmap.recycle();
                            Engine.visonmix_bgBitmap = null;
                        }

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
                        dialog.dismiss();
                    }
                });
        CustomAlertDialog alert = alert_confirm.create();
        alert.show();
    }

    int m_SubMenuType = 0;
    private void show_subMenu(Context context, int menuType)
    {
        m_SubMenuType = menuType;
            if(menuType == SUB_MENU_ERASER)
            {
                rl_subtoolbar.removeAllViews();
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                rl_subtoolbar.addView(rl_subEraserMenu, param);

                ll_subtoolmenu.setVisibility(View.VISIBLE);
                rl_navilayout.setVisibility(View.GONE);
                ((ImageView)findViewById(R.id.button_undo)).setVisibility(View.VISIBLE);

                m_bEraseMode = true;
                ((SeekBar)rl_subEraserMenu.findViewById(R.id.sb_brush_size)).setMax(fvForeground.BRUSH_MAX);
                ((SeekBar)rl_subEraserMenu.findViewById(R.id.sb_brush_size)).setProgress((int)fvForeground.getEraserSize());

            } else if(menuType == SUB_MENU_BACK_BLUR) {
                findViewById(R.id.button_reset).setVisibility(View.INVISIBLE);
                rl_subtoolbar.removeAllViews();
                LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                rl_subtoolbar.addView(rl_subBlurMenu, param);

                ll_subtoolmenu.setVisibility(View.VISIBLE);
                sbStrokeWidth.setMax(100);
                sbStrokeWidth.setProgress(1);
                findViewById(R.id.imageView_erase).setVisibility(View.INVISIBLE);
                findViewById(R.id.imageView_blur).setVisibility(View.VISIBLE);
            }
    }

    private void hide_subMenu()
    {
        findViewById(R.id.button_reset).setVisibility(View.VISIBLE);
        ll_subtoolmenu.setVisibility(View.INVISIBLE);
        rl_navilayout.setVisibility(View.GONE);
        ((ImageView)findViewById(R.id.button_undo)).setVisibility(View.INVISIBLE);
        m_bEraseMode = false;
    }

    private void gotoVisionMaskActivity ()
    {
        Intent intent = new Intent(VisionmixActivity.this, VisionmaskActivity.class);
        intent.putExtra("editing", true);
        intent.putExtra("selectedImagePath", "");
        startActivityForResult(intent, MY_REQUEST_VISIONMASK);
    }

    void gotoBackgroundMagic(String path) {
        Intent intent = new Intent(VisionmixActivity.this, BackgroundMagicActivity.class);
        intent.putExtra("mode", 0);
        intent.putExtra("selectedImagePath", path);
        startActivityForResult(intent, MY_REQUEST_BG_MAGIC);
    }

    protected void openMedia_Background(String title) {

//        final CharSequence[] items = {getString(R.string.camera), getString(R.string.gallery), getString(R.string.default_image)};
        final CharSequence[] items = {getString(R.string.gallery), getString(R.string.default_image)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                if (item == 0) {
                    GalleryUtils.openGallery(VisionmixActivity.this);
                    dialog.dismiss();
                } else if (item == 1) {
                    openDefaultGallery();
                    dialog.dismiss();
                }
//                else if(item == 2)
//                {
//                    GalleryUtils.startCameraActivity(VisionmixActivity.this);
//                    dialog.dismiss();
//                }
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void add_BackgroundImage()
    {
        int w = Engine.visonmix_bgBitmap.getWidth();
        int h = Engine.visonmix_bgBitmap.getHeight();
        bgScale = Math.min(((float) scrHeight) / Engine.visonmix_bgBitmap.getHeight(), ((float) scrWidth) / Engine.visonmix_bgBitmap.getWidth());
        bgMatrix = new Matrix();
        bgMatrix.reset();
        bgMatrix.postScale(bgScale, bgScale);
        bgMatrix.postTranslate((int) Math.abs(w * bgScale - scrWidth) / 2, (int) Math.abs(h * bgScale - scrHeight) / 2);

        bitmapBackground = Bitmap.createBitmap(Engine.visonmix_bgBitmap);

        ivBackground.setBitmap(Engine.visonmix_bgBitmap);

        ivBackground.setMatrix(bgMatrix);

        navigatorView.setBitmap(Engine.visonmix_bgBitmap, bgMatrix);
        if(effectBitmap!=null)
            effectBitmap.recycle();
        ivEffect.setImageBitmap(null);

        if(Engine.visonmix_fgBitmap != null) {
            Matrix fMatrix = new Matrix();
            float scale = Math.min(((float)Engine.visonmix_bgBitmap.getWidth() / Engine.visonmix_fgBitmap.getWidth()),
                    ((float)Engine.visonmix_bgBitmap.getHeight() / Engine.visonmix_fgBitmap.getHeight()));
            scale *= bgScale;
            fMatrix.setScale(scale , scale);
//            fMatrix.setScale(bgScale, bgScale);
            fMatrix.postTranslate(-(int) (Engine.visonmix_fgBitmap.getWidth() * scale - scrWidth) / 2, -(int) (Engine.visonmix_fgBitmap.getHeight() * scale - scrHeight) / 2);
//            fMatrix.postTranslate(-(int) (Engine.visonmix_fgBitmap.getWidth() * bgScale - scrWidth) / 2, -(int) (Engine.visonmix_fgBitmap.getHeight() * bgScale - scrHeight) / 2);

            int left = (int)((scrWidth - (Engine.visonmix_bgBitmap.getWidth() * bgScale)) /2);
            int top =(int)((scrHeight- (Engine.visonmix_bgBitmap.getHeight() * bgScale)) /2);
            fvForeground.setClipRect(left, top, (int)(Engine.visonmix_bgBitmap.getWidth() * bgScale), (int)(Engine.visonmix_bgBitmap.getHeight() * bgScale));
            fvForeground.setMatrix(fMatrix);
            fvForeground.setOnTouchListener(new ForegroundTouchListener(VisionmixActivity.this, fvForeground,navigatorView, bgScale, fMatrix));
        }
    }

    boolean m_bIsDefaultBackground = false;
    public void openDefaultGallery() {
        Dialog_DefaultBackground dlgDefaultBack = new Dialog_DefaultBackground(this, this, defaultDialogClickListener);
        dlgDefaultBack.show();
    }
    DialogInterface.OnClickListener defaultDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(which == DialogInterface.BUTTON_POSITIVE) {
                String path = "assets/backgrounds/src/"+(((Dialog_DefaultBackground)dialog).m_nSelectedItem+1)+".jpg";
                String path1 = "assets/backgrounds/src/"+(((Dialog_DefaultBackground)dialog).m_nSelectedItem+1)+"_effect.png";
                dialog.dismiss();
                m_bIsDefaultBackground = true;
                //gotoBackgroundMagic(path);
                try {
//                    Engine.visonmix_bgBitmap = BitmapFactory.decodeStream(getAssets().open(path));
                    InputStream is = MainActivity.gZipResourceFile.getInputStream(path);
                    Engine.visonmix_bgBitmap = BitmapFactory.decodeStream(is);
                    add_BackgroundImage();
                    AdjustEffectView(path1);
                    is.close();
                }catch(Exception ex) {}

            }
        }
    };

    Bitmap effectBitmap = null;
    private void AdjustEffectView(String path) {
        try {
            effectBitmap = BitmapFactory.decodeStream(MainActivity.gZipResourceFile.getInputStream(path));
            ViewGroup.LayoutParams params = ivEffect.getLayoutParams();
            params.width = (int)(effectBitmap.getWidth() * bgScale);
            //params.width = 720;
            params.height = (int)(effectBitmap.getHeight() * bgScale);
            int backHeight = (int)(Engine.visonmix_bgBitmap.getHeight() * bgScale);
            ivEffect.setTranslationY((backHeight-params.height)/2);
            efftBitmap_draw = Bitmap.createBitmap(effectBitmap);
            ivEffect.setImageBitmap(efftBitmap_draw);

        } catch (Exception ex) {}
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode)
        {
            case RESULT_OK:
                if(requestCode == MY_REQUEST_VISIONMASK) {
                    fvForeground.setBitmap(Engine.visonmix_fgBitmap);
                    fvForeground.invalidate();
                }
                else if(requestCode == GalleryUtils.GALLERY_INTENT_CALLED || requestCode == GalleryUtils.CAMERA_CODE
                        || requestCode == GalleryUtils.GALLERY_KITKAT_INTENT_CALLED || requestCode == MY_REQUEST_DEFAULT_GALLERY)
                {
                    String path = "";
                    int back_idx = 0;

                    if (requestCode == GalleryUtils.GALLERY_INTENT_CALLED) {
                        Uri uri = data.getData();
                        path = GalleryUtils.getFilePathFromUri(this, uri);
                    } else if (requestCode == GalleryUtils.GALLERY_KITKAT_INTENT_CALLED) {
                        Uri uri = data.getData();
                        final int takeFlags = data.getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        // Check for the freshest data.
                        if (uri != null) {
                            path = GalleryUtils.getFilePathFromUri(this, uri);
                        }
                    }
                    else
                    {
                        back_idx = data.getIntExtra("back_idx", 0);
                    }

                    gotoBackgroundMagic(path);

                }
                else if(requestCode == MY_REQUEST_BG_MAGIC)
                {
                    m_bIsDefaultBackground = false;
                    add_BackgroundImage();
                }
                break;
            case RESULT_CANCELED:
                if (requestCode == GalleryUtils.CAMERA_CODE) {
                    Cursor cr = GalleryUtils.loadCursor(this);
                    String[] paths = GalleryUtils.getShootedImagePaths(m_arrOldPaths, cr);
                    cr.close();
                    if (paths.length > 0) {
                        gotoBackgroundMagic(paths[paths.length - 1]);
                    } else
                        GalleryUtils.openMedia(this, getString(R.string.upload_dialog_title_fg));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class AsyncTask_savePNG extends AsyncTask<Void, Void, String> {
        Boolean bSuccess = true;
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();
        }

        @Override
        protected void onPostExecute(String fileName) {
            HideWaitDialog();
            File f = new File(fileName);
            String msg = "전경화상이 보관되였습니다.\n경로: 수정구슬/"+f.getName();
            Toast.makeText(VisionmixActivity.this, msg, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String path = ImageUtils.saveImage(Engine.visonmix_fgBitmap, "", true);
            return path;
        }
    };

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
