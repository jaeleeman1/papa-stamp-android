package com.subin.papastamp.model;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.subin.papastamp.common.Algorithm;

import java.util.concurrent.Executor;

import static android.support.v4.content.ContextCompat.checkSelfPermission;
import static com.subin.papastamp.common.Constants.CONFIG_KEY_AES_KEY;

public class UserManager {
	private static UserManager mInstance;
	private Context mContext;
	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener mAuthListener;
	private static String mPid = null;
	private String mUid;
	private String mFid;
	private String mEmail;
	private static final String TAG = "[UserManager] ";

	private UserManager() {}

	public static UserManager getInstance() {
		if (mInstance == null) {
			synchronized (UserManager.class) {
				if (mInstance == null) {
					mInstance = new UserManager();
				}
			}
		}
		return mInstance;
	}

	public Context getContext() {
		return mContext;
	}

	public void init(Context context) {
		mContext = context;
		mAuth = FirebaseAuth.getInstance();
	}

	public String getFid() {
		Log.d(TAG, "@@@@@@@@@@@ firebase start @@@@@@@@@@@@");
		mAuthListener = new FirebaseAuth.AuthStateListener() {
			@Override
			public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                if (!mFirst) {
				FirebaseUser user = firebaseAuth.getCurrentUser();
				String accessToken = FirebaseInstanceId.getInstance().getToken();
				if (user != null) {
					Log.d(TAG, "onAuthStateChanged(Firebase):" + user.getUid());
					Log.d(TAG, "token :" + accessToken);

					mFid = user.getUid();
				} else {
					Log.d(TAG, "onAuthStateChanged(initUserId)" + mUid);
					mFid = null;
				}
//                }
//                mFirst = false;
			}
		};
		Log.d(TAG, "@@@@@@@@@@@ firebase end @@@@@@@@@@@@");
		return mFid;
	}

	@Nullable
	private String getPid() {
		if (mPid == null) {
			if (isPhonePermissionGranted()) {
				TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);

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

		try {
			mUid = Algorithm.encrypt(getPid(), aesKey);
		} catch (Exception e) {
			Log.d(TAG, "Get Uid error");
			e.printStackTrace();
		}

		Log.d(TAG, "Uid : " + mUid);

		return mUid;
	}

	public void startListeningForAuthentication() {
		mAuth.addAuthStateListener(mAuthListener);
	}

	public void stopListeningForAuthentication() {
		if (mAuthListener != null) {
			mAuth.removeAuthStateListener(mAuthListener);
		}
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