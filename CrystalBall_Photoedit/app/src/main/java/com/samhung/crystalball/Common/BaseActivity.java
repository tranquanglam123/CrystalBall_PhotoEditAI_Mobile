package com.samhung.crystalball.Common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.samhung.crystalball.photoeditor.R;

public class BaseActivity extends AppCompatActivity {
    int currentApiVersion;
    public String[] m_arrOldPaths = null;

    public static final int NONE = 0x100;
    public static final int DRAG = 0x101;
    public static final int ZOOM = 0x102;
    public static final int DRAW= 0x103;

    public RelativeLayout rl_waitView = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature( Window.FEATURE_NO_TITLE );
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        currentApiVersion = Build.VERSION.SDK_INT;

        final View decorView = getWindow().getDecorView();

        final int flags =  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                |View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE;
        decorView.setSystemUiVisibility(flags);
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                {
                    decorView.setSystemUiVisibility(flags);
                }
            }
        });

    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        if(currentApiVersion >= Build.VERSION_CODES.KITKAT && hasFocus)
        {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    private void removeTitleBar()
    {
 //       setTheme(android.R.style.Theme_NoTitleBar);
//        setTheme(style.Theme_NoTitleBar_Fullscreen);
//        setTheme(style.Theme_Black);
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        |View.SYSTEM_UI_FLAG_LOW_PROFILE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);

    }

    public void InitWaitView(ViewGroup rootView) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rl_waitView = (RelativeLayout)inflater.inflate(R.layout.wait_window, null);
        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootView.addView(rl_waitView, param);
        rl_waitView.bringToFront();
        rl_waitView.setClickable(true);
        rl_waitView.setVisibility(View.GONE);
    }

    public void ShowWaitDialog() {
        rl_waitView.setVisibility(View.VISIBLE);
        ImageView iv = (ImageView)rl_waitView.findViewById(R.id.iv_progress);
        AnimationDrawable animationDrawable = (AnimationDrawable)getResources().getDrawable(R.drawable.myprogress);
        iv.setImageDrawable(animationDrawable);
        animationDrawable.start();
        rl_waitView.bringToFront();
    }

    public void HideWaitDialog() {
        rl_waitView.setVisibility(View.GONE);
    }

    //////////////////////////////
    public float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    /** Calculate the mid point of the first two fingers */
    public void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

//    public static void showOverlay(BaseActivity activity){
//        LayoutInflater inflater = activity.getLayoutInflater();
//        LinearLayout layout = inflater.inflate(R.layout.overlay_layout, null);
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
//        params.gravity = Gravity.BOTTOM;
//        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        params.type = WindowManager.LayoutParams.TYPE_APPLICATION;
//        final WindowManager mWindowManager = activity.getWindowManager();
//        activity.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
//        mWindowManager.addView(layout, params);
//    }
//    public static void removeOverlay(){
//        windowManager.removeView(view);
//    }
}
