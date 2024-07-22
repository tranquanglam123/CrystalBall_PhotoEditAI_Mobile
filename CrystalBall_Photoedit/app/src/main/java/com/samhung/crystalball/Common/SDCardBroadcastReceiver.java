package com.samhung.crystalball.Common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SDCardBroadcastReceiver extends BroadcastReceiver {
    public SDCardBroadcastReceiver() {
        super();
        System.err.println("constructor");
    }

    public void onReceive(Context context, Intent intent) {
        Log.d("SDCardBroadCastReceiver", "receive " + intent.getAction());
        System.err.println("jonathan receive " + intent.getAction());

    }
}