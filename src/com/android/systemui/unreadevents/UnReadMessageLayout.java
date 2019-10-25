package com.android.systemui.unreadevents;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.android.systemui.R;
import android.util.Log;
import android.content.res.Configuration;
import android.view.ViewConfiguration;
import com.android.systemui.unreadevents.SwipeHelper;
import com.android.systemui.unreadevents.SwipeHelper.Callback;
import android.content.Intent;

public class UnReadMessageLayout extends ScrollView implements Callback {

   private static final String TAG = "UnReadMessageLayout";
	
    private long mQueryBaseTime;
    public LinearLayout mContentLayout;
    private LayoutInflater mInflater;
	public static final String TYPE_MMS = "mms";
	public static final String TYPE_SMS = "sms";
    
    private MmsUnReadObserver obMms;
    private MissCallUnReadObserver obCall;

	private Context mContext;
	private Handler mHandler = new Handler();

	private SwipeHelper mSwipeHelper;
    	private int listsize=0;
    
    public UnReadMessageLayout(Context context) {
        this(context, null);
    }
    
    public UnReadMessageLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public UnReadMessageLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setMotionEventSplittingEnabled(false);


        float densityScale = getResources().getDisplayMetrics().density;
        float pagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mSwipeHelper = new SwipeHelper(SwipeHelper.X, this, densityScale, pagingTouchSlop);

		
        mInflater = LayoutInflater.from(context);
        mContentLayout = (LinearLayout) mInflater.inflate(R.layout.zzzzz_unread_message_content_layout, null);
        addView(mContentLayout);

		
       /* obMms = new MmsUnReadObserver(mHandler, UnReadMessageLayout.this, mQueryBaseTime);
        obCall = new MissCallUnReadObserver(mHandler, UnReadMessageLayout.this, mQueryBaseTime);*/
	mContext = context;
    }

	public void addMessage(List<UnReadMessage> data) {
		//System.out.println("updateMessage ");
		if(data == null || data.size() == 0) {
			return;
		}
		final int size = data.size();
		System.out.println("UnReadMessageLayout updateMessage size="+size);

		for(int i=0;i<size;i++) {
			final UnReadMessage msg = data.get(i);
			addMessage(msg);
		}
	}
	
	private boolean containsMessage(UnReadMessage msg) {
		final int size = mContentLayout.getChildCount();
		for(int i=0;i<size;i++) {
			final UnReadMessageView view = (UnReadMessageView) mContentLayout.getChildAt(i);
			final UnReadMessage m = view.getData();
			if(m.equals(msg)) {
				return true;
			}
		}
		return false;
	}
	
	private void addMessage(UnReadMessage msg) {
		if(null == msg || containsMessage(msg)) {
			return;
		}
		UnReadMessageView view = (UnReadMessageView) mInflater.inflate(R.layout.unread_message_view, null);
		view.updateView(msg);
        /*added by xujia*/
        if(mContentLayout.getChildCount()==0){

            view.divderLine.setVisibility(View.GONE);
          }else{
            view.divderLine.setVisibility(View.VISIBLE);
            }
          /*End by xujia*/
		addView(view);
	}


	public void addOrUpdateMessage(List<UnReadMessage> data) {
		//System.out.println("updateMessage ");
		if(data == null || data.size() == 0) {
			return;
		}
		final int size = data.size();
		System.out.println("UnReadMessageLayout updateMessage size="+size);

		for(int i=0;i<size;i++) {
			final UnReadMessage msg = data.get(i);
			addOrUpdateMessage(msg);
		}
	}


	private void addOrUpdateMessage(UnReadMessage msg) {
		if(null == msg) {
			return;
		}
		if(containsMessage(msg)) {
			updateMessage(msg);
			return;
		}
		UnReadMessageView view = (UnReadMessageView) mInflater.inflate(R.layout.unread_message_view, null);
		view.updateView(msg);
        /*added by xujia*/
        if(mContentLayout.getChildCount()==0){

            view.divderLine.setVisibility(View.GONE);
          }else{
            view.divderLine.setVisibility(View.VISIBLE);
            }
          /*End by xujia*/
		addView(view);
	}



	private void updateMessage(UnReadMessage msg) {
		final int size = mContentLayout.getChildCount();
		for(int i=0;i<size;i++) {
			final UnReadMessageView view = (UnReadMessageView) mContentLayout.getChildAt(i);
			final UnReadMessage m = view.getData();
			if(m.equals(msg)) {
				view.updateSnippet(msg);
				break;
			}
		}
	}


	public void removeAllMessage(List<UnReadMessageView> data) {
		//System.out.println("updateMessage ");
		if(data == null||data.size()==0 ) {
			return;
		}
		final int size = data.size();
		
		for(int i=0;i<size;i++) {
			mContentLayout.removeView(data.get(i));
		}
	}

	
	private void addView(UnReadMessageView itemView) {
		mContentLayout.addView(itemView, 0);
		setVisibility(View.VISIBLE);
		hideMusicAlbumart();
	}

	private void hideMusicAlbumart() {
		final Intent updateIntent = new Intent("hide_lock_screen_albumart");
		getContext().sendBroadcast(updateIntent);
	}

	
    @Override
	protected void onFinishInflate() {
		super.onFinishInflate();
	}

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

       /* mContext.getContentResolver().registerContentObserver(
        		MmsUnReadObserver.MMS_URI, true, obMms);
        obMms.refreshUnReadMessage();
        
        mContext.getContentResolver().registerContentObserver(
        		MissCallUnReadObserver.MISS_CALL_URI, true, obCall);
        obCall.refreshUnReadMessage();
		obMms.updateQueryBaseTime(System.currentTimeMillis());
		obCall.updateQueryBaseTime(System.currentTimeMillis());*/
    }
    
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        
        /*mContext.getContentResolver().unregisterContentObserver(obMms);
        mContext.getContentResolver().unregisterContentObserver(obCall);*/
    }
    
    public void updateQueryBaseTimeAndRefreshUnReadNumber(long qbt) {
        mQueryBaseTime = qbt;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float densityScale = getResources().getDisplayMetrics().density;
        mSwipeHelper.setDensityScale(densityScale);
        float pagingTouchSlop = ViewConfiguration.get(getContext()).getScaledPagingTouchSlop();
        mSwipeHelper.setPagingTouchSlop(pagingTouchSlop);
    }
