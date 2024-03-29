/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.systemui.recent;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.systemui.R;
import com.android.systemui.SwipeHelper;
import com.android.systemui.recent.RecentsPanelView.TaskDescriptionAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class RecentsHorizontalScrollView extends HorizontalScrollView
        implements SwipeHelper.Callback, RecentsPanelView.RecentsScrollView {
    private static final String TAG = RecentsPanelView.TAG;
    private static final boolean DEBUG = RecentsPanelView.DEBUG;
    private LinearLayout mLinearLayout;
    private TaskDescriptionAdapter mAdapter;
    private RecentsCallback mCallback;
    protected int mLastScrollPosition;
    private SwipeHelper mSwipeHelper;
    private RecentsScrollViewPerformanceHelper mPerformanceHelper;
    private HashSet<View> mRecycledViews;
    private int mNumItemsInOneScreenful;
    private int mScrollViewCenter=330; //add by csc
    private int mChildWidth=320; // add by csc
    
    public RecentsHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        float densityScale = getResources().getDisplayMetrics().density;
        float pagingTouchSlop = ViewConfiguration.get(mContext).getScaledPagingTouchSlop();
        mSwipeHelper = new SwipeHelper(SwipeHelper.Y, this, densityScale, pagingTouchSlop);
        mPerformanceHelper = RecentsScrollViewPerformanceHelper.create(context, attrs, this, false);
        mRecycledViews = new HashSet<View>();
    }

    public void setMinSwipeAlpha(float minAlpha) {
        mSwipeHelper.setMinAlpha(minAlpha);
    }

    private int scrollPositionOfMostRecent() {
        //Log.d("kay6", "scrollPositionOfMostRecent: " + (mLinearLayout.getWidth() - getWidth()-200));
        return mLinearLayout.getWidth() - getWidth()-200;// edit by csc
    }

    private void addToRecycledViews(View v) {
        if (mRecycledViews.size() < mNumItemsInOneScreenful) {
            mRecycledViews.add(v);
        }
    }

    public View findViewForTask(int persistentTaskId) {
        for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
            View v = mLinearLayout.getChildAt(i);
            RecentsPanelView.ViewHolder holder = (RecentsPanelView.ViewHolder) v.getTag();
            if (holder.taskDescription.persistentTaskId == persistentTaskId) {
                return v;
            }
        }
        return null;
    }

    private void update() {
        for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
            View v = mLinearLayout.getChildAt(i);
//            v.setScaleY(1f+i*0.1f);
            addToRecycledViews(v);
            mAdapter.recycleView(v);
        }
        LayoutTransition transitioner = getLayoutTransition();
        setLayoutTransition(null);

        mLinearLayout.removeAllViews();
        Iterator<View> recycledViews = mRecycledViews.iterator();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View old = null;
            if (recycledViews.hasNext()) {
                old = recycledViews.next();
                recycledViews.remove();
                old.setVisibility(VISIBLE);
            }

            final View view = mAdapter.getView(i, old, mLinearLayout);

            if (mPerformanceHelper != null) {
                mPerformanceHelper.addViewCallback(view);
            }

            OnTouchListener noOpListener = new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            };

            view.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    mCallback.dismiss();
                }
            });
            // We don't want a click sound when we dimiss recents
            view.setSoundEffectsEnabled(false);

            OnClickListener launchAppListener = new OnClickListener() {
                public void onClick(View v) {
                    mCallback.handleOnClick(view);
                }
            };

            RecentsPanelView.ViewHolder holder = (RecentsPanelView.ViewHolder) view.getTag();
            final View thumbnailView = holder.thumbnailView;
            OnLongClickListener longClickListener = new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    final View anchorView = view.findViewById(R.id.app_description);
                    mCallback.handleLongPress(view, anchorView, thumbnailView);
                    return true;
                }
            };
            thumbnailView.setClickable(true);
            thumbnailView.setOnClickListener(launchAppListener);
            thumbnailView.setOnLongClickListener(longClickListener);

            // We don't want to dismiss recents if a user clicks on the app title
            // (we also don't want to launch the app either, though, because the
            // app title is a small target and doesn't have great click feedback)
            final View appTitle = view.findViewById(R.id.app_label);
            appTitle.setContentDescription(" ");
            appTitle.setOnTouchListener(noOpListener);
            mLinearLayout.addView(view);

        }
        //initTransform();
        startTransform(100);
        Log.d("kay6", "update: startTransform(100);");
        // Scroll to end after initial layout.

        final OnGlobalLayoutListener updateScroll = new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    mLastScrollPosition = scrollPositionOfMostRecent();
                    scrollTo(mLastScrollPosition, 0);
                    final ViewTreeObserver observer = getViewTreeObserver();
                    if (observer.isAlive()) {
                        observer.removeOnGlobalLayoutListener(this);
                    }
                }
            };
        getViewTreeObserver().addOnGlobalLayoutListener(updateScroll);
    }

    @Override
    public void removeViewInLayout(final View view) {
        dismissChild(view);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (DEBUG) Log.v(TAG, "onInterceptTouchEvent()");
        return mSwipeHelper.onInterceptTouchEvent(ev) ||
            super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return mSwipeHelper.onTouchEvent(ev) ||
            super.onTouchEvent(ev);
    }
    																					
    public boolean canChildBeDismissed(View v) {
        return true;
    }

    public void dismissChild(View v) {
        mSwipeHelper.dismissChild(v, 0);
    }

    public void onChildDismissed(View v) {
        addToRecycledViews(v);
        mLinearLayout.removeView(v);
        mCallback.handleSwipe(v);
        // Restore the alpha/translation parameters to what they were before swiping
        // (for when these items are recycled)
        View contentView = getChildContentView(v);
        contentView.setAlpha(1f);
        contentView.setTranslationY(0);
    }

    public void onBeginDrag(View v) {
        // We do this so the underlying ScrollView knows that it won't get
        // the chance to intercept events anymore
        requestDisallowInterceptTouchEvent(true);
    }

    public void onDragCancelled(View v) {
    }

// edit by csc
    public View getChildAtPosition(MotionEvent ev) {
        final float x = ev.getX() ;//+ getScrollX()
        final float y = ev.getY() + getScrollY();
        List<View> viewList = new ArrayList<View>();
       int COUNT = 0;
        for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
            View item = mLinearLayout.getChildAt(i);
           
            int[] location = new  int[2] ;
            item.getLocationInWindow(location);
            if (x >= location[0]) {
//                return item;
                viewList.add(item);
                COUNT++;
            }
        }
        if(COUNT>0){
            int max = viewList.get(0).getLeft();
            int count=0;
            for(int i=0;i<viewList.size();i++){
                if (max < viewList.get(i).getLeft())
                {
                    max = viewList.get(i).getLeft();
                    count = i;
                }
            }

            return viewList.get(count);
        }else{
        	return null;
        }
    }

    public View getChildContentView(View v) {
        return v.findViewById(R.id.recent_item);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (mPerformanceHelper != null) {
            /// M: [ALPS00554282] Don't draw fading edge when doing transition in screen.
            LayoutTransition transition = mLinearLayout.getLayoutTransition();
            if (transition != null 
                && transition.isRunning() 
                && (mLinearLayout.getChildCount() < mNumItemsInOneScreenful)) {
                return;
            }

            int paddingLeft = mPaddingLeft;
            final boolean offsetRequired = isPaddingOffsetRequired();
            if (offsetRequired) {
                paddingLeft += getLeftPaddingOffset();
            }

            int left = mScrollX + paddingLeft;
            int right = left + mRight - mLeft - mPaddingRight - paddingLeft;
            int top = mScrollY + getFadeTop(offsetRequired);
            int bottom = top + getFadeHeight(offsetRequired);
            if (offsetRequired) {
                right += getRightPaddingOffset();
                bottom += getBottomPaddingOffset();
            }
           /* mPerformanceHelper.drawCallback(canvas,
                    left, right, top, bottom, mScrollX, mScrollY,
                    0, 0,
                    getLeftFadingEdgeStrength(), getRightFadingEdgeStrength());*/
        }
    }

    @Override
    public int getVerticalFadingEdgeLength() {
        if (mPerformanceHelper != null) {
            return mPerformanceHelper.getVerticalFadingEdgeLengthCallback();
        } else {
            return super.getVerticalFadingEdgeLength();
        }
    }

    @Override
    public int getHorizontalFadingEdgeLength() {
        if (mPerformanceHelper != null) {
            return mPerformanceHelper.getHorizontalFadingEdgeLengthCallback();
        } else {
            return super.getHorizontalFadingEdgeLength();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setScrollbarFadingEnabled(true);
        mLinearLayout = (LinearLayout) findViewById(R.id.recents_linear_layout);
        final int leftPadding = mContext.getResources()
            .getDimensionPixelOffset(R.dimen.status_bar_recents_thumbnail_left_margin);
        setOverScrollEffectPadding(leftPadding, 0);
    }

    @Override
    public void onAttachedToWindow() {
        if (mPerformanceHelper != null) {
            mPerformanceHelper.onAttachedToWindowCallback(
                    mCallback, mLinearLayout, isHardwareAccelerated());
        }
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        float densityScale = getResources().getDisplayMetrics().density;
        mSwipeHelper.setDensityScale(densityScale);
        float pagingTouchSlop = ViewConfiguration.get(mContext).getScaledPagingTouchSlop();
        mSwipeHelper.setPagingTouchSlop(pagingTouchSlop);
    }

    private void setOverScrollEffectPadding(int leftPadding, int i) {
        // TODO Add to (Vertical)ScrollView
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Skip this work if a transition is running; it sets the scroll values independently
        // and should not have those animated values clobbered by this logic
        //Log.d("kay6", "onSizeChanged: w:" + w +" h:" + h +" oldw:" + oldw + " oldh:"+oldh);
        LayoutTransition transition = mLinearLayout.getLayoutTransition();
        if (transition != null && transition.isRunning()) {
            return;
        }
        // Keep track of the last visible item in the list so we can restore it
        // to the bottom when the orientation changes.
        mLastScrollPosition = scrollPositionOfMostRecent();

        // This has to happen post-layout, so run it "in the future"
        post(new Runnable() {
            public void run() {
                // Make sure we're still not clobbering the transition-set values, since this
                // runnable launches asynchronously
                LayoutTransition transition = mLinearLayout.getLayoutTransition();
                if (transition == null || !transition.isRunning()) {
                    scrollTo(mLastScrollPosition, 0);
                }
            }
        });
    }
	  /*
     * add by csc
     * */
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    	startTransform(l);
        super.onScrollChanged(l,t,oldl,oldt);
    }

    @java.lang.Override
    protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {
        return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);
    }

    private void initTransform(){
        int count = mLinearLayout.getChildCount();
        Log.d("kay6", "initTransform: count:" + count);
        View child;
        for (int i = 0; i < count; i++) {

            child = mLinearLayout.getChildAt(i);

            int[] location = new  int[2] ;
            child.getLocationInWindow(location);
            Log.d("kay6", "initTransform: location:"+location[0] + "  " + location[1]);
            //child.setScaleY(1f+(float)location[0]/5400);
            child.setScaleY(1f+0.03f*i);
           /* int childCenter = getCenterOfChildView(child);
            int delta = Math.abs(childCenter - mScrollViewCenter);
            Log.d("kay6", "startTransform: location[0]:" + location[0] + " childCenter:" + childCenter + " delta:"+delta + "  mChildWidth:"+mChildWidth);
            float scaleFactor = 1.0f;
            if (delta > mChildWidth) {
                scaleFactor = 0.3f;
            } else {
                scaleFactor = 0.3f + 0.7f * ((float)(mChildWidth - delta)/mChildWidth);
            }*/
            float scaleFactor = 0.3f + i*0.03f;
            child.setTranslationX(-scaleFactor*300+250);//320 -> 300  196 -> 300
            //child.setTranslationX(-scaleFactor*300+250);//320 -> 300  196 -> 250
            child.setPivotX(child.getWidth() / 2);
            child.setPivotY(child.getHeight() / 2);
        }
    }
    /*
         * add by csc
         * */
    private void startTransform(int l) {
    	int countTT = (l-55)/170;
    	
        int count = mLinearLayout.getChildCount();
        Log.d("kay6", "startTransform: count:" + count);
        RelativeLayout v = (RelativeLayout)findViewById(R.id.recent_item);
        View child;
        for (int i = 0; i < count; i++) {

             child = mLinearLayout.getChildAt(i);

             int[] location1 = new  int[2] ;
             child.getLocationOnScreen(location1);
            Log.d("kay6", "startTransform:location1: " + location1[0] + "," + location1[1]);
             int[] location = new  int[2] ;
             child.getLocationInWindow(location);

             //child.setScaleY(1f+(float)location[0]/5400);
            child.setScaleY(1f+0.03f*i);
            int childCenter = getCenterOfChildView(child);
            int delta = Math.abs(childCenter - mScrollViewCenter);
            Log.d("kay6", "startTransform: location[0]:" + location[0] + " childCenter:" + childCenter + " delta:"+delta + "  mChildWidth:"+mChildWidth);
            float scaleFactor = 1.0f;
            if (delta > mChildWidth) {
                scaleFactor = 0.3f;
            } else {
                scaleFactor = 0.3f + 0.7f * ((float)(mChildWidth - delta)/mChildWidth);
            }

            //child.setTranslationX(0.1f*i*300+250);//320 -> 300  196 -> 300
            child.setTranslationX(-scaleFactor*300+250);//320 -> 300  196 -> 250
            child.setPivotX(child.getWidth() / 2);
            child.setPivotY(child.getHeight() / 2);
        }
    }
	/*
     * add by csc
     * */
    private int getCenterOfChildView(View view) {
        int left = view.getLeft();
        int scrollX = this.getScrollX() ;
        //Log.d("kay6", "getCenterOfChildView: left:" + left +" scrollX:"+scrollX + " view.getScrollX():" + view.getScrollX());
        //Log.d("kay6", "getCenterOfChildView: view.getWidth():" + view.getWidth() +" center:"+(left - scrollX + view.getWidth() / 2));
        return left - scrollX + view.getWidth() / 2;
    }
    public void setAdapter(TaskDescriptionAdapter adapter) {
        mAdapter = adapter;
        mAdapter.registerDataSetObserver(new DataSetObserver() {
            public void onChanged() {
                update();
                Log.d(TAG, " setAdapter onChanged: ");
            }

            public void onInvalidated() {
                update();
                Log.d("kay6", "setAdapter onInvalidated: ");
            }
        });
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int childWidthMeasureSpec =
                MeasureSpec.makeMeasureSpec(dm.widthPixels, MeasureSpec.AT_MOST);
        int childheightMeasureSpec =
                MeasureSpec.makeMeasureSpec(dm.heightPixels, MeasureSpec.AT_MOST);
        View child = mAdapter.createView(mLinearLayout);
        child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        mNumItemsInOneScreenful =
                (int) FloatMath.ceil(dm.widthPixels / (float) child.getMeasuredWidth());
        addToRecycledViews(child);

        //Log.d("kay6", "setAdapter: mNumItemsInOneScreenful:" + mNumItemsInOneScreenful);
        for (int i = 0; i < mNumItemsInOneScreenful - 1; i++) {
            addToRecycledViews(mAdapter.createView(mLinearLayout));
        }
    }

    public int numItemsInOneScreenful() {
        Log.d("kay6", "numItemsInOneScreenful: " + mNumItemsInOneScreenful);
        return mNumItemsInOneScreenful;
    }

    @Override
    public void setLayoutTransition(LayoutTransition transition) {
        // The layout transition applies to our embedded LinearLayout
        mLinearLayout.setLayoutTransition(transition);
    }

    public void setCallback(RecentsCallback callback) {
        mCallback = callback;
    }
}
