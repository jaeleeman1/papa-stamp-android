package com.subin.papastamp.model.http;

import android.util.Log;

import com.subin.papastamp.model.AccountManager;
import com.subin.papastamp.model.ConfigManager;

import java.util.List;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.subin.papastamp.Common.Constants.CONFIG_KEY_API_URL;

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

		mAccessUid = AccountManager.getInstance().getAccessUid();
		if (mAccessUid == null) {
			Log.e(TAG, "Invalid Firebase ID");
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

	public Call<ResponseBody> updateLocation(String uid, HttpRequestLocationInfo body) {
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

	/*public Call<ResponseBody> addAccount(HttpRequestAccountInfo body) {
		Log.d(TAG, "HttpClientManager.addAccount() was called");

		if (mAccessPidToken == null || mAccessPidToken.isEmpty())
		{
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.addAccount(body);
	}

	public Call<ResponseBody> updateAccessToken() {
		Log.d(TAG, "updateAccessToken() enter");

		if (mIdToken == null || mIdToken.isEmpty())
		{
			return null;
		}
		if (mAid == null || mAid.isEmpty())
		{
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.updateAccessToken();
	}

	public Call<ResponseBody> addProfile(RequestBody nickname, MultipartBody.Part thumbnail,
			RequestBody expression, RequestBody city, RequestBody gender) {
		Log.d(TAG, "HttpClientManager.addProfile() was called");

		if (mIdToken == null || mIdToken.isEmpty())
		{
			return null;
		}
		if (mAid == null || mAid.isEmpty())
		{
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.addProfile(nickname, thumbnail, expression, city, gender);
	}

	public Call<ResponseBody> updateProfileInfo(String uid, HttpRequestProfileInfo body) {
		Log.d(TAG, "HttpClientManager.updateProfileInfo() was called");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.updateProfileInfo(uid, body);
	}

	public Call<ResponseBody> updateProfileThumbnail(String uid, MultipartBody.Part thumbnail) {
		Log.d(TAG, "HttpClientManager.updateProfileThumbnail() was called");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.updateProfileThumbnail(uid, thumbnail);
	}

	public Call<List<HttpResponseProfileInfo>> getProfileInfo(String uid) {
		Log.d(TAG, "HttpClientManager.getProfileInfo() was called");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.getProfileInfo(uid);
	}

	public Call<ResponseBody> getProfileThumbnail(String uid) {
		Log.d(TAG, "HttpClientManager.getProfileThumbnail() was called");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.getProfileThumbnail(uid);
	}

	public Call<ResponseBody> updateLocation(String uid, HttpRequestLocationInfo body) {
		Log.d(TAG, "updateLocation() was called");

		if (mIdToken == null || mIdToken.isEmpty()) {
			Log.e(TAG, "Access token is invalid");
			return null;
		}
		if (mAid == null || mAid.isEmpty()) {
			Log.e(TAG, "AID is invalid");
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		//return service.updateLocation(uid, Double.toString(latitude), Double.toString(longitude));
		return service.updateLocation(uid, body);
	}

	public Call<List<HttpResponseLocationInfo>> getFriendsNearby(Double latitude, Double longitude, Integer limit) {
		Log.d(TAG, "getFriendsNearby() was called");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		String location = "latitude=" + Double.toString(latitude) + ",longitude=" + Double.toString(longitude);

		return service.getFriendsNearby(location, Integer.toString(limit));
	}

	public Call<ResponseBody> sendNotification(String uid, HttpRequestNotificationInfo body) {
		Log.d(TAG, "sendNotification() enter");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.sendNotification(uid, body);
	}

	public Call<ResponseBody> addContactList(String uid, HttpRequestContactInfo body) {
		Log.d(TAG, "addContactList() enter");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.addContactList(uid, body);
	}

	public Call<List<HttpResponseContactInfo>> getCandidateFriends() {
		Log.d(TAG, "getCandidateFriends() enter");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.getCandidateFriends();
	}

	public Call<List<HttpResponseContactInfo>> getFriends() {
		Log.d(TAG, "getFriends() enter");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.getFriends();
	}

	public Call<ResponseBody> addFriend(String uid) {
		Log.d(TAG, "addFriend() enter");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.addFriend(uid);
	}

	public Call<ResponseBody> addFriendsList(HttpRequestContactInfo body) {
		Log.d(TAG, "addFriendsList() enter");

		if (mIdToken == null || mIdToken.isEmpty())
			return null;
		if (mAid == null || mAid.isEmpty())
			return null;

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.client(mHttpClient)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.addFriendsList(body);
	}

	public Call<ResponseBody> updateUserStatus(HttpRequestStatusInfo body) {
		Log.d(TAG, "updateUserStatus() enter");

		if (mIdToken == null || mIdToken.isEmpty())	{
			return null;
		}
		if (mAid == null || mAid.isEmpty()) {
			return null;
		}

		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl(mBaseUrl)
				.addConverterFactory(GsonConverterFactory.create())
				.client(mHttpClient)
				.build();

		RestApiService service = retrofit.create(RestApiService.class);

		return service.updateUserStatus(body);
	}*/
}