package com.math.photostickersdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PhotoStickerSDK implements MultiTouchListener.OnMultiTouchListener {

    private Context context;
    private RelativeLayout parentView;
    private ImageView imageView;
    private View deleteView;
    private Slate slateView;
//    private BrushDrawingView brushDrawingView;
    public List<View> addedViews;
    private OnPhotoStickerSDKListener onPhotoStickerSDKListener;
    private View addTextRootView;

    private PhotoStickerSDK(PhotoEditorSDKBuilder photoEditorSDKBuilder) {
        this.context = photoEditorSDKBuilder.context;
        this.parentView = photoEditorSDKBuilder.parentView;
        this.imageView = photoEditorSDKBuilder.imageView;
        this.deleteView = photoEditorSDKBuilder.deleteView;
        this.slateView = photoEditorSDKBuilder.slateView;
        addedViews = new ArrayList<>();
    }

    public List<View> getAddedViews() {return addedViews; }
    public void addImage(Bitmap desiredImage, int imageIndex) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View imageRootView = inflater.inflate(R.layout.photo_editor_sdk_image_item_list, null);
        ImageView imageView = (ImageView) imageRootView.findViewById(R.id.photo_editor_sdk_image_iv);
        imageView.setImageBitmap(desiredImage);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        MultiTouchListener multiTouchListener = new MultiTouchListener(deleteView,
                parentView, this.imageView, onPhotoStickerSDKListener);
        multiTouchListener.setOnMultiTouchListener(this);
        imageRootView.setOnTouchListener(multiTouchListener);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        parentView.addView(imageRootView, params);
        addedViews.add(imageRootView);
        onFocusViewListener(imageRootView);
        if (onPhotoStickerSDKListener != null)
            onPhotoStickerSDKListener.onAddViewListener(ViewType.IMAGE, imageIndex);
    }

    public void addEmoticon(Bitmap desiredImage, int imageIndex) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View imageRootView = inflater.inflate(R.layout.photo_editor_sdk_image_item_list, null);
        ImageView imageView = (ImageView) imageRootView.findViewById(R.id.photo_editor_sdk_image_iv);
        imageView.setImageBitmap(desiredImage);
        imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        MultiTouchListener multiTouchListener = new MultiTouchListener(deleteView,
                parentView, this.imageView, onPhotoStickerSDKListener);
        multiTouchListener.setOnMultiTouchListener(this);
        imageRootView.setOnTouchListener(multiTouchListener);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        parentView.addView(imageRootView, params);
        addedViews.add(imageRootView);
        onFocusViewListener(imageRootView);
        if (onPhotoStickerSDKListener != null)
            onPhotoStickerSDKListener.onAddViewListener(ViewType.EMOJI, imageIndex);
    }

    public void addText(String text, int colorCodeTextView, Typeface font) {
        if(text.trim().isEmpty()) return;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        addTextRootView = inflater.inflate(R.layout.photo_editor_sdk_text_item_list, null);
        TextView addTextView = (TextView) addTextRootView.findViewById(R.id.photo_editor_sdk_text_tv);
        addTextView.setGravity(Gravity.CENTER);
        addTextView.setText(text);
        if (colorCodeTextView != -1)
            addTextView.setTextColor(colorCodeTextView);
        else
            addTextView.setTextColor(Color.WHITE);

        addTextView.setTypeface(font);
        MultiTouchListener multiTouchListener = new MultiTouchListener(deleteView,
                parentView, this.imageView, onPhotoStickerSDKListener);
        multiTouchListener.setOnMultiTouchListener(this);
        addTextRootView.setOnTouchListener(multiTouchListener);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        parentView.addView(addTextRootView, params);
        addedViews.add(addTextRootView);
        onFocusViewListener(addTextRootView);
        if (onPhotoStickerSDKListener != null)
            onPhotoStickerSDKListener.onAddViewListener(ViewType.TEXT, addedViews.size());
    }

    public void addEmoji(String emojiName, Typeface emojiFont) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View emojiRootView = inflater.inflate(R.layout.photo_editor_sdk_text_item_list, null);
        TextView emojiTextView = (TextView) emojiRootView.findViewById(R.id.photo_editor_sdk_text_tv);
        emojiTextView.setTypeface(emojiFont);
        emojiTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        emojiTextView.setText(convertEmoji(emojiName));
        MultiTouchListener multiTouchListener = new MultiTouchListener(deleteView,
                parentView, this.imageView, onPhotoStickerSDKListener);
        multiTouchListener.setOnMultiTouchListener(this);
        emojiRootView.setOnTouchListener(multiTouchListener);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        parentView.addView(emojiRootView, params);
        addedViews.add(emojiRootView);
        onFocusViewListener(emojiRootView);

        if (onPhotoStickerSDKListener != null)
            onPhotoStickerSDKListener.onAddViewListener(ViewType.EMOJI, addedViews.size());
    }

    public void setBrushDrawingMode(boolean bEnable) {
        if(slateView != null ) {
            slateView.setDrawingEnbale(bEnable);
        }
//        if (brushDrawingView != null)
//            brushDrawingView.setBrushDrawingMode(brushDrawingMode);
    }

    public void setBrushSize(float size) {
        if(slateView!=null) {
            slateView.setPenSize(size - 0, size + 0);
        }
//        if (brushDrawingView != null)
//            brushDrawingView.setBrushSize(size);
    }

    public void setBrushColor(@ColorInt int color) {
        if(slateView != null) {
            slateView.setPenColor(color);
        }

//        if (brushDrawingView != null)
//            brushDrawingView.setBrushColor(color);
    }

    public void setBrushType(int nType) {
        if(slateView != null ) {
            slateView.setPenType(nType);
        }
//        if( brushDrawingView != null )
//            brushDrawingView.setBrushType(nType);
    }
