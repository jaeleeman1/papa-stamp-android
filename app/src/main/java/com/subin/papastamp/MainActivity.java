package com.subin.papastamp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.view.WindowManager.LayoutParams;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.subin.papastamp.model.RecoMonitoringService;
import com.subin.papastamp.model.http.HttpClientManager;
import com.subin.papastamp.model.http.HttpRequestStampInfo;
import com.subin.papastamp.model.http.HttpResponseShopInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    static final String TAG = "MainActivity";

    //This is a default proximity uuid of the RECO
    public static final String RECO_UUID = "24DDF411-8CF1-440C-87CD-E368DAF9C93E";

    /**
     * SCAN_RECO_ONLY:
     * true일 경우 레코 비콘만 스캔하며, false일 경우 모든 비콘을 스캔합니다.
     * RECOBeaconManager 객체 생성 시 사용합니다.
     */
    public static final boolean SCAN_RECO_ONLY = true;

    /**
     * ENABLE_BACKGROUND_RANGING_TIMEOUT:
     * 백그라운드 ranging timeout을 설정합니다.
     * true일 경우, 백그라운드에서 입장한 region에서 ranging이 실행 되었을 때, 10초 후 자동으로 정지합니다.
     * false일 경우, 계속 ranging을 실행합니다. (배터리 소모율에 영향을 끼칩니다.)
     * RECOBeaconManager 객체 생성 시 사용합니다.
     */
    public static final boolean ENABLE_BACKGROUND_RANGING_TIMEOUT = true;

    /**
     * DISCONTINUOUS_SCAN:
     * 일부 안드로이드 기기에서 BLE 장치들을 스캔할 때, 한 번만 스캔 후 스캔하지 않는 버그(참고: http://code.google.com/p/android/issues/detail?id=65863)가 있습니다.
     * 해당 버그를 SDK에서 해결하기 위해, RECOBeaconManager에 setDiscontinuousScan() 메소드를 이용할 수 있습니다.
     * 해당 메소드는 기기에서 BLE 장치들을 스캔할 때(즉, ranging 시에), 연속적으로 계속 스캔할 것인지, 불연속적으로 스캔할 것인지 설정하는 것입니다.
     * 기본 값은 FALSE로 설정되어 있으며, 특정 장치에 대해 TRUE로 설정하시길 권장합니다.
     */
    public static final boolean DISCONTINUOUS_SCAN = false;
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 10;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBLEScanner;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean mScanning;
    private Handler mHandler;
    private ToggleButton stampPushButton;
    private ToggleButton couponPushButton;
    private Vibrator vide;
    private AlertDialog requestStampDialog;
    private AlertDialog requestCouponDialog;

    // Stops scanning after 10 seconds.(스캐닝을 10초후에 자동으로 멈춥.)
    private static final long SCAN_PERIOD = 10000;

    private Boolean threadFlag = true;

    private static String PAPAURL = "";

    private static String shopId = "";

    private static String shopBeacon = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        //진동
        vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        Intent pushIntent = getIntent();
        //Push Check
        String pushCheck = pushIntent.getExtras().getString("pushCheck");
        Log.d(TAG, "pushCheck: " + pushCheck);
        //Shop ID
        shopId = pushIntent.getExtras().getString("shopId");
        Log.d(TAG, "shopId: " + shopId);
        //Shop Beacon
        shopBeacon = pushIntent.getExtras().getString("shopBeacon");
        Log.d(TAG, "shopBeacon: " + shopBeacon);
        //User ID
        String mUid = pushIntent.getExtras().getString("userId");
        Log.d(TAG, "userId: " + pushCheck);

        String mLatitude = pushIntent.getExtras().getString("mLatitude");
        String mLongitude = pushIntent.getExtras().getString("mLongitude");
        Log.d(TAG, "mLatitude: " + pushCheck);
        Log.d(TAG, "mLongitude: " + pushCheck);

        stampPushButton = (ToggleButton) findViewById(R.id.stampPushButton);
        couponPushButton = (ToggleButton) findViewById(R.id.couponPushButton);
        couponPushButton.setVisibility(View.GONE);

        if(pushCheck.equals("hide")) {
            stampPushButton.setVisibility(View.GONE);
            PAPAURL = "https://whereareevent.com/v1/shop/main?user_id="+mUid +"&current_lat="+ mLatitude +"&current_lng=" + mLongitude;
        }else {
            Log.d(TAG, "shopId: " + shopId);
            PAPAURL = "https://whereareevent.com/v1/stamp/main?user_id="+mUid + "&shop_id="+shopId + "&current_lat=37.650804099999995&current_lng=126.88645269999999";
        }

        stampPushButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isStampChecked) {
                if (isStampChecked == true){
                    scanLeStampDevice(true);
                    showStampDialog();
                } else {
                    threadFlag = true;
                    scanLeStampDevice(false);
                    mScanning = false;
                    mBLEScanner.stopScan(mScanStampCallback);
                    closeStampDialog();
                }
            }
        });

        WebView papastampWebView = (WebView) findViewById(R.id.webView);
        papastampWebView.setWebViewClient(new WebViewClient());
        papastampWebView.addJavascriptInterface(this, "Bridge");
        WebSettings webSettings = papastampWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);

        try {
//            papastampWebView.loadUrl(PAPAURL+"/"+mUid);
            papastampWebView.loadUrl(PAPAURL);
        }catch (Exception e){
            Log.d("Error",e.getMessage());
        }

        //사용자가 블루투스를 켜도록 요청합니다.
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mHandler = new Handler();
        mLeDeviceListAdapter = new LeDeviceListAdapter();

        if(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        }
        mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();

        RecoeBackgroundStart();

        /**
         * 안드로이드 API 23 (마시멜로우)이상 버전부터, 정상적으로 RECO SDK를 사용하기 위해서는
         * 위치 권한 (ACCESS_COARSE_LOCATION 혹은 ACCESS_FINE_LOCATION)을 요청해야 합니다.
         * 권한 요청의 경우, 구글에서 제공하는 가이드를 참고하시기 바랍니다.
         *
         * http://www.google.com/design/spec/patterns/permissions.html
         * https://github.com/googlesamples/android-RuntimePermissions
         */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is not granted.");
                this.requestLocationPermission();
            } else {
                Log.i("MainActivity", "The location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is already granted.");
            }
        }
    }

    @JavascriptInterface
    public void callSettingsActivity(final String message) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                couponPushButton.setVisibility(View.VISIBLE);
                couponPushButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isCouponChecked) {
                        if (isCouponChecked == true){
                            scanLeCouponDevice(true);
                            showCouponDialog();
                        } else {
                            threadFlag = true;
                            scanLeCouponDevice(false);
                            mScanning = false;
                            mBLEScanner.stopScan(mScanCouponCallback);
                            closeCouponDialog();
                        }
                    }
                });
            }
        });
    }

    private void updateStamp() {
        Log.i(TAG, "updateStamp");

        HttpClientManager httpClientManager = HttpClientManager.getInstance();

        httpClientManager.initHeader();
        HttpRequestStampInfo body = new HttpRequestStampInfo(shopId);
        Call<ResponseBody> call = httpClientManager.updateStamp(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response: " + response);
                Log.d(TAG, "Response header: " + response.headers());

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK");
                    if (response.body() != null) {
                        try {
                            //TODO: Use response.body().bytes() to handle thumbnail file image
                            Log.d(TAG, "Response body: " + response.body().string());
                        } catch (IOException e) {
                        }
                    } else {
                        Log.d(TAG, "REST API response failed");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "REST API request failed");
            }
        });
    }

    private void updateCoupon() {
        Log.i(TAG, "updateCoupon");

        HttpClientManager httpClientManager = HttpClientManager.getInstance();

        httpClientManager.initHeader();
        HttpRequestStampInfo body = new HttpRequestStampInfo(shopId, "couponNumber");
        Call<ResponseBody> call = httpClientManager.updateCoupon(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response: " + response);
                Log.d(TAG, "Response header: " + response.headers());

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK");
                    if (response.body() != null) {
                        try {
                            //TODO: Use response.body().bytes() to handle thumbnail file image
                            Log.d(TAG, "Response body: " + response.body().string());
                        } catch (IOException e) {
                        }
                    } else {
                        Log.d(TAG, "REST API response failed");
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "REST API request failed");
            }
        });
    }

    public void showStampDialog() {
        Context mContext = getApplicationContext();

        LayoutInflater inflater
                = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View stampLayout = inflater.inflate(R.layout.stamp_image, null);

        ImageView image = (ImageView) stampLayout.findViewById(R.id.papaStampImage);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(image);
        Glide.with(this).load(R.drawable.stamp_icon).into(imageViewTarget);

        AlertDialog.Builder myDialogBuilder = new AlertDialog.Builder(this, R.style.stamp_dialog);
        TextView title = new TextView(this);
        title.setText(R.string.dialog_title);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.RED);
        title.setTextSize(12);
        myDialogBuilder.setCancelable(false);
        myDialogBuilder.setCustomTitle(title);
        myDialogBuilder.setMessage(R.string.dialog_message);

        requestStampDialog = myDialogBuilder.create();
        requestStampDialog.setView(stampLayout);

        LayoutParams params = requestStampDialog.getWindow().getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.height = 1200;
        params.dimAmount = 0.8f;
        params.y = 230;
        params.gravity = Gravity.TOP;

        requestStampDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        requestStampDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        requestStampDialog.getWindow().setAttributes(params);

        requestStampDialog.show();

        TextView textView = (TextView) requestStampDialog.findViewById(android.R.id.message);
        textView.setTextSize(30.0f);
    }

    private void closeStampDialog() {
        requestStampDialog.dismiss();
    }

    public void showCouponDialog() {
        Context mContext = getApplicationContext();

        LayoutInflater inflater
                = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View stampLayout = inflater.inflate(R.layout.stamp_image, null);

        ImageView image = (ImageView) stampLayout.findViewById(R.id.papaStampImage);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(image);
        Glide.with(this).load(R.drawable.stamp_icon).into(imageViewTarget);

        AlertDialog.Builder myDialogBuilder = new AlertDialog.Builder(this, R.style.stamp_dialog);
        TextView title = new TextView(this);
        title.setText(R.string.dialog_title);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.RED);
        title.setTextSize(12);
        myDialogBuilder.setCancelable(false);
        myDialogBuilder.setCustomTitle(title);
        myDialogBuilder.setMessage(R.string.dialog_message);

        requestCouponDialog = myDialogBuilder.create();
        requestCouponDialog.setView(stampLayout);

        LayoutParams params = requestCouponDialog.getWindow().getAttributes();
        params.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        params.height = 1200;
        params.dimAmount = 0.8f;
        params.y = 230;
        params.gravity = Gravity.TOP;

        requestCouponDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        requestCouponDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        requestCouponDialog.getWindow().setAttributes(params);

        requestCouponDialog.show();

        TextView textView = (TextView) requestCouponDialog.findViewById(android.R.id.message);
        textView.setTextSize(30.0f);
    }

    private void closeCouponDialog() {
        requestCouponDialog.dismiss();
    }

