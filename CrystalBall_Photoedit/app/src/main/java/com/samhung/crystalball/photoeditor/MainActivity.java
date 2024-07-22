package com.samhung.crystalball.photoeditor;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.OnObbStateChangeListener;
import android.os.storage.StorageManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.l.l.l.f;
import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.Common.RefreshProvider;
import com.samhung.crystalball.Common.SingleMediaScanner;
import com.samhung.crystalball.Common.StorageUtils;
import com.samhung.crystalball.photoeditor.Help.HelpActivity;
import com.samhung.crystalball.photoeditor.Photoshop.PhotoeditActivity;
import com.samhung.crystalball.photoeditor.Photosticker.PhotostickerActivity;
import com.samhung.crystalball.photoeditor.Phototheme.Activities.PhotothemeActivity;
import com.samhung.crystalball.photoeditor.Utilis.ColorChanger;
import com.samhung.crystalball.photoeditor.Utilis.GalleryUtils;
import com.samhung.crystalball.photoeditor.Utilis.zipfile.APKExpansionSupport;
import com.samhung.crystalball.photoeditor.Utilis.zipfile.ZipResourceFile;
import com.samhung.crystalball.photoeditor.VisionMix.Activities.VisionmaskActivity;
import com.samhung.crystalball.photoeditor.VisionMix.Activities.VisionmixActivity;
import com.samhung.crystalball.photoeditor.VisionMix.Dialogs.VisionPreviewDialog;

import org.CrystalBall.Engine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static android.support.v4.widget.DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
import static android.support.v4.widget.DrawerLayout.LOCK_MODE_UNLOCKED;

public class MainActivity extends BaseActivity {

    static final int MY_REQUEST_FG_MAGIC = 0x20;
    static final int MY_REQUEST_VISIONMIX = 0x24;
    static final int MY_REQUEST_PHOTOSHOP = 0x25;
    static final int  MY_REQUEST_STICKER = 0x26;
    static final int MY_REQUEST_THEME_SIMPLE= 0x27;

    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION = 0x202;
    private static final int REQUEST_READ_EXTERNAL_STORAGE_PERMISSION = 0x203;
    private static final int REQUEST_CAMERA_PERMISSION = 0x204;

    public static final String WORK_PHOTO_DIR = "crystal_ball";
    public static final String DONE_PHOTO_DIR = "수정구슬";

    public static int WORK_TYPE = -1;
    final int VISONMIX_FLAG = 0x30;
    final int PHOTOSHOP_FLAG = 0x31;
    final int FRAME_FLAG = 0x32;
    final int STICKER_FLAG = 0x33;

    public static MainActivity mainActivity = null;
    public static ZipResourceFile gZipResourceFile = null;
    public static boolean bAppLaunched = false;
    ImageView logo = null;

    public Bitmap mBitmapLogo = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestWriteExternalStoragePermission();
        com.t.t.t.d.init(this);
        mainActivity = MainActivity.this;

        try {
            gZipResourceFile = APKExpansionSupport.getAPKExpansionZipFile(getApplicationContext(), 40434, 0);
            if(gZipResourceFile == null) {
                Toast.makeText(this, "obb화일을 나의길동무/프로그람 등록부에 복사하십시오.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }catch (Exception ex) {
            Toast.makeText(this, "obb화일을 나의길동무/프로그람 등록부에 복사하십시오.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        try {
            mBitmapLogo = BitmapFactory.decodeStream(gZipResourceFile.getInputStream("assets/logo_main.png"));
//            ImageView logo = (ImageView)findViewById(R.id.imageView_logo);
//            logo.setImageBitmap(mBitmapLogo);
        }catch (Exception ex) {}

        logo = (ImageView)findViewById(R.id.imageView_logo);
        logo.setImageBitmap(mBitmapLogo);
        logo.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        logo.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                logo.setImageBitmap(mBitmapLogo);
            }
        });


        InitWaitView((ViewGroup)findViewById(R.id.MainView));
        asyncTask_initEngine = new AsyncTask_initEngine();
        asyncTask_initEngine.execute();
        bAppLaunched = true;