/*
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
    	Log.d(TAG, "onInterceptTouchEvent");
        return mSwipeHelper.onInterceptTouchEvent(ev) || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	Log.d(TAG, "onTouchEvent");
        return mSwipeHelper.onTouchEvent(ev) || super.onTouchEvent(ev);
    }
*/

    @Override
    public View getChildAtPosition(MotionEvent ev) {
        // find the view under the pointer, accounting for GONE views
        final int count = mContentLayout.getChildCount();
        int touchY = (int) ev.getY();
        int childIdx = 0;
        View slidingChild;
        for (; childIdx < count; childIdx++) {
            slidingChild = mContentLayout.getChildAt(childIdx);
            if (slidingChild.getVisibility() == GONE) {
                continue;
            }
            if (touchY >= slidingChild.getTop() && touchY <= slidingChild.getBottom()) {
                return slidingChild;
            }
        }
        return null;
    }

    @Override
    public View getChildContentView(View view) {
        return view;
    }

    @Override
    public boolean canChildBeDismissed(View v) {
        return true;
    }

    @Override
    public void onChildDismissed(final View v) {

    }

    @Override
    public void onDragCancelled(View v) {
    }

    @Override
    public void onBeginDrag(View v) {
        // We do this so the underlying ScrollView knows that it won't get
        // the chance to intercept events anymore
        requestDisallowInterceptTouchEvent(true);
    }
}
