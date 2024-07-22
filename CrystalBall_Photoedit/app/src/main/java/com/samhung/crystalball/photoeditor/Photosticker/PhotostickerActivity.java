package com.samhung.crystalball.photoeditor.Photosticker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.math.photostickersdk.Slate;
import com.math.photostickersdk.Vector2D;
import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.CustomAlertDialog;
import com.samhung.crystalball.Common.ImageUtils;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.Photosticker.Dialogs.Dialog_TextInput;
import com.samhung.crystalball.photoeditor.Photosticker.fragments.BrushPickerAdapter;
import com.samhung.crystalball.photoeditor.Photosticker.fragments.ColorPickerAdapter;
import com.samhung.crystalball.photoeditor.Photosticker.fragments.EmoticonFragment;
import com.samhung.crystalball.photoeditor.Photosticker.fragments.FontPickerAdapter;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.ThemeViewTouchListener;
import com.samhung.crystalball.photoeditor.R;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import com.samhung.crystalball.photoeditor.Photosticker.Views.SlidingUpPanelLayout;
import com.samhung.crystalball.photoeditor.Photosticker.fragments.EmojiFragment;
import com.samhung.crystalball.photoeditor.Photosticker.fragments.ImageFragment;
import com.samhung.crystalball.photoeditor.Photosticker.Views.SlidingUpPanelLayout;
import com.math.photostickersdk.BrushDrawingView;
import com.math.photostickersdk.OnPhotoStickerSDKListener;
import com.math.photostickersdk.PhotoStickerSDK;
import com.math.photostickersdk.ViewType;
import com.samhung.crystalball.photoeditor.Utilis.MeasureUtil;
import com.samhung.crystalball.photoeditor.VisionMix.Dialogs.VisionPreviewDialog;
import com.samhung.crystalball.widgets.Slider;

import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Typeface;

import org.CrystalBall.Engine;

public class PhotostickerActivity extends BaseActivity  implements OnPhotoStickerSDKListener {
    Bitmap mBitmap = null;

    ImageView ivPhoto = null;
    ImageView ivFocus = null;

     private PhotoStickerSDK photoStickerSDK  = null;
    private RelativeLayout parentImageRelativeLayout  = null;
    private RecyclerView drawingViewColorPickerRecyclerView = null;
    private RecyclerView drawingViewBrushPickerRecyclerView = null;

    private Typeface emojiFont  = null;
    private SlidingUpPanelLayout mLayout  = null;
    private Slate slateView = null;
//    private BrushDrawingView brushDrawingView = null;
    ViewPager pager = null;

    int wSrc = 0;
    int hSrc = 0;
    int wDrawingPane = 0;
    int hDrawingPane = 0;
    int xInDrawingPane =0;
    int yInDrawingPane =0;
    int layout_Left = 0;
    int layout_Right = 0;
    float scale0 =1;

    public int colorCodeTextView = -1;
    public Typeface textFont = null;
    public ArrayList<Typeface> fontsList;
    public ArrayList<Integer> colorPickerColors;
    ImageView doneDrawingTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photosticker);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        InitWaitView((RelativeLayout)findViewById(R.id.MainView));

        String imagePath = getIntent().getExtras().getString("selectedImagePath");
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 1;
//        options.inDither = false;                     //Disable Dithering mode
//        mBitmap = BitmapFactory.decodeFile(imagePath, options);
        mBitmap = ImageUtils.decodeFile(imagePath);

        InitControls();
    }

    private boolean isKeyboardShown(View rootView) {
        /* 128dp = 32dp * 4, minimum button height 32dp and generic 4 rows soft keyboard */
        final int SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD = 128;

        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        /* heightDiff = rootView height - status bar height (r.top) - visible frame height (r.bottom - r.top) */
        int heightDiff = rootView.getBottom() - r.bottom;
        /* Threshold size: dp to pixels, multiply with display density */
        boolean isKeyboardShown = heightDiff > SOFT_KEYBOARD_HEIGHT_DP_THRESHOLD * dm.density;


        return isKeyboardShown;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (mLayout != null && mLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
             mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return;
        }
        if(m_bBrushDrawingMode) {
            updateBrushDrawingView(false);
            return;
        }
        if(m_bTextMode) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

            if(!isKeyboardShown(findViewById(R.id.fr_pop))) {
                findViewById(R.id.btn_text).setBackground(getResources().getDrawable(R.drawable.rect_btn_back));
                findViewById(R.id.btn_text).setEnabled(true);
                m_bTextMode = false;
                findViewById(R.id.fr_pop).setVisibility(View.INVISIBLE);
                return;
            }
            return;
        }
//        else {
            quitActivity();
