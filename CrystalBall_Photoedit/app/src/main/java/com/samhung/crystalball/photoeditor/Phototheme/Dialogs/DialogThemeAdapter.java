package com.samhung.crystalball.photoeditor.Phototheme.Dialogs;

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
import com.samhung.crystalball.photoeditor.Utilis.MeasureUtil;

import java.util.List;

public class DialogThemeAdapter extends RecyclerView.Adapter<DialogThemeAdapter.ViewHolder>  {
    private List<Bitmap> imageBitmaps;
    private LayoutInflater inflater;
    private DialogThemeAdapter.OnItemClickListener onImageClickListener;
    private Context mContext;
    int m_nSelectedIdx = 0;

    public DialogThemeAdapter(@NonNull Context context, @NonNull List<Bitmap> imageBitmaps, int selected) {
        this.inflater = LayoutInflater.from(context);
        this.imageBitmaps = imageBitmaps;
        mContext = context;
        m_nSelectedIdx = selected;
    }

    @Override
    public DialogThemeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.theme_item_dialog, parent, false);
        ViewGroup.LayoutParams params = view.getLayoutParams();

        params.width = MeasureUtil.convertDpToPixels(70, mContext);
        params.height = MeasureUtil.convertDpToPixels(70, mContext);
        //        params.width = (int)(mContext.getResources().getDisplayMetrics().widthPixels * 0.45);
//        params.height = (int)(mContext.getResources().getDisplayMetrics().widthPixels * 0.45);

        view.setLayoutParams(params);
        return new DialogThemeAdapter.ViewHolder(view);
    }

    ViewHolder selected;
    @Override
    public void onBindViewHolder(DialogThemeAdapter.ViewHolder holder, int position) {
        holder.imageView.setImageBitmap(imageBitmaps.get(position));
        holder.ivSetted.setVisibility(View.INVISIBLE);
        if(position == m_nSelectedIdx) {
            holder.ivSetted.setVisibility(View.VISIBLE);
            selected = holder;
        }
    }

    @Override
    public int getItemCount() {
        return imageBitmaps.size();
    }

    public void setOnImageClickListener(DialogThemeAdapter.OnItemClickListener onImageClickListener) {
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
        ImageView ivSetted;
        public ViewHolder(View itemView) {
            super(itemView);
            ivSetted = (ImageView) itemView.findViewById(R.id.img_setted);
            imageView = (ImageView) itemView.findViewById(R.id.grid_back_img);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selected!=null)
                        selected.ivSetted.setVisibility(View.INVISIBLE);
                    m_nSelectedIdx = getAdapterPosition();
                    onBindViewHolder(ViewHolder.this, m_nSelectedIdx);
                    if (onImageClickListener != null)
                        onImageClickListener.onRecyclerItemClickListener(null, getAdapterPosition());
                    //onImageClickListener.onImageClickListener(decodeSampledBitmapFromAssets(getAdapterPosition()), getAdapterPosition());

                }
            });
        }
    }

    public interface OnItemClickListener {
        void onRecyclerItemClickListener(Bitmap image, int imageIndex);
    }
}