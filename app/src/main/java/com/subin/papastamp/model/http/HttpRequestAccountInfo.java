package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestAccountInfo {
	@SerializedName("accountType")
	private final String mAccountType;

	@SerializedName("uid")
	private final String mUid;

	@SerializedName("email")
	private final String mEmail;

	@SerializedName("pid")
	private final String mPid;

	public HttpRequestAccountInfo (String accountType, String uid, String email, String pid) {
		this.mAccountType = accountType;
		this.mUid = uid;
		this.mEmail = email;
		this.mPid = pid;
	}
}