//        }

    }

    private void InitControls()
    {
        ivPhoto = (ImageView) findViewById(R.id.iv_photo);
        ivFocus = (ImageView) findViewById(R.id.iv_focus);
        ivFocus.setVisibility(View.INVISIBLE);
        mLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_layout);
        parentImageRelativeLayout = (RelativeLayout) findViewById(R.id.parent_image_rl);
        RelativeLayout deleteRelativeLayout = (RelativeLayout) findViewById(R.id.delete_rl);
        deleteRelativeLayout.setVisibility(View.INVISIBLE);
        slateView = (Slate) findViewById(R.id.drawing_view);
        doneDrawingTextView = (ImageView) findViewById(R.id.done_drawing_tv);
        doneDrawingTextView.setOnClickListener(btnClickListener);
        findViewById(R.id.iv_brushSize).setOnClickListener(btnClickListener);

        drawingViewColorPickerRecyclerView = (RecyclerView) findViewById(R.id.drawing_view_color_picker_recycler_view);
        drawingViewBrushPickerRecyclerView = (RecyclerView) findViewById(R.id.drawing_view_brush_picker_recycler_view);
        pager = (ViewPager) findViewById(R.id.image_emoji_view_pager);

        final List<Fragment> fragmentsList = new ArrayList<>();
        fragmentsList.add(new ImageFragment());
        fragmentsList.add(new EmoticonFragment());
        //fragmentsList.add(new EmojiFragment());

        PreviewSlidePagerAdapter adapter = new PreviewSlidePagerAdapter(getSupportFragmentManager(), fragmentsList);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(2);

        photoStickerSDK = new PhotoStickerSDK.PhotoEditorSDKBuilder(PhotostickerActivity.this)
                .parentView(parentImageRelativeLayout) // add parent image view
                .childView(ivPhoto) // add the desired image view
                .deleteView(deleteRelativeLayout) // add the deleted view that will appear during the movement of the views
                .slateView(slateView) // add the brush drawing view that is responsible for drawing on the image view
                .buildPhotoEditorSDK(); // build photo editor sdk
        photoStickerSDK.setOnPhotoEditorSDKListener(this);


        Typeface newFont = Typeface.createFromAsset(getAssets(), "fonts/Eventtus-Icons.ttf");
        emojiFont = Typeface.createFromAsset(getAssets(), "fonts/emojione-android.ttf");

