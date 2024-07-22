package com.samhung.crystalball.photoeditor.Utilis;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPref {
    public static final String PREFNAME = "VisionMixer";

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences pref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        return sp.getBoolean(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences pref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public static int getInt(Context context, String key, int defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        return sp.getInt(key, defaultValue);
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences pref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }
    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        return sp.getString(key, defaultValue);
    }

    public static void clear(Context context){
        SharedPreferences pref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public static void putFloat(Context context, String key, float value) {
        SharedPreferences pref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public static float getInt(Context context, String key, float defaultValue) {
        SharedPreferences sp = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        return sp.getFloat(key, defaultValue);
    }

    public static void putLong(Context context, String key, long value){
        SharedPreferences pref = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    public static long getLong(Context context, String key, long defaultValue){
        SharedPreferences sp = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        return sp.getLong(key, defaultValue);
    }

    public static void remove(Context context, String key){
        SharedPreferences sp = context.getSharedPreferences(PREFNAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(key);
        editor.commit();
    }
}
