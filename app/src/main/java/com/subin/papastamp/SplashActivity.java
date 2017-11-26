package com.subin.papastamp;

import android.app.ProgressDialog;
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
import com.subin.papastamp.model.UserManager;

public class SplashActivity extends AppCompatActivity {
    private final String TAG = "[Splash Activity] : ";
    private Context mContext;
    private SplashActivity mActivity;
    private UserManager userManager;
    private String mUid;

    public SplashActivity() {
        mContext = this;
        mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        userManager = UserManager.getInstance();
        if (userManager.getContext() == null) {
            userManager.init(mContext);
        }

        mUid = userManager.getUid();
        Log.d(TAG, "access uid : " + mUid);

        if (Util.checkPermission(SplashActivity.this)) {
            papastampStart();
        }
    }

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

    private void papastampStart() {
        Log.d(TAG, "papastampStart() enter");
        final ProgressDialog progressDialog = ProgressDialog.show(SplashActivity.this, "Please wait...", "Progressing...", true);
        Thread mThread = new Thread() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                    SharedPreferences pref = getSharedPreferences(Constants.PREFERENCE_INTRO, Context.MODE_PRIVATE);
                    Log.d(TAG, "PREFERENCE_INTRO : " + pref);
                    boolean needSkip = false;
                    if (pref != null) {
                        needSkip = pref.getBoolean(Constants.PREFERENCE_INTRO_SKIP, false);
                        Log.d(TAG, "Intro Skip case : " + needSkip);
                    }
                    Intent splashItntent;
                    if (needSkip) {
                        splashItntent = new Intent(SplashActivity.this, LoginActivity.class);//LoginActivity
                        splashItntent.putExtra("userId", mUid);
                    } else {
                        splashItntent = new Intent(SplashActivity.this, IntroActivity.class);//IntroActivity
                        splashItntent.putExtra("userId", mUid);
                    }
                    startActivity(splashItntent);
                    finish();
                    progressDialog.dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        mThread.start();
    }
}
