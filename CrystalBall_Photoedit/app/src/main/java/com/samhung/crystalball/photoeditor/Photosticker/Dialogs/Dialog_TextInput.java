package com.samhung.crystalball.photoeditor.Photosticker.Dialogs;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.photoeditor.Photosticker.PhotostickerActivity;
import com.samhung.crystalball.photoeditor.Photosticker.fragments.ColorPickerAdapter;
import com.samhung.crystalball.photoeditor.Photosticker.fragments.FontPickerAdapter;
import com.samhung.crystalball.photoeditor.R;

public class Dialog_TextInput extends FullScreenDialog {
    OnClickListener m_onClickListener = null;
    Typeface textFont = null;
    int colorCodeTextView = 0;
    PhotostickerActivity mStickerActivity = null;
    String mText = "";
    public Dialog_TextInput(PhotostickerActivity activity, OnClickListener onClickListener, String text,  Typeface font, int colorCode ){
        super(activity);
        mStickerActivity = activity;

        m_onClickListener = onClickListener;
        textFont = font;
        mText = text;
        colorCodeTextView = colorCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_text_popup_window);
        InitControls();

    }

    void InitControls() {

        RelativeLayout rl_tvarea = (RelativeLayout)findViewById(R.id.rl_textarea);

        RecyclerView addTextColorPickerRecyclerView = (RecyclerView) findViewById(R.id.add_text_color_picker_recycler_view);
        final EditText addTextEditText = (EditText) findViewById(R.id.add_text_edit_text);
        addTextEditText.setTypeface(textFont);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mStickerActivity, LinearLayoutManager.HORIZONTAL, false);
        addTextColorPickerRecyclerView.setLayoutManager(layoutManager);
        addTextColorPickerRecyclerView.setHasFixedSize(true);
        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(mStickerActivity, mStickerActivity.colorPickerColors);
        colorPickerAdapter.setOnColorPickerClickListener(new ColorPickerAdapter.OnColorPickerClickListener() {
            @Override
            public void onColorPickerClickListener(int colorCode) {
                addTextEditText.setTextColor(colorCode);
                colorCodeTextView = colorCode;
                mStickerActivity.colorCodeTextView = colorCode;
            }
        });
        addTextColorPickerRecyclerView.setAdapter(colorPickerAdapter);

        RecyclerView addTextFontPickerRecyclerView = (RecyclerView)findViewById(R.id.add_text_font_picker_recycler_view);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(mStickerActivity, LinearLayoutManager.HORIZONTAL, false);
        addTextFontPickerRecyclerView.setLayoutManager(layoutManager1);
        addTextFontPickerRecyclerView.setHasFixedSize(true);
        FontPickerAdapter fontPickerAdapter = new FontPickerAdapter(mStickerActivity, mStickerActivity.fontsList);
        fontPickerAdapter.setOnFontPickerClickListener(new FontPickerAdapter.OnFontPickerClickListener() {
            @Override
            public void onFontPickerClickListener(Typeface font) {
                addTextEditText.setTypeface(font);
                textFont = font;
                mStickerActivity.textFont = font;
            }
        });
        addTextFontPickerRecyclerView.setAdapter(fontPickerAdapter);

        if (stringIsNotEmpty(mText)) {
            addTextEditText.setText(mText);
            addTextEditText.setTextColor(colorCodeTextView == -1 ? getContext().getResources().getColor(R.color.white) : colorCodeTextView);
        } else  {
            addTextEditText.setText("");
            addTextEditText.setTextColor(colorCodeTextView == -1 ? getContext().getResources().getColor(R.color.white) : colorCodeTextView);
        }

        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    View.OnClickListener viewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.rl_textarea) {

            }
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

}
