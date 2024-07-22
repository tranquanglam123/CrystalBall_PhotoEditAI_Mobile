package com.samhung.crystalball.Common;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.samhung.crystalball.photoeditor.R;
import com.samhung.crystalball.photoeditor.Utilis.MeasureUtil;

public class CustomAlertDialog extends Dialog implements DialogInterface{
    String mStringMessage = "";
    String mPositiveButtonText = "예";
    String mNegativeButtonText = "예";
    OnClickListener mPositiveButtonClickerListener = null;
    OnClickListener mNegativeButtonClickerListener = null;

    Button btnYes=null;
    Button btnNo = null;

    float xdps = 0;
    public CustomAlertDialog(Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        float density = context.getResources().getDisplayMetrics().density;
        xdps = context.getResources().getDisplayMetrics().widthPixels * context.getResources().getDisplayMetrics().xdpi / density;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_alert);
//        getWindow().setLayout((int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.8f), (int)(getContext().getResources().getDisplayMetrics().heightPixels * 0.35f));
        getWindow().setLayout((int)(getContext().getResources().getDisplayMetrics().widthPixels * 0.9f), WindowManager.LayoutParams.WRAP_CONTENT);
        btnYes= ((Button)findViewById(R.id.button_yes));
        btnNo = ((Button)findViewById(R.id.button_no));
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        create();
    }

    public void create() {

       btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPositiveButtonClickerListener.onClick(CustomAlertDialog.this, BUTTON_POSITIVE);
            }
        });
       btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNegativeButtonClickerListener.onClick(CustomAlertDialog.this, BUTTON_NEGATIVE);
            }
        });

        btnYes.setText(mPositiveButtonText);
        btnNo.setText(mNegativeButtonText);
        setCanceledOnTouchOutside(true);

        TextView tvMessage = (TextView)findViewById(R.id.textView_Message);
        tvMessage.setText(mStringMessage);
    }
    public static class Builder {
        private static CustomAlertDialog P=null;
        public Builder(Context context) {P = new CustomAlertDialog(context);}
        public Builder setPositiveButton(String text, final OnClickListener onClickListener) {
            P.mPositiveButtonText = text;
            P.mPositiveButtonClickerListener = onClickListener;
            return this;
        }
        public Builder setNegativeButton(String text, final OnClickListener onClickListener) {
            P.mNegativeButtonText = text;
            P.mNegativeButtonClickerListener = onClickListener;
            return this;
        }

        public Builder setCancelable(Boolean cancelable) {P.setCancelable(cancelable); return this;}
        public Builder setMessage(String message) {P.mStringMessage = message; return this; }
        public CustomAlertDialog create() {
         //   P.create();
            return P;
        }
    }

}