        //227init
        new File("/data/data/" + getPackageName() + "/files").mkdir();
        File file = new File("/data/data/" + getPackageName() + "/files/splash.jpg");
        if (!file.exists()) {
            try {
                InputStream openRawResource = getResources().openRawResource(R.raw.cam_autofocus_warning);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = openRawResource.read(bArr);
                    if (read < 0) {
                        break;
                    }
                    fileOutputStream.write(bArr, 0, read);
                }
                fileOutputStream.close();
                openRawResource.close();
            } catch (Exception e2) {
            }
        }
        f.init(this);
        /////
    }

    public boolean InitEngine() {

//        String modelPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crystal_ball";
        List<StorageUtils.StorageInfo> storageInfos = StorageUtils.getStorageList();
        if(storageInfos.size() == 0) return false;
        String lmDBPath = "";
        for(int i=0; i<storageInfos.size(); i++) {
            File file = new File(storageInfos.get(i).path + "/나의길동무/프로그람");
            File filedb = new File(storageInfos.get(i).path + "/나의길동무/프로그람/Crystalball.db");
//            File file = new File(storageInfos.get(i).path + "/나의길동무/프로그람");
//            File filedb = new File(storageInfos.get(i).path + "/나의길동무/프로그람/Crystalball.db");
            if(filedb.exists()) {
                lmDBPath = file.getAbsolutePath();
                break;
            }
        }

        if(lmDBPath == "") return false;
        Bitmap bmp = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.sketch_texture);
        String modelPath = "/data/data/" + getApplicationContext().getPackageName()+"/models";
        boolean bRet = Engine.getIntance().EngineInit(modelPath, lmDBPath, bmp);
        bmp.recycle();
        return bRet;
    }

    @Override
    protected void onStart() {
        super.onStart();
        logo.requestLayout();
    }

    public static final String SD_CARD = "sdCard";
    public static final String EXTERNAL_SD_CARD = "externalSdCard";
    private static final String ENV_SECONDARY_STORAGE = "EXTERNAL_STORAGE";//"SECONDARY_STORAGE";

    public static Map<String, File> getAllStorageLocations() {
        Map<String, File> storageLocations = new HashMap<>(10);
        File sdCard = Environment.getExternalStorageDirectory();
        storageLocations.put(SD_CARD, sdCard);
        final String rawSecondaryStorage = System.getenv(ENV_SECONDARY_STORAGE);
        if (!TextUtils.isEmpty(rawSecondaryStorage)) {
            String[] externalCards = rawSecondaryStorage.split(":");
            for (int i = 0; i < externalCards.length; i++) {
                String path = externalCards[i];
                storageLocations.put(EXTERNAL_SD_CARD + String.format(i == 0 ? "" : "_%d", i), new File(path));
            }
        }
        return storageLocations;
    }

    private void InitActivityViews(Context context)
    {
        NavigationView navigationView = (NavigationView)findViewById(R.id.nav_drawer);
        ViewGroup.LayoutParams params = navigationView.getLayoutParams();
        params.width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.65f);
        navigationView.setLayoutParams(params);;
