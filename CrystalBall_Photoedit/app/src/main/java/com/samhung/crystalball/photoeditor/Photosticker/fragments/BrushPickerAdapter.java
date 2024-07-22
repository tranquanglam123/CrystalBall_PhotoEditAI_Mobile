package com.samhung.crystalball.photoeditor.Photosticker.fragments;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.samhung.crystalball.photoeditor.R;

import java.util.ArrayList;
import java.util.List;


public class BrushPickerAdapter extends RecyclerView.Adapter<BrushPickerAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private OnBrushPickerClickListener onFontPickerClickListener;
    ArrayList<Bitmap> brushBitmps = new ArrayList<>();
    int m_nSelectedIdx = 0;

    public BrushPickerAdapter(@NonNull Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        TypedArray images = context.getResources().obtainTypedArray(R.array.photo_editor_brushes);
        for(int i=0; i<images.length(); i++) {
            brushBitmps.add(BitmapFactory.decodeResource(context.getResources(), images.getResourceId(i, -1)));
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.brush_picker_item_list, parent, false);
        return new ViewHolder(view);
    }

    ViewHolder selected;
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        buildBrushPickerView(holder.imageView, brushBitmps.get(position));
        holder.frame.setBackgroundColor(Color.TRANSPARENT);
        if(position == m_nSelectedIdx) {
            selected = holder;
            holder.frame.setBackgroundColor(Color.argb(255, 238, 21, 129));
        } else {

        }
    }

    @Override
    public int getItemCount() {
        return brushBitmps.size();
    }

    private void buildBrushPickerView(ImageView view, Bitmap bm) {
        view.setVisibility(View.VISIBLE);
        view.setImageBitmap(bm);

    }

    public void setOnBrushPickerClickListener(OnBrushPickerClickListener onBrushPickerClickListener) {
        this.onFontPickerClickListener = onBrushPickerClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        FrameLayout frame;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView= (ImageView)itemView.findViewById(R.id.imageView_brush);
            frame = (FrameLayout)itemView.findViewById(R.id.frame_brush);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(selected!=null)
                        selected.frame.setBackgroundColor(Color.TRANSPARENT);
                    m_nSelectedIdx = getAdapterPosition();
                    onBindViewHolder(ViewHolder.this, m_nSelectedIdx);
                    if (onFontPickerClickListener != null)
                        onFontPickerClickListener.onBrushPickerClickListener(getAdapterPosition());
                }
            });
        }
    }

    public interface OnBrushPickerClickListener {
        void onBrushPickerClickListener(int position);
    }
}