//        TextView tvDelete = (TextView)findViewById(R.id.delete_tv);
//        tvDelete.setTypeface(newFont);

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0)
                    mLayout.setScrollableView(((ImageFragment) fragmentsList.get(position)).imageRecyclerView);
                else if (position == 1)
                    mLayout.setScrollableView(((EmoticonFragment) fragmentsList.get(position)).imageRecyclerView);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        parentImageRelativeLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (wDrawingPane == 0 || hDrawingPane == 0 || (wDrawingPane != right - left) || (hDrawingPane != bottom - top)) {
                    layout_Left = left;
                    layout_Right = right;
                    wDrawingPane = right - left;
                    hDrawingPane = bottom - top;
                    wSrc = mBitmap.getWidth();
                    hSrc = mBitmap.getHeight();
                    ivPhoto.setImageBitmap(mBitmap);
                    scale0 = Math.min(((float) hDrawingPane) / hSrc, ((float) wDrawingPane) / wSrc);

                    View view = findViewById(R.id.rl_workarea);
                    parentImageRelativeLayout.setOnTouchListener(new StickerTouchListener(view.getWidth(), view.getHeight()));
//                    landmark_task = new AsyncTask_getLandmark();
//                    landmark_task.execute();
                }
            }
        });

        ivPhoto.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
               ViewGroup.LayoutParams params = slateView.getLayoutParams();
               params.width = right-left;
               params.height = bottom - top;
               slateView.setLayoutParams(params);

            }
        });

        InitColorList();
        InitFontList();

        ((Slider)findViewById(R.id.slider_brushSize)).setOnValueChangeListener(sliderValueChangeListener);
    }

    void InitFontList() {
        try {
            fontsList = new ArrayList<>();
            String[] fontNames = getAssets().list("font");
            for(int i=0; i<fontNames.length; i++) {
                Typeface font = Typeface.createFromAsset(getAssets(), "font/" + fontNames[i]);
                fontsList.add(font);
                Log.e("-----"+i,fontNames[i]);
            }
        }catch (IOException ex) {}
    }

    void InitColorList() {
        colorPickerColors = new ArrayList<>();
        colorPickerColors.add(Color.TRANSPARENT);
        colorPickerColors.add(getResources().getColor(R.color.black));
        colorPickerColors.add(getResources().getColor(R.color.blue_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.brown_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.green_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.orange_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.red_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.red_orange_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.sky_blue_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.violet_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.white));
        colorPickerColors.add(getResources().getColor(R.color.yellow_color_picker));
        colorPickerColors.add(getResources().getColor(R.color.yellow_green_color_picker));
    }
    @Override
    protected void onStart() {
        super.onStart();
        landmark_task = new AsyncTask_getLandmark();
        landmark_task.execute();
    }

    View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.done_drawing_tv) {
                updateBrushDrawingView(false);
            }else if(v.getId() == R.id.iv_brushSize) {
                RelativeLayout brushsliderContainer = (RelativeLayout)findViewById(R.id.rl_brushSlider);
                if(brushsliderContainer.getVisibility() == View.INVISIBLE)
                    brushsliderContainer.setVisibility(View.VISIBLE);
                else
                    brushsliderContainer.setVisibility(View.INVISIBLE);
            }
        }
    };

    Slider.OnValueChangeListener sliderValueChangeListener = new Slider.OnValueChangeListener() {
        @Override
        public void onProgressChanged(Slider slider, int progress, boolean fromUser) {
            photoStickerSDK.setBrushSize(10+progress);
        }

        @Override
        public void onStartTrackingTouch(Slider slider) {

        }

        @Override
        public void onStopTrackingTouch(Slider seekBar) {
            findViewById(R.id.rl_brushSlider).setVisibility(View.INVISIBLE);
        }
    };

    private boolean stringIsNotEmpty(String string) {
        if (string != null && !string.equals("null")) {
            if (!string.trim().equals("")) {
                return true;
            }
        }
        return false;
    }

    private void eraseDrawing() {
       // photoStickerSDK.brushEraser();
    }

    private void undoViews() {
        photoStickerSDK.viewUndo();
    }

    private void clearAllViews() {
        photoStickerSDK.clearAllViews();
    }

    PopupWindow pop = null;
    boolean m_bTextMode = false;
    private void openAddTextPopupWindow(final TextView txview, String text, int colorCode, Typeface font) {
        m_bTextMode = true;
        colorCodeTextView = colorCode;
        textFont = font;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View addTextPopupWindowRootView = inflater.inflate(R.layout.add_text_popup_window, null);
        RelativeLayout rl_tvarea = (RelativeLayout)addTextPopupWindowRootView.findViewById(R.id.rl_textarea);

        final EditText addTextEditText = (EditText) addTextPopupWindowRootView.findViewById(R.id.add_text_edit_text);
        addTextEditText.setTypeface(textFont);
        TextView addTextDoneTextView = (TextView) addTextPopupWindowRootView.findViewById(R.id.add_text_done_tv);
        TextView addTextCloseTextView = (TextView) addTextPopupWindowRootView.findViewById(R.id.add_text_close_tv);
        RecyclerView addTextColorPickerRecyclerView = (RecyclerView) addTextPopupWindowRootView.findViewById(R.id.add_text_color_picker_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(PhotostickerActivity.this, LinearLayoutManager.HORIZONTAL, false);
        addTextColorPickerRecyclerView.setLayoutManager(layoutManager);
        addTextColorPickerRecyclerView.setHasFixedSize(true);
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(PhotostickerActivity.this, colorPickerColors);
        colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
            @Override
            public void onColorPickerClickListener(int colorCode) {
                addTextEditText.setTextColor(colorCode);
                colorCodeTextView = colorCode;
            }
        });
        addTextColorPickerRecyclerView.setAdapter(colorPickerAdapter);

        RecyclerView addTextFontPickerRecyclerView = (RecyclerView)addTextPopupWindowRootView.findViewById(R.id.add_text_font_picker_recycler_view);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(PhotostickerActivity.this, LinearLayoutManager.HORIZONTAL, false);
        addTextFontPickerRecyclerView.setLayoutManager(layoutManager1);
        addTextFontPickerRecyclerView.setHasFixedSize(true);
        FontPickerAdapter fontPickerAdapter = new FontPickerAdapter(PhotostickerActivity.this, fontsList);
        fontPickerAdapter.setOnFontPickerClickListener(new FontPickerAdapter.OnFontPickerClickListener() {
            @Override
            public void onFontPickerClickListener(Typeface font) {
                addTextEditText.setTypeface(font);
                textFont = font;
            }
        });
        addTextFontPickerRecyclerView.setAdapter(fontPickerAdapter);

        if (stringIsNotEmpty(text)) {
            addTextEditText.setText(text);
            addTextEditText.setTextColor(colorCode == -1 ? getResources().getColor(R.color.white) : colorCodeTextView);
        } else  {
            addTextEditText.setText("");
            addTextEditText.setTextColor(colorCode == -1 ? getResources().getColor(R.color.white) : colorCodeTextView);
        }
