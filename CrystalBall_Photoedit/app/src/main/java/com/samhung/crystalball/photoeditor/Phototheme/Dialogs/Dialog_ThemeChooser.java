package com.samhung.crystalball.photoeditor.Phototheme.Dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.samhung.crystalball.Common.BaseActivity;
import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.Phototheme.Activities.PhotothemeActivity;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.GridItem;
import com.samhung.crystalball.photoeditor.Phototheme.Utils.PhotothemeAdapter;
import com.samhung.crystalball.photoeditor.R;

import java.util.ArrayList;

public class Dialog_ThemeChooser extends FullScreenDialog {

    private GridView m_gridView = null;
    private ImageView ivReturn = null;
    private ImageView ivAccept = null;
    public int m_nSelectedItem = 0;

    private ArrayList<GridItem> m_gridItemArray = new ArrayList<GridItem>();
    public ArrayList<Bitmap> m_thumbThemeArray = new ArrayList<Bitmap>();

    DialogInterface.OnClickListener m_onClickListener = null;

    Context m_context = null;
    BaseActivity m_parent = null;
    int mCategoryID;
    public Dialog_ThemeChooser(Context context, BaseActivity parent, int categoryID, DialogInterface.OnClickListener onClickListener)
    {
        super(context);
        m_context= context;
        m_parent = parent;
        m_onClickListener = onClickListener;
        mCategoryID = categoryID;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_theme_chooser);
        m_gridView = (GridView)findViewById(R.id.gridview_frame);
        ivReturn = (ImageView)findViewById(R.id.button_close);
        ivReturn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        ivAccept = (ImageView)findViewById(R.id.button_accept);
        RefreshGridItemArray();

        m_gridView.addOnLayoutChangeListener(new View.OnLayoutChangeListener()
        {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(right==oldRight && left == oldLeft && top == oldTop && bottom == oldBottom) return;
                //                float scalefactor = m_parent.getResources().getDisplayMetrics().density * 160;
//                int number = m_parent.getWindowManager().getDefaultDisplay().getWidth();
//                int columns = (int) ((float) number / (float) scalefactor);
//                int columns = 2;
//                int number = right-left;
//                float width = number / columns- 20;// / m_parent.getResources().getDisplayMetrics().density ;
//                m_gridView.setNumColumns(columns);
//                m_gridView.setColumnWidth((int)width);
//                ThemeItemAdapter adapter = new ThemeItemAdapter(Dialog_ThemeChooser.this.getContext(), R.layout.grid_item_theme, m_gridItemArray, (int)width );
//                m_gridView.setAdapter(adapter);

            }
        });
        m_gridView.setChoiceMode(GridView.CHOICE_MODE_SINGLE);
        m_gridView.setOnItemClickListener(m_listItemClickListener);
        m_gridView.setEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
        int columns = 2;

        int number = getWindow().getDecorView().getWidth();
        float width = number / columns- 20;// / m_parent.getResources().getDisplayMetrics().density ;
        m_gridView.setNumColumns(columns);
        m_gridView.setColumnWidth((int)width);
        ThemeItemAdapter adapter = new ThemeItemAdapter(Dialog_ThemeChooser.this.getContext(), R.layout.grid_item_theme, m_gridItemArray, (int)width );
        m_gridView.setAdapter(adapter);

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        dismiss();
    }

    AdapterView.OnItemClickListener m_listItemClickListener =  new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            m_gridItemArray.get(m_nSelectedItem).m_bSetted = false;
            m_gridItemArray.get(position).m_bSetted = true;
            m_nSelectedItem = position;

            m_onClickListener.onClick(Dialog_ThemeChooser.this, DialogInterface.BUTTON_POSITIVE);
        }
    };

    public void RefreshGridItemArray()
    {
        m_gridItemArray.clear();
        String dir = String.format("assets/theme/%d", mCategoryID+1);
        try {
//            String[] paths = m_parent.getResources().getAssets().list(dir);

//            for(int i=0; i<paths.length; i++)
            for(int i=0; i<MainActivity.gZipResourceFile.getList(dir).length; i++)
            {
//                Bitmap bm = BitmapFactory.decodeStream(m_parent.getResources().getAssets().open(dir+"/"+(i+1)+"/thumb.png"));
                Bitmap bm = BitmapFactory.decodeStream(MainActivity.gZipResourceFile.getInputStream(dir+"/"+(i+1)+"/thumb.png"));
                m_gridItemArray.add(new GridItem(i+1, bm,false));
                m_thumbThemeArray.add(bm);
            }
        }
        catch (Exception ex){ex.printStackTrace();}
    }
}

class ThemeItemAdapter extends BaseAdapter {
    ArrayList<GridItem> m_gridItemArray;
    LayoutInflater m_inflate;
    int m_layout;
    int m_itemwidth = 50;
    Context mContext;
    public ThemeItemAdapter(Context context, int alayout, ArrayList<GridItem> skinItemArray, int item_width)
    {
        m_inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        m_gridItemArray = skinItemArray;
        m_layout = alayout;
        m_itemwidth = item_width;
        mContext = context;
    }

    @Override
    public int getCount() {
        return m_gridItemArray.size();
    }

    @Override
    public Object getItem(int position) {
        return m_gridItemArray.get(position);
    }

    @Override
    public long getItemId(int position) {
        return m_gridItemArray.get(position)._id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
        {
            convertView = m_inflate.inflate(m_layout, parent, false);
            ViewGroup.LayoutParams params = convertView.getLayoutParams();
            params.width = (int)(mContext.getResources().getDisplayMetrics().widthPixels * 0.45);
            params.height = (int)(mContext.getResources().getDisplayMetrics().widthPixels * 0.45);

            convertView.setLayoutParams(params);
       //     convertView.setLayoutParams(param);
        }

        ImageView imgItem = (ImageView)convertView.findViewById(R.id.grid_back_img);
        imgItem.setImageBitmap(m_gridItemArray.get(position).m_bitmap);
        return convertView;
    }
}
