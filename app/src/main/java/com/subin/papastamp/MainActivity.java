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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.subin.papastamp.model.AccountManager;
import com.subin.papastamp.model.RecoMonitoringService;
import com.subin.papastamp.model.http.HttpClientManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.RunnableFuture;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

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
    private ToggleButton pushButton;
    private Vibrator vide;
    private AlertDialog requestStampDialog;

    // Stops scanning after 10 seconds.(스캐닝을 10초후에 자동으로 멈춥.)
    private static final long SCAN_PERIOD = 10000;

    private Boolean threadFlag = true;

    private static final String PAPAURL = "https://whereareevent.com/main";

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
        //User ID
        String userId = pushIntent.getExtras().getString("userId");

        pushButton = (ToggleButton) findViewById(R.id.pushButton);

        Log.d(TAG, "pushCheck: " + pushCheck);
        if(pushCheck.equals("hide")) {
            pushButton.setVisibility(View.GONE);
        }

        pushButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    scanLeDevice(true);
                    showDialog();
                } else {
                    threadFlag = true;
                    scanLeDevice(false);
                    mScanning = false;
                    mBLEScanner.stopScan(mScanCallback);
                    closeDialog();
                }
            }
        });

        WebView papastampWebView = (WebView) findViewById(R.id.webView);
        papastampWebView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = papastampWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        Map<String, String> extraHeaders = new HashMap<String, String>();

        try {
            extraHeaders.put("uid",userId);
            papastampWebView.loadUrl(PAPAURL, extraHeaders);
        }catch (Exception e){
            Log.d("Error",e.getMessage());
        }

        /*HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();

        Call<ResponseBody> call = httpClientManager.getMain();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<ResponseBody> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response: " + response);
                Log.d(TAG, "Response header: " + response.headers());
                Log.d(TAG, "call: " + call);

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK");
                } else {
                    Log.d(TAG, "REST API response failed");
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Log.d(TAG, "REST API request failed");
            }
        });*/

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

    private void showDialog()
    {
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

    private void closeDialog()
    {
        requestStampDialog.dismiss();
    }

    static class HttpAsyncTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... urls) {
            try {
                // HttpURLConnection.setFollowRedirects(false);
                HttpURLConnection con = (HttpURLConnection) new URL(urls[0]).openConnection();
                con.setInstanceFollowRedirects(false);
                con.setRequestMethod("GET");
                return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            } catch (Exception e) {
                return false;
            }
        }
    }
    //======================================================================

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

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Log.i("BackMonitoringService", "블루투스 찾기 시작");
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(pushButton.isChecked()) {
                        pushButton.performClick();
                        Toast.makeText(MainActivity.this, "요청 시간이 초과되었습니다. 다시 요청하세요", Toast.LENGTH_SHORT).show();
                    }
                    mScanning = false;
                    mBLEScanner.stopScan(mScanCallback);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            mBLEScanner.startScan(mScanCallback);
        } else {
            Log.i("BackMonitoringService", "블루투스 찾기 중지");
            threadFlag = true;
            mHandler.removeCallbacksAndMessages(null);
            mScanning = false;
            mBLEScanner.stopScan(mScanCallback);
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

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
//            Log.i("BackMonitoringService", "블루투스 1개 찾음 : " + result.getRssi());
            String compareBeacon = result.getDevice().toString();
            String deviceName = result.getDevice().getName();
            int rssi = result.getRssi();

            if("RECO".equals(deviceName) && "D6:EC:1F:36:3E:CA".equals(compareBeacon)) {
                if(rssi > -74) {
                    if (threadFlag) {
                        threadFlag = false;
                        Log.i("BackMonitoringService", "비콘 접근 완료 : " + compareBeacon);
                        Thread stampThread = new Thread(new Runnable(){
                            @Override
                            public void run() {
                                mHandler.post(new Runnable(){
                                    @Override
                                    public void run() {
                                        Log.i("BackMonitoringService", "찍음");
                                        try {
                                            Toast.makeText(MainActivity.this, "도장 찍기 완료", Toast.LENGTH_SHORT).show();
                                            vide.vibrate(700);
                                            (new HttpAsyncTask()).execute("https://whereareevent.com/tablet/temppushStamp");
                                            pushButton.performClick();
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
            return;
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

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;

        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }
}