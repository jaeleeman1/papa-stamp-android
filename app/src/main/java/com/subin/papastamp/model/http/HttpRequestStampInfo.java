package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestStampInfo {
	@SerializedName("shop_id")
	private final String shopId;

	@SerializedName("param_number")
	private final String paramNumber;

	public HttpRequestStampInfo (String shopId, String paramNumber) {
		this.shopId = shopId;
		this.paramNumber = paramNumber;
	}
}
