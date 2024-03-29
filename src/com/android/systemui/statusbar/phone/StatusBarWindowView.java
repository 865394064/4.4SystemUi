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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextSwitcher;

import com.android.systemui.ExpandHelper;
import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.policy.NotificationRowLayout;
import android.content.Intent;
import android.telephony.TelephonyManager;//addeed by xss for back to phone
public class StatusBarWindowView extends FrameLayout
{
    public static final String TAG = "StatusBarWindowView";
    public static final boolean DEBUG = BaseStatusBar.DEBUG;

    private ExpandHelper mExpandHelper;
    private NotificationRowLayout latestItems;
    private NotificationPanelView mNotificationPanel;
    private ScrollView mScrollView;

    PhoneStatusBar mService;

    public StatusBarWindowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMotionEventSplittingEnabled(false);
        setWillNotDraw(!DEBUG);
    }

    @Override
    protected void onAttachedToWindow () {
        super.onAttachedToWindow();
        latestItems = (NotificationRowLayout) findViewById(R.id.latestItems);
        mScrollView = (ScrollView) findViewById(R.id.scroll);
        mNotificationPanel = (NotificationPanelView) findViewById(R.id.notification_panel);
        int minHeight = getResources().getDimensionPixelSize(R.dimen.notification_row_min_height);
        int maxHeight = getResources().getDimensionPixelSize(R.dimen.notification_row_max_height);
        mExpandHelper = new ExpandHelper(mContext, latestItems, minHeight, maxHeight);
        mExpandHelper.setEventSource(this);
        mExpandHelper.setScrollView(mScrollView);
        /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error.
        getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);

    }
    
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error.
        getViewTreeObserver().removeGlobalOnLayoutListener(mGlobalLayoutListener);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_BACK:
            if (!down) {
                mService.animateCollapsePanels();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }
 private boolean phoneIsUsed(Context context){
                boolean calling = false;  
	         TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	         if(telephonyManager.getCallState()==TelephonyManager.CALL_STATE_OFFHOOK || telephonyManager.getCallState()==TelephonyManager.CALL_STATE_RINGING){
	        	 calling=true;        	 
	         }
	   
	         return calling;
	 }
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
         if(ev.getX()<300 && ev.getY()<40){
	 	  //Log.i("backtolastapp","StatusBar    onInterceptTouchEvent()  ------------click");
                Intent intent=new Intent("click_back_button");
		  intent.putExtra("click", true);
		  getContext().sendBroadcast(intent);
	 }
	 /*Begin:added by xss for back to phone*/
	 if(ev.getY()<=150 && ev.getY()>=40&&ev.getAction()==MotionEvent.ACTION_DOWN && phoneIsUsed(getContext())){
	 	  mService.showBackToPhoneBtn(false);
		  mService.hideLandsView();
                Intent backToPhoneIntent=new Intent(Intent.ACTION_MAIN);
		  backToPhoneIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		  backToPhoneIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	         backToPhoneIntent.setClassName("com.android.phone","com.android.phone.InCallScreen");
		  getContext().startActivity(backToPhoneIntent);
	 }
	 /*End:added by xss for back to phone*/
        boolean intercept = false;
        if (mNotificationPanel.isFullyExpanded() && mScrollView.getVisibility() == View.VISIBLE) {
            intercept = mExpandHelper.onInterceptTouchEvent(ev);
        }
        if (!intercept) {
            super.onInterceptTouchEvent(ev);
        }
        if (intercept) {
            MotionEvent cancellation = MotionEvent.obtain(ev);
            cancellation.setAction(MotionEvent.ACTION_CANCEL);
            latestItems.onInterceptTouchEvent(cancellation);
            cancellation.recycle();
        }
        return intercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean handled = false;
        if (mNotificationPanel.isFullyExpanded()) {
            handled = mExpandHelper.onTouchEvent(ev);
        }
        if (!handled) {
            handled = super.onTouchEvent(ev);
        }
        return handled;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
       /* if (DEBUG) {
            Paint pt = new Paint();
            pt.setColor(0x80FFFF00);
            pt.setStrokeWidth(12.0f);
            pt.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), pt);
        }*/
    }

    public void cancelExpandHelper() {
        if (mExpandHelper != null) {
            mExpandHelper.cancel();
        }
    }

    /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @{
    private final OnGlobalLayoutListener mGlobalLayoutListener = new OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            if (mService.mNeedRelayout) {
                requestLayout();
                mService.mNeedRelayout = false;
                mService.updateCarrierLabelVisibility(true);
            }
        }
    };
    /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @}
}

