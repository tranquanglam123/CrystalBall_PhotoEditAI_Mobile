package com.samhung.crystalball.photoeditor.Phototheme.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.Phototheme.Activities.PhotothemeActivity;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.ThemeInfo;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.ThemeViewTouchListener;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.GalleryUtils;
import com.samhung.crystalball.photoeditor.VisionMix.Dialogs.VisionPreviewDialog;

import org.CrystalBall.Engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Dialog_ThemeMultiSimple extends FullScreenDialog implements View.OnTouchListener, DialogThemeAdapter.OnItemClickListener{
    OnClickListener m_onClickListener = null;
    public Bitmap themeBitmap = null;
    public Bitmap themeBitmap_draw = null;
    Context mContext = null;
    PhotothemeActivity mParent = null;

    RelativeLayout mRelativeLayout_Work = null;
    int main_CategoryID = 0;
    int sub_CategoryID = 0;
    String themePath = "";
    ImageView ivTheme = null;
    RecyclerView recycler_Themes = null;
    ThemeInfo mThemeInfo = null;
    int m_nPhotoIndex = 0;

    private ArrayList<Bitmap> m_themeBitmapArray = new ArrayList<Bitmap>();
    private ArrayList<ImageView> m_ivPhotos = new ArrayList<ImageView>();
    private Bitmap[] m_Bitmaps = new Bitmap[7];
    private ImageView[] imv_buttonDrawings = new ImageView[7];
    private RelativeLayout[] btn_photos = new RelativeLayout[7];

    public Dialog_ThemeMultiSimple(Context context , PhotothemeActivity parent, int mainCategory, int subCategory, ArrayList<Bitmap> thumbArray, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mParent = parent;
        main_CategoryID = mainCategory;
        sub_CategoryID = subCategory;
        themePath = "";
//        mResultBitmap = Bitmap.createBitmap(mBitmap);

        m_themeBitmapArray = thumbArray;
//        InitThemeItemArray();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_theme_multisimple);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));
        mRelativeLayout_Work = (RelativeLayout)findViewById(R.id.work_rl);
        ivTheme = (ImageView)findViewById(R.id.imageview_theme);
        recycler_Themes = (RecyclerView)findViewById(R.id.recyler_thememenu);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        recycler_Themes.setLayoutManager(layoutManager);
        recycler_Themes.setHasFixedSize(true);
        DialogThemeAdapter adapter = new DialogThemeAdapter(mContext, m_themeBitmapArray, sub_CategoryID);
        adapter.setOnImageClickListener(this);
        recycler_Themes.setAdapter(adapter);
        recycler_Themes.scrollToPosition(sub_CategoryID);
        mRelativeLayout_Work.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
//                wDrawingPane = right - left;
//                hDrawingPane = bottom - top;
                layout_Left = left;
                layout_Top = top;

                if(m_bThemeLoaded) {
                    AdjustViewPosition();
                }
                m_bThemeLoaded = false;

                mRelativeLayout_Work.setOnTouchListener(new ThemeViewTouchListener());
            }
        });

        ImageView btnAccept = (ImageView)findViewById(R.id.button_accept);
        ImageView btnBack = (ImageView)findViewById(R.id.button_close);
        btnAccept.setOnClickListener(btnClickListener);
        btnBack.setOnClickListener(btnClickListener);
        findViewById(R.id.iv_phedit).setOnClickListener(btnClickListener);
        findViewById(R.id.iv_phsticker).setOnClickListener(btnClickListener);
        ((ImageView)findViewById(R.id.btn_photo)).setOnClickListener(btnClickListener);
        ((ImageView)findViewById(R.id.btn_photo)).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //findViewById(R.id.rl_openmenu).setVisibility(View.VISIBLE);
                GalleryUtils.openGalleryMulti(mParent, mThemeInfo.ImageCount);
                return false;
            }
        });

        findViewById(R.id.rl_openmenu).setOnClickListener(btnClickListener);

        btn_photos[0] = (RelativeLayout)findViewById(R.id.btn_photo1);
        btn_photos[1] = (RelativeLayout)findViewById(R.id.btn_photo2);
        btn_photos[2] = (RelativeLayout)findViewById(R.id.btn_photo3);
        btn_photos[3] = (RelativeLayout)findViewById(R.id.btn_photo4);
        btn_photos[4] = (RelativeLayout)findViewById(R.id.btn_photo5);
        btn_photos[5] = (RelativeLayout)findViewById(R.id.btn_photo6);
        btn_photos[6] = (RelativeLayout)findViewById(R.id.btn_photo7);
        for(int i=0; i<btn_photos.length; i++)
            btn_photos[i].setOnClickListener(btnClickListener);

        findViewById(R.id.iv_back).setOnClickListener(btnClickListener);
        findViewById(R.id.iv_save).setOnClickListener(btnClickListener);

        imv_buttonDrawings[0] = (ImageView)findViewById(R.id.imv_btn1);
        imv_buttonDrawings[1] = (ImageView)findViewById(R.id.imv_btn2);
        imv_buttonDrawings[2] = (ImageView)findViewById(R.id.imv_btn3);
        imv_buttonDrawings[3] = (ImageView)findViewById(R.id.imv_btn4);
        imv_buttonDrawings[4] = (ImageView)findViewById(R.id.imv_btn5);
        imv_buttonDrawings[5] = (ImageView)findViewById(R.id.imv_btn6);
        imv_buttonDrawings[6] = (ImageView)findViewById(R.id.imv_btn7);

        findViewById(R.id.rl_openmenu).setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                v.setVisibility(View.INVISIBLE);
            }
        });
        LoadTheme();
    }

    boolean m_bThemeLoaded = false;
    void LoadTheme() {
        String infoPath =String.format("assets/theme/%d/%d/info.pos", main_CategoryID+1, sub_CategoryID+1);
        themePath = String.format("assets/theme/%d/%d/src.png", main_CategoryID+1, sub_CategoryID+1);
        mThemeInfo = ThemeInfo.LoadInfo(getContext(), infoPath);
//        m_Bitmaps = new Bitmap[mThemeInfo.ImageCount];
        try{
            //themeBitmap = BitmapFactory.decodeStream(getContext().getResources().getAssets().open(themePath));
            themeBitmap = BitmapFactory.decodeStream(MainActivity.gZipResourceFile.getInputStream(themePath));
            themeBitmap_draw = themeBitmap.copy(themeBitmap.getConfig(), true);
            ivTheme.setImageBitmap(themeBitmap_draw);
            mRelativeLayout_Work.requestLayout();
            CreatePhotoViews();
            for(int i=0; i<btn_photos.length; i++)
            {
                btn_photos[i].setVisibility(View.GONE);
                if(i<mThemeInfo.ImageCount)
                    btn_photos[i].setVisibility(View.VISIBLE);
            }

            m_bThemeLoaded = true;
        } catch (Exception ex) {}
    }

    void CreatePhotoViews() {
        ClearAllPhotoViews();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i=0; i<mThemeInfo.ImageCount; i++) {
            ImageView imageView = (ImageView)inflater.inflate(R.layout.theme_photo_item, null);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(10, 10);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            mRelativeLayout_Work.addView(imageView, params);
            m_ivPhotos.add(imageView);

            imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    int wid = right - left;
                    int hei = bottom - top;
                    if(wid==0||hei==0||left == oldLeft || top==oldTop || wid == (oldRight - oldLeft) || hei == (oldBottom-oldTop) )
                        return;
                    int index = m_ivPhotos.indexOf((ImageView)v);
                    Refresh_PhotoView(index);
                }
            });
        }
        ivTheme.bringToFront();
    }

    void ClearAllPhotoViews() {
        if(m_ivPhotos.size() == 0) return;
        for(int i=0; i<m_ivPhotos.size(); i++){
            mRelativeLayout_Work.removeView(m_ivPhotos.get(i));
        }
        m_ivPhotos.clear();
    }

    void AdjustViewPosition() {
        Matrix m = ivTheme.getImageMatrix();
        float x[]=new float[9];
        m.getValues(x);
        float scale = x[Matrix.MSCALE_X];

        float centerX = mThemeInfo.centerX[0];
        float centerY = mThemeInfo.centerY[0];
        float photoViewWidth = mThemeInfo.width[0];
        float photoViewHeight = mThemeInfo.height[0];

        for(int i=0; i<mThemeInfo.ImageCount; i++) {
            centerX = mThemeInfo.centerX[i];
            centerY = mThemeInfo.centerY[i];
            photoViewWidth = mThemeInfo.width[i];
            photoViewHeight = mThemeInfo.height[i];

            ImageView ivPhoto = m_ivPhotos.get(i);
            ViewGroup.LayoutParams layoutParams = ivPhoto.getLayoutParams();
            layoutParams.width = (int)(photoViewWidth * scale);
            layoutParams.height = (int)(photoViewHeight * scale);
            ivPhoto.setLayoutParams(layoutParams);
            int nWidth = (int)(themeBitmap.getWidth() * scale); //mRelativeLayout_Work.getWidth();
            int nHeight = (int)(themeBitmap.getHeight() * scale); //mRelativeLayout_Work.getHeight();
            ivPhoto.setTranslationX(-nWidth/2+centerX * scale);
            ivPhoto.setTranslationY(-nHeight/2+centerY * scale);
            ivPhoto.setRotation(mThemeInfo.angle[i]);
            ivPhoto.setOnLongClickListener(mIVLongClickListener);

        }
        if(!mThemeInfo.bThemeTouch)
            ivTheme.setOnTouchListener(null);
    }


    public PhotothemeActivity.LoadGalleryListener mLoadGalleryListener = new PhotothemeActivity.LoadGalleryListener() {
        @Override
        public void onLoadGallery(String path) {
            LoadPhoto(path);
        }

        @Override
        public void onLoadGalleryMulti(List<String> paths) {
            LoadPhotoMulti(paths);
        }
    };

    void LoadPhoto(String strPath) {
        m_Bitmaps[m_nPhotoIndex] = ImageUtils.decodeFile(strPath, themeBitmap.getWidth()/2, themeBitmap.getHeight()/2);
        if(mThemeInfo.bGray[m_nPhotoIndex] == true)
           Engine.getIntance().makeBitmapToGray(m_Bitmaps[m_nPhotoIndex]);

        Log.e("------wwww",""+m_Bitmaps[m_nPhotoIndex].getWidth()+"   "+m_Bitmaps[m_nPhotoIndex].getHeight());
//        Bitmap thumb = Bitmap.createScaledBitmap(m_Bitmaps[m_nPhotoIndex], 120, 120, true);
        Bitmap thumb = ThumbnailUtils.extractThumbnail(m_Bitmaps[m_nPhotoIndex], 120, 120);
        imv_buttonDrawings[m_nPhotoIndex].setImageBitmap( thumb);

        Refresh_PhotoView(m_nPhotoIndex);

//        findViewById(R.id.rl_openmenu).setVisibility(View.GONE);
    }

    void LoadPhotoMulti(List<String> strPaths) {

        for(int i=0; i<strPaths.size(); i++)
        {
            if(i<mThemeInfo.ImageCount) {
                m_Bitmaps[i] = ImageUtils.decodeFile(strPaths.get(i), themeBitmap.getWidth()/2, themeBitmap.getHeight()/2);
                Log.e("------wwww",""+m_Bitmaps[i].getWidth()+"   "+m_Bitmaps[i].getHeight());
                Bitmap thumb = ThumbnailUtils.extractThumbnail(m_Bitmaps[i], 120, 120);
                imv_buttonDrawings[i].setImageBitmap( thumb);

                Refresh_PhotoView(i);
            }
        }
    }

    float[] scale0s = new float[7];
    Matrix[] matrix_arr = new Matrix[7];
    void Refresh_PhotoView(int index)
    {
        if(m_Bitmaps[index]==null) return;
        ImageView ivPhoto = m_ivPhotos.get(index);

        int wDrawingPane = ivPhoto.getWidth();
        int  hDrawingPane = ivPhoto.getHeight();
        int wSrc = m_Bitmaps[index].getWidth();
        int hSrc = m_Bitmaps[index].getHeight();
        float scale0 = Math.max(((float) ivPhoto.getHeight()) / hSrc, ((float) ivPhoto.getWidth()) / wSrc);
        scale0s[index] = scale0;
        Matrix matrix = new Matrix();
        matrix.postScale(scale0, scale0);
        int xInDrawingPane = (int)Math.abs(wSrc * scale0 - ivPhoto.getWidth()) / 2;
        int yInDrawingPane = (int) Math.abs(hSrc * scale0 - ivPhoto.getHeight()) / 2;
        matrix.postTranslate(-xInDrawingPane, -yInDrawingPane);
        ivPhoto.setImageBitmap(m_Bitmaps[index]);
        ivPhoto.setImageMatrix(matrix);
        matrix_arr[index ] = matrix;
        ivPhoto.setOnTouchListener(this);

    }

    public void InitThemeItemArray()
    {
        m_themeBitmapArray.clear();
        String dir = String.format("theme/%d", main_CategoryID+1);

        try {
            String[] paths = mContext.getAssets().list(dir);
            for(int i=0; i<paths.length; i++)
            {
                String strfileName = String.format("theme/%d/%d/thumb.png", main_CategoryID+1, i+1);
                Bitmap ret=null;
                try {
                    ret = BitmapFactory.decodeStream(mContext.getAssets().open(strfileName));
                    m_themeBitmapArray.add(ret);
                }catch (Exception ex) {}

            }
        }
        catch (Exception ex){ex.printStackTrace();}
    }

    private Bitmap MakeResultBitmap() {
        Bitmap resultBitmap = Bitmap.createBitmap(themeBitmap.getWidth(), themeBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas resultCanvas = new Canvas(resultBitmap);
        Bitmap[] photos = new Bitmap[mThemeInfo.ImageCount];
        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        Matrix m = ivTheme.getImageMatrix();
        float x[]=new float[9];
        m.getValues(x);
        float scale = x[Matrix.MSCALE_X];

        for(int i=0; i<mThemeInfo.ImageCount; i++)
        {
            ImageView ivPhoto = m_ivPhotos.get(i);
            photos[i] = Bitmap.createBitmap(mThemeInfo.width[i], mThemeInfo.height[i],  Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(photos[i]);
            Matrix matrix = new Matrix(ivPhoto.getImageMatrix());
            matrix.postScale(1/scale, 1/scale, 0,0);
            canvas.drawBitmap(m_Bitmaps[i], matrix, paint);

            Matrix matrix1 = new Matrix();
            matrix1.postTranslate(mThemeInfo.centerX[i]-mThemeInfo.width[i]/2, mThemeInfo.centerY[i]-mThemeInfo.height[i]/2);
            matrix1.postRotate(mThemeInfo.angle[i], mThemeInfo.centerX[i],mThemeInfo.centerY[i]);
            resultCanvas.drawBitmap(photos[i], matrix1, paint);
        }
        resultCanvas.drawBitmap(themeBitmap, 0, 0, paint);
        return resultBitmap;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        findViewById(R.id.rl_openmenu).setVisibility(View.INVISIBLE);
        return super.onTouchEvent(event);
    }

    int mode = BaseActivity.NONE;
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 0;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        super.onTouchEvent(event);
        ImageView ivPhoto = (ImageView)v;
        int index = m_ivPhotos.indexOf(ivPhoto);
        Matrix matrix = matrix_arr[index];
        findViewById(R.id.rl_openmenu).setVisibility(View.INVISIBLE);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mode = BaseActivity.DRAG;
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);

                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    start.set(event.getX(), event.getY());
                    mode = BaseActivity.ZOOM;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == BaseActivity.DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x,
                            event.getY() - start.y);

                }
                else if (mode == BaseActivity.ZOOM) {
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
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = BaseActivity.NONE;
                RefreshLayoutPos(ivPhoto);
                break;
        }
        ivPhoto.setImageMatrix(matrix);
        return true;
    }

    public void RefreshLayoutPos(ImageView ivPhoto)
    {
        int index=m_ivPhotos.indexOf(ivPhoto);
        Log.e("-----", "drag"+index);
        float scale0 = scale0s[index];
        Matrix matrix = matrix_arr[index];
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

        int wDrawingPane = ivPhoto.getWidth();
        int hDrawingPane = ivPhoto.getHeight();
        int wSrc = m_Bitmaps[index].getWidth();
        int hSrc =  m_Bitmaps[index].getHeight();
        int xInDrawingPane = (int)Math.abs(wSrc* scale - wDrawingPane)/2;
        int yInDrawingPane = (int)Math.abs(hSrc* scale - hDrawingPane)/2;
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
        ivPhoto.setImageMatrix(matrix);
    }

    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.button_accept) {
                boolean bAllPhotos = true;
                for(int i=0; i<mThemeInfo.ImageCount; i++) {
                    if(m_Bitmaps[i]==null) {
                        bAllPhotos = false;
                        break;
                    }
                }

                if(!bAllPhotos) {
                    Toast.makeText(mContext, "화상을 선택하십시오.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mResultBitmap = MakeResultBitmap();
                ((ImageView)findViewById(R.id.imageView_preview)).setImageBitmap(mResultBitmap);
                findViewById(R.id.rl_preview).setVisibility(View.VISIBLE);
                ImageUtils.saveImage(mResultBitmap,"", false);
                //m_onClickListener.onClick(Dialog_ThemeMultiSimple.this, DialogInterface.BUTTON_POSITIVE);
            }else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_ThemeMultiSimple.this, BUTTON_NEGATIVE);
            else if(v.getId() == R.id.btn_photo) {
//                GalleryUtils.openGalleryMulti(mParent, mThemeInfo.ImageCount);
                findViewById(R.id.rl_openmenu).setVisibility(View.VISIBLE);
            } else if(v.getId() == R.id.btn_photo1) {
                m_nPhotoIndex = 0;
                GalleryUtils.openGallery(mParent);
            }else if(v.getId() == R.id.btn_photo2 ){
                m_nPhotoIndex = 1;
                GalleryUtils.openGallery(mParent);
            }else if(v.getId() == R.id.btn_photo3) {
                m_nPhotoIndex = 2;
                GalleryUtils.openGallery(mParent);
            }else if(v.getId() == R.id.btn_photo4) {
                m_nPhotoIndex = 3;
                GalleryUtils.openGallery(mParent);
            }else if(v.getId() == R.id.btn_photo5) {
                m_nPhotoIndex = 4;
                GalleryUtils.openGallery(mParent);
            }else if(v.getId() == R.id.btn_photo6) {
                m_nPhotoIndex = 5;
                GalleryUtils.openGallery(mParent);
            }else if(v.getId() == R.id.btn_photo7) {
                m_nPhotoIndex = 6;
                GalleryUtils.openGallery(mParent);
            }  else if(v.getId() == R.id.iv_back) {
                findViewById(R.id.rl_preview).setVisibility(View.GONE);
            } else if(v.getId() == R.id.iv_save) {
                m_nResultButton = BUTTON_POSITIVE;
                AsyncTask_saveResult asyncTask_saveResult = new AsyncTask_saveResult();
                asyncTask_saveResult.execute();
            }else if(v.getId() == R.id.iv_phedit) {
                m_nResultButton = VisionPreviewDialog.BTN_PHEDIT;
                AsyncTask_saveResult asyncTask_saveResult = new AsyncTask_saveResult();
                asyncTask_saveResult.execute();
            }else if(v.getId() == R.id.iv_phsticker) {
                m_nResultButton = VisionPreviewDialog.BTN_STICKER;
                AsyncTask_saveResult asyncTask_saveResult = new AsyncTask_saveResult();
                asyncTask_saveResult.execute();
            }
        }
    };

    View.OnLongClickListener mIVLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            m_nPhotoIndex = m_ivPhotos.indexOf(v);
            GalleryUtils.openGallery(mParent);
            return false;
        }
    };

    @Override
    public void onRecyclerItemClickListener(Bitmap image, int imageIndex) {
        if(imageIndex==sub_CategoryID) return;
        sub_CategoryID = imageIndex;

        LoadTheme();
    }

    int m_nResultButton = 0;

    private class AsyncTask_saveResult extends AsyncTask<Void, Void, String> {
        Boolean bSuccess = true;
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();
        }

        @Override
        protected void onPostExecute(String fileName) {
            HideWaitDialog();
            m_strResultPath = fileName;
            m_onClickListener.onClick(Dialog_ThemeMultiSimple.this, m_nResultButton);
            File f = new File(fileName);
            String msg = "화상이 성과적으로 보관되였습니다.\n경로: 수정구슬/"+f.getName();
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            String fileName = ImageUtils.saveImage(mResultBitmap, "", false);
            return fileName;
        }
    };
}
