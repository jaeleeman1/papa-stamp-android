package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestUserInfo {
	@SerializedName("access_token")
	private final String accessToken;

	@SerializedName("user_email")
	private final String userEmail;

	@SerializedName("user_password")
	private final String userPassword;

	@SerializedName("terms_check")
	private final String termsCheck;

	@SerializedName("latitude")
	private final Double latitude;

	@SerializedName("longitude")
	private final Double longitude;

	public HttpRequestUserInfo(String accessToken, String userEmail, String userPassword) {
		this.accessToken = accessToken;
		this.userEmail = userEmail;
		this.userPassword = userPassword;
		this.termsCheck = "";
		this.latitude = 0.0;
		this.longitude = 0.0;
	}

	public HttpRequestUserInfo(String accessToken, String userEmail, String userPassword, String termsCheck, Double latitude, Double longitude) {
		this.accessToken = accessToken;
		this.userEmail = userEmail;
		this.userPassword = userPassword;
		this.termsCheck = termsCheck;
		this.latitude = latitude;
		this.longitude = longitude;
	}
}
