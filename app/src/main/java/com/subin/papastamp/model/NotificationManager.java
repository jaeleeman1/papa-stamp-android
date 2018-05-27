package com.subin.papastamp.model;

import android.util.Log;

import com.subin.papastamp.model.http.HttpClientManager;
import com.subin.papastamp.model.http.HttpRequestLocationInfo;
import com.subin.papastamp.model.http.HttpRequestNotificationInfo;
import com.subin.papastamp.model.http.HttpResponseFirebaseToken;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationManager {
	private static NotificationManager mInstance;
	private NotificationListener mListener;
	public enum NotificationType {
		NOTIFICATION_TYPE_NOT_DEFINED(0),
		NOTIFICATION_TYPE_SSUP(1),
		NOTIFICATION_TYPE_SSUP_REPLY(2),
		NOTIFICATION_TYPE_CHAT_START(3);
		private int value;
		NotificationType(int value) {
			this.value = value;
		}
		public int getValue() {
			return this.value;
		}
	}

	private static final String TAG = "NotificationManage";

	public NotificationManager() {}

	public static NotificationManager getInstance() {
		if (mInstance == null) {
			synchronized (NotificationManager.class) {
				if (mInstance == null) {
					mInstance = new NotificationManager();
				}
			}
		}
		return mInstance;
	}

	// The specified listener is called in main thread.
	public void setNotificationListener(NotificationListener listener) {
		Log.d(TAG, "setNotificationListener() enter");

		mListener = listener;
	}


	public void deliverSsupNotification(String uid) {
		Log.d(TAG, "SSUP notification received!");

		if (mListener != null) {
			mListener.onMessageNotificationReceived(uid);
		}
	}

	public int sendSsupNotificationToServer(String uid, Double latitude, Double longitude,
											Double distance, String nickname, String expression) {
		return sendNotificationToServer(uid, latitude, longitude, distance, nickname, expression,
				NotificationType.NOTIFICATION_TYPE_SSUP.getValue());
	}

	public int sendSsupReplyNotificationToServer(String uid, Double latitude, Double longitude,
											Double distance, String nickname, String expression) {
		return sendNotificationToServer(uid, latitude, longitude, distance, nickname, expression,
				NotificationType.NOTIFICATION_TYPE_SSUP_REPLY.getValue());
	}

	public int sendChatStartNotificationToServer(String uid, Double latitude, Double longitude,
											Double distance, String nickname, String expression) {
		return sendNotificationToServer(uid, latitude, longitude, distance, nickname, expression,
				NotificationType.NOTIFICATION_TYPE_CHAT_START.getValue());
	}

	public int sendNotificationToServer(String uid, Double latitude, Double longitude,
										Double distance, String nickname, String expression,
										Integer type) {
		HttpClientManager httpClientManager = HttpClientManager.getInstance();

		HttpRequestNotificationInfo body = new HttpRequestNotificationInfo(latitude, longitude,
				distance, nickname, expression, type);
		Call<ResponseBody> call = httpClientManager.sendNotification(uid, body);
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
				Log.e(TAG, "REST API request error: " + t.getMessage());
			}
		});

		return 0;
	}
}