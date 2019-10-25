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

import android.animation.Animator;
import android.animation.LayoutTransition;
import android.animation.TimeInterpolator;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.tablet.StatusBarPanel;
import com.android.systemui.statusbar.tablet.TabletStatusBar;

import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import android.graphics.BitmapFactory;
import java.io.FileNotFoundException;
import java.io.IOException;
//add by xujia for calendar 20130904
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.graphics.Typeface;
import java.util.Date;
import java.util.Calendar;
import android.graphics.Canvas;
import android.graphics.Paint;
//end by xujia for calendar 20130904
import android.view.KeyEvent;
import com.android.systemui.recent.RecentCircleLayout;


public class RecentsPanelView extends FrameLayout implements OnItemClickListener, RecentsCallback,
        StatusBarPanel, Animator.AnimatorListener {
    static final String TAG = "RecentsPanelView";
    static final boolean DEBUG = TabletStatusBar.DEBUG || PhoneStatusBar.DEBUG || false;
    private PopupMenu mPopup;
    private View mRecentsScrim;
    private View mRecentsNoApps;
    private ViewGroup mRecentsContainer;
    private StatusBarTouchProxy mStatusBarTouchProxy;

    private boolean mShowing;
    private boolean mWaitingToShow;
    private ViewHolder mItemToAnimateInWhenWindowAnimationIsFinished;
    private boolean mAnimateIconOfFirstTask;
    private boolean mWaitingForWindowAnimation;
    private long mWindowAnimationStartTime;

    private RecentTasksLoader mRecentTasksLoader;
    private ArrayList<TaskDescription> mRecentTaskDescriptions;
    private TaskDescriptionAdapter mListAdapter;
    private int mThumbnailWidth;
    private boolean mFitThumbnailToXY;
    private int mRecentItemLayoutId;
    private boolean mHighEndGfx;

    //add by zhengqingshui for calendar 20120511
	public static int mCalendarWeakTextSize;
	public static int mCalendarWeakTextTop;
	public static int mCalendarDateTextSize;
	public static int mCalendarDateTextTop;	
//end by zhengqingshui for calendar 20120511  

    public static interface RecentsScrollView {
        public int numItemsInOneScreenful();
        public void setAdapter(TaskDescriptionAdapter adapter);
        public void setCallback(RecentsCallback callback);
        public void setMinSwipeAlpha(float minAlpha);
        public View findViewForTask(int persistentTaskId);
    }

    private final class OnLongClickDelegate implements View.OnLongClickListener {
        View mOtherView;
        OnLongClickDelegate(View other) {
            mOtherView = other;
        }
        public boolean onLongClick(View v) {
            return mOtherView.performLongClick();
        }
    }

    /* package */ final static class ViewHolder {
        View thumbnailView; //
        ImageView thumbnailViewImage;
        Bitmap thumbnailViewImageBitmap;
        ImageView iconView;
        TextView labelView;
        TextView descriptionView;
        View calloutLine;
        TaskDescription taskDescription;
        boolean loadedThumbnailAndIcon;
    }

    /* package */ final class TaskDescriptionAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public TaskDescriptionAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
        }

        public int getCount() {
            return mRecentTaskDescriptions != null ? mRecentTaskDescriptions.size() : 0;
        }

        public Object getItem(int position) {
            return position; // we only need the index
        }

        public long getItemId(int position) {
            return position; // we just need something unique for this position
        }

        public View createView(ViewGroup parent) {
            View convertView = mInflater.inflate(mRecentItemLayoutId, parent, false);//
            ViewHolder holder = new ViewHolder();
            holder.thumbnailView = convertView.findViewById(R.id.app_thumbnail);
            holder.thumbnailViewImage =
                    (ImageView) convertView.findViewById(R.id.app_thumbnail_image);
            // If we set the default thumbnail now, we avoid an onLayout when we update
            // the thumbnail later (if they both have the same dimensions)
            updateThumbnail(holder, mRecentTasksLoader.getDefaultThumbnail(), false, false);
            holder.iconView = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.iconView.setImageBitmap(mRecentTasksLoader.getDefaultIcon());
            holder.labelView = (TextView) convertView.findViewById(R.id.app_label);
            holder.calloutLine = convertView.findViewById(R.id.recents_callout_line);
            holder.descriptionView = (TextView) convertView.findViewById(R.id.app_description);
            // begin add by csc
            if(Settings.System.getInt(mContext.getContentResolver(), 
    				Settings.System.WALLPAPER_NEED_WHITE_THEME,
    				0) == 1){
            	holder.labelView.setTextColor(0xFFFFFFFF);
            }else{
            	holder.labelView.setTextColor(0xFF000000);
            }
            // end add by csc
            convertView.setTag(holder);
            return convertView;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = createView(parent);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            // index is reverse since most recent appears at the bottom...
            final int index = mRecentTaskDescriptions.size() - position - 1;

            final TaskDescription td = mRecentTaskDescriptions.get(index);

            holder.labelView.setText(td.getLabel());
            holder.thumbnailView.setContentDescription(td.getLabel());
            holder.loadedThumbnailAndIcon = td.isLoaded();
            if (td.isLoaded()) {
                updateThumbnail(holder, td.getThumbnail(), true, false);
                updateIcon(holder, td.getIcon(), true, false,td);
            }
            if (index == 0) {
                if (mAnimateIconOfFirstTask) {
                    if (mItemToAnimateInWhenWindowAnimationIsFinished != null) {
                        holder.iconView.setAlpha(1f);
                        holder.iconView.setTranslationX(0f);
                        holder.iconView.setTranslationY(0f);
                        holder.labelView.setAlpha(1f);
                        holder.labelView.setTranslationX(0f);
                        holder.labelView.setTranslationY(0f);
                        if (holder.calloutLine != null) {
                            holder.calloutLine.setAlpha(1f);
                            holder.calloutLine.setTranslationX(0f);
                            holder.calloutLine.setTranslationY(0f);
                        }
                    }
                    mItemToAnimateInWhenWindowAnimationIsFinished = holder;
                    final int translation = -getResources().getDimensionPixelSize(
                            R.dimen.status_bar_recents_app_icon_translate_distance);
                    final Configuration config = getResources().getConfiguration();
                    if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        holder.iconView.setAlpha(0f);
                        holder.iconView.setTranslationX(translation);
                        holder.labelView.setAlpha(0f);
                        holder.labelView.setTranslationX(translation);
                        holder.calloutLine.setAlpha(0f);
                        holder.calloutLine.setTranslationX(translation);
                    } else {
                        holder.iconView.setAlpha(0f);
                        holder.iconView.setTranslationY(translation);
                    }
                    if (!mWaitingForWindowAnimation) {
                        animateInIconOfFirstTask();
                    }
                }
            }

            holder.thumbnailView.setTag(td);
            holder.thumbnailView.setOnLongClickListener(new OnLongClickDelegate(convertView));
            holder.taskDescription = td;
            return convertView;
        }

        public void recycleView(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            updateThumbnail(holder, mRecentTasksLoader.getDefaultThumbnail(), false, false);
            holder.iconView.setImageBitmap(mRecentTasksLoader.getDefaultIcon());
            holder.iconView.setVisibility(INVISIBLE);
            holder.iconView.animate().cancel();
            holder.labelView.setText(null);
            holder.labelView.animate().cancel();
            holder.thumbnailView.setContentDescription(null);
            holder.thumbnailView.setTag(null);
            holder.thumbnailView.setOnLongClickListener(null);
            holder.thumbnailView.setVisibility(INVISIBLE);
            holder.iconView.setAlpha(1f);
            holder.iconView.setTranslationX(0f);
            holder.iconView.setTranslationY(0f);
            holder.labelView.setAlpha(1f);
            holder.labelView.setTranslationX(0f);
            holder.labelView.setTranslationY(0f);
            if (holder.calloutLine != null) {
                holder.calloutLine.setAlpha(1f);
                holder.calloutLine.setTranslationX(0f);
                holder.calloutLine.setTranslationY(0f);
                holder.calloutLine.animate().cancel();
            }
            holder.taskDescription = null;
            holder.loadedThumbnailAndIcon = false;
        }
    }

    public RecentsPanelView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentsPanelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        updateValuesFromResources();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RecentsPanelView,
                defStyle, 0);

        mRecentItemLayoutId = a.getResourceId(R.styleable.RecentsPanelView_recentItemLayout, 0);
        mRecentTasksLoader = RecentTasksLoader.getInstance(context);
        a.recycle();
    }

    public int numItemsInOneScreenful() {
        if (mRecentsContainer instanceof RecentsScrollView){
            RecentsScrollView scrollView
                    = (RecentsScrollView) mRecentsContainer;
            return scrollView.numItemsInOneScreenful();
        }  else {
            throw new IllegalArgumentException("missing Recents[Horizontal]ScrollView");
        }
    }

    private boolean pointInside(int x, int y, View v) {
        final int l = v.getLeft();
        final int r = v.getRight();
        final int t = v.getTop();
        final int b = v.getBottom();
        return x >= l && x < r && y >= t && y < b;
    }

    public boolean isInContentArea(int x, int y) {
        if (pointInside(x, y, mRecentsContainer)) {
            return true;
        } else if (mStatusBarTouchProxy != null &&
                pointInside(x, y, mStatusBarTouchProxy)) {
            return true;
        } else {
            return false;
        }
    }

    public void show(boolean show) {
        show(show, null, false, false);
    }

    public void show(boolean show, ArrayList<TaskDescription> recentTaskDescriptions,
            boolean firstScreenful, boolean animateIconOfFirstTask) {
        mAnimateIconOfFirstTask = animateIconOfFirstTask;
        mWaitingForWindowAnimation = animateIconOfFirstTask;
        if (show) {
            mWaitingToShow = true;
            refreshRecentTasksList(recentTaskDescriptions, firstScreenful);
            showIfReady();
        } else {
            showImpl(false);
        }
    }

    private void showIfReady() {
        // mWaitingToShow => there was a touch up on the recents button
        // mRecentTaskDescriptions != null => we've created views for the first screenful of items
        if (mWaitingToShow && mRecentTaskDescriptions != null) {
            showImpl(true);
        }
    }

    static void sendCloseSystemWindows(Context context, String reason) {
        //Log.d("kay5", "recentat sendCloseSystemWindows: ");
        //Log.d("kay5", Log.getStackTraceString(new Throwable()));
        if (ActivityManagerNative.isSystemReady()) {
            /// M: [ALPS00592271] Send close system window when keyguard is not shown.
            boolean isKeyguardShowing = false;
            try {
                IWindowManager mWm = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
                if (mWm != null) {
                    isKeyguardShowing = mWm.isKeyguardLocked();
                }
            } catch (RemoteException e) {

            }

            try {
                if (!isKeyguardShowing) {
                   // Log.d("kay5", "sendCloseSystemWindows: closeSystemDialogs reason:" + reason);
                    ActivityManagerNative.getDefault().closeSystemDialogs(reason);
                }
            } catch (RemoteException e) {
            }
        }
    }

    private void showImpl(boolean show) {
        //Log.d("kay5", "showImpl: " + show);
        sendCloseSystemWindows(mContext, BaseStatusBar.SYSTEM_DIALOG_REASON_RECENT_APPS);

        mShowing = show;

        if (show) {
            // if there are no apps, bring up a "No recent apps" message
            boolean noApps = mRecentTaskDescriptions != null
                    && (mRecentTaskDescriptions.size() == 0);
            /// M: Support theme for ThemeManager @{
            final ImageView noRecentTextView = (ImageView) mRecentsNoApps
                    .findViewById(R.id.recents_no_apps_text);
            Xlog.d(TAG, "show: mRecentsNoApps = " + mRecentsNoApps
                    + ",noRecentTextView = " + noRecentTextView);
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                final int themeMainColor = mContext.getResources().getThemeMainColor();
                if (themeMainColor != 0) {
                   // noRecentTextView.setTextColor(themeMainColor);
                }
            }
            /// M: Support theme for ThemeManager @}
            mRecentsNoApps.setAlpha(1f);
            mRecentsNoApps.setVisibility(noApps ? View.VISIBLE : View.INVISIBLE);
            if(noApps)
                {
                    Log.d("kay5", "showImpl: noApps:" + noApps);
                BitmapDrawable logoBK = getLoacalBitmap(getContext(),"/data/data/com.hskj.hometest/launcher.png");
                System.out.println("recentspanelView: "+(logoBK==null));
                
                          //BitmapDrawable logoDrawable=new BitmapDrawable(logoBK);
                         noRecentTextView.setImageDrawable(logoBK);
                            
                     
                
                }
            onAnimationEnd(null);
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
        } else {
            mWaitingToShow = false;
            // call onAnimationEnd() and clearRecentTasksList() in onUiHidden()
            if (mPopup != null) {
                mPopup.dismiss();
            }
        }
    }

    public void onUiHidden() {
        if (!mShowing && mRecentTaskDescriptions != null) {
            onAnimationEnd(null);
            clearRecentTasksList();
        }
        /// M: [ALPS00557795] Dismiss popup window on ui hidden condition.
        if (mPopup != null) {
            mPopup.dismiss();
        }
    }

    public void dismiss() {
        ((RecentsActivity) mContext).dismissAndGoHome();
    }

    public void dismissAndGoBack() {
        ((RecentsActivity) mContext).dismissAndGoBack();
    }

    public void onAnimationCancel(Animator animation) {
    }

    public void onAnimationEnd(Animator animation) {
        if (mShowing) {
            final LayoutTransition transitioner = new LayoutTransition();
            ((ViewGroup)mRecentsContainer).setLayoutTransition(transitioner);
            createCustomAnimations(transitioner);
        } else {
            ((ViewGroup)mRecentsContainer).setLayoutTransition(null);
        }
    }

    public void onAnimationRepeat(Animator animation) {
    }

    public void onAnimationStart(Animator animation) {
    }

    @Override
    public boolean dispatchHoverEvent(MotionEvent event) {
        // Ignore hover events outside of this panel bounds since such events
        // generate spurious accessibility events with the panel content when
        // tapping outside of it, thus confusing the user.
        final int x = (int) event.getX();
        final int y = (int) event.getY();
        if (x >= 0 && x < getWidth() && y >= 0 && y < getHeight()) {
            return super.dispatchHoverEvent(event);
        }
        return true;
    }

    /**
     * Whether the panel is showing, or, if it's animating, whether it will be
     * when the animation is done.
     */
    public boolean isShowing() {
        return mShowing;
    }

    public void setStatusBarView(View statusBarView) {
        if (mStatusBarTouchProxy != null) {
            mStatusBarTouchProxy.setStatusBar(statusBarView);
        }
    }

    public void setRecentTasksLoader(RecentTasksLoader loader) {
        mRecentTasksLoader = loader;
    }

    public void updateValuesFromResources() {
                    //add by xujia for calendar 20130904
        	mCalendarWeakTextSize = (int) getResources().getInteger(R.integer.zzzz_calendar_weak_text_size);
        	mCalendarWeakTextTop = (int) getResources().getDimension(R.dimen.zzzz_calerdar_weak_text_top);
        	mCalendarDateTextSize = (int) getResources().getInteger(R.integer.zzzz_calendar_weak_date_size);
        	mCalendarDateTextTop = (int) getResources().getDimension(R.dimen.zzzz_calerdar_weak_date_top);		
            //end by xujia for calendar 20130904
        final Resources res = mContext.getResources();
        mThumbnailWidth = Math.round(res.getDimension(R.dimen.status_bar_recents_thumbnail_width));
        mFitThumbnailToXY = res.getBoolean(R.bool.config_recents_thumbnail_image_fits_to_xy);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mRecentsContainer = (ViewGroup) findViewById(R.id.recents_container);
        mStatusBarTouchProxy = (StatusBarTouchProxy) findViewById(R.id.status_bar_touch_proxy);
        mListAdapter = new TaskDescriptionAdapter(mContext);
        if (mRecentsContainer instanceof RecentsScrollView){
            RecentsScrollView scrollView
                    = (RecentsScrollView) mRecentsContainer;
            scrollView.setAdapter(mListAdapter);
            scrollView.setCallback(this);
        } else {
            throw new IllegalArgumentException("missing Recents[Horizontal]ScrollView");
        }

        mRecentsScrim = findViewById(R.id.recents_bg_protect);
        mRecentsNoApps = findViewById(R.id.recents_no_apps);

        if (mRecentsScrim != null) {
            mHighEndGfx = false;//ActivityManager.isHighEndGfx();
            if (!mHighEndGfx) {
                mRecentsScrim.setBackground(null);
            } else if (mRecentsScrim.getBackground() instanceof BitmapDrawable) {
                // In order to save space, we make the background texture repeat in the Y direction
                ((BitmapDrawable) mRecentsScrim.getBackground()).setTileModeY(TileMode.REPEAT);
            }
        }
    }

    public void setMinSwipeAlpha(float minAlpha) {
        if (mRecentsContainer instanceof RecentsScrollView){
            RecentsScrollView scrollView
                = (RecentsScrollView) mRecentsContainer;
            scrollView.setMinSwipeAlpha(minAlpha);
        }
    }

    private void createCustomAnimations(LayoutTransition transitioner) {
        transitioner.setDuration(200);
        transitioner.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, null);
    }

    private void updateIcon(ViewHolder h, Drawable icon, boolean show, boolean anim,TaskDescription td) {
       //add by xujia for caledar 20130904
       if(td!=null)
        {
            if(td.packageName.equals("com.android.calendar")){
			    	final String date = String.valueOf(getLauncherDayOfMonth());
			    	final String week = DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_WEEKDAY);				
				icon = createCalendarIcon(date, week, icon, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
	}
        }
			//end by xujia for calendar 20130904
        if (icon != null) {
            h.iconView.setImageDrawable(icon);
            if (show && h.iconView.getVisibility() != View.VISIBLE) {
                if (anim) {
                    h.iconView.setAnimation(
                            AnimationUtils.loadAnimation(mContext, R.anim.recent_appear));
                }
                h.iconView.setVisibility(View.VISIBLE);
            }
        }
    }

    private Bitmap changeToRoundedCornerBitmap(Bitmap bitmap, float corners) {
        try {
            BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            final Paint paint = new Paint();
            final Rect rect = new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight());
            final RectF rectF = new RectF(new Rect(0, 0, bitmap.getWidth(),
                    bitmap.getHeight()));

            paint.setAntiAlias(true);
            paint.setShader(bitmapShader);
            //canvas.drawARGB(0, 0, 0, 0);
            //paint.setColor(Color.BLACK);
            canvas.drawRoundRect(rectF, corners, corners, paint);
            /*paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

            final Rect src = new Rect(1, 1, bitmap.getWidth() - 2,
                    bitmap.getHeight() - 2);

            canvas.drawBitmap(bitmap, src, rect, paint);*/
            return output;
        } catch (Exception e) {
            return bitmap;
        }
    }

    private void updateThumbnail(ViewHolder h, Bitmap thumbnail, boolean show, boolean anim) {
        if (thumbnail != null) {
            // Should remove the default image in the frame
            // that this now covers, to improve scrolling speed.
            // That can't be done until the anim is complete though.
            Bitmap tmpThumnail = changeToRoundedCornerBitmap(thumbnail,20.0f);
            h.thumbnailViewImage.setImageBitmap(tmpThumnail); //thumbnail -> tmpThumnail

            // scale the image to fill the full width of the ImageView. do this only if
            // we haven't set a bitmap before, or if the bitmap size has changed
            //begin del by kay
        /*    if (h.thumbnailViewImageBitmap == null ||
                h.thumbnailViewImageBitmap.getWidth() != thumbnail.getWidth() ||
                h.thumbnailViewImageBitmap.getHeight() != thumbnail.getHeight()) {
                if (mFitThumbnailToXY) {
                    h.thumbnailViewImage.setScaleType(ScaleType.FIT_XY);
                } else {
                    Matrix scaleMatrix = new Matrix();
                    float scale = mThumbnailWidth / (float) thumbnail.getWidth();
                    scaleMatrix.setScale(scale+0.4f, scale+0.45f);// edit by csc
                    h.thumbnailViewImage.setScaleType(ScaleType.MATRIX);
                    h.thumbnailViewImage.setImageMatrix(scaleMatrix);
                }
            }*/
            //end del by kay
            if (show && h.thumbnailView.getVisibility() != View.VISIBLE) {
                if (anim) {
                    h.thumbnailView.setAnimation(
                            AnimationUtils.loadAnimation(mContext, R.anim.recent_appear));
                }
                h.thumbnailView.setVisibility(View.VISIBLE);
            }
            h.thumbnailViewImageBitmap = tmpThumnail;//thumbnail -> tmpThumnail;
        }
    }

    void onTaskThumbnailLoaded(TaskDescription td) {
        synchronized (td) {
            if (mRecentsContainer != null) {
                ViewGroup container = mRecentsContainer;
                if (container instanceof RecentsScrollView) {
                    container = (ViewGroup) container.findViewById(
                            R.id.recents_linear_layout);
                }
                // Look for a view showing this thumbnail, to update.
                for (int i=0; i < container.getChildCount(); i++) {
                    View v = container.getChildAt(i);
                    if (v.getTag() instanceof ViewHolder) {
                        ViewHolder h = (ViewHolder)v.getTag();
                        if (!h.loadedThumbnailAndIcon && h.taskDescription == td) {
                            // only fade in the thumbnail if recents is already visible-- we
                            // show it immediately otherwise
                            //boolean animateShow = mShowing &&
                            //    mRecentsContainer.getAlpha() > ViewConfiguration.ALPHA_THRESHOLD;
                            boolean animateShow = false;
                            updateIcon(h, td.getIcon(), true, animateShow,td);
                            updateThumbnail(h, td.getThumbnail(), true, animateShow);
                            h.loadedThumbnailAndIcon = true;
                        }
                    }
                }
            }
        }
        showIfReady();
    }

    private void animateInIconOfFirstTask() {
        if (mItemToAnimateInWhenWindowAnimationIsFinished != null &&
                !mRecentTasksLoader.isFirstScreenful()) {
            int timeSinceWindowAnimation =
                    (int) (System.currentTimeMillis() - mWindowAnimationStartTime);
            final int minStartDelay = 150;
            final int startDelay = Math.max(0, Math.min(
                    minStartDelay - timeSinceWindowAnimation, minStartDelay));
            final int duration = 250;
            final ViewHolder holder = mItemToAnimateInWhenWindowAnimationIsFinished;
            final TimeInterpolator cubic = new DecelerateInterpolator(1.5f);
            for (View v :
                new View[] { holder.iconView, holder.labelView, holder.calloutLine }) {
                if (v != null) {
                    v.animate().translationX(0).translationY(0).alpha(1f).setStartDelay(startDelay)
                            .setDuration(duration).setInterpolator(cubic);
                }
            }
            mItemToAnimateInWhenWindowAnimationIsFinished = null;
            mAnimateIconOfFirstTask = false;
        }
    }

    public void onWindowAnimationStart() {
        mWaitingForWindowAnimation = false;
        mWindowAnimationStartTime = System.currentTimeMillis();
        animateInIconOfFirstTask();
    }

    public void clearRecentTasksList() {
        // Clear memory used by screenshots
        if (mRecentTaskDescriptions != null) {
            mRecentTasksLoader.cancelLoadingThumbnailsAndIcons(this);
            onTaskLoadingCancelled();
        }
    }

    public void onTaskLoadingCancelled() {
        // Gets called by RecentTasksLoader when it's cancelled
        if (mRecentTaskDescriptions != null) {
            mRecentTaskDescriptions = null;
            mListAdapter.notifyDataSetInvalidated();
        }
    }

    public void refreshViews() {
        mListAdapter.notifyDataSetInvalidated();
        updateUiElements();
        showIfReady();
    }

    /// M: [SystemUI][ALPS00444338]When a call come in, will clear recent tasks, but when resume, doesn't reload tasks. @{
    public void refreshRecentTasks() {
        if (mRecentTaskDescriptions == null) {
            Log.d("kay6", "refreshRecentTasks()");
            refreshRecentTasksList();
        }
    }
    /// M: [SystemUI][ALPS00444338]When a call come in, will clear recent tasks, but when resume, doesn't reload tasks. @}

    public void refreshRecentTasksList() {
        refreshRecentTasksList(null, false);
    }

    private void refreshRecentTasksList(
         ArrayList<TaskDescription> recentTasksList, boolean firstScreenful) {
            if (mRecentTaskDescriptions == null && recentTasksList != null) {
                onTasksLoaded(recentTasksList, firstScreenful);
            } else {
                mRecentTasksLoader.loadTasksInBackground();
            }
        }

    public void onTasksLoaded(ArrayList<TaskDescription> tasks, boolean firstScreenful) {
        if (mRecentTaskDescriptions == null) {
            mRecentTaskDescriptions = new ArrayList<TaskDescription>(tasks);
        } else {
            mRecentTaskDescriptions.addAll(tasks);
        }
        if (((RecentsActivity) mContext).isActivityShowing()) {
            refreshViews();
        }
    }

    private void updateUiElements() {
        final int items = mRecentTaskDescriptions != null
                ? mRecentTaskDescriptions.size() : 0;

        mRecentsContainer.setVisibility(items > 0 ? View.VISIBLE : View.GONE);

        // Set description for accessibility
        int numRecentApps = mRecentTaskDescriptions != null
                ? mRecentTaskDescriptions.size() : 0;
        String recentAppsAccessibilityDescription;
        if (numRecentApps == 0) {
            recentAppsAccessibilityDescription =
                getResources().getString(R.string.status_bar_no_recent_apps);
        } else {
            recentAppsAccessibilityDescription = getResources().getQuantityString(
                R.plurals.status_bar_accessibility_recent_apps, numRecentApps, numRecentApps);
        }
        setContentDescription(recentAppsAccessibilityDescription);
    }

    public boolean simulateClick(int persistentTaskId) {
        if (mRecentsContainer instanceof RecentsScrollView){
            RecentsScrollView scrollView
                = (RecentsScrollView) mRecentsContainer;
            View v = scrollView.findViewForTask(persistentTaskId);
            if (v != null) {
                handleOnClick(v);
                return true;
            }
        }
        return false;
    }

    public void handleOnClick(View view) {
        Log.d("kay6", "handleOnClick: ");
        ViewHolder holder = (ViewHolder)view.getTag();
        TaskDescription ad = holder.taskDescription;
        final Context context = view.getContext();
        final ActivityManager am = (ActivityManager)
                context.getSystemService(Context.ACTIVITY_SERVICE);
        Bitmap bm = holder.thumbnailViewImageBitmap;
        boolean usingDrawingCache;
        if (bm.getWidth() == holder.thumbnailViewImage.getWidth() &&
                bm.getHeight() == holder.thumbnailViewImage.getHeight()) {
            usingDrawingCache = false;
        } else {
            holder.thumbnailViewImage.setDrawingCacheEnabled(true);
            bm = holder.thumbnailViewImage.getDrawingCache();
            usingDrawingCache = true;
        }
        Bundle opts = (bm == null) ?
                null :
                ActivityOptions.makeThumbnailScaleUpAnimation(
                        holder.thumbnailViewImage, bm, 0, 0, null).toBundle();

        show(false);
        if (ad.taskId >= 0) {
            // This is an active task; it should just go to the foreground.
            am.moveTaskToFront(ad.taskId, ActivityManager.MOVE_TASK_WITH_HOME,
                    opts);
        } else {
            Intent intent = ad.intent;
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY
                    | Intent.FLAG_ACTIVITY_TASK_ON_HOME
                    | Intent.FLAG_ACTIVITY_NEW_TASK);
            if (DEBUG) Log.v(TAG, "Starting activity " + intent);
            context.startActivityAsUser(intent, opts,
                    new UserHandle(UserHandle.USER_CURRENT));
        }
        if (usingDrawingCache) {
            holder.thumbnailViewImage.setDrawingCacheEnabled(false);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        handleOnClick(view);
    }

	private static final String MUSIC_PACKAGE_NAME = "com.android.music";
    public void handleSwipe(View view) {
        TaskDescription ad = ((ViewHolder) view.getTag()).taskDescription;
        if (ad == null || mRecentTaskDescriptions==null) {
            Log.v(TAG, "Not able to find activity description for swiped task; view=" + view +
                    " tag=" + view.getTag());
            Log.d("hjz","mRecentTaskDescriptions is"+mRecentTaskDescriptions+"ad="+ad);
            return;
        }
        Log.d("kay6", "handleSwipe: ");
        if (DEBUG) Log.v(TAG, "Jettison " + ad.getLabel());
        //Log.d("hjz",""+mRecentTaskDescriptions);
        if(mRecentTaskDescriptions==null){
            Log.d("hjz","is null"+mRecentTaskDescriptions);
        }
        mRecentTaskDescriptions.remove(ad);
        mRecentTasksLoader.remove(ad);

        // Handled by widget containers to enable LayoutTransitions properly
        // mListAdapter.notifyDataSetChanged();

        if (mRecentTaskDescriptions.size() == 0) {
            Log.d("kay6", "handleSwipe: dismissAndGoBack()");
            //dismissAndGoBack();  //del by kay
            dismiss();  //add by kay
        }

        // Currently, either direction means the same thing, so ignore direction and remove
        // the task.
        final ActivityManager am = (ActivityManager)
                mContext.getSystemService(Context.ACTIVITY_SERVICE);
        if (am != null) {
            am.removeTask(ad.persistentTaskId, ActivityManager.REMOVE_TASK_KILL_PROCESS);

            // Accessibility feedback
            setContentDescription(
                    mContext.getString(R.string.accessibility_recents_item_dismissed, ad.getLabel()));
            sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
            setContentDescription(null);

			//added by wang 20130911 start
			if(ad.packageName.equals(MUSIC_PACKAGE_NAME)) {
				sendMediaButtonClick(KeyEvent.KEYCODE_MEDIA_PAUSE);
			}
			//added by wang 20130911 end
        }
    }


