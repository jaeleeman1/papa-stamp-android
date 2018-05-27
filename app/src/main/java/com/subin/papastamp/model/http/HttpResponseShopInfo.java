package com.subin.papastamp.model.http;

public class HttpResponseShopInfo {
	public String shopId;
	public String shopCode;
	public String shopBeacon;

	public String getShopId() {
		return shopId;
	}
	public String getShopCode() {
		return shopCode;
	}
	public String getShopBeacon() {
		return shopBeacon;
	}

	@Override
	public String toString() {
		return "shopId: " + shopId +
				", shopCode: " + shopCode +
				", shopBeacon: " + shopBeacon;
	}
}