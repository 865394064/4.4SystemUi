/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.systemui.R;
import com.android.systemui.statusbar.GestureRecorder;

public class NotificationPanelView extends PanelView {

    Drawable mHandleBar;
    int mHandleBarHeight;
    View mHandleView;
    int mFingers;
    PhoneStatusBar mStatusBar;
    boolean mOkToFlip;

    public NotificationPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setStatusBar(PhoneStatusBar bar) {
        mStatusBar = bar;
    }
    /*Begin:added by xss for ios 10 */
    public ApplicationPreview mApplicationPreview;
    public void setContext(){
        mApplicationPreview.setContext(getContext(),false);
    }
    public void lockScreen(){
        mApplicationPreview.lockScreen();
    }
    public void unLockScreen(){
        mApplicationPreview.unLockScreen();
    }
    public void showAppWidget(Context context){
        mApplicationPreview.showAppWidget(context);
    }
    public void getAppWeather(){
        mApplicationPreview.getAppWeather();
    }
    public void memoStartQuery(){
        mApplicationPreview.memoStartQuery();
    }
    public void calendarEventChanged(){
        mApplicationPreview.calendarEventChanged();
    }
    public void calendarEventUpdated(){
        mApplicationPreview.calendarEventUpdated();;
    }
    /*End:added by xss for ios 10*/
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        Resources resources = getContext().getResources();
        mHandleBar = resources.getDrawable(R.drawable.status_bar_close);
        mHandleBarHeight = resources.getDimensionPixelSize(R.dimen.close_handle_height);
        mHandleView = findViewById(R.id.handle);

        setContentDescription(resources.getString(R.string.accessibility_desc_notification_shade));
         /*Begin:added by xss for ios */
        mApplicationPreview=(ApplicationPreview)View.inflate(getContext(), R.layout.application_preview, null);
        mApplicationPreview.setContext(getContext(),false);
	 /*End:added by xss for ios */
    }

    @Override
    public void fling(float vel, boolean always) {
        GestureRecorder gr = ((PhoneStatusBarView) mBar).mBar.getGestureRecorder();
        if (gr != null) {
            gr.tag(
                "fling " + ((vel > 0) ? "open" : "closed"),
                "notifications,v=" + vel);
        }
        super.fling(vel, always);
    }

    // We draw the handle ourselves so that it's always glued to the bottom of the window.
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            final int pl = getPaddingLeft();
            final int pr = getPaddingRight();
            mHandleBar.setBounds(pl, 0, getWidth() - pr, (int) mHandleBarHeight);
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        final int off = (int) (getHeight() - mHandleBarHeight - getPaddingBottom());
        canvas.translate(0, off);
        mHandleBar.setState(mHandleView.getDrawableState());
        mHandleBar.draw(canvas);
        canvas.translate(0, -off);
    }

     /*Begin:added by xss for back to last app*/ 
   Intent notifipanelIsShowIntent=new Intent("notificationContentView_is_show"); 
   float mLastY;
/*End:added by xss for back to last app*/	

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (PhoneStatusBar.SETTINGS_DRAG_SHORTCUT && mStatusBar.mHasFlipSettings) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    mOkToFlip = getExpandedHeight() == 0;
		      mLastY = event.getRawY();		//added by xss for back to last app 			
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    if (mOkToFlip) {
                        float miny = event.getY(0);
                        float maxy = miny;
                        for (int i=1; i<event.getPointerCount(); i++) {
                            final float y = event.getY(i);
                            if (y < miny) miny = y;
                            if (y > maxy) maxy = y;
                        }
                        if (maxy - miny < mHandleBarHeight) {
                            if (getMeasuredHeight() < mHandleBarHeight) {
                                mStatusBar.switchToSettings();
                            } else {
                                mStatusBar.flipToSettings();
                            }
                            mOkToFlip = false;
                        }
                    }
                    break;
		 /*Begin:added by xss for back to last app*/ 			
		 case MotionEvent.ACTION_MOVE:
		 	  int dy = (int) (event.getRawY() - mLastY); 
                         if(dy>100){
				      //Log.i("backtolastapp2","notificationPanelView    onTouchEvent()  -----------------ACTION_MOVE ");	 	 	
                                  //notifipanelIsShowIntent.putExtra("is_click",true);
	                           getContext().sendBroadcast(notifipanelIsShowIntent);
			   }
			 break;
		  /*End:added by xss for back to last app*/ 			
            }
        }
        return mHandleView.dispatchTouchEvent(event);
    }

    @Override
    protected  void initGetDownState(Context context,MotionEvent event,int handHeight){
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        Log.d("kay", "notifacation initGetDownState: (" + event.getX() + "," + event.getY() + ")" + " screenWidth:" + screenWidth/2.0 + " handheight:" + handHeight);
        if(event.getY() < handHeight ){
            if(event.getX() < screenWidth/2.0) {
                Log.d("kay", "initGetDownState: left");
                mStatusBar.changeViewPager(PhoneStatusBar.SHOWTYPE_LEFT);
            }else{
                Log.d("kay", "initGetDownState: right");
                mStatusBar.changeViewPager(PhoneStatusBar.SHOWTYPE_RIGHT);
            }
        }
    }

}