//        final HorizontalScrollView hSV = (HorizontalScrollView)findViewById(R.id.hsv_main);
////        hSV.postDelayed(new Runnable() {
////            @Override
////            public void run() {
////                hSV.fullScroll(View.FOCUS_RIGHT);
////                hSV.postDelayed(new Runnable() {
////                    @Override
////                    public void run() {
////                        hSV.fullScroll(View.FOCUS_LEFT);
////                    }
////                },600);
////            }
////        }, 500);
//        hSV.setSmoothScrollingEnabled(true);
//        final Timer timer = new Timer();
//        final TimerTask ts = new TimerTask() {
//            boolean bRight = false;
//            boolean bFinish = false;
//            int scroll = 0;
//            @Override
//            public void run() {
//                if(!bRight)
//                {
//                    hSV.smoothScrollBy(16, 0);
//                    scroll += 16;
//                }
//                else {
//                    hSV.smoothScrollBy(-16, 0);
//                    scroll -=  16;
//                }
//
//                if(scroll >= hSV.getRight()) {
//                    bRight = true;
//                }
//                if(bRight && hSV.getScrollX() == 0) {
//
//                    bFinish = true;
//                }
//                if(bFinish) {
//                    timer.cancel();
//                }
//            }
//        };
//        hSV.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                timer.schedule(ts, 10, 10);
//            }
//        }, 200);
    }

    public void onDrawerButtonClick(View view)
    {
        switch(view.getId())
        {
            case R.id.ll_visionmix:
                WORK_TYPE = VISONMIX_FLAG;
                ClearVisionmixCache();
                //GalleryUtils.openMedia(this, getString(R.string.upload_dialog_title_fg));
                GalleryUtils.openGallery(this);
                break;
            case R.id.ll_photoshop:
                WORK_TYPE = PHOTOSHOP_FLAG;
//                GalleryUtils.openMedia(this, getString(R.string.upload_dialog_title_fg));
                GalleryUtils.openGallery(this);
                break;
            case R.id.ll_frameeffect:
                WORK_TYPE = FRAME_FLAG;
//                Dialog_ThemeChooser dialog_themeChooser = new Dialog_ThemeChooser(this, this, dlgButtonClickListener);
//                dialog_themeChooser.show();
                gotoThemeActivity();
                break;
            case R.id.ll_sticker:
                WORK_TYPE = STICKER_FLAG;
//                GalleryUtils.openMedia(this, getString(R.string.upload_dialog_title_fg));
                GalleryUtils.openGallery(this);
                break;
            case R.id.ll_help:
                gotoHelpActivity();
                break;
            case R.id.ll_camera:
                GalleryUtils.startCamera(this, GalleryUtils.getOutputMediaFileUri(this));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    void EnableButtons(Boolean bEnable) {
        findViewById(R.id.button_mat).setEnabled(bEnable);
        findViewById(R.id.button_phedit).setEnabled(bEnable);
        findViewById(R.id.button_frame).setEnabled(bEnable);
        findViewById(R.id.button_sticker).setEnabled(bEnable);
        findViewById(R.id.button_help).setEnabled(bEnable);
        findViewById(R.id.button_camera).setEnabled(bEnable);
    }
    public void onButtonClick(View view)
    {
        RelativeLayout button_camera = (RelativeLayout) findViewById(R.id.button_camera);
        switch(view.getId())
        {
            case R.id.button_mat:
                EnableButtons(false);
                WORK_TYPE = VISONMIX_FLAG;
                ClearVisionmixCache();
                GalleryUtils.openGallery(this);
                break;
            case R.id.button_phedit:
                EnableButtons(false);
                WORK_TYPE = PHOTOSHOP_FLAG;
                GalleryUtils.openGallery(this);
                break;
            case R.id.button_frame:
                EnableButtons(false);
                WORK_TYPE = FRAME_FLAG;
                gotoThemeActivity();
                break;
            case R.id.button_sticker:
                EnableButtons(false);
                WORK_TYPE = STICKER_FLAG;
//                GalleryUtils.openMedia(this, getString(R.string.upload_dialog_title_fg));
                GalleryUtils.openGallery(this);
                break;
            case R.id.button_help:
                EnableButtons(false);
                gotoHelpActivity();
                break;
            case R.id.button_camera:
                EnableButtons(false);
                GalleryUtils.startCamera(this, GalleryUtils.getOutputMediaFileUri(this));
                break;
            case R.id.btn_drawer_open:
                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.openDrawer(GravityCompat.START);
                break;
        }
    }

//    DialogInterface.OnClickListener dlgButtonClickListener = new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//            if(which == DialogInterface.BUTTON_POSITIVE)
//            {
//                gotoThemeActivity(((Dialog_ThemeChooser)dialog).m_nSelectedItem);
//                dialog.dismiss();
//
//            }
//        }
//    };

    public static boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);

    }

    public static final boolean isKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    private void requestWriteExternalStoragePermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION);
                }
                else{
                    requestReadExternalStoragePermission();
                }
            }
            else{
//                setupViewPager(viewPager);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void requestReadExternalStoragePermission() {
        try {
            //Android6.0 이상인 경우
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                    },REQUEST_READ_EXTERNAL_STORAGE_PERMISSION);
                }
                else{
                    requestCameraPermission();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestCameraPermission() {
        try {
            //Android6.0 이상인 경우
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int permission = ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA);
                if(permission!= PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,new String[] {
                            Manifest.permission.CAMERA
                    },REQUEST_CAMERA_PERMISSION);
                }
                else{
                    //  requestRecordAudioPermission();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION)
                requestReadExternalStoragePermission();
            else if (requestCode == REQUEST_READ_EXTERNAL_STORAGE_PERMISSION)
                requestCameraPermission();
//            else if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION)
//                //setupViewPager(viewPager);
//                RestartApp();
        } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            MainActivity.this.finish();
        }
    }

    private void addImageGallery( File file ) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // setar isso
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    protected ListAdapter createAdapter()
    {
        // return play-lists
        Uri playlist_uri= MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String [] STAR= {"*"};
        Cursor cursor= managedQuery(playlist_uri, STAR, null,null,null);
        cursor.moveToFirst();
        for(int r= 0; r<cursor.getCount(); r++, cursor.moveToNext()){
            int i = cursor.getInt(0);
            int l = cursor.getString(1).length();
            if(l>0){
                // keep any playlists with a valid data field, and let me know
            }else{
                // delete any play-lists with a data length of '0'
                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, i);
                getContentResolver().delete(uri, null, null);
            }
        }
        cursor.close();
        // publish list of retained / deleted playlists

        return null;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        EnableButtons(true);
