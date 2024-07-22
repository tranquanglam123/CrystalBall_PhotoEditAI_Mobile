package com.math.photostickersdk;

import android.graphics.Typeface;
import android.widget.TextView;

public interface OnPhotoStickerSDKListener {

    void onEditTextChangeListener(TextView view, String text, int colorCode, Typeface font);

    void onAddViewListener(ViewType viewType, int numberOfAddedViews);

    void onRemoveViewListener(int numberOfAddedViews);

    void onStartViewChangeListener(ViewType viewType);

    void onStopViewChangeListener(ViewType viewType);
}