//        final PopupWindow
        pop = new PopupWindow(findViewById(R.id.rl_workarea));
        pop.setContentView(addTextPopupWindowRootView);
        pop.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        pop.setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        //pop.setHeight(findViewById(R.id.rl_workarea).getMeasuredHeight());

        pop.setFocusable(true);
//        pop.setBackgroundDrawable(null);
        pop.setBackgroundDrawable(new BitmapDrawable());
//        pop.showAtLocation(addTextPopupWindowRootView, Gravity.TOP, 0, 0);
        pop.showAtLocation(findViewById(R.id.rl_workarea), Gravity.TOP, 0, 0);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        addTextEditText.requestFocus();
        imm.toggleSoftInput(0, InputMethodManager.HIDE_IMPLICIT_ONLY);
//        imm.showSoftInput(addTextEditText, InputMethodManager.HIDE_IMPLICIT_ONLY);
        rl_tvarea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(txview==null)
//                    addText(addTextEditText.getText().toString(), colorCodeTextView, textFont);
//                else {
//                    String text = addTextEditText.getText().toString();
//                    if(text.trim().isEmpty()) {
//                        parentImageRelativeLayout.removeView(txview);
//                        photoStickerSDK.addedViews.remove(txview);
//                    } else {
//                        txview.setText(text);
//                        txview.setTextColor(colorCodeTextView);
//                        txview.setTypeface(textFont);
//                    }
//                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                //if(pop.isShowing())
                    pop.dismiss();
                findViewById(R.id.btn_text).setBackground(getResources().getDrawable(R.drawable.rect_btn_back));
                findViewById(R.id.btn_text).setEnabled(true);
                m_bTextMode = false;
            }
        });
        addTextCloseTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                //if(pop.isShowing())
                    pop.dismiss();
                findViewById(R.id.btn_text).setBackground(getResources().getDrawable(R.drawable.rect_btn_back));
                findViewById(R.id.btn_text).setEnabled(true);
                m_bTextMode = false;
            }
        });

        findViewById(R.id.btn_text).setBackgroundColor(getResources().getColor(R.color.color29));
        findViewById(R.id.btn_text).setEnabled(false);

        pop.getContentView().setFocusableInTouchMode(true);
        pop.getContentView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() ==  MotionEvent.ACTION_UP) {
                    if(!isKeyboardShown(v)) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.SHOW_FORCED);
                        //if(pop.isShowing())
                        pop.dismiss();
                        findViewById(R.id.btn_text).setBackground(getResources().getDrawable(R.drawable.rect_btn_back));
                        findViewById(R.id.btn_text).setEnabled(true);
                        m_bTextMode = false;
                    }
                    return true;
                }
                return false;
            }
        });

        pop.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if(txview==null)
                    addText(addTextEditText.getText().toString(), colorCodeTextView, textFont);
                else {
                    String text = addTextEditText.getText().toString();
                    if(text.trim().isEmpty()) {
                        parentImageRelativeLayout.removeView(txview);
                        photoStickerSDK.addedViews.remove(txview);
                    } else {
                        txview.setText(text);
                        txview.setTextColor(colorCodeTextView);
                        txview.setTypeface(textFont);
                    }
                }
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(addTextEditText.getWindowToken(), 0);
                //if(pop.isShowing())
                pop.dismiss();
                findViewById(R.id.btn_text).setBackground(getResources().getDrawable(R.drawable.rect_btn_back));
                findViewById(R.id.btn_text).setEnabled(true);
                m_bTextMode = false;
            }
        });
    }

