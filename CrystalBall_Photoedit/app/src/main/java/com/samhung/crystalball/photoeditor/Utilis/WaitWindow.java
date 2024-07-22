package com.samhung.crystalball.photoeditor.Utilis;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.samhung.crystalball.Common.FullScreenDialog;
import com.samhung.crystalball.photoeditor.Photosticker.Views.SlidingUpPanelLayout;
import com.samhung.crystalball.photoeditor.R;

public class WaitWindow  extends Dialog{
    private static WaitWindow waitDlg = null;
    public static void ShowWindow(Context context) {
        if(waitDlg == null) {
            waitDlg = new WaitWindow(context);
        }
        waitDlg.show();
    }

    public static void HideWindow()
    {
        if(waitDlg !=null && waitDlg.isShowing()) {
            waitDlg.dismiss();
            waitDlg = null;
        }

    }
    public WaitWindow(Context context) {
        super(context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_wait);
    }
}
