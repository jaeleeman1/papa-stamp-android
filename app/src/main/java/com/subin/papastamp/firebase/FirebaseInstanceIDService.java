
package com.subin.papastamp.firebase;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/*import com.nbnl.emobility.model.AccountManager;
import com.nbnl.emobility.model.ConfigManager;

import static com.nbnl.emobility.model.AccountManager.PREFERENCES_KEY_LOGIN_STATE;
import static com.nbnl.emobility.model.AccountManager.PREFERENCES_LOGIN_STATE;*/


public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
	private static final String TAG = "Papa FirebaseInstance";

	/**
	 * Called if InstanceID token is updated. This may occur if the security of
	 * the previous token had been compromised. Note that this is called when the InstanceID token
	 * is initially generated so this is where you would retrieve the token.
	 */
// [START refresh_token]
@Override
public void onTokenRefresh() {
		// Get updated InstanceID token.
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.d(TAG, "Refreshed access token: " + refreshedToken);

		// If you want to send messages to this application instance or
		// manage this apps subscriptions on the server side, send the
		// Instance ID token to your app server.
		sendRegistrationToServer(refreshedToken);
		}
// [END refresh_token]

/**
 * Persist token to third-party servers.
 *
 * Modify this method to associate the user's FCM InstanceID token with any server-side account
 * maintained by your application.
 *
 * @param token The new token.
 */
private void sendRegistrationToServer(String token) {
		// Implement this method to send token to your app server.

		// Do not use AccountManager.getLoginState()
		/*SharedPreferences pref = getSharedPreferences(PREFERENCES_LOGIN_STATE, MODE_PRIVATE);
		AccountManager.LoginState loginState = AccountManager.LoginState.toEnum(
		pref.getString(PREFERENCES_KEY_LOGIN_STATE, ""));

		if (loginState == AccountManager.LoginState.LOGIN_STATE_LOGGED_IN) {
			Log.d(TAG, "Login state: LOGGED_IN");

			ConfigManager configManager = ConfigManager.getInstance();
			if (configManager.getContext() == null)
				configManager.init(this);

			AccountManager accountManager = AccountManager.getInstance();
			if (accountManager.getContext() == null)
				accountManager.init(this, null);

			accountManager.updateAccessToken(token);
		}*/
	}
}