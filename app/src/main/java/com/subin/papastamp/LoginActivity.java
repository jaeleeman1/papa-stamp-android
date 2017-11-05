package com.subin.papastamp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.subin.papastamp.model.AccountManager;
import com.subin.papastamp.model.LocationManager;
import com.subin.papastamp.model.UserInfo;

import com.subin.papastamp.model.ConfigManager;
import com.subin.papastamp.model.http.HttpClientManager;
import com.subin.papastamp.model.http.HttpRequestLocationInfo;
import com.subin.papastamp.model.http.HttpResponseFirebaseToken;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    static final String TAG = "LoginActivity";
    private LoginActivity mActivity;
    private EditText emailInput, passwordInput, passwordConfirmInput;
    private TextView loginSkip;
    private Boolean loginChecked;
    private SharedPreferences pref;
    private SharedPreferences.Editor saveEditor;
    private Button registrationEmail;
    private String mUid;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private Context mContext;
    private AccountManager accountManager;
    private LocationManager locationManager;

    private final double INITIAL_LATITUDE = -181;
    private final double INITIAL_LONGITUDE = -181;
    private double mLatitude = INITIAL_LATITUDE;
    private double mLongitude = INITIAL_LONGITUDE;

    public final int PERMISSION_TYPE_READ_PHONE_NUMBER = 0;
    public final int PERMISSION_TYPE_ACCESS_LOCATION = 1;

    public LoginActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_papastamp);

        mContext = this;
        mActivity = this;
        mAuth = FirebaseAuth.getInstance();

        emailInput = (EditText) findViewById(R.id.emailInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        passwordConfirmInput = (EditText) findViewById(R.id.passwordConfirmInput);
        loginSkip = (TextView) findViewById(R.id.skipBox);
        registrationEmail = (Button) findViewById(R.id.regButton);

        pref = getSharedPreferences("userLogin", AppCompatActivity.MODE_PRIVATE);
        saveEditor = pref.edit();

        checkPermission();

        accountManager = AccountManager.getInstance();
        if (accountManager.getContext() == null) {
            accountManager.init(mContext);
        }

        locationManager = LocationManager.getInstance();
        if (locationManager.getContext() == null) {
            locationManager.init(mContext);
        }

        mLatitude = locationManager.getLatitude();
        mLongitude = locationManager.getLongitude();
        Log.d(TAG, "Location Latitude : " + mLatitude);
        Log.d(TAG, "Location Longitude : " + mLongitude);

        emailInput.setText(""+ (mLatitude + " : " + mLongitude));

        mUid = accountManager.getAccessUid();
        Log.d(TAG, "access uid : " + mUid);

        sendLocationToServer(mUid, mLatitude, mLongitude);

        String checkUid = pref.getString("firebaseUid", "");
        Log.d(TAG, "get Uid : " + checkUid);

        if (checkUid == "") {
            Log.d(TAG, "Check Uid : Firebase init uid ");
            insertFirebaseUserId();
            saveEditor.putString("firebaseUid", mUid);
            saveEditor.commit();
        }
/*
        if (pref.getBoolean("emailpasswordCheck", false)) {
            Intent papaMainIntent = new Intent (LoginActivity.this, MainActivity.class);
            papaMainIntent.putExtra("pushCheck", "hide");
            papaMainIntent.putExtra("userId", mUid);
            startActivity(papaMainIntent);
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

        //click registrationEmail button
        registrationEmail.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {


/*
                Intent papastampIntent = new Intent(LoginActivity.this, MainActivity.class);
                papastampIntent.putExtra("pushCheck", "hide");
                //papastampIntent.putExtra("accessPidToken", accessPidToken);
                startActivity(papastampIntent);
                finish();*/




                String emailInputStr = emailInput.getText().toString();
                String passwordInputStr = passwordInput.getText().toString();

                if ("".equals(emailInputStr)) {
                    Toast.makeText(LoginActivity.this, "E-mail을 입력하세요", Toast.LENGTH_LONG).show();
                } else {
                    if (!isValidEmail(emailInputStr)) { //!isValidEmail(emailInputStr)) {
                        Toast.makeText(LoginActivity.this, "올바른 E-mail을 입력하세요", Toast.LENGTH_LONG).show();
                    } else {
                        if ("".equals(passwordInputStr)) {
                            Toast.makeText(LoginActivity.this, "패스워드를 입력하세요", Toast.LENGTH_LONG).show();
                        } else {
                            if (!isValidPasswd(passwordInputStr)) {
                                Toast.makeText(LoginActivity.this, "패스워드는 6자 이상으로 설정하세요(한글 미포함)", Toast.LENGTH_LONG).show();
                            } else {
                                if (pref.getBoolean("initCheck", false)) {
                                    String saveId = pref.getString("userId", "");
                                    String savePw = pref.getString("userPw", "");

                                    //createAccount(inputId, inputPw);

                                    //updateEmail("fromjaelee@naver.com");

                                    //if(saveId.equals(emailInputStr)) {
                                    //    if(savePw.equals(passwordInputStr)) {
                                            updateEmail(emailInputStr);
                                            Intent papastampIntent = new Intent(LoginActivity.this, MainActivity.class);
                                            papastampIntent.putExtra("pushCheck", "hide");
                                            papastampIntent.putExtra("userId", mUid);
                                            startActivity(papastampIntent);
                                            finish();
                                     //}else {
                                     //      Toast.makeText(LoginActivity.this, "Password를 확인하세요", Toast.LENGTH_LONG).show();
                                     //   }
                                    //}else {
                                    //    Toast.makeText(LoginActivity.this, "Phone Number를 확인하세요", Toast.LENGTH_LONG).show();
                                    //}
                                }else {
                                    saveEditor.putBoolean("initCheck", true);
                                    saveEditor.commit();
                                    Toast.makeText(LoginActivity.this, "Papa Stamp에 오신걸 환영합니다.", Toast.LENGTH_LONG).show();
                                }


                            /*if(pref.getBoolean("userLogin", false)) {
                                if(loginValidation(phone, password)) {
                                    if(loginChecked) {
                                        saveEditor.putString("initCheck", "true");
                                    }else {
                                        saveEditor.putString("initCheck", "false");
                                    }
                                    saveEditor.commit();

                                    Intent papastampIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    papastampIntent.putExtra("pushCheck", false);
                                    papastampIntent.putExtra("userId", phone);
                                    startActivity(papastampIntent);
                                    finish();
                                }
                            }else {
                                String passwordConfirm = passwordConfirmInput.getText().toString();
                                if(password.equals(passwordConfirm)) {
                                    //(new HttpFirstLoginPostTask()).execute("https://whereareevent.com/login/userInfo", phone, password);

                                    if(loginChecked) {
                                        saveEditor.putString("initCheck", "true");
                                    }else {
                                        saveEditor.putString("initCheck", "false");
                                    }
                                    saveEditor.putString("userId", phone);
                                    saveEditor.putString("userPw", password);
                                    saveEditor.commit();

                                    Intent papastampIntent = new Intent(LoginActivity.this, MainActivity.class);
                                    papastampIntent.putExtra("pushCheck", false);
                                    papastampIntent.putExtra("userId", phone);
                                    startActivity(papastampIntent);
                                    finish();
                                }else {
                                    Toast.makeText(LoginActivity.this, "패스워드를 다시 확인하세요", Toast.LENGTH_LONG).show();
                                }
                            }*/
                            }
                        }
                    }
                }
            }
        });

        loginSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //saveEditor.putBoolean("emailPasswordCheck", false);

                Intent papaMainIntent = new Intent (LoginActivity.this, MainActivity.class);
                papaMainIntent.putExtra("pushCheck", "hide");
                papaMainIntent.putExtra("userId", mUid);
                startActivity(papaMainIntent);
                finish();
            }
        });
    }

    private boolean loginValidation(String phone, String password) {
        Log.i(TAG, "Login Check Validation");

        if (pref.getString("userId", "").equals(phone) && pref.getString("userPw", "").equals(password)) {
            // login success
            return true;
        } else if (pref.getString("userId", "").equals(null)) {
            // sign in first
//            Toast.makeText(LoginActivity.this, "Please Sign in first", Toast.LENGTH_LONG).show();
            return false;
        } else {
            // login failed
            return false;
        }
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(mActivity.getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Permission READ_PHONE_STATE denied");

            if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.READ_PHONE_STATE)) {
                Log.d(TAG, "Permission READ_PHONE_STATE was already disabled by user");
            } else {
                //Log.d(TAG, "First permission READ_PHONE_STATE check");
            }

            ActivityCompat.requestPermissions(mActivity,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_TYPE_READ_PHONE_NUMBER);
        } else {
            Log.d(TAG, "Permission READ_PHONE_STATE ok");

            if (ContextCompat.checkSelfPermission(mActivity.getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mActivity.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Permission ACCESS_FINE_LOCATION denied");

                if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.d(TAG, "Permission ACCESS_FINE_LOCATION was already disabled by user");
                } else {
                    //Log.d(TAG, "First permission ACCESS_FINE_LOCATION check");
                }

                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_TYPE_ACCESS_LOCATION);
            } else {
                Log.d(TAG, "Permission ACCESS_FINE_LOCATION ok");
            }
        }
    }

    private void insertFirebaseUserId() {
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
        FirebaseUser user = mAuth.getCurrentUser();

        Log.d(TAG, "updateEmail: firebase uid : " + user.getUid());
        Log.d(TAG, "updateEmail: firebase uid : " + updateEmail);
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

    private void sendLocationToServer(String uid, Double latitude, Double longitude) {
        Log.i(TAG, "send Location to server");

        HttpClientManager httpClientManager = HttpClientManager.getInstance();
        httpClientManager.initHeader();
        HttpRequestLocationInfo body = new HttpRequestLocationInfo(latitude, longitude);
        Call<ResponseBody> call = httpClientManager.updateLocation(uid, body);
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
        if (m.find() && !target.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")){
            return true;
        }else{
            return false;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        FirebaseUser currentUser = mAuth.getCurrentUser();
        accountManager.startListeningForAuthentication();
    }

    @Override
    public void onStop() {
        super.onStop();
        accountManager.stopListeningForAuthentication();
    }
}