package com.subin.papastamp.model.http;

//import com.google.android.exoplayer2.C;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RestApiService {
	//public static final String BASE_URL = "http://52.78.164.163:7979/";

	@Headers({
			"Accept: application/json",
			"Content-Type: application/json"
	})

	@PUT("user/v1/updateLocation")
	Call<ResponseBody> updateLocation(@Body HttpRequestLocationInfo body);

	@POST("user/v1/userLogin")
	Call<HttpResponseLoginInfo> userLoginCheck(@Body HttpRequestLoginInfo body);

	@GET("user/v1/userCreate")
	Call<HttpResponseFirebaseToken> getAdminAuthToken();

	@POST("user/v1/userInfo")
	Call<ResponseBody> insertUserInfo(@Body HttpRequestUserInfo body);

	@POST("user/v1/userLogin")
	Call<ResponseBody> sendUserLoginToServer(@Body HttpRequestUserInfo body);

	@PUT("user/v1/accessToekn")
	Call<ResponseBody> insertAccessToken(@Path("access_token") String accessToken);

	@POST("notification/v1/request-stamp")
	Call<ResponseBody> requestStamp(@Body HttpRequestPushInfo body);

	@POST("notification/v1/request-coupon")
	Call<ResponseBody> usedCoupon(@Body HttpRequestPushInfo body);

	@GET("user/v1/shopCodeToShopId/{shop_code}")
	Call<HttpResponseShopInfo> selectShopCodeToShopId(@Path("shop_code") String shopCode);

	@GET("user/v1/beaconToShopId/{beacon_code}")
	Call<HttpResponseShopInfo> selectbeaconToShopId(@Path("beacon_code") String beaconCode);

	@GET("user/v1/numberCheck")
	Call<ResponseBody> getUserNumber(@Path("login_number") String userNumber);




	@PUT("tablet/v1.0/insertStampHistory")
	Call<ResponseBody> insertStampHistory(@Body HttpRequestPushInfo body);




	@POST("notification/v0.1/users/{uid}")
	Call<ResponseBody> sendNotification(@Path("uid") String uid,
										@Body HttpRequestNotificationInfo body);

	/*
	@PUT("account/v0.1/users/me/accesstoken")
	Call<ResponseBody> updateAccessToken();

	@Multipart
	@POST("profile/v0.1/users/")
	//Call<ResponseBody> addProfile(@Body HttpRequestProfileInfo body, @Part MultipartBody.Part file);
	Call<ResponseBody> addProfile(@Part("nickname") RequestBody nickname, //Can also use @Field
								  @Part MultipartBody.Part thumbnail,
								  @Part("expression") RequestBody expression,
								  @Part("city") RequestBody city,
								  @Part("gender") RequestBody gender);

	@PUT("profile/v0.1/users/{uid}")
	Call<ResponseBody> updateProfileInfo(@Path("uid") String uid,
										 @Body HttpRequestProfileInfo body);

	@Multipart
	@PUT("profile/v0.1/thumbnails/{uid}")
	Call<ResponseBody> updateProfileThumbnail(@Path("uid") String uid,
											  @Part MultipartBody.Part thumbnail);

	@GET("profile/v0.1/users/{uid}")
	Call<List<HttpResponseProfileInfo>> getProfileInfo(@Path("uid") String uid);

	@GET("profile/v0.1/thumbnails/{uid}")
	Call<ResponseBody> getProfileThumbnail(@Path("uid") String uid);

	@PUT("profile/v0.1/users/me/status")
	Call<ResponseBody> updateUserStatus(@Body HttpRequestStatusInfo body);

	@PUT("location/v0.1/users/{uid}")
	Call<ResponseBody> updateLocation(@Path("uid") String uid,
									  @Body HttpRequestLocationInfo body);

	@GET("location/v0.1/friends/")
	Call<List<HttpResponseLocationInfo>> getFriendsNearby(@Query("q") String location,
														  @Query("limit") String limit);

	@POST("notification/v0.1/users/{uid}")
	Call<ResponseBody> sendNotification(@Path("uid") String uid,
										@Body HttpRequestNotificationInfo body);

	@POST("contact/v0.1/users/{uid}")
	Call<ResponseBody> addContactList(@Path("uid") String uid,
									  @Body HttpRequestContactInfo body);

	@GET("contact/v0.1/candidatefriends")
	Call<List<HttpResponseContactInfo>> getCandidateFriends();

	@GET("contact/v0.1/friends")
	Call<List<HttpResponseContactInfo>> getFriends();

	@POST("contact/v0.1/friends/{uid}")
	Call<ResponseBody> addFriend(@Path("uid") String uid);

	@POST("contact/v0.1/users/me/friends")
	Call<ResponseBody> addFriendsList(@Body HttpRequestContactInfo body);*/
}
