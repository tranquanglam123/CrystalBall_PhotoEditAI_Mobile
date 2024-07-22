package com.samhung.crystalball.photoeditor.Phototheme.Utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.provider.CalendarContract;
import android.util.Log;

import com.samhung.crystalball.photoeditor.MainActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CalendarUtil {
    public static int[][] holidays = new int[][] {
            {1}, {8, 16}, {8}, {15, 25}, {1}, {6}, {27}, {15}, {9}, {10}, {16},{27}
    };

    public static String[] week_names = new String[] {
            "일요일", "월요일", "화요일", "수요일", "목요일", "금요일", "토요일"
    };

    static boolean isHoliday(int Month /*0~11*/, int Day/*0~31*/) {
        boolean holiday = false;
        for(int i=0; i<holidays[Month].length; i++) {
            if(holidays[Month][i] == (Day + 1))
                holiday = true;
        }
        return holiday;
    }

    public static Bitmap makeCalendar(Context context, CalendarInfo calendarInfo, Bitmap back, int Year, int Month, int Day) {
        switch (calendarInfo.calendar_type) {
            case 0:
            case 1:
                return makeMonthCalendar(context, calendarInfo, back, Year, Month, Day);
            case 3:
                return makeBungiCalendar(context, calendarInfo, back, Year, Month, Day);
            case 4:
                return makeDailyCalendar(context, calendarInfo, back, Year, Month, Day);
            case 5:
                return makeDailyCalendarWithBitmap(context, calendarInfo, back, Year, Month, Day);
        }
        return null;
    }

    public static Paint getPaint(Context context, String fontName, int fontSize, int color) {
        Paint paint = new Paint();
        Typeface font = Typeface.createFromAsset(context.getAssets(), fontName);
        paint.setTypeface(font);
        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(0xff000000|color);
        return paint;
    }

    public static Bitmap makeMonthCalendar(Context context, CalendarInfo calendarInfo, Bitmap back, int Year, int Month, int Day) {
        Bitmap bmRet = back.copy(back.getConfig(), true);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Year, Month, Day);
        Canvas retCanvas = new Canvas(bmRet);
        int daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Paint yearPaint = getPaint(context, calendarInfo.year_font_name, calendarInfo.year_text_size, calendarInfo.year_font_color);
        if(calendarInfo.year_stroke_width > 0) {
            yearPaint.setStyle(Paint.Style.FILL);
            Paint yearStroke = new Paint(yearPaint);
            yearStroke.setStyle(Paint.Style.STROKE);
            yearStroke.setColor(Color.WHITE);
            yearStroke.setStrokeWidth(calendarInfo.year_stroke_width);
            retCanvas.drawText(""+Year, calendarInfo.year_pos.x, calendarInfo.year_pos.y, yearStroke);
        }
        Paint monthPaint = getPaint(context, calendarInfo.month_font_name, calendarInfo.month_text_size, calendarInfo.month_font_color);
        if(calendarInfo.month_stroke_width > 0) {
            monthPaint.setStyle(Paint.Style.FILL);
            Paint monthStroke = new Paint(monthPaint);
            monthStroke.setStyle(Paint.Style.STROKE);
            monthStroke.setColor(Color.WHITE);
            monthStroke.setStrokeWidth(calendarInfo.month_stroke_width);

            retCanvas.drawText(""+(Month+1), calendarInfo.month_pos1.x, calendarInfo.month_pos1.y, monthStroke);
        }

        retCanvas.drawText(""+Year, calendarInfo.year_pos.x, calendarInfo.year_pos.y, yearPaint);
        retCanvas.drawText(""+(Month+1), calendarInfo.month_pos1.x, calendarInfo.month_pos1.y, monthPaint);
        int sx =  calendarInfo.day_start_pos1.x;
        int sy = calendarInfo.day_start_pos1.y;
        int hGap = calendarInfo.HGap;
        int vGap = calendarInfo.VGap;

        Paint txtPaint = getPaint(context, calendarInfo.day_font_name, calendarInfo.day_text_size,calendarInfo.day_font_color);
        Paint holidayPaint = getPaint(context, calendarInfo.day_font_name, calendarInfo.day_text_size,Color.RED);

        Paint today_back = new Paint();
        today_back.setStyle(Paint.Style.STROKE);
        if(calendarInfo.today_mark_type == 1)
            today_back.setStyle(Paint.Style.FILL);
        today_back.setColor(0xff000000|calendarInfo.today_color);
        today_back.setStrokeWidth(6);

        int line_idx = 0;
        int sunday_count = 0;
        for(int i=0; i<daysOfMonth; i++) {
            calendar.set(Calendar.DATE, i+1);
            int week_num = calendar.get(Calendar.DAY_OF_WEEK);

            if(i==Day-1 && calendarInfo.today_mark > 0) {
                retCanvas.drawCircle(sx + ((sunday_count % 2)*7+week_num-1)*hGap, sy + line_idx*vGap-calendarInfo.day_text_size/3, calendarInfo.today_radius, today_back);
            }
            if(isHoliday(Month, i) || week_num == Calendar.SUNDAY)
                retCanvas.drawText("" +(i+1), sx + (week_num-1 + (sunday_count %2) * 7)*hGap, sy + line_idx*vGap, holidayPaint);
            else
                retCanvas.drawText("" +(i+1), sx + (week_num-1 + (sunday_count %2) * 7)*hGap, sy + line_idx*vGap, txtPaint);
            if(calendarInfo.calendar_type == 0) {
                if(week_num == Calendar.SATURDAY) {
                    line_idx++;
                    if(line_idx > 4) line_idx = 0;
                }
            } else if(calendarInfo.calendar_type == 1) {
                if(week_num == Calendar.SATURDAY)
                {
                    sunday_count++;
                    if(sunday_count % 2 == 0)
                        line_idx++;
                }
            }
        }
        ///Log.e("------", "--"+ aaa);
        return bmRet;
    }

    private static int[] getBungi(int Month) {
        int[] bungi = new int[3];
        if(Month < 3) {
            bungi[0] = 0; bungi[1] = 1; bungi[2] = 2;
        } else if(Month <6) {
            bungi[0] = 3; bungi[1] = 4; bungi[2] = 5;
        } else if(Month <9) {
            bungi[0] = 6; bungi[1] = 7; bungi[2] = 8;
        } else {
            bungi[0] = 9;
            bungi[1] = 10;
            bungi[2] = 11;
        }
        return bungi;
    }

    public static Bitmap makeBungiCalendar(Context context, CalendarInfo calendarInfo, Bitmap back, int Year, int Month, int Day) {
        Bitmap bmRet = back.copy(back.getConfig(), true);
        Calendar calendar = Calendar.getInstance();

        Canvas retCanvas = new Canvas(bmRet);
        int daysOfMonth = 0;

        Paint yearPaint = getPaint(context, calendarInfo.year_font_name, calendarInfo.year_text_size, calendarInfo.year_font_color);
        Paint monthPaint = getPaint(context, calendarInfo.month_font_name, calendarInfo.month_text_size, calendarInfo.month_font_color);
        if(calendarInfo.year_font_bold > 0)
            yearPaint.setFakeBoldText(true);
        retCanvas.drawText(""+Year, calendarInfo.year_pos.x, calendarInfo.year_pos.y, yearPaint);
        int[] mm = getBungi(Month);

        retCanvas.drawText(""+(mm[0]+1), calendarInfo.month_pos1.x, calendarInfo.month_pos1.y, monthPaint);
        retCanvas.drawText(""+(mm[1]+1), calendarInfo.month_pos2.x, calendarInfo.month_pos2.y, monthPaint);
        retCanvas.drawText(""+(mm[2]+1), calendarInfo.month_pos3.x, calendarInfo.month_pos3.y, monthPaint);

        Paint txtPaint = getPaint(context, calendarInfo.day_font_name, calendarInfo.day_text_size,calendarInfo.day_font_color);
        Paint holidayPaint = getPaint(context, calendarInfo.day_font_name, calendarInfo.day_text_size,Color.RED);

        Paint today_back = new Paint();
        today_back.setStyle(Paint.Style.STROKE);
        if(calendarInfo.today_mark_type == 1)
            today_back.setStyle(Paint.Style.FILL);
        today_back.setColor(0xff000000|calendarInfo.today_color);
        today_back.setStrokeWidth(6);

        int[] sxs = new int[3];
        int[] sys = new int[3];
        sxs[0] = calendarInfo.day_start_pos1.x;
        sxs[1] = calendarInfo.day_start_pos2.x;
        sxs[2] = calendarInfo.day_start_pos3.x;
        sys[0] = calendarInfo.day_start_pos1.y;
        sys[1] = calendarInfo.day_start_pos2.y;
        sys[2] = calendarInfo.day_start_pos3.y;

        int hGap = calendarInfo.HGap;
        int vGap = calendarInfo.VGap;

        for(int k=0; k<3; k++) {
            calendar.set(Year, mm[k], Day);
            daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            int sx =  sxs[k];
            int sy = sys[k];
            int line_idx = 0;

            for (int i = 0; i < daysOfMonth; i++) {
                calendar.set(Calendar.DATE, i + 1);
                int week_num = calendar.get(Calendar.DAY_OF_WEEK);

                if (i == Day - 1 && mm[k]==Month && calendarInfo.today_mark > 0) {
                    retCanvas.drawCircle(sx + (week_num - 1) * hGap, sy + line_idx * vGap - calendarInfo.day_text_size / 2, calendarInfo.today_radius, today_back);
                }
                if (isHoliday(mm[k], i) || week_num == Calendar.SUNDAY)
                    retCanvas.drawText("" + (i + 1), sx + (week_num - 1 ) * hGap, sy + line_idx * vGap, holidayPaint);
                else
                    retCanvas.drawText("" + (i + 1), sx + (week_num - 1 ) * hGap, sy + line_idx * vGap, txtPaint);

                if (week_num == Calendar.SATURDAY) {
                    line_idx++;
                    if(line_idx > 4) line_idx = 0;
                }
            }
        }
        return bmRet;
    }

    public static Bitmap makeDailyCalendar(Context context, CalendarInfo calendarInfo, Bitmap back, int Year, int Month, int Day) {
        Bitmap bmRet = back.copy(back.getConfig(), true);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Year, Month, Day);
        Canvas retCanvas = new Canvas(bmRet);
        int daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Paint yearPaint = getPaint(context, calendarInfo.year_font_name, calendarInfo.year_text_size, calendarInfo.year_font_color);
        Paint monthPaint = getPaint(context, calendarInfo.month_font_name, calendarInfo.month_text_size, calendarInfo.month_font_color);

        String strYear = "주체"+(Year-1911)+"("+Year+")"+"년";
        retCanvas.drawText(""+strYear, calendarInfo.year_pos.x, calendarInfo.year_pos.y, yearPaint);
        retCanvas.drawText(""+(Month+1)+"월", calendarInfo.month_pos1.x, calendarInfo.month_pos1.y, monthPaint);
        int sx =  calendarInfo.day_start_pos1.x;
        int sy = calendarInfo.day_start_pos1.y;
        int hGap = calendarInfo.HGap;
        int vGap = calendarInfo.VGap;

        Paint txtPaint = getPaint(context, calendarInfo.day_font_name, calendarInfo.day_text_size,calendarInfo.day_font_color);
        Paint holidayPaint = getPaint(context, calendarInfo.day_font_name, calendarInfo.day_text_size,Color.RED);

        Paint weekPaint = getPaint(context, calendarInfo.day_font_name, calendarInfo.week_font_size, calendarInfo.week_font_color);
        Paint weekSundayPaint = getPaint(context, calendarInfo.day_font_name, calendarInfo.week_font_size, Color.RED);
        int week_num = calendar.get(Calendar.DAY_OF_WEEK);
        if(isHoliday(Month, Day-1) || week_num == Calendar.SUNDAY) {
            retCanvas.drawText("" + Day, calendarInfo.day_start_pos1.x, calendarInfo.day_start_pos1.y, holidayPaint);

        } else {
            retCanvas.drawText("" + Day, calendarInfo.day_start_pos1.x, calendarInfo.day_start_pos1.y, txtPaint);
        }
        if(week_num == Calendar.SUNDAY) {
            retCanvas.drawText(week_names[week_num-1], calendarInfo.week_start_pos.x, calendarInfo.week_start_pos.y, weekSundayPaint);
        } else  {
            retCanvas.drawText(week_names[week_num-1], calendarInfo.week_start_pos.x, calendarInfo.week_start_pos.y, weekPaint);
        }

        ///Log.e("------", "--"+ aaa);
        return bmRet;
    }

    public static Bitmap[] getChBitmaps(Context context,String strPath, int ch_wid) {
        Bitmap[] rets = new Bitmap[11];
        try {
//            Bitmap bm = BitmapFactory.decodeStream(context.getAssets().open(strPath));
            Bitmap bm = BitmapFactory.decodeStream(MainActivity.gZipResourceFile.getInputStream(strPath));

            for(int i=0; i<11; i++) {
                Bitmap bmCh = Bitmap.createBitmap(ch_wid, bm.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas bmCanvas = new Canvas(bmCh);
                Rect rt = new Rect();
                rt.left = i*ch_wid;
                rt.right = rt.left +ch_wid;
                rt.top = 0;
                rt.bottom = bm.getHeight();
                Rect rtt = new Rect();
                rtt.left = 0; rtt.right = ch_wid; rtt.top = 0; rtt.bottom = bm.getHeight();
                bmCanvas.drawBitmap(bm,rt, rtt, new Paint());
                rets[i] = bmCh;
            }
        }catch (Exception ex) {}
        return rets;
    }

    public static void printBitmapCharacter(Canvas canvas, Bitmap[] bmCh, int printNum, int cx, int cy, boolean bDot) {
        int num = printNum % 10000;
        int[] chList = new int[4];
        int nChCnt = 0;
        while(num >= 10) {
            chList[nChCnt] = num % 10;
            num = num / 10;
            nChCnt ++;
        }
        chList[nChCnt] = num;

        int sx = cx - bmCh[0].getWidth() * (nChCnt+1) / 2;
        int sy = cy - bmCh[0].getHeight() / 2;

        for(int i=0; i<nChCnt+1; i++) {
            canvas.drawBitmap(bmCh[chList[nChCnt-i]], sx+i*bmCh[0].getWidth(), sy, new Paint());
        }
        if(bDot)
            canvas.drawBitmap(bmCh[10], sx + (nChCnt+1) * bmCh[10].getWidth(), sy, new Paint());
    }

    public static Bitmap makeDailyCalendarWithBitmap(Context context, CalendarInfo calendarInfo, Bitmap back, int Year, int Month, int Day) {
        Bitmap bmRet = back.copy(back.getConfig(), true);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Year, Month, Day);
        Canvas retCanvas = new Canvas(bmRet);
        int daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Bitmap[] dayCh = getChBitmaps(context, "assets/"+calendarInfo.day_font_name, calendarInfo.day_text_size);
        Bitmap[] yearCh = getChBitmaps(context, "assets/"+calendarInfo.year_font_name, calendarInfo.year_text_size);
        Bitmap[] monthCh = getChBitmaps(context, "assets/"+calendarInfo.month_font_name, calendarInfo.month_text_size);

        printBitmapCharacter(retCanvas, yearCh,Year, calendarInfo.year_pos.x, calendarInfo.year_pos.y, true);
        printBitmapCharacter(retCanvas, monthCh,Month+1, calendarInfo.month_pos1.x, calendarInfo.month_pos1.y, false);
        printBitmapCharacter(retCanvas, dayCh,Day, calendarInfo.day_start_pos1.x, calendarInfo.day_start_pos1.y, false);

        ///Log.e("------", "--"+ aaa);
        return bmRet;
    }
}
