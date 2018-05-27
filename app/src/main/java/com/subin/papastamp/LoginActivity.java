package com.subin.papastamp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.subin.papastamp.common.Constants;
import com.subin.papastamp.model.LocationManager;

import com.subin.papastamp.model.UserManager;
import com.subin.papastamp.model.http.HttpClientManager;
import com.subin.papastamp.model.http.HttpRequestLocationInfo;
import com.subin.papastamp.model.http.HttpRequestLoginInfo;
import com.subin.papastamp.model.http.HttpRequestUserInfo;
import com.subin.papastamp.model.http.HttpResponseFirebaseToken;
import com.subin.papastamp.model.http.HttpResponseLoginInfo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "[Login Activity] : ";
    private LoginActivity mActivity;
    private EditText emailInput, passwordInput, passwordConfirmInput;
    private SharedPreferences pref;
    private boolean needSkip;
    private Button registrationEmail;
    private TextView signupText;
    private TextView findPassword;
    private String emailInputStr;
    private String passwordInputStr;
    private String mUid;
    private String fUid;
    private FirebaseAuth mAuth;
    private Context mContext;
    private UserManager userManager;
    private LocationManager locationManager;

    private final double INITIAL_LATITUDE = -181;
    private final double INITIAL_LONGITUDE = -181;
    private double mLatitude = INITIAL_LATITUDE;
    private double mLongitude = INITIAL_LONGITUDE;

    public LoginActivity() {
        mContext = this;
        mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailInput = (EditText) findViewById(R.id.emailInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        passwordConfirmInput = (EditText) findViewById(R.id.passwordConfirmInput);
        registrationEmail = (Button) findViewById(R.id.regButton);
        signupText = (TextView) findViewById(R.id.signup_text);
//        findPassword = (TextView) findViewById(R.id.find_password);

        //Init userManager
        userManager = UserManager.getInstance();
        if (userManager.getContext() == null) {
            userManager.init(mContext);
        }

        //Init locationManager
        locationManager = LocationManager.getInstance();
        if (locationManager.getContext() == null) {
            locationManager.init(mContext);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mUid = userManager.getUid();
        Log.d(TAG, "Get user id : " + mUid);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fUid =  currentUser.getUid();
            Log.d(TAG, "firebase id : " + fUid);
        }

        mLatitude = locationManager.getLatitude();
        mLongitude = locationManager.getLongitude();
        Log.d(TAG, "Get location latitude : " + mLatitude);
        Log.d(TAG, "Get location longitude : " + mLongitude);

        //Send to server of user location
        sendLocationToServer(mLatitude, mLongitude);

        //Check user login info
        pref = mActivity.getSharedPreferences(Constants.PREFERENCE_USER, Context.MODE_PRIVATE);
        needSkip = pref.getBoolean(Constants.PREFERENCE_USER_SKIP, false);

        if ((mUid.equals(fUid)) && needSkip) {
            String registrationToken = FirebaseInstanceId.getInstance().getToken();
            sendAccessTokenToServer(registrationToken);
            Intent passIntent = new Intent(LoginActivity.this, MainActivity.class);
            passIntent.putExtra("pushCheck", "hide");
            passIntent.putExtra("shopCode", "");
            passIntent.putExtra("shopBeacon", "");
            passIntent.putExtra("userId", mUid);
            passIntent.putExtra("mLatitude", ""+mLatitude);
            passIntent.putExtra("mLongitude",""+mLongitude);
            startActivity(passIntent);
            finish();
        }

        registrationEmail.setBackgroundResource(R.drawable.login_button);

        signupText.setText("회원가입");
        signupText.setOnClickListener(new View.OnClickListener() {
            public void  onClick(View v) {
                Intent termsItntent = new Intent(LoginActivity.this, TermsActivity.class);
                startActivity(termsItntent);
                finish();
            }
        });

        registrationEmail.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailInputStr = emailInput.getText().toString();
                passwordInputStr = passwordInput.getText().toString();

                userSignIn();
            }
        });

        //input phone number
        emailInput.addTextChangedListener(new TextWatcher() {
            int prevL = 0;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prevL = emailInput.getText().toString().length();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = emailInput.getText().toString();

                if (isValidEmail(email)) {
                    emailInput.setBackgroundResource(R.drawable.edit_bg);
                } else {
                    emailInput.setBackgroundResource(R.drawable.edit_wrong_bg);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                int length = s.length();
                /*if (prevL == 0) {
                    s.append("10-");
                }
                if ((prevL < length) && (length == 3 || length == 8)) {
                    s.append("-");
                }
                if (length > 13) {
                    String data = phoneInput.getText().toString();
                    phoneInput.setText(data.substring(0, 13));
                }*/
            }
        });

        //password check
        passwordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = passwordInput.getText().toString();

                if (isValidPasswd(password)) {
                    passwordInput.setBackgroundResource(R.drawable.edit_bg);
                } else {
                    passwordInput.setBackgroundResource(R.drawable.edit_wrong_bg);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void userSignIn() {
        Log.i(TAG, "Send check the user info");
        final ProgressDialog progressDialog = ProgressDialog.show(mActivity,
                "Please wait...", "Progressing...", true);
        HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();
        HttpRequestLoginInfo body = new HttpRequestLoginInfo(emailInputStr, passwordInputStr);
        Call<HttpResponseLoginInfo> call = httpClientManager.userLoginCheck(body);
        call.enqueue(new Callback<HttpResponseLoginInfo>() {
            @Override
            public void onResponse(Call<HttpResponseLoginInfo> call, Response<HttpResponseLoginInfo> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response: " + response);
                Log.d(TAG, "Response header: " + response.headers());
                Log.d(TAG, "Response body: " + response.body());
                Log.d(TAG, "call: " + call);

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK , User sign in");
                    String checkEmail = response.body().userEmailCheck;
                    String checkPassword = response.body().userPwCheck;

                    if("1".equals(checkEmail)) {
                        if("1".equals(checkPassword)) {
                            progressDialog.dismiss();
                            //Create firebase user id
                            createFirebaseUserId();

                            SharedPreferences.Editor edit = pref.edit();
                            edit.putBoolean(Constants.PREFERENCE_USER_SKIP, true);
                            edit.commit();

                            Intent papaMainIntent = new Intent (LoginActivity.this, MainActivity.class);
                            papaMainIntent.putExtra("pushCheck", "hide");
                            papaMainIntent.putExtra("shopId", "");
                            papaMainIntent.putExtra("shopBeacon", "");
                            papaMainIntent.putExtra("userId", mUid);
                            papaMainIntent.putExtra("mLatitude", ""+mLatitude);
                            papaMainIntent.putExtra("mLongitude",""+mLongitude);
                            Log.i(TAG, "user id : " + mUid);
                            Log.i(TAG, "latitude : " + mLatitude);
                            Log.i(TAG, "longitude : " + mLongitude);
                            startActivity(papaMainIntent);
                            finish();

                            Toast.makeText(LoginActivity.this, "Papa Stamp에 오신걸 환영합니다.", Toast.LENGTH_LONG).show();
                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "패스워드를 확인 바랍니다.", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "E-mail을 확인 바랍니다.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "REST API response failed");
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "E-mail 및 Password를 확인 바랍니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<HttpResponseLoginInfo> call, Throwable t) {
                Log.d(TAG, "REST API request failed");
                progressDialog.dismiss();
            }
        });
    }

    private void createFirebaseUserId() {
        Log.i(TAG, "Create firebase user id");

        HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();
        Call<HttpResponseFirebaseToken> call = httpClientManager.getAdminAuthToken();
        call.enqueue(new Callback<HttpResponseFirebaseToken>() {
            @Override
            public void onResponse(Call<HttpResponseFirebaseToken> call, Response<HttpResponseFirebaseToken> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response: " + response);
                Log.d(TAG, "Response header: " + response.headers());

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK");
                    if (response.body() != null) {
                        Log.d(TAG, "Response body: " + response.body().customToken);
                        String customToken = response.body().customToken;
                        customAccount(customToken);
                        sendUserLoginToServer(customToken, emailInputStr, passwordInputStr);
                    } else {
                        Log.d(TAG, "REST API response failed");
                    }
                }
            }

            @Override
            public void onFailure(Call<HttpResponseFirebaseToken> call, Throwable t) {
                Log.d(TAG, "REST API request failed");
            }
        });
    }

    private void customAccount(String mCustomToken) {
        // Anonymously User
        mAuth.signInWithCustomToken(mCustomToken)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCustomToken:success");
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void sendLocationToServer(Double latitude, Double longitude) {
        Log.i(TAG, "Send Location to server");

        HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();
        HttpRequestLocationInfo body = new HttpRequestLocationInfo(latitude, longitude);
        Call<ResponseBody> call = httpClientManager.updateLocation(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response(sendLocationToServer): " + response);
                Log.d(TAG, "Response header(sendLocationToServer): " + response.headers());
                Log.d(TAG, "Response body(sendLocationToServer): " + response.body());
                Log.d(TAG, "call(sendLocationToServer): " + call);

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK(sendLocationToServer)");
                } else {
                    Log.d(TAG, "REST API response failed(sendLocationToServer)");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "REST API request failed(sendLocationToServer)");
            }
        });
    }

    private void sendUserLoginToServer(String accessToken, String userEmail, String userPassword) {
        Log.i(TAG, "Send user info to server");
        HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();
        HttpRequestUserInfo body = new HttpRequestUserInfo(accessToken, userEmail, userPassword);
        Call<ResponseBody> call = httpClientManager.sendUserLoginToServer(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response(sendUserInfoToServer): " + response);
                Log.d(TAG, "Response header(sendUserInfoToServer): " + response.headers());
                Log.d(TAG, "Response body(sendUserInfoToServer): " + response.body());
                Log.d(TAG, "call(sendUserInfoToServer): " + call);

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK(sendUserInfoToServer)");
                } else {
                    Log.d(TAG, "REST API response failed(sendUserInfoToServer)");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "REST API request failed(sendUserInfoToServer)");
            }
        });
    }

    private void sendAccessTokenToServer(String accessToken) {
        Log.i(TAG, "Send user info to server");
        HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();
        Call<ResponseBody> call = httpClientManager.insertAccessToken(accessToken);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response(sendUserInfoToServer): " + response);
                Log.d(TAG, "Response header(sendUserInfoToServer): " + response.headers());
                Log.d(TAG, "Response body(sendUserInfoToServer): " + response.body());
                Log.d(TAG, "call(sendUserInfoToServer): " + call);

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK(sendUserInfoToServer)");
                } else {
                    Log.d(TAG, "REST API response failed(sendUserInfoToServer)");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "REST API request failed(sendUserInfoToServer)");
            }
        });
    }

    private boolean isValidEmail(String target) {
        if (target == null || TextUtils.isEmpty(target)) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
        }
    }

    private boolean isValidPasswd(String target) {
        Pattern p = Pattern.compile("(^.*(?=.{6,100})(?=.*[0-9])(?=.*[a-zA-Z]).*$)");

        Matcher m = p.matcher(target);
        return m.find() && !target.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
    }
}