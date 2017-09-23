package com.subin.papastamp.model;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class LocationManager implements LocationListener {
    private static LocationManager mInstance = null;
    private Context mContext;
    private static android.location.LocationManager mAndroidLocationManager;
    //	private static final long mMinInterval = 10000; //10sec for test
    private static final long mMinInterval = 300000; //5min
    //	private static final float mMaxDistance = 10; //10m for test
    private static final float mMaxDistance = 100; //100m
    private static final double INITIAL_LATITUDE = -181;
    private static final double INITIAL_LONGITUDE = -181;

    private static double mLatitude = INITIAL_LATITUDE;
    private static double mLongitude = INITIAL_LONGITUDE;

    private static final String TAG = "LocationManager";

    private LocationManager() {
    }

    public static LocationManager getInstance() {
        if (mInstance == null) {
            synchronized (LocationManager.class) {
                if (mInstance == null) {
                    mInstance = new LocationManager();
                }
            }
        }
        return mInstance;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location changed (" + location.getLatitude() + " " + location.getLongitude() + ")");

        if (mLatitude == INITIAL_LATITUDE && mLongitude == INITIAL_LONGITUDE) {
            Log.d(TAG, "Restarted map update");
        }

        // TODO: Remove mLatitude, mLongitude, mDirection
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.d(TAG, s + " provider status changed, status: " + i);
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.d(TAG, s + " provider enabled");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.d(TAG, s + " provider disabled");
    }

    // Methods
    public void init(Context context) {
        mContext = context;
        Log.d(TAG, "init() enter");

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission denied");

        } else {
            Log.d(TAG, "Permission ok");
            mAndroidLocationManager = (android.location.LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            Location lastKnownLocation = mAndroidLocationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                mLatitude = lastKnownLocation.getLatitude();
                mLongitude = lastKnownLocation.getLongitude();
                Log.d(TAG, "Last known location (" + mLatitude + " " + mLongitude + ")");
            } else {
                SharedPreferences pref = mContext.getSharedPreferences("pref", MODE_PRIVATE);
                mLatitude = 37.650804099999995;
                mLongitude = 126.88645269999999;
                Log.d(TAG, "Last cached location (" + mLatitude + " " + mLongitude + ")");
            }
        }
    }

    public Context getContext() {
        return mContext;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }
}
