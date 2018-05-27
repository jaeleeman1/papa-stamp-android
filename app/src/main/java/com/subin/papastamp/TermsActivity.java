package com.subin.papastamp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class TermsActivity extends AppCompatActivity {

    private CheckBox mCbTermsAll, mCbTermsPv, mCbTermsGps;
    private Button mNextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);
        setLayout();
    }

    private void setLayout() {
        mCbTermsAll = (CheckBox)findViewById(R.id.cb_terms_all);
        mCbTermsPv = (CheckBox)findViewById(R.id.cb_terms_private);
        mCbTermsGps = (CheckBox)findViewById(R.id.cb_terms_gps);
        mCbTermsAll.setOnCheckedChangeListener(new CheckBoxCheckListener());
        mCbTermsPv.setOnCheckedChangeListener(new CheckBoxCheckListener());
        mCbTermsGps.setOnCheckedChangeListener(new CheckBoxCheckListener());

        mNextBtn = (Button)findViewById(R.id.btn_terms_next);
        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCbTermsAll.isChecked() || (mCbTermsGps.isChecked() && mCbTermsPv.isChecked())) {
                    Intent signupItntent = new Intent(TermsActivity.this, SignupActivity.class);
                    signupItntent.putExtra("termsCheck", "Y");
                    startActivity(signupItntent);
                    finish();
                } else {
                    Toast.makeText(TermsActivity.this, "약관 동의해야 등록 가능합니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public class CheckBoxCheckListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(buttonView.getId() == mCbTermsAll.getId()) {
                if(isChecked) {
                    mCbTermsGps.setChecked(true);
                    mCbTermsPv.setChecked(true);
                    mNextBtn.setAlpha(1.0f);
                } else {
                    mCbTermsGps.setChecked(false);
                    mCbTermsPv.setChecked(false);
                    mNextBtn.setAlpha(0.5f);
                }
            } else {
                if(mCbTermsAll.isChecked()) {
                    if(!isChecked) {
                        mCbTermsAll.setChecked(false);
                        if(buttonView.getId() == mCbTermsGps.getId()) {
                            mCbTermsPv.setChecked(false);
                        } else {
                            mCbTermsGps.setChecked(false);
                        }
                    }
                } else {
                    if(isChecked) {
                        if(buttonView.getId() == mCbTermsGps.getId()) {
                            if(mCbTermsPv.isChecked()) mCbTermsAll.setChecked(true);
                        } else {
                            if(mCbTermsGps.isChecked()) mCbTermsAll.setChecked(true);
                        }
                    }
                }
            }
        }
    }
}