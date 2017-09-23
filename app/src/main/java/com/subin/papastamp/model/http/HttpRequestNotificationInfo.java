package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestNotificationInfo {
	@SerializedName("latitude")
	private final String mLatitude;

	@SerializedName("longitude")
	private final String mLongitude;

	@SerializedName("distance")
	private final String mDistance;

	@SerializedName("nickname")
	private final String mNickname;

	@SerializedName("expression")
	private final String mExpression;

	@SerializedName(("type"))
	private final Integer mType;

	public HttpRequestNotificationInfo (Double latitude, Double longitude, Double distance,
										String nickname, String expression, Integer type) {
		this.mLatitude = Double.toString(latitude);
		this.mLongitude = Double.toString(longitude);
		this.mDistance = Double.toString(distance);
		this.mNickname = nickname;
		this.mExpression = expression;
		this.mType = type;
	}
}
