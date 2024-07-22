package com.samhung.crystalball.photoeditor.Photosticker.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.samhung.crystalball.photoeditor.R;

import java.util.List;


public class FontPickerAdapter extends RecyclerView.Adapter<FontPickerAdapter.ViewHolder> {

    private Context context;
    private LayoutInflater inflater;
    private List<Typeface> fontsList;
    private OnFontPickerClickListener onFontPickerClickListener;

    public FontPickerAdapter(@NonNull Context context, @NonNull List<Typeface> fontsList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
//        this.colorPickerColors = colorPickerColors;
        this.fontsList = fontsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.font_picker_item_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        buildColorPickerView(holder.fontPickerView, fontsList.get(position));
    }

    @Override
    public int getItemCount() {
        return fontsList.size();
    }

    private void buildColorPickerView(TextView view, Typeface typeface) {
        view.setVisibility(View.VISIBLE);

        view.setTypeface(typeface);
    }

    public void setOnFontPickerClickListener(OnFontPickerClickListener onColorPickerClickListener) {
        this.onFontPickerClickListener = onColorPickerClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView fontPickerView;

        public ViewHolder(View itemView) {
            super(itemView);
            fontPickerView = (TextView)itemView.findViewById(R.id.font_picker_view);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onFontPickerClickListener != null)
                        onFontPickerClickListener.onFontPickerClickListener(fontsList.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface OnFontPickerClickListener {
        void onFontPickerClickListener(Typeface font);
    }
}
