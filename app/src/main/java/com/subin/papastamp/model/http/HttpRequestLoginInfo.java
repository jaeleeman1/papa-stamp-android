package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestLoginInfo {
	@SerializedName("user_email")
	private final String userEmail;

	@SerializedName("user_password")
	private final String userPassword;

	public HttpRequestLoginInfo (String userEmail, String userPassword) {
		this.userEmail = userEmail;
		this.userPassword = userPassword;
	}
}