/*    private Button.OnClickListener pushButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //startLeScan(UUID[], BluetoothAdapter.LeScanCallback)
            scanLeDevice(true);
            pushButton.setTextColor(0xffffff);
        }
    };

    private Button.OnClickListener stopButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            scanLeDevice(false);
            pushButton.setTextColor(0xFF000000);
        }
    };*/

    private void scanLeStampDevice(final boolean enable) {
        if (enable) {
            Log.i("BackMonitoringService", "블루투스 찾기 시작");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(stampPushButton.isChecked()) {
                        stampPushButton.performClick();
                        Toast.makeText(MainActivity.this, "요청 시간이 초과되었습니다. 다시 요청하세요", Toast.LENGTH_SHORT).show();
                    }
                    mScanning = false;
                    mBLEScanner.stopScan(mScanStampCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBLEScanner.startScan(mScanStampCallback);
        } else {
            Log.i("BackMonitoringService", "블루투스 찾기 중지");
            threadFlag = true;
            mHandler.removeCallbacksAndMessages(null);
            mScanning = false;
            mBLEScanner.stopScan(mScanStampCallback);
        }
    }

    private void scanLeCouponDevice(final boolean enable) {
        if (enable) {
            Log.i("BackMonitoringService", "블루투스 찾기 시작");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(couponPushButton.isChecked()) {
                        couponPushButton.performClick();
                        Toast.makeText(MainActivity.this, "요청 시간이 초과되었습니다. 다시 요청하세요", Toast.LENGTH_SHORT).show();
                    }
                    mScanning = false;
                    mBLEScanner.stopScan(mScanCouponCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBLEScanner.startScan(mScanCouponCallback);
        } else {
            Log.i("BackMonitoringService", "블루투스 찾기 중지");
            threadFlag = true;
            mHandler.removeCallbacksAndMessages(null);
            mScanning = false;
            mBLEScanner.stopScan(mScanCouponCallback);
        }
    }

    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDevices;
        private LayoutInflater mInflator;

        public LeDeviceListAdapter() {
            super();
            mLeDevices = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            if(!mLeDevices.contains(device)) {
                mLeDevices.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDevices.get(position);
        }

        public void clear() {
            mLeDevices.clear();
        }

        @Override
        public int getCount() {
            return mLeDevices.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDevices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            BluetoothDevice device = mLeDevices.get(i);
            final String deviceName = device.getName();

            Log.i("BackMonitoringService", "블루투스 이름 : " + deviceName);

            return view;
        }
    }

    private ScanCallback mScanStampCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            Log.i("BackMonitoringService", "블루투스 1개 찾음 : " + result.getRssi());
            String compareBeacon = result.getDevice().toString();
            String deviceName = result.getDevice().getName();
            int rssi = result.getRssi();

            if("RECO".equals(deviceName) && shopBeacon.equals(compareBeacon)) {
                if(rssi > -74) {
                    if (threadFlag) {
                        threadFlag = false;
                        Log.i("BackMonitoringService", "스탬프 비콘 접근 완료 : " + compareBeacon);
                        Thread stampThread = new Thread(new Runnable(){
                            @Override
                            public void run() {
                                mHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        Log.i("BackMonitoringService", "스탬프 찍음");
                                        try {
                                            vide.vibrate(700);
                                            updateStamp();
                                            Toast.makeText(MainActivity.this, "스탬프 찍기 완료", Toast.LENGTH_SHORT).show();
                                            stampPushButton.performClick();
                                            stampPushButton.setVisibility(View.GONE);
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            System.err.println(e.getMessage());
                                        }
                                    }
                                });
                            }
                        });
                        stampThread.setDaemon(true);
                        stampThread.start();
                    }
                }
            }
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i("BackMonitoringService", "블루투스 여러개 찾음");
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("BackMonitoringService", "블루투스 못 찾음");
        }

        private void processResult(final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private ScanCallback mScanCouponCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            Log.i("BackMonitoringService", "블루투스 1개 찾음 : " + result.getRssi());
            String compareBeacon = result.getDevice().toString();
            String deviceName = result.getDevice().getName();
            int rssi = result.getRssi();

            if("RECO".equals(deviceName) && shopBeacon.equals(compareBeacon)) {
                if(rssi > -74) {
                    if (threadFlag) {
                        threadFlag = false;
                        Log.i("BackMonitoringService", "쿠폰 비콘 접근 완료 : " + compareBeacon);
                        Thread stampThread = new Thread(new Runnable(){
                            @Override
                            public void run() {
                                mHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        Log.i("BackMonitoringService", "쿠폰 찍음");
                                        try {
                                            vide.vibrate(700);
                                            updateCoupon();
                                            Toast.makeText(MainActivity.this, "쿠폰 사용 완료", Toast.LENGTH_SHORT).show();
                                            couponPushButton.performClick();
                                            couponPushButton.setVisibility(View.GONE);
                                            Thread.sleep(100);
                                        } catch (InterruptedException e) {
                                            System.err.println(e.getMessage());
                                        }
                                    }
                                });
                            }
                        });
                        stampThread.setDaemon(true);
                        stampThread.start();
                    }
                }
            }
            processResult(result);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i("BackMonitoringService", "블루투스 여러개 찾음");
            for (ScanResult result : results) {
                processResult(result);
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.i("BackMonitoringService", "블루투스 못 찾음");
        }

        private void processResult(final ScanResult result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mLeDeviceListAdapter.addDevice(result.getDevice());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    public void RecoeBackgroundStart() {
        Log.i("BackMonitoringService", "비콘에 연결합니다.");
        Intent intent = new Intent(this, RecoMonitoringService.class);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            //사용자가 블루투스 요청을 허용하지 않았을 경우, 어플리케이션은 종료됩니다.
            finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case REQUEST_LOCATION : {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Snackbar.make(mLayout, R.string.location_permission_granted, Snackbar.LENGTH_LONG).show();
//                } else {
//                    Snackbar.make(mLayout, R.string.location_permission_not_granted, Snackbar.LENGTH_LONG).show();
//                }
            }
            default :
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(this.isBackgroundMonitoringServiceRunning(this)) {
//            ToggleButton toggle = (ToggleButton)findViewById(R.id.backgroundMonitoringToggleButton);
//            toggle.setChecked(true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * In order to use RECO SDK for Android API 23 (Marshmallow) or higher,
     * the location permission (ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION) is required.
     *
     * This sample project requests "ACCESS_COARSE_LOCATION" permission only,
     * but you may request "ACCESS_FINE_LOCATION" permission depending on your application.
     *
     * "ACCESS_COARSE_LOCATION" permission is recommended.
     *
     * 안드로이드 API 23 (마시멜로우)이상 버전부터, 정상적으로 RECO SDK를 사용하기 위해서는
     * 위치 권한 (ACCESS_COARSE_LOCATION 혹은 ACCESS_FINE_LOCATION)을 요청해야 합니다.
     *
     * 본 샘플 프로젝트에서는 "ACCESS_COARSE_LOCATION"을 요청하지만, 필요에 따라 "ACCESS_FINE_LOCATION"을 요청할 수 있습니다.
     *
     * 당사에서는 ACCESS_COARSE_LOCATION 권한을 권장합니다.
     *
     */
    private void requestLocationPermission() {
        if(!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
        }

//        Snackbar.make(mLayout, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                .setAction(R.string.ok, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION);
//                    }
//                })
//                .show();
    }

    private boolean isBackgroundMonitoringServiceRunning(Context context) {
        ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        for(ActivityManager.RunningServiceInfo runningService : am.getRunningServices(Integer.MAX_VALUE)) {
            if(RecoMonitoringService.class.getName().equals(runningService.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed() enter");
        moveTaskToBack(true);
    }

    /*private void sendLocationToServer(String uid, Double latitude, Double longitude) {
        Log.i(TAG, "send Location to server");

        HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();
        HttpRequestLocationInfo body = new HttpRequestLocationInfo(latitude, longitude);
        Call<ResponseBody> call = httpClientManager.updateLocation(uid, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response: " + response);
                Log.d(TAG, "Response header: " + response.headers());
                Log.d(TAG, "Response body: " + response.body());
                Log.d(TAG, "call: " + call);

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK");
                } else {
                    Log.d(TAG, "REST API response failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "REST API request failed");
            }
        });
    }*/
}