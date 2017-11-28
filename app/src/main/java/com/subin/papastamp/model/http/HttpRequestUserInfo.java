package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestUserInfo {
	@SerializedName("access_token")
	private final String accessToken;

	@SerializedName("user_email")
	private final String userEmail;

	@SerializedName("user_password")
	private final String userPassword;

	public HttpRequestUserInfo(String accessToken, String userEmail, String userPassword) {
		this.accessToken = accessToken;
		this.userEmail = userEmail;
		this.userPassword = userPassword;
	}
}
