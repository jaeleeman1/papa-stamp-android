package com.subin.papastamp;

import android.app.Activity;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class IntroPageAdapter extends PagerAdapter {
	private final String TAG = "Papastamp " + getClass().getSimpleName();
	private Activity mActivity;

	private final int PAGE_CNT = 2;


	public IntroPageAdapter(Activity ctx) {
		this.mActivity = ctx;
	}

	@Override
	public int getCount() {
		return PAGE_CNT;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Log.d(TAG, "instantiateItem : " + position);
		View view = null;
		view = mActivity.getLayoutInflater().inflate(R.layout.intro_pageview, null);
		setLayout(position, view);
		container.addView(view);
		return view;
	}

	private void setLayout(int position, View view) {
		Log.d(TAG, "setLayout : " + position);
		ImageView image = (ImageView)view.findViewById(R.id.image_view);
		TextView title = (TextView)view.findViewById(R.id.title_view);
		TextView subtitle = (TextView)view.findViewById(R.id.subtitle_view);
		if(position == 0) {
			image.setImageResource(R.drawable.header_papastamp_icon);
			title.setText("파파 스탬프");
			subtitle.setText("이제 온라인으로 모든 스탬프를 관리해 보세요!");
		} else {
			image.setImageResource(R.drawable.stamp_action);
			title.setText("스탬프 적립");
			subtitle.setText("매장에서 간편하게 사용해 보세요~");
		}
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		Log.d(TAG, "destroyItem : " + position);
		container.removeView((View)object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}
