package com.samhung.crystalball;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.samhung.inapp.ResourceHelper;
import com.samhung.crystalball.photoeditor.MainActivity;
import com.samhung.crystalball.photoeditor.R;
import com.shitc.license.ProductLicense;


public class SplashActivity extends Activity {
    final int APP_ID = 40434;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ResourceHelper.initPackageName(this);
        setContentView(R.layout.activity_splash_);

        final ProductLicense license = new ProductLicense(SplashActivity.this, 1, APP_ID);
        int nResult = ProductLicense.nResultCode;

        TextView btnNext = (TextView)findViewById(ResourceHelper.getViewId("lblComment"));
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(ProductLicense.nResultCode !=0)
//                {
//                    license.showProsecutorQR(SplashActivity.this, 1, APP_ID);
//                }
                if(!MainActivity.bAppLaunched) {
                    Intent intent = new Intent(getBaseContext(), MainActivity.class);
                    startActivity(intent);
                }
                SplashActivity.this.finish();
            }
        });

        ImageView btnHelp = (ImageView)findViewById(ResourceHelper.getViewId("adsBtn"));
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(SplashActivity.this, AdvertiseActivity.class);
//                startActivity(intent);
//                SplashActivity.this.finish();
            }
        });

        ImageView btnQR = (ImageView)findViewById(ResourceHelper.getViewId("qrBtn"));
        btnQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               license.showProsecutorQR(SplashActivity.this, 1, APP_ID);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
