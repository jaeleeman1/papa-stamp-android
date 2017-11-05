package com.subin.papastamp.model;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.subin.papastamp.Common.Algorithm;
import com.subin.papastamp.model.http.HttpClientManager;

import java.io.IOException;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static com.subin.papastamp.Common.Constants.CONFIG_KEY_AES_KEY;

public class UserInfo {
	private static UserInfo mInstance;
	private Context mContext;
	private static String mPid = null;
	private static final String TAG = "UserInfo";

	private UserInfo() {}

	public static UserInfo getInstance() {
		if (mInstance == null) {
			synchronized (UserInfo.class) {
				if (mInstance == null) {
					mInstance = new UserInfo();
				}
			}
		}
		return mInstance;
	}

	public void init(Context context) {
		mContext = context;
	}

	@Nullable
	private String getPid() {
		if (mPid == null) {
			if (isPhonePermissionGranted()) {
				TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(mContext.TELEPHONY_SERVICE);

				//TODO: Check national number
				String num = telephonyManager.getLine1Number();

				if (num != null) {
					if (num.startsWith("+82"))
						num = "0" + num.substring(3);

					num = num.replaceAll("-", "");

					if (num.startsWith("010")) {
						mPid = "082" + num;
					}

					Log.d(TAG, "PID: " + mPid);
				} else {
					// Change the virtual phone number for test
					mPid = "08201000000000";
					Log.d(TAG, "PID: " + mPid + " for test");
				}
			}
		}

		return mPid;
	}

	@Nullable
	public String getUid() {
		ConfigManager configManager = ConfigManager.getInstance();
		configManager.init(mContext);
		String aesKey = configManager.getProperty(CONFIG_KEY_AES_KEY);

		Algorithm algorithm = new Algorithm();
		String convertUid = algorithm.Encrypt(getPid(), aesKey);

		return convertUid;
	}

	private boolean isPhonePermissionGranted() {
		if (Build.VERSION.SDK_INT >= 23) {
			if (checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE)
					== PackageManager.PERMISSION_GRANTED) {
				Log.d(TAG, "Permission READ_PHONE_STATE is granted");
				return true;
			} else {
				Log.d(TAG, "Permission READ_PHONE_STATE is revoked");
				return false;
			}
		} else { // Permission is automatically granted on sdk<23 upon installation
			Log.v(TAG, "Permission READ_PHONE_STATE is granted");
			return true;
		}
	}
}