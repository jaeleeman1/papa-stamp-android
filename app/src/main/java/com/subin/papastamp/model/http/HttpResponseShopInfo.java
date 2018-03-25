package com.subin.papastamp.model.http;

public class HttpResponseShopInfo {
	public String shopId;
	public String shopCode;
	public String shopBeacon;
	public String shopCount;

	public String getShopId() {
		return shopId;
	}
	public String getShopCode() {
		return shopCode;
	}
	public String getShopBeacon() {
		return shopBeacon;
	}
	public String getShopCount() {
		return shopCount;
	}

	@Override
	public String toString() {
		return "shopId: " + shopId +
				", shopCode: " + shopCode +
				", shopBeacon: " + shopBeacon +
				", shopCount: " + shopCount;
	}
}