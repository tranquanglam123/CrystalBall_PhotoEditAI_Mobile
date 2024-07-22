package com.samhung.crystalball.photoeditor.Phototheme.Dialogs;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.Phototheme.Activities.PhotothemeActivity;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.CalendarInfo;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.CalendarUtil;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.ThemeInfo;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.ThemeViewTouchListener;
import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.GalleryUtils;
import com.samhung.crystalball.photoeditor.VisionMix.Dialogs.VisionPreviewDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Dialog_ThemeCalendar extends FullScreenDialog implements View.OnTouchListener, DialogThemeAdapter.OnItemClickListener{
    OnClickListener m_onClickListener = null;
    public Bitmap mBitmap = null;
    public Bitmap themeBitmap = null;
    public Bitmap themeBitmap_draw = null;
    Context mContext = null;
    PhotothemeActivity mParent = null;

    RelativeLayout mRelativeLayout_Work = null;
    int main_CategoryID = 0;
    int sub_CategoryID = 0;
    String themePath = "";
    ImageView ivPhoto = null;
    ImageView ivTheme = null;
    RecyclerView recycler_Themes = null;
    ThemeInfo mThemeInfo = null;
    CalendarInfo mCalendarInfo = null;
    private int mYear;
    private int mMonth;
    private int mDayOfMonth;

    private ArrayList<Bitmap> m_themeBitmapArray = new ArrayList<Bitmap>();

    public Dialog_ThemeCalendar(Context context , PhotothemeActivity parent, int mainCategory, int subCategory, ArrayList<Bitmap> thumbArray, OnClickListener onClickListener) {
        super(context);
        mContext = context;
        m_onClickListener = onClickListener;
        mParent = parent;
        main_CategoryID = mainCategory;
        sub_CategoryID = subCategory;
        themePath = String.format("/theme/%d/%d/src.png", main_CategoryID, sub_CategoryID);
//        mResultBitmap = Bitmap.createBitmap(mBitmap);
        m_themeBitmapArray = thumbArray;
//        InitThemeItemArray();

        mYear = Calendar.getInstance().getTime().getYear() + 1900;
        mMonth = Calendar.getInstance().getTime().getMonth() + 1;
        mDayOfMonth = Calendar.getInstance().getTime().getDate();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_theme_calendar);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));
        mRelativeLayout_Work = (RelativeLayout)findViewById(R.id.work_rl);
        ivTheme = (ImageView)findViewById(R.id.imageview_theme);
        ivPhoto = (ImageView)findViewById(R.id.imageview_photo);
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

        ivPhoto.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(mBitmap!=null)
                {
                    Refresh_PhotoView();
                }
            }
        });
        ImageView btnAccept = (ImageView)findViewById(R.id.button_accept);
        ImageView btnBack = (ImageView)findViewById(R.id.button_close);
        btnAccept.setOnClickListener(btnClickListener);
        btnBack.setOnClickListener(btnClickListener);
        ((ImageView)findViewById(R.id.btn_photo)).setOnClickListener(btnClickListener);
        findViewById(R.id.iv_back).setOnClickListener(btnClickListener);
        findViewById(R.id.iv_save).setOnClickListener(btnClickListener);
        findViewById(R.id.rl_date).setVisibility(View.INVISIBLE);

        findViewById(R.id.button_date).setOnClickListener(btnClickListener);
        findViewById(R.id.rl_date).setOnClickListener(btnClickListener);
        findViewById(R.id.button_confirm).setOnClickListener(btnClickListener);

        findViewById(R.id.btnUP_year).setOnClickListener(btnClickListener);
        findViewById(R.id.btnUP_month).setOnClickListener(btnClickListener);
        findViewById(R.id.btnUP_day).setOnClickListener(btnClickListener);

        findViewById(R.id.btnDWN_year).setOnClickListener(btnClickListener);
        findViewById(R.id.btnDWN_month).setOnClickListener(btnClickListener);
        findViewById(R.id.btnDWN_day).setOnClickListener(btnClickListener);
        findViewById(R.id.iv_phedit).setOnClickListener(btnClickListener);
        findViewById(R.id.iv_phsticker).setOnClickListener(btnClickListener);

        findViewById(R.id.btnUP_year).setOnLongClickListener(mLongClickListener);
        findViewById(R.id.btnUP_month).setOnLongClickListener(mLongClickListener);
        findViewById(R.id.btnUP_day).setOnLongClickListener(mLongClickListener);

        findViewById(R.id.btnDWN_year).setOnLongClickListener(mLongClickListener);
        findViewById(R.id.btnDWN_month).setOnLongClickListener(mLongClickListener);
        findViewById(R.id.btnDWN_day).setOnLongClickListener(mLongClickListener);

        mTxtYear = (TextView) findViewById(R.id.txtYY);
        mTxtMonth = (TextView) findViewById(R.id.txtMM);
        mTxtDay = (TextView) findViewById(R.id.txtDD);

        mTxtYear.setText(""+mYear);
        mTxtMonth.setText(""+mMonth);
        mTxtDay.setText(""+mDayOfMonth);

        LoadTheme();
    }

    boolean m_bThemeLoaded = false;
    void LoadTheme() {
        String infoPath =String.format("assets/theme/%d/%d/info.pos", main_CategoryID+1, sub_CategoryID+1);
        themePath = String.format("assets/theme/%d/%d/src.png", main_CategoryID+1, sub_CategoryID+1);
        mThemeInfo = ThemeInfo.LoadInfo(getContext(), infoPath);
        infoPath =String.format("assets/theme/%d/%d/calendar.pos", main_CategoryID+1, sub_CategoryID+1);
        mCalendarInfo = CalendarInfo.LoadInfo(getContext(), infoPath);
        try{
//            themeBitmap = BitmapFactory.decodeStream(getContext().getResources().getAssets().open(themePath));
            themeBitmap = BitmapFactory.decodeStream(MainActivity.gZipResourceFile.getInputStream(themePath));

            UpdateThemeBitmap();


            mRelativeLayout_Work.requestLayout();
            m_bThemeLoaded = true;

        } catch (Exception ex) {}
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

        ViewGroup.LayoutParams layoutParams = ivPhoto.getLayoutParams();
        layoutParams.width = (int)(photoViewWidth * scale);
        layoutParams.height = (int)(photoViewHeight * scale);
        ivPhoto.setLayoutParams(layoutParams);

        int nWidth = (int)(themeBitmap.getWidth() * scale); //mRelativeLayout_Work.getWidth();
        int nHeight = (int)(themeBitmap.getHeight() * scale); //mRelativeLayout_Work.getHeight();

        ivPhoto.setTranslationX(-nWidth/2+centerX * scale);
        ivPhoto.setTranslationY(-nHeight/2+centerY * scale);
        ivPhoto.setRotation(mThemeInfo.angle[0]);
    }

    void UnAdjustViewPosition() {
        Matrix m = ivTheme.getImageMatrix();
        float x[]=new float[9];
        m.getValues(x);
        float scale = x[Matrix.MSCALE_X];

        float centerX = mThemeInfo.centerX[0];
        float centerY = mThemeInfo.centerY[0];

        int nWidth = (int)(themeBitmap.getWidth() * scale); //mRelativeLayout_Work.getWidth();
        int nHeight = (int)(themeBitmap.getHeight() * scale); //mRelativeLayout_Work.getHeight();

        ivPhoto.setTranslationX(0-(nWidth/2+centerX * scale));
        ivPhoto.setTranslationY(0-(nHeight/2+centerY * scale));
    }

    String mPhotoPath = "";
    public PhotothemeActivity.LoadGalleryListener mLoadGalleryListener = new PhotothemeActivity.LoadGalleryListener() {
        @Override
        public void onLoadGallery(String path) {
            LoadPhoto(path);
            mPhotoPath = path;
        }

        @Override
        public void onLoadGalleryMulti(List<String> paths) {

        }
    };

    void UpdateThemeBitmap() {
        themeBitmap_draw = CalendarUtil.makeCalendar(getContext(), mCalendarInfo,themeBitmap, mYear, mMonth-1, mDayOfMonth);
        ivTheme.setImageBitmap(themeBitmap_draw);
    }
    void LoadPhoto(String strPath) {
        mBitmap = ImageUtils.decodeFile(strPath);
        //ivPhoto.setImageBitmap(mBitmap);
        Refresh_PhotoView();
    }
    void Refresh_PhotoView()
    {
        wDrawingPane = ivPhoto.getWidth();
        hDrawingPane = ivPhoto.getHeight();
        wSrc = mBitmap.getWidth();
        hSrc = mBitmap.getHeight();
        scale0 = Math.max(((float) ivPhoto.getHeight()) / hSrc, ((float) ivPhoto.getWidth()) / wSrc);
        matrix.reset();
        matrix.postScale(scale0, scale0);
        xInDrawingPane = (int)Math.abs(wSrc * scale0 - ivPhoto.getWidth()) / 2;
        yInDrawingPane = (int) Math.abs(hSrc * scale0 - ivPhoto.getHeight()) / 2;
        matrix.postTranslate(-xInDrawingPane, -yInDrawingPane);

        ivPhoto.setImageBitmap(mBitmap);
        ivPhoto.setImageMatrix(matrix);
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

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        Matrix m = ivTheme.getImageMatrix();
        float x[]=new float[9];
        m.getValues(x);
        float scale = x[Matrix.MSCALE_X];

        Bitmap photo = Bitmap.createBitmap(mThemeInfo.width[0], mThemeInfo.height[0],  Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(photo);
        Matrix matrix = new Matrix(ivPhoto.getImageMatrix());
        matrix.postScale(1/scale, 1/scale, 0,0);
        canvas.drawBitmap(mBitmap, matrix, paint);

        Matrix matrix1 = new Matrix();
        matrix1.postTranslate(mThemeInfo.centerX[0]-mThemeInfo.width[0]/2, mThemeInfo.centerY[0]-mThemeInfo.height[0]/2);
        if(mThemeInfo.angle[0] != 0)
            matrix1.postRotate(mThemeInfo.angle[0], mThemeInfo.centerX[0],mThemeInfo.centerY[0]);
        resultCanvas.drawBitmap(photo, matrix1, paint);

        resultCanvas.drawBitmap(themeBitmap_draw, 0, 0, paint);
        return resultBitmap;
    }

    int mode = BaseActivity.NONE;
    Matrix savedMatrix = new Matrix();
    PointF start = new PointF();
    PointF mid = new PointF();
    float oldDist = 0;
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        super.onTouchEvent(event);
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
                RefreshLayoutPos();
                break;
        }
        ivPhoto.setImageMatrix(matrix);
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

    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.button_accept) {
                if(mBitmap == null) {
                    Toast.makeText(mContext, "화상을 선택하십시오.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mResultBitmap = MakeResultBitmap();
                ((ImageView)findViewById(R.id.imageView_preview)).setImageBitmap(mResultBitmap);
                findViewById(R.id.rl_preview).setVisibility(View.VISIBLE);
                //m_onClickListener.onClick(Dialog_ThemeSimple.this, DialogInterface.BUTTON_POSITIVE);
            } else if(v.getId() == R.id.button_close)
                m_onClickListener.onClick(Dialog_ThemeCalendar.this, BUTTON_NEGATIVE);
            else if(v.getId() == R.id.btn_photo) {
                GalleryUtils.openGallery(mParent);
            } else if(v.getId() == R.id.iv_back) {
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
            } else if(v.getId() == R.id.button_date) {
                findViewById(R.id.rl_date).setVisibility(View.VISIBLE);
            } else if(v.getId() == R.id.rl_date) {
                v.setVisibility(View.INVISIBLE);
            } else if(v.getId() == R.id.btnUP_year ||v.getId() == R.id.btnDWN_year ||
                    v.getId() == R.id.btnUP_month ||v.getId() == R.id.btnDWN_month ||
                    v.getId() == R.id.btnUP_day ||v.getId() == R.id.btnDWN_day )
            {
                onArrowClickProc(v.getId());
            } else if(v.getId() == R.id.button_confirm){
                mYear =  Integer.parseInt(mTxtYear.getText().toString());
                mMonth = Integer.parseInt(mTxtMonth.getText().toString());
                mDayOfMonth = Integer.parseInt(mTxtDay.getText().toString());
                findViewById(R.id.rl_date).setVisibility(View.INVISIBLE);
                UpdateThemeBitmap();
            }
        }
    };

    View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            final  View view = v;
            Button btn = (Button)v.findViewById(R.id.btnUP_year);
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(view.isPressed())
                        handler.postDelayed(this, 200);
                    onArrowClickProc(view.getId());
                }
            }, 200);
            return false;
        }
    };
    @Override
    public void onRecyclerItemClickListener(Bitmap image, int imageIndex) {
        if(imageIndex==sub_CategoryID) return;
        sub_CategoryID = imageIndex;

        UnAdjustViewPosition();
        LoadTheme();
//        if(mBitmap!=null) {
//            UpdateThemeBitmap();
//        }
    }


    ///////////////////////
    private TextView mTxtYear = null;
    private TextView mTxtMonth = null;
    private TextView mTxtDay = null;

    private void onArrowClickProc(int resID) {
        switch (resID) {
            case R.id.btnUP_year:
                YearArrowButton(true);
                break;
            case R.id.btnDWN_year:
                YearArrowButton(false);
                break;
            case R.id.btnUP_month:
                MonthArrowButton(true);
                break;
            case R.id.btnDWN_month:
                MonthArrowButton(false);
                break;
            case R.id.btnUP_day:
                DayArrowButton(true);
                break;
            case R.id.btnDWN_day:
                DayArrowButton(false);
                break;
        }
    }

    private void YearArrowButton(boolean bIncrease) {
        String strTxt = mTxtYear.getText().toString();
        int nDay = Integer.parseInt(mTxtDay.getText().toString());
        int nMonth = Integer.parseInt(mTxtMonth.getText().toString());

        int nValue = Integer.parseInt(strTxt);
        if (bIncrease) {
            if (nValue < 2100) {
                nValue++;
            }
        } else {
            if (nValue > 1921) {
                nValue--;
            }
        }
        mTxtYear.setText("" + nValue);
        if((nDay > 28) && (nMonth == 2) && ((nValue%4)!=0))
        {
            nDay = 28;
            mTxtDay.setText(""+nDay);
        }
    }

    private void MonthArrowButton(boolean bIncrease) {
        String strTxt = mTxtMonth.getText().toString();
        int nValue = Integer.parseInt(strTxt);
        int nDay = Integer.parseInt(mTxtDay.getText().toString());

        if (bIncrease) {
            nValue++;
            if (nValue > 12) nValue = 1;
        } else {
            nValue--;
            if (nValue < 1) nValue = 12;
        }
        mTxtMonth.setText("" + nValue);
        if ((nValue == 4) || (nValue == 6) || (nValue == 9) || (nValue == 11)) {
            if (nDay == 31)
                nDay = 30;
        } else if (nValue == 2) {
            if (nDay > 28)
                nDay = 28;
        }
        mTxtDay.setText("" + nDay);
    }

    int[] nDaysOfMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private void DayArrowButton(boolean bIncrease) {
        String strTxt = mTxtDay.getText().toString();
        int nValue = Integer.parseInt(strTxt);
        int nMonth = Integer.parseInt(mTxtMonth.getText().toString());
        int nYear = Integer.parseInt(mTxtYear.getText().toString());
        if (bIncrease) {
            nValue++;
            if (nMonth != 2) {
                if (nValue > nDaysOfMonth[nMonth - 1]) nValue = 1;
            } else {
                if ((nYear % 4) == 0) {
                    if (nValue > 29) nValue = 1;
                } else {
                    if (nValue > 28) nValue = 1;
                }
            }
        } else {
            nValue--;
            if (nMonth != 2) {
                if (nValue < 1) nValue = nDaysOfMonth[nMonth - 1];
            } else {
                if ((nYear % 4) == 0) {
                    if (nValue < 1) nValue = 29;
                } else {
                    if (nValue < 1) nValue = 28;
                }

            }
        }
        mTxtDay.setText("" + nValue);
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
            m_onClickListener.onClick(Dialog_ThemeCalendar.this, m_nResultButton);
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
