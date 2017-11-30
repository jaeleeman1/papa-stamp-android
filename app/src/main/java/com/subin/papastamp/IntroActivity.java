package com.subin.papastamp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.subin.papastamp.common.Constants;

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

		//Get user id
		Intent initIntent = getIntent();
		mUid = initIntent.getExtras().getString("userId");
		Log.d(TAG, "access uid : " + mUid);

		TextView skip = (TextView)findViewById(R.id.skip_text);
		skip.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//Check intro page
				SharedPreferences pref = mActivity.getSharedPreferences(Constants.PREFERENCE_INTRO, Context.MODE_PRIVATE);
				SharedPreferences.Editor edit = pref.edit();
				edit.putBoolean(Constants.PREFERENCE_INTRO_SKIP, true);
				edit.commit();

				Intent introItntent = new Intent(IntroActivity.this, LoginActivity.class);
				introItntent.putExtra("userId", mUid);
				startActivity(introItntent);
				finish();
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
