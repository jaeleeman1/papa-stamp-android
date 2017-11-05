package com.subin.papastamp.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.subin.papastamp.model.http.HttpClientManager;
import com.subin.papastamp.model.http.HttpResponseFirebaseToken;

import java.util.concurrent.Executor;
import com.subin.papastamp.model.ConfigManager;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountManager {
    private static AccountManager mInstance;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String mUid;
    private String mEmail;
    private Context mContext;
    private static final String TAG = "AccountManager";

    public AccountManager() {}

    public static AccountManager getInstance() {
        if (mInstance == null) {
            synchronized (AccountManager.class) {
                if (mInstance == null) {
                    mInstance = new AccountManager();
                }
            }
        }
        return mInstance;
    }

    public Context getContext() {
        return mContext;
    }

    public String getAccessUid() {
        UserInfo userInfo = UserInfo.getInstance();
        userInfo.init(mContext);
        String accessUid = userInfo.getUid();

        if (accessUid == null) {
            Log.d(TAG, "Not found access token");

            if (mContext == null)
                Log.e(TAG, "Context is null");
        }
        return accessUid;
    }

    public void init(Context context) {
        mContext = context;
        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                if (!mFirst) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged(Firebase):" + user.getUid());
                    mUid = user.getUid();
                } else {
                    Log.d(TAG, "onAuthStateChanged(initUserId)" + mUid);
                    mUid = null;
                }
//                }
//                mFirst = false;
            }
        };
    }



    public void startListeningForAuthentication() {
        mAuth.addAuthStateListener(mAuthListener);
    }

    public void stopListeningForAuthentication() {
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /*private void sendAccountToServer(FirebaseUser user) {
        mUid = user.getUid();
        mEmail = user.getEmail();
        Log.d(TAG, "Firebase uid: " + mUid + ", email: " + mEmail);

        user.getToken(false).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if (task.isSuccessful()) {
                    String idToken = task.getResult().getToken();
                    Log.d(TAG, "Firebase ID token: " + idToken + ", length: " + idToken.length());

                    HttpClientManager httpClientManager = HttpClientManager.getInstance();
                    httpClientManager.initHeader();

                    String mPid = getAccountPidToken();

                    // Call addAccount() REST API
                    HttpRequestAccountInfo body = new HttpRequestAccountInfo("ACCOUNT_TYPE_FIREBASE", mUid, mEmail, mPid);

                    Call<ResponseBody> call = httpClientManager.addAccount(body);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<okhttp3.ResponseBody> call, Response<ResponseBody> response) {
                            //Log.d(TAG, "REST API request OK");
                            Log.d(TAG, "Response: " + response);
                            Log.d(TAG, "Response header: " + response.headers());
                            Log.d(TAG, "Response body: " + response.body());
                            Log.d(TAG, "call: " + call);

                            if (response.code() == 200) {
                                Log.d(TAG, "REST API response OK");

                                if (mPresenter != null)
                                    mPresenter.gotoNextPage();
                            } else {
                                Log.d(TAG, "REST API response failed");
                            }
                        }

                        @Override
                        public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                            Log.d(TAG, "REST API request failed");
                        }
                    });
                } else {
                    // Handle error -> task.getException();
                    Log.e(TAG, "Failed to get ID token");
                }
            }
        });
    }*/
}