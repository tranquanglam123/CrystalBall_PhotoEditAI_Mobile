package com.samhung.crystalball.photoeditor.Phototheme.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.samhung.crystalball.photoeditor.R;

import java.util.List;

public class PhotothemeAdapter extends RecyclerView.Adapter<PhotothemeAdapter.ViewHolder>  {
    private List<Bitmap> imageBitmaps;
    private LayoutInflater inflater;
    private PhotothemeAdapter.OnImageClickListener onImageClickListener;
    private Context mContext;

    public PhotothemeAdapter(@NonNull Context context, @NonNull List<Bitmap> imageBitmaps) {
        this.inflater = LayoutInflater.from(context);
        this.imageBitmaps = imageBitmaps;
        mContext = context;
    }

    @Override
    public PhotothemeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.grid_item_theme, parent, false);
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = (int)(mContext.getResources().getDisplayMetrics().widthPixels * 0.45);
        params.height = (int)(mContext.getResources().getDisplayMetrics().widthPixels * 0.45);

        view.setLayoutParams(params);
        return new PhotothemeAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotothemeAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageBitmap(imageBitmaps.get(position));
    }

    @Override
    public int getItemCount() {
        return imageBitmaps.size();
    }

    public void setOnImageClickListener(PhotothemeAdapter.OnImageClickListener onImageClickListener) {
        this.onImageClickListener = onImageClickListener;
    }

    public Bitmap decodeSampledBitmapFromAssets(int resId) {
        String strfileName = String.format("theme/%d/src.png", resId + 1);
        Bitmap ret=null;
        try {
            ret = BitmapFactory.decodeStream(mContext.getAssets().open(strfileName));
        }catch (Exception ex) {}
        return ret;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.grid_back_img);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onImageClickListener != null)
                        onImageClickListener.onRecyclerItemClickListener(null, getAdapterPosition());
                    //onImageClickListener.onImageClickListener(decodeSampledBitmapFromAssets(getAdapterPosition()), getAdapterPosition());

                }
            });
        }
    }

    public interface OnImageClickListener {
        void onRecyclerItemClickListener(Bitmap image, int imageIndex);
    }
}