package com.android.systemui.statusbar;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import com.android.systemui.R;

public class ProgressWidget extends ImageView {
	private ValueAnimator valueAnimation;
	private int mAnimationFrame = 0;
	public ProgressWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
		final LinearInterpolator lin = new LinearInterpolator();
		valueAnimation = ValueAnimator.ofInt(0,360);
		valueAnimation.setDuration(1000);
		valueAnimation.setInterpolator(lin);
		valueAnimation.setRepeatCount(-1);
		valueAnimation.addUpdateListener(new AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int keyframe = (Integer)animation.getAnimatedValue()/30 + 1;
				if(mAnimationFrame != keyframe) {
					setRotation(keyframe * 30);
					invalidate();
					mAnimationFrame = keyframe;
				}
			}
		});
	}
	public void startProgress(){
		if(valueAnimation.isStarted()) return;
		setImageResource(R.drawable.connect_progress);
		valueAnimation.start();
		setVisibility(View.VISIBLE);
	}
	
	public void progressOk(){
		valueAnimation.end();
		setRotation(0);
		//setImageResource(R.drawable.contect_ok);
		setVisibility(View.VISIBLE);
	}
	
	public void stop(){
		valueAnimation.end();
		setVisibility(View.INVISIBLE);
	}
}
