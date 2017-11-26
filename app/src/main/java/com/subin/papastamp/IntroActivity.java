package com.subin.papastamp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.subin.papastamp.common.Constants;
import com.subin.papastamp.model.UserManager;

public class IntroActivity extends Activity implements ViewPager.OnPageChangeListener {
	private final String TAG = "[Intro Activity] ";
	private IntroActivity mActivity;
	private String mUid;

	public IntroActivity() {
		mActivity = this;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_intro);

		Intent initIntent = getIntent();
		mUid = initIntent.getExtras().getString("userId");
		Log.d(TAG, "access uid : " + mUid);

		TextView skip = (TextView)findViewById(R.id.skip_text);
		skip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				SharedPreferences pref = mActivity.getSharedPreferences(Constants.PREFERENCE_INTRO, Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = pref.edit();
				edit.putBoolean(Constants.PREFERENCE_INTRO_SKIP, true);
				edit.commit();

				Intent introItntent = new Intent(IntroActivity.this, LoginActivity.class);
//				introItntent.putExtra("regCheck", "hide");
				introItntent.putExtra("userId", mUid);
				startActivity(introItntent);
				finish();
				/*AlertDialog.Builder alert_confirm = new AlertDialog.Builder(IntroActivity.this);
				alert_confirm.setMessage("E-mail 등록 하시겠습니까??").setCancelable(false).setPositiveButton("등록하기",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						}).setNegativeButton("다음에 등록하기",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								itntent = new Intent(IntroActivity.this, MainActivity.class);
								itntent.putExtra("pushCheck", "hide");
								itntent.putExtra("shopCode", "0000");
								itntent.putExtra("userId", mUid);
								startActivity(itntent);
								finish();
							}
						});
				AlertDialog alert = alert_confirm.create();
				alert.show();*/
			}
		});

		IntroPageAdapter adapter = new IntroPageAdapter(this);
		((ViewPager)findViewById(R.id.intro_pager)).setAdapter(adapter);
		((ViewPager)findViewById(R.id.intro_pager)).addOnPageChangeListener(this);
	}

	@Override
	public void onPageSelected(int position) {
		Log.d(TAG, "onPageSelected : " + position);
		setPage(position);
	}

	private void setPage(int position) {
		if(position == 0) {
			((TextView)findViewById(R.id.skip_text)).setText("건너뛰기");
			((ToggleButton)findViewById(R.id.indi_0)).setChecked(true);
			((ToggleButton)findViewById(R.id.indi_1)).setChecked(false);
		} else {
			((TextView)findViewById(R.id.skip_text)).setText("확인완료");
			((ToggleButton)findViewById(R.id.indi_0)).setChecked(false);
			((ToggleButton)findViewById(R.id.indi_1)).setChecked(true);
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
	}
}
