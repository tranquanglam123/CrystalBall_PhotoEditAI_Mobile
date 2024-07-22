package com.samhung.crystalball.photoeditor.Phototheme.Utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

public class GridItem {
    public int _id;
    public Bitmap m_bitmap;
    public boolean m_bSetted = false;

    public GridItem(){}
    public GridItem(int nId, Bitmap image, boolean bSetted )
    {
        _id=nId;
        m_bitmap = image;
        m_bSetted = bSetted;
    }
}