//    private void openAddTextPopupWindow(final TextView txview, String text, int colorCode, Typeface font) {
//        m_bTextMode = true;
//        colorCodeTextView = colorCode;
//        textFont = font;
//       // LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//       // View addTextPopupWindowRootView = inflater.inflate(R.layout.add_text_popup_window, null);
//        RelativeLayout rl_tvarea = (RelativeLayout)findViewById(R.id.rl_textarea);
//
//        final EditText addTextEditText = (EditText) findViewById(R.id.add_text_edit_text);
//        addTextEditText.setTypeface(textFont);
//        TextView addTextDoneTextView = (TextView) findViewById(R.id.add_text_done_tv);
//        TextView addTextCloseTextView = (TextView) findViewById(R.id.add_text_close_tv);
//        RecyclerView addTextColorPickerRecyclerView = (RecyclerView) findViewById(R.id.add_text_color_picker_recycler_view);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(PhotostickerActivity.this, LinearLayoutManager.HORIZONTAL, false);
//        addTextColorPickerRecyclerView.setLayoutManager(layoutManager);
//        addTextColorPickerRecyclerView.setHasFixedSize(true);
//        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(PhotostickerActivity.this, colorPickerColors);
//        colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
//            @Override
//            public void onColorPickerClickListener(int colorCode) {
//                addTextEditText.setTextColor(colorCode);
//                colorCodeTextView = colorCode;
//            }
//        });
//        addTextColorPickerRecyclerView.setAdapter(colorPickerAdapter);
//
//        RecyclerView addTextFontPickerRecyclerView = (RecyclerView)findViewById(R.id.add_text_font_picker_recycler_view);
//        LinearLayoutManager layoutManager1 = new LinearLayoutManager(PhotostickerActivity.this, LinearLayoutManager.HORIZONTAL, false);
//        addTextFontPickerRecyclerView.setLayoutManager(layoutManager1);
//        addTextFontPickerRecyclerView.setHasFixedSize(true);
//        FontPickerAdapter fontPickerAdapter = new FontPickerAdapter(PhotostickerActivity.this, fontsList);
//        fontPickerAdapter.setOnFontPickerClickListener(new FontPickerAdapter.OnFontPickerClickListener() {
//            @Override
//            public void onFontPickerClickListener(Typeface font) {
//                addTextEditText.setTypeface(font);
//                textFont = font;
//            }
//        });
//        addTextFontPickerRecyclerView.setAdapter(fontPickerAdapter);
//
//        if (stringIsNotEmpty(text)) {
//            addTextEditText.setText(text);
//            addTextEditText.setTextColor(colorCode == -1 ? getResources().getColor(R.color.white) : colorCodeTextView);
//        } else  {
//            addTextEditText.setText("");
//            addTextEditText.setTextColor(colorCode == -1 ? getResources().getColor(R.color.white) : colorCodeTextView);
//        }
//
//        findViewById(R.id.fr_pop).setVisibility(View.VISIBLE);
//
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//       // imm.toggleSoftInput(0, 0);
//        addTextEditText.requestFocus();
//        imm.showSoftInput(addTextEditText, InputMethodManager.SHOW_FORCED);
//        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
////        addTextDoneTextView.setOnClickListener(new View.OnClickListener() {
//        rl_tvarea.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(txview==null)
//                    addText(addTextEditText.getText().toString(), colorCodeTextView, textFont);
//                else {
//                    String text = addTextEditText.getText().toString();
//                    if(text.trim().isEmpty()) {
//                        parentImageRelativeLayout.removeView(txview);
//                        photoStickerSDK.addedViews.remove(txview);
//                    } else {
//                        txview.setText(text);
//                        txview.setTextColor(colorCodeTextView);
//                        txview.setTypeface(textFont);
//                    }
//                }
//                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//                findViewById(R.id.fr_pop).setVisibility(View.INVISIBLE);
//                findViewById(R.id.btn_text).setBackground(getResources().getDrawable(R.drawable.rect_btn_back));
//                findViewById(R.id.btn_text).setEnabled(true);
//                m_bTextMode = false;
//            }
//        });
//
//        findViewById(R.id.btn_text).setBackgroundColor(getResources().getColor(R.color.color29));
//        findViewById(R.id.btn_text).setEnabled(false);
//
//    }

