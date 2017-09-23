package com.subin.papastamp.model.http;

public class HttpResponseFirebaseToken {
	public String customToken;

	@Override
	public String toString() {
		return "customToken: " + customToken;
	}

	public String getCustomToken() {
		return customToken;
	}
}