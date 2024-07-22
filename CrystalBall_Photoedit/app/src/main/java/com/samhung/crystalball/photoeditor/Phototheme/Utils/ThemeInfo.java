package com.samhung.crystalball.photoeditor.Phototheme.Utils;

import android.content.Context;

import com.samhung.crystalball.photoeditor.MainActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ThemeInfo {
    public int ThemeType;
    public int BackType;
    public int ImageCount = 1;
    public int[] centerX;
    public int[] centerY;
    public int[] width;
    public int[] height;
    public int [] radius;
    public int[] angle;
    public boolean[] bGray;

    public int PorterDuff = 0;
    public boolean bThemeTouch = true;

    public ThemeInfo() {

    }
    public static ThemeInfo LoadInfo(Context context, String path) {
        ThemeInfo ret = new ThemeInfo();
        File infoFile = new File(path);
        try {
            byte[] buf = new byte[1024];

//            InputStream fIS = context.getResources().getAssets().open(path);
            InputStream fIS = MainActivity.gZipResourceFile.getInputStream(path);
            int len = fIS.read(buf);
            fIS.close();
            byte[] strBuf = new byte[len];
            System.arraycopy(buf, 0, strBuf, 0, len);
            String strJSON = new String(strBuf);
            JSONObject j = new JSONObject(strJSON);
            ret.ThemeType = j.getInt("theme_type");
            ret.ImageCount = j.getInt("image_count");
            ret.BackType = j.getInt("back_type");

            if(j.has("PorterDuff"))
                ret.PorterDuff = j.getInt("PorterDuff");
            if(j.has("theme_touch"))
                ret.bThemeTouch = j.getInt("theme_touch") == 0? false : true;
            ret.centerX = new int[ret.ImageCount];
            ret.centerY = new int[ret.ImageCount];
            ret.width = new int[ret.ImageCount];
            ret.height = new int[ret.ImageCount];
            ret.radius = new int[ret.ImageCount];
            ret.angle = new int[ret.ImageCount];
            ret.bGray = new boolean[ret.ImageCount];

            for(int i=0; i<ret.ImageCount; i++) {
                ret.centerX[i] = j.getInt("cx"+i);
                ret.centerY[i] = j.getInt("cy"+i);
                ret.width[i] = j.getInt("width"+i);
                ret.height[i] = j.getInt("height"+i);
                if(ret.BackType == 1)
                    ret.radius[i] = j.getInt("radius"+i);
                if(j.has("angle"+i))
                    ret.angle[i] = j.getInt(("angle"+i));
                ret.bGray[i] = false;
                if(j.has("gray"+i))
                    ret.bGray[i] = j.getInt("gray"+i) == 1? true :false;
            }

        } catch (Exception ex) {}
        return ret;
    }
}
