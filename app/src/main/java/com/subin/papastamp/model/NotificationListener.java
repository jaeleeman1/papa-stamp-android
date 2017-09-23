package com.subin.papastamp.model;

public interface NotificationListener {

	void onProximityNotificationReceived(String uid, Double latitude, Double longitude, Double distance,
										 String nickname, String expression);

	void onMessageNotificationReceived(String uid);
}
