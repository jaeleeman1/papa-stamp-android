package com.subin.papastamp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.subin.papastamp.common.Constants;
import com.subin.papastamp.common.Util;

public class SplashActivity extends AppCompatActivity {
    private final String TAG = "[Splash Activity] : ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Permission check
        if (Util.checkPermission(SplashActivity.this)) {
            papastampStart();
        }
    }

    private void papastampStart() {
        Log.d(TAG, "Papastamp Start()");

        Thread mThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    SharedPreferences pref = getSharedPreferences(Constants.PREFERENCE_INTRO, Context.MODE_PRIVATE);
                    boolean needSkip = false;
                    if (pref != null) {
                        needSkip = pref.getBoolean(Constants.PREFERENCE_INTRO_SKIP, false);
                        Log.d(TAG, "Intro Preferences Check : " + needSkip);
                    }
                    Intent splashItntent;
                    if (needSkip) {
                        splashItntent = new Intent(SplashActivity.this, LoginActivity.class);
                        Log.d(TAG, "Start Login Activity()");
                    } else {
                        splashItntent = new Intent(SplashActivity.this, IntroActivity.class);
                        Log.d(TAG, "Start Intro Activity()");
                    }
                    startActivity(splashItntent);
                    finish();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        mThread.start();
    }

    //Permission check method
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult() enter");

        switch (requestCode) {
            case Util.PERMISSION_ALL:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (Util.checkPermission(SplashActivity.this)) {
                        papastampStart();
                    }
                } else {
                    //TODO: Use locale
                    Toast.makeText(SplashActivity.this, "Please confirm all permission", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "Undefined code");
                break;
        }
    }
}
