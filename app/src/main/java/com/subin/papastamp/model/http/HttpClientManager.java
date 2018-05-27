package com.subin.papastamp.model.http;

import android.util.Log;

import com.subin.papastamp.model.UserManager;
import com.subin.papastamp.model.ConfigManager;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.subin.papastamp.common.Constants.CONFIG_KEY_API_URL;

public class HttpClientManager {
	private static String mBaseUrl = null;
	private static String mAccessUid = null;
	private static HttpClientManager mInstance = null;
	private static OkHttpClient mHttpClient = null;
	private static HttpLoggingInterceptor mHttpLoggingInterceptor = null;
	public static final String TAG = "HttpClientManager";

	private HttpClientManager() {
		mHttpLoggingInterceptor = new HttpLoggingInterceptor();
		mHttpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
	}

	public static synchronized HttpClientManager getInstance() {
		if (mInstance == null)
			mInstance = new HttpClientManager();
		return mInstance;
	}

	public void initHeader() {
		Log.d(TAG, "initHeader() enter");

		mAccessUid = UserManager.getInstance().getUid();
		if (mAccessUid == null) {
			Log.e(TAG, "Invalid User ID");
		}

		Log.d(TAG, "User ID: " + mAccessUid);

		HttpInterceptor httpInterceptor = new HttpInterceptor(mAccessUid);

		mHttpClient = new OkHttpClient.Builder()
				.addInterceptor(httpInterceptor)
				.addInterceptor(mHttpLoggingInterceptor)
				.build();

		mBaseUrl = ConfigManager.getInstance().getProperty(CONFIG_KEY_API_URL);
	}

	public Call<HttpResponseFirebaseToken> getAdminAuthToken() {
		Log.d(TAG, "Papastamp getAdminAuthToken() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.getAdminAuthToken();
	}

	public Call<ResponseBody> insertUserInfo(HttpRequestUserInfo body) {
		Log.d(TAG, "Papastamp getAdminAuthToken() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.insertUserInfo(body);
	}

	public Call<ResponseBody> sendUserLoginToServer(HttpRequestUserInfo body) {
		Log.d(TAG, "Papastamp getAdminAuthToken() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.sendUserLoginToServer(body);
	}

	public Call<ResponseBody> insertAccessToken(String accessToken) {
		Log.d(TAG, "Papastamp getAdminAuthToken() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.insertAccessToken(accessToken);
	}

	public Call<ResponseBody> updateLocation(HttpRequestLocationInfo body) {
		Log.d(TAG, "updateLocation() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		//return service.updateLocation(uid, Double.toString(latitude), Double.toString(longitude));
		return service.updateLocation(body);
	}

	public Call<HttpResponseLoginInfo> userLoginCheck(HttpRequestLoginInfo body) {
		Log.d(TAG, "userLoginCheck() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		//return service.updateLocation(uid, Double.toString(latitude), Double.toString(longitude));
		return service.userLoginCheck(body);
	}

	public Call<ResponseBody> insertStampHistory(HttpRequestPushInfo body) {
		Log.d(TAG, "Papastamp getMapMain() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.insertStampHistory(body);
	}

	public Call<HttpResponseShopInfo> selectShopCodeToShopId(String shopCode) {
		Log.d(TAG, "Papastamp getMapMain() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.selectShopCodeToShopId(shopCode);
	}

	public Call<HttpResponseShopInfo> selectbeaconToShopId(String beaconCode) {
		Log.d(TAG, "Papastamp getMapMain() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.selectbeaconToShopId(beaconCode);
	}

	public Call<ResponseBody> requestStamp(HttpRequestPushInfo body) {
		Log.d(TAG, "Papastamp getMapMain() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.requestStamp(body);
	}

	public Call<ResponseBody> usedCoupon(HttpRequestPushInfo body) {
		Log.d(TAG, "Papastamp getMapMain() was called");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.usedCoupon(body);
	}

	public Call<ResponseBody> sendNotification(String uid, HttpRequestNotificationInfo body) {
		Log.d(TAG, "sendNotification() enter");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.sendNotification(uid, body);
	}

	public Call<ResponseBody> getUserNumber(String userNumber) {
		Log.d(TAG, "sendNotification() enter");

		if (mAccessUid == null || mAccessUid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.getUserNumber(userNumber);
	}
}