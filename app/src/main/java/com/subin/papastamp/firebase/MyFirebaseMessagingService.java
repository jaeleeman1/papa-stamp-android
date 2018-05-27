package com.subin.papastamp.firebase;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.subin.papastamp.MainActivity;
import com.subin.papastamp.R;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
	private static final String TAG = "SSUP_MyFirebaseMessagin";
	private Context mContext;

	@Override
	public void onCreate() {
		super.onCreate();

		mContext = this;
	}
	/**
	 * Called when message is received.
	 *
	 * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
	 */
	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		// There are two types of messages data messages and notification messages. Data messages are handled
		// here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
		// traditionally used with FCM. Notification messages are only received here in onMessageReceived when the app
		// is in the foreground. When the app is in the background an automatically generated notification is displayed.
		// When the user taps on the notification they are returned to the app. Messages containing both notification
		// and data payloads are treated as notification messages. The Firebase console always sends notification
		// messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
			String title = remoteMessage.getNotification().getTitle();
			String message = remoteMessage.getNotification().getBody();
			showMessage(mContext, title, message, "Online Stamp Management");
		}else {
			Log.d(TAG, "Message data payload: " + remoteMessage.getData());
			String title = remoteMessage.getData().get("title");
			String message = remoteMessage.getData().get("body");
			showMessage(mContext, title, message, "Online Stamp Management");
		}

		super.onMessageReceived(remoteMessage);
	}

	private void showMessage(Context context, String title, String msg, String ticker) {
		//비콘 신호 수신시 메시지 전송....
		NotificationManager mManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
		int notifyID=  (int) System.currentTimeMillis();
		mManager.cancel(notifyID);
		NotificationCompat.Builder mBuilder;

		NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
		style.setBigContentTitle(title)
				.bigText(msg);
		System.currentTimeMillis();

		mBuilder = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.papastamp_icon) //작은 아이콘 이미지
				.setContentTitle(title)
				.setContentText(msg)
				.setNumber(1)
				.setAutoCancel(true)//선택하면 사라진다.
				.setTicker(ticker)
				.setVibrate(new long[]{0, 2000})
				.setStyle(style);

		mManager.notify(notifyID, mBuilder.build());
	}
}
