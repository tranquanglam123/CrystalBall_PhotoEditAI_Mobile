package com.samhung.crystalball.Common;

import java.io.File;
import java.util.LinkedList;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class SingleMediaScanner implements MediaScannerConnectionClient {

    private MediaScannerConnection mMs;

    private static LinkedList<String> mDrawingsToScan = new LinkedList<String>();

    private static SingleMediaScanner sInstance = null;
    public static SingleMediaScanner RefreshScan(Context context, String filename) {
        if(sInstance == null) {
            sInstance = new SingleMediaScanner(context, filename);
        } else {
            sInstance.setParam(context, filename);
        }
        return sInstance;
    }

    public SingleMediaScanner(Context context, String fn) {
        mMs = new MediaScannerConnection(context, this);
        mDrawingsToScan.add(fn);
        mMs.connect();
    }

    private void setParam(Context context, String fn) {
        synchronized(mDrawingsToScan) {
            mDrawingsToScan.add(fn);
            if (!mMs.isConnected()) {
                mMs.connect();
            }
        }
    }

    private void scanNext() {
        synchronized (mDrawingsToScan) {
            if (mDrawingsToScan.isEmpty()) {
                mMs.disconnect();
                return;
            }
            String fn = mDrawingsToScan.removeFirst();
            mMs.scanFile(fn, "image/png");
        }
    }

    @Override
    public void onMediaScannerConnected() {
        scanNext();
    }

    @Override
    public void onScanCompleted(String path, Uri uri) {
        mMs.disconnect();
        synchronized (mDrawingsToScan) {
            scanNext();
        }
    }

}