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

import android.animation.ObjectAnimator;
import android.animation.TimeAnimator;
import android.animation.TimeAnimator.TimeListener;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.android.systemui.R;

import java.util.ArrayDeque;
import java.util.Iterator;

public class PanelView extends FrameLayout {
    public static final boolean DEBUG = PanelBar.DEBUG;
    public static final String TAG = PanelView.class.getSimpleName();
    public final void LOG(String fmt, Object... args) {
        if (!DEBUG) return;
        Slog.v(TAG, (mViewName != null ? (mViewName + ": ") : "") + String.format(fmt, args));
    }

    public static final boolean BRAKES = false;
    private boolean mRubberbandingEnabled = true;

    private float mSelfExpandVelocityPx; // classic value: 2000px/s
    private float mSelfCollapseVelocityPx; // classic value: 2000px/s (will be negated to collapse "up")
    private float mFlingExpandMinVelocityPx; // classic value: 200px/s
    private float mFlingCollapseMinVelocityPx; // classic value: 200px/s
    private float mCollapseMinDisplayFraction; // classic value: 0.08 (25px/min(320px,480px) on G1)
    private float mExpandMinDisplayFraction; // classic value: 0.5 (drag open halfway to expand)
    private float mFlingGestureMaxXVelocityPx; // classic value: 150px/s

    private float mFlingGestureMinDistPx;

    private float mExpandAccelPx; // classic value: 2000px/s/s
    private float mCollapseAccelPx; // classic value: 2000px/s/s (will be negated to collapse "up")

    private float mFlingGestureMaxOutputVelocityPx; // how fast can it really go? (should be a little
                                                    // faster than mSelfCollapseVelocityPx)

    private float mCollapseBrakingDistancePx = 200; // XXX Resource
    private float mExpandBrakingDistancePx = 150; // XXX Resource
    private float mBrakingSpeedPx = 150; // XXX Resource

    private View mHandleView;
    private float mPeekHeight;
    private float mTouchOffset;
    private float mExpandedFraction = 0;
    private float mExpandedHeight = 0;
    private boolean mJustPeeked;
    private boolean mClosing;
    private boolean mRubberbanding;
    private boolean mTracking;

    private TimeAnimator mTimeAnimator;
    private ObjectAnimator mPeekAnimator;
    private FlingTracker mVelocityTracker;

    /**
     * A very simple low-pass velocity filter for motion events; not nearly as sophisticated as
     * VelocityTracker but optimized for the kinds of gestures we expect to see in status bar
     * panels.
     */
    private static class FlingTracker {
        static final boolean DEBUG = false;
        final int MAX_EVENTS = 8;
        final float DECAY = 0.75f;
        ArrayDeque<MotionEventCopy> mEventBuf = new ArrayDeque<MotionEventCopy>(MAX_EVENTS);
        float mVX, mVY = 0;
        private static class MotionEventCopy {
            public MotionEventCopy(float x2, float y2, long eventTime) {
                this.x = x2;
                this.y = y2;
                this.t = eventTime;
            }
            public float x, y;
            public long t;
        }
        public FlingTracker() {
        }
        public void addMovement(MotionEvent event) {
            if (mEventBuf.size() == MAX_EVENTS) {
                mEventBuf.remove();
            }
            mEventBuf.add(new MotionEventCopy(event.getX(), event.getY(), event.getEventTime()));
        }
        public void computeCurrentVelocity(long timebase) {
            /// M: [ALPS00563144] Fix FlingTracker defect when two events have the same timestamp.
            if (FlingTracker.DEBUG) {
                Slog.v("FlingTracker", "computing velocities for " + mEventBuf.size() + " events");
            }
            mVX = mVY = 0;
            MotionEventCopy last = null;
            int i = 0;
            int j = 0;
            float totalweight = 0f;
            float weight = 10f;
            for (final Iterator<MotionEventCopy> iter = mEventBuf.descendingIterator();
                    iter.hasNext();) {
                final MotionEventCopy event = iter.next();
                if (last != null) {
                    final float dt = (float) (event.t - last.t) / timebase;
                    if (dt == 0) {
                        last = event;
                        continue;
                    }
                    final float dx = (event.x - last.x);
                    final float dy = (event.y - last.y);
                    if (FlingTracker.DEBUG) {
                        Slog.v("FlingTracker", String.format("   [%d] dx=%.1f dy=%.1f dt=%.0f vx=%.1f vy=%.1f",
                                i,
                                dx, dy, dt,
                                (dx/dt),
                                (dy/dt)
                                ));
                    }
                    mVX += weight * dx / dt;
                    mVY += weight * dy / dt;
                    totalweight += weight;
                    weight *= DECAY;
                    j++;
                }
                last = event;
                i++;
            }
            if (j != 0) {
                mVX /= totalweight;
                mVY /= totalweight;
            }

            if (FlingTracker.DEBUG) {
                Slog.v("FlingTracker", "computed: vx=" + mVX + " vy=" + mVY);
            }
        }
        public float getXVelocity() {
            return mVX;
        }
        public float getYVelocity() {
            return mVY;
        }
        public void recycle() {
            mEventBuf.clear();
        }

