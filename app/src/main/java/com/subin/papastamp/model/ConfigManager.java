package com.subin.papastamp.model;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import junit.framework.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static com.subin.papastamp.common.Constants.CONFIG_KEY_API_URL;
import static com.subin.papastamp.common.Constants.CONFIG_KEY_AES_KEY;

public class ConfigManager {
	private static ConfigManager mInstance = null;
	private Context mContext = null;
	private static String mApiUrl = null;
	private static String mAesKey = null;
	private static final String TAG = "ConfigManager";

	public ConfigManager() {}

	public static ConfigManager getInstance() {
		if (mInstance == null) {
			synchronized (ConfigManager.class) {
				if (mInstance == null) {
					mInstance = new ConfigManager();
				}
			}
		}
		return mInstance;
	}

	public void init(@NonNull Context context) {
		Assert.assertNotNull("Context was null", context);
		synchronized (ConfigManager.class) {
			mContext = context;
		}
	}

	@Nullable
	public Context getContext() {
		return mContext;
	}

	@Nullable
	public String getProperty(@NonNull String key) {
		if (mApiUrl == null) {
			// Load all configuration
			Properties properties = new Properties();
			AssetManager assetManager = mContext.getAssets();
			try {
				InputStream inputStream = assetManager.open("config.properties");
				properties.load(inputStream);
			} catch (IOException e) {
				Log.e(TAG, "Failed to read config, error: " + e.getMessage());
				return null;
			}
			mApiUrl = properties.getProperty("api_url");
			mAesKey = properties.getProperty("aes_key");
		}

		//Log.d(TAG, "Configuration was cached");
		// Configuration was cached
		switch (key) {
			case CONFIG_KEY_API_URL:
				return mApiUrl;
			case CONFIG_KEY_AES_KEY:
				return mAesKey;
			default:
				//TODO: maybe throw exception here in the future.
				break;
		}

		Log.e(TAG, "Key was invalid");
		return null;
	}

}