//    public void setBrushEraserSize(float brushEraserSize) {
//        if (brushDrawingView != null)
//            brushDrawingView.setBrushEraserSize(brushEraserSize);
//    }
//
//    public void setBrushEraserColor(@ColorInt int color) {
//        if (brushDrawingView != null)
//            brushDrawingView.setBrushEraserColor(color);
//    }
//
//    public float getEraserSize() {
//        if (brushDrawingView != null)
//            return brushDrawingView.getEraserSize();
//        return 0;
//    }
//
//    public float getBrushSize() {
//        if (brushDrawingView != null)
//            return brushDrawingView.getBrushSize();
//        return 0;
//    }
//
//    public int getBrushColor() {
//        if (brushDrawingView != null)
//            return brushDrawingView.getBrushColor();
//        return 0;
//    }

//    public void brushEraser() {
//        if (brushDrawingView != null)
//            brushDrawingView.brushEraser();
//    }

    public void viewUndo() {
        if (addedViews.size() > 0) {
            parentView.removeView(addedViews.remove(addedViews.size() - 1));
            if (onPhotoStickerSDKListener != null)
                onPhotoStickerSDKListener.onRemoveViewListener(addedViews.size());
        }
    }

    private void viewUndo(View removedView) {
        if (addedViews.size() > 0) {
            if (addedViews.contains(removedView)) {
                parentView.removeView(removedView);
                addedViews.remove(removedView);
                if (onPhotoStickerSDKListener != null)
                    onPhotoStickerSDKListener.onRemoveViewListener(addedViews.size());
            }
        }
    }

    public void clearBrushAllViews() {
        if(slateView != null) {
            slateView.clear();
        }
//        if (brushDrawingView != null)
//            brushDrawingView.clearAll();
    }

    public void clearAllViews() {
        for (int i = 0; i < addedViews.size(); i++) {
            parentView.removeView(addedViews.get(i));
        }

        if(slateView != null) {
            slateView.clear();
        }
//        if (brushDrawingView != null)
//            brushDrawingView.clearAll();
    }

    public String saveImage(String folderName, String imageName) {
        String selectedOutputPath = "";
        if (isSDCARDMounted()) {
            File mediaStorageDir = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), folderName);
            // Create a storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("PhotoEditorSDK", "Failed to create directory");
                }
            }
            // Create a media file name
            selectedOutputPath = mediaStorageDir.getPath() + File.separator + imageName;
            Log.d("PhotoEditorSDK", "selected camera path " + selectedOutputPath);
            File file = new File(selectedOutputPath);
            try {
                ImageView iv;
                FileOutputStream out = new FileOutputStream(file);
                if (parentView != null) {
                    parentView.setDrawingCacheEnabled(true);
                    parentView.getDrawingCache(false).compress(Bitmap.CompressFormat.JPEG, 80, out);
                    parentView.setDrawingCacheEnabled(false);
                }
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return selectedOutputPath;
    }

    private boolean isSDCARDMounted() {
        String status = Environment.getExternalStorageState();
        return status.equals(Environment.MEDIA_MOUNTED);
    }

    private String convertEmoji(String emoji) {
        String returnedEmoji = "";
        try {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            returnedEmoji = getEmojiByUnicode(convertEmojiToInt);
        } catch (NumberFormatException e) {
            returnedEmoji = "";
        }
        return returnedEmoji;
    }

    private String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }

    public void setOnPhotoEditorSDKListener(OnPhotoStickerSDKListener onPhotoEditorSDKListener) {
        this.onPhotoStickerSDKListener = onPhotoEditorSDKListener;
        //brushDrawingView.setOnPhotoEditorSDKListener(onPhotoEditorSDKListener);
    }

    @Override
    public void onEditTextClickListener(String text, int colorCode, Typeface font) {
//        if (addTextRootView != null) {
//            parentView.removeView(addTextRootView);
//            addedViews.remove(addTextRootView);
//        }
    }

    @Override
    public void onRemoveViewListener(View removedView) {
        viewUndo(removedView);
    }

    @Override
    public void onFocusViewListener(View focousedView) {
        for(int i=0; i<addedViews.size(); i++)
            addedViews.get(i).setBackground(null);
        focousedView.setBackground(context.getResources().getDrawable(R.drawable.item_border));
    }

    public static class PhotoEditorSDKBuilder {

        private Context context;
        private RelativeLayout parentView;
        private ImageView imageView;
        private View deleteView;
        private Slate slateView;
        //private BrushDrawingView brushDrawingView;

        public PhotoEditorSDKBuilder(Context context) {
            this.context = context;
        }

        public PhotoEditorSDKBuilder parentView(RelativeLayout parentView) {
            this.parentView = parentView;
            return this;
        }

        public PhotoEditorSDKBuilder childView(ImageView imageView) {
            this.imageView = imageView;
            return this;
        }

        public PhotoEditorSDKBuilder deleteView(View deleteView) {
            this.deleteView = deleteView;
            return this;
        }

        public PhotoEditorSDKBuilder slateView(Slate slateView) {
            this.slateView = slateView;
            return this;
        }

//        public PhotoEditorSDKBuilder brushDrawingView(BrushDrawingView brushDrawingView) {
//            this.brushDrawingView = brushDrawingView;
//            return this;
//        }

        public PhotoStickerSDK buildPhotoEditorSDK() {
            return new PhotoStickerSDK(this);
        }
    }
}