        static FlingTracker sTracker;
        static FlingTracker obtain() {
            if (sTracker == null) {
                sTracker = new FlingTracker();
            }
            return sTracker;
        }
    }

    private int[] mAbsPos = new int[2];
    PanelBar mBar;
    /// M: [SystemUI] Avoid statusbar can't excute animateExpandNotificationsPanel() rightly when first power on device.
    private boolean mIsFirstAnimTick = true;

    private final TimeListener mAnimationCallback = new TimeListener() {
        @Override
        public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
            animationTick(deltaTime);
        }
    };

    private final Runnable mStopAnimator = new Runnable() {
        @Override
        public void run() {
            if (mTimeAnimator != null && mTimeAnimator.isStarted()) {
                mTimeAnimator.end();
                mRubberbanding = false;
                mClosing = false;
            }
        }
    };

    private float mVel, mAccel;
    private int mFullHeight = 0;
    private String mViewName;
    protected float mInitialTouchY;
    protected float mFinalTouchY;

    public void setRubberbandingEnabled(boolean enable) {
        mRubberbandingEnabled = enable;
    }

    private void runPeekAnimation() {
        if (DEBUG) LOG("peek to height=%.1f", mPeekHeight);
        if (mTimeAnimator.isStarted()) {
            return;
        }
        if (mPeekAnimator == null) {
            mPeekAnimator = ObjectAnimator.ofFloat(this,
                    "expandedHeight", mPeekHeight-1280)//mod by csc form mPeekHeight
                .setDuration(250);
        }
        mPeekAnimator.start();
    }

    private void animationTick(long dtms) {
        if (!mTimeAnimator.isStarted()) {
            // XXX HAX to work around bug in TimeAnimator.end() not resetting its last time
            mTimeAnimator = new TimeAnimator();
            mTimeAnimator.setTimeListener(mAnimationCallback);

            if (mPeekAnimator != null) mPeekAnimator.cancel();

            mTimeAnimator.start();

            mRubberbanding = mRubberbandingEnabled // is it enabled at all?
                    && mExpandedHeight > getFullHeight() // are we past the end?
                    && mVel >= -mFlingGestureMinDistPx; // was this not possibly a "close" gesture?
            if (mVel >= -mFlingGestureMinDistPx) {//mod by csc from mRubberbanding
                mClosing = false;//mod by csc from true
            } else if (mVel == 0) {
                // if the panel is less than halfway open, close it
                mClosing = (mFinalTouchY / getFullHeight()) < 0.5f;
            } else {
                mClosing = true;//mod by csc from mExpandedHeight > 0 && mVel < 0
            }
        } else if (dtms > 0) {
            final float dt = dtms * 0.001f;                  // ms -> s
            if (DEBUG) LOG("tick: v=%.2fpx/s dt=%.4fs", mVel, dt);
            if (DEBUG) LOG("tick: before: h=%d", (int) mExpandedHeight);

            final float fh = getFullHeight();
            boolean braking = true;//mod by csc from false
            if (BRAKES) {
                if (mClosing) {
                    braking = mExpandedHeight <= mCollapseBrakingDistancePx;
                    mAccel = braking ? 10*mCollapseAccelPx : -mCollapseAccelPx;
                } else {
                    braking = mExpandedHeight >= (fh-mExpandBrakingDistancePx);
                    mAccel = braking ? 10*-mExpandAccelPx : mExpandAccelPx;
                }
            } else {
                mAccel = mClosing ? -mCollapseAccelPx : mExpandAccelPx;
            }

            mVel += mAccel * dt;

            if (braking) {
                if (mClosing && mVel > -mBrakingSpeedPx) {
                    mVel = -mBrakingSpeedPx;
                } else if (!mClosing && mVel < mBrakingSpeedPx) {
                    mVel = mBrakingSpeedPx;
                }
            } else {
                if (mClosing && mVel > -mFlingCollapseMinVelocityPx) {
                    mVel = -mFlingCollapseMinVelocityPx;
                } else if (!mClosing && mVel > mFlingGestureMaxOutputVelocityPx) {
                    mVel = mFlingGestureMaxOutputVelocityPx;
                }
            }

			//begin  delete by csc
            /*float h = mExpandedHeight + mVel * dt;

            if (mRubberbanding && h < fh) {
                h = fh;
            }

            if (DEBUG) LOG("tick: new h=%d closing=%s", (int) h, mClosing?"true":"false");

            setExpandedHeightInternal(h);*/
			// end delete by csc
			
			// begin add by csc
            float translationY = PanelView.this.getY() + mVel * dt;

            if (translationY > 0) translationY = 0;
            if(translationY + getFullHeight() < 0) translationY = -getFullHeight();
            //setExpandedHeightInternal(h);
            PanelView.this.setY(translationY);
            mExpandedHeight = fh - Math.abs(PanelView.this.getY());
			
			// end add by csc

            //PanelView.this.requestLayout();

            mBar.panelExpansionChanged(PanelView.this, mExpandedFraction);

            if (mVel == 0
                    || (mClosing && mExpandedHeight == 0)
                    || ((mRubberbanding || !mClosing) && mExpandedHeight == fh)) {
                post(mStopAnimator);
            }
        }
    }


	private final Context mContext;

    public PanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
	    mContext=context;
        mTimeAnimator = new TimeAnimator();
        mTimeAnimator.setTimeListener(mAnimationCallback);
    }

    private void loadDimens() {
        final Resources res = getContext().getResources();

        mSelfExpandVelocityPx = res.getDimension(R.dimen.self_expand_velocity);
        mSelfCollapseVelocityPx = res.getDimension(R.dimen.self_collapse_velocity);
        mFlingExpandMinVelocityPx = res.getDimension(R.dimen.fling_expand_min_velocity);
        mFlingCollapseMinVelocityPx = res.getDimension(R.dimen.fling_collapse_min_velocity);

        mFlingGestureMinDistPx = res.getDimension(R.dimen.fling_gesture_min_dist);

        mCollapseMinDisplayFraction = res.getFraction(R.dimen.collapse_min_display_fraction, 1, 1);
        mExpandMinDisplayFraction = res.getFraction(R.dimen.expand_min_display_fraction, 1, 1);

        mExpandAccelPx = res.getDimension(R.dimen.expand_accel);
        mCollapseAccelPx = res.getDimension(R.dimen.collapse_accel);

        mFlingGestureMaxXVelocityPx = res.getDimension(R.dimen.fling_gesture_max_x_velocity);

        mFlingGestureMaxOutputVelocityPx = res.getDimension(R.dimen.fling_gesture_max_output_velocity);

        mPeekHeight = res.getDimension(R.dimen.peek_height) 
            + getPaddingBottom() // our window might have a dropshadow
            - (mHandleView == null ? 0 : mHandleView.getPaddingTop()); // the handle might have a topshadow
    }

    private void trackMovement(MotionEvent event) {
        // Add movement to velocity tracker using raw screen X and Y coordinates instead
        // of window coordinates because the window frame may be moving at the same time.
        float deltaX = event.getRawX() - event.getX();
        float deltaY = event.getRawY() - event.getY();
        event.offsetLocation(deltaX, deltaY);
        if (mVelocityTracker != null) mVelocityTracker.addMovement(event);
        event.offsetLocation(-deltaX, -deltaY);
    }

    protected void initGetDownState(Context mContext,MotionEvent event,int handHeight){
        Log.d("kay", "panelview testNotification: ");
    }
    // Pass all touches along to the handle, allowing the user to drag the panel closed from its interior
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mHandleView.dispatchTouchEvent(event);
    }
    private float initPanelViewYLocation;//add by joyisn
    boolean isFromFullExpend = false;//added by lzp
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mHandleView = findViewById(R.id.handle);

        loadDimens();

        if (DEBUG) LOG("handle view: " + mHandleView);
        if (mHandleView != null) {
            mHandleView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    /// M: [ALPS00583181] Handle on-going touch condition to stop tracking events.
                    if (isCollapsing()
                        || (event.getAction() != MotionEvent.ACTION_DOWN && !mTracking)) {
                        return true;
                    }
                    final float x = event.getX();
                    final float y = event.getY();
                    final float rawY = event.getRawY();
                    //Log.d("kay", "onTouch: mHandleView:(" + x + "," + y + ")  rawY:" + rawY);
                    if (DEBUG) LOG("handle.onTouch: a=%s y=%.1f rawY=%.1f off=%.1f",
                            MotionEvent.actionToString(event.getAction()),
                            y, rawY, mTouchOffset);
                    PanelView.this.getLocationOnScreen(mAbsPos);

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            mTracking = true;
                            requestDisallowInterceptTouchEvent(true);
                            mHandleView.setPressed(true);
                            postInvalidate(); // catch the press state change
                           initPanelViewYLocation = PanelView.this.getY();//add by joyisn
                                mInitialTouchY = rawY;
                            mVelocityTracker = FlingTracker.obtain();
                            trackMovement(event);
                            mTimeAnimator.cancel(); // end any outstanding animations
                            mBar.onTrackingStarted(PanelView.this);
                            mTouchOffset = (rawY - mAbsPos[1]) - PanelView.this.getExpandedHeight();
                            if (mExpandedHeight == 0) {
                                mJustPeeked = true;
                                runPeekAnimation();
                            }
				            isFromFullExpend =isFullyExpanded();//added by lzp
                            initGetDownState(mContext,event,mHandleView.getHeight());
                            break;

                        case MotionEvent.ACTION_MOVE:
                            final float h = rawY - mAbsPos[1] - mTouchOffset;
							final float touchMoveYDistance = rawY - mInitialTouchY;
                         //   if (h > mPeekHeight) {
                                if (mPeekAnimator != null && mPeekAnimator.isRunning()) {
                                    mPeekAnimator.cancel();
                                }
                                mJustPeeked = false;
                          //  }
                            if (!mJustPeeked) {
//                                PanelView.this.setExpandedHeightInternal(h);// delete by csc
                                /*Log.d("chenshichun",""+this.getClass().getCanonicalName()+"::::::::::::::::::touchMoveYDistance:::"+touchMoveYDistance);
                                Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::PanelView.this.getY()::::"+PanelView.this.getY());
                                Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::PanelView.getH::"+PanelView.this.getHeight());
                                PanelView.this.setY(touchMoveYDistance==0?-1080:touchMoveYDistance-1080);//initPanelViewYLocation + touchMoveYDistance
                                    mExpandedHeight = getFullHeight() + PanelView.this.getY();
                                mBar.panelExpansionChanged(PanelView.this, mExpandedFraction);*/
                                PanelView.this.setY(initPanelViewYLocation + touchMoveYDistance > 0 ? 0 : initPanelViewYLocation + touchMoveYDistance);// add by csc
                                Log.d("chenshichun", "" + this.getClass().getCanonicalName() + ":::::::::::::::PanelView.this.getY()::::::" + PanelView.this.getY());
                                Log.d("chenshichun",""+this.getClass().getCanonicalName()+"::::::::::::::::::PanelView.this.H:::"+PanelView.this.getHeight());
                                mExpandedHeight = getFullHeight() + PanelView.this.getY();// add by csc
                                //PanelView.this.requestLayout();
                                mBar.panelExpansionChanged(PanelView.this, mExpandedFraction);
                            }

                            trackMovement(event);
                            break;

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            mFinalTouchY = rawY; // mod by csc from y to rawY
                            mTracking = false;
                            mHandleView.setPressed(false);
                            postInvalidate(); // catch the press state change
                            mBar.onTrackingStopped(PanelView.this);
                            trackMovement(event);

                            float vel = 0, yVel = 0, xVel = 0;
                            boolean negative = false;

                            if (mVelocityTracker != null) {
                                // the velocitytracker might be null if we got a bad input stream
                                mVelocityTracker.computeCurrentVelocity(1000);

                                yVel = mVelocityTracker.getYVelocity();
                                negative = yVel < 0;

                                xVel = mVelocityTracker.getXVelocity();
                                if (xVel < 0) {
                                    xVel = -xVel;
                                }
                                if (xVel > mFlingGestureMaxXVelocityPx) {
                                    xVel = mFlingGestureMaxXVelocityPx; // limit how much we care about the x axis
                                }

                                vel = (float)Math.hypot(yVel, xVel);
                                if (vel > mFlingGestureMaxOutputVelocityPx) {
                                    vel = mFlingGestureMaxOutputVelocityPx;
                                }

                                mVelocityTracker.recycle();
                                mVelocityTracker = null;
                            }

                            // if you've barely moved your finger, we treat the velocity as 0
                            // preventing spurious flings due to touch screen jitter
                            final float deltaY = Math.abs(mFinalTouchY - mInitialTouchY);
                            if (deltaY < mFlingGestureMinDistPx
                                    || vel < mFlingExpandMinVelocityPx
                                    ) {
                                vel = 0;
                            }

                            if (negative) {
                                vel = -vel;
                            }

                            if (DEBUG) LOG("gesture: dy=%f vel=(%f,%f) vlinear=%f", deltaY, xVel, yVel, vel);
                             if(deltaY > 0)// add by csc
                                fling(vel, true);
                            requestDisallowInterceptTouchEvent(false);// add by csc

                            /*Begin added by xujia for ios8*/
                            final Intent intent = new Intent("close_frame_offset");

                            mContext.sendBroadcast(intent);

                            //boolean isNoLockdir = Settings.System.getInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, -1) == 1;
                            //Log.d("kay6", "onTouch: panelview isFromFullExpend:" + isFromFullExpend + "  isNoLockdir:" + isNoLockdir);
                            /*End added by xujia*/
				            if(isFromFullExpend){
                                collapse();//added by lzp
                            }
                            break;
                    }
                    return true;
                }});
        }
    }

    public void fling(float vel, boolean always) {
        if (DEBUG) LOG("fling: vel=%.3f, this=%s", vel, this);
        mVel = vel;

        if (always||mVel != 0) {
            animationTick(0); // begin the animation
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mViewName = getResources().getResourceName(getId());
    }

    public String getName() {
        return mViewName;
    }

    @Override
    protected void onViewAdded(View child) {
        if (DEBUG) LOG("onViewAdded: " + child);
    }

    public View getHandle() {
        return mHandleView;
    }

    // Rubberbands the panel to hold its contents.
    /*begin delete by csc*/
    /*@Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (DEBUG) LOG("onMeasure(%d, %d) -> (%d, %d)",
                widthMeasureSpec, heightMeasureSpec, getMeasuredWidth(), getMeasuredHeight());

        // Did one of our children change size?
        int newHeight = getMeasuredHeight();
        if (newHeight != mFullHeight) {
            mFullHeight = newHeight;
            // If the user isn't actively poking us, let's rubberband to the content
            if (!mTracking && !mRubberbanding && !mTimeAnimator.isStarted()
                    && mExpandedHeight > 0 && mExpandedHeight != mFullHeight) {
                mExpandedHeight = mFullHeight;
            }
        }
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    (int) mExpandedHeight, MeasureSpec.AT_MOST); // MeasureSpec.getMode(heightMeasureSpec));
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }*/
    /*end delete by csc*/


    public void setExpandedHeight(float height) {
        if (DEBUG) LOG("setExpandedHeight(%.1f)", height);
        mRubberbanding = false;
        if (mTimeAnimator.isRunning()) {
            post(mStopAnimator);
        }
        setExpandedHeightInternal(height);
        mBar.panelExpansionChanged(PanelView.this, mExpandedFraction);
    }

    @Override
    protected void onLayout (boolean changed, int left, int top, int right, int bottom) {
        if (DEBUG) LOG("onLayout: changed=%s, bottom=%d eh=%d fh=%d", changed?"T":"f", bottom, (int)mExpandedHeight, mFullHeight);
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setExpandedHeightInternal(float h) {
      /*  float fh = getFullHeight();
        if (fh == 0) {
            // Hmm, full height hasn't been computed yet
        }

        if (h < 0) h = 0;
        if (!(mRubberbandingEnabled && (mTracking || mRubberbanding)) && h > fh) h = fh;
        mExpandedHeight = h;

        if (DEBUG) LOG("setExpansion: height=%.1f fh=%.1f tracking=%s rubber=%s", h, fh, mTracking?"T":"f", mRubberbanding?"T":"f");

        requestLayout();
//        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) getLayoutParams();
//        lp.height = (int) mExpandedHeight;
//        setLayoutParams(lp);

        mExpandedFraction = Math.min(1f, (fh == 0) ? 0 : h / fh);*/
    }

    private float getFullHeight() {
        /// M: [SystemUI] Add mIsFirstAnimTick to avoid statusbar can't excute animateExpandNotificationsPanel() rightly when first power on device.
        if (mFullHeight <= 0 || mIsFirstAnimTick) {
            if (DEBUG) LOG("Forcing measure() since fullHeight=" + mFullHeight);
            measure(MeasureSpec.makeMeasureSpec(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, MeasureSpec.EXACTLY));
            mIsFirstAnimTick = false;
        }
        return 1520.0f;//1280.0f;//mod by csc from mFullHeight
    }

    public void setExpandedFraction(float frac) {
        setExpandedHeight(getFullHeight() * frac);
    }

    public float getExpandedHeight() {
        return mExpandedHeight;
    }

    public float getExpandedFraction() {
        return mExpandedFraction;
    }

    public boolean isFullyExpanded() {
	  //Log.v("joyisn","PanelView.isFullyExpanded mExpandedHeight="+mExpandedHeight+" getFullHeight="+getFullHeight());
        //return mExpandedHeight >= getFullHeight();
        return PanelView.this.getY() >= 0;
    }

    public boolean isFullyCollapsed() {
	//Log.v("joyisn","PanelView.isFullyCollapsed mExpandedHeight="+mExpandedHeight);
        //return mExpandedHeight <= 0;
       return PanelView.this.getY() + getFullHeight() <= 0;
    }

    public boolean isCollapsing() {
        return mClosing;
    }

    public void setBar(PanelBar panelBar) {
        mBar = panelBar;
    }

    /// M: [ALPS00583181] Handle on-going touch condition to stop tracking events.
    private void stopTracking() {
        if (!mTracking) {
            return;
        }
        mTracking = false;
        mHandleView.setPressed(false);
        postInvalidate();
        mBar.onTrackingStopped(PanelView.this);
        mVelocityTracker.recycle();
        mVelocityTracker = null;
    }

    public void collapse() {
        // TODO: abort animation or ongoing touch
        if (DEBUG) LOG("collapse: " + this);
        if (!isFullyCollapsed()) {
            mTimeAnimator.cancel();
            mClosing = true;
            // collapse() should never be a rubberband, even if an animation is already running
            mRubberbanding = false;
            fling(-mSelfCollapseVelocityPx, /*always=*/ true);
            /// M: [ALPS00583181] Handle on-going touch condition to stop tracking events.
            stopTracking();
        }
    }

    public void expand() {
        if (DEBUG) LOG("expand: " + this);
        if (isFullyCollapsed()) {
            mBar.startOpeningPanel(this);
            fling(mSelfExpandVelocityPx, /*always=*/ true);
        } else if (DEBUG) {
            if (DEBUG) LOG("skipping expansion: is expanded");
        }
    }
    
    // M: To expand slowly than usual.
    public void expandSlow() {
        LOG("expandSlow: " + this);
        if (isFullyCollapsed()) {
            mBar.startOpeningPanel(this);
            fling(mFlingExpandMinVelocityPx, /*always=*/ true);
        } else {
            LOG("skipping expansion: is expanded");
        }
    }

    /// M: Avoid rotate screen application guide show error @{.
    protected void cancelTimeAnimator() {
        if (mTimeAnimator != null) {
            mTimeAnimator.cancel();
        }
    }
    /// M: Avoid rotate screen application guide show error @}.
}
