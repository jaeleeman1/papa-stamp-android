package com.subin.papastamp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
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
    private boolean needSkip = false;
    private SharedPreferences.Editor saveEditor;
    private Button registrationEmail;
    private TextView signupText;
    private TextView findPassword;
    private String emailInputStr;
    private String passwordInputStr;
    private String passwordConfirmInputStr;
    private String mUid;
    private String fUid;
    private String faccesstoken;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context mContext;
    private UserManager userManager;
    private LocationManager locationManager;
    private ProgressDialog waitDialog;

    private final double INITIAL_LATITUDE = -181;
    private final double INITIAL_LONGITUDE = -181;
    private double mLatitude = INITIAL_LATITUDE;
    private double mLongitude = INITIAL_LONGITUDE;

    public LoginActivity() {
        mContext = this;
        mActivity = this;
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Get user id
        userManager = UserManager.getInstance();
        if (userManager.getContext() == null) {
            userManager.init(mContext);
        }
        mUid = userManager.getUid();
        Log.d(TAG, "access uid : " + mUid);

/*        fUid = userManager.getFid();
        Log.d(TAG, "firebase uid : " + fUid);*/

        //Get user location
        locationManager = LocationManager.getInstance();
        if (locationManager.getContext() == null) {
            locationManager.init(mContext);
        }

        mLatitude = locationManager.getLatitude();
        mLongitude = locationManager.getLongitude();
        Log.d(TAG, "Location Latitude : " + mLatitude);
        Log.d(TAG, "Location Longitude : " + mLongitude);

        //Send to server of user location
        sendLocationToServer(mLatitude, mLongitude);

        //Check user login info
        pref = mActivity.getSharedPreferences(Constants.PREFERENCE_USER, Context.MODE_PRIVATE);
        needSkip = pref.getBoolean(Constants.PREFERENCE_USER_SKIP, false);

       /* FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            fUid =  user.getUid();
            Log.d(TAG, "firebase : " + user.getUid());
        }*/
        /*String fUid = userManager.getFid();
        Log.d(TAG, "firebase uid : " + fUid);*/

//        Log.d(TAG, "firebase accesstoken : " + faccesstoken);

        emailInput = (EditText) findViewById(R.id.emailInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        passwordConfirmInput = (EditText) findViewById(R.id.passwordConfirmInput);
        registrationEmail = (Button) findViewById(R.id.regButton);
        signupText = (TextView) findViewById(R.id.signup_text);
        findPassword = (TextView) findViewById(R.id.find_password);

        if (needSkip) {
            Intent passIntent = new Intent(LoginActivity.this, MainActivity.class);
            passIntent.putExtra("pushCheck", "hide");
            passIntent.putExtra("shopCode", "");
            passIntent.putExtra("userId", mUid);
            passIntent.putExtra("mLatitude", ""+mLatitude);
            passIntent.putExtra("mLongitude",""+mLongitude);
            startActivity(passIntent);
            finish();
        }

        if(fUid != null) {
            registrationEmail.setBackgroundResource(R.drawable.login_button);
            passwordConfirmInput.setVisibility(View.GONE);
            signupText.setText("회원가입");
        }else {
            registrationEmail.setBackgroundResource(R.drawable.registration_button);
            passwordConfirmInput.setVisibility(View.VISIBLE);
            signupText.setText("로그인");
        }

        signupText.setOnClickListener(new View.OnClickListener() {
            public void  onClick(View v) {
                if(signupText.getText().equals("회원가입")) {
                    registrationEmail.setBackgroundResource(R.drawable.registration_button);
                    passwordConfirmInput.setVisibility(View.VISIBLE);
                    signupText.setText("로그인");
                }else {
                    registrationEmail.setBackgroundResource(R.drawable.login_button);
                    passwordConfirmInput.setVisibility(View.GONE);
                    signupText.setText("회원가입");
                }
            }
        });

        registrationEmail.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailInputStr = emailInput.getText().toString();
                passwordInputStr = passwordInput.getText().toString();

                if (signInUpCheck(emailInputStr, passwordInputStr)) {
                    if("회원가입".equals(signupText.getText())) {
                        userSignIn();
                    }else {
                        userSignUp();
                    }
                }
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
                String passwordConfirm = passwordConfirmInput.getText().toString();

                if (!password.equals(passwordConfirm)) {
                    passwordConfirmInput.setBackgroundResource(R.drawable.edit_wrong_bg);
                } else {
                    passwordConfirmInput.setBackgroundResource(R.drawable.edit_bg);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //password confirm check
        passwordConfirmInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String password = passwordInput.getText().toString();
                String passwordConfirm = passwordConfirmInput.getText().toString();

                if (!password.equals(passwordConfirm)) {
                    passwordConfirmInput.setBackgroundResource(R.drawable.edit_wrong_bg);
                } else {
                    passwordConfirmInput.setBackgroundResource(R.drawable.edit_bg);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private boolean signInUpCheck(String email, String password) {
        Log.i(TAG, "Login Check User Info");

        if ("".equals(emailInputStr)) {
            Toast.makeText(LoginActivity.this, "E-mail을 입력하세요", Toast.LENGTH_LONG).show();
            return false;
        } else {
            if (!isValidEmail(emailInputStr)) {
                Toast.makeText(LoginActivity.this, "올바른 E-mail을 입력하세요", Toast.LENGTH_LONG).show();
            } else {
                if ("".equals(passwordInputStr)) {
                    Toast.makeText(LoginActivity.this, "패스워드를 입력하세요", Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    if (!isValidPasswd(passwordInputStr)) {
                        Toast.makeText(LoginActivity.this, "패스워드는 6자 이상으로 설정하세요(한글 미포함)", Toast.LENGTH_LONG).show();
                    } else {
                        if(signupText.getText().equals("로그인")){
                            passwordConfirmInputStr = passwordConfirmInput.getText().toString();

                            if ("".equals(passwordConfirmInputStr)) {
                                Toast.makeText(LoginActivity.this, "패스워드를 한번 더 입력하세요", Toast.LENGTH_LONG).show();
                                return false;
                            } else {
                                if(passwordInputStr.equals(passwordConfirmInputStr)){
                                    return true;
                                }else {
                                    Toast.makeText(LoginActivity.this, "동일한 패스워드를 입력하세요", Toast.LENGTH_LONG).show();
                                    return false;
                                }
                            }
                        }else {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
                    Log.d(TAG, "REST API response OK");
                    String checkEmail = response.body().userEmailCheck;
                    String checkPassword = response.body().userPwCheck;

                    if("1".equals(checkEmail)) {
                        if("1".equals(checkPassword)) {
                            progressDialog.dismiss();
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

    private void userSignUp() {
        Log.i(TAG, "send Location to server");
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(LoginActivity.this);
        alert_confirm.setMessage("입력하신 내용으로 등록 하시겠습니까??").setCancelable(false).setPositiveButton("확인",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final ProgressDialog progressDialog = ProgressDialog.show(mActivity,
                            "Please wait...", "Progressing...", true);

                    Log.i(TAG, "Insert user info");

                    HttpClientManager httpClientManager = HttpClientManager.getInstance();
                    httpClientManager.initHeader();
                    HttpRequestUserInfo body = new HttpRequestUserInfo(faccesstoken, emailInputStr, passwordInputStr);
                    Call<ResponseBody> call = httpClientManager.insertUserInfo(body);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            //Log.d(TAG, "REST API request OK");
                            Log.d(TAG, "Response: " + response);
                            Log.d(TAG, "Response header: " + response.headers());

                            if (response.code() == 200) {
                                Log.d(TAG, "REST API (updating access token) response succeed");

                                //Create firebase user id
                                createFirebaseUserId();

                                //Check user login info
                                SharedPreferences.Editor edit = pref.edit();
                                edit.putBoolean(Constants.PREFERENCE_USER_SKIP, true);
                                edit.commit();


                                progressDialog.dismiss();

                                Intent papaMainIntent = new Intent(LoginActivity.this, MainActivity.class);
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
                            } else {
                                Log.e(TAG, "REST API (updating access token) response failed");
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "입력하신 E-Mail은 존재합니다.", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.d(TAG, "REST API request failed");
                            progressDialog.dismiss();
                        }
                    });
                }
            }).setNegativeButton("취소",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    return;
                }
            });
        AlertDialog alert = alert_confirm.create();
        alert.show();
    }

    private void loginFirebase() {
        Log.i(TAG, "Login firebase user id");

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
                        FirebaseUser user = mAuth.getCurrentUser();
                        Log.d(TAG, "firebase UID : " + user.getUid());

                        updateEmail(emailInputStr);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                        Toast.makeText(LoginActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    public void updateEmail(String updateEmail) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Log.d(TAG, "firebase uid : " + user.getUid());
        Log.d(TAG, "updateEmail: " + updateEmail);
        user.updateEmail(updateEmail)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "updateEmail: User email address updated.");
                    }
                }
            });
    }

    private void sendLocationToServer(Double latitude, Double longitude) {
        Log.i(TAG, "send Location to server");

        HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();
        HttpRequestLocationInfo body = new HttpRequestLocationInfo(latitude, longitude);
        Call<ResponseBody> call = httpClientManager.updateLocation(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                //Log.d(TAG, "REST API request OK");
                Log.d(TAG, "Response: " + response);
                Log.d(TAG, "Response header: " + response.headers());
                Log.d(TAG, "Response body: " + response.body());
                Log.d(TAG, "call: " + call);

                if (response.code() == 200) {
                    Log.d(TAG, "REST API response OK");
                } else {
                    Log.d(TAG, "REST API response failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(TAG, "REST API request failed");
            }
        });
    }



    /*private void anonymouslyAccount() {
        // Anonymously User
        mFirebaseAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInAnonymously:success");
                            FirebaseUser user = mFirebaseAuth.getCurrentUser();
                            String tempUser = user.getUid();
                            Log.d(TAG, "HHHHHHHHHHHHHHHHHHHHHHHHHH " + tempUser);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInAnonymously:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }*/

    /*private void createAccount(String email, String password) {
        //email.replace("-", "");
        email = "01026181715@papastamp.com";
        Log.i(TAG, "XXXXXXXXXXXXXXXXXXXXXX : " + email);
        Log.i(TAG, "XXXXXXXXXXXXXXXXXXXXXX : " + password);

        *//*if(!isValidEmail(email)){
            Log.e(TAG, "createAccount: email is not valid ");
            Toast.makeText(LoginActivity.this, "Email is not valid",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (isValidPasswd(password)){
            Log.e(TAG, "createAccount: password is not valid ");
            Toast.makeText(mContext, "Password is not valid",
                    Toast.LENGTH_SHORT).show();
            return;
        }*//*

//        showProgressDialog();

        // [START create_user_with_email]
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, "Authentication failed",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }*/

    /*private void createReAccount(String email, String password) {
        Log.i(TAG, "XXXXXXXXXXXXXXXXXXXXXX : " + email);
        Log.i(TAG, "XXXXXXXXXXXXXXXXXXXXXX : " + password);
        *//*if(!isValidEmail(email)){
            Log.e(TAG, "createAccount: email is not valid ");
            Toast.makeText(LoginActivity.this, "Email is not valid",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (isValidPasswd(password)){
            Log.e(TAG, "createAccount: password is not valid ");
            Toast.makeText(mContext, "Password is not valid",
                    Toast.LENGTH_SHORT).show();
            return;
        }*//*

//        showProgressDialog();
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

        if (credential == null) {
            Log.d(TAG, "GGGGGGGGGGGGGGGGGGGGGGGGGG ");
        } else {
            // [START create_user_with_email]
            mFirebaseAuth.getCurrentUser().linkWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                            if (task.isSuccessful()) {
                                Log.d(TAG, "linkWithCredential:success");
                                FirebaseUser user = task.getResult().getUser();
                                String tempUser = user.getUid();
                                Log.d(TAG, "KKKKKKKKKKKKKKKKKKKKKKKK " + tempUser);
                            } else {
                                Log.w(TAG, "linkWithCredential:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                            // [START_EXCLUDE]
//                        hideProgressDialog();
                            // [END_EXCLUDE]
                        }
                    });
            // [END create_user_with_email]
        }
    }*/

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