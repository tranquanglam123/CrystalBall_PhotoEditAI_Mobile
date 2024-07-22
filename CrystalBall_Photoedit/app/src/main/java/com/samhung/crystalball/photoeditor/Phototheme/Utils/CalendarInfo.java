package com.samhung.crystalball.photoeditor.Phototheme.Utils;

import android.content.Context;
import android.graphics.Point;

import com.samhung.crystalball.photoeditor.MainActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;

public class CalendarInfo {
    public int calendar_type;  //0,1-month, 2-bungi, 3-day
    public Point year_pos = new Point();
    public Point month_pos1= new Point();
    public Point month_pos2= new Point();
    public Point month_pos3= new Point();
    public Point day_start_pos1= new Point();
    public Point day_start_pos2= new Point();
    public Point day_start_pos3= new Point();
    public Point week_start_pos= new Point();
    public int VGap;
    public int HGap;

    public int year_text_size;
    public int month_text_size;
    public int day_text_size;

    public String year_font_name;
    public String month_font_name;
    public String day_font_name;

    public int year_font_color;
    public int month_font_color;
    public int day_font_color;

    public int week_font_color;
    public int week_font_size;

    public int today_mark = 0;
    public int today_radius = 0;
    public int today_color = 0;
    public int today_mark_type = 0;

    public int year_stroke_width = 0;
    public int month_stroke_width = 0;

    public int year_font_bold = 0;
    public CalendarInfo() {

    }
    public static CalendarInfo LoadInfo(Context context, String path) {
        CalendarInfo ret = new CalendarInfo();
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
            ret.calendar_type = j.getInt("calendar_type");
            ret.year_text_size = j.getInt("year_font_size");
            ret.month_text_size = j.getInt("month_font_size");
            ret.day_text_size = j.getInt("day_font_size");

            ret.year_font_name = j.getString("year_font_name");
            ret.month_font_name = j.getString("month_font_name");
            ret.day_font_name = j.getString("day_font_name");



            ret.month_pos1.x = j.getInt("month_pos_x1");
            ret.month_pos1.y = j.getInt("month_pos_y1");
            ret.month_pos2.x = j.getInt("month_pos_x2");
            ret.month_pos2.y = j.getInt("month_pos_y2");
            ret.month_pos3.x = j.getInt("month_pos_x3");
            ret.month_pos3.y = j.getInt("month_pos_y3");

            ret.day_start_pos1.x = j.getInt("day_start_x1");
            ret.day_start_pos1.y = j.getInt("day_start_y1");
            ret.day_start_pos2.x = j.getInt("day_start_x2");
            ret.day_start_pos2.y = j.getInt("day_start_y2");
            ret.day_start_pos3.x = j.getInt("day_start_x3");
            ret.day_start_pos3.y = j.getInt("day_start_y3");
            ret.VGap = j.getInt("day_v_gap");
            ret.HGap = j.getInt("day_h_gap");
            ret.year_pos.y = j.getInt("year_pos_y");
            ret.year_pos.x = j.getInt("year_pos_x");

            ret.year_font_color = j.getInt("year_font_color");
            ret.month_font_color = j.getInt("month_font_color");
            ret.day_font_color = j.getInt("day_font_color");
            ret.today_mark = j.getInt("today_mark");
            if(ret.today_mark > 0) {
                ret.today_radius = j.getInt("today_radius");
                ret.today_mark_type = j.getInt("today_mark_type");
                ret.today_color = j.getInt("today_mark_color");
            }
            if(ret.calendar_type == 4) {
                ret.week_start_pos.x = j.getInt("week_pos_x");
                ret.week_start_pos.y = j.getInt("week_pos_y");
                ret.week_font_size = j.getInt("week_font_size");
                ret.week_font_color = j.getInt("week_font_color");
            }

            if(j.has("year_stroke_width"))
                ret.year_stroke_width = j.getInt("year_stroke_width");
            if(j.has("month_stroke_width"))
                ret.month_stroke_width = j.getInt("month_stroke_width");
            if(j.has("year_font_bold"))
                ret.year_font_bold = j.getInt("year_font_bold");
        } catch (Exception ex) {
            String str = ex.toString();
        }
        return ret;
    }
}
