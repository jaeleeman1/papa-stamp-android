package com.subin.papastamp.common;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class Util {
    private static String TAG = "[Util] : ";

    // permission
    public static final int PERMISSION_ALL = 0;

    public static boolean checkPermission(Activity activity) {
        if ((ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                        != PackageManager.PERMISSION_GRANTED)) {
            Log.d(TAG, "Permission READ_PHONE_STATE denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_PHONE_STATE)) {
                Log.d(TAG, "Permission READ_PHONE_STATE, ACCESS_FINE_LOCATION was already disabled by user");
            } else {
                //Log.d(TAG, "First permission READ_PHONE_STATE check");
            }

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ALL);
        }else {
            Log.d(TAG, "Permission PERMISSION_ALL ok");
            return true;
        }
        return false;
    }
}