//        File mediaStorageDir = new File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), MainActivity.DONE_PHOTO_DIR);
//        addImageGallery(mediaStorageDir);

        switch (resultCode) {
            case RESULT_CANCELED:
//                if (requestCode == GalleryUtils.CAMERA_CODE) {
//                    Cursor cr = GalleryUtils.loadCursor(this);
//                    String[] paths = GalleryUtils.getShootedImagePaths(m_arrOldPaths, cr);
//                    cr.close();
//                    if (paths.length > 0) {
//                        if (WORK_TYPE == VISONMIX_FLAG)
//                            gotoVisionMaskActivity(false, paths[paths.length - 1]);
//                        else if (WORK_TYPE == PHOTOSHOP_FLAG)
//                            gotoPhotoshopActivity(paths[paths.length - 1]);
//                        else if (WORK_TYPE == STICKER_FLAG)
//                            gotoStickerActivity(paths[paths.length - 1]);
//                    } else
//                        GalleryUtils.openMedia(this, getString(R.string.upload_dialog_title_fg));
//                }
                break;
            case RESULT_OK:
                if (requestCode == GalleryUtils.GALLERY_INTENT_CALLED /*|| requestCode == GalleryUtils.CAMERA_CODE*/
                        || requestCode == GalleryUtils.GALLERY_KITKAT_INTENT_CALLED) {

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
//                            getContentResolver().takePersistableUriPermission(
//                                    uri, takeFlags);
                            path = GalleryUtils.getFilePathFromUri(this, uri);
                        }
                    } else if (requestCode == GalleryUtils.CAMERA_CODE) {
                        //path = getFilePathFromUri(this, selectedCameraPictureUri);  /////////////////////////
//                        GalleryUtils.getOutputMediaFileUri(this);
//                        path = GalleryUtils.cameraPath;
                    } else {
                        back_idx = data.getIntExtra("back_idx", 0);
                    }

                    if (WORK_TYPE == VISONMIX_FLAG) {
                        gotoVisionMaskActivity(false, path);
                    } else if(WORK_TYPE == PHOTOSHOP_FLAG) {
                        gotoPhotoshopActivity(path);
                    } else if(WORK_TYPE == STICKER_FLAG) {
                        gotoStickerActivity(path);
                    }
                    break;
                }
                else if(requestCode ==MY_REQUEST_FG_MAGIC )
                {
                    boolean bPNGLoaded = data.getExtras().getBoolean("bPNGLoaded");
                    gotoVisionmixActivity(bPNGLoaded);
                }
                else if(requestCode == MY_REQUEST_VISIONMIX)
                {
                    String path = data.getStringExtra("path");
                    File f = new File(path);
                    String msg = "화상이 성과적으로 보관되였습니다.\n경로: 수정구슬/"+f.getName();
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    ClearVisionmixCache();

                    int sendTo = data.getExtras().getInt("sendto");

                    if(sendTo == VisionPreviewDialog.BTN_PHEDIT)
                        gotoPhotoshopActivity(path);
                    else if(sendTo == VisionPreviewDialog.BTN_STICKER)
                        gotoStickerActivity(path);
                } else if(requestCode == MY_REQUEST_PHOTOSHOP) {
                    String path = data.getStringExtra("path");
                    File f = new File(path);
                    String msg = "화상이 성과적으로 보관되였습니다.\n경로: 수정구슬/"+f.getName();
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                    int sendTo = data.getExtras().getInt("sendto");

                    if(sendTo == VisionPreviewDialog.BTN_STICKER)
                        gotoStickerActivity(path);
                } else if(requestCode == MY_REQUEST_STICKER) {
                    String path = data.getStringExtra("path");
                    File f = new File(path);
                    String msg = "화상이 성과적으로 보관되였습니다.\n경로: 수정구슬/"+f.getName();
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    int sendTo = data.getExtras().getInt("sendto");

                    if(sendTo == VisionPreviewDialog.BTN_PHEDIT)
                        gotoPhotoshopActivity(path);
                } else if(requestCode == MY_REQUEST_THEME_SIMPLE) {
                    String path = data.getStringExtra("path");
                    File f = new File(path);
                    String msg = "화상이 성과적으로 보관되였습니다.\n경로: 수정구슬/"+f.getName();
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

                    int sendTo = data.getExtras().getInt("sendto");

                    if(sendTo == VisionPreviewDialog.BTN_PHEDIT)
                        gotoPhotoshopActivity(path);
                    else if(sendTo == VisionPreviewDialog.BTN_STICKER)
                        gotoStickerActivity(path);
                }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void ClearVisionmixCache()
    {
        if(Engine.visonmix_fgBitmap!=null)
        {
            Engine.visonmix_fgBitmap.recycle();
            Engine.visonmix_fgBitmap = null;
        }
        if(Engine.visonmix_bgBitmap!=null)
        {
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

    }
    private void gotoVisionMaskActivity (boolean isEditing, String path)
    {
        Intent intent = new Intent(MainActivity.this, VisionmaskActivity.class);
        intent.putExtra("editing", isEditing);
        intent.putExtra("selectedImagePath", path);
        startActivityForResult(intent, MY_REQUEST_FG_MAGIC);
    }

    private void gotoVisionmixActivity(boolean bPNGLoaded)
    {
        Intent intent = new Intent(MainActivity.this, VisionmixActivity.class);
        intent.putExtra("bPNGLoaded", bPNGLoaded);
        startActivityForResult(intent, MY_REQUEST_VISIONMIX);
    }

    private void gotoPhotoshopActivity(String path)
    {
        Intent intent = new Intent(MainActivity.this, PhotoeditActivity.class);
        intent.putExtra("selectedImagePath", path);
        startActivityForResult(intent, MY_REQUEST_PHOTOSHOP);
    }

    private void gotoThemeActivity() {
        Intent intent = new Intent(MainActivity.this, PhotothemeActivity.class);
        //intent.putExtra("theme_index", idx);
        startActivityForResult(intent, MY_REQUEST_THEME_SIMPLE);
    }

    private void gotoStickerActivity(String path)
    {
        Intent intent = new Intent(MainActivity.this, PhotostickerActivity.class);
        intent.putExtra("selectedImagePath", path);
        startActivityForResult(intent, MY_REQUEST_STICKER);
    }

    private void gotoHelpActivity() {
        Intent intent = new Intent(MainActivity.this, HelpActivity.class);
        startActivityForResult(intent, MY_REQUEST_STICKER);
    }

    long backPressedMilli = 0;
    Toast backToast = null;
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if(backPressedMilli == 0) {
            backPressedMilli = Calendar.getInstance().getTimeInMillis();
            backToast = Toast.makeText(this, "끝내려면 한번 더 누르십시오.", Toast.LENGTH_SHORT);
            backToast.show();
        } else {
            long curr = Calendar.getInstance().getTimeInMillis();
            if(curr-backPressedMilli < 2000) {
                backToast.cancel();
                bAppLaunched = false;
                super.onBackPressed();
            }
            backPressedMilli = 0;
        }
    }

    AsyncTask_initEngine asyncTask_initEngine = null;
    private class AsyncTask_initEngine extends AsyncTask<Void, Void, Void> {
        Boolean bSuccess = false;
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();
            DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
            drawerLayout.setDrawerLockMode(LOCK_MODE_LOCKED_CLOSED);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            HideWaitDialog();
            if(!bSuccess) {
                Toast.makeText(MainActivity.this, "자료등록부를 복사하십시오.", Toast.LENGTH_SHORT).show();
                finish();
            }
            DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
            drawerLayout.setDrawerLockMode(LOCK_MODE_UNLOCKED);

            InitActivityViews(MainActivity.this);

//            ColorChanger colorChanger = new ColorChanger(getResources().getColor(R.color.color31),
//                    getResources().getColor(R.color.color21),
//                    getResources().getColor(R.color.color28));
//            colorChanger.run(logo);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            CopyModelFiles();
            HelpActivity.copyHelpData();
            bSuccess = InitEngine();
            return null;
        }

        void CopyModelFiles()
        {
            String[] arrFiles = {"LutFile.png", "seg.bin","seg.proto", "det1.bin", "det1.param", "det2.bin", "det2.param", "det3.bin", "det3.param"};
            String strDataPath ="/data/data/" + getApplicationContext().getPackageName();
            strDataPath += "/models/";
            File f = null;
            f = new File(strDataPath);
            InputStream is = null;
            FileOutputStream fos = null;
            byte	buf[] = new byte[131072];
            int nSize = 0;
            int nLen = 0;

            if(!f.exists())
            f.mkdir();
            try{
                for(int i=0; i<arrFiles.length; i++)
                {
                    f = new File(strDataPath+arrFiles[i]);
                    if(f.exists())
                        continue;

                    is = gZipResourceFile.getInputStream("assets/models/"+arrFiles[i]);
                    fos = new FileOutputStream(strDataPath+arrFiles[i]);
                    nSize =is.available();
                    while(nSize > 0)
                    {
                        nLen = is.read(buf, 0, 131072);
                        if(nLen != -1)
                        {
                            fos.write(buf, 0, nLen);
                            nSize -= nLen;
                        }
                    }
                    fos.flush();
                    fos.close();
                    is.close();
                    is=null;
                    fos = null;
                }
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }


            List<StorageUtils.StorageInfo> storageInfos = StorageUtils.getStorageList();
            if(storageInfos.size() == 0) return;
            String modelPath = "";
            for(int i=0; i<storageInfos.size(); i++) {
                File file = new File(storageInfos.get(i).path + "/나의길동무/프로그람");
                if(file.exists()) {
                    file = new File(file, "/Crystalball.db");
                    if(!file.exists()) {
                        try{
                            is = MainActivity.gZipResourceFile.getInputStream("assets/models/lm68_dlib.dat");
                            fos = new FileOutputStream(file.getAbsoluteFile());
                            nSize =is.available();
                            while(nSize > 0)
                            {
                                nLen = is.read(buf, 0, 131072);
                                if(nLen != -1)
                                {
                                    fos.write(buf, 0, nLen);
                                    nSize -= nLen;
                                }
                            }
                            fos.flush();
                            fos.close();
                            is.close();
                            is=null;
                            fos = null;
                        }catch (Exception ex) {}
                    }
                    break;
                }
            }
        }
    }
}