//added by wang 20130911 start
    private void sendMediaButtonClick(int keyCode) {
        // use the registered PendingIntent that will be processed by the registered
        //    media button event receiver, which is the component of mClientIntent
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        getContext().sendOrderedBroadcast(intent, null);

        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        getContext().sendOrderedBroadcast(intent, null);
    }
//added by wang 20130911 end
















    private void startApplicationDetailsActivity(String packageName) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", packageName, null));
        intent.setComponent(intent.resolveActivity(mContext.getPackageManager()));
        TaskStackBuilder.create(getContext())
                .addNextIntentWithParentStack(intent).startActivities();
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mPopup != null) {
            return true;
        } else {
            return super.onInterceptTouchEvent(ev);
        }
    }

    public BitmapDrawable getLoacalBitmap(Context context,String url) 
    {
        /*   try 
        {
             FileInputStream fis = new FileInputStream(url);
             Bitmap bitmap=BitmapFactory.decodeStream(fis); 
             System.out.println("getlocalBitmap "+(bitmap==null));
             return bitmap;

        } 
        catch (FileNotFoundException e) 
        {
             e.printStackTrace();
             return null;
        }*/
        	BitmapDrawable bitmapDrawable = new BitmapDrawable(context.getResources(), url);
      return bitmapDrawable;
   }
