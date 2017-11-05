package com.subin.papastamp.model.http;

public class HttpResponseLocationInfo {
	public String uid;
	public String latitude;
	public String longitude;

	@Override
	public String toString() {
		return "uid: " + uid + ", latitude: " + latitude + ", longitude: " + longitude;
	}

	public String getUid() {
		return uid;
	}

	public Double getLatitude() {
		return Double.parseDouble(latitude);
	}

	public Double getLongitude() {
		return Double.parseDouble(longitude);
	}
}
