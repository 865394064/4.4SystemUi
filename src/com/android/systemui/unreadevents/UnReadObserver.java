package com.android.systemui.unreadevents;

import java.util.List;

import android.database.ContentObserver;
import android.os.Handler;
import android.util.Log;
import android.provider.Settings;
import android.app.KeyguardManager;
import android.content.Context;

public abstract class UnReadObserver extends ContentObserver {
    static final String TAG = "UnReadObserver";

	private final static String IPHONE_SMS_PREVIEW_STATE="iphonesmspreviewstate";
	boolean mShowMessageContent = false;
	
    
    final UnReadMessageLayout mNewEventView;

    private KeyguardManager mKeyguardManager;
    
    long mCreateTime;
    public Handler mRefreshHandler;
    public UnReadObserver(Handler handler, UnReadMessageLayout newEventView, long createTime) {
        super(handler);
		mRefreshHandler = handler;
        mNewEventView = newEventView;
        mCreateTime = createTime;
	mShowMessageContent = Settings.System.getInt(newEventView.getContext().getContentResolver(), IPHONE_SMS_PREVIEW_STATE, 0) != 0;
	mKeyguardManager = (KeyguardManager) newEventView.getContext().getSystemService(Context.KEYGUARD_SERVICE);
    }
    
    public void onChange(boolean selfChange) {

	
	 if(mKeyguardManager.isKeyguardLocked()){
    		refreshUnReadMessage();
	}else{
		refreshAllMessage();
	}
    }

   public void setShowMessageContent(boolean showMessage){
		
		this.mShowMessageContent=showMessage;

	}
    

    public abstract void refreshAllMessage();
    public abstract void refreshUnReadMessage();


   public final void removeAllMessage(final List<UnReadMessageView> list){
		if (mNewEventView != null) {
			mNewEventView.removeAllMessage(list);
		}

	}
    
    public final void upateNewEventMessage(final List<UnReadMessage> data) {
        if (mNewEventView != null) {
        	if(!mShowMessageContent) {
				mNewEventView.addOrUpdateMessage(data);
				return;
        	}
            mNewEventView.addMessage(data);
        } else {
            Log.e(TAG, "mNewEventView is null");
        }
    }
    
    // When queryt base time changed, we need to reset new event number
    public void updateQueryBaseTime(long newBaseTime) {
        mCreateTime = newBaseTime;
        upateNewEventMessage(null);
    }
    
}