//add by xujia 20130904
     private Drawable createCalendarIcon(String date, String week, Drawable background, int width, int height) {
    	
    	final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    	final Canvas canvas = new Canvas(bitmap);
    	background.setBounds(0, 0, width, height);
    	background.draw(canvas);
    	
    	final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	paint.setColor(0xffff0000);
    	float textSize = TypedValue.applyDimension(
    			TypedValue.COMPLEX_UNIT_SP, mCalendarWeakTextSize, getResources().getDisplayMetrics());
    	paint.setTextSize(textSize);
    	float textWidth = paint.measureText(week);
    	canvas.drawText(week, width/2 - textWidth/2, mCalendarWeakTextTop, paint);
    	
    	paint.setColor(0xff000000);
    	textSize = TypedValue.applyDimension(
    			TypedValue.COMPLEX_UNIT_SP, mCalendarDateTextSize, getResources().getDisplayMetrics());
    	paint.setTextSize(textSize);
	paint.setTypeface(Typeface.createFromFile("/system/fonts/PhonepadTwo.ttf"));
    	//paint.setTypeface(Typeface.DEFAULT_BOLD);
    	textWidth = paint.measureText(date);
    	canvas.drawText(date, width/2 - textWidth/2, mCalendarDateTextTop, paint);
    	return new BitmapDrawable(getResources(), bitmap);
    }

    public static int getLauncherDayOfMonth() {
    	final Calendar calendar = Calendar.getInstance();   
    	calendar.setTime(new Date());  
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
//end by xujia 20130904
    public void handleLongPress(
            final View selectedView, final View anchorView, final View thumbnailView) {
        thumbnailView.setSelected(true);
        final PopupMenu popup =
            new PopupMenu(mContext, anchorView == null ? selectedView : anchorView);
        mPopup = popup;
        popup.getMenuInflater().inflate(R.menu.recent_popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.recent_remove_item) {
                    mRecentsContainer.removeViewInLayout(selectedView);
                } else if (item.getItemId() == R.id.recent_inspect_item) {
                    ViewHolder viewHolder = (ViewHolder) selectedView.getTag();
                    if (viewHolder != null) {
                        final TaskDescription ad = viewHolder.taskDescription;
                        startApplicationDetailsActivity(ad.packageName);
                        show(false);
                    } else {
                        throw new IllegalStateException("Oops, no tag on view " + selectedView);
                    }
                } else {
                    return false;
                }
                return true;
            }
        });
        popup.setOnDismissListener(new PopupMenu.OnDismissListener() {
            public void onDismiss(PopupMenu menu) {
                thumbnailView.setSelected(false);
                mPopup = null;
            }
        });
        popup.show();
    }
}
