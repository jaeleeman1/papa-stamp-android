package com.subin.papastamp.model.http;

import android.util.Log;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HttpInterceptor implements Interceptor {
	private String mAccessUid = null;
	private static final String TAG = "HttpInterceptor";

	public HttpInterceptor(String accessUid) {
		this.mAccessUid = accessUid;
	}

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request originalRequest = chain.request();
		Log.d(TAG, "HTTP original: " + originalRequest);
		Request request = originalRequest.newBuilder()
				.addHeader("user_id", mAccessUid)
				.build();

		Response response = chain.proceed(request);
		return response;
	}
}
