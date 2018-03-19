package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestStampInfo {
	@SerializedName("shop_id")
	private final String shopId;

	@SerializedName("coupon_number")
	private final String couponNumber;

	public HttpRequestStampInfo (String shopId) {
		this.shopId = shopId;
		this.couponNumber = "";
	}

	public HttpRequestStampInfo (String shopId, String couponNumber) {
		this.shopId = shopId;
		this.couponNumber = couponNumber;
	}
}