//    private void openAddTextPopupWindow(final TextView txview, String text, int colorCode, Typeface font) {
//        m_bTextMode = true;
//        colorCodeTextView = colorCode;
//        textFont = font;
//
//        Dialog_TextInput dlg = new Dialog_TextInput(this, onTextDlgClickListener, text, font, colorCode);
//        dlg.show();
//    }
//
//    DialogInterface.OnClickListener onTextDlgClickListener = new DialogInterface.OnClickListener() {
//        @Override
//        public void onClick(DialogInterface dialog, int which) {
//
//        }
//    };
    boolean m_bBrushDrawingMode = false;
    private void updateBrushDrawingView(boolean brushDrawingMode) {
        m_bBrushDrawingMode = brushDrawingMode;
        photoStickerSDK.setBrushDrawingMode(brushDrawingMode);
        if (brushDrawingMode) {
            updateView(View.GONE);
            findViewById(R.id.ll_bottombar).setVisibility(View.VISIBLE);
//            drawingViewColorPickerRecyclerView.setVisibility(View.VISIBLE);
            findViewById(R.id.drawing_view_picker_panel).setVisibility(View.VISIBLE);
            doneDrawingTextView.setVisibility(View.VISIBLE);

            if(drawingViewColorPickerRecyclerView.getAdapter() == null) {
                ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(PhotostickerActivity.this, colorPickerColors);
                colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
                    @Override
                    public void onColorPickerClickListener(int colorCode) {
                        photoStickerSDK.setBrushColor(colorCode);
                    }
                });
                drawingViewColorPickerRecyclerView.setAdapter(colorPickerAdapter);
                LinearLayoutManager layoutManager = new LinearLayoutManager(PhotostickerActivity.this, LinearLayoutManager.HORIZONTAL, false);
                drawingViewColorPickerRecyclerView.setLayoutManager(layoutManager);
                drawingViewColorPickerRecyclerView.setHasFixedSize(true);
                drawingViewColorPickerRecyclerView.setBackgroundColor(Color.argb(0xff, 254, 168, 187));
                photoStickerSDK.setBrushColor(Color.BLACK);
            }
            if(drawingViewBrushPickerRecyclerView.getAdapter() == null) {
                BrushPickerAdapter brushPickerAdapter = new BrushPickerAdapter(PhotostickerActivity.this);
                brushPickerAdapter.setOnBrushPickerClickListener(new BrushPickerAdapter.OnBrushPickerClickListener() {
                    @Override
                    public void onBrushPickerClickListener(int position) {
                        photoStickerSDK.setBrushType(position);
                    }
                });
                drawingViewBrushPickerRecyclerView.setAdapter(brushPickerAdapter);
                LinearLayoutManager layoutManager_brush = new LinearLayoutManager(PhotostickerActivity.this, LinearLayoutManager.HORIZONTAL, false);
                drawingViewBrushPickerRecyclerView.setLayoutManager(layoutManager_brush);
                drawingViewBrushPickerRecyclerView.setHasFixedSize(true);
            }

            findViewById(R.id.btn_draw).setBackgroundColor(getResources().getColor(R.color.color29));
            findViewById(R.id.drawing_view).setClickable(true);
            findViewById(R.id.drawing_view).bringToFront();

            photoStickerSDK.setBrushSize(((Slider)findViewById(R.id.slider_brushSize)).getProgress());

        } else {
            updateView(View.VISIBLE);
            findViewById(R.id.btn_draw).setBackground(getResources().getDrawable(R.drawable.rect_btn_back));
            findViewById(R.id.drawing_view).setClickable(false);
//            drawingViewColorPickerRecyclerView.setVisibility(View.GONE);
            findViewById(R.id.drawing_view_picker_panel).setVisibility(View.GONE);
            doneDrawingTextView.setVisibility(View.GONE);
            findViewById(R.id.rl_brushSlider).setVisibility(View.INVISIBLE);
        }
    }

    private void updateView(int visibility) {
        findViewById(R.id.ll_bottombar).setVisibility(visibility);
        findViewById(R.id.iv_back).setVisibility(visibility);
        findViewById(R.id.iv_undo).setVisibility(visibility);
        findViewById(R.id.iv_save).setVisibility(visibility);
    }

    private void returnBackWithSavedImage() {
        updateView(View.GONE);
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        parentImageRelativeLayout.setLayoutParams(layoutParams);
        new CountDownTimer(1000, 500) {
            public void onTick(long millisUntilFinished) {

            }

            public void onFinish() {
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageName = "IMG_" + timeStamp + ".jpg";
                Intent returnIntent = new Intent();
                Bitmap bm = makeResultBitmap();
                returnIntent.putExtra("imagePath", ImageUtils.saveImage(bm, "", false));
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }.start();
    }

    private Bitmap makeResultBitmap() {
        Bitmap ret = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), mBitmap.getConfig());
        Canvas tmpCanvas = new Canvas(ret);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        Matrix fMatrix = ivPhoto.getImageMatrix();
        float[] x = new float[9];
        fMatrix.getValues(x);
        tmpCanvas.drawBitmap(mBitmap, 0,0, paint);
        List<View> addedViews = photoStickerSDK.getAddedViews();
        for(int i=0; i<addedViews.size(); i++) {
            View view = addedViews.get(i);
            view.setBackground(null);
            view.setDrawingCacheEnabled(true);
            view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            Bitmap bm = view.getDrawingCache();
            Matrix matrix = new Matrix(view.getMatrix());
            matrix.postTranslate((wSrc *x[Matrix.MSCALE_X] -view.getWidth())/2, (hSrc * x[Matrix.MSCALE_X]-view.getHeight())/2);
            matrix.postScale(1/x[Matrix.MSCALE_X], 1/x[Matrix.MSCALE_X]);
            tmpCanvas.drawBitmap(bm, matrix, paint);
            view.setDrawingCacheEnabled(false);
        }

        //brushDrawingView.setDrawingCacheEnabled(true);
        Matrix matrix_brushView = new Matrix(slateView.getMatrix());
        matrix_brushView.postTranslate((wSrc *x[Matrix.MSCALE_X] -slateView.getWidth())/2, (hSrc * x[Matrix.MSCALE_X]-slateView.getHeight())/2);
        matrix_brushView.postScale(1/x[Matrix.MSCALE_X], 1/x[Matrix.MSCALE_X]);
        Bitmap bmBrushView = slateView.getBitmap();
        if(bmBrushView != null)
            tmpCanvas.drawBitmap(bmBrushView, matrix_brushView, paint);
        //brushDrawingView.setDrawingCacheEnabled(false);

