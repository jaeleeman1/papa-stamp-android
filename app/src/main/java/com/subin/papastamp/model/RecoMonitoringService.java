package com.subin.papastamp.model;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.perples.recosdk.RECOBeacon;
import com.perples.recosdk.RECOBeaconManager;
import com.perples.recosdk.RECOBeaconRegion;
import com.perples.recosdk.RECOBeaconRegionState;
import com.perples.recosdk.RECOErrorCode;
import com.perples.recosdk.RECOMonitoringListener;
import com.perples.recosdk.RECOServiceConnectListener;
import com.subin.papastamp.MainActivity;
import com.subin.papastamp.R;
import com.subin.papastamp.model.http.HttpClientManager;
import com.subin.papastamp.model.http.HttpResponseShopInfo;

import java.util.ArrayList;
import java.util.Collection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecoMonitoringService extends Service implements RECOMonitoringListener, RECOServiceConnectListener{
    static final String TAG = "RecoMonitoring";
    private long mScanDuration = 1*1000L;
    private long mSleepDuration = 10*1000L;
    private long mRegionExpirationTime = 60*1000L;
    private int mNotificationID = 9999;

    private RECOBeaconManager mRecoManager;
    private ArrayList<RECOBeaconRegion> mRegions;

    private Context mContext;
    private UserManager userManager;
    private String mUid;

    @Override
    public void onCreate() {
        Log.i("BackMonitoringService", "onCreate()");
        super.onCreate();

        mContext = this;

        userManager = UserManager.getInstance();
        if (userManager.getContext() == null) {
            userManager.init(mContext);
        }

        mUid = userManager.getUid();
        Log.d(TAG, "access uid : " + mUid);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BackMonitoringService", "onStartCommand()");

        mRecoManager = RECOBeaconManager.getInstance(getApplicationContext(), MainActivity.SCAN_RECO_ONLY, MainActivity.ENABLE_BACKGROUND_RANGING_TIMEOUT);
        this.bindRECOService();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("BackMonitoringService", "onDestroy()");
        this.tearDown();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i("BackMonitoringService", "onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
    }

    private void bindRECOService() {
        Log.i("BackMonitoringService", "bindRECOService()");

        mRegions = new ArrayList<RECOBeaconRegion>();
        this.generateBeaconRegion();

        mRecoManager.setMonitoringListener(this);
        mRecoManager.bind(this);
    }

    private void generateBeaconRegion() {
        Log.i("BackMonitoringService", "generateBeaconRegion()");

        RECOBeaconRegion recoRegion;

        recoRegion = new RECOBeaconRegion(MainActivity.RECO_UUID, "Papa Stamp Region");
        recoRegion.setRegionExpirationTimeMillis(mRegionExpirationTime);
        mRegions.add(recoRegion);
    }

    private void startMonitoring() {
        Log.i("BackMonitoringService", "startMonitoring()");

        mRecoManager.setScanPeriod(mScanDuration);
        mRecoManager.setSleepPeriod(mSleepDuration);

        for(RECOBeaconRegion region : mRegions) {
            try {
                mRecoManager.startMonitoringForRegion(region);
            } catch (RemoteException e) {
                Log.e("BackMonitoringService", "RemoteException has occured while executing RECOManager.startMonitoringForRegion()");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.e("BackMonitoringService", "NullPointerException has occured while executing RECOManager.startMonitoringForRegion()");
                e.printStackTrace();
            }
        }
    }

    private void stopMonitoring() {
        Log.i("BackMonitoringService", "stopMonitoring()");

        for(RECOBeaconRegion region : mRegions) {
            try {
                mRecoManager.stopMonitoringForRegion(region);
            } catch (RemoteException e) {
                Log.e("BackMonitoringService", "RemoteException has occured while executing RECOManager.stopMonitoringForRegion()");
                e.printStackTrace();
            } catch (NullPointerException e) {
                Log.e("BackMonitoringService", "NullPointerException has occured while executing RECOManager.stopMonitoringForRegion()");
                e.printStackTrace();
            }
        }
    }

    private void tearDown() {
        Log.i("BackMonitoringService", "tearDown()");
        this.stopMonitoring();

        try {
            mRecoManager.unbind();
        } catch (RemoteException e) {
            Log.e("BackMonitoringService", "RemoteException has occured while executing unbind()");
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceConnect() {
        Log.i("BackMonitoringService", "onServiceConnect()");
        this.startMonitoring();
    }

    @Override
    public void didDetermineStateForRegion(RECOBeaconRegionState state, RECOBeaconRegion region) {
        Log.i("BackMonitoringService", "비콘 변화 감지 : " + state);
    }

    @Override
    public void didEnterRegion(RECOBeaconRegion region, Collection<RECOBeacon> beacons) {
        /**
         * For the first run, this callback method will not be called.
         * Please check the state of the region using didDetermineStateForRegion() callback method.
         *
         * 최초 실행시, 이 콜백 메소드는 호출되지 않습니다.
         * didDetermineStateForRegion() 콜백 메소드를 통해 region 상태를 확인할 수 있습니다.
         */

        //Get the region and found beacon list in the entered region
        Log.i("BackMonitoringService", "비콘 지역 들어옴 : " + region.getUniqueIdentifier());
        ArrayList<RECOBeacon> monitoringBeacons = new ArrayList<RECOBeacon>(beacons);
        for (RECOBeacon monitoringBeacon : monitoringBeacons) {
            String papaStampMajor = String.valueOf(monitoringBeacon.getMajor());
            String papaStampMinor = String.valueOf(monitoringBeacon.getMinor());
            Log.i("BackMonitoringService", papaStampMajor+papaStampMinor);
            selectShopCodeToShopId(papaStampMajor+papaStampMinor);
        }
        //Write the code when the device is enter the region
    }

    @Override
    public void didExitRegion(RECOBeaconRegion region) {
        /**
         * For the first run, this callback method will not be called.
         * Please check the state of the region using didDetermineStateForRegion() callback method.
         *
         * 최초 실행시, 이 콜백 메소드는 호출되지 않습니다.
         * didDetermineStateForRegion() 콜백 메소드를 통해 region 상태를 확인할 수 있습니다.
         */

        Log.i("BackMonitoringService", "비콘 지역 벗어남 : " + region.getUniqueIdentifier());
//        showMessage(this,"파파스탬프","방문해 주셔서 감사합니다.", "Ticket", "구분값");
        //Write the code when the device is exit the region
    }

    @Override
    public void didStartMonitoringForRegion(RECOBeaconRegion region) {
        Log.i("BackMonitoringService", "비콘 모니터링 시작! : " + region.getUniqueIdentifier());
        //Write the code when starting monitoring the region is started successfully
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This method is not used
        return null;
    }

    @Override
    public void onServiceFail(RECOErrorCode errorCode) {
        //Write the code when the RECOBeaconService is failed.
        //See the RECOErrorCode in the documents.
        return;
    }

    @Override
    public void monitoringDidFailForRegion(RECOBeaconRegion region, RECOErrorCode errorCode) {
        //Write the code when the RECOBeaconService is failed to monitor the region.
        //See the RECOErrorCode in the documents.
        return;
    }

    private void showMessage(Context context, String title, String msg, String ticker, String shopCode, String shopId, String shopBeacon) {
        //비콘 신호 수신시 메시지 전송....
        NotificationManager mManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        PendingIntent pendingIntent ;
        int notifyID=  Integer.parseInt(shopCode);

        Intent pushIntent =new Intent(getApplicationContext(), MainActivity.class);
        pushIntent.putExtra("pushCheck", "show");
        pushIntent.putExtra("userId", mUid);

        pushIntent.putExtra("shopId", shopId);
        pushIntent.putExtra("shopBeacon", shopBeacon);
        pendingIntent = PendingIntent.getActivity(context, notifyID, pushIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder;

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.setBigContentTitle(title)
                .bigText(msg);
        System.currentTimeMillis();

        mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.stamp_action) //작은 아이콘 이미지
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.papastamp_icon))
                .setContentTitle("파파 스탬프")
                .setContentText("결제 시 스탬프 요청 버튼 클릭!")
                .setContentIntent(pendingIntent)
                .setNumber(1)
                .setAutoCancel(true)//선택하면 사라진다.
                .setTicker(ticker)
                .setVibrate(new long[]{1000, 1000})
                .setStyle(style);

        mManager.notify(notifyID, mBuilder.build());
    }

    private void selectShopCodeToShopId(String shopCode) {
        Log.i(TAG, "Select stamp code");

        HttpClientManager httpClientManager = HttpClientManager.getInstance();

        httpClientManager.initHeader();
        Call<HttpResponseShopInfo> call = httpClientManager.selectShopCodeToShopId(shopCode);
        call.enqueue(new Callback<HttpResponseShopInfo>() {
            @Override
            public void onResponse(Call<HttpResponseShopInfo> call, Response<HttpResponseShopInfo> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response: " + response);
                Log.d(TAG, "Response header: " + response.headers());

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK");
                    if (response.body() != null) {
                        Log.d(TAG, "Response body: " + response.body().shopCode);
                        Log.d(TAG, "Response body: " + response.body().shopId);
                        String responseShopCode = response.body().shopCode;
                        String responseShopId = response.body().shopId;
                        String responseShopBeacon = response.body().shopBeacon;

                        showMessage(mContext, "파파스탬프","쿠폰 적립을 쉽고 간편하게~!!", "Online Stamp Management", responseShopCode, responseShopId, responseShopBeacon);
                    } else {
                        Log.d(TAG, "REST API response failed");
                    }
                }
            }

            @Override
            public void onFailure(Call<HttpResponseShopInfo> call, Throwable t) {
                Log.d(TAG, "REST API request failed");
            }
        });
    }
}