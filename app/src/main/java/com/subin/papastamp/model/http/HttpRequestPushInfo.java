package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestPushInfo {
	@SerializedName("shop_id")
	private final String shopId;

	@SerializedName("coupon_number")
	private final String couponNumber;

	@SerializedName("request_check")
	private final String requestCheck;

	public HttpRequestPushInfo(String shopId) {
		this.shopId = shopId;
		this.couponNumber = "";
		this.requestCheck = "true";
	}

	public HttpRequestPushInfo(String shopId, String couponNumber) {
		this.shopId = shopId;
		this.couponNumber = couponNumber;
		this.requestCheck = "true";
	}
}