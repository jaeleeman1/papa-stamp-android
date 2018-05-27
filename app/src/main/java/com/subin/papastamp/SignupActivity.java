package com.subin.papastamp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.subin.papastamp.common.Algorithm;
import com.subin.papastamp.common.Constants;
import com.subin.papastamp.model.ConfigManager;
import com.subin.papastamp.model.LocationManager;
import com.subin.papastamp.model.UserManager;
import com.subin.papastamp.model.http.HttpClientManager;
import com.subin.papastamp.model.http.HttpRequestLocationInfo;
import com.subin.papastamp.model.http.HttpRequestLoginInfo;
import com.subin.papastamp.model.http.HttpRequestUserInfo;
import com.subin.papastamp.model.http.HttpResponseFirebaseToken;
import com.subin.papastamp.model.http.HttpResponseLoginInfo;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.subin.papastamp.common.Constants.CONFIG_KEY_AES_KEY;

public class SignupActivity extends AppCompatActivity {
    private final String TAG = "[Signup Activity] : ";
    private SignupActivity mActivity;
    private EditText emailInput, passwordInput, passwordConfirmInput, phoneNumberInput, authNumberInput;
    private SharedPreferences pref;
    private SharedPreferences auth;
    private boolean needSkip;
    private String authCode;
    private Button authBtn, registrationBtn;
    private String emailInputStr;
    private String passwordInputStr;
    private String passwordConfirmInputStr;
    private String phoneNumber;
    private String termsCheck;
    private String resultCheck = "";
    private String mUid;
    private String fUid;
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

    public SignupActivity() {
        mContext = this;
        mActivity = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();

        emailInput = (EditText) findViewById(R.id.emailInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        passwordConfirmInput = (EditText) findViewById(R.id.passwordConfirmInput);
        phoneNumberInput = (EditText) findViewById(R.id.phoneNumberInput);
        authNumberInput = (EditText) findViewById(R.id.authNumberInput);
        authBtn = (Button) findViewById(R.id.authBtn);
        registrationBtn = (Button) findViewById(R.id.regButton);

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

        Intent termsIntent = getIntent();

        termsCheck = termsIntent.getExtras().getString("termsCheck");
        Log.d(TAG, "pushCheck: " + termsCheck);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mUid = userManager.getUid();
        Log.d(TAG, "Get user id : " + mUid);

        mLatitude = locationManager.getLatitude();
        mLongitude = locationManager.getLongitude();
        Log.d(TAG, "Get location latitude : " + mLatitude);
        Log.d(TAG, "Get location longitude : " + mLongitude);

        pref = mActivity.getSharedPreferences(Constants.PREFERENCE_USER, Context.MODE_PRIVATE);
        needSkip = pref.getBoolean(Constants.PREFERENCE_USER_SKIP, false);
        auth = mActivity.getSharedPreferences(Constants.PREFERENCE_AUTH, Context.MODE_PRIVATE);

        authCreate();

        //Send to server of user location
//        sendLocationToServer(mLatitude, mLongitude);

/*        if ((mUid.equals(fUid)) && needSkip) {
            String registrationToken = FirebaseInstanceId.getInstance().getToken();
            sendAccessTokenToServer(registrationToken);
            Intent passIntent = new Intent(SignupActivity.this, MainActivity.class);
            passIntent.putExtra("pushCheck", "hide");
            passIntent.putExtra("shopCode", "");
            passIntent.putExtra("shopBeacon", "");
            passIntent.putExtra("userId", mUid);
            passIntent.putExtra("mLatitude", ""+mLatitude);
            passIntent.putExtra("mLongitude",""+mLongitude);
            startActivity(passIntent);
            finish();
        }*/


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

        //password confirm check
        phoneNumberInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                phoneNumber = phoneNumberInput.getText().toString();
                String regEx = "(\\d{3})(\\d{4})(\\d{4})";
                if(Pattern.matches(regEx, phoneNumber))
                    phoneNumberInput.setText(phoneNumber.replaceAll(regEx, "$1-$2-$3"));
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        authBtn.setOnClickListener(new View.OnClickListener() {
            public void  onClick(View v) {
                phoneNumber = phoneNumberInput.getText().toString();

                if(phoneNumber.length() == 0) {
                    Toast.makeText(SignupActivity.this, "핸드폰 번호를 입력하세요!!", Toast.LENGTH_LONG).show();
                }

                if(phoneNumber.length() < 13) {
                    Toast.makeText(SignupActivity.this, "올바른 핸드폰 번호를 입력하세요!!", Toast.LENGTH_LONG).show();
                }else {

//                    if(phoneNumberCheck(phoneNumber)) {
                        HttpClientManager httpClientManager = HttpClientManager.getInstance();
                        httpClientManager.initHeader();
                        Call<ResponseBody> call = httpClientManager.getUserNumber(phoneNumber);
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                //Log.d(TAG, "REST API request OK");
                                Log.d(TAG, "Response: " + response);
                                Log.d(TAG, "Response header: " + response.headers());

                                if (response.code() == 200) {
                                    Log.d(TAG, "REST API response OK");
                                    if (response.body() != null) {

                                    } else {
                                        Log.d(TAG, "REST API response failed");
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                Log.d(TAG, "REST API request failed");
                            }
                        });
                    /*}else {
                        Toast.makeText(SignupActivity.this, "올바른 핸드폰 번호를 입력하세요!!", Toast.LENGTH_LONG).show();
                    }*/
                }
            }
        });


        registrationBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailInputStr = emailInput.getText().toString();
                passwordInputStr = passwordInput.getText().toString();

                if (signInUpCheck(emailInputStr, passwordInputStr)) {
                    userSignUp();
                }
            }
        });
    }

    private boolean phoneNumberCheck(String number) {
        ConfigManager configManager = ConfigManager.getInstance();
        configManager.init(mContext);
        String aesKey = configManager.getProperty(CONFIG_KEY_AES_KEY);

        Algorithm algorithm = new Algorithm();

        number = number.replaceAll("-", "");
        number = "082" + number;

        String checkUid = "";
        try {
            checkUid = algorithm.encrypt(number, aesKey);
            Log.d(TAG, "Check user id : " + checkUid);
        } catch (Exception e) {
            Log.d(TAG, "Check user id error");
            e.printStackTrace();
        }

        if(mUid.equals(checkUid)){
            return true;
        }else {
            return false;
        }
    }

    private boolean signInUpCheck(String email, String password) {
        Log.i(TAG, "Login Check User Info");

        if ("".equals(emailInputStr)) {
            Toast.makeText(SignupActivity.this, "E-mail을 입력하세요", Toast.LENGTH_LONG).show();
            return false;
        } else {
            if (!isValidEmail(emailInputStr)) {
                Toast.makeText(SignupActivity.this, "올바른 E-mail을 입력하세요", Toast.LENGTH_LONG).show();
            } else {
                if ("".equals(passwordInputStr)) {
                    Toast.makeText(SignupActivity.this, "패스워드를 입력하세요", Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    if (!isValidPasswd(passwordInputStr)) {
                        Toast.makeText(SignupActivity.this, "패스워드는 6자 이상으로 설정하세요(한글 미포함)", Toast.LENGTH_LONG).show();
                    } else {
                        passwordConfirmInputStr = passwordConfirmInput.getText().toString();

                        if ("".equals(passwordConfirmInputStr)) {
                            Toast.makeText(SignupActivity.this, "패스워드를 한번 더 입력하세요", Toast.LENGTH_LONG).show();
                            return false;
                        } else {
                            if(passwordInputStr.equals(passwordConfirmInputStr)){
                                //Check auth code
                                authCode = auth.getString(Constants.PREFERENCE_AUTH_CODE, "");
                                if("".equals(authNumberInput)) {
                                    Toast.makeText(SignupActivity.this, "인증번호를 입력하세요", Toast.LENGTH_LONG).show();
                                    return false;
                                }else {
                                    if(authCode.equals(authNumberInput)) {
                                        return true;
                                    } else {
                                        Toast.makeText(SignupActivity.this, "올바른 인증번호를 입력하세요", Toast.LENGTH_LONG).show();
                                        return false;
                                    }
                                }
                            }else {
                                Toast.makeText(SignupActivity.this, "동일한 패스워드를 입력하세요", Toast.LENGTH_LONG).show();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private void authCreate() {
        Random random = new Random();
        String code = String.format("%04d", random.nextInt(10000));
        Log.i(TAG, "AUTH CODE : " + code);
        SharedPreferences.Editor authEdit = auth.edit();
        authEdit.putString(Constants.PREFERENCE_AUTH_CODE, code);
        authEdit.commit();
    }

    /*private void userSignIn() {
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

                            Intent papaMainIntent = new Intent (SignupActivity.this, MainActivity.class);
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

                            Toast.makeText(SignupActivity.this, "Papa Stamp에 오신걸 환영합니다.", Toast.LENGTH_LONG).show();
                        }else {
                            progressDialog.dismiss();
                            Toast.makeText(SignupActivity.this, "패스워드를 확인 바랍니다.", Toast.LENGTH_LONG).show();
                        }
                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(SignupActivity.this, "E-mail을 확인 바랍니다.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.d(TAG, "REST API response failed");
                    progressDialog.dismiss();
                    Toast.makeText(SignupActivity.this, "E-mail 및 Password를 확인 바랍니다.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<HttpResponseLoginInfo> call, Throwable t) {
                Log.d(TAG, "REST API request failed");
                progressDialog.dismiss();
            }
        });
    }*/

    private void userSignUp() {
        Log.i(TAG, "send Location to server");
        AlertDialog.Builder alert_confirm = new AlertDialog.Builder(SignupActivity.this);
        alert_confirm.setMessage("입력하신 내용으로 등록 하시겠습니까??").setCancelable(false).setPositiveButton("확인",
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final ProgressDialog progressDialog = ProgressDialog.show(mActivity,
                            "Please wait...", "Progressing...", true);

                    Log.i(TAG, "Insert user info");

                    //Create firebase user id
                    createFirebaseUserId();

                    //Check user login info
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putBoolean(Constants.PREFERENCE_USER_SKIP, true);
                    edit.commit();

                    progressDialog.dismiss();

                    Intent papaMainIntent = new Intent(SignupActivity.this, MainActivity.class);
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

                    Toast.makeText(SignupActivity.this, "Papa Stamp에 오신걸 환영합니다.", Toast.LENGTH_LONG).show();
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
                        sendUserInfoToServer(customToken, emailInputStr, passwordInputStr, termsCheck, mLatitude, mLongitude);
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
                        Toast.makeText(SignupActivity.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    /*private void sendLocationToServer(Double latitude, Double longitude) {
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
    }*/

    private void sendUserInfoToServer(String accessToken, String userEmail, String userPassword, String terms, Double lat, Double lng) {
        Log.i(TAG, "Send user info to server");
        HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();
        HttpRequestUserInfo body = new HttpRequestUserInfo(accessToken, userEmail, userPassword, terms, lat, lng);
        Call<ResponseBody> call = httpClientManager.insertUserInfo(body);
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

/*    private void sendAccessTokenToServer(String accessToken) {
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