package com.samhung.crystalball.photoeditor.Photosticker.fragments;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.Photosticker.PhotostickerActivity;
import com.samhung.crystalball.photoeditor.R;

import java.util.ArrayList;


public class ImageFragment extends Fragment implements ImageAdapter.OnImageClickListener {

    private ArrayList<Bitmap> stickerBitmaps;
    private PhotostickerActivity stickerActivity;
    public RecyclerView imageRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stickerActivity = (PhotostickerActivity) getActivity();

//        TypedArray images = getResources().obtainTypedArray(R.array.photo_editor_photos);

        stickerBitmaps = new ArrayList<>();
//        for (int i = 0; i < images.length(); i++) {
        for (int i = 0; i < 30; i++) {
//            stickerBitmaps.add(decodeSampledBitmapFromResource(stickerActivity.getResources(), images.getResourceId(i, -1), 120, 120));
            stickerBitmaps.add(decodeSampledBitmapFromOBB(i));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main_photo_edit_image, container, false);

        imageRecyclerView = (RecyclerView) rootView.findViewById(R.id.fragment_main_photo_edit_image_rv);
        imageRecyclerView.setLayoutManager(new GridLayoutManager(stickerActivity, 3));
        ImageAdapter adapter = new ImageAdapter(stickerActivity, stickerBitmaps);
        adapter.setOnImageClickListener(this);
        imageRecyclerView.setAdapter(adapter);

        return rootView;
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public Bitmap decodeSampledBitmapFromAssets(int resId) {
        String strfileName = String.format("sticker/%d/icon.png", resId + 1);
        Bitmap ret=null;
        try {
            ret = BitmapFactory.decodeStream(getContext().getAssets().open(strfileName));
        }catch (Exception ex) {}
        return ret;
    }

    public Bitmap decodeSampledBitmapFromOBB(int resId) {
        String strfileName = String.format("assets/sticker/%d/icon.png", resId + 1);
        Bitmap ret=null;
        try {
            ret = BitmapFactory.decodeStream(MainActivity.gZipResourceFile.getInputStream(strfileName));
        }catch (Exception ex) {}
        return ret;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    @Override
    public void onImageClickListener(Bitmap image, int nImageIndex) {
        stickerActivity.addImage(image, nImageIndex);
    }
}