//        brushDrawingView.setDrawingCacheEnabled(true);
//        Matrix matrix_brushView = new Matrix(brushDrawingView.getMatrix());
//        matrix_brushView.postTranslate((wSrc *x[Matrix.MSCALE_X] -brushDrawingView.getWidth())/2, (hSrc * x[Matrix.MSCALE_X]-brushDrawingView.getHeight())/2);
//        matrix_brushView.postScale(1/x[Matrix.MSCALE_X], 1/x[Matrix.MSCALE_X]);
//        Bitmap bmBrushView = brushDrawingView.getDrawingCache();
//        if(bmBrushView != null)
//            tmpCanvas.drawBitmap(bmBrushView, matrix_brushView, paint);
//        brushDrawingView.setDrawingCacheEnabled(false);
        return ret;
    }

    public void onButtonClick(View view) {
        if(view.getId() == R.id.btn_image) {
            updateBrushDrawingView(false);
            pager.setCurrentItem(0);
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        } else if(view.getId() == R.id.btn_emoj) {
            updateBrushDrawingView(false);
            pager.setCurrentItem(1);
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        } else if(view.getId() == R.id.btn_text) {
            updateBrushDrawingView(false);
            openAddTextPopupWindow(null,"", -1, fontsList.get(0));
        } else if(view.getId() == R.id.btn_draw) {
            updateBrushDrawingView(true);
        } else if(view.getId() == R.id.iv_undo) {
            undoViews();
        } else if(view.getId() == R.id.iv_focus) {
            selectFace();
        } else if(view.getId() == R.id.iv_save) {
            //returnBackWithSavedImage();
            VisionPreviewDialog vpd = new VisionPreviewDialog(this, makeResultBitmap(), previewDialogClickListener,2);
            vpd.show();
        } else if(view.getId() == R.id.iv_back) {
            quitActivity();
        }
    }

    public void quitActivity()
    {
        CustomAlertDialog.Builder alert_confirm = new CustomAlertDialog.Builder(this);
        alert_confirm.setMessage("작업을 중지하겠습니까?\n작업하던 내용이 모두 삭제됩니다.").setCancelable(true).setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_CANCELED);
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
    public void addEmoji(String emojiName) {
        photoStickerSDK.addEmoji(emojiName, emojiFont);
        if (mLayout != null)
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    int drawableWidth = 0;
    int drawableHeight = 0;
    public void addImage(Bitmap image, int nImageIndex) {
        drawableWidth = image.getWidth();
        drawableHeight = image.getHeight();

        photoStickerSDK.addImage(image, nImageIndex);
        if (mLayout != null)
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
    public void addEmoticon(Bitmap image, int nImageIndex) {
        drawableWidth = image.getWidth();
        drawableHeight = image.getHeight();

        photoStickerSDK.addEmoticon(image, nImageIndex);
        if (mLayout != null)
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }
    private void addText(String text, int colorCodeTextView, Typeface font) {
        photoStickerSDK.addText(text, colorCodeTextView, font);
    }

    @Override
    public void onEditTextChangeListener(TextView view, String text, int colorCode, Typeface font) {
        openAddTextPopupWindow(view, text, colorCode, font);
    }

    @Override
    public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
        if (numberOfAddedViews > 0) {
            findViewById(R.id.iv_undo).setVisibility(View.VISIBLE);
        }
        if(m_nFaceCount > 1) ivFocus.bringToFront();
        switch (viewType) {
            case BRUSH_DRAWING:
                Log.i("BRUSH_DRAWING", "onAddViewListener");
                break;
            case EMOJI:
                Log.i("EMOJI", "onAddViewListener");
                break;
            case IMAGE:
                Log.i("IMAGE", "onAddViewListener");
                if(m_nFaceCount > 0 ) {
                    List<View> addedViews = photoStickerSDK.getAddedViews();
                    if (numberOfAddedViews >= 0 && numberOfAddedViews < 6) {
                        AdjustItemPosition.adjustposition_glass(PhotostickerActivity.this, addedViews.get(addedViews.size() - 1));
                    } else if (numberOfAddedViews == 8) {
                        AdjustItemPosition.adjustposition_mouthRose(PhotostickerActivity.this,addedViews.get(addedViews.size() - 1));
                    } else if(numberOfAddedViews == 10 || numberOfAddedViews == 12 || numberOfAddedViews==23 || numberOfAddedViews==24 || numberOfAddedViews==27) {
                        AdjustItemPosition.adjustposition_headobject(PhotostickerActivity.this, addedViews.get(addedViews.size() - 1), numberOfAddedViews);
                    } else if(numberOfAddedViews == 9 || numberOfAddedViews==17 || numberOfAddedViews==26 || numberOfAddedViews==28) {
                        AdjustItemPosition.adjustposition_noseobject(PhotostickerActivity.this , addedViews.get(addedViews.size() - 1), numberOfAddedViews);
                    }
                }
                break;
            case TEXT:
                Log.i("TEXT", "onAddViewListener");
                break;
        }
    }

    @Override
    public void onRemoveViewListener(int numberOfAddedViews) {
        Log.i("STICKER", "onRemoveViewListener");
        if (numberOfAddedViews == 0) {
            findViewById(R.id.iv_undo).setVisibility(View.GONE);
//            undoTextTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onStartViewChangeListener(ViewType viewType) {
        switch (viewType) {
            case BRUSH_DRAWING:
                Log.i("BRUSH_DRAWING", "onStartViewChangeListener");
                break;
            case EMOJI:
                Log.i("EMOJI", "onStartViewChangeListener");
                break;
            case IMAGE:
                Log.i("IMAGE", "onStartViewChangeListener");
                break;
            case TEXT:
                Log.i("TEXT", "onStartViewChangeListener");
                break;
        }
    }

    @Override
    public void onStopViewChangeListener(ViewType viewType) {
        if(m_nFaceCount > 1) ivFocus.bringToFront();
        switch (viewType) {
            case BRUSH_DRAWING:
                Log.i("BRUSH_DRAWING", "onStopViewChangeListener");
                break;
            case EMOJI:
                Log.i("EMOJI", "onStopViewChangeListener");
                break;
            case IMAGE:
                Log.i("IMAGE", "onStopViewChangeListener");
                break;
            case TEXT:
                Log.i("TEXT", "onStopViewChangeListener");
                break;
        }
    }



    public PointF EnginePointToAppPoint(int x, int y, int ImgWidth, int ImgHeight)
    {
        Matrix fMatrix = ivPhoto.getImageMatrix();
        float[] m = new float[9];
        fMatrix.getValues(m);

        PointF pt = new PointF();
        float nWidth = wSrc * m[Matrix.MSCALE_X];
        float nHeight = hSrc * m[Matrix.MSCALE_Y];

        pt.x = x * nWidth / ImgWidth;
        pt.y = y * nHeight / ImgHeight;

        return pt;
    }

    private class PreviewSlidePagerAdapter extends FragmentStatePagerAdapter {
        private List<Fragment> mFragments;

        PreviewSlidePagerAdapter(FragmentManager fm, List<Fragment> fragments) {
            super(fm);
            mFragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            if (mFragments == null) {
                return (null);
            }
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    private void selectFace() {
        if(m_nFaceCount < 1) return;

        if(m_nSelectedFaceIndex < 0){
            m_nSelectedFaceIndex = 0;
        } else {
            m_nSelectedFaceIndex ++;
            m_nSelectedFaceIndex %= m_nFaceCount;
        }
        m_landmarkPoints = Engine.getIntance().getLandmarkInfo(m_nSelectedFaceIndex);
        AdjustItemPosition.adjust_FaceFocusPosition(PhotostickerActivity.this);
        if(m_nFaceCount > 1)
            ivFocus.setVisibility(View.VISIBLE);
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
    int m_nSelectedFaceIndex = -1;
    int m_nFaceCount = 0;
    private class AsyncTask_getLandmark extends AsyncTask<Void, Void, Void> {
        Boolean bSuccess = true;
        @Override
        protected void onPreExecute() {
            ShowWaitDialog();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            m_nSelectedFaceIndex = -1;
            if(m_nFaceCount > 0) {
                selectFace();
            }
            HideWaitDialog();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int[] ret = Engine.getIntance().FaceDetect(mBitmap);
            if(ret == null) {m_nFaceCount = 0; return null;}
            m_nFaceCount = ret[0];
            if(m_nFaceCount == 0) return null;
            m_nFaceCount = Engine.getIntance().FaceLandmark(mBitmap);


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
