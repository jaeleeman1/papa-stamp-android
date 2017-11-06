package com.subin.papastamp.model.http;

public class HttpResponseLoginInfo {
	public String userEmailCheck;
	public String userPwCheck;

	@Override
	public String toString() {
		return "userEmailCheck: " + userEmailCheck +
				"userPwCheck: " + userPwCheck;
	}

	public String getUserEmailCheck() {
		return userEmailCheck;
	}

	public String getUserPwCheck() {
		return userPwCheck;
	}
}
