package com.subin.papastamp.model.http;

import com.google.gson.annotations.SerializedName;

public class HttpRequestLocationInfo {
	@SerializedName("latitude")
	private final String mLatitude;

	@SerializedName("longitude")
	private final String mLongitude;

	public HttpRequestLocationInfo (Double latitude, Double longitude) {
		this.mLatitude = Double.toString(latitude);
		this.mLongitude = Double.toString(longitude);
	}
}
