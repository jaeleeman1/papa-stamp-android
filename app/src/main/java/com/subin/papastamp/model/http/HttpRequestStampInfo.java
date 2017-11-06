package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestStampInfo {
	@SerializedName("shop_id")
	private final String shopId;

	public HttpRequestStampInfo(String shopId) {
		this.shopId = shopId;
	}
}
