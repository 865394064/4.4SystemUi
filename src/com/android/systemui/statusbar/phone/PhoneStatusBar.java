/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.InputMethodService;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.storage.StorageVolume;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.provider.Telephony;
import android.service.dreams.DreamService;
import android.service.dreams.IDreamManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

//import com.android.internal.policy.impl.keyguard.ClockView;

import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.statusbar.StatusBarNotification;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.util.Objects;
import com.android.systemui.R;
import com.android.systemui.recent.User;
import com.android.systemui.slider.CalendarView;
import com.android.systemui.slider.Event;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.CommandQueue;
import com.android.systemui.statusbar.GestureRecorder;
import com.android.systemui.statusbar.NotificationData;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.SignalClusterView;
import com.android.systemui.statusbar.SignalClusterViewGemini;
import com.android.systemui.statusbar.StatusBarIconView;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.DateView;
import com.android.systemui.statusbar.policy.IntruderAlertView;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkControllerGemini;
import com.android.systemui.statusbar.policy.NotificationRowLayout;
import com.android.systemui.statusbar.policy.OnSizeChangedListener;
import com.android.systemui.statusbar.policy.Prefs;
import com.android.systemui.statusbar.policy.TelephonyIcons;
import com.android.systemui.statusbar.toolbar.ToolBarIndicator;
import com.android.systemui.statusbar.toolbar.ToolBarView;
import com.android.systemui.statusbar.util.SIMHelper;
import com.android.systemui.unreadevents.MissCallUnReadObserver;
import com.android.systemui.unreadevents.MmsUnReadObserver;
import com.hskj.iphone.view.BitmapUtils;
//import com.hskj.iphone.view.BottomPanel;
import com.android.systemui.BottomPanel;
import com.android.systemui.statusbar.phone.BottomPanelBar;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.systemui.ext.PluginFactory;
import com.mediatek.telephony.SimInfoManager;
import com.mediatek.xlog.Xlog;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
//added by wang 20130725 start
//added by wang 20130725 end
//BEGIN added by xujia
//END added by xujia
/*Begin added by xujia 20131111*/
/*End added by xujia 20131111*/
/**Begin: added by xujia**/
/**End: added by xujia **/

//add by yuhuizhong xss
import android.view.ViewTreeObserver;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.view.Surface;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.hardware.display.DisplayManager;
import android.os.SystemProperties;
import android.app.NotificationManager;
import com.android.systemui.statusbar.phone.MyViewPager;
import android.telephony.TelephonyManager;//addeed by xss for back to phone
import android.telephony.PhoneStateListener;//addeed by xss for back to phone
import android.os.AsyncTask;//added by xss for ios10 forceTouch
import java.util.Calendar;//added by xss for ios10 IphoneLunar 20170214
import com.hskj.iphone.view.IphoneLunar;//added by xss for ios10 IphoneLunar 20170214

//import SQLite.Exception;


public class PhoneStatusBar extends BaseStatusBar implements View.OnTouchListener, BottomPanelBar.ControlCenterCallback{
    static final String TAG = "PhoneStatusBar";
    public static final boolean DEBUG = BaseStatusBar.DEBUG;
    public static final boolean SPEW = DEBUG;
    public static final boolean DUMPTRUCK = true; // extra dumpsys info
    public static final boolean DEBUG_GESTURES = false;

    public static final boolean DEBUG_CLINGS = false;

    public static final boolean ENABLE_NOTIFICATION_PANEL_CLING = false;

    public static final boolean SETTINGS_DRAG_SHORTCUT = true;

    // additional instrumentation for testing purposes; intended to be left on during development
    public static final boolean CHATTY = DEBUG;

    public static final String ACTION_STATUSBAR_START
            = "com.android.internal.policy.statusbar.START";

    /// M: Support AirplaneMode for Statusbar SimIndicator.
    private static final String ACTION_BOOT_IPO
            = "android.intent.action.ACTION_PREBOOT_IPO";
    /// M: [SystemUI] Dismiss new event icon when click clear button for keyguard.
    private static final String CLEAR_NEW_EVENT_VIEW_INTENT = "android.intent.action.KEYGUARD_CLEAR_UREAD_TIPS";

    private static final int MSG_OPEN_NOTIFICATION_PANEL = 1000;
    private static final int MSG_CLOSE_PANELS = 1001;
    private static final int MSG_OPEN_SETTINGS_PANEL = 1002;
    // 1020-1030 reserved for BaseStatusBar
    /// M: [SystemUI] Support "SIM indicator". @{
    private static final int MSG_SHOW_INTRUDER = 1003;
    private static final int MSG_HIDE_INTRUDER = 1004;
    /// @}

    // will likely move to a resource or other tunable param at some point
    private static final int INTRUDER_ALERT_DECAY_MS = 0; // disabled, was 10000;

    private static final boolean CLOSE_PANEL_WHEN_EMPTIED = true;

    /// M: [ALPS00512845] Handle SD Swap Condition.
    private static final boolean SUPPORT_SD_SWAP = true;

    private static final int NOTIFICATION_PRIORITY_MULTIPLIER = 10; // see NotificationManagerService
    private static final int HIDE_ICONS_BELOW_SCORE = Notification.PRIORITY_LOW * NOTIFICATION_PRIORITY_MULTIPLIER;

    // fling gesture tuning parameters, scaled to display density
    private float mSelfExpandVelocityPx; // classic value: 2000px/s
    private float mSelfCollapseVelocityPx; // classic value: 2000px/s (will be negated to collapse "up")
    private float mFlingExpandMinVelocityPx; // classic value: 200px/s
    private float mFlingCollapseMinVelocityPx; // classic value: 200px/s
    private float mCollapseMinDisplayFraction; // classic value: 0.08 (25px/min(320px,480px) on G1)
    private float mExpandMinDisplayFraction; // classic value: 0.5 (drag open halfway to expand)
    private float mFlingGestureMaxXVelocityPx; // classic value: 150px/s

    private float mExpandAccelPx; // classic value: 2000px/s/s
    private float mCollapseAccelPx; // classic value: 2000px/s/s (will be negated to collapse "up")

    private float mFlingGestureMaxOutputVelocityPx; // how fast can it really go? (should be a little 
                                                    // faster than mSelfCollapseVelocityPx)

    PhoneStatusBarPolicy mIconPolicy,mIconPolicy1;

    // These are no longer handled by the policy, because we need custom strategies for them
    BluetoothController mBluetoothController;
    BatteryController mBatteryController,mBatteryController1;
    LocationController mLocationController;
    NetworkController mNetworkController,mNetworkController1;
    int mNaturalBarHeight = -1;
    int mIconSize = -1;
    int mIconHPadding = -1;
    Display mDisplay;
    Point mCurrentDisplaySize = new Point();

    IDreamManager mDreamManager;

    StatusBarWindowView mStatusBarWindow;
    PhoneStatusBarView mStatusBarView;

    int mPixelFormat;
    Object mQueueLock = new Object();

    // viewgroup containing the normal contents of the statusbar
    RelativeLayout mStatusBarContents;//changed by wang 20130725 from 'LinearLayout' to 'RelativeLayout'

    // right-hand icons
    LinearLayout mSystemIconArea,mSystemIconArea1;

    // left-hand icons 
    LinearLayout mStatusIcons,mStatusIcons1;
    // the icons themselves
    /// M: Support "SIM Indicator".
    private ImageView mSimIndicatorIcon;
    IconMerger mNotificationIcons,mNotificationIcons1;
    // [+>
    View mMoreIcon;

    // expanded notifications
    NotificationPanelView mNotificationPanel; // the sliding/resizing panel within the notification window
    public ScrollView mScrollView;
    View mExpandedContents;
    int mNotificationPanelGravity;
    int mNotificationPanelMarginBottomPx, mNotificationPanelMarginPx;
    float mNotificationPanelMinHeightFrac;
    boolean mNotificationPanelIsFullScreenWidth;
    TextView mNotificationPanelDebugText;

    // settings
    QuickSettings mQS;
    public boolean mHasSettingsPanel, mHasFlipSettings;
    SettingsPanelView mSettingsPanel;
    public View mFlipSettingsView;
    QuickSettingsContainerView mSettingsContainer;
    int mSettingsPanelGravity;

    // top bar
    View mNotificationPanelHeader;
    View mDateTimeView;
    View mClearButton;
    ImageView mSettingsButton, mNotificationButton;
    /// M: [SystemUI] Remove settings button to notification header.
    private View mHeaderSettingsButton;

    // carrier/wifi label
    private TextView mCarrierLabel;
    private boolean mCarrierLabelVisible = false;
    private int mCarrierLabelHeight;
    /// M: Calculate ToolBar height when sim indicator is showing.
    private int mToolBarViewHeight;
    private TextView mEmergencyCallLabel;
    private int mNotificationHeaderHeight;

    private boolean mShowCarrierInPanel = false;

    // position
    int[] mPositionTmp = new int[2];
    boolean mExpandedVisible;
    View header;
    // the date view
    DateView mDateView;

    // for immersive activities
    private IntruderAlertView mIntruderAlertView;

    // on-screen navigation buttons
    private NavigationBarView mNavigationBarView = null;

    // the tracker view
    int mTrackingPosition; // the position of the top of the tracking view.

    // ticker
    private Ticker mTicker;
    private View mTickerView;
    private boolean mTicking;

    // Tracking finger for opening/closing.
    int mEdgeBorder; // corresponds to R.dimen.status_bar_edge_ignore
    boolean mTracking;
    VelocityTracker mVelocityTracker;

    // help screen
    private boolean mClingShown;
    private ViewGroup mCling;
    private boolean mSuppressStatusBarDrags; // while a cling is up, briefly deaden the bar to give things time to settle

    boolean mAnimating;
    boolean mClosing; // only valid when mAnimating; indicates the initial acceleration
    float mAnimY;
    float mAnimVel;
    float mAnimAccel;
    long mAnimLastTimeNanos;
    boolean mAnimatingReveal = false;
    int mViewDelta;
    float mFlingVelocity;
    int mFlingY;
    int[] mAbsPos = new int[2];
    Runnable mPostCollapseCleanup = null;

    private Animator mLightsOutAnimation;
    private Animator mLightsOnAnimation;
    /// M: Support "Change font size of phone".
    private float mPreviousConfigFontScale;

    // for disabling the status bar
    int mDisabled = 0;

    // tracking calls to View.setSystemUiVisibility()
    int mSystemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE;

    DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @{
    boolean mNeedRelayout = false;
    private int mPrevioutConfigOrientation;
    /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @}

    // XXX: gesture research
    private final GestureRecorder mGestureRec = DEBUG_GESTURES
        ? new GestureRecorder("/sdcard/statusbar_gestures.dat")
        : null;

    private int mNavigationIconHints = 0;
    private final Animator.AnimatorListener mMakeIconsInvisible = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            // double-check to avoid races
            if (mStatusBarContents.getAlpha() == 0) {
                if (DEBUG) Slog.d(TAG, "makeIconsInvisible");
                mStatusBarContents.setVisibility(View.INVISIBLE);
            }
        }
    };

    // ensure quick settings is disabled until the current user makes it through the setup wizard
    private boolean mUserSetup = false;
    private ContentObserver mUserSetupObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            final boolean userSetup = 0 != Settings.Secure.getIntForUser(
                    mContext.getContentResolver(),
                    Settings.Secure.USER_SETUP_COMPLETE,
                    0 /*default */,
                    mCurrentUserId);
            if (MULTIUSER_DEBUG) Slog.d(TAG, String.format("User setup changed: " +
                    "selfChange=%s userSetup=%s mUserSetup=%s",
                    selfChange, userSetup, mUserSetup));
            if (mSettingsButton != null && mHasFlipSettings) {
                mSettingsButton.setVisibility(userSetup ? View.VISIBLE : View.INVISIBLE);
            }
            if (mSettingsPanel != null) {
                mSettingsPanel.setEnabled(userSetup);
            }
            if (userSetup != mUserSetup) {
                mUserSetup = userSetup;
                if (!mUserSetup && mStatusBarView != null)
                    animateCollapseQuickSettings();
            }
        }
    };

    @Override
    public void start() {
        mDisplay = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();

        mDreamManager = IDreamManager.Stub.asInterface(
                ServiceManager.checkService(DreamService.DREAM_SERVICE));

        super.start(); // calls createAndAddWindows()

        addNavigationBar();

        if (ENABLE_INTRUDERS) addIntruderView();

        // Lastly, call to the icon policy to install/update all the icons.
        mIconPolicy = new PhoneStatusBarPolicy(mContext);
    }

    // ================================================================================
    // Constructing the view
    // ================================================================================
    ImageView deleteAllBtn;// add by csc on 20161008
    TextView deleteAllTv;// add by csc on 20161008
    TextView noNotificationText;
    TextView unReadTv;
    public DownClock mClockView;
    RelativeLayout deleteLin; //add by csc
    LinearLayout allStatusbarLayout ;
    LinearLayout bottomSettingPanel ;
    private boolean isNoNotification = false; // add by csc on 20161007
    protected PhoneStatusBarView makeStatusBarView() {
        Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::makeStatusBarView");
        final Context context = mContext;
        /// M: Support "Change font size of phone".
        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        mPreviousConfigFontScale = config.fontScale;
        mPrevioutConfigOrientation = config.orientation;
        updateDisplaySize(); // populates mDisplayMetrics
        loadDimens();

        /// M: Support AirplaneMode for Statusbar SimIndicator.
        updateAirplaneMode();

        mIconSize = res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_icon_size);

        /*begin modified by xujia for iphone 20140115*/
        /// M: [SystemUI] Support "Dual SIM". {
        Log.d("hjz","MTK_GEMINI_SUPPORT="+FeatureOption.MTK_GEMINI_SUPPORT);
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mStatusBarWindow = (StatusBarWindowView)View.inflate(context, R.layout.gemini_super_status_bar, null);
            //mStatusBarWindow = (StatusBarWindowView) View.inflate(context, R.layout.super_status_bar, null);
        } else {
            mStatusBarWindow = (StatusBarWindowView) View.inflate(context, R.layout.super_status_bar, null);
        }
        /// M: [SystemUI] Support "Dual SIM". }
        mStatusBarView = (PhoneStatusBarView) mStatusBarWindow.findViewById(R.id.status_bar);

       /* if (DEBUG) {
            mStatusBarWindow.setBackgroundColor(0x6000FF80);
        }*/
        mStatusBarWindow.mService = this;
        mStatusBarWindow.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (mExpandedVisible && !mAnimating) {
                        animateCollapsePanels();
                    }
                }
                return mStatusBarWindow.onTouchEvent(event);
            }});

        mStatusBarView.setBar(this);


        PanelHolder holder = (PanelHolder) mStatusBarWindow.findViewById(R.id.panel_holder);
        mStatusBarView.setPanelHolder(holder);
        mNotificationPanel = (NotificationPanelView) mStatusBarWindow.findViewById(R.id.notification_panel);
        mNotificationPanel.setStatusBar(this);
        mNotificationPanelIsFullScreenWidth =
            (mNotificationPanel.getLayoutParams().width == ViewGroup.LayoutParams.MATCH_PARENT);

        // make the header non-responsive to clicks
        mNotificationPanel.findViewById(R.id.header).setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true; // e eats everything
                    }
                });
        /// M: [ALPS00352181] When ActivityManager.isHighEndGfx(mDisplay) return true, the dialog
        /// will show error, it will has StatusBar windowBackground.
        mStatusBarWindow.setBackground(null);

        /*begin modified by xujia for A18232+8*/
        //if (!ActivityManager.isHighEndGfx()) {
 		if(isLiveWallpaperNow()){//adde by xujia for set Live wallpaper
            setNotificationBg();
        }else {
        	mUpdateHandler.postDelayed(mResetNotificationPanelBg, 100);
        }
        //}
         /*end modified by xujia for A18232+8*/
        if (ENABLE_INTRUDERS) {
            mIntruderAlertView = (IntruderAlertView) View.inflate(context, R.layout.intruder_alert, null);
            mIntruderAlertView.setVisibility(View.GONE);
            mIntruderAlertView.setBar(this);
        }
        if (MULTIUSER_DEBUG) {
            mNotificationPanelDebugText = (TextView) mNotificationPanel.findViewById(R.id.header_debug_info);
            mNotificationPanelDebugText.setVisibility(View.VISIBLE);
        }

        updateShowSearchHoldoff();

        try {
            boolean showNav = mWindowManagerService.hasNavigationBar();
            if (DEBUG) Slog.v(TAG, "hasNavigationBar=" + showNav);
            if (showNav) {
                mNavigationBarView =
                    (NavigationBarView) View.inflate(context, R.layout.navigation_bar, null);

                mNavigationBarView.setDisabledFlags(mDisabled);
                mNavigationBarView.setBar(this);
            }
        } catch (RemoteException ex) {
            // no window manager? good luck with that
        }

        // figure out which pixel-format to use for the status bar.
        mPixelFormat = PixelFormat.OPAQUE;

        mSystemIconArea = (LinearLayout) mStatusBarView.findViewById(R.id.system_icon_area);
        mStatusIcons = (LinearLayout)mStatusBarView.findViewById(R.id.statusIcons);
        mNotificationIcons = (IconMerger)mStatusBarView.findViewById(R.id.notificationIcons);
        mNotificationIcons.setOverflowIndicator(mMoreIcon);
        mStatusBarContents = (RelativeLayout)mStatusBarView.findViewById(R.id.status_bar_contents);
        mTickerView = mStatusBarView.findViewById(R.id.ticker);
        /// M: For AT&T
        if (!FeatureOption.MTK_GEMINI_SUPPORT &&
                !PluginFactory.getStatusBarPlugin(mContext)
                .isHspaDataDistinguishable() &&
                !PluginFactory.getStatusBarPlugin(context)
                .supportDataTypeAlwaysDisplayWhileOn()) {
           // mPlmnLabel = (TextView) mStatusBarView.findViewById(R.id.att_plmn);
        }
        /// M: [SystemUI] Support "Notification toolbar". {
        mToolBarSwitchPanel = mStatusBarWindow.findViewById(R.id.toolBarSwitchPanel);
        mToolBarView = (ToolBarView) mStatusBarWindow.findViewById(R.id.tool_bar_view);
        ToolBarIndicator indicator = (ToolBarIndicator) mStatusBarWindow.findViewById(R.id.indicator);
        mToolBarView.setStatusBarService(this);
        mToolBarView.setToolBarSwitchPanel(mToolBarSwitchPanel);
        mToolBarView.setScrollToScreenCallback(indicator);
        mToolBarView.setToolBarIndicator(indicator);
        mToolBarView.hideSimSwithPanel();
        mToolBarView.moveToDefaultScreen(false);
        /// M: [SystemUI] Support "Notification toolbar". }

        /// M: [SystemUI] Support "SIM indicator". {
        mSimIndicatorIcon = (ImageView) mStatusBarView.findViewById(R.id.sim_indicator_internet_or_alwaysask);
        /// M: [SystemUI] Support "SIM indicator". }


            /*delete by xujia*/
        /*mPile = (NotificationRowLayout)mStatusBarWindow.findViewById(R.id.latestItems);
        mPile.setLayoutTransitionsEnabled(false);
        mPile.setLongPressListener(getNotificationLongClicker());
        mExpandedContents = mPile; // was: expanded.findViewById(R.id.notificationLinearLayout);*/

        mNotificationPanelHeader = mStatusBarWindow.findViewById(R.id.header);//hjz
		/*Begin added by xujia for ios8*/
		  mNotificationPanelHeader.setOnTouchListener(new View.OnTouchListener() {
	                @Override
	                public boolean onTouch(View v, MotionEvent event) {
	                		android.util.Log.d("xujia111","event=="+event.getRawY()+event.getAction());
		if(event.getAction()==MotionEvent.ACTION_UP){

			android.util.Log.d("xujia111","smallFrame_offset=="+(Settings.System.getInt(mContext.getContentResolver(),
		            Settings.System.SMALL_FRAME_OFFSET_GUEST, -1)));
			if(Settings.System.getInt(mContext.getContentResolver(),
		            Settings.System.SMALL_FRAME_OFFSET_GUEST, -1)==1){

					final Intent intent = new Intent("close_frame_offset");

	        			    mContext.sendBroadcast(intent);
					}
			}
					return true;
	                	}

		  	});
   	/*End added by xujia*/

        mClearButton = mStatusBarWindow.findViewById(R.id.clear_all_button);
        mClearButton.setOnClickListener(mClearButtonListener);
        mClearButton.setAlpha(0f);
        mClearButton.setVisibility(View.GONE);
        mClearButton.setEnabled(false);
        mDateView = (DateView)mStatusBarWindow.findViewById(R.id.date);

        mHasSettingsPanel = res.getBoolean(R.bool.config_hasSettingsPanel);
        mHasFlipSettings = res.getBoolean(R.bool.config_hasFlipSettingsPanel);

        mDateTimeView = mNotificationPanelHeader.findViewById(R.id.datetime);
        if (mHasFlipSettings) {
            mDateTimeView.setOnClickListener(mClockClickListener);
            mDateTimeView.setEnabled(true);
        }

        mSettingsButton = (ImageView) mStatusBarWindow.findViewById(R.id.settings_button);
        if (mSettingsButton != null) {
            mSettingsButton.setOnClickListener(mSettingsButtonListener);
            if (mHasSettingsPanel) {
                /// M: [SystemUI] Remove settings button to notification header @{.
                mHeaderSettingsButton = mStatusBarWindow.findViewById(R.id.header_settings_button);
                mHeaderSettingsButton.setOnClickListener(mHeaderSettingsButtonListener);
                /// M: [SystemUI] Remove settings button to notification header @}.
                if (mStatusBarView.hasFullWidthNotifications()) {
                    // the settings panel is hiding behind this button
                    mSettingsButton.setImageResource(R.drawable.ic_notify_quicksettings);
                    mSettingsButton.setVisibility(View.VISIBLE);
                } else {
                    // there is a settings panel, but it's on the other side of the (large) screen
                    final View buttonHolder = mStatusBarWindow.findViewById(
                            R.id.settings_button_holder);
                    if (buttonHolder != null) {
                        buttonHolder.setVisibility(View.GONE);
                    }
                }
            } else {
                // no settings panel, go straight to settings
                mSettingsButton.setVisibility(View.VISIBLE);
                mSettingsButton.setImageResource(R.drawable.ic_notify_settings);
            }
        }
        if (mHasFlipSettings) {
            mNotificationButton = (ImageView) mStatusBarWindow.findViewById(R.id.notification_button);
            if (mNotificationButton != null) {
                mNotificationButton.setOnClickListener(mNotificationButtonListener);
            }
        }

        mScrollView = (ScrollView)mStatusBarWindow.findViewById(R.id.scroll);
        mScrollView.setVerticalScrollBarEnabled(false); // less drawing during pulldowns
        if (!mNotificationPanelIsFullScreenWidth) {
            mScrollView.setSystemUiVisibility(
                    View.STATUS_BAR_DISABLE_NOTIFICATION_TICKER |
                    View.STATUS_BAR_DISABLE_NOTIFICATION_ICONS |
                    View.STATUS_BAR_DISABLE_CLOCK);
        }

        mTicker = new MyTicker(context, mStatusBarView);

        TickerView tickerView = (TickerView)mStatusBarView.findViewById(R.id.tickerText);
        tickerView.mTicker = mTicker;

        mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);

        // set the inital view visibility
        setAreThereNotifications();


        /*begin: added by xujia 20131111*/

         initViewPager();
        mBatteryController1 = new BatteryController(mContext);
        mBatteryController1.addIconView((ImageView)mNotificationPanelHeader.findViewById(R.id.battery));
        mBatteryController1.addLabelView((TextView) mNotificationPanelHeader.findViewById(R.id.percentage));
		mBatteryController1.setChargingView((ImageView) mNotificationPanelHeader.findViewById(R.id.charging));

        MyTicker mTicker1 = new MyTicker(context, mNotificationPanelHeader);

        TickerView tickerView1 = (TickerView)mNotificationPanelHeader.findViewById(R.id.tickerText);
        tickerView1.mTicker = mTicker1;


		/**begin: mod by lzp **/
        final   PhoneStatusBarView   mStatusBarView1 = (PhoneStatusBarView) mNotificationPanelHeader.findViewById(R.id.status_bar);
	    mNetworkController1 = new NetworkController(mContext);
	    mNetworkController1.setNeedChangeColor(false);
        final SignalClusterView signalCluster1 =(SignalClusterView)mStatusBarView1.findViewById(R.id.signal_cluster);



        signalCluster1.setPosType(SignalClusterView.SIGNAL_POSTYPE_TOP);
		/**End: mod by lzp **/
        mNetworkController1.addSignalCluster(signalCluster1);
        signalCluster1.setNetworkController(mNetworkController1);

        mSystemIconArea1= (LinearLayout) mNotificationPanelHeader.findViewById(R.id.system_icon_area);
        mStatusIcons1= (LinearLayout)mNotificationPanelHeader.findViewById(R.id.statusIcons);
        mNotificationIcons1 = (IconMerger)mNotificationPanelHeader.findViewById(R.id.notificationIcons);
        mNotificationIcons1.setOverflowIndicator(mMoreIcon);

         mPile = (NotificationRowLayout)(listViews.get(1)).findViewById(R.id.latestItems);
        mPile.setLayoutTransitionsEnabled(false);
        mPile.setLongPressListener(getNotificationLongClicker());
        mPile.setViewPager(viewPager);//added by xujia for scroll
        mExpandedContents = mPile; // was: expanded.findViewById(R.id.notificationLinearLayout);*/
            
        /*end by xujia 20131111*/

        deleteLin = (RelativeLayout)listViews.get(1).findViewById(R.id.deleteLin);
        deleteAllBtn = (ImageView)listViews.get(1).findViewById(R.id.delete_all_btn);
        deleteAllTv = (TextView)listViews.get(1).findViewById(R.id.delete_all_tv);// add by csc on 20161008
        noNotificationText = (TextView)allStatusbarLayout.findViewById(R.id.no_notification);
        mClockView=(DownClock)allStatusbarLayout.findViewById(R.id.clock_view);//hjz
        unReadTv = (TextView)listViews.get(1).findViewById(R.id.unread_notification_text);
        if(deleteAllBtn!=null)
            deleteAllBtn.setOnClickListener( new OnClickListener() {// mod by csc on 20161008

                @Override
                public void onClick( View v )
                {
                    deleteAllBtn.setVisibility(View.GONE);
                    deleteAllTv.setVisibility(View.VISIBLE);
                }
            });

        if(deleteAllTv!=null)
            deleteAllTv.setOnClickListener(new OnClickListener() {// add by csc on 20161008
                @Override
                public void onClick(View view) {
                    Intent i = new Intent("DELETE_ALL_NOTIFICATION");
                    mContext.sendBroadcast(i);
                    deleteLin.setVisibility(View.GONE);
                    noNotificationText.setVisibility(View.VISIBLE);
                    isNoNotification=true;// add by csc on 20161007
                }
            });
        // end add by csc


        // Other icons
        mLocationController = new LocationController(mContext); // will post a notification
        mBatteryController = new BatteryController(mContext);
        mBatteryController.addIconView((ImageView)mStatusBarView.findViewById(R.id.battery));
        mBatteryController.addLabelView((TextView) mStatusBarWindow.findViewById(R.id.percentage));
		mBatteryController.setChargingView((ImageView) mStatusBarView.findViewById(R.id.charging));


        mBluetoothController = new BluetoothController(mContext);
		landsview=  mStatusBarView.findViewById(R.id.landsView);
		clockpaddingView=mStatusBarView.findViewById(R.id.clockView);//added by xujia
        /// M: [SystemUI] Support "Dual SIM". {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            /// M: Support GeminiPlus
            mCarrier1 = (CarrierLabelGemini) mStatusBarWindow.findViewById(R.id.carrier1);
            mCarrier2 = (CarrierLabelGemini) mStatusBarWindow.findViewById(R.id.carrier2);
            mCarrier3 = (CarrierLabelGemini) mStatusBarWindow.findViewById(R.id.carrier3);
            mCarrier4 = (CarrierLabelGemini) mStatusBarWindow.findViewById(R.id.carrier4);
            mCarrierDivider = mStatusBarWindow.findViewById(R.id.carrier_divider);
            mCarrierDivider2 = mStatusBarWindow.findViewById(R.id.carrier_divider2);
            mCarrierDivider3 = mStatusBarWindow.findViewById(R.id.carrier_divider3);
            mCarrierLabelGemini = (LinearLayout) mStatusBarWindow.findViewById(R.id.carrier_label_gemini);
            mShowCarrierInPanel = (mCarrierLabelGemini != null);
            if (mShowCarrierInPanel) {
                mCarrier1.setSlotId(PhoneConstants.GEMINI_SIM_1);
                mCarrier2.setSlotId(PhoneConstants.GEMINI_SIM_2);
                mCarrier3.setSlotId(PhoneConstants.GEMINI_SIM_3);
                mCarrier4.setSlotId(PhoneConstants.GEMINI_SIM_4);
            }
        } else {
           // mCarrierLabel = (TextView)mStatusBarWindow.findViewById(R.id.carrier_label);
            mShowCarrierInPanel = (mCarrierLabel != null);
        }
        Log.d("hjz","signalCluster= "+PhoneConstants.GEMINI_SIM_NUM+"shaungka="+FeatureOption.MTK_GEMINI_SUPPORT);
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mNetworkControllerGemini = new NetworkController(mContext);
            final SignalClusterView signalCluster =
                (SignalClusterView) mStatusBarView.findViewById(R.id.signal_cluster);
            mNetworkControllerGemini.addSignalCluster(signalCluster);
            signalCluster.setNetworkController(mNetworkControllerGemini);
            /// M: Support GeminiPlus
            if(PhoneConstants.GEMINI_SIM_NUM == 2) {
              //  mNetworkControllerGemini.setCarrierGemini(mCarrier1, mCarrier2, mCarrierDivider);
            } else if(PhoneConstants.GEMINI_SIM_NUM == 3) {
             //   mNetworkControllerGemini.setCarrierGemini(mCarrier1, mCarrier2, mCarrier3, mCarrierDivider, mCarrierDivider2);
            } else if(PhoneConstants.GEMINI_SIM_NUM == 4) {
              //  mNetworkControllerGemini.setCarrierGemini(mCarrier1, mCarrier2, mCarrier3, mCarrier4, mCarrierDivider, mCarrierDivider2, mCarrierDivider3);
            }
        } else {
            mNetworkController = new NetworkController(mContext);
            final SignalClusterView signalCluster =
                (SignalClusterView)mStatusBarView.findViewById(R.id.signal_cluster);
            mNetworkController.addSignalCluster(signalCluster);
            signalCluster.setNetworkController(mNetworkController);
            signalCluster.setPosType(SignalClusterView.SIGNAL_POSTYPE_TOP);
        }
        /// M: [SystemUI] Support "Dual SIM". }

        if (!FeatureOption.MTK_GEMINI_SUPPORT) {
            mEmergencyCallLabel = (TextView)mStatusBarWindow.findViewById(R.id.emergency_calls_only);
            if (mEmergencyCallLabel != null) {
                mNetworkController.addEmergencyLabelView(mEmergencyCallLabel);
                mEmergencyCallLabel.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) { }});
                mEmergencyCallLabel.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom,
                            int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        updateCarrierLabelVisibility(false);
                    }});
            }
        }
        if (DEBUG) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                Slog.v(TAG, "carrierlabelGemini=" + mCarrierLabelGemini + " show=" + mShowCarrierInPanel);
            } else {
                Slog.v(TAG, "carrierlabel=" + mCarrierLabel + " show=" + mShowCarrierInPanel);
            }
        }
        if (mShowCarrierInPanel) {
            /// M: [SystemUI] Support "Dual SIM". {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                mCarrierLabelGemini.setVisibility(mCarrierLabelVisible ? View.VISIBLE : View.INVISIBLE);
                mCarrier2.setVisibility(View.GONE);
                mCarrierDivider.setVisibility(View.GONE);
                mCarrierLabelGemini.setVisibility(View.GONE);   //by kay
            } else {
                if(mCarrierLabel!=null)
                mCarrierLabel.setVisibility(mCarrierLabelVisible ? View.VISIBLE : View.INVISIBLE);
            }
            /// M: [SystemUI] Support "Dual SIM". }

            // for mobile devices, we always show mobile connection info here (SPN/PLMN)
            // for other devices, we show whatever network is connected
            if (!FeatureOption.MTK_GEMINI_SUPPORT) {
                if (!mNetworkController.hasMobileDataFeature()) {
                    mNetworkController.addCombinedLabelView(mCarrierLabel);
                }
            }

            // set up the dynamic hide/show of the label
            mPile.setOnSizeChangedListener(new OnSizeChangedListener() {
                @Override
                public void onSizeChanged(View view, int w, int h, int oldw, int oldh) {
                    updateCarrierLabelVisibility(false);
                }
            });
        }

        // Quick Settings (where available, some restrictions apply)
        if (mHasSettingsPanel) {
            // first, figure out where quick settings should be inflated
            final View settings_stub;
            if (mHasFlipSettings) {
                // a version of quick settings that flips around behind the notifications
                settings_stub = mStatusBarWindow.findViewById(R.id.flip_settings_stub);
                if (settings_stub != null) {
                    mFlipSettingsView = ((ViewStub)settings_stub).inflate();
                    mFlipSettingsView.setVisibility(View.GONE);
                    mFlipSettingsView.setVerticalScrollBarEnabled(false);
                }
            } else {
                // full quick settings panel
                settings_stub = mStatusBarWindow.findViewById(R.id.quick_settings_stub);
                if (settings_stub != null) {
                    mSettingsPanel = (SettingsPanelView) ((ViewStub)settings_stub).inflate();
                } else {
                    mSettingsPanel = (SettingsPanelView) mStatusBarWindow.findViewById(R.id.settings_panel);
                }

                if (mSettingsPanel != null) {
                    if (true/*!ActivityManager.isHighEndGfx()*/) {
		            //mNotificationPanel.setBackground(new FastColorDrawable(context.getResources().getColor(
		                    //R.color.notification_panel_solid_background)));
		            //mNotificationPanel.setBackgroundDrawable(
		        			//BitmapUtils.getBlurWallpaper(context));//modify by zqs 20130830
		        		mUpdateHandler.postDelayed(mResetNotificationPanelBg, 100);
                    }
                }
            }

            // wherever you find it, Quick Settings needs a container to survive
            mSettingsContainer = (QuickSettingsContainerView)
                    mStatusBarWindow.findViewById(R.id.quick_settings_container);
            mSettingsContainer=null;
            if (mSettingsContainer != null) {
                mQS = new QuickSettings(mContext, mSettingsContainer);
                if (!mNotificationPanelIsFullScreenWidth) {
                    mSettingsContainer.setSystemUiVisibility(
                            View.STATUS_BAR_DISABLE_NOTIFICATION_TICKER
                            | View.STATUS_BAR_DISABLE_SYSTEM_INFO);
                }
                if (mSettingsPanel != null) {
                    mSettingsPanel.setQuickSettings(mQS);
                }
                mQS.setService(this);
                mQS.setBar(mStatusBarView);
                mQS.setup(mBatteryController);
            } else {
                mQS = null; // fly away, be free
            }
        }

        mClingShown = ! (DEBUG_CLINGS
            || !Prefs.read(mContext).getBoolean(Prefs.SHOWN_QUICK_SETTINGS_HELP, false));

        if (!ENABLE_NOTIFICATION_PANEL_CLING || ActivityManager.isRunningInTestHarness()) {
            mClingShown = true;
        }

//        final ImageView wimaxRSSI =
//                (ImageView)sb.findViewById(R.id.wimax_signal);
//        if (wimaxRSSI != null) {
//            mNetworkController.addWimaxIconView(wimaxRSSI);
//        }

        // receive broadcasts
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SKIN_CHANGED);
        /// M: ALPS00349274 to hide navigation bar when ipo shut down to avoid it flash when in boot ipo mode.{
        filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        filter.addAction("android.intent.action.ACTION_BOOT_IPO");
        /// M: ALPS00349274 to hide navigation bar when ipo shut down to avoid it flash when in boot ipo mode.}
        /// M: Support "Dual SIM PLMN".
        filter.addAction(Telephony.Intents.SPN_STRINGS_UPDATED_ACTION);
		filter.addAction(Intent.ACTION_UPDATE_STATUSBAR);//added by wang 20130730
		filter.addAction(Intent.ACTION_USER_PRESENT);//added by wang 20130730
		filter.addAction("iphone_goto_lock_screen");//added by wang 20130730
		filter.addAction(Intent.ACTION_WALLPAPER_CHANGED);//added by wang 20130730
		filter.addAction("control_center_action");//added by wang 20130730
		filter.addAction("input_method_state");//added by wang 20130730
        filter.addAction("opencamera");
        filter.addAction("closecamera");//added by xujia 20130922
        filter.addAction("allow_access_in_activity");
        filter.addAction("ACTION_TO_EXIT_APP");
        filter.addAction("is_launcher");
        filter.addAction("go_to_activity");
        filter.addAction("phone_schedule_change");
        filter.addAction("close_status_bar_expand");
	 filter.addAction("toggle_status_bar_expand");
	  filter.addAction("toggle_control_center");
	  filter.addAction("reset_statusbar_from_launcher");//added by xujia
	  filter.addAction("hide_statusbar_panel_from_launcher");
	  filter.addAction("set_live_wallpaper_cenon");//added by xujia
        filter.addAction("IS_DELETE_ALL_NOTIFICATION");// add by csc
        filter.addAction("STATUS_BAR_RESTORE");// add by csc
       filter.addAction("receive_unread_messages");// add by xss for ios10
       filter.addAction("status_bar_bg_change");// add by xss for ios10
       filter.addAction("show_status_bar_lock_iocn");// add by xss for ios10
       filter.addAction(Intent.ACTION_LOCALE_CHANGED);// add by xss for ios10
       filter.addAction("iphone_weather_the_first_city_name_changed"); //added by xss for ios Bug[4414]
      /*Begin::added by xss for back to last app*/
	  filter.addAction("top_activity_changes");
	  filter.addAction("notificationContentView_is_show");
	  filter.addAction("notificationContentView_is_click");
	  filter.addAction("back_button_text_color");
	  filter.addAction("click_back_button");
      filter.addAction("listen_keyguard_openOrclose");
	  /*End:added by xss for back to last app*/
 		filter.addAction("TO_SCREEN_SHOTS");// add by csc
        filter.addAction("CURRENT_SCREEN_SLIDE");// add by csc
       	   /*Begin::added by xss for blur*/
	  filter.addAction("workspace_screen_change_blur");
	  /*End::added by xss for blur*/
        context.registerReceiver(mBroadcastReceiver, filter);
        context.registerReceiver(mHomeKeyEventReceiver, new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));//added by xss for back to last app
        // listen for USER_SETUP_COMPLETE setting (per-user)
        resetUserSetupObserver();
        /// M: [SystemUI] Support "Dual SIM". {
        IntentFilter simInfoIntentFilter = new IntentFilter();
        simInfoIntentFilter.addAction(Intent.SIM_SETTINGS_INFO_CHANGED);
        simInfoIntentFilter.addAction(TelephonyIntents.ACTION_SIM_INSERTED_STATUS);
        simInfoIntentFilter.addAction(TelephonyIntents.ACTION_SIM_INFO_UPDATE);
        simInfoIntentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        simInfoIntentFilter.addAction(ACTION_BOOT_IPO);
        context.registerReceiver(mSIMInfoReceiver, simInfoIntentFilter);
        /// M: [SystemUI] Support "Dual SIM". }

        /// M: [ALPS00512845] Handle SD Swap Condition.
        mNeedRemoveKeys = new ArrayList<IBinder>();
        if (SUPPORT_SD_SWAP) {
            IntentFilter mediaEjectFilter = new IntentFilter();
            mediaEjectFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            mediaEjectFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            mediaEjectFilter.addDataScheme("file");
            context.registerReceiver(mMediaEjectBroadcastReceiver, mediaEjectFilter);
        }
        /*Begin:added by xss for ios10 blur*/
	 mBlurWallpaperBgView=(ImageView)mNotificationPanel.findViewById(R.id.blur_wallpaper_bg);//added by xss for Blur
        mBlurWallpaperBgViewright=(ImageView)mNotificationPanel.findViewById(R.id.blur_wallpaper_bg_right);
     mBlurSettingBgView=(View)mNotificationPanel.findViewById(R.id.blur_setting_bg);
	 keyguardLockIv = (ImageView)mStatusBarView.findViewById(R.id.keyguard_lock_iv);// add by csc for ios 10
	 /*End:added by xss for ios10 blur*/
	  /*Begin:added by xss for back to last app*/   
	 mBackToLastAppView= (LinearLayout)mStatusBarView.findViewById(R.id.back_to_last_app_view);
	 mBackToLastAppIcon= (View)mStatusBarView.findViewById(R.id.back_to_last_app_icon);
	 mBackToLastAppText= (TextView)mStatusBarView.findViewById(R.id.back_to_last_app_text);
	 mBackButtonText=mBackToLastAppText.getText();
        /*End:added by xss for back to last app*/
	/*Begin:added by xss for back to phone*/
	  backToPhoneBtn=(TextView) mStatusBarView.findViewById(R.id.back_to_phone_btn);
         if(DEBUG)Log.i("back_to_phone","PhoneStatusBar makeStatusBarviews()   -----backToPhoneBtn="+backToPhoneBtn); 		
	  TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
	  telephonyManager.listen(new mPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
	  backToPhoneLandsView=  mStatusBarView.findViewById(R.id.lands_View_back_to_phone);
	 /*End:added by xss for back to phone*/  	
        return mStatusBarView;
    }
    /*
         * add by csc
         * */
    private void isShowToday(boolean isShow) {
        if(isShow){
            deleteLin.setVisibility(View.VISIBLE);
            deleteAllBtn.setVisibility(View.VISIBLE);
            deleteAllTv.setVisibility(View.GONE);
            noNotificationText.setVisibility(View.GONE);
            isNoNotification=false;// add by csc on 20161007
        }else{
            deleteLin.setVisibility(View.GONE);
            noNotificationText.setVisibility(View.VISIBLE);
            isNoNotification=true;// add by csc on 20161007
        }

    }

/*begin: added by xujia 20131111*/
	Button mCloseButton;
	Button mOpenButton;
	Button todayButton,allButton,notButton;
    private ImageView pageOneIv,pageTwoIv;// add by csc

    EditText searchSrcTextEt;
    LinearLayout searchLi;
    MyViewPager viewPager;
	ArrayList<View> listViews;
    MyViewPagerAdapter myViewPagerAdapter;
    public final static int SHOWTYPE_LEFT = 0;   //
    public final static int SHOWTYPE_RIGHT = 1;
    private int mShowType = 0;
    private int currIndex = 0;
    private ScrollView mAllstatusScrollview;
    private EditText mSearchText;;
    //private FrameLayout mPageSetting;
    private BottomPanelBar mPageSetting;
    private CalendarView calendarView;
    private TextView nextScheduleText;
    private TextView tomorrowScheduleText;
    private long remainingMinutes=0;
    private long eventTime=0;
    private  StringBuilder titles=new StringBuilder();
    private final SimpleDateFormat nDateFormat = new SimpleDateFormat("H:mm");
    private int earlyEventId=0;
    private List<Event> todayEventsList;
    private int[] earlyEventsID;
    private List<Event> earlyTodayEvents =new ArrayList<Event>();
    private MissCallUnReadObserver obCall;
    private MmsUnReadObserver obMms;
     private static List<User> iphoneCallUsers;//added by xujia



     private ContentObserver mObserver = new ContentObserver(new Handler())
        {
            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override
            public void onChange(boolean selfChange) {
                eventsChanged();
            }
        };










     private void updateObserver(){
                boolean mShowMessageContent = Settings.System.getInt(mContext.getContentResolver(), "iphonesmspreviewstate", 0) != 0;
                if(obCall!=null&&obMms!=null){
                     obMms.setShowMessageContent(mShowMessageContent);
                     obCall.updateQueryBaseTime(System.currentTimeMillis());
                     obMms.updateQueryBaseTime(System.currentTimeMillis());
                  }
        }



    private void updateTodaySchedule(){
                if(calendarView!=null) {
                    todayEventsList = calendarView.getTodayEvents();
                    titles.delete(0, titles.length());

            Log.d("calendar_todays=","todayEventsList="+todayEventsList.size()+" earlyTodayEvents=="+earlyTodayEvents.size());




            if(todayEventsList.size()>0){


                  Calendar start=todayEventsList.get(0).getStartCalendar();
                  long beginHour=start.getTimeInMillis();


                  for(int i=1;i<todayEventsList.size();i++){
                           long hour=todayEventsList.get(i).getStartCalendar().getTimeInMillis();

                           if(beginHour>=hour){
                               beginHour=hour;
                               earlyEventId=i;
                            }

                      }

                    List<Event> todays=getTodayEarlyEvents(todayEventsList, beginHour,earlyEventId);
                    Log.d("calendar_todays=","today="+todays.size());
                     for(int i=0;i<todays.size();i++){
                           titles.append(todays.get(i).getEventTitle());
                            if((i+1)!=todays.size()){
                                    titles.append(",");
                                }
                        }


                    Calendar calendar = Calendar.getInstance();
                    long nowTime=calendar.getTimeInMillis();

                  long  remainingTime=beginHour-nowTime;
                    remainingMinutes=remainingTime/(1000*60);
                    eventTime=beginHour;
                    startUpdateTime();

                    updateToday(titles.toString(),beginHour);


             }else{
                    nextScheduleText.setVisibility(View.GONE);
                    handler.removeCallbacks(runnable);

                    }
                }
        }

        private void updateToday(String title,long beginHour){


            if(remainingMinutes<60&&remainingMinutes>0){
                    nextScheduleText.setVisibility(View.VISIBLE);
                     nextScheduleText.setText(mContext.getString(R.string.today_next_event_in_hour,title,remainingMinutes));
              }else if(remainingMinutes>=60){
                    nextScheduleText.setVisibility(View.VISIBLE);
                    nextScheduleText.setText(mContext.getString(R.string.today_next_event,title,nDateFormat.format(beginHour)));
                }else
                    {




                if(todayEventsList!=null){
                 for(int i=0;i<earlyTodayEvents.size();i++){
                        Event event =earlyTodayEvents.get(i);
                     for(int j=0;j<todayEventsList.size();j++){
                            if((todayEventsList.get(j)).getEventId()==event.getEventId()){
                                todayEventsList.remove(j);
                        }

                    }

                  }
                     earlyTodayEvents.clear();

                }
                       updateTodaySchedule();

                }

         }


        private void startUpdateTime(){
                 if(remainingMinutes<60&&remainingMinutes>0){

                    handler.removeCallbacks(runnable);
                     handler.postDelayed(runnable,1000*60);
                    }else if(remainingMinutes>=60){

                         handler.removeCallbacks(runnable);
                          handler.postDelayed(runnable,1000*60*60);

                        }


            }



        private Handler handler = new Handler();
            private Runnable runnable = new Runnable() {
            public void run () {
                remainingMinutes--;

                if(remainingMinutes<60&&remainingMinutes>0){
                    handler.postDelayed(this,1000*60);
                 }else if(remainingMinutes>=60){
                    handler.postDelayed(this,1000*60*60);
                 }
                 updateToday(titles.toString(),eventTime);
            }
        };




        private List<Event> getTodayEarlyEvents(List<Event> list,long earlyHour,int j){


          Log.d("calendar_todays=","list="+list.size());
              earlyTodayEvents.clear();
              earlyTodayEvents.add(list.get(j));

          long eventId= list.get(j).getEventId();
             for(int i=0;i<list.size();i++){
                     long hour=list.get(i).getStartCalendar().getTimeInMillis();

                        if(earlyHour==hour&&eventId!=list.get(i).getEventId()){
                            earlyTodayEvents.add(list.get(i));
                        }
                }

             return earlyTodayEvents;

            }



        private void updateTomorrowSchedule(){
            if(calendarView!=null) {
                List<Event> list = calendarView.getTomorrowEvents();
                if (list != null) {
                    switch (list.size()) {
                        case 0:
                            tomorrowScheduleText.setText(mContext.getString(R.string.tomorrow_no_events));
                            break;
                        case 1:


                            Event event = list.get(0);
                            String startTime = nDateFormat.format(event.getStartCalendar().getTime());
                            tomorrowScheduleText.setText(mContext.getString(R.string.tomorrow_one_event, startTime));
                            break;
                        default:
                            Calendar start = list.get(0).getStartCalendar();
                            long beginHour = start.getTimeInMillis();
                            for (int i = 1; i < list.size(); i++) {
                                long hour = list.get(i).getStartCalendar().getTimeInMillis();
                                if (beginHour > hour) {
                                    beginHour = hour;
                                }

                            }

                            tomorrowScheduleText.setText(mContext.getString(R.string.tomorrow_many_events, list.size(), nDateFormat.format(beginHour)));
                            break;

                    }

                }


                }
            }


        public TextView getTomorrowSchedule(){

            return tomorrowScheduleText;


        }

       public TextView getTodayNextSchedule(){

            return nextScheduleText;


        }

    public void setStatusBarBg(int color){
        if(mStatusBarView!=null)mStatusBarView.setBackgroundColor(color);
    }

    public void changeViewPager(final int showType){
        if(mKeyguardManager.isKeyguardLocked() && showType == SHOWTYPE_RIGHT){
            return;
        }
        if(listViews != null && mShowType != showType){
            if(showType == SHOWTYPE_LEFT){
                Log.d("hjz3","LEFT");
                mAllstatusScrollview.setVisibility(View.VISIBLE);
                mClockView.setVisibility(View.VISIBLE);
                mPageSetting.setVisibility(View.GONE);
                mBlurWallpaperBgViewright.setVisibility(View.GONE);
                mBlurWallpaperBgView.setVisibility(View.VISIBLE);
                mNotificationPanelHeader.setVisibility(View.VISIBLE);
            }else{
                Log.d("hjz3","RIGHT");
                mClockView.setVisibility(View.GONE);
                mAllstatusScrollview.setVisibility(View.GONE);
                mPageSetting.setVisibility(View.VISIBLE);
                mBlurWallpaperBgViewright.setVisibility(View.VISIBLE);
                mBlurWallpaperBgView.setVisibility(View.GONE);
                mNotificationPanelHeader.setVisibility(View.INVISIBLE);
            }
            setBlurSettingBgView(showType);
            mShowType = showType;
        }


        if(listViews.size() > 1){
            int curItem = viewPager.getCurrentItem();
            if(curItem != 1){
                Log.d("kay", "changeViewPager: setCurrentItem(1):");
                viewPager.setCurrentItem(1);
            }
        }


        new Handler().postDelayed(new Runnable(){
            @Override
            public void run(){
                if(showType == SHOWTYPE_LEFT){
                    viewPager.setScrollble(true);
                }else{
                    viewPager.setScrollble(false);
                }
            }
        },300); //300 -> 10

        //
    }

    /*add by kay*/
    private void setSearchMicIcon(boolean isGone){
        Drawable nav_right=mContext.getResources().getDrawable(R.drawable.search_mic_icon);
        Drawable nav_left=mContext.getResources().getDrawable(R.drawable.search_magnifier_3);
        nav_right.setBounds(0, 0, nav_right.getMinimumWidth(), nav_right.getMinimumHeight());
        nav_left.setBounds(0, 0, nav_left.getMinimumWidth(), nav_left.getMinimumHeight());
        Log.d("kay8", "setSearchMicIcon: nav_left:" + nav_left + " nav_right:" + nav_right + "isGone:" + isGone);
        if(isGone){
            mSearchText.setCompoundDrawables(nav_left, null, null, null);
        }else {
            mSearchText.setCompoundDrawables(nav_left, null, nav_right, null);
        }
    }

    private void initViewPager(){

        pageOneIv = (ImageView) mNotificationPanel.findViewById(R.id.page_one_icon);
        pageTwoIv   = (ImageView)mNotificationPanel.findViewById(R.id.page_two_icon);
        Log.d("kay22", "initViewPager: pageOneIv:" + pageOneIv + "  pageTwoIv:" + pageTwoIv);
        pageOneIv.setBackgroundResource(R.drawable.iphone_dian_gray);
        pageTwoIv.setBackgroundResource(R.drawable.iphone_dian_bai);
        todayButton = (Button) mNotificationPanel.findViewById(R.id.today_button);
        allButton = (Button) mNotificationPanel.findViewById(R.id.all_button);
        searchSrcTextEt = (EditText) mNotificationPanel.findViewById(R.id.search_src_text);
        searchLi = (LinearLayout) mNotificationPanel.findViewById(R.id.search_text);
    //notButton = (Button) mNotificationPanel.findViewById(R.id.miss_button);//del by xujia
      /*Begin:added by xss for ios10 lunarDate*/	
      saveLunarDate();
      /*End:added by xss for ios10 lunarDate*/		  
      listViews = new ArrayList<View>();
      allStatusbarLayout = (LinearLayout) View.inflate(mContext,
                R.layout.all_statusbar_layout, null);
        //begin kay
        //bottomSettingPanel = (LinearLayout) View.inflate(mContext, R.layout.page_windonw_bar, null);

        //end kay
/*Begin:added by xss for ios */
        mApplicationPreview=(ApplicationPreview)View.inflate(mContext, R.layout.application_preview, null);
        mApplicationPreview.setContext(mContext,false);


        //setmBlurWallpaperBgView();
	 /*End:added by xss for ios */
    listViews.add(mApplicationPreview);
    listViews.add(allStatusbarLayout);/*View.inflate(mContext, R.layout.all_statusbar_layout, null)*/
    //add by hjz
    mShowType = SHOWTYPE_LEFT;
    mAllstatusScrollview = (ScrollView)allStatusbarLayout.findViewById(R.id.all);
    mPageSetting = (BottomPanelBar) allStatusbarLayout.findViewById(R.id.page_setting);
         appPreviewSearchText = (LinearLayout)mApplicationPreview.findViewById(R.id.search_text);
        appPreviewSearchText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("kayphone", "onClick: appPreviewSearchText");
                animateCollapsePanels();
                Intent intent=new Intent();
                ComponentName cn=new ComponentName("com.hskj.hometest",
                        "com.hskj.hometest.AssistSearchActivity");
                intent.setComponent(cn);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
        mSearchText = (EditText) mApplicationPreview.findViewById(R.id.search_src_text);
        setSearchMicIcon(false);
        mSearchText.setOnClickListener(new OnClickListener() {
            @java.lang.Override
            public void onClick(View v) {
                Log.d("kayphone", "onClick: appPreviewSearchText");
                animateCollapsePanels();
                Intent intent=new Intent();
                ComponentName cn=new ComponentName("com.hskj.hometest",
                        "com.hskj.hometest.AssistSearchActivity");
                intent.setComponent(cn);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
    //end kay
    //listViews.add(bottomSettingPanel);
    //listViews.add(View.inflate(mContext, R.layout.miss_statusbar_layout, null));//del by xujia

    viewPager = (MyViewPager) mNotificationPanel.findViewById(R.id.vPager);
    myViewPagerAdapter = new MyViewPagerAdapter(listViews);
    viewPager.setAdapter(myViewPagerAdapter);
    viewPager.setCurrentItem(1);
    viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    viewPager.setScrollble(true);
    todayButton.setSelected(true);
        pageOneIv.setBackgroundResource(R.drawable.iphone_dian_bai);
        pageTwoIv.setBackgroundResource(R.drawable.iphone_dian_gray);
        todayButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick( View v )
            {
                viewPager.setCurrentItem(0);
                pageOneIv.setBackgroundResource(R.drawable.iphone_dian_bai);
                pageTwoIv.setBackgroundResource(R.drawable.iphone_dian_gray);
            }
        });

        allButton.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick( View v )
            {
                viewPager.setCurrentItem(1);
                pageOneIv.setBackgroundResource(R.drawable.iphone_dian_gray);
                pageTwoIv.setBackgroundResource(R.drawable.iphone_dian_bai);
            }
        });
        searchLi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
               animateCollapsePanels();
                Intent intent=new Intent();
                ComponentName cn=new ComponentName("com.hskj.hometest",
                        "com.hskj.hometest.AssistSearchActivity");
                intent.setComponent(cn);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        });
       /**Begin: del by xujia **/
       /* notButton.setOnClickListener( new OnClickListener() {
            
            @Override
            public void onClick( View v )
            {
                viewPager.setCurrentItem(2);
            }
        });*/
       /**End: del by xujia **/

  // 	mDrawer = (MultiDirectionSlidingDrawer) mStatusBarWindow.findViewById( R.id.drawer );
   	   calendarView=(CalendarView)listViews.get(0).findViewById(R.id.calendar_list);
//       calendarView.setBar(this);


       // UnReadMessageLayout unReadMessageLayout=(UnReadMessageLayout)listViews.get(2).findViewById(R.id.unread_message_layout);//del by xujia
        //unReadMessageLayout.setBar(this);

//        obCall = new MissCallUnReadObserver(new Handler(), unReadMessageLayout, System.currentTimeMillis());//del by xujia
       // obMms = new MmsUnReadObserver(new Handler(), unReadMessageLayout, System.currentTimeMillis());//del by xujia

       nextScheduleText=(TextView)listViews.get(0).findViewById(R.id.next_schedule_text);
       tomorrowScheduleText=(TextView)listViews.get(0).findViewById(R.id.tomorrow_schedule_text);
        
 	/*SimpleDateFormat nDateFormat = new SimpleDateFormat("EEEE,\nMMMM d");

    
   	TextView dateText=(TextView)listViews.get(0).findViewById(R.id.date_text);
   	dateText.setText(nDateFormat.format(calendar.getTime()));*/

}

    private void eventsChanged(){


        if(calendarView!=null){
            calendarView.clearEvents();
            calendarView.getEventsLists();

          }
    }



    


   



/*public class MissedCallContentObserver extends ContentObserver {
 
 private Context ctx;
 
 private static final String TAG = "MissedCallContentObserver";
 
 public MissedCallContentObserver(Context context, Handler handler) {
  super(handler);
  ctx = context;
 }

 @Override
 public void onChange(boolean selfChange) {
  
  Cursor csr = ctx.getContentResolver().query(Calls.CONTENT_URI, new String[] {Calls.NUMBER, 

Calls.TYPE, Calls.NEW}, null, null, Calls.DEFAULT_SORT_ORDER);
  
  if (csr != null) {
   if (csr.moveToFirst()) {
    int type = csr.getInt(csr.getColumnIndex(Calls.TYPE));
    switch (type) {
    case Calls.MISSED_TYPE:
     Log.v(TAG, "missed type");
     if (csr.getInt(csr.getColumnIndex(Calls.NEW)) == 1) {
      Log.v(TAG, "you have a missed call");
     }
     break;
    case Calls.INCOMING_TYPE:
     Log.v(TAG, "incoming type");
     break;
    case Calls.OUTGOING_TYPE:
     Log.v(TAG, "outgoing type");
     break;
    }
   }
   // release resource
   csr.close();
  }
 }
 
 @Override
 public boolean deliverSelfNotifications() {
  return super.deliverSelfNotifications();
 }
}*/






   public class MyOnPageChangeListener implements OnPageChangeListener{

        public void onPageScrollStateChanged(int arg0) {


        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {


        }

        public void onPageSelected(int arg0) {

            switch (arg0) {
            case 0:
                 todayButton.setSelected(true);
                 allButton.setSelected(false);
                pageOneIv.setBackgroundResource(R.drawable.iphone_dian_bai);
                pageTwoIv.setBackgroundResource(R.drawable.iphone_dian_gray);
                 //notButton.setSelected(false);//del by xujia
                break;
            case 1:
                  todayButton.setSelected(false);
                 allButton.setSelected(true);
                pageOneIv.setBackgroundResource(R.drawable.iphone_dian_gray);
                pageTwoIv.setBackgroundResource(R.drawable.iphone_dian_bai);
                 //notButton.setSelected(false);//del by xujia
                break;
            case 2:
                 todayButton.setSelected(false);
                 allButton.setSelected(false);
                pageOneIv.setBackgroundResource(R.drawable.iphone_dian_gray);
                pageTwoIv.setBackgroundResource(R.drawable.iphone_dian_gray);
                 //notButton.setSelected(true);//del by xujia
                break;

            }

        }

    }

 public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            //container.removeView(mListViews.get(position));
            container.removeView((View)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View objView = mListViews.get(position);
            String obji = "obj"+position;
            //Log.d("kay", "instantiateItem: " + position + " " + obji);
           // Object obj = (Object)obji;
            if(position == 0){
                objView.setTag(obji/*R.id.tag_vpager_first*/);
            }else if(position == 1){
                objView.setTag(obji/*R.id.tag_vpager_second*/);
            }

            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

         /*@Override
         public int getItemPosition(Object object) {
             View objView = (View)object;
             String obj = (String)objView.getTag();
             //Log.d("kay", "getItemPosition: obj:" + obj +"  " + TextUtils.equals(obj,"obj1"));
             if(TextUtils.equals(obj,"obj1")){
             //if(obj){
                 return POSITION_NONE;
             }else{
                return POSITION_UNCHANGED;
             }
         }*/
 }
    
/*End: added by xujia 20131111 */
    @Override
    protected View getStatusBarView() {
        return mStatusBarView;
    }

    @Override
    protected WindowManager.LayoutParams getRecentsLayoutParams(LayoutParams layoutParams) {
        boolean opaque = false;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                layoutParams.width,
                layoutParams.height,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                (opaque ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT));
        if (false/*ActivityManager.isHighEndGfx()*/) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        } else {
            lp.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            lp.dimAmount = 0.75f;
        }
        lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        lp.setTitle("RecentsPanel");
        lp.windowAnimations = com.android.internal.R.style.Animation_RecentApplications;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        return lp;
    }

    @Override
    protected WindowManager.LayoutParams getSearchLayoutParams(LayoutParams layoutParams) {
        boolean opaque = false;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                (opaque ? PixelFormat.OPAQUE : PixelFormat.TRANSLUCENT));
        if (false/*ActivityManager.isHighEndGfx()*/) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }
        lp.gravity = Gravity.BOTTOM | Gravity.LEFT;
        lp.setTitle("SearchPanel");
        // TODO: Define custom animation for Search panel
        lp.windowAnimations = com.android.internal.R.style.Animation_RecentApplications;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
        | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        return lp;
    }

    @Override
    protected void updateSearchPanel() {
        super.updateSearchPanel();
        mSearchPanelView.setStatusBarView(mNavigationBarView);
        mNavigationBarView.setDelegateView(mSearchPanelView);
    }

    @Override
    public void showSearchPanel() {
        super.showSearchPanel();
        mHandler.removeCallbacks(mShowSearchPanel);

        // we want to freeze the sysui state wherever it is
        mSearchPanelView.setSystemUiVisibility(mSystemUiVisibility);

        WindowManager.LayoutParams lp =
            (android.view.WindowManager.LayoutParams) mNavigationBarView.getLayoutParams();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mWindowManager.updateViewLayout(mNavigationBarView, lp);
    }

    @Override
    public void hideSearchPanel() {
        super.hideSearchPanel();
        WindowManager.LayoutParams lp =
            (android.view.WindowManager.LayoutParams) mNavigationBarView.getLayoutParams();
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mWindowManager.updateViewLayout(mNavigationBarView, lp);
    }

    protected int getStatusBarGravity() {
        return Gravity.TOP | Gravity.FILL_HORIZONTAL;
    }

    public int getStatusBarHeight() {
        if (mNaturalBarHeight < 0) {
            final Resources res = mContext.getResources();
            mNaturalBarHeight =
                    res.getDimensionPixelSize(com.android.internal.R.dimen.status_bar_height);
        }
        return mNaturalBarHeight;
    }

    private View.OnClickListener mRecentsClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            toggleRecentApps();
        }
    };

    private int mShowSearchHoldoff = 0;
    private Runnable mShowSearchPanel = new Runnable() {
        public void run() {
            showSearchPanel();
            awakenDreams();
        }
    };

    View.OnTouchListener mHomeSearchActionListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (!shouldDisableNavbarGestures()) {
                    mHandler.removeCallbacks(mShowSearchPanel);
                    mHandler.postDelayed(mShowSearchPanel, mShowSearchHoldoff);
                }
            break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mHandler.removeCallbacks(mShowSearchPanel);
                awakenDreams();
            break;
        }
        return false;
        }
    };

    private void awakenDreams() {
        if (mDreamManager != null) {
            try {
                mDreamManager.awaken();
            } catch (RemoteException e) {
                // fine, stay asleep then
            }
        }
    }

    private void prepareNavigationBarView() {
        mNavigationBarView.reorient();

        mNavigationBarView.getRecentsButton().setOnClickListener(mRecentsClickListener);
        mNavigationBarView.getRecentsButton().setOnTouchListener(mRecentsPreloadOnTouchListener);
        mNavigationBarView.getHomeButton().setOnTouchListener(mHomeSearchActionListener);
        mNavigationBarView.getSearchLight().setOnTouchListener(mHomeSearchActionListener);
        updateSearchPanel();
    }

    // For small-screen devices (read: phones) that lack hardware navigation buttons
    private void addNavigationBar() {
        if (DEBUG) Slog.v(TAG, "addNavigationBar: about to add " + mNavigationBarView);
        if (mNavigationBarView == null) return;

        prepareNavigationBarView();

        mWindowManager.addView(mNavigationBarView, getNavigationBarLayoutParams());
    }

    private void repositionNavigationBar() {
        if (mNavigationBarView == null) return;

        prepareNavigationBarView();

        mWindowManager.updateViewLayout(mNavigationBarView, getNavigationBarLayoutParams());
    }

    private void notifyNavigationBarScreenOn(boolean screenOn) {
        if (mNavigationBarView == null) return;
        mNavigationBarView.notifyScreenOn(screenOn);
    }

    private WindowManager.LayoutParams getNavigationBarLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
                    0
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.OPAQUE);
        // this will allow the navbar to run in an overlay on devices that support this
        if (false/*ActivityManager.isHighEndGfx()*/) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }

        lp.setTitle("NavigationBar");
        lp.windowAnimations = 0;
        return lp;
    }

    private void addIntruderView() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL, // above the status bar!
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.FILL_HORIZONTAL;
        //lp.y += height * 1.5; // FIXME
        lp.setTitle("IntruderAlert");
        lp.packageName = mContext.getPackageName();
        lp.windowAnimations = R.style.Animation_StatusBar_IntruderAlert;

        mWindowManager.addView(mIntruderAlertView, lp);
    }

    public void addIcon(String slot, int index, int viewIndex, StatusBarIcon icon) {
        if (SPEW) Slog.d(TAG, "addIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                + " icon=" + icon);
        StatusBarIconView view = new StatusBarIconView(mContext, slot, null);
        view.set(icon);
        mStatusIcons.addView(view, viewIndex, new LinearLayout.LayoutParams(mIconSize, mIconSize));

        /*begin added by xujia*/
        StatusBarIconView view1 = new StatusBarIconView(mContext, slot, null);
        view1.set(icon);
        mStatusIcons1.addView(view1, viewIndex, new LinearLayout.LayoutParams(mIconSize, mIconSize));
        /*end by xujia*/
    }

    public void updateIcon(String slot, int index, int viewIndex,
            StatusBarIcon old, StatusBarIcon icon) {
        if (SPEW) Slog.d(TAG, "updateIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex
                + " old=" + old + " icon=" + icon);
        StatusBarIconView view = (StatusBarIconView)mStatusIcons.getChildAt(viewIndex);
        view.set(icon);
       /*added by xujia*/
          StatusBarIconView view1 = (StatusBarIconView)mStatusIcons1.getChildAt(viewIndex);
        view1.set(icon);
    }

    public void removeIcon(String slot, int index, int viewIndex) {
        if (SPEW) Slog.d(TAG, "removeIcon slot=" + slot + " index=" + index + " viewIndex=" + viewIndex);
        mStatusIcons.removeViewAt(viewIndex);
        mStatusIcons1.removeViewAt(viewIndex);
    }

    public void addNotification(IBinder key, StatusBarNotification notification) {
        /// M: [ALPS00512845] Handle SD Swap Condition.
        if (SUPPORT_SD_SWAP) {
            try {
                ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(notification.pkg, 0);
                if ((applicationInfo.flags & applicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
                    if (mAvoidSDAppAddNotification) {
                        return;
                    }
                    if (!mNeedRemoveKeys.contains(key)) {
                        mNeedRemoveKeys.add(key);
                    }
                    Slog.d(TAG, "addNotification, applicationInfo pkg = " + notification.pkg + " to remove notification key = " + key);
                }
            } catch (NameNotFoundException e1) {
                e1.printStackTrace();
            }
        }

        if (DEBUG) Slog.d(TAG, "addNotification score=" + notification.score);
        StatusBarIconView iconView = addNotificationViews(key, notification);
        if (iconView == null) return;

        boolean immersive = false;
        try {
            immersive = ActivityManagerNative.getDefault().isTopActivityImmersive();
            if (DEBUG) {
                Slog.d(TAG, "Top activity is " + (immersive?"immersive":"not immersive"));
            }
        } catch (RemoteException ex) {
        }

        /*
         * DISABLED due to missing API
        if (ENABLE_INTRUDERS && (
                   // TODO(dsandler): Only if the screen is on
                notification.notification.intruderView != null)) {
            Slog.d(TAG, "Presenting high-priority notification");
            // special new transient ticker mode
            // 1. Populate mIntruderAlertView

            if (notification.notification.intruderView == null) {
                Slog.e(TAG, notification.notification.toString() + " wanted to intrude but intruderView was null");
                return;
            }

            // bind the click event to the content area
            PendingIntent contentIntent = notification.notification.contentIntent;
            final View.OnClickListener listener = (contentIntent != null)
                    ? new NotificationClicker(contentIntent,
                            notification.pkg, notification.tag, notification.id)
                    : null;

            mIntruderAlertView.applyIntruderContent(notification.notification.intruderView, listener);

            mCurrentlyIntrudingNotification = notification;

            // 2. Animate mIntruderAlertView in
            mHandler.sendEmptyMessage(MSG_SHOW_INTRUDER);

            // 3. Set alarm to age the notification off (TODO)
            mHandler.removeMessages(MSG_HIDE_INTRUDER);
            if (INTRUDER_ALERT_DECAY_MS > 0) {
                mHandler.sendEmptyMessageDelayed(MSG_HIDE_INTRUDER, INTRUDER_ALERT_DECAY_MS);
            }
        } else
         */

        if (notification.notification.fullScreenIntent != null) {
            // Stop screensaver if the notification has a full-screen intent.
            // (like an incoming phone call)
            awakenDreams();

            // not immersive & a full-screen alert should be shown
            if (DEBUG) Slog.d(TAG, "Notification has fullScreenIntent; sending fullScreenIntent");
            try {
                notification.notification.fullScreenIntent.send();
            } catch (PendingIntent.CanceledException e) {
            }
        } else {
            // usual case: status bar visible & not immersive

            // show the ticker if there isn't an intruder too
            if (mCurrentlyIntrudingNotification == null) {
                tick(null, notification, true);
            }
        }

        /// M: [SystemUI] Support "Dual SIM". {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            // process SIM info of notification.
            SimInfoManager.SimInfoRecord simInfo = null;
            int simInfoType = notification.notification.simInfoType;
            long simId = notification.notification.simId;
            if ((simInfoType >= 1 || simInfoType <= 3) && simId > 0) {
                Xlog.d(TAG, "addNotificationViews, simInfoType=" + simInfoType + ", simId=" + simId + ".");
                simInfo = SIMHelper.getSIMInfo(mContext, simId);
            }
            if (simInfo != null) {
                NotificationData.Entry entry = mNotificationData.findByKey(key);
                updateNotificationSimInfo(simInfo, notification.notification, iconView, entry.expanded);
            }
        }
        /// M: [SystemUI] Support "Dual SIM". }

        // Recalculate the position of the sliding windows and the titles.
        setAreThereNotifications();
        updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
    }

    public void removeNotification(IBinder key) {
        StatusBarNotification old = removeNotificationViews(key);
        if (SPEW) Slog.d(TAG, "removeNotification key=" + key + " old=" + old);

        /// M: [ALPS00512845] Handle SD Swap Condition.
        if (SUPPORT_SD_SWAP) {
            if (mNeedRemoveKeys.contains(key)) {
                mNeedRemoveKeys.remove(key);
            }
        }

        if (old != null) {
            // Cancel the ticker if it's still running
            //mTicker.removeEntry(old);//removed by wang 20130729

            // Recalculate the position of the sliding windows and the titles.
            updateExpandedViewPos(EXPANDED_LEAVE_ALONE);

            if (ENABLE_INTRUDERS && old == mCurrentlyIntrudingNotification) {
                mHandler.sendEmptyMessage(MSG_HIDE_INTRUDER);
            }

            if (CLOSE_PANEL_WHEN_EMPTIED && mNotificationData.size() == 0 && !mAnimating) {
                animateCollapsePanels();
            }
        }

        setAreThereNotifications();
    }

    private void updateShowSearchHoldoff() {
        mShowSearchHoldoff = mContext.getResources().getInteger(
            R.integer.config_show_search_delay);
    }

    private void loadNotificationShade() {
        if (mPile == null) return;

        int N = mNotificationData.size();

        ArrayList<View> toShow = new ArrayList<View>();

        final boolean provisioned = isDeviceProvisioned();
        // If the device hasn't been through Setup, we only show system notifications
        for (int i=0; i<N; i++) {
            Entry ent = mNotificationData.get(N-i-1);
            if (!(provisioned || showNotificationEvenIfUnprovisioned(ent.notification))) continue;
            if (!notificationIsForCurrentUser(ent.notification)) continue;
            toShow.add(ent.row);
        }

        ArrayList<View> toRemove = new ArrayList<View>();
        for (int i=0; i<mPile.getChildCount(); i++) {
            View child = mPile.getChildAt(i);
            if (!toShow.contains(child)) {
                toRemove.add(child);
            }
        }

        for (View remove : toRemove) {
            mPile.removeView(remove);
        }

        for (int i=0; i<toShow.size(); i++) {
            View v = toShow.get(i);
            if (v.getParent() == null) {
                mPile.addView(v, i);
            }
        }

        if (mSettingsButton != null) {
            mSettingsButton.setEnabled(isDeviceProvisioned());
        }
    }

    @Override
    protected void updateNotificationIcons() {
        if (mNotificationIcons == null) return;

        loadNotificationShade();

        final LinearLayout.LayoutParams params
            = new LinearLayout.LayoutParams(mIconSize + 2*mIconHPadding, mNaturalBarHeight);

        int N = mNotificationData.size();

        if (DEBUG) {
            Slog.d(TAG, "refreshing icons: " + N + " notifications, mNotificationIcons=" + mNotificationIcons);
        }

        ArrayList<View> toShow = new ArrayList<View>();
        // M: StatusBar IconMerger feature, hash{pkg+icon}=iconlevel
        HashMap<String, Integer> uniqueIcon = new HashMap<String, Integer>();

        final boolean provisioned = isDeviceProvisioned();
        // If the device hasn't been through Setup, we only show system notifications
        for (int i=0; i<N; i++) {
            Entry ent = mNotificationData.get(N-i-1);
            if (!((provisioned && ent.notification.score >= HIDE_ICONS_BELOW_SCORE)
                    || showNotificationEvenIfUnprovisioned(ent.notification))) continue;
            if (!notificationIsForCurrentUser(ent.notification)) continue;

            // M: StatusBar IconMerger feature
            String key = ent.notification.pkg + String.valueOf(ent.notification.notification.icon);
            if (uniqueIcon.containsKey(key) && uniqueIcon.get(key) == ent.notification.notification.iconLevel) {
                Xlog.d(TAG, "updateNotificationIcons(), IconMerger feature, skip pkg / icon / iconlevel ="
                    + ent.notification.pkg + "/" + ent.notification.notification.icon + "/" + ent.notification.notification.iconLevel);
                continue;
            }

            toShow.add(ent.icon);
            uniqueIcon.put(key, ent.notification.notification.iconLevel);
        }
        uniqueIcon = null;

        ArrayList<View> toRemove = new ArrayList<View>();
        for (int i=0; i<mNotificationIcons.getChildCount(); i++) {
            View child = mNotificationIcons.getChildAt(i);
            if (!toShow.contains(child)) {
                toRemove.add(child);
            }
        }

        for (View remove : toRemove) {
            mNotificationIcons.removeView(remove);
        }

        for (int i=0; i<toShow.size(); i++) {
            View v = toShow.get(i);
            if (v.getParent() == null) {
                mNotificationIcons.addView(v, i, params);
            }
        }
    }

    protected void updateCarrierLabelVisibility(boolean force) {
        if (!mShowCarrierInPanel) return;
        // The idea here is to only show the carrier label when there is enough room to see it, 
        // i.e. when there aren't enough notifications to fill the panel.
        if (DEBUG) {
            Slog.d(TAG, String.format("pileh=%d scrollh=%d carrierh=%d",
                    mPile.getHeight(), mScrollView.getHeight(), mCarrierLabelHeight));
        }

        final boolean emergencyCallsShownElsewhere = mEmergencyCallLabel != null;
        boolean makeVisible = false;
        /// M: Calculate ToolBar height when sim indicator is showing.
        /// M: Fix [ALPS00455548] Use getExpandedHeight instead of getHeight to avoid race condition.
        int height = mToolBarSwitchPanel.getVisibility() == View.VISIBLE ?
                ((int)mNotificationPanel.getExpandedHeight() - mCarrierLabelHeight - mNotificationHeaderHeight - mToolBarViewHeight)
                : ((int)mNotificationPanel.getExpandedHeight() - mCarrierLabelHeight - mNotificationHeaderHeight);
        /// M: Support "Dual Sim" @{
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            makeVisible =
                mPile.getHeight() < height && mScrollView.getVisibility() == View.VISIBLE;
        } else {
            makeVisible =
                !(emergencyCallsShownElsewhere && mNetworkController.isEmergencyOnly())
                && mPile.getHeight() < height && mScrollView.getVisibility() == View.VISIBLE;
        }
        /// M: Support "Dual Sim" @}
        if (force || mCarrierLabelVisible != makeVisible) {
            mCarrierLabelVisible = makeVisible;
            if (DEBUG) {
                Slog.d(TAG, "making carrier label " + (makeVisible?"visible":"invisible"));
            }
            /// M: Support "Dual Sim" @{
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                mCarrierLabelGemini.animate().cancel();
                if (makeVisible) {
                    mCarrierLabelGemini.setVisibility(View.VISIBLE);
                    mCarrierLabelGemini.setVisibility(View.GONE);//by kay
                }
                mCarrierLabelGemini.animate()
                    .alpha(makeVisible ? 1f : 0f)
                    .setDuration(150)
                    .setListener(makeVisible ? null : new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!mCarrierLabelVisible) { // race
                                mCarrierLabelGemini.setVisibility(View.INVISIBLE);
                                mCarrierLabelGemini.setAlpha(0f);
                            }
                        }
                    })
                    .start();
            /// M: Support "Dual Sim" @{
            } else {
                mCarrierLabel.animate().cancel();
                if (makeVisible) {
                    if(mCarrierLabel!=null)
                    mCarrierLabel.setVisibility(View.VISIBLE);
                }
                mCarrierLabel.animate()
                    .alpha(makeVisible ? 1f : 0f)
                    //.setStartDelay(makeVisible ? 500 : 0)
                    //.setDuration(makeVisible ? 750 : 100)
                    .setDuration(150)
                    .setListener(makeVisible ? null : new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (!mCarrierLabelVisible) { // race
                                mCarrierLabel.setVisibility(View.INVISIBLE);
                                mCarrierLabel.setAlpha(0f);
                            }
                        }
                    })
                    .start();
            }
        }
    }

    @Override
    protected void setAreThereNotifications() {
        final boolean any = mNotificationData.size() > 0;

        final boolean clearable = any && mNotificationData.hasClearableItems();

        if (DEBUG) {
            Slog.d(TAG, "setAreThereNotifications: N=" + mNotificationData.size()
                    + " any=" + any + " clearable=" + clearable);
        }

        if (mHasFlipSettings
                && mFlipSettingsView != null
                && mFlipSettingsView.getVisibility() == View.VISIBLE
                && mScrollView.getVisibility() != View.VISIBLE) {
            // the flip settings panel is unequivocally showing; we should not be shown
            mClearButton.setVisibility(View.GONE);
        } else if (mClearButton.isShown()) {
            if (clearable != (mClearButton.getAlpha() == 1.0f)) {
                ObjectAnimator clearAnimation = ObjectAnimator.ofFloat(
                        mClearButton, "alpha", clearable ? 1.0f : 0.0f).setDuration(250);
                clearAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (mClearButton.getAlpha() <= 0.0f) {
                            mClearButton.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (mClearButton.getAlpha() <= 0.0f) {
                            mClearButton.setVisibility(View.VISIBLE);
                        }
                    }
                });
                clearAnimation.start();
            }
        } else {
            mClearButton.setAlpha(clearable ? 1.0f : 0.0f);
            mClearButton.setVisibility(clearable ? View.VISIBLE : View.GONE);
        }
        mClearButton.setEnabled(clearable);

        final View nlo = mStatusBarView.findViewById(R.id.notification_lights_out);
        final boolean showDot = (any&&!areLightsOn());
        if (showDot != (nlo.getAlpha() == 1.0f)) {
            if (showDot) {
                nlo.setAlpha(0f);
                nlo.setVisibility(View.VISIBLE);
            }
            nlo.animate()
                .alpha(showDot?1:0)
                .setDuration(showDot?750:250)
                .setInterpolator(new AccelerateInterpolator(2.0f))
                .setListener(showDot ? null : new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator _a) {
                        nlo.setVisibility(View.GONE);
                    }
                })
                .start();
        }

        updateCarrierLabelVisibility(false);
    }
	
/*Begin:added by xss for ios10*/
     public void showLockIocn(boolean show) {
        if(DEBUG)Log.i(TAG," showLockIocn()  =========mStatusBarView"+mStatusBarView+"   show="+show);
        if (mStatusBarView == null) return;
        View lockIcon = mStatusBarView.findViewById(R.id.keyguard_lock_iv);
        if (lockIcon != null) {
            lockIcon.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
/*End:added by xss for ios10*/

    public void showClock(boolean show) {
        if (mStatusBarView == null) return;
        View clock = mStatusBarView.findViewById(R.id.clock);
        if (clock != null) {
            clock.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * State is one or more of the DISABLE constants from StatusBarManager.
     */
    public void disable(int state) {
        final int old = mDisabled;
        final int diff = state ^ old;
        mDisabled = state;

        if (DEBUG) {
            Slog.d(TAG, String.format("disable: 0x%08x -> 0x%08x (diff: 0x%08x)",
                old, state, diff));
        }

        StringBuilder flagdbg = new StringBuilder();
        flagdbg.append("disable: < ");
        flagdbg.append(((state & StatusBarManager.DISABLE_EXPAND) != 0) ? "EXPAND" : "expand");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_EXPAND) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) ? "ICONS" : "icons");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_NOTIFICATION_ALERTS) != 0) ? "ALERTS" : "alerts");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_NOTIFICATION_ALERTS) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) ? "TICKER" : "ticker");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) ? "SYSTEM_INFO" : "system_info");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_BACK) != 0) ? "BACK" : "back");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_BACK) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_HOME) != 0) ? "HOME" : "home");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_HOME) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_RECENT) != 0) ? "RECENT" : "recent");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_RECENT) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_CLOCK) != 0) ? "CLOCK" : "clock");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_CLOCK) != 0) ? "* " : " ");
        flagdbg.append(((state & StatusBarManager.DISABLE_SEARCH) != 0) ? "SEARCH" : "search");
        flagdbg.append(((diff  & StatusBarManager.DISABLE_SEARCH) != 0) ? "* " : " ");
        flagdbg.append(">");
        Slog.d(TAG, flagdbg.toString());

        if ((diff & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) {
            mSystemIconArea.animate().cancel();
            if ((state & StatusBarManager.DISABLE_SYSTEM_INFO) != 0) {
                mSystemIconArea.animate()
                    .alpha(0f)
                    .translationY(mNaturalBarHeight*0.5f)
                    .setDuration(175)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setListener(mMakeIconsInvisible)
                    .start();
            } else {
                mSystemIconArea.setVisibility(View.VISIBLE);
                mSystemIconArea.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setStartDelay(0)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setDuration(175)
                    .start();
            }
        }

        if ((diff & StatusBarManager.DISABLE_CLOCK) != 0) {
            boolean show = (state & StatusBarManager.DISABLE_CLOCK) == 0;
            showClock(show);
        }
        if ((diff & StatusBarManager.DISABLE_EXPAND) != 0) {
            if ((state & StatusBarManager.DISABLE_EXPAND) != 0) {
                animateCollapsePanels();
            }
        }

        if ((diff & (StatusBarManager.DISABLE_HOME
                        | StatusBarManager.DISABLE_RECENT
                        | StatusBarManager.DISABLE_BACK
                        | StatusBarManager.DISABLE_SEARCH)) != 0) {
            // the nav bar will take care of these
            if (mNavigationBarView != null) mNavigationBarView.setDisabledFlags(state);

            if ((state & StatusBarManager.DISABLE_RECENT) != 0) {
                // close recents if it's visible
                mHandler.removeMessages(MSG_CLOSE_RECENTS_PANEL);
                mHandler.sendEmptyMessage(MSG_CLOSE_RECENTS_PANEL);
            }
        }

        if ((diff & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
            if ((state & StatusBarManager.DISABLE_NOTIFICATION_ICONS) != 0) {
                if (mTicking) {
                    haltTicker();
                }

                mNotificationIcons.animate()
                    .alpha(0f)
                    .translationY(mNaturalBarHeight*0.5f)
                    .setDuration(175)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setListener(mMakeIconsInvisible)
                    .start();
            } else {
                mNotificationIcons.setVisibility(View.VISIBLE);
                mNotificationIcons.animate()
                    .alpha(1f)
                    .translationY(0)
                    .setStartDelay(0)
                    .setInterpolator(new DecelerateInterpolator(1.5f))
                    .setDuration(175)
                    .start();
            }
        } else if ((diff & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
            if (mTicking && (state & StatusBarManager.DISABLE_NOTIFICATION_TICKER) != 0) {
                haltTicker();
            }
        }
    }

    @Override
    protected BaseStatusBar.H createHandler() {
        return new PhoneStatusBar.H();
    }

    /**
     * All changes to the status bar and notifications funnel through here and are batched.
     */
    private class H extends BaseStatusBar.H {
        public void handleMessage(Message m) {
            super.handleMessage(m);
            switch (m.what) {
                case MSG_OPEN_NOTIFICATION_PANEL:
                    animateExpandNotificationsPanel();
                    break;
                case MSG_OPEN_SETTINGS_PANEL:
                    animateExpandSettingsPanel();
                    break;
                case MSG_CLOSE_PANELS:
                    animateCollapsePanels();
                    break;
                case MSG_SHOW_INTRUDER:
                    setIntruderAlertVisibility(true);
                    break;
                case MSG_HIDE_INTRUDER:
                    setIntruderAlertVisibility(false);
                    mCurrentlyIntrudingNotification = null;
                    break;
            }
        }
    }

    public Handler getHandler() {
        return mHandler;
    }

    View.OnFocusChangeListener mFocusChangeListener = new View.OnFocusChangeListener() {
        public void onFocusChange(View v, boolean hasFocus) {
            // Because 'v' is a ViewGroup, all its children will be (un)selected
            // too, which allows marqueeing to work.
            v.setSelected(hasFocus);
        }
    };

    void makeExpandedVisible(boolean revealAfterDraw) {
        if (SPEW) Slog.d(TAG, "Make expanded visible: expanded visible=" + mExpandedVisible);
        if (mExpandedVisible) {
            return;
        }

        mExpandedVisible = true;
        mPile.setLayoutTransitionsEnabled(true);
        if (mNavigationBarView != null)
            mNavigationBarView.setSlippery(true);

        updateCarrierLabelVisibility(true);

        updateExpandedViewPos(EXPANDED_LEAVE_ALONE);

        // Expand the window to encompass the full screen in anticipation of the drag.
        // This is only possible to do atomically because the status bar is at the top of the screen!
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mStatusBarWindow.getLayoutParams();
        lp.flags &= ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        //lp.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
        lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        //Log.d("kay18", "makeExpandedVisible: mStatusBarWindow:" + lp.height);
        mWindowManager.updateViewLayout(mStatusBarWindow, lp);

        // Updating the window layout will force an expensive traversal/redraw.
        // Kick off the reveal animation after this is complete to avoid animation latency.
        if (revealAfterDraw) {
//            mHandler.post(mStartRevealAnimation);
        }

        /// M: Show always update clock of DateView.
        if (mDateView != null) {
            mDateView.updateClock();
        }
        visibilityChanged(true);
		onStatusBarStateChanged(false);//added by wang 20130911
    }

    public void animateCollapsePanels() {
        animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
    }

    public void animateCollapsePanels(int flags) {
        if (SPEW) {
            Slog.d(TAG, "animateCollapse():"
                    + " mExpandedVisible=" + mExpandedVisible
                    + " mAnimating=" + mAnimating
                    + " mAnimatingReveal=" + mAnimatingReveal
                    + " mAnimY=" + mAnimY
                    + " mAnimVel=" + mAnimVel
                    + " flags=" + flags);
        }

        if ((flags & CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL) == 0) {
            mHandler.removeMessages(MSG_CLOSE_RECENTS_PANEL);
            mHandler.sendEmptyMessage(MSG_CLOSE_RECENTS_PANEL);
        }

        if ((flags & CommandQueue.FLAG_EXCLUDE_SEARCH_PANEL) == 0) {
            Log.d("kay6", "animateCollapsePanels: MSG_CLOSE_SEARCH_PANEL:mKeyguardManager.isKeyguardLocked():" + mKeyguardManager.isKeyguardLocked());
            mHandler.removeMessages(MSG_CLOSE_SEARCH_PANEL);
            mHandler.sendEmptyMessage(MSG_CLOSE_SEARCH_PANEL);
            if(mKeyguardManager.isKeyguardLocked()){
                addOrRemoveSlideView(false);
            }
        }

        mStatusBarWindow.cancelExpandHelper();
        mStatusBarView.collapseAllPanels(true);

        /// M: [ALPS00802561] Dismiss app guide while we collapse the panel.
        if (mAppGuideDialog != null) {
            Slog.d(TAG, "animateCollapsePanels,  dismiss app guide dialog");
            mAppGuideDialog.dismiss();
        }
    }

    public ViewPropertyAnimator setVisibilityWhenDone(
            final ViewPropertyAnimator a, final View v, final int vis) {
        a.setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(vis);
                a.setListener(null); // oneshot
            }
        });
        return a;
    }

    public Animator setVisibilityWhenDone(
            final Animator a, final View v, final int vis) {
        a.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                v.setVisibility(vis);
            }
        });
        return a;
    }

    public Animator interpolator(TimeInterpolator ti, Animator a) {
        a.setInterpolator(ti);
        return a;
    }

    public Animator startDelay(int d, Animator a) {
        a.setStartDelay(d);
        return a;
    }

    public Animator start(Animator a) {
        a.start();
        return a;
    }

    final TimeInterpolator mAccelerateInterpolator = new AccelerateInterpolator();
    final TimeInterpolator mDecelerateInterpolator = new DecelerateInterpolator();
    final int FLIP_DURATION_OUT = 125;
    final int FLIP_DURATION_IN = 225;
    final int FLIP_DURATION = (FLIP_DURATION_IN + FLIP_DURATION_OUT);

    Animator mScrollViewAnim, mFlipSettingsViewAnim, mNotificationButtonAnim,
        mSettingsButtonAnim, mClearButtonAnim;
    /// M: [SystemUI] Remove settings button to notification header.
    private Animator mHeaderSettingsButtonAnim;

    @Override
    public void animateExpandNotificationsPanel() {
        if (SPEW) Slog.d(TAG, "animateExpand: mExpandedVisible=" + mExpandedVisible);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return ;
        }

        mNotificationPanel.expand();
        if (mHasFlipSettings && mScrollView.getVisibility() != View.VISIBLE) {
            flipToNotifications();
        }

        if (false) postStartTracing();
    }

    // M: To expand slowly than usual.
    private void animateExpandNotificationsPanelSlow() {
        Slog.d(TAG, "animateExpandSlow: mExpandedVisible=" + mExpandedVisible);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return ;
        }

        mNotificationPanel.expandSlow();
        if (mHasFlipSettings && mScrollView.getVisibility() != View.VISIBLE) {
            flipToNotifications();
        }

        if (false) postStartTracing();
    }

    public void flipToNotifications() {
        if (mFlipSettingsViewAnim != null) mFlipSettingsViewAnim.cancel();
        if (mScrollViewAnim != null) mScrollViewAnim.cancel();
        if (mSettingsButtonAnim != null) mSettingsButtonAnim.cancel();
        if (mNotificationButtonAnim != null) mNotificationButtonAnim.cancel();
        if (mClearButtonAnim != null) mClearButtonAnim.cancel();
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButtonAnim != null) {
            mHeaderSettingsButtonAnim.cancel();
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        mScrollView.setVisibility(View.VISIBLE);
        mScrollViewAnim = start(
            startDelay(FLIP_DURATION_OUT,
                interpolator(mDecelerateInterpolator,
                    ObjectAnimator.ofFloat(mScrollView, View.SCALE_X, 0f, 1f)
                        .setDuration(FLIP_DURATION_IN)
                    )));
        mFlipSettingsViewAnim = start(
            setVisibilityWhenDone(
                interpolator(mAccelerateInterpolator,
                        ObjectAnimator.ofFloat(mFlipSettingsView, View.SCALE_X, 1f, 0f)
                        )
                    .setDuration(FLIP_DURATION_OUT),
                mFlipSettingsView, View.INVISIBLE));
        mNotificationButtonAnim = start(
            setVisibilityWhenDone(
                ObjectAnimator.ofFloat(mNotificationButton, View.ALPHA, 0f)
                    .setDuration(FLIP_DURATION),
                mNotificationButton, View.INVISIBLE));
        mSettingsButton.setVisibility(View.VISIBLE);
        mSettingsButtonAnim = start(
            ObjectAnimator.ofFloat(mSettingsButton, View.ALPHA, 1f)
                .setDuration(FLIP_DURATION));
        mClearButton.setVisibility(View.VISIBLE);
        mClearButton.setAlpha(0f);
        setAreThereNotifications(); // this will show/hide the button as necessary
        mNotificationPanel.postDelayed(new Runnable() {
            public void run() {
                updateCarrierLabelVisibility(false);
            }
        }, FLIP_DURATION - 150);
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButton != null) {
            mHeaderSettingsButton.setVisibility(View.GONE);
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        /// M: [SystemUI] Support SimIndicator, show SimIndicator when notification panel is visible. @{
        if (mToolBarView.mSimSwitchPanelView.isPanelShowing()) {
            mToolBarSwitchPanel.setVisibility(View.GONE);
        }
        /// M: [SystemUI] Support SimIndicator, show SimIndicator when notification panel is visible. @{
    }

    @Override
    public void animateExpandSettingsPanel() {
        if (SPEW) Slog.d(TAG, "animateExpand: mExpandedVisible=" + mExpandedVisible);
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            return;
        }

        // Settings are not available in setup
        if (!mUserSetup) return;

        if (mHasFlipSettings) {
            mNotificationPanel.expand();
            if (mFlipSettingsView.getVisibility() != View.VISIBLE) {
                flipToSettings();
            }
        } else if (mSettingsPanel != null) {
            mSettingsPanel.expand();
        }

        if (false) postStartTracing();
    }

    public void switchToSettings() {
        // Settings are not available in setup
        if (!mUserSetup) return;

        mFlipSettingsView.setScaleX(1f);
        mFlipSettingsView.setVisibility(View.VISIBLE);
        mSettingsButton.setVisibility(View.GONE);
        mScrollView.setVisibility(View.GONE);
        mScrollView.setScaleX(0f);
        mNotificationButton.setVisibility(View.VISIBLE);
        mNotificationButton.setAlpha(1f);
        mClearButton.setVisibility(View.GONE);
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButton != null) {
            mHeaderSettingsButton.setVisibility(View.VISIBLE);
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        /// M: [SystemUI] Support SimIndicator, hide SimIndicator when settings panel is visible.
        mToolBarSwitchPanel.setVisibility(View.GONE);
    }

    public void flipToSettings() {
        // Settings are not available in setup
        if (!mUserSetup) return;

        if (mFlipSettingsViewAnim != null) mFlipSettingsViewAnim.cancel();
        if (mScrollViewAnim != null) mScrollViewAnim.cancel();
        if (mSettingsButtonAnim != null) mSettingsButtonAnim.cancel();
        if (mNotificationButtonAnim != null) mNotificationButtonAnim.cancel();
        if (mClearButtonAnim != null) mClearButtonAnim.cancel();
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButtonAnim != null) {
            mHeaderSettingsButtonAnim.cancel();
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        mFlipSettingsView.setVisibility(View.VISIBLE);
        mFlipSettingsView.setScaleX(0f);
        mFlipSettingsViewAnim = start(
            startDelay(FLIP_DURATION_OUT,
                interpolator(mDecelerateInterpolator,
                    ObjectAnimator.ofFloat(mFlipSettingsView, View.SCALE_X, 0f, 1f)
                        .setDuration(FLIP_DURATION_IN)
                    )));
        mScrollViewAnim = start(
            setVisibilityWhenDone(
                interpolator(mAccelerateInterpolator,
                        ObjectAnimator.ofFloat(mScrollView, View.SCALE_X, 1f, 0f)
                        )
                    .setDuration(FLIP_DURATION_OUT),
                mScrollView, View.INVISIBLE));
        mSettingsButtonAnim = start(
            setVisibilityWhenDone(
                ObjectAnimator.ofFloat(mSettingsButton, View.ALPHA, 0f)
                    .setDuration(FLIP_DURATION),
                    mScrollView, View.INVISIBLE));
        mNotificationButton.setVisibility(View.VISIBLE);
        mNotificationButtonAnim = start(
            ObjectAnimator.ofFloat(mNotificationButton, View.ALPHA, 1f)
                .setDuration(FLIP_DURATION));
        mClearButtonAnim = start(
            setVisibilityWhenDone(
                ObjectAnimator.ofFloat(mClearButton, View.ALPHA, 0f)
                .setDuration(FLIP_DURATION),
                mClearButton, View.GONE));
        /// M: [SystemUI] Remove settings button to notification header @{.
        if (mHeaderSettingsButton != null) {
            mHeaderSettingsButtonAnim = start(
                    setVisibilityWhenDone(
                            ObjectAnimator.ofFloat(mHeaderSettingsButton, View.ALPHA, 1f)
                            .setDuration(FLIP_DURATION),
                            mHeaderSettingsButton, View.VISIBLE));
        }
        /// M: [SystemUI] Remove settings button to notification header @}.
        mNotificationPanel.postDelayed(new Runnable() {
            public void run() {
                updateCarrierLabelVisibility(false);
            }
        }, FLIP_DURATION - 150);
        /// M: [SystemUI] Support SimIndicator, hide SimIndicator when settings panel is visible.
        mToolBarSwitchPanel.setVisibility(View.GONE);
    }

    public void flipPanels() {
        if (mHasFlipSettings) {
            if (mFlipSettingsView.getVisibility() != View.VISIBLE) {
                flipToSettings();
            } else {
                flipToNotifications();
            }
        }
    }

    public void animateCollapseQuickSettings() {
        mStatusBarView.collapseAllPanels(true);
    }

    void makeExpandedInvisibleSoon() {
        mHandler.postDelayed(new Runnable() { public void run() { makeExpandedInvisible(); }}, 50);
    }

    void makeExpandedInvisible() {
        if (SPEW) Slog.d(TAG, "makeExpandedInvisible: mExpandedVisible=" + mExpandedVisible
                + " mExpandedVisible=" + mExpandedVisible);

        if (!mExpandedVisible) {
            return;
        }

        // Ensure the panel is fully collapsed (just in case; bug 6765842, 7260868)
        mStatusBarView.collapseAllPanels(/*animate=*/ false);

        if (mHasFlipSettings) {
            // reset things to their proper state
            if (mFlipSettingsViewAnim != null) mFlipSettingsViewAnim.cancel();
            if (mScrollViewAnim != null) mScrollViewAnim.cancel();
            if (mSettingsButtonAnim != null) mSettingsButtonAnim.cancel();
            if (mNotificationButtonAnim != null) mNotificationButtonAnim.cancel();
            if (mClearButtonAnim != null) mClearButtonAnim.cancel();

            mScrollView.setScaleX(1f);
            mScrollView.setVisibility(View.VISIBLE);
            mSettingsButton.setAlpha(1f);
            mSettingsButton.setVisibility(View.VISIBLE);
            mNotificationPanel.setVisibility(View.GONE);
            mFlipSettingsView.setVisibility(View.GONE);
            mNotificationButton.setVisibility(View.GONE);
            /// M: [SystemUI] Support SimIndicator, show SimIndicator when notification panel is visible. @{
            if (mToolBarView.mSimSwitchPanelView.isPanelShowing()) {
                mToolBarSwitchPanel.setVisibility(View.GONE);
            }
            /// M: [SystemUI] Support SimIndicator, show SimIndicator when notification panel is visible. @}
            /// M: [SystemUI] Remove settings button to notification header @{.
            if (mHeaderSettingsButton != null) {
                mHeaderSettingsButton.setVisibility(View.GONE);
            }
            /// M: [SystemUI] Remove settings button to notification header @}.
            setAreThereNotifications(); // show the clear button
        }

        mExpandedVisible = false;
        mPile.setLayoutTransitionsEnabled(false);
        if (mNavigationBarView != null)
            mNavigationBarView.setSlippery(false);
        visibilityChanged(false);
		onStatusBarStateChanged(true);//added by wang 20130911

        // Shrink the window to the size of the status bar only
        WindowManager.LayoutParams lp = (WindowManager.LayoutParams) mStatusBarWindow.getLayoutParams();
        lp.height = getStatusBarHeight();
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        //lp.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
        //Log.d("kay18", "+makeExpandedInvisible: mStatusBarWindow:height:" + lp.height);
        mWindowManager.updateViewLayout(mStatusBarWindow, lp);

        if ((mDisabled & StatusBarManager.DISABLE_NOTIFICATION_ICONS) == 0) {
            setNotificationIconVisibility(true, com.android.internal.R.anim.fade_in);
        }

        /// M: [SystemUI] Support "Notification toolbar". {
        mToolBarView.dismissDialogs();
        if (mQS != null) {
            mQS.dismissDialogs();
        }
        /// M: [SystemUI] Support "Notification toolbar". }

        /// M: [SystemUI] Dismiss application guide dialog.@{
        if (mAppGuideDialog != null && mAppGuideDialog.isShowing()) {
            mAppGuideDialog.dismiss();
            Xlog.d(TAG, "performCollapse dismiss mAppGuideDialog");
        }
        /// M: [SystemUI] Dismiss application guide dialog.@}

        // Close any "App info" popups that might have snuck on-screen
        dismissPopups();

        if (mPostCollapseCleanup != null) {
            mPostCollapseCleanup.run();
            mPostCollapseCleanup = null;
        }
    }

    /**
     * Enables or disables layers on the children of the notifications pile.
     *
     * When layers are enabled, this method attempts to enable layers for the minimal
     * number of children. Only children visible when the notification area is fully
     * expanded will receive a layer. The technique used in this method might cause
     * more children than necessary to get a layer (at most one extra child with the
     * current UI.)
     *
     * @param layerType {@link View#LAYER_TYPE_NONE} or {@link View#LAYER_TYPE_HARDWARE}
     */
    public void setPileLayers(int layerType) {
        final int count = mPile.getChildCount();

        switch (layerType) {
            case View.LAYER_TYPE_NONE:
                for (int i = 0; i < count; i++) {
                    mPile.getChildAt(i).setLayerType(layerType, null);
                }
                break;
            case View.LAYER_TYPE_HARDWARE:
                final int[] location = new int[2];
                mNotificationPanel.getLocationInWindow(location);

                final int left = location[0];
                final int top = location[1];
                final int right = left + mNotificationPanel.getWidth();
                final int bottom = top + getExpandedViewMaxHeight();

                final Rect childBounds = new Rect();

                for (int i = 0; i < count; i++) {
                    final View view = mPile.getChildAt(i);
                    view.getLocationInWindow(location);

                    childBounds.set(location[0], location[1],
                            location[0] + view.getWidth(), location[1] + view.getHeight());

                    if (childBounds.intersects(left, top, right, bottom)) {
                        view.setLayerType(layerType, null);
                    }
                }

                break;
        }
    }

    public boolean isClinging() {
        return mCling != null && mCling.getVisibility() == View.VISIBLE;
    }

    public void hideCling() {
        if (isClinging()) {
            mCling.animate().alpha(0f).setDuration(250).start();
            mCling.setVisibility(View.GONE);
            mSuppressStatusBarDrags = false;
        }
    }

    public void showCling() {
        // lazily inflate this to accommodate orientation change
        final ViewStub stub = (ViewStub) mStatusBarWindow.findViewById(R.id.status_bar_cling_stub);
        if (stub == null) {
            mClingShown = true;
            return; // no clings on this device
        }

        mSuppressStatusBarDrags = true;

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCling = (ViewGroup) stub.inflate();

                mCling.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true; // e eats everything
                    }});
                mCling.findViewById(R.id.ok).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        hideCling();
                    }});

                mCling.setAlpha(0f);
                mCling.setVisibility(View.VISIBLE);
                mCling.animate().alpha(1f);

                mClingShown = true;
                SharedPreferences.Editor editor = Prefs.edit(mContext);
                editor.putBoolean(Prefs.SHOWN_QUICK_SETTINGS_HELP, true);
                editor.apply();

                makeExpandedVisible(true); // enforce visibility in case the shade is still animating closed
                animateExpandNotificationsPanel();

                mSuppressStatusBarDrags = false;
            }
        }, 500);

        animateExpandNotificationsPanel();
    }

    public boolean interceptTouchEvent(MotionEvent event) {
        Slog.d("kay", "Touch: rawY=" + event.getRawY());
        if (SPEW) {
            Slog.d(TAG, "Touch: rawY=" + event.getRawY() + " event=" + event + " mDisabled="
                + mDisabled + " mTracking=" + mTracking);
        } else if (CHATTY) {
            if (event.getAction() != MotionEvent.ACTION_MOVE) {
                Slog.d(TAG, String.format(
                            "panel: %s at (%f, %f) mDisabled=0x%08x",
                            MotionEvent.actionToString(event.getAction()),
                            event.getRawX(), event.getRawY(), mDisabled));
            }
        }

        if (DEBUG_GESTURES) {
            mGestureRec.add(event);
        }

		//added by wang 20130830 start
		if(event.getAction() == MotionEvent.ACTION_DOWN) {
			if(mControlCenterShow) {
				hideControlCenter();
			}
		}
		//added by wang 20130830 end


        // Cling (first-run help) handling.
        // The cling is supposed to show the first time you drag, or even tap, the status bar.
        // It should show the notification panel, then fade in after half a second, giving you 
        // an explanation of what just happened, as well as teach you how to access quick
        // settings (another drag). The user can dismiss the cling by clicking OK or by 
        // dragging quick settings into view.
        final int act = event.getActionMasked();
        if (mSuppressStatusBarDrags) {
            return true;
        } else if (act == MotionEvent.ACTION_UP && !mClingShown) {
            showCling();
        } else {
            hideCling();
        }



		if(event.getAction() == MotionEvent.ACTION_UP){
			  Slog.d(TAG, "xujiaTouch: rawY=" + event.getRawY() );
			 if(event.getRawY()==0.0){

					final Intent intent = new Intent("close_frame_offset");

        			       mContext.sendBroadcast(intent);
			 	}

			}



        return false;
    }

    public GestureRecorder getGestureRecorder() {
        return mGestureRec;
    }

    @Override // CommandQueue
    public void setNavigationIconHints(int hints) {
        if (hints == mNavigationIconHints) return;

        mNavigationIconHints = hints;

        if (mNavigationBarView != null) {
            mNavigationBarView.setNavigationIconHints(hints);
        }
    }




    private View mBottomBackgroundView;//add by scq 20161008
    private WindowManager.LayoutParams mBottomLpBackground;//add by scq 20161008
    /*Begin: added by scq 20160907*/
    private void setBottomWindowBackground(){
        if(mBottomBackgroundView != null){
            setDisplayStatus(true);
            setHieOrShow(false);
            return;
        }
        mBottomBackgroundView = LayoutInflater.from(mContext).inflate(com.android.internal.R.layout.bottom_panel, null);
        final int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                flags,
                PixelFormat.TRANSLUCENT);
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.gravity = Gravity.BOTTOM | Gravity.FILL_HORIZONTAL;
        //lp.y = mControlCenterNormalY;
        lp.setTitle("BottomWindowBackground");
        lp.packageName = mContext.getPackageName();
        mWindowManager.addView(mBottomBackgroundView, lp);
        mBottomLpBackground = (WindowManager.LayoutParams) mBottomBackgroundView.getLayoutParams();
        mBottomBackgroundView.setVisibility(View.GONE);
    }
/*End:added by scq 20160907*/

    @Override // CommandQueue
    public void setSystemUiVisibility(int vis, int mask) {
        final int oldVal = mSystemUiVisibility;
        final int newVal = (oldVal&~mask) | (vis&mask);
        final int diff = newVal ^ oldVal;

        if (diff != 0) {
            mSystemUiVisibility = newVal;

            if (0 != (diff & View.SYSTEM_UI_FLAG_LOW_PROFILE)) {
                final boolean lightsOut = (0 != (vis & View.SYSTEM_UI_FLAG_LOW_PROFILE));
                if (lightsOut) {
                    animateCollapsePanels();
                    if (mTicking) {
                        haltTicker();
                    }
                }

                if (mNavigationBarView != null) {
                    mNavigationBarView.setLowProfile(lightsOut);
                }

                setStatusBarLowProfile(lightsOut);
            }

            notifyUiVisibilityChanged();
        }
    }

    private void setStatusBarLowProfile(boolean lightsOut) {
        if (mLightsOutAnimation == null) {
            final View notifications = mStatusBarView.findViewById(R.id.notification_icon_area);
            final View systemIcons = mStatusBarView.findViewById(R.id.statusIcons);
            final View signal = mStatusBarView.findViewById(R.id.signal_cluster);
            final View battery = mStatusBarView.findViewById(R.id.battery);
            final View clock = mStatusBarView.findViewById(R.id.clock);

            final AnimatorSet lightsOutAnim = new AnimatorSet();
            lightsOutAnim.playTogether(
                    ObjectAnimator.ofFloat(notifications, View.ALPHA, 0),
                    ObjectAnimator.ofFloat(systemIcons, View.ALPHA, 0),
                    ObjectAnimator.ofFloat(signal, View.ALPHA, 0),
                    ObjectAnimator.ofFloat(battery, View.ALPHA, 0.5f),
                    ObjectAnimator.ofFloat(clock, View.ALPHA, 0.5f)
                );
            lightsOutAnim.setDuration(750);

            final AnimatorSet lightsOnAnim = new AnimatorSet();
            lightsOnAnim.playTogether(
                    ObjectAnimator.ofFloat(notifications, View.ALPHA, 1),
                    ObjectAnimator.ofFloat(systemIcons, View.ALPHA, 1),
                    ObjectAnimator.ofFloat(signal, View.ALPHA, 1),
                    ObjectAnimator.ofFloat(battery, View.ALPHA, 1),
                    ObjectAnimator.ofFloat(clock, View.ALPHA, 1)
                );
            lightsOnAnim.setDuration(250);

            mLightsOutAnimation = lightsOutAnim;
            mLightsOnAnimation = lightsOnAnim;
        }

        mLightsOutAnimation.cancel();
        mLightsOnAnimation.cancel();

        final Animator a = lightsOut ? mLightsOutAnimation : mLightsOnAnimation;
        a.start();

        setAreThereNotifications();
    }

    private boolean areLightsOn() {
        return 0 == (mSystemUiVisibility & View.SYSTEM_UI_FLAG_LOW_PROFILE);
    }

    public void setLightsOn(boolean on) {
        Log.v(TAG, "setLightsOn(" + on + ")");
        if (on) {
            setSystemUiVisibility(0, View.SYSTEM_UI_FLAG_LOW_PROFILE);
        } else {
            setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE, View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }

    private void notifyUiVisibilityChanged() {
        try {
            mWindowManagerService.statusBarVisibilityChanged(mSystemUiVisibility);
        } catch (RemoteException ex) {
        }
    }

    public void topAppWindowChanged(boolean showMenu) {
        if (DEBUG) {
            Slog.d(TAG, (showMenu?"showing":"hiding") + " the MENU button");
        }
        if (mNavigationBarView != null) {
            mNavigationBarView.setMenuVisibility(showMenu);
        }

        // See above re: lights-out policy for legacy apps.
        if (showMenu) setLightsOn(true);
    }

    @Override
    public void setImeWindowStatus(IBinder token, int vis, int backDisposition) {
        boolean altBack = (backDisposition == InputMethodService.BACK_DISPOSITION_WILL_DISMISS)
            || ((vis & InputMethodService.IME_VISIBLE) != 0);

        mCommandQueue.setNavigationIconHints(
                altBack ? (mNavigationIconHints | StatusBarManager.NAVIGATION_HINT_BACK_ALT)
                        : (mNavigationIconHints & ~StatusBarManager.NAVIGATION_HINT_BACK_ALT));
        if (mQS != null) mQS.setImeWindowStatus(vis > 0);
    }

    @Override
    public void setHardKeyboardStatus(boolean available, boolean enabled) {}

    @Override
    protected void tick(IBinder key, StatusBarNotification n, boolean firstTime) {
        // no ticking in lights-out mode
        if (!areLightsOn()) return;

        // no ticking in Setup
        if (!isDeviceProvisioned()) return;

        // not for you
        if (!notificationIsForCurrentUser(n)) return;

        // Show the ticker if one is requested. Also don't do this
        // until status bar window is attached to the window manager,
        // because...  well, what's the point otherwise?  And trying to
        // run a ticker without being attached will crash!
        if (n.notification.tickerText != null && mStatusBarWindow.getWindowToken() != null) {
            if (0 == (mDisabled & (StatusBarManager.DISABLE_NOTIFICATION_ICONS
                            | StatusBarManager.DISABLE_NOTIFICATION_TICKER))) {
                //mTicker.addEntry(n);//removed by wang 20130729
            }
        }
    }

    private class MyTicker extends Ticker {
        MyTicker(Context context, View sb) {
            super(context, sb);
        }

        @Override
        public void tickerStarting() {
            mTicking = true;
            mStatusBarContents.setVisibility(View.GONE);
            mTickerView.setVisibility(View.VISIBLE);
            mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_up_in, null));
            mStatusBarContents.startAnimation(loadAnim(com.android.internal.R.anim.push_up_out, null));
        }

        @Override
        public void tickerDone() {
            mStatusBarContents.setVisibility(View.VISIBLE);
            mTickerView.setVisibility(View.GONE);
            mStatusBarContents.startAnimation(loadAnim(com.android.internal.R.anim.push_down_in, null));
            mTickerView.startAnimation(loadAnim(com.android.internal.R.anim.push_down_out,
                        mTickingDoneListener));
        }

        public void tickerHalting() {
            mStatusBarContents.setVisibility(View.VISIBLE);
            mTickerView.setVisibility(View.GONE);
            mStatusBarContents.startAnimation(loadAnim(com.android.internal.R.anim.fade_in, null));
            // we do not animate the ticker away at this point, just get rid of it (b/6992707)
        }
    }

    Animation.AnimationListener mTickingDoneListener = new Animation.AnimationListener() {;
        public void onAnimationEnd(Animation animation) {
            mTicking = false;
        }
        public void onAnimationRepeat(Animation animation) {
        }
        public void onAnimationStart(Animation animation) {
        }
    };

    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(mContext, id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    }

    public static String viewInfo(View v) {
        return "[(" + v.getLeft() + "," + v.getTop() + ")(" + v.getRight() + "," + v.getBottom()
                + ") " + v.getWidth() + "x" + v.getHeight() + "]";
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        synchronized (mQueueLock) {
            pw.println("Current Status Bar state:");
            pw.println("  mExpandedVisible=" + mExpandedVisible
                    + ", mTrackingPosition=" + mTrackingPosition);
            pw.println("  mTicking=" + mTicking);
            pw.println("  mTracking=" + mTracking);
            pw.println("  mNotificationPanel=" +
                    ((mNotificationPanel == null)
                            ? "null"
                            : (mNotificationPanel + " params=" + mNotificationPanel.getLayoutParams().debug(""))));
            pw.println("  mAnimating=" + mAnimating
                    + ", mAnimY=" + mAnimY + ", mAnimVel=" + mAnimVel
                    + ", mAnimAccel=" + mAnimAccel);
            pw.println("  mAnimLastTimeNanos=" + mAnimLastTimeNanos);
            pw.println("  mAnimatingReveal=" + mAnimatingReveal
                    + " mViewDelta=" + mViewDelta);
            pw.println("  mDisplayMetrics=" + mDisplayMetrics);
            pw.println("  mPile: " + viewInfo(mPile));
            pw.println("  mTickerView: " + viewInfo(mTickerView));
            pw.println("  mScrollView: " + viewInfo(mScrollView)
                    + " scroll " + mScrollView.getScrollX() + "," + mScrollView.getScrollY());
        }

        pw.print("  mNavigationBarView=");
        if (mNavigationBarView == null) {
            pw.println("null");
        } else {
            mNavigationBarView.dump(fd, pw, args);
        }

        if (DUMPTRUCK) {
            synchronized (mNotificationData) {
                int N = mNotificationData.size();
                pw.println("  notification icons: " + N);
                for (int i=0; i<N; i++) {
                    NotificationData.Entry e = mNotificationData.get(i);
                    pw.println("    [" + i + "] key=" + e.key + " icon=" + e.icon);
                    StatusBarNotification n = e.notification;
                    pw.println("         pkg=" + n.pkg + " id=" + n.id + " score=" + n.score);
                    pw.println("         notification=" + n.notification);
                    pw.println("         tickerText=\"" + n.notification.tickerText + "\"");
                }
            }

            int N = mStatusIcons.getChildCount();
            pw.println("  system icons: " + N);
            for (int i=0; i<N; i++) {
                StatusBarIconView ic = (StatusBarIconView) mStatusIcons.getChildAt(i);
                pw.println("    [" + i + "] icon=" + ic);
            }

            if (false) {
                pw.println("see the logcat for a dump of the views we have created.");
                // must happen on ui thread
                mHandler.post(new Runnable() {
                        public void run() {
                            mStatusBarView.getLocationOnScreen(mAbsPos);
                            Slog.d(TAG, "mStatusBarView: ----- (" + mAbsPos[0] + "," + mAbsPos[1]
                                    + ") " + mStatusBarView.getWidth() + "x"
                                    + getStatusBarHeight());
                            mStatusBarView.debug();
                        }
                    });
            }
        }

        if (DEBUG_GESTURES) {
            pw.print("  status bar gestures: ");
            mGestureRec.dump(fd, pw, args);
        }

        /// M: [SystemUI] Support "Dual SIM". {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mNetworkControllerGemini.dump(fd, pw, args);
        } else {
            mNetworkController.dump(fd, pw, args);
        }
        /// M: [SystemUI] Support "Dual SIM". }
    }

    @Override
    public void createAndAddWindows() {

        System.out.println("xujia createAndAddWindows");
        addStatusBarWindow();
         isAllowInActivity=Settings.System.getInt(mContext.getContentResolver(),
								Settings.System.ALLOW_IN_ACTIVITY, -1)!= 0;

		addBottomWindow(true);//added by wang 20130801//modified by lzp for bug [2814]

        if(!isAllowInActivity&&!isLauncherForeground(mContext)){
			   mBottomRootView.setVisibility(View.GONE);
	       }else{
			  mBottomRootView.setVisibility(View.VISIBLE);
		}

          /*begin added by xujia*/
          eventsChanged();
          /*end by xujia*/


    }

    private void addStatusBarWindow() {
        // Put up the view
        final int height = getStatusBarHeight();
        Log.d("kay18", "addStatusBarWindow: height:" + height);
        // Now that the status bar window encompasses the sliding panel and its
        // translucent backdrop, the entire thing is made TRANSLUCENT and is
        // hardware-accelerated.
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height,
                WindowManager.LayoutParams.TYPE_STATUS_BAR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);

        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        //lp.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
        lp.gravity = getStatusBarGravity();
        lp.setTitle("StatusBar");
        lp.packageName = mContext.getPackageName();

        makeStatusBarView();
        //Log.d("kay18", "addStatusBarWindow: lp.height:" + lp.height);
        mWindowManager.addView(mStatusBarWindow, lp);
    }



//added by wang 20130801 start
	public void closeControlCenter() {
		closeBottomPanel();
	}



	private LinearLayout mBottomWindow;
	private BottomPanelBar mBottomRootView;
    //private BottomPanelBar mBottomRootView;
	private WindowManager.LayoutParams mBottomLp;
    private VelocityTracker mBottomVelocityTracker = null;//add by scq
    private static final int DIRECTION_LEFT_RIGHT = 1;//add by scq
    private static final int DIRECTION_UP_DOWN = 2;//add by scq
    private float mLastY;
    private float mLastX;//add by scq
	private int mDirection;
	private static final int DIRECTION_NONE = -1;
	private static final int DIRECTION_UP = 1;//move from bottom to top
	private static final int DIRECTION_DOWN = 2;//move from top to bottom
	private static final int DIRECTION_FROM_TOP_MAX_TO_TOP = 3;
    private static final int BOTTOM_MAX_LIMIT = -666;//add by scq 20161008

	private static final int SCREEN_HEIGHT = FeatureOption.LCM_HEIGHT;

	private int mControlCenterHeight, mControlCenterMinY, mControlCenterNormalY, mControlCenterMaxY;

	//if screen off more than 3 seconds, then dismiss control center if it's already popup
	private static final int DISMISS_PANEL_DURATION = 3000;

	private boolean mControlCenterShow = false;
	private int mPreConfigOrientation;

	private KeyguardManager mKeyguardManager;

    private void addBottomWindow(boolean firstAdd) {//modified by lzp for bug [2814]
    	makeBottomWindow(firstAdd);//.setOnTouchListener(this);//modified by lzp for bug [2814]
//        setBottomWindowBackground();// add by csc
		mControlCenterHeight = mBottomRootView.getContentHeight();
		mControlCenterMinY = -mControlCenterHeight;//completely hidden the bottom window
		mControlCenterNormalY = mControlCenterMinY + mBottomRootView.getTouchHandleHeight();//just show a little bottom window for touch purpose
		mControlCenterMaxY = SCREEN_HEIGHT - mControlCenterHeight;//completely shown the bottom window


        // Put up the view
        final int height = mControlCenterHeight;

		final int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    //| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    //| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                    //| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                    //| WindowManager.LayoutParams.FLAG_DIM_BEHIND
                    | WindowManager.LayoutParams.FLAG_HOMEKEY_DISPATCHED;

        // Now that the status bar window encompasses the sliding panel and its
        // translucent backdrop, the entire thing is made TRANSLUCENT and is
        // hardware-accelerated.
        final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                height,
                WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                flags,
                PixelFormat.TRANSLUCENT);

        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.gravity = Gravity.BOTTOM | Gravity.FILL_HORIZONTAL;
		lp.y = mControlCenterNormalY;
        lp.setTitle("BottomWindow");
        lp.packageName = mContext.getPackageName();

    //    mWindowManager.addView(mBottomRootView, lp);
    //
	//	mBottomLp = (WindowManager.LayoutParams) mBottomRootView.getLayoutParams();
		mKeyguardManager = (KeyguardManager) mContext.getSystemService(Context.KEYGUARD_SERVICE);
        Log.d("kay6", "addBottomWindow: " + mKeyguardManager.isKeyguardLocked());
    }

    private Bitmap bottomPanelBg = null;//added by lzp for bug [2814]
	private void updateFavoritePanelBg() {
		WallpaperManager wallpaperManager = WallpaperManager.getInstance(mContext);
	  int  wallPaperWidth = wallpaperManager.getDesiredMinimumWidth();
	int	wallPaperHeight = wallpaperManager.getDesiredMinimumHeight();

		WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
	    DisplayMetrics metrics = new DisplayMetrics();
	    wm.getDefaultDisplay().getMetrics(metrics);
		final int screenWidth = metrics.widthPixels;
		final int screenHeight = metrics.heightPixels;


     //   final int clipLeft = (wallpaperManager.getBitmap().getWidth() - screenHeight)/2, clipTop = (wallpaperManager.getBitmap().getHeight() - screenWidth)/2;
	//	final int clipLeft = (wpm.getBitmap().getWidth() - screenWidth)/2, clipTop = (wpm.getBitmap().getHeight() - screenHeight)/2;
	  final int clipLeft = screenHeight>screenWidth ?(wallpaperManager.getBitmap().getWidth() - screenWidth)/2:(wallpaperManager.getBitmap().getWidth() - screenHeight)/2, clipTop = screenHeight>screenWidth ?(wallpaperManager.getBitmap().getHeight() - screenHeight)/2:(wallpaperManager.getBitmap().getHeight() - screenWidth)/2;
	//	final int clipLeft = (wpm.getBitmap().getWidth() - screenWidth)/2, clipTop = (wpm.getBitmap().getHeight() - screenHeight)/2;

/*landsview.setBackground(new BitmapDrawable(mContext.getResources(), BitmapUtils.BoxBlurFilter(Bitmap.createBitmap(wallpaperManager.getBitmap(),
				clipLeft, clipTop, (screenHeight>screenWidth?screenWidth:screenHeight ),getStatusBarHeight(),
				null,false))));*/
landsview.setBackgroundColor(0xff786f7b);
	
	/*	landsview.setBackground(new BitmapDrawable(mContext.getResources(), BitmapUtils.BoxBlurFilter(Bitmap.createBitmap(wallpaperManager.getBitmap(),
				clipLeft, clipTop+(screenWidth -getStatusBarHeight()), screenHeight ,getStatusBarHeight(),
				null,false))));*/
	}

private View landsview,clockpaddingView;
private int statusBarColor=-1;
    protected void onConfigurationChanged(Configuration newConfig) {
		if(mPreConfigOrientation != newConfig.orientation) {
                     System.out.println("xujia onConfigurationChanged");
					 
					 
					 
					 
					 /*Begin added by xujia*/

					 if(isCurrentActivityLauncher()){
	            		if(newConfig.orientation==2){
					if(statusBarColor==-1){
						statusBarColor=BitmapUtils.getPanpelWallpaperColor(mContext);
						}
					     if(isLiveWallpaperNow()){
						landsview.setBackgroundResource(R.drawable.panel_bg);//added by xujia for set Live wallpaper
					     	}else{
						landsview.setBackgroundColor(statusBarColor);

					     		}
					mUpdateHandler.postDelayed(mshowStatusBar, 300);
					landsview.setVisibility(View.VISIBLE);
					clockpaddingView.setVisibility(View.VISIBLE);
                                       backToPhoneLandsView.setVisibility(View.INVISIBLE);//added by xss for back to phone
	            			}else{

						mUpdateHandler.postDelayed(mresetStatusBar, 300);

	            			}
				}else{
					if(landsview.getVisibility()==View.VISIBLE){
						mUpdateHandler.postDelayed(mresetStatusBar, 300);

						}

					}
				/*End added by xujia*/
					 /**Begin: modified by lzp for bug [2814] **/
            	    if(mBottomRootView != null){
						bottomPanelBg = mBottomRootView.getBgDrawable();
 //                   	mWindowManager.removeView(mBottomRootView);  //by kay
            	    }
		    		mBottomRootView.clear();
                    addBottomWindow(false);
					/**End: modified by lzp for bug [2814] **/
			        correctWindowPosition(mInputMethodShow ? mControlCenterMinY : mControlCenterNormalY);

			    if(!isAllowInActivity&&!isLauncherForeground(mContext))
			    {
			        mBottomRootView.setVisibility(View.GONE);
			    }else
			        {
			            mBottomRootView.setVisibility(View.VISIBLE);
			        }
	    }
    }

	public void hideControlCenter() {
        closeBottomPanel();
	}
	private View makeBottomWindow(boolean firstAdd) {//modified by lzp for bug [2814]
		final Context context = mContext;
		mPreConfigOrientation = context.getResources().getConfiguration().orientation;
	//	mBottomRootView = new BottomPanel(context);//View.inflate(context, R.layout.bottom_panel, null);
    //    mBottomRootView = new BottomPanelBar(context);
        mBottomRootView = mPageSetting;
        //mBottomRootView.setPhoneStatusBar(this);
        /**Begin: added by lzp for bug [2814] **/
		if(!firstAdd && bottomPanelBg != null) {
			mBottomRootView.setIsChangeBackgroundAttached(firstAdd);
	//		mBottomRootView.setBgDrawable(bottomPanelBg);
		}
		/**End: added by lzp for bug [2814] **/
		mBottomRootView.setCallback(this);
		//mBottomRootView.setPhoneStatusBar(this);
		final View touchView = mBottomRootView.mTitleLayout;
        //modify by kay
		/*final View touchHandle = mBottomRootView.mTouchHandle;
		if(touchView != null) {
			touchView.setOnTouchListener(this);
		}

		if(touchHandle != null) {
			touchHandle.setOnTouchListener(setOnTouchListener);
		}*/
		return mBottomRootView;
	}

	private void closeStatusBarIfPossible() {
		if (mExpandedVisible && !mAnimating) {
			animateCollapsePanels();
		}
	}

	//if status bar expanded, then hide control center completely;
	//else show control center's touch handle
	private void onStatusBarStateChanged(boolean showControlCenter) {
		final View touchView = mBottomRootView.mTitleLayout;
		final View touchHandle = mBottomRootView.mTouchHandle;
		if(showControlCenter) {
			correctWindowPosition(mControlCenterNormalY);
		} else {
			correctWindowPosition(mControlCenterMinY);
		}
	}


	private boolean mScreenLocked = false;
	private boolean mInputMethodShow = false;











    /**Start: add by scq 20161010**/
    private void setDisplayStatus(boolean isDisplay){
        if(mBottomBackgroundView != null) {
            if(!isDisplay){
                if(mBottomBackgroundView.getVisibility() == View.GONE)mBottomBackgroundView.setVisibility(View.VISIBLE);
            }else{
                if(mBottomBackgroundView.getVisibility() == View.VISIBLE)mBottomBackgroundView.setVisibility(View.GONE);
            }
        }
    }

    private void setHieOrShow(boolean isDisplay){
        Intent intent = new Intent();
        intent.setAction("com.bottomlits");
        intent.putExtra("isDisplay",isDisplay);
        mContext.sendBroadcast(intent);
    }

    private void onTouchY(int dy){
        if(dy > 0) {
            mDirection = DIRECTION_DOWN;
        } else if(dy < 0) {
            mDirection = DIRECTION_UP;
				/*Begin:added by xss for blur*/
            if(DEBUG)Log.i(TAG, "added by xss for blur   dy < 0");
            Intent it=new Intent();
            it.setAction("workspace_scroll_to_right");
            mContext.sendBroadcast(it);
				/*End:added by xss for blur*/
				/*Begin:added by xss for back to last app*/
           notificationPanelViewIsShow=true;
            lastAppPackageName=getTopActivityPackageName();
            lastAppClassName=getTopActivityClassName();
				/*End:added by xss for back to last app*/
        }
//        if(mBottomLp.y > 0) {
//            dy = dy / 2;
            //now y has greater than mControlCenterMaxY, so it'll reset y to mControlCenterMaxY
//            mDirection = DIRECTION_FROM_TOP_MAX_TO_TOP;
//        }
        if(!mBottomRootView.isTitleLineStraight() && dy != 0) {//if move, then animate title line to straight
            mBottomRootView.animateCurveToStraightLine();
        }
//        mBottomLp.y += -dy;
			/*Begin: added by scq 20161008*/
//        if(mBottomLp.y>BOTTOM_MAX_LIMIT){
//            if(mBottomBackgroundView != null)mBottomBackgroundView.setVisibility(View.VISIBLE);
//        }else{
//            if(mBottomBackgroundView != null)mBottomBackgroundView.setVisibility(View.GONE);
 //       }
			/*End: added by scq 20161008*/
 //       if(mBottomLp.y >= 0) {
 //           mBottomLp.y = 0;
 //           mBottomRootView.setLayoutParams(mBottomLp);
//            mWindowManager.updateViewLayout(mBottomRootView, mBottomLp);
 //       }
    }

    private View.OnTouchListener setOnTouchListener = new View.OnTouchListener (){
        @Override
        public boolean onTouch(View v, MotionEvent ev) {
            if(!mNotificationPanel.isFullyCollapsed()) {
                return mNotificationPanel.onTouchEvent(ev);//add by joyisn
            }
           /* if(panelHolder.isInBouncerView()){//add byjoyisn
                return false;
            }*/
            final int action = ev.getAction();
            switch(action) {
                case MotionEvent.ACTION_DOWN: {
                    Log.d("kay1", "*****************setOnTouchListener  onTouch ACTION_DOWN *****************");
//                    mBottomRootView.setLayoutParams(mBottomLp);
//                    mWindowManager.updateViewLayout(mBottomRootView, mBottomLp);
                    mBottomRootView.invalidate();
//                    userActivity();
                    if (mExpandedVisible && !mAnimating) {
                        return false;
                    }
                    mLastY = ev.getRawY();
                    mDirection = DIRECTION_NONE;
                    /**begin:added by gaojunbin**/
                  /*  isAlowInLock =Settings.System.getInt(mContext.getContentResolver(),
                            Settings.System.ALLOW_IN_LOCK, -1) != 0;
                    isAlowConterInActivity=Settings.System.getInt(mContext.getContentResolver(),
                            Settings.System.PHONE_CONTROL_CENTER_IN_ACTIVITY, -1)!= 0;*/
                    /**end:added by gaojunbin**/
                    //mUpdateHandler.obtainMessage(DIRECTION_NONE).sendToTarget();
                    break;
                }
                case MotionEvent.ACTION_OUTSIDE:
                case MotionEvent.ACTION_MOVE: {
                    int dy = (int) (ev.getRawY() - mLastY);
                 //   Log.d("kay1", "onTouch: " + dy);
                    /**begin:added by gaojunbin**/
                   /* if(isInLockScreen()){
                        if(isAlowInLock){
                            onTouchY(dy);
                        }
                    }else{
                        if(isLauncherForeground(mContext)){
                            onTouchY(dy);
                            break;
                        }else {
                            if(isAlowConterInActivity){
                                onTouchY(dy);
                                break;
                            }
                        }
                    }*/
                    onTouchY(dy);
                    /**end:added by gaojunbin**/

                    //onTouchY(dy);
                    mLastY = ev.getRawY();
                    break;
                }
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP: {
                    Log.d("kay1", "onTouch: ACTION_UP");
                    if(mDirection == DIRECTION_UP) {
                        mUpdateHandler.obtainMessage(DIRECTION_UP).sendToTarget();
                    } else if(mDirection == DIRECTION_DOWN) {
                        mUpdateHandler.obtainMessage(DIRECTION_DOWN).sendToTarget();
                    } else if(mDirection == DIRECTION_FROM_TOP_MAX_TO_TOP) {
                        mUpdateHandler.obtainMessage(DIRECTION_FROM_TOP_MAX_TO_TOP).sendToTarget();
                    }
                    mDirection = DIRECTION_NONE;
                    break;
                }
            }
            return true;
        }
    };
    private int mAnyway = DIRECTION_NONE;
    private boolean isRun = false;
    /**End: add by scq 20161010**/








    @Override
	public boolean onTouch(View v, MotionEvent ev) {
		Log.d(TAG, "*****************onTouch*******************");

		final int action = ev.getAction();
		switch(action) {
		case MotionEvent.ACTION_DOWN: {
			//closeStatusBarIfPossible();

		/*	if (mExpandedVisible && !mAnimating) {
				return false;
			}*/
              mAnyway = DIRECTION_NONE;//added by xss for BottomPanel scroll
            if (mBottomVelocityTracker == null) {
                mBottomVelocityTracker = VelocityTracker.obtain();
            }else{
                mBottomVelocityTracker.clear();
            }
            mBottomVelocityTracker.addMovement(ev);
            mBottomRootView.mLastX = ev.getRawX();//add by scq
			mLastY = ev.getRawY();
			mDirection = DIRECTION_NONE;
			mUpdateHandler.obtainMessage(DIRECTION_NONE).sendToTarget();

			break;
		}
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_MOVE: {
            int dy = (int) (ev.getRawY() - mLastY);
            int dx = (int) (ev.getRawX() - mLastX);
			//to determine finger's direction: move up or move down
            if(ev.getPointerCount() > 1) {//add by scq
                return false;
            }
            int velocityX = 0;//add by scq
            int velocityY = 0;//add by scq

            if(mBottomVelocityTracker != null){//add by scq
                mBottomVelocityTracker.addMovement(ev);
                mBottomVelocityTracker.computeCurrentVelocity(1000);
                velocityX = (int) mBottomVelocityTracker.getXVelocity();
                velocityY = (int) mBottomVelocityTracker.getYVelocity();
            }
            /*if(mAnyway == DIRECTION_LEFT_RIGHT){
                Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::1111111111111");
                return mBottomRootView.onTouchEvent(ev);
            }*/
            Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::dy:"+dy+"  dx::"+dx+" velocityX::"+velocityX+"  velocityY::"+velocityY +"::isRun::"+isRun+" count::"+ev.getPointerCount());
            if((Math.abs(velocityX) > Math.abs(velocityY) && (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 20) && ev.getPointerCount() == 1 && mAnyway != DIRECTION_UP_DOWN) /*|| mBottomRootView.isScrollToRightOrLeft()*/){//add by scq  //modified by xss for BottomPanel scroll
                //Log.d("Alinscq", "*******xxxx****1***" + touchViewPager.onTouchEvent(ev));
                Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::left");
                mAnyway = DIRECTION_LEFT_RIGHT;//add by scq
                mLastX = ev.getRawX();//add by scq
                mLastY = ev.getRawY();//add by scqs
                return mBottomRootView.onTouchEvent(ev);

            }else if(Math.abs(velocityX) < Math.abs(velocityY) && Math.abs(dx) < Math.abs(dy) /*&& (!isRun)*/ && ev.getPointerCount() == 1 && mAnyway != DIRECTION_LEFT_RIGHT) {//add by scq
                Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::up");

                isRun = true;
                mAnyway = DIRECTION_UP_DOWN;//add by scq
                //to determine finger's direction: move up or move down
                if (!mBottomRootView.isTitleLineStraight() && dy != 0) {//if move, then animate title line to straight
                    mBottomRootView.animateCurveToStraightLine();
                }
//                mBottomLp.y += -dy;
 //               mBottomRootView.setLayoutParams(mBottomLp);
//                mWindowManager.updateViewLayout(mBottomRootView, mBottomLp);
                if (dy > 0) {
                    Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::dy>0");
                    mDirection = DIRECTION_DOWN;
                    return true;
                } else if (dy < 0) {
                    Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::dy<0");
                    mDirection = DIRECTION_UP;
					/*Begin:added by xss for back to last app*/
					notificationPanelViewIsShow=true;
					lastAppPackageName=getTopActivityPackageName();
					lastAppClassName=getTopActivityClassName();
					/*End:added by xss for back to last app*/
                }


                mLastY = ev.getRawY();
                mLastX = ev.getRawX();
                //Log.d(TAG, "****onTouch ACTION_OUTSIDE ACTION_MOVE******mBottomLp.y="+mBottomLp.y+" mBottomLp.height="+mBottomLp.height);
            }
			break;
		}
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
            Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::1111111111111121212121");
			if(mDirection == DIRECTION_UP) {
				mUpdateHandler.obtainMessage(DIRECTION_UP).sendToTarget();
			} else if(mDirection == DIRECTION_DOWN) {
				mUpdateHandler.obtainMessage(DIRECTION_DOWN).sendToTarget();
			} else if(mDirection == DIRECTION_FROM_TOP_MAX_TO_TOP) {
				mUpdateHandler.obtainMessage(DIRECTION_FROM_TOP_MAX_TO_TOP).sendToTarget();
			}

			mDirection = DIRECTION_NONE;
			break;
		}
		}
        Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::1111111111111222");
        if(mAnyway == DIRECTION_LEFT_RIGHT){
            return mBottomRootView.onTouchEvent(ev);
        }else{
            return true;

        }

        //Log.d("Alin-scq", "*****************onTouch*****************");
        /*if(!mNotificationPanel.isFullyCollapsed()) {
            //Log.d("Alin-scq", "******onTouch === mNotificationPanel.onTouchEvent(ev)*******");
            Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::1111");
            return mNotificationPanel.onTouchEvent(ev);//add by joyisn
        }
        *//*if(panelHolder.isInBouncerView()){//add byjoyisn
            //Log.d("Alin-scq", "*******onTouch === panelHolder.isInBouncerView()*******");
            return false;
        }*//*
        if(ev.getPointerCount() > 1){//add by scq
            Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::1112");
            return false;
        }

        final int action = ev.getAction();
        switch(action) {
            case MotionEvent.ACTION_DOWN: {
                Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::ACTION_DOWN");
                //Log.d("Alin-scq", "*******onTouch === MotionEvent.ACTION_DOWN*******");
                //if(Launcher.getInstance().getScreen()==0)Launcher.getInstance().getWorkspace().scrollRight();
                Log.d(TAG, "*****************onTouch ACTION_DOWN *****************");
                mBottomRootView.setLayoutParams(mBottomLp);
                mWindowManager.updateViewLayout(mBottomRootView, mBottomLp);
                mBottomRootView.invalidate();
//                userActivity();
                //closeStatusBarIfPossible();
                if (mExpandedVisible && !mAnimating) {
                    return false;
                }


                if (mBottomVelocityTracker == null) {
                    mBottomVelocityTracker = VelocityTracker.obtain();
                }else{
                    mBottomVelocityTracker.clear();
                }
                mBottomVelocityTracker.addMovement(ev);
                mLastX = ev.getRawX();//add by scq
                mLastY = ev.getRawY();
                mDirection = DIRECTION_NONE;
                //mUpdateHandler.obtainMessage(DIRECTION_NONE).sendToTarget();
                break;
            }
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_MOVE: {
                Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::ACTION_MOVE");

                //Log.d("Alin-scq", "*******onTouch === MotionEvent.ACTION_MOVE*******");
                if(ev.getPointerCount() > 1){//add by scq
                    return false;
                }
                int dx = (int) (ev.getRawX() - mLastX);
                int dy = (int) (ev.getRawY() - mLastY);
                int velocityX = 0;//add by scq
                int velocityY = 0;//add by scq
                if(mBottomVelocityTracker != null){//add by scq
                    mBottomVelocityTracker.addMovement(ev);
                    mBottomVelocityTracker.computeCurrentVelocity(1000);
                    velocityX = (int) mBottomVelocityTracker.getXVelocity();
                    velocityY = (int) mBottomVelocityTracker.getYVelocity();
                }
                //Log.d("Alinscq", "velocityX = " + velocityX + " , velocityY = " + velocityY);
                //Log.d("Alinscq", "dx = " + dx + " , dy = " + dy);
                switch(mAnyway){
                    case DIRECTION_LEFT_RIGHT:{
                        return mBottomRootView.onTouchEvent(ev);
                    }
                    case DIRECTION_UP_DOWN:{
                        return true;
                    }
                }//add by scq
                if(Math.abs(velocityX) > Math.abs(velocityY) && (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 20) && ev.getPointerCount() == 1){//add by scq
                    //Log.d("Alinscq", "*******xxxx****1***" + touchViewPager.onTouchEvent(ev));
                    mAnyway = DIRECTION_LEFT_RIGHT;//add by scq
                    mLastX = ev.getRawX();//add by scq
                    mLastY = ev.getRawY();//add by scqs

                }else if(Math.abs(velocityX) < Math.abs(velocityY) && Math.abs(dx) < Math.abs(dy) && (!isRun) && ev.getPointerCount() == 1){//add by scq
                    isRun = true;
                    mAnyway = DIRECTION_UP_DOWN;//add by scq
                    //to determine finger's direction: move up or move down
                    if(dy > 0) {
                        mDirection = DIRECTION_DOWN;
                    } else if(dy < 0) {
                        mDirection = DIRECTION_UP;
					*//*Begin:added by xss for blur*//*
                        if(DEBUG)Log.i(TAG, "added by xss for blur   dy < 0");
                        Intent it=new Intent();
                        it.setAction("workspace_scroll_to_right");
                        mContext.sendBroadcast(it);
					*//*End:added by xss for blur*//*
					*//*Begin:added by xss for back to last app*//*
                      *//*  notificationPanelViewIsShow=true;
                        lastAppPackageName=getTopActivityPackageName();
                        lastAppClassName=getTopActivityClassName();*//*
					*//*End:added by xss for back to last app*//*
                    }
                    mLastX = ev.getRawX();
                    mLastY = ev.getRawY();
                    //Log.d(TAG, "****onTouch ACTION_OUTSIDE ACTION_MOVE******mBottomLp.y="+mBottomLp.y+" mBottomLp.height="+mBottomLp.height);
                }else{
                    Log.d("Alinscq", "----------------null-------------");
                    return false;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                Log.d(TAG, "*****************onTouch ACTION_UP**** mDirection="+mDirection+"  mBottomLp.y="+mBottomLp.y);
                //Log.d("Alin-scq", "*******onTouch === MotionEvent.ACTION_UP*******");
                if(mAnyway == DIRECTION_LEFT_RIGHT){//add by scq
                    //Log.d("Alinscq", "**123456**LEFT_RIGHT : " + touchViewPager.onTouchEvent(ev));
                    mAnyway = DIRECTION_NONE;
                    if(mBottomVelocityTracker != null){
                        mBottomVelocityTracker.clear();
                    }
                    return mBottomRootView.onTouchEvent(ev);//add by scq
                }else if(mAnyway == DIRECTION_UP_DOWN){
                    //Log.d("Alinscq", "**123456**  UP_DOWN : ");
                    if(mDirection == DIRECTION_UP) {
                        mUpdateHandler.obtainMessage(DIRECTION_UP).sendToTarget();
                    } else if(mDirection == DIRECTION_DOWN) {
                        mUpdateHandler.obtainMessage(DIRECTION_DOWN).sendToTarget();
                    } else if(mDirection == DIRECTION_FROM_TOP_MAX_TO_TOP) {
                        mUpdateHandler.obtainMessage(DIRECTION_FROM_TOP_MAX_TO_TOP).sendToTarget();
                    }
                    mDirection = DIRECTION_NONE;
                    mAnyway = DIRECTION_NONE;
                    if(mBottomVelocityTracker != null){
                        mBottomVelocityTracker.clear();
                    }
                }else{//add by scq
                    if(mBottomVelocityTracker != null){
                        mBottomVelocityTracker.clear();
                    }
                    //touchViewPager.setCurrentItem(touchViewPager.getCurrentItem(), true);
                    return false;//add by scq
                }
                break;
            }
        }*/
//        mBottomRootView.setSlideState(mAnyway);//added by lzp
//        return true;


    }

	private static final int ANIMATION_INTERVAL = 5;
	private static final int ANIMATION_MAX_STEP = 25;
	private static final int ANIMATION_MIN_STEP = 5;

	private Handler mUpdateHandler = new Handler() {
		public void handleMessage(Message msg) {
			final int what = msg.what;
            Log.d("kay", "handleMessage: " + what);
           // Log.d("kay", Log.getStackTraceString(new Throwable()));
            switch(what) {
				case DIRECTION_UP: {
//					if(mBottomLp.y < 0) {
//						mBottomLp.y += ANIMATION_MAX_STEP;
//						mBottomRootView.setLayoutParams(mBottomLp);
//						mWindowManager.updateViewLayout(mBottomRootView, mBottomLp);
//						mUpdateHandler.sendEmptyMessageDelayed(DIRECTION_UP, ANIMATION_INTERVAL);
//					} else
                    {//completely show the bottom window
						correctWindowPosition(0);
						mBottomRootView.animateStraightToCurveLine();
						mUpdateHandler.obtainMessage(DIRECTION_NONE).sendToTarget();
						notifyControlCenterState(true);
						mBottomRootView.resetTouchHandle(false);//hide touch handle, so control center full screen
                        mBottomRootView.resetTouchHandleTwo(true);// add by csc
                        Log.d("chenshichun",""+this.getClass().getCanonicalName()+"::::::::::::::resetTouchHandleTwo:::::::false");
					}
					break;
				}
				case DIRECTION_DOWN: {
//					if(mBottomLp.y > mControlCenterNormalY) {
//						mBottomLp.y -= ANIMATION_MAX_STEP;
//						mBottomRootView.setLayoutParams(mBottomLp);
//						mWindowManager.updateViewLayout(mBottomRootView, mBottomLp);
//						mUpdateHandler.sendEmptyMessageDelayed(DIRECTION_DOWN, ANIMATION_INTERVAL);
//					} else
                     {//completely hidden the bottom window
						correctWindowPosition(mControlCenterNormalY);
						mBottomRootView.animateCurveToStraightLine();
						mUpdateHandler.obtainMessage(DIRECTION_NONE).sendToTarget();
						notifyControlCenterState(false);
						mBottomRootView.resetTouchHandle(true);//show touch handle, to listen user's touch envent
                        mBottomRootView.resetTouchHandleTwo(true);// add by csc
                        Log.d("chenshichun",""+this.getClass().getCanonicalName()+"::::::::::::::resetTouchHandleTwo:::::::true");

                    }
					break;
				}
				case DIRECTION_FROM_TOP_MAX_TO_TOP: {
//					if(mBottomLp.y > 0) {
//						mBottomLp.y -= ANIMATION_MIN_STEP;
//						mBottomRootView.setLayoutParams(mBottomLp);
//						mWindowManager.updateViewLayout(mBottomRootView, mBottomLp);
//						mUpdateHandler.sendEmptyMessageDelayed(DIRECTION_FROM_TOP_MAX_TO_TOP, ANIMATION_INTERVAL);
//					} else
                    {
						correctWindowPosition(0);
						mBottomRootView.animateStraightToCurveLine();
						mUpdateHandler.obtainMessage(DIRECTION_NONE).sendToTarget();
						notifyControlCenterState(true);
					}
					break;
				}
				case DIRECTION_NONE: {
					removeMessages(DIRECTION_UP);
					removeMessages(DIRECTION_DOWN);
					break;
				}
			}
		}
	};

	void correctWindowPosition(int yPos) {
		if(mKeyguardManager.isKeyguardLocked()) {//when keyguard was locked, never show control center(completely hide)
			yPos = mControlCenterMinY;
		}
//		mBottomLp.y = yPos;
//		mBottomRootView.setLayoutParams(mBottomLp);
//		mWindowManager.updateViewLayout(mBottomRootView, mBottomLp);
	}

	void onInputMethodStateChanged() {
	/*	if(mInputMethodShow) {
			correctWindowPosition(mControlCenterMinY);
		} else {
			correctWindowPosition(mControlCenterNormalY);
			mBottomRootView.animateCurveToStraightLine();
			notifyControlCenterState(false);
		//	mBottomRootView.resetTouchHandle(true);    //by kay
        //  mBottomRootView.resetTouchHandleTwo(true);// add by csc by kay
           // Log.d("kay",""+this.getClass().getCanonicalName()+"::::::::::::::resetTouchHandleTwo:::::::true1");
           // Log.d("kay",""+"::::::::::::::resetTouchHandleTwo:::::::true1");
        }*/
	}

	public void closeBottomPanel() {
		mUpdateHandler.obtainMessage(DIRECTION_DOWN).sendToTarget();
	}

	private Runnable mClosePanelRunnable = new Runnable() {
		public void run() {
			closeBottomPanel();
		}
	};


	void notifyControlCenterState(boolean show){
        if(true){
            return;
        }

		mControlCenterShow = show;
		if(show) {
			mBottomRootView.resetMusicPlayer();
		}
		Intent intent = new Intent("control_center_action");
		intent.putExtra("control_center_show", show);
        mContext.sendBroadcast(intent);
        //Log.d("kay", Log.getStackTraceString(new Throwable()));
    }

	public boolean isControlCenterShow() {
		return mControlCenterShow;
	}

//added by wang 20130801 end


    void setNotificationIconVisibility(boolean visible, int anim) {
        int old = mNotificationIcons.getVisibility();
        int v = visible ? View.VISIBLE : View.INVISIBLE;
        if (old != v) {
            mNotificationIcons.setVisibility(v);
            mNotificationIcons.startAnimation(loadAnim(anim, null));
        }
    }

    void updateExpandedInvisiblePosition() {
        mTrackingPosition = -mDisplayMetrics.heightPixels;
    }

    static final float saturate(float a) {
        return a < 0f ? 0f : (a > 1f ? 1f : a);
    }

    @Override
    protected int getExpandedViewMaxHeight() {
        return mDisplayMetrics.heightPixels - mNotificationPanelMarginBottomPx;
    }

    @Override
    public void updateExpandedViewPos(int thingy) {
        if (DEBUG) Slog.v(TAG, "updateExpandedViewPos");

        // on larger devices, the notification panel is propped open a bit
        mNotificationPanel.setMinimumHeight(
                (int)(mNotificationPanelMinHeightFrac * mCurrentDisplaySize.y));

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mNotificationPanel.getLayoutParams();
        lp.gravity = mNotificationPanelGravity;
        lp.leftMargin = mNotificationPanelMarginPx;
        mNotificationPanel.setLayoutParams(lp);

        if (mSettingsPanel != null) {
            lp = (FrameLayout.LayoutParams) mSettingsPanel.getLayoutParams();
            lp.gravity = mSettingsPanelGravity;
            lp.rightMargin = mNotificationPanelMarginPx;
            mSettingsPanel.setLayoutParams(lp);
        }

        updateCarrierLabelVisibility(false);
    }

    // called by makeStatusbar and also by PhoneStatusBarView
    void updateDisplaySize() {
        mDisplay.getMetrics(mDisplayMetrics);
        if (DEBUG_GESTURES) {
            mGestureRec.tag("display",
                    String.format("%dx%d", mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels));
        }
    }

    private View.OnClickListener mClearButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            synchronized (mNotificationData) {
                // animate-swipe all dismissable notifications, then animate the shade closed
                int numChildren = mPile.getChildCount();

                int scrollTop = mScrollView.getScrollY();
                int scrollBottom = scrollTop + mScrollView.getHeight();
                final ArrayList<View> snapshot = new ArrayList<View>(numChildren);
                for (int i=0; i<numChildren; i++) {
                    final View child = mPile.getChildAt(i);
                    if (mPile.canChildBeDismissed(child) && child.getBottom() > scrollTop &&
                            child.getTop() < scrollBottom) {
                        snapshot.add(child);
                    }
                }
                if (snapshot.isEmpty()) {
                    animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Decrease the delay for every row we animate to give the sense of
                        // accelerating the swipes
                        final int ROW_DELAY_DECREMENT = 10;
                        int currentDelay = 140;
                        int totalDelay = 0;

                        // Set the shade-animating state to avoid doing other work during
                        // all of these animations. In particular, avoid layout and
                        // redrawing when collapsing the shade.
                        mPile.setViewRemoval(false);

                        mPostCollapseCleanup = new Runnable() {
                            @Override
                            public void run() {
                                if (DEBUG) {
                                    Slog.v(TAG, "running post-collapse cleanup");
                                }
                                try {
                                    mPile.setViewRemoval(true);
                                    mBarService.onClearAllNotifications();
                                } catch (Exception ex) { }
                            }
                        };

                        View sampleView = snapshot.get(0);
                        int width = sampleView.getWidth();
                        final int velocity = width * 8; // 1000/8 = 125 ms duration
                        for (final View _v : snapshot) {
                            mHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mPile.dismissRowAnimated(_v, velocity);
                                }
                            }, totalDelay);
                            currentDelay = Math.max(50, currentDelay - ROW_DELAY_DECREMENT);
                            totalDelay += currentDelay;
                        }
                        // Delay the collapse animation until after all swipe animations have
                        // finished. Provide some buffer because there may be some extra delay
                        // before actually starting each swipe animation. Ideally, we'd
                        // synchronize the end of those animations with the start of the collaps
                        // exactly.
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                animateCollapsePanels(CommandQueue.FLAG_EXCLUDE_NONE);
                            }
                        }, totalDelay + 225);
                    }
                }).start();
                /// M: [SystemUI] Dismiss new event icon when click clear button for keyguard.@{
                Intent intent = new Intent(CLEAR_NEW_EVENT_VIEW_INTENT);
                mContext.sendBroadcast(intent);
                /// M: [SystemUI] Dismiss new event icon when click clear button for keyguard.@}
            }
        }
    };

    public void startActivityDismissingKeyguard(Intent intent, boolean onlyProvisioned) {
        if (onlyProvisioned && !isDeviceProvisioned()) return;
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        animateCollapsePanels();
    }

    private View.OnClickListener mSettingsButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mHasSettingsPanel) {
                animateExpandSettingsPanel();
            } else {
                startActivityDismissingKeyguard(
                        new Intent(android.provider.Settings.ACTION_SETTINGS), true);
            }
        }
    };

    /// M: [SystemUI] Remove settings button to notification header @{.
    private View.OnClickListener mHeaderSettingsButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
                startActivityDismissingKeyguard(new Intent(android.provider.Settings.ACTION_SETTINGS), true);
        }
    };
    /// M: [SystemUI] Remove settings button to notification header @}.

    private View.OnClickListener mClockClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            startActivityDismissingKeyguard(
                    new Intent(Intent.ACTION_QUICK_CLOCK), true); // have fun, everyone
        }
    };

//add by xujia 20130912
    private View.OnClickListener mNotificationButtonListener = new View.OnClickListener() {
        public void onClick(View v) {
            animateExpandNotificationsPanel();
        }
    };

//add by xujia
private int preColor= -1;
public boolean preWhite=false;
private boolean isFirstScreen=false;
private boolean isAllowInActivity=false;
//end by xujia 20130913
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (DEBUG) Slog.v(TAG, "onReceive: " + intent);
            //Slog.v("kay", "onReceive: " + intent);
            String action = intent.getAction();
           // android.util.Log.d("xujiaStatusBaraction","==="+action);
            //Log.d("kay13", "onReceive: action:" + action);
            /// M: ALPS00349274 to hide navigation bar when ipo shut down to avoid it flash when in boot ipo mode.{
            if ("android.intent.action.ACTION_BOOT_IPO".equals(action)) {
                if (mNavigationBarView != null) {
                    View view = mNavigationBarView.findViewById(R.id.rot0);
                    if (view != null && view.getVisibility() != View.GONE) {
                        Xlog.d(TAG, "receive android.intent.action.ACTION_BOOT_IPO to set mNavigationBarView visible");
                        view.setVisibility(View.VISIBLE);
                    }
                }
            } else if("phone_schedule_change".equals(action)){


                    handler.removeCallbacks(runnable);
                    updateTomorrowSchedule();
                    updateTodaySchedule();

                }else if("close_status_bar_expand".equals(action)){
                    animateCollapsePanels();

                }else if("toggle_status_bar_expand".equals(action)){
                    mHandler.post(new Runnable(){
			   public void run(){
				if(mExpandedVisible) {
					animateCollapsePanels();
				} else {
					animateExpandNotificationsPanel();
				}
			   }
			});

		  }else if("toggle_control_center".equals(action)){
		       if(mKeyguardManager.isKeyguardLocked())
			   	return;
			mHandler.post(new Runnable(){
			   public void run(){
			       if(isControlCenterShow()) {
					mUpdateHandler.obtainMessage(DIRECTION_DOWN).sendToTarget();
				} else {
					mUpdateHandler.obtainMessage(DIRECTION_UP).sendToTarget();
				}
				mDirection = DIRECTION_NONE;
			   }
			});
		  }else if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(action)) {
                if (mNavigationBarView != null) {
                    Xlog.d(TAG, "receive android.intent.action.ACTION_SHUTDOWN_IPO to set mNavigationBarView invisible");
                    mNavigationBarView.hideForIPOShutdown();
                }
            /// M: ALPS00349274 to hide navigation bar when ipo shut down to avoid it flash when in boot ipo mode.}
            } else if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                int flags = CommandQueue.FLAG_EXCLUDE_NONE;
                if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                    String reason = intent.getStringExtra("reason");
                    if (reason != null && reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        flags |= CommandQueue.FLAG_EXCLUDE_RECENTS_PANEL;
                    }
                }
                animateCollapsePanels(flags);
            /// M: [SystemUI] Support "ThemeManager" @{
            } else if (Intent.ACTION_SKIN_CHANGED.equals(action)) {
                refreshApplicationGuide();
                refreshExpandedView(context);
                if (mNavigationBarView != null) {
                    mNavigationBarView.upDateResources();
                }
                repositionNavigationBar();
                updateResources();
            /// M: [SystemUI] Support "Theme management". @}
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                // no waiting!
                /// M: [SystemUI]Show application guide for App.
                if (mAppGuideDialog != null && mAppGuideDialog.isShowing()) {
                    mAppGuideDialog.dismiss();
                    Xlog.d(TAG, "mAppGuideDialog.dismiss()");
                }
                /// M: [SystemUI]Show application guide for App. @}
                makeExpandedInvisible();
                notifyNavigationBarScreenOn(false);
				mHandler.removeCallbacks(mClosePanelRunnable);//added by lzp
				mHandler.postDelayed(mClosePanelRunnable, DISMISS_PANEL_DURATION);//added by wang 20130823
            } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
                if (DEBUG) {
                    Slog.v(TAG, "configuration changed: " + mContext.getResources().getConfiguration());
                }
                /// M: [SystemUI]Show application guide for App.
                refreshApplicationGuide();
                Configuration currentConfig = context.getResources().getConfiguration();
                /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @{
                if (currentConfig.orientation != mPrevioutConfigOrientation) {
                    mNeedRelayout = true;
                    mPrevioutConfigOrientation = currentConfig.orientation;
                }
                /// M: [ALPS00336833] When orientation changed, request layout to avoid status bar layout error. @}
                mDisplay.getSize(mCurrentDisplaySize);

                updateResources();
                repositionNavigationBar();
                updateExpandedViewPos(EXPANDED_LEAVE_ALONE);
                updateShowSearchHoldoff();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                // work around problem where mDisplay.getRotation() is not stable while screen is off (bug 7086018)
                //Log.d("kay6", "onReceive phonestatus: ACTION_SCREEN_ON:");
                repositionNavigationBar();
		        setBottomOrTopPanelBlurbg();//added by xss for blur
                notifyNavigationBarScreenOn(true);
				mHandler.removeCallbacks(mClosePanelRunnable);//added by wang 20130823
				if(mKeyguardManager.isKeyguardLocked()) {
					mHandler.post(new Runnable() {
						public void run() {
							onStatusBarStateChanged(false);
						}
					});
				} else {
					mHandler.post(new Runnable() {
						public void run() {
							onStatusBarStateChanged(true);
						}
					});
				}
		// if(mApplicationPreview!=null) mApplicationPreview.getAppWeather();//added by xss for ios10 weather	//del by gaojunbin	 
            /// M: [SystemUI] Support "Dual SIM PLMN Change". @{
            } else if (Telephony.Intents.SPN_STRINGS_UPDATED_ACTION.equals(action)) {
                if (mShowCarrierInPanel) {
                    if (FeatureOption.MTK_GEMINI_SUPPORT) {
                        final int tempSimId = intent.getIntExtra(PhoneConstants.GEMINI_SIM_ID_KEY, PhoneConstants.GEMINI_SIM_1);
                        /// M: Support GeminiPlus
                        for (int childIdx = 0; childIdx < mCarrierLabelGemini.getChildCount(); childIdx++) {
                            final View mChildView = mCarrierLabelGemini.getChildAt(childIdx);
                            if(mChildView instanceof CarrierLabelGemini) {
                                CarrierLabelGemini mChildCarrier = (CarrierLabelGemini) mChildView;
                                if (tempSimId == mChildCarrier.getSlotId()) {
                                    mChildCarrier.updateNetworkName(intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_SPN, false),
                                    intent.getStringExtra(Telephony.Intents.EXTRA_SPN),
                                    intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_PLMN, false),
                                    intent.getStringExtra(Telephony.Intents.EXTRA_PLMN));
                                }
                            }
                        }
                    }
                    else {
                        ((CarrierLabel)mCarrierLabel).updateNetworkName(
                                intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_SPN, false),
                                intent.getStringExtra(Telephony.Intents.EXTRA_SPN),
                                intent.getBooleanExtra(Telephony.Intents.EXTRA_SHOW_PLMN, false),
                                intent.getStringExtra(Telephony.Intents.EXTRA_PLMN));
                    }
                }
            }
            //added by wang 20130729 start
			else if (Intent.ACTION_UPDATE_STATUSBAR.equals(action)) {
				 int statusBarBgColor = intent.getIntExtra("status_bar_bg_color_index", -1);//status bar's background color: white, black or transparent
				 boolean white = intent.getBooleanExtra("status_bar_font_white", false);//status bar's font and drawable color, white or black

               // System.out.println("receive the broadcast Intent.ACTION_UPDATE_STATUSBAR====================statusBarBgColor="+white);
              Log.d("hjz5","white=="+white +"statusBarBgColor=="+ statusBarBgColor+" mKeyguardManager.isKeyguardLocked()=="+mKeyguardManager.isKeyguardLocked());
               if(!mKeyguardManager.isKeyguardLocked())
                {
                                  updateViews(white, statusBarBgColor);
                }else
                    {
                        updateViews(white, statusBarBgColor);
                    }

            } else if (Intent.ACTION_USER_PRESENT.equals(action)) {//unlock screen
				mScreenLocked = false;
				setBottomOrTopPanelBlurbg();//added by xss for blur
				isLockScreen=false;//added by xss for back to phone
                //updateViews(preWhite,preColor);//add by xujia 20130913
                if(mApplicationPreview!=null)mApplicationPreview.unLockScreen();
				mHandler.post(new Runnable() {
					public void run() {
						onStatusBarStateChanged(true);
					}
				});
            }  else if ("input_method_state".equals(action)) {//unlock screen
				mInputMethodShow = intent.getBooleanExtra("show", false);
				mHandler.post(new Runnable() {
					public void run() {
						//correctWindowPosition(mInputMethodShow ? mControlCenterMinY : mControlCenterNormalY);
							onInputMethodStateChanged();
					}
				});
				//System.out.println("mInputMethodShow="+mInputMethodShow);
            } else if ("iphone_goto_lock_screen".equals(action)) {//lock screen
				mScreenLocked = true;
                  isFirstScreen=false;//add by xujia
                   /*Begin;added by xss for blur*/
		     myHandler.removeCallbacks(mRunnable);
		     myHandler.postDelayed(mRunnable, 2000);	
		     /*End;added by xss for blur*/
		    /*Begin;added by xss for back to last app*/	 
		     notificationPanelViewIsShow=false;
	            notificationPanelViewIsClick=false; 
		     hideBackToLastAppView();
	           /*End;added by xss for back to last app*/
		     isLockScreen=true;	//added by xss for back to phone	  
		    showBackToPhoneBtn(false);//added by xss for back to phone	   
                   updateObserver();
                  if(mApplicationPreview!=null) mApplicationPreview.lockScreen();
		    /*Begin:added by xss for ios10 lunarDate*/	
		     saveLunarDate();
		    /*End:added by xss for ios10 lunarDate*/		  		
            } else if (action.equals(Intent.ACTION_WALLPAPER_CHANGED)) {
                Log.d(TAG, "Intent.ACTION_WALLPAPER_CHANGED");
				mUpdateHandler.postDelayed(mResetNotificationPanelBg, 200);
            } else if (action.equals("control_center_action")) {
                mControlCenterShow = intent.getBooleanExtra("control_center_show", false);
            }else if(action.equals("opencamera")){
                    if(mBottomRootView!=null)
                        {

                              mBottomRootView.closeCamera();
                        }
            }else if(action.equals("closecamera"))
                    {
                            if(mBottomRootView!=null)
                                {
                                    mBottomRootView.openLight();
                                }
          }else if(action.equals("allow_access_in_activity")){
			       updateBottomPanel();
		  }else if(action.equals("is_launcher")){

                 enabledBottomPanel();
		  }else if(action.equals("go_to_activity"))
		    {
		        updateBottomPanel();

		  	}else if(action.equals("reset_statusbar_from_launcher")){/*added by xujia 20141030*/

				boolean isShowLandsView=landsview.getVisibility()==View.VISIBLE;

				boolean isShow=intent.getBooleanExtra("show_status_bar",false);
				//Log.d("xujiastatusbar","isShowLandsView=="+isShow+" isCurrentActivityLauncher()="+isCurrentActivityLauncher());

			//	if(isCurrentActivityLauncher()){

							if(isShow){
					mUpdateHandler.postDelayed(mresetStatusBar, 150);

					}else{

							mUpdateHandler.postDelayed(mshowStatusBar, 150);
						}
				/*}else{
						if(isShow){
							mUpdateHandler.postDelayed(mresetStatusBar, 150);
							}else{



								}
						

						}*/


		  }else if(action.equals("hide_statusbar_panel_from_launcher")){

				boolean isShow=intent.getBooleanExtra("show_status_bar",false);
				if(isShow){

					landsview.setVisibility(View.INVISIBLE);
                                    backToPhoneLandsView.setVisibility(View.INVISIBLE);//added by xss for back to phone
					}else{
                                    backToPhoneLandsView.setVisibility(View.INVISIBLE);//added by xss for back to phone
					landsview.setVisibility(View.VISIBLE);
					}

				}else if(action.equals("set_live_wallpaper_cenon")){//added by xujia for set live wallpaper
				setNotificationBg();
		    	}else if (action.equals("receive_unread_messages")) { //Begin:added by xss for Show the number of unread messages
		        int number=1;
			 String pkg=intent.getStringExtra("package_name");	
			 int id=intent.getIntExtra("messagse_id",-1);
			 Nid nid = new Nid();
			 nid.id = id;
                        nid.pkg = pkg;
			 if((pkg.equals("com.tencent.mobileqq")&& id!=2)||(pkg.equals("com.tencent.mm") && id!=99)){
                              number+=mNidHashMap.getNumber(nid);
			 }		   
			 mNidHashMap.putNumber(nid,number);
			
			 if(!pkg.equals(topAppPackageName) &&!pkg.equals("com.android.mms") && !pkg.equals("com.android.dialer")){
				 Intent showUnreadCountIntent=new Intent("Show_the_number_of_unread_messages");
				 showUnreadCountIntent.putExtra("package_name",pkg);
				 showUnreadCountIntent.putExtra("unread_number",mNidHashMap.getNumber(pkg));
				 mContext.sendBroadcast(showUnreadCountIntent);
				 if(DEBUG)Log.i("showUnread"," receive_unread_messages  ===========unread_number="+number);
				 //mUnreadMessagesNumber.put(pkg,number); 
			 }
		}/*End:added by xss for Show the number of unread messages*/
		else if(action.equals("listen_keyguard_openOrclose")){
                if(topAppPackageName == null && topAppClassName == null){
                    AppTopActivityChange();
                }
            }
		else if (action.equals("top_activity_changes")) { /*Begin:added by xss for back to last app*/
                AppTopActivityChange();
		     // Log.d("backtolastapp", "mBroadcastReceiver() top_activity_changes");
			 /* topAppPackageName=getTopActivityPackageName();
			  topAppClassName=getTopActivityClassName();

                if(topAppClassName.equals("com.android.systemui.recent.RecentsActivity")) {
                    slideMoveCloseBlurScreen();
                }
              //  getAllTasks();
			  //Begin:added by xss for back to phone
			  if(DEBUG)Log.i("back_to_phone","PhoneStatusBar top_activity_changes   -----true");
              showBackToPhoneBtn(phoneIsUsed(mContext) && !topAppClassName.equals("com.android.phone.InCallScreen")&& !topAppClassName.equals("com.android.contacts.Dialtacts"));
			  //End:added by xss for back to phone
                // add by kay for slide
                Log.d("kay6", "onReceive: top_activity_changes:" + topAppPackageName + " Classname:" + topAppClassName + " KeyguardLocked:" + mKeyguardManager.isKeyguardLocked()
                    + "\n inKeyguardRestrictedInputMode:" + mKeyguardManager.inKeyguardRestrictedInputMode());
                if(topAppPackageName!=null &&  topAppClassName!=null){
                    if(!mKeyguardManager.isKeyguardLocked())
                    {
                        addOrRemoveSlideView(true);
                    }
                    if((topAppPackageName.equals("com.mediatek.oobe")&&!topAppClassName.equals("com.mediatek.oobe.basic.OobeLockScreenActivity"))
                            ||topAppClassName.equals("com.mediatek.settings.SetupWizardForOobe")||topAppClassName.equals("com.android.settings.OobeDisplayModeSettings")
                            ||(topAppClassName.equals("com.android.incallui.InCallActivity") && callStateIsRinging(mContext))){
                        addOrRemoveSlideView(false);
                    }

                 //   if(!topAppClassName.equals("com.android.systemui.recents.RecentsActivity"))
                 //      isOpenRecents = false;
                    //if(topAppPackageName.equals("com.mediatek.camera"))mKeyguardBottomArea.updateFlashBtn();//added by xss for ios11
                    //if(!topAppPackageName.equals("com.hskj.hometest") && !topAppPackageName.equals("com.android.packageinstaller"))updateStatusBarContentsViewAndEditFinishParent(false);//added by xss for ios11
                }
			  Log.d("backtolastapp", "mBroadcastReceiver() lastAppPackageName="+lastAppPackageName+"     topAppPackageName="+topAppPackageName+"   notificationPanelViewIsShow="+notificationPanelViewIsShow);
              myHandler.removeCallbacks(mRunnable);
              myHandler.postDelayed(mRunnable, 1500);
                Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::topAppPackageName:"+topAppPackageName+"  topAppPackageName::"+topAppPackageName);
               if(topAppPackageName.equals("com.hskj.hometest")){
                    Settings.System.putInt(mContext.getContentResolver(),"IS_TOP_APP_LAUNCHER",1);
               }else{
                    Settings.System.putInt(mContext.getContentResolver(),"IS_TOP_APP_LAUNCHER",0);
               }*/
		}else if (action.equals("notificationContentView_is_show")) { 
			  //Log.d("backtolastapp", "mBroadcastReceiver()  notificationPanelView_is_show");
			  notificationPanelViewIsShow=true;
			  lastAppPackageName=getTopActivityPackageName();
			  lastAppClassName=getTopActivityClassName();
			//Log.d("backtolastapp", "mBroadcastReceiver()    topActivityPackageName="+topActivityPackageName);
			/*Begin:added by xss for blur*/
				if(DEBUG)Log.i(TAG, "added by xss for blur   notificationPanelView_is_show");
				Intent it=new Intent();
				it.setAction("workspace_scroll_to_right");
				mContext.sendBroadcast(it);
			/*End:added by xss for blur*/
			/**begin:added by gaojunbin**/
		/**	Vibrator vibrator = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
		    long [] pattern = {100,45}; 
		    vibrator.vibrate(pattern,-1); **/
            deleteAllBtn.setVisibility(View.VISIBLE);// add by csc on 20161008
            deleteAllTv.setVisibility(View.GONE);// add by csc on 20161008
			//vibrator.cancel();
		    /**end:added by gaojunbin**/
		}else if (action.equals("notificationContentView_is_click")) { 
			  notificationPanelViewIsClick=true;
		}else if (action.equals("back_button_text_color")) { 
			  boolean isWhite=intent.getBooleanExtra("isWhite",false);
			  if(mBackToLastAppText!=null)mBackToLastAppText.setTextColor(isWhite ? 0xFFFFFFFF : 0xFF000000);
			  if(mBackToLastAppIcon!=null)mBackToLastAppIcon.setBackgroundResource(isWhite ?R.drawable.back_to_last_app_white:R.drawable.back_to_last_app_black);
		}else if (action.equals("click_back_button")) { 
			  boolean click=intent.getBooleanExtra("click",false);
			  if(DEBUG)Log.i("backtolastapp","receive   click_back_button  =====click="+click+"   lastAppPackageName="+lastAppPackageName+"  notificationPanelViewIsShow="+notificationPanelViewIsShow+"  notificationPanelViewIsClick="+notificationPanelViewIsClick);	
			  if(click && !"com.android.packageinstaller".equals(lastAppPackageName) && notificationPanelViewIsShow && notificationPanelViewIsClick)hideBackToLastAppView();
			  /*End:added by xss for back to last app*/
		}else if((intent.getAction()).equals("IS_DELETE_ALL_NOTIFICATION")){
                Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::getActionIS_DELETE_ALL_NOTIFICATION");
                int count=Integer.parseInt(intent.getStringExtra("COUNT"));
                if(count>0){// mod by csc from 1 to 0
                    isShowToday(true);
                }else{
                    isShowToday(false);
                    deleteAllBtn.setVisibility(View.VISIBLE);// add by csc on 20161008
                    deleteAllTv.setVisibility(View.GONE);// add by csc on 20161008
                }
                // end add by csc
            }else if((intent.getAction()).equals("STATUS_BAR_RESTORE")){ // add by csc for ios 10
                animateCollapsePanels();
            }else if("status_bar_bg_change".equals(action)){//added by xss for ios10
		       boolean scrollup=intent.getBooleanExtra("is_scroll_up",false);
			boolean isInLockScreen = intent.getBooleanExtra("is_in_lock_screen",false);
			boolean showStatusBarBlurBg = intent.getBooleanExtra("show_status_bar_blur_bg",false);
			Log.i("show_status_bar_blur_bg","PhoneStatusBar status_bar_bg_change  ===========scrollup="+scrollup+"   isInLockScreen="+isInLockScreen+"   showStatusBarBlurBg="+showStatusBarBlurBg);
			if(scrollup){
                             if(mStatusBarView!=null)mStatusBarView.setBackgroundColor(mContext.getResources().getColor(R.color.search_plate_color));
			}else if(showStatusBarBlurBg){
                             if(mStatusBarView!=null && mBottomRootView!=null)mStatusBarView.setBackground(mBottomRootView.getStatusBarBlurBg());
			}else{
                             if(mStatusBarView!=null)mStatusBarView.setBackgroundColor(0x00000000);
			}
		       
			if(isInLockScreen)showClock(scrollup);
	     }else if("show_status_bar_lock_iocn".equals(action)){//added by xss for ios10
	              if(DEBUG)Log.i(TAG," show_status_bar_lock_iocn  ===========");
		       boolean show=intent.getBooleanExtra("lock_iocn_show",false);
			showLockIocn(show);
		}else if ("TO_SCREEN_SHOTS".equals(action)) {// add by csc
                Log.d("chenshichun", "" + this.getClass().getCanonicalName() + ":::::::::::::::::::::TO_SCREEN_SHOTS:::::::::::::;;;");
                setBottomOrTopPanelBlurbg();
            } else if ("CURRENT_SCREEN_SLIDE".equals(action)) {// add by csc
                int mCurrentScreen = intent.getIntExtra("CURRENT_SCREEN", 0);
                if (mCurrentScreen == 0) {
                    pageOneIv.setVisibility(View.GONE);
                    pageTwoIv.setVisibility(View.GONE);
                    viewPager.setCurrentItem(1);
                } else {
                    pageOneIv.setVisibility(View.GONE); //View.VISIBLE  -> View.GONE
                    pageTwoIv.setVisibility(View.GONE); //View.VISIBLE  -> View.GONE
                }
            }else if(action.equals(Intent.ACTION_LOCALE_CHANGED)){//added by xss for ios10
                    if(mApplicationPreview!=null) mApplicationPreview.setContext(mContext,false);
            }else if("iphone_weather_the_first_city_name_changed".equals(action) ){//added by xss for ios Bug[4414]
		      if(true)Log.i("ApplicationPreview", "  iphone_weather_the_first_city_name_changed()  ========");
                    if(mApplicationPreview!=null) mApplicationPreview.getAppWeather();
              	}
        }
    };

/*added by xujia*/
   boolean isCurrentActivityLauncher(){
    	final ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
    	final String currentActivityPackageName = ((ActivityManager.RunningTaskInfo)am.getRunningTasks(1).get(0)).topActivity.getPackageName();
			final String currentActivityClassName = ((ActivityManager.RunningTaskInfo)am.getRunningTasks(1).get(0)).topActivity.getClassName();
		System.out.println("currentActivityPackageName=="+currentActivityPackageName+" currentActivityClassName=="+currentActivityClassName);
    	return "com.android.launcher2".equals(currentActivityPackageName)
    		|| "com.hskj.hometest".equals(currentActivityPackageName)
			|| "com.android.stk".equals(currentActivityPackageName);
    }

private Runnable mresetStatusBar = new Runnable() {
		public void run() {
                      backToPhoneLandsView.setVisibility(View.GONE);//added by xss for back to phone
			resetStatusBar();
			landsview.setVisibility(View.GONE);
			clockpaddingView.setVisibility(View.GONE);
		}
	};

private Runnable mshowStatusBar = new Runnable() {
		public void run() {
			 moveFromUnderRight(1);
			landsview.setVisibility(View.VISIBLE);
			clockpaddingView.setVisibility(View.VISIBLE);
			backToPhoneLandsView.setVisibility(View.INVISIBLE);//added by xss for back to phone
		}
	};

private void resetStatusBar(){

final ValueAnimator vAnimator = ValueAnimator.ofFloat(1f,0f);
        		vAnimator.setDuration(250/2);
        		vAnimator.addUpdateListener(new AnimatorUpdateListener() {
					public void onAnimationUpdate(ValueAnimator arg0) {
						moveFromUnderRight(((Float)arg0.getAnimatedValue()).floatValue());
					}
				});

        		vAnimator.start();
	//mUpdateHandler.postDelayed(mresetStatusBar, 50);
}


public void moveFromUnderRight(float moveDistancePercentage){

	LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)landsview.getLayoutParams();
    	layoutParams.gravity = Gravity.RIGHT;
    	layoutParams.rightMargin = -landsview.getMeasuredWidth();
    	layoutParams.rightMargin += moveDistancePercentage*landsview.getMeasuredWidth();

    	if(layoutParams.rightMargin > 0)
    		layoutParams.rightMargin = 0;
		landsview.setLayoutParams(layoutParams);

    }

    private void enabledBottomPanel()
    {
        android.util.Log.d("xujiaStatusBar","in_launcher");
         mBottomRootView.setVisibility(View.VISIBLE);

    }
    private void updateBottomPanel()
    {
         isAllowInActivity=Settings.System.getInt(mContext.getContentResolver(),
								Settings.System.ALLOW_IN_ACTIVITY, -1)!= 0;
         android.util.Log.d("xujiaStatusBar","updateBottomPanel=="+isAllowInActivity+"   isLauncherForeground"+!isLauncherForeground(mContext));
        if(!isAllowInActivity&&!isLauncherForeground(mContext)){
			 mBottomRootView.setVisibility(View.GONE);
	    }else{
		    mBottomRootView.setVisibility(View.VISIBLE);
	    }

    }

    public static ActivityManager getActivityManager(Context context){
                return (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

     }

    public static List<String> getLaunchers(Context context){
            List<String> packageNames = new ArrayList<String>();
            PackageManager packageManager = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);

            List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

            for(ResolveInfo resolveInfo:resolveInfos){
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                if(activityInfo != null) {
                    packageNames.add(resolveInfo.activityInfo.processName);
                    packageNames.add(resolveInfo.activityInfo.packageName);
                }
            }
            return packageNames;
    }

    public static boolean isLauncherForeground(Context context){
        boolean isLauncherForeground = false;
        ActivityManager activityManager = getActivityManager(context);
        List<String> lanuchers = getLaunchers(context);
        List<RunningTaskInfo> runningTaskInfos =  activityManager.getRunningTasks(1);
     	//mod by lzp
        if(runningTaskInfos != null
			&& runningTaskInfos.get(0) != null
			&& runningTaskInfos.get(0).baseActivity != null
			&& lanuchers.contains(runningTaskInfos.get(0).baseActivity.getPackageName())) {
            isLauncherForeground = true;
        }

        return isLauncherForeground;
}
    
/*end by xujia*/
//added by wang 20130729 start
	/*
	private Runnable mResetNotificationPanelBg = new Runnable() {
		public void run() {
			if(mNotificationPanel != null) {
               	mNotificationPanel.setBackgroundDrawable(BitmapUtils.getBlurWallpaper(mContext));//modify by zqs 20130830
			}
		}
	};*/


	private WallpaperManager mWallpaperManager;
	private BitmapDrawable mBgDrawable;
    private BitmapDrawable mBgBlurDrawable;
    private BitmapDrawable mBgBlurDrawable1;
	private Runnable mResetNotificationPanelBg = new Runnable() {
		public void run() {
			mWallpaperManager = (WallpaperManager) mContext.getSystemService(mContext.WALLPAPER_SERVICE);
		    final int wallPaperWidth = mWallpaperManager.getDesiredMinimumWidth();
			final int wallPaperHeight =	mWallpaperManager.getDesiredMinimumHeight();
			/* Begin: changed by yuanhuawei 20130826 */
			WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
       			DisplayMetrics metrics = new DisplayMetrics();
      			wm.getDefaultDisplay().getMetrics(metrics);
			final int screenWidth = metrics.widthPixels;
			final int screenHeight = metrics.heightPixels;
			final int clipLeft = (wallPaperWidth - screenWidth)/2, clipTop = (wallPaperHeight - screenHeight)/2;
            BitmapDrawable bitmapDrawable = new BitmapDrawable(mContext.getResources(),
                    "/data/data/com.android.providers.settings/blur_wallpaper.png");
			Bitmap wallpaperBitMap = Bitmap.createBitmap(mWallpaperManager.getBitmap(),
								clipLeft, clipTop, screenWidth, screenHeight, null,false);

			/* End: changed by yuanhuawei 20130826 */
			//by kay
            Log.d("hjz3", "run: wallpaperBitMap");
            mBgDrawable = new BitmapDrawable(mContext.getResources(), wallpaperBitMap/*BitmapUtils.BoxBlurFilter(wallpaperBitMap)*/); //by kay
            mBgBlurDrawable  = new BitmapDrawable(mContext.getResources(), BitmapUtils.BoxBlurFilter(wallpaperBitMap)); //by kay
            mBgDrawable = bitmapDrawable;
            mBgBlurDrawable = bitmapDrawable;
            mNotificationPanel.setAlpha(0);   //by kay
            mBlurWallpaperBgView.setBackground(mBgDrawable); //by hjz

            //mBlurWallpaperBgView.setAlpha(0);
            //end
			//if(screenWidth>screenHeight)
			statusBarColor=BitmapUtils.getPanpelWallpaperColor(mContext);//added by xujia
			final Intent colorIntent=new Intent("launcher_panel_color");
			colorIntent.putExtra("color_for_launcher",statusBarColor);
			  mContext.sendBroadcast(colorIntent);
		}
	};

	/*Begin added by xujia for set Live Wallpaper*/
	private void setNotificationBg(){
        try{
            if(mWallpaperManager==null)
                mWallpaperManager = (WallpaperManager) mContext.getSystemService(mContext.WALLPAPER_SERVICE);
            PackageManager packageManager = mContext.getPackageManager();
            WallpaperInfo wallInfo = mWallpaperManager.getWallpaperInfo();
            if(wallInfo!=null){
                Drawable d=wallInfo.loadThumbnail(packageManager);
                BitmapDrawable bd = (BitmapDrawable) d;
                Log.d("hjz3", "setNotificationBg: ");
                Bitmap bm = bd.getBitmap();
                mBgDrawable = new BitmapDrawable(mContext.getResources(), BitmapUtils.BoxBlurFilter(bm));
                //	    mNotificationPanel.setBackground(mBgDrawable);  //by kay
                mBlurWallpaperBgView.setBackground(mBgDrawable); //by hjz

            }

        }catch (Exception e){

        }

    }
	private boolean isLiveWallpaperNow(){
			if(mWallpaperManager==null)
			    mWallpaperManager = (WallpaperManager) mContext.getSystemService(mContext.WALLPAPER_SERVICE);

			WallpaperInfo wallInfo = mWallpaperManager.getWallpaperInfo();

			return (wallInfo!=null);
		}
	/*End added by xujia for set Live Wallpaper*/
	private void updateViews(boolean white, int colorIndex) {
		if(colorIndex < 0)
			return;
		Log.i("hjz5","colorIndex = "+colorIndex+"/white = "+white +"/preWhite = "+preWhite+"/preColor="+preColor);
		if(colorIndex == 5 ){
			if(preColor == 4 && !preWhite) {
				colorIndex = preColor;
				white = !preWhite;
			} else {
					colorIndex = preColor;
					white=preWhite;
			}
		}
		/**Begin: added by lzp  for bug [2601]**/
		//change status bar's background
		if(!mScreenLocked&&!isLauncherForeground(mContext))
		    {
		        preColor=colorIndex;
                           preWhite=white;
		    }
        //System.out.println("preColor+preWhite="+preColor+"   "+preWhite);
        	//mStatusBarWindow.setBackground(null);

		mStatusBarView.setBackgroundColor(getColor(colorIndex));

		//changed clock's font color
		final TextView clockView = (TextView) mStatusBarView.findViewById(R.id.clock);
		clockView.setTextColor(white ? 0xFFFFFFFF : 0xFF000000);
	//change phone signal's drawable color
        Log.d("hjz", "updateViews: " + mNetworkController + "  mNetworkController1:" + mNetworkController1);
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mNetworkControllerGemini.setWhiteSignalStrength(white);
            Log.d("hjz", "receive mwhite"+white);
        }
        else{
            mNetworkController.setWhiteSignalStrength(white);
        }
		mNetworkController1.setWhiteSignalStrength(white);    //added by lzp
                //change phone dataTYpe drawable color
                   // mNetworkController.updateDataNetType( white);

		//change battery's drawable color
        Log.d("hjz", "mwhite="+white);
		mBatteryController.setBatteryColor(white);
		//change bluetooth's drawable color
		mBluetoothController.setBluetoothColor(white);
		//change system icon(ie. alarm icon, disturbance icon)'s drawable color
		mIconPolicy.updateSystemIconDrawables(white);
              if(keyguardLockIv!=null)keyguardLockIv.setImageResource(white? R.drawable.keyguard_lock_icon : R.drawable.keyguard_lock_icon_black);//added by xss for ios10
	}

	private int getColor(int colorIndex) {
		switch(colorIndex) {
			case COLOR_INDEX_WHITE: {
				return 0xFFF8F8F8;
			}
			case COLOR_INDEX_WHITE_CONTACTS: {
				return 0xFFFFFFFF;
			}
			case COLOR_INDEX_BLACK: {
				return 0xFF000000;
			}
			case COLOR_INDEX_GRAY: {
				return 0x01060000;
			}
			case COLOR_INDEX_TRANSPARENT: {
				return 0x00000000;
			}
			case COLOR_INDEX_TRANSBLACK:{  //add by joyisn for screen lock background View alpha
				return 0x87000000;
			}
            case 9:{  //add by joyisn for screen lock background View alpha
                return 0xff5a5a5a;
            }

			default: {
				return 0x00000000;//default, transparent
			}
		}
	}
	private static final int COLOR_INDEX_WHITE = 1;
	private static final int COLOR_INDEX_BLACK = 2;
	private static final int COLOR_INDEX_GRAY = 3;
	private static final int COLOR_INDEX_TRANSPARENT = 4;
   	private static final int COLOR_INDEX_WHITE_CONTACTS = 6;
	private static final int COLOR_INDEX_TRANSBLACK = 7;//add by joyisn:a new color

	public void updateRotationStatus(boolean locked) {
		mIconPolicy.updateRotation(locked);
	}


    private ContentObserver mAutoRotationChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            //to process rotation
            boolean locked = Settings.System.getInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, -1) == 0;
			updateRotationStatus(locked);
        }
    };

//added by wang 20130729 end	

    @Override
    public void userSwitched(int newUserId) {
        if (MULTIUSER_DEBUG) mNotificationPanelDebugText.setText("USER " + newUserId);
        animateCollapsePanels();
        updateNotificationIcons();
        resetUserSetupObserver();
    }

    private void resetUserSetupObserver() {
        mContext.getContentResolver().unregisterContentObserver(mUserSetupObserver);
        mUserSetupObserver.onChange(false);
        mContext.getContentResolver().registerContentObserver(
                Settings.Secure.getUriFor(Settings.Secure.USER_SETUP_COMPLETE), true,
                mUserSetupObserver,
                mCurrentUserId);
		//added by wang 20130924 start
		mContext.getContentResolver().unregisterContentObserver(mAutoRotationChangeObserver);
		mUserSetupObserver.onChange(false);
		//for rotation
		mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
			true, mAutoRotationChangeObserver);
		//added by wang 20130924 end


        //begin added by xujia
       mContext.getContentResolver().unregisterContentObserver(mObserver);
         mObserver.onChange(false);

         mContext.getContentResolver().registerContentObserver(CalendarContract.Events.CONTENT_URI,
                    false, mObserver);





      //del by xujia
     /*   mContext.getContentResolver().unregisterContentObserver(obCall);
      obCall.onChange(false);
        
      mContext.getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI, false, obCall);
      obCall.refreshUnReadMessage();
         obCall.updateQueryBaseTime(System.currentTimeMillis());


        
       mContext.getContentResolver().unregisterContentObserver(obMms);
      obMms.onChange(false);
        
      mContext.getContentResolver().registerContentObserver(MmsUnReadObserver.MMS_URI, false, obMms);
      obMms.refreshUnReadMessage();
      obMms.updateQueryBaseTime(System.currentTimeMillis());*/
       //end added by xujia
    }

    private void setIntruderAlertVisibility(boolean vis) {
        if (!ENABLE_INTRUDERS) return;
        if (DEBUG) {
            Slog.v(TAG, (vis ? "showing" : "hiding") + " intruder alert window");
        }
        mIntruderAlertView.setVisibility(vis ? View.VISIBLE : View.GONE);
    }

    public void dismissIntruder() {
        if (mCurrentlyIntrudingNotification == null) return;

        try {
            mBarService.onNotificationClear(
                    mCurrentlyIntrudingNotification.pkg,
                    mCurrentlyIntrudingNotification.tag,
                    mCurrentlyIntrudingNotification.id);
        } catch (android.os.RemoteException ex) {
            // oh well
        }
    }

    /**
     * Reload some of our resources when the configuration changes.
     *
     * We don't reload everything when the configuration changes -- we probably
     * should, but getting that smooth is tough.  Someday we'll fix that.  In the
     * meantime, just update the things that we know change.
     */
    void updateResources() {
        Xlog.d(TAG, "updateResources");

        final Context context = mContext;
        final Resources res = context.getResources();

        if (mClearButton instanceof TextView) {
            ((TextView)mClearButton).setText(context.getText(R.string.status_bar_clear_all_button));
        }
        /// M: [SystemUI] Support "Notification toolbar". {
        mToolBarView.updateResources();
        /// M: [SystemUI] Support "Notification toolbar". }

        // Update the QuickSettings container
        if (mQS != null) mQS.updateResources();

        loadDimens();
    }

    protected void loadDimens() {
        final Resources res = mContext.getResources();

        mNaturalBarHeight = res.getDimensionPixelSize(
                com.android.internal.R.dimen.status_bar_height);
        //Log.d("kay18", "loadDimens: mNaturalBarHeight:" + mNaturalBarHeight);
        int newIconSize = res.getDimensionPixelSize(
            com.android.internal.R.dimen.status_bar_icon_size);
        int newIconHPadding = res.getDimensionPixelSize(
            R.dimen.status_bar_icon_padding);

        if (newIconHPadding != mIconHPadding || newIconSize != mIconSize) {
//            Slog.d(TAG, "size=" + newIconSize + " padding=" + newIconHPadding);
            mIconHPadding = newIconHPadding;
            mIconSize = newIconSize;
            //reloadAllNotificationIcons(); // reload the tray
        }

        mEdgeBorder = res.getDimensionPixelSize(R.dimen.status_bar_edge_ignore);

        mSelfExpandVelocityPx = res.getDimension(R.dimen.self_expand_velocity);
        mSelfCollapseVelocityPx = res.getDimension(R.dimen.self_collapse_velocity);
        mFlingExpandMinVelocityPx = res.getDimension(R.dimen.fling_expand_min_velocity);
        mFlingCollapseMinVelocityPx = res.getDimension(R.dimen.fling_collapse_min_velocity);

        mCollapseMinDisplayFraction = res.getFraction(R.dimen.collapse_min_display_fraction, 1, 1);
        mExpandMinDisplayFraction = res.getFraction(R.dimen.expand_min_display_fraction, 1, 1);

        mExpandAccelPx = res.getDimension(R.dimen.expand_accel);
        mCollapseAccelPx = res.getDimension(R.dimen.collapse_accel);

        mFlingGestureMaxXVelocityPx = res.getDimension(R.dimen.fling_gesture_max_x_velocity);

        mFlingGestureMaxOutputVelocityPx = res.getDimension(R.dimen.fling_gesture_max_output_velocity);

        mNotificationPanelMarginBottomPx
            = (int) res.getDimension(R.dimen.notification_panel_margin_bottom);
        mNotificationPanelMarginPx
            = (int) res.getDimension(R.dimen.notification_panel_margin_left);
        mNotificationPanelGravity = res.getInteger(R.integer.notification_panel_layout_gravity);
        if (mNotificationPanelGravity <= 0) {
            mNotificationPanelGravity = Gravity.LEFT | Gravity.TOP;
        }
        mSettingsPanelGravity = res.getInteger(R.integer.settings_panel_layout_gravity);
        if (mSettingsPanelGravity <= 0) {
            mSettingsPanelGravity = Gravity.RIGHT | Gravity.TOP;
        }

        mCarrierLabelHeight = res.getDimensionPixelSize(R.dimen.carrier_label_height);
        mNotificationHeaderHeight = res.getDimensionPixelSize(R.dimen.notification_panel_header_height);
        /// M: Calculate ToolBar height when sim indicator is showing.
        mToolBarViewHeight = res.getDimensionPixelSize(R.dimen.toolbar_height);

        mNotificationPanelMinHeightFrac = res.getFraction(R.dimen.notification_panel_min_height_frac, 1, 1);
        if (mNotificationPanelMinHeightFrac < 0f || mNotificationPanelMinHeightFrac > 1f) {
            mNotificationPanelMinHeightFrac = 0f;
        }

        if (false) Slog.v(TAG, "updateResources");
    }

    //
    // tracing
    //

    void postStartTracing() {
        mHandler.postDelayed(mStartTracing, 3000);
    }

    void vibrate() {
        android.os.Vibrator vib = (android.os.Vibrator)mContext.getSystemService(
                Context.VIBRATOR_SERVICE);
        vib.vibrate(250);
    }

    Runnable mStartTracing = new Runnable() {
        public void run() {
            vibrate();
            SystemClock.sleep(250);
            Slog.d(TAG, "startTracing");
            android.os.Debug.startMethodTracing("/data/statusbar-traces/trace");
            mHandler.postDelayed(mStopTracing, 10000);
        }
    };

    Runnable mStopTracing = new Runnable() {
        public void run() {
            android.os.Debug.stopMethodTracing();
            Slog.d(TAG, "stopTracing");
            vibrate();
        }
    };

    @Override
    protected void haltTicker() {
        //mTicker.halt();//removed by wang 20130729
    }

    @Override
    protected boolean shouldDisableNavbarGestures() {
        return !isDeviceProvisioned()
                || mExpandedVisible
                || (mDisabled & StatusBarManager.DISABLE_SEARCH) != 0;
    }

    private static class FastColorDrawable extends Drawable {
        private final int mColor;

        public FastColorDrawable(int color) {
            mColor = 0xff000000 | color;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawColor(mColor, PorterDuff.Mode.SRC);
        }

        @Override
        public void setAlpha(int alpha) {
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }

        @Override
        public void setBounds(int left, int top, int right, int bottom) {
        }

        @Override
        public void setBounds(Rect bounds) {
        }
    }
    /// M: [SystemUI] Support "Dual SIM". @{

    private NetworkController mNetworkControllerGemini;

    /// M: Support GeminiPlus
    private CarrierLabelGemini mCarrier1 = null;
    private CarrierLabelGemini mCarrier2 = null;
    private CarrierLabelGemini mCarrier3 = null;
    private CarrierLabelGemini mCarrier4 = null;
    private View mCarrierDivider = null;
    private View mCarrierDivider2 = null;
    private View mCarrierDivider3 = null;

    private LinearLayout mCarrierLabelGemini = null;

    private BroadcastReceiver mSIMInfoReceiver = new BroadcastReceiver() {
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            Xlog.d(TAG, "onReceive, intent action is " + action + ".");
            if (action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        SIMHelper.updateSIMInfos(context);
                        int type = intent.getIntExtra("type", -1);
                        long simId = intent.getLongExtra("simid", -1);
                        if (type == 0 || type == 1) {
                            // name and color changed
                            updateNotificationsSimInfo(simId);
                        }
                        // update ToolBarView's panel views
                        mToolBarView.updateSimInfos(intent);
                        if (mQS != null) {
                            mQS.updateSimInfo(intent);
                        }
                    }
                });
            } else if (action.equals(TelephonyIntents.ACTION_SIM_INSERTED_STATUS)
                    || action.equals(TelephonyIntents.ACTION_SIM_INFO_UPDATE)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        SIMHelper.updateSIMInfos(context);
                    }
                });
                updateSimIndicator();
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
                updateAirplaneMode();
            } else if (action.equals(ACTION_BOOT_IPO)) {
                if (mSimIndicatorIcon != null) {
                    mSimIndicatorIcon.setVisibility(View.GONE);
                }
            } else if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) { ///for AT&T
                int simStatus = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_STATE, -1);
                if (simStatus == PhoneConstants.SIM_INDICATOR_SEARCHING) {
                    Xlog.d(TAG, "updateSIMState. simStatus is " + simStatus);
                 //   updatePLMNSearchingStateView(true);//delete by xujia
                } else {
                    //updatePLMNSearchingStateView(false);////delete by xujia
                }
            }
        }
    };

    private void updateNotificationsSimInfo(long simId) {
        Xlog.d(TAG, "updateNotificationsSimInfo, the simId is " + simId + ".");
        if (simId == -1) {
            return;
        }
        SimInfoManager.SimInfoRecord simInfo = SIMHelper.getSIMInfo(mContext, simId);
        if (simInfo == null) {
            Xlog.d(TAG, "updateNotificationsSimInfo, the simInfo is null.");
            return;
        }
        for (int i = 0, n = this.mNotificationData.size(); i < n; i++) {
            Entry entry = this.mNotificationData.get(i);
            updateNotificationSimInfo(simInfo, entry.notification.notification, entry.icon, entry.expanded);
        }
    }

    private void updateNotificationSimInfo(SimInfoManager.SimInfoRecord simInfo, Notification n, StatusBarIconView iconView, View itemView) {
        if (n.simId != simInfo.mSimInfoId) {
            return;
        }
        int simInfoType = n.simInfoType;
        if (iconView == null) { //for update SimIndicatorView
            for (int i=0; i<mNotificationIcons.getChildCount(); i++) {
                View child = mNotificationIcons.getChildAt(i);
                if (child instanceof StatusBarIconView) {
                    StatusBarIconView iconViewtemp = (StatusBarIconView) child;
                    if(iconViewtemp.getNotificationSimId() == n.simId){
                        iconView = iconViewtemp;
                        break;
                    }
                }
            }
        }
        // icon part.
//        if ((simInfoType == 2 || simInfoType == 3) && simInfo != null && iconView != null) {
//            Xlog.d(TAG, "updateNotificationSimInfo, add sim info to status bar.");
//            Drawable drawable = iconView.getResources().getDrawable(simInfo.mSimBackgroundRes);
//           if (drawable != null) {
//                iconView.setSimInfoBackground(drawable);
//                iconView.invalidate();
//            }
//        }
        // item part.
        if ((simInfoType == 1 || simInfoType == 3) && simInfo != null && (simInfo.mColor >= 0 && simInfo.mColor < Telephony.SIMBackgroundRes.length)) {
            Xlog.d(TAG, "updateNotificationSimInfo, add sim info to notification item. simInfo.mColor = " + simInfo.mColor);
            View simIndicatorLayout = itemView.findViewById(com.android.internal.R.id.notification_sim_indicator);
            simIndicatorLayout.setVisibility(View.VISIBLE);
            ImageView bgView = (ImageView) itemView.findViewById(com.android.internal.R.id.notification_sim_indicator_bg);
            bgView.setBackground(mContext.getResources().getDrawable(TelephonyIcons.SIM_INDICATOR_BACKGROUND_NOTIFICATION[simInfo.mColor]));
            bgView.setVisibility(View.VISIBLE);
        } else {
            View simIndicatorLayout = itemView.findViewById(com.android.internal.R.id.notification_sim_indicator);
            simIndicatorLayout.setVisibility(View.VISIBLE);
            View bgView = itemView.findViewById(com.android.internal.R.id.notification_sim_indicator_bg);
            bgView.setVisibility(View.GONE);
        }
    }

    /// M: [SystemUI] Support "Dual SIM". @}

    /// M: [SystemUI] Support "Notification toolbar". @{
    private ToolBarView mToolBarView;
    private View mToolBarSwitchPanel;
    public boolean isExpanded() {
        return mExpandedVisible;
    }
    /// M: [SystemUI] Support "Notification toolbar". @}

    /// M: [SystemUI] Support "SIM indicator". @{

    private boolean mIsSimIndicatorShowing = false;
    private String mBusinessType = null;
    public void showSimIndicator(String businessType) {
        if (mIsSimIndicatorShowing) {
            hideSimIndicator();
        }
        mBusinessType = businessType;
        long simId = SIMHelper.getDefaultSIM(mContext, businessType);
        Xlog.d(TAG, "showSimIndicator, show SIM indicator which business is " + businessType + "  simId = "+simId+".");
        if (simId == android.provider.Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK) {
            List<SimInfoManager.SimInfoRecord> simInfos = SIMHelper.getSIMInfoList(mContext);
            if (simInfos != null && simInfos.size() > 0) {
                showAlwaysAskOrInternetCall(simId);
                mToolBarView.showSimSwithPanel(businessType);
            }
        } else if (businessType.equals(android.provider.Settings.System.VOICE_CALL_SIM_SETTING)
                && simId == android.provider.Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
            showAlwaysAskOrInternetCall(simId);
            mToolBarView.showSimSwithPanel(businessType);
        } else if (simId == android.provider.Settings.System.SMS_SIM_SETTING_AUTO) {
            List<SimInfoManager.SimInfoRecord> simInfos = SIMHelper.getSIMInfoList(mContext);
            if (simInfos != null && simInfos.size() > 0) {
                showAlwaysAskOrInternetCall(simId);
                mToolBarView.showSimSwithPanel(businessType);
            }
        } else {
            mSimIndicatorIconShow = false;
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                List<SimInfoManager.SimInfoRecord> simInfos = SIMHelper.getSIMInfoList(mContext);
                if (simInfos == null) {
                    return;
                }
                int slot = 0;
                for (int i = 0; i < simInfos.size(); i++) {
                    if (simInfos.get(i).mSimInfoId == simId) {
                        slot = simInfos.get(i).mSimSlotId;
                        break;
                    }
                }
                if (simInfos.size() == 1) {
                    if (businessType.equals(android.provider.Settings.System.VOICE_CALL_SIM_SETTING)
                            && isInternetCallEnabled(mContext)) {
                        mNetworkControllerGemini.showSimIndicator();
                        mToolBarView.showSimSwithPanel(businessType);
                    }
                } else if (simInfos.size() > 1) {
                    mNetworkControllerGemini.showSimIndicator();
                    mToolBarView.showSimSwithPanel(businessType);
                }
            } else {
                List<SimInfoManager.SimInfoRecord> simInfos = SIMHelper.getSIMInfoList(mContext);
                if (simInfos == null) {
                    return;
                }
                if (businessType.equals(android.provider.Settings.System.VOICE_CALL_SIM_SETTING)
                        && isInternetCallEnabled(mContext) && simInfos.size() == 1) {
                    mNetworkController.showSimIndicator();
                    mToolBarView.showSimSwithPanel(businessType);
                }
            }
        }
        mIsSimIndicatorShowing = true;
    }

    public void hideSimIndicator() {
        Xlog.d(TAG, "hideSimIndicator SIM indicator.mBusinessType = " + mBusinessType);
        if (mBusinessType == null) return;
        long simId = SIMHelper.getDefaultSIM(mContext, mBusinessType);
        Xlog.d(TAG, "hideSimIndicator, hide SIM indicator simId = "+simId+".");
        mSimIndicatorIcon.setVisibility(View.GONE);
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            mNetworkControllerGemini.hideSimIndicator();
            mNetworkControllerGemini.hideSimIndicator();
            if(PhoneConstants.GEMINI_SIM_NUM == 3) {
                mNetworkControllerGemini.hideSimIndicator();
            }
            if(PhoneConstants.GEMINI_SIM_NUM == 4) {
                mNetworkControllerGemini.hideSimIndicator();
            }
        } else {
            mNetworkController.hideSimIndicator();
        }
        mToolBarView.hideSimSwithPanel();
        mIsSimIndicatorShowing = false;
        mSimIndicatorIconShow = false;
    }

    private boolean mAirplaneMode = false;
    private boolean mSimIndicatorIconShow = false;

    private void updateAirplaneMode() {
        mAirplaneMode = (Settings.System.getInt(mContext.getContentResolver(),
            Settings.Global.AIRPLANE_MODE_ON, 0) == 1);
        if (mSimIndicatorIcon != null) {
            //mSimIndicatorIcon.setVisibility(mSimIndicatorIconShow && !mAirplaneMode ? View.VISIBLE : View.GONE);//del by kay
            mSimIndicatorIcon.setVisibility(View.GONE);//add by kay
        }
    }

    private void updateSimIndicator() {
        Xlog.d(TAG, "updateSimIndicator mIsSimIndicatorShowing = " + mIsSimIndicatorShowing + " mBusinessType is "
                + mBusinessType);
        if (mIsSimIndicatorShowing && mBusinessType != null) {
            showSimIndicator(mBusinessType);
        }
        if (mSimIndicatorIconShow && mBusinessType != null) {
            long simId = SIMHelper.getDefaultSIM(mContext, mBusinessType);
            if (mSimIndicatorIcon != null && simId != android.provider.Settings.System.DEFAULT_SIM_SETTING_ALWAYS_ASK
                    && simId != android.provider.Settings.System.VOICE_CALL_SIM_SETTING_INTERNET
                    && simId != android.provider.Settings.System.SMS_SIM_SETTING_AUTO) {
                mSimIndicatorIcon.setVisibility(View.GONE);
            }
        }
    }

    private void showAlwaysAskOrInternetCall(long simId) {
        mSimIndicatorIconShow = true;
        if (simId == android.provider.Settings.System.VOICE_CALL_SIM_SETTING_INTERNET) {
            mSimIndicatorIcon.setBackgroundResource(R.drawable.sim_indicator_internet_call);
        } else if (simId == android.provider.Settings.System.SMS_SIM_SETTING_AUTO) {
            mSimIndicatorIcon.setBackgroundResource(R.drawable.sim_indicator_auto);
        } else {
            mSimIndicatorIcon.setBackgroundResource(R.drawable.sim_indicator_always_ask);
        }
        if (!mAirplaneMode) {
            //mSimIndicatorIcon.setVisibility(View.VISIBLE); //del by kay
            mSimIndicatorIcon.setVisibility(View.GONE); //add by kay
        } else {
            mSimIndicatorIcon.setVisibility(View.GONE);
            mSimIndicatorIconShow = false;
        }
    }

    private static boolean isInternetCallEnabled(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.System.ENABLE_INTERNET_CALL, 0) == 1;
    }

    /// M: [SystemUI] Support "SIM Indicator". }@

    /// M: [SystemUI]Show application guide for App. @{
    private Dialog mAppGuideDialog;
    private Button mAppGuideButton;
    private String mAppName;
    private View mAppGuideView;
    private static final String SHOW_APP_GUIDE_SETTING = "settings";
    private static final String MMS = "MMS";
    private static final String PHONE = "PHONE";
    private static final String CONTACTS = "CONTACTS";
    private static final String MMS_SHOW_GUIDE = "mms_show_guide";
    private static final String PHONE_SHOW_GUIDE = "phone_show_guide";
    private static final String CONTACTS_SHOW_GUIDE = "contacts_show_guide";

    public void showApplicationGuide(String appName) {
        SharedPreferences settings = mContext.getSharedPreferences(SHOW_APP_GUIDE_SETTING, 0);
        mAppName = appName;
        Xlog.d(TAG, "showApplicationGuide appName = " + appName);
        if (MMS.equals(appName) && "1".equals(settings.getString(MMS_SHOW_GUIDE, "1"))) {
            createAndShowAppGuideDialog();
        } else if (PHONE.equals(appName) && "1".equals(settings.getString(PHONE_SHOW_GUIDE, "1"))) {
            createAndShowAppGuideDialog();
        } else if (CONTACTS.equals(appName) && "1".equals(settings.getString(CONTACTS_SHOW_GUIDE, "1"))) {
            createAndShowAppGuideDialog();
        }
    }

    public void createAndShowAppGuideDialog() {
        Xlog.d(TAG, "createAndShowAppGuideDialog");
        if ((mDisabled & StatusBarManager.DISABLE_EXPAND) != 0) {
            Xlog.d(TAG, "StatusBar can not expand, so return.");
            return;
        }
        mAppGuideDialog = new ApplicationGuideDialog(mContext, R.style.ApplicationGuideDialog);
        mAppGuideDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL);
        animateExpandNotificationsPanelSlow();
        mAppGuideDialog.show();
        ObjectAnimator oa = ObjectAnimator.ofFloat(mAppGuideView, "alpha", 0.0f, 1.0f);
        oa.setDuration(1500);
        oa.start();
    }

    private class ApplicationGuideDialog extends Dialog {

        public ApplicationGuideDialog(Context context, int theme) {
            super(context, theme);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mAppGuideView = View.inflate(mContext, R.layout.application_guide, null);
            setContentView(mAppGuideView);
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                final int themeMainColor = mContext.getResources().getThemeMainColor();
                if (themeMainColor != 0) {
                    TextView applicationGuideTitle = (TextView)mAppGuideView.findViewById(R.id.applicationGuideTitleText);
                    applicationGuideTitle.setTextColor(themeMainColor);
                }
            }
            mAppGuideButton = (Button) mAppGuideView.findViewById(R.id.appGuideBtn);
            mAppGuideButton.setOnClickListener(mAppGuideBtnListener);

        }

        @Override
        public void onBackPressed() {
            mAppGuideDialog.dismiss();
            animateCollapsePanels();
            super.onBackPressed();
        }

    }

    private View.OnClickListener mAppGuideBtnListener = new View.OnClickListener() {
        public void onClick(View v) {
            Xlog.d(TAG, "onClick! dimiss application guide dialog.");
            mAppGuideDialog.dismiss();
            animateCollapsePanels();
            SharedPreferences settings = mContext.getSharedPreferences(SHOW_APP_GUIDE_SETTING, 0);
            SharedPreferences.Editor editor = settings.edit();
            if (MMS.equals(mAppName)) {
                editor.putString(MMS_SHOW_GUIDE, "0");
                editor.commit();
            } else if (PHONE.equals(mAppName)) {
                editor.putString(PHONE_SHOW_GUIDE, "0");
                editor.commit();
            } else if (CONTACTS.equals(mAppName)) {
                editor.putString(CONTACTS_SHOW_GUIDE, "0");
                editor.commit();
            }
        }
    };

    public void dismissAppGuide() {
        if (mAppGuideDialog != null && mAppGuideDialog.isShowing()) {
            Xlog.d(TAG, "dismiss app guide dialog");
            mAppGuideDialog.dismiss();
            mNotificationPanel.cancelTimeAnimator();
            makeExpandedInvisible();
        }
    }

    private void refreshApplicationGuide() {
        if (mAppGuideDialog != null) {
            mAppGuideView = View.inflate(mContext, R.layout.application_guide, null);
            mAppGuideDialog.setContentView(mAppGuideView);
            if (FeatureOption.MTK_THEMEMANAGER_APP) {
                final int themeMainColor = mContext.getResources().getThemeMainColor();
                if (themeMainColor != 0) {
                    TextView applicationGuideTitle = (TextView)mAppGuideView.findViewById(R.id.applicationGuideTitleText);
                    applicationGuideTitle.setTextColor(themeMainColor);
                }
            }
            mAppGuideButton = (Button) mAppGuideView.findViewById(R.id.appGuideBtn);
            mAppGuideButton.setOnClickListener(mAppGuideBtnListener);
        }
    }
    /// M: [SystemUI]Show application guide for App. @}

    /// M: [SystemUI]Support ThemeManager. @{
    private void refreshExpandedView(Context context) {
        for (int i = 0, n = this.mNotificationData.size(); i < n; i++) {
            Entry entry = this.mNotificationData.get(i);
            inflateViews(entry, mPile);
        }
        loadNotificationShade();
        updateExpansionStates();
        setAreThereNotifications();
        mNotificationPanel.onFinishInflate();
        mToolBarView.mSimSwitchPanelView.updateSimInfo();
        if (mHasFlipSettings) {
            ImageView notificationButton = (ImageView) mStatusBarWindow.findViewById(R.id.notification_button);
            if (notificationButton != null) {
                notificationButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notifications));
            }
        }
        if (mHasSettingsPanel) {
            if (mStatusBarView.hasFullWidthNotifications()) {
                ImageView settingsButton = (ImageView) mStatusBarWindow.findViewById(R.id.settings_button);
                settingsButton.setImageDrawable(context.getResources()
                        .getDrawable(R.drawable.ic_notify_quicksettings));
            }
        } else {
            ImageView settingsButton = (ImageView) mStatusBarWindow.findViewById(R.id.settings_button);
            settingsButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notify_settings));
        }
        ImageView clearButton = (ImageView) mStatusBarWindow.findViewById(R.id.clear_all_button);
        clearButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notify_clear));
        ImageView headerSettingsButton = (ImageView) mStatusBarWindow.findViewById(R.id.header_settings_button);
        headerSettingsButton.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_notify_settings));
    }
    /// M: [SystemUI]Support ThemeManager. @}

    /// M: [ALPS00512845] Handle SD Swap Condition
    private ArrayList<IBinder> mNeedRemoveKeys;
    private boolean mAvoidSDAppAddNotification;
    private static final String EXTERNAL_SD0 = (FeatureOption.MTK_SHARED_SDCARD && !FeatureOption.MTK_2SDCARD_SWAP) ? "/storage/emulated/0" : "/storage/sdcard0";
    private static final String EXTERNAL_SD1 = "/storage/sdcard1";

    private BroadcastReceiver mMediaEjectBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            StorageVolume storageVolume = (StorageVolume) intent.getParcelableExtra(StorageVolume.EXTRA_STORAGE_VOLUME);
            if (storageVolume == null) {
                return;
            }
            String path = storageVolume.getPath();
            if (!EXTERNAL_SD0.equals(path) && !EXTERNAL_SD1.equals(path)) {
                return;
            }
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                Xlog.d(TAG, "receive Intent.ACTION_MEDIA_EJECT to remove notification & path = " + path);
                mAvoidSDAppAddNotification = true;
                if (mNeedRemoveKeys.isEmpty()) {
                    Xlog.d(TAG, "receive Intent.ACTION_MEDIA_EJECT to remove notificaiton done, array is empty");
                    return;
                }
                ArrayList<IBinder> copy = (ArrayList) mNeedRemoveKeys.clone();
                for (IBinder key : copy) {
                    removeNotification(key);
                }
                copy.clear();
                System.gc();
                Xlog.d(TAG, "receive Intent.ACTION_MEDIA_EJECT to remove notificaiton done, array size is " + mNeedRemoveKeys.size());
            } else if(action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                Xlog.d(TAG, "receive Intent.ACTION_MEDIA_MOUNTED, path =" + path);
                mAvoidSDAppAddNotification = false;
            }
        }
    };

//begin: add by joyisn 20141225. AIDL IPC with keyguardhostview.change the lock screen background alpha
    	public void onLockScreenAlphaChanged(float alpha){
		/*mStatusBarView.setBackgroundColor(getColor(7));
		mStatusBarView.getBackground().setAlpha((int)(alpha*255));*///del by xujia for set Lock screen
	}
//end: add by joyisn 20141225. AIDL IPC with keyguardhostview.change the lock screen background alpha


      /*Begin:added by xss for Show the number of unread messages */
	NidHashMap mNidHashMap=new NidHashMap();
	   
	public class Nid{
		public String pkg = "";
		public int id = -1;
		@Override
		public boolean equals(Object o) {
			if(o instanceof Nid) {
				Nid compare = (Nid)o;
				System.out.println("compare:"+compare);
				return id != -1 && compare.id != -1 
					&& !"".equals(pkg) && !"".equals(compare.pkg) 
					&& pkg.equals(compare.pkg) && compare.id == id; 
			}
			return super.equals(o);
		}
		
		public String toString(){
			return pkg + "//"+id;
		}
	}


	public class NidHashMap{
		HashMap<Nid,Integer> hs = new HashMap<Nid,Integer>();
		public NidHashMap(){
		}
		
		public int getNumber(Nid nid){
			if(nid == null || "".equals(nid.pkg) || -1 == nid.id) return 0;
			Iterator<Nid> s = hs.keySet().iterator();
			while(s.hasNext()) {
				Nid n = s.next();
				System.out.println("result:"+n);
				if(nid.equals(n)) {
					return hs.get(n);
				}
			}
			return 0;
		}
		public int getNumber(String pkg){
				if("".equals(pkg)) return 0;
				int number = 0;
				Iterator<Nid> s = hs.keySet().iterator();
				while(s.hasNext()) {
					Nid n = s.next();
					if(pkg.equals(n.pkg)) {
						number +=hs.get(n);
					}
				}
				return number;
			}
		
		public void putNumber(Nid nid,int number){
			if(nid == null || "".equals(nid.pkg) || -1 == nid.id) return;
			Iterator<Nid> s = hs.keySet().iterator();
			while(s.hasNext()) {
				Nid n = s.next();
				if(nid.equals(n)) {
					if(hs.get(n) >= 0) {
						hs.remove(n);
						break;
					}
				}
			}
			hs.put(nid, number);
		}

		public void clearNumber(String pkg){
			if(TextUtils.isEmpty(pkg)) return;
			Iterator<Nid> s = hs.keySet().iterator();
			while(s.hasNext()) {
				Nid n = s.next();
				if(n != null /*&& TextUtils.isEmpty(n.pkg)*/ && pkg.equals(n.pkg)) {//added by wangyouyou
					hs.remove(n);
					clearNumber(pkg);
					return;
				}
			}
		}
	}

 /*End:added by xss for Show the number of unread messages */
      
    /*Begin:added by xss for back to last app*/
	 private boolean notificationPanelViewIsShow=false;
	 private boolean notificationPanelViewIsClick=false;
	 private String lastAppPackageName;
	 private String lastAppClassName;
	 private String topAppPackageName;
	 private String topAppClassName;
	 private SignalClusterView mSignalClusterView;
	Handler myHandler= new Handler();
	Runnable mRunnable=new Runnable() {
		
		@Override
		public void run() {

                       /*Begin:added by xss for Show the number of unread messages */
			   Log.v("showUnread","top_activity_changes=======");
			  /*if(mUnreadMessagesNumber.containsKey(topAppPackageName)){
				mUnreadMessagesNumber.remove(topAppPackageName);
				mUnreadMessagesNumber.put(topAppPackageName,0);
			 }*/
			 mNidHashMap.clearNumber(topAppPackageName);
			  if(topAppPackageName!=null && !topAppPackageName.equals("com.android.mms") && !topAppPackageName.equals("com.android.dialer")){
				  Intent mIntent=new Intent("Show_the_number_of_unread_messages");
				  mIntent.putExtra("package_name",topAppPackageName);
				  mIntent.putExtra("unread_number",0);
				  mContext.sendBroadcast(mIntent);
			  } 
			  /*End:added by xss for Show the number of unread messages */ 
		         if(DEBUG)Log.i("backtolastapp","myHandler()  +++++topAppPackageName="+topAppPackageName+"  lastAppPackageName="+lastAppPackageName+"   notificationPanelViewIsShow="+notificationPanelViewIsShow+"   notificationPanelViewIsClick="+notificationPanelViewIsClick);	  
		        setBottomOrTopPanelBlurbg();
		        if(topAppPackageName!=null && !"com.hskj.hometest".equals(topAppPackageName) &&!"com.hskj.hometest".equals(lastAppPackageName) && !"com.android.packageinstaller".equals(lastAppPackageName) && !topAppPackageName.equals(lastAppPackageName) && notificationPanelViewIsShow && notificationPanelViewIsClick){
				 	showBackToLastAppView(lastAppPackageName);		  
			  }
		}
	};

	private void getAllTasks(){
        final ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(100);
        for(ActivityManager.RunningTaskInfo amTask : runningTasks){
            //Log.d("kay5", "getbaseTasks: " + amTask.baseActivity.getClassName());
            Log.d("kay5", "gettopActivityTasks: " + amTask.topActivity.getClassName());
        }
    }

	 private String getTopActivityPackageName(){
	    final ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
		if(am.getRunningTasks(1) != null && am.getRunningTasks(1).get(0) != null){  //added by gaojunbin
             final String currentActivityPackageName = ((ActivityManager.RunningTaskInfo)am.getRunningTasks(1).get(0)).topActivity.getPackageName();
			 return currentActivityPackageName;
		}
	    
		//if(DEBUG)Log.i("backtolastapp","currentActivityPackageName=="+currentActivityPackageName);	    	
              return null;
	}
	 private String getTopActivityClassName(){
	    	final ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
		if(am.getRunningTasks(1) != null && am.getRunningTasks(1).get(0) != null){  //added by gaojunbin
	            String TopActivityClassName = ((ActivityManager.RunningTaskInfo)am.getRunningTasks(1).get(0)).topActivity.getClassName();
			return  TopActivityClassName;
		  }
               return null;
	 }
       public String getProgramNameByPackageName(Context context,
	            String packageName) {
	        PackageManager pm = context.getPackageManager();
	        String name = null;
	        try {
	            name = pm.getApplicationLabel(
	                    pm.getApplicationInfo(packageName,
	                            PackageManager.GET_META_DATA)).toString();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return name;
	    }

	 private void showBackToLastAppView(String packageName){
	 	if(DEBUG)Log.i("backtolastapp","showBackToLastAppView()  ===================");	
	 	String appName;
		appName=getProgramNameByPackageName(mContext,packageName);		
		if(DEBUG)Log.i("backtolastapp","showBackToLastAppVisetBottomOrTopPanelBlurbgew()  back="+mBackButtonText+"  appName=="+appName);
		if(mSignalClusterView==null)mSignalClusterView=(SignalClusterView) mStatusBarView.findViewById(R.id.signal_cluster);
               if(mBackToLastAppView!=null && mBackToLastAppText !=null){
			  mSignalClusterView.setVisibility(View.GONE);
			  mBackToLastAppView.setVisibility(View.VISIBLE);
			  mBackToLastAppText.setText(mBackButtonText+"\""+appName+"\"");
	        }	
	 }
	 private void hideBackToLastAppView(){
	 	if(DEBUG)Log.i("backtolastapp","hideBackToLastAppView()  ++++++++++++++++++++++");		 
               if(mBackToLastAppView!=null && mSignalClusterView!=null){
			  	
			  mSignalClusterView.setVisibility(View.VISIBLE);
			  mBackToLastAppView.setVisibility(View.GONE);
			 if(notificationPanelViewIsShow && notificationPanelViewIsClick)onBack();
               }			
	 }

        private BroadcastReceiver mHomeKeyEventReceiver = new BroadcastReceiver() {  
        String SYSTEM_REASON = "reason";  
        String SYSTEM_HOME_KEY = "homekey";  
        String SYSTEM_HOME_KEY_LONG = "recentapps";  
           
        @Override  
        public void onReceive(Context context, Intent intent) {  
            String action = intent.getAction();  
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {  
                String reason = intent.getStringExtra(SYSTEM_REASON);  
                if (TextUtils.equals(reason, SYSTEM_HOME_KEY)) {  
                     //表示按了home键,程序到了后台  
                   if(notificationPanelViewIsShow && notificationPanelViewIsClick) {
			   	notificationPanelViewIsShow=false;
                             notificationPanelViewIsClick=false;
			   	hideBackToLastAppView();
                   	}
                    //Toast.makeText(getApplicationContext(), "home", 1).show();  
                }else if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)){  
                    //表示长按home键,显示最近使用的程序列表  
                    if(notificationPanelViewIsShow && notificationPanelViewIsClick){
			   	notificationPanelViewIsShow=false;
                             notificationPanelViewIsClick=false;
			   	hideBackToLastAppView();
                   	}
                }  
            }   
        }  
    };

   public void onBack(){
   	   notificationPanelViewIsShow=false;
	   notificationPanelViewIsClick=false; 
   	   //if(DEBUG)Log.i("backtolastapp3","onBack()  +++++lastAppPackageName="+lastAppPackageName+"   lastAppClassName="+lastAppClassName+"   topAppClassName="+topAppClassName);	
   	   Intent intent=new Intent(Intent.ACTION_MAIN);
	   intent.setClassName(lastAppPackageName, lastAppClassName);
	   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//FLAG_ACTIVITY_REORDER_TO_FRONT
	   mContext.startActivity(intent);
   	   
   }	
	 
   /*End:added by xss for back to last app*/

 /*Begin:add by yuhuizhong   xss */
     public static ImageView keyguardLockIv;	 
     private ImageView mBlurWallpaperBgView;
    private ImageView mBlurWallpaperBgViewright;
     private View mBlurActivityBgView;
     private View mBlurSettingBgView;
     private boolean setBlurEnable=true;	 
    private boolean isFistOpen=true;
		@Override
	public void setBitMapFromActivity(){
               if(true)Log.d("xssblur3", " PhoneStatusBar setBitMapFromActivity() setBlurEnable="+setBlurEnable);
                    setBottomOrTopPanelBlurbg();
    }

    public void setBottomOrTopPanelBlurbg(){
        if(true)Log.d("hjz3", " PhoneStatusBar setBottomOrTopPanelBlurbg() mBottomRootView="+mBottomRootView+"    mBlurWallpaperBgView="+mBlurWallpaperBgView);
        mBottomRootView.setBlurWallpaperBgView(mBlurWallpaperBgView);
        if(mBottomRootView != null){
            mBottomRootView.setBgDrawable(takeScreenshot());
        }
    }

   public boolean topActivityIsLauncher(){
           boolean mTopActivityIsLauncher=false;
        final ActivityManager am = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        final String currentActivityPackageName = ((ActivityManager.RunningTaskInfo)am.getRunningTasks(1).get(0)).topActivity.getPackageName();
    //if(DEBUG)Log.i("backtolastapp","currentActivityPackageName=="+currentActivityPackageName);
          if(true)Log.i("xssblur","BottomPanelBar  topActivityIsLauncher()    currentActivityPackageName="+currentActivityPackageName);
    if(currentActivityPackageName!=null)mTopActivityIsLauncher = currentActivityPackageName.equals("com.hskj.hometest")?true:false;
    return mTopActivityIsLauncher;
}

	private void setmBlurWallpaperBgView(){
        //Drawable workspaceBg = com.hskj.hometest.BitmapUtils.getBlurWallpaper(mContext) ;
        //mBlurWallpaperBgView.setBackground(workspaceBg);
        Log.d("hjz3", "setmBlurWallpaperBgView: " );
        if(mBlurWallpaperBgView != null && mBlurSettingBgView != null){
            mBlurSettingBgView.setVisibility(View.VISIBLE);
            mBlurWallpaperBgView.setVisibility(View.VISIBLE);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(mContext.getResources(),
                    "/data/data/com.android.providers.settings/blur_wallpaper.png");
            if(bitmapDrawable != null){
                Log.d("hjz3", "setmBlurWallpaperBgView: " + bitmapDrawable.getIntrinsicHeight());
                mBlurWallpaperBgView.setBackground(bitmapDrawable);
                mBlurSettingBgView.setBackground(bitmapDrawable);
            }
        }

    }

    private void setBlurSettingBgView(int type){
        if(mBlurSettingBgView != null && mBlurWallpaperBgView != null){
            if(type == SHOWTYPE_LEFT){
               // mBlurWallpaperBgView.setBackground(mBgDrawable);
                mBlurWallpaperBgView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.livewallpapers_10));
                Log.d("hjz3", "setBlurSettingBgView: visible:" + mBgDrawable+"==left");
                mBlurWallpaperBgViewright.setVisibility(View.GONE);
            }else{
               // BitmapUtils.getBlurWallpaper(mContext);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(mContext.getResources(),
                        "/data/data/com.android.providers.settings/blur_wallpaper.png");
                mBlurWallpaperBgViewright.setVisibility(View.VISIBLE);
                mBlurWallpaperBgViewright.setImageDrawable(BitmapUtils.getBlurWallpaper(mContext));
                Log.d("hjz3", "setBlurSettingBgView: gone:" + mBgBlurDrawable+"===right");
               // mBlurWallpaperBgView.setVisibility(View.VISIBLE);
            }

            //mBlurSettingBgView.setAlpha(0);
        }
    }

	 public Bitmap takeScreenshot() {
	 	   long start=System.currentTimeMillis();
                 Bitmap mScreenBitmap;   
	          WindowManager  myWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);  
		  Display mDisplay = myWindowManager.getDefaultDisplay();  
		  DisplayMetrics mDisplayMetrics = new DisplayMetrics();  
		  mDisplay.getRealMetrics(mDisplayMetrics);  
		  Matrix mDisplayMatrix = new Matrix();  
		  float[] dims = { mDisplayMetrics.widthPixels,mDisplayMetrics.heightPixels};  
		  DisplayManager mDisplayManager = (DisplayManager)mContext.getSystemService(Context.DISPLAY_SERVICE);		  		 
	         float degrees = getDegreesForRotation(mDisplay.getRotation());  
		  if(DEBUG)Log.d("xssblur3", "takeScreenshot()   dims = " + dims[0] + "," + dims[1] + " of " + degrees);	 
		  boolean requiresRotation = (degrees > 0);  
		  if (requiresRotation) {  
			   // Get the dimensions of the device in its native orientation  
			   mDisplayMatrix.reset();  
			   mDisplayMatrix.preRotate(-degrees);  
			   mDisplayMatrix.mapPoints(dims);  
			  
			   dims[0] = Math.abs(dims[0]);  
			   dims[1] = Math.abs(dims[1]);  
		  }  
		 if(DEBUG)Log.i("xssblur3","PhoneStatusBar takeScreenshot()  width ="+dims[0]+" height="+dims[1]);	
	        mScreenBitmap = Surface.screenshot((int) dims[0], (int) dims[1]); 
			if(mScreenBitmap == null) return null;//added by lzp
		 if(DEBUG)Log.i("xssblur3","PhoneStatusBar takeScreenshot()  width ="+mScreenBitmap.getWidth()+" height="+mScreenBitmap.getHeight());		  
		 if (requiresRotation) {
	            // Rotate the screenshot to the current orientation
	            Bitmap ss = Bitmap.createBitmap(mDisplayMetrics.widthPixels / 1,
	                    mDisplayMetrics.heightPixels / 1, Bitmap.Config.RGB_565);//modified  by lzp /2
	            Canvas c = new Canvas(ss);
	            c.translate(ss.getWidth() / 2, ss.getHeight() / 2);
	            c.rotate(degrees);
	            c.translate(-dims[0] /2, -dims[1] / 2);
		    	//c.scale(0.5f, 0.5f);
	            c.drawBitmap(mScreenBitmap, 0, 0, null);
	            c.setBitmap(null);
	            // Recycle the previous bitmap
	            mScreenBitmap.recycle();
	            mScreenBitmap = ss;
	        }
		 long end=System.currentTimeMillis();
		 android.util.Log.i("xssblur3","PhoneStatusBar takeScreenshot() time="+(end-start));
		 return mScreenBitmap;
         } 

     private float getDegreesForRotation(int value) {  
		  switch (value) {  
		  case Surface.ROTATION_90:  
		   return 360f - 90f;  
		  case Surface.ROTATION_180:  
		   return 360f - 180f;  
		  case Surface.ROTATION_270:  
		   return 360f - 270f;  
		  }  
		  return 0f;  
       }	  
	public  Bitmap getBitmap(byte[] data){  
	      return BitmapFactory.decodeByteArray(data, 0, data.length);//\u4ece\u5b57\u8282\u6570\u7ec4\u89e3\u7801\u4f4d\u56fe  
	} 

   /*Begin:added by xss for back to last app*/   
    private LinearLayout mBackToLastAppView;
    private View  mBackToLastAppIcon;
    private TextView mBackToLastAppText;
    private CharSequence mBackButtonText;	
     /*End:added by xss for back to last app*/ 

     /*Begin:added by xss for back to phone*/
 private TextView backToPhoneBtn;
 private boolean isLockScreen=false;
 private View backToPhoneLandsView;
	 private boolean phoneIsUsed(Context context){
                boolean calling = false;  
	         TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	         if(telephonyManager.getCallState()==TelephonyManager.CALL_STATE_OFFHOOK || telephonyManager.getCallState()==TelephonyManager.CALL_STATE_RINGING){
	        	 calling=true;        	 
	         }
	         if(DEBUG)Log.i("back_to_phone","PhoneStatusBar  phoneIsUsed()-----calling="+calling);
	         return calling;
	 }
        public void showBackToPhoneBtn(boolean show){
		 if(DEBUG)Log.i("back_to_phone","PhoneStatusBar showBackToPhoneBtn()-----show="+show);
		 backToPhoneBtn.setVisibility(show?View.VISIBLE:View.GONE);
               FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams) mStatusBarView.getLayoutParams();
               lp.height = show?80:mNaturalBarHeight;//modified by lzp for ios fhd from 80:40
		 mNaturalBarHeight = show?80:mNaturalBarHeight;//modified by lzp for ios fhd  from 80:40
            Log.i("kay18","PhoneStatusBar showBackToPhoneBtn()-----lp.height="+lp.height+"   mNaturalBarHeight="+mNaturalBarHeight);
		 Settings.System.putInt(mContext.getContentResolver(), Settings.System.STATUS_BAR_HEIGHT,mNaturalBarHeight); 	
		 mStatusBarView.setLayoutParams(lp);
		 mStatusBarView.requestLayout();
		 
		 final WindowManager.LayoutParams wmlp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                mNaturalBarHeight,
                WindowManager.LayoutParams.TYPE_STATUS_BAR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);

	        wmlp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            //wmlp.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
	        wmlp.gravity = getStatusBarGravity();
	        wmlp.setTitle("StatusBar");
	        wmlp.packageName = mContext.getPackageName();

		 //mStatusBarWindow.setLayoutParams(lp);
		 //mStatusBarWindow.requestLayout();
		 if(DEBUG)Log.i("back_to_phone","PhoneStatusBar showBackToPhoneBtn()-----isLockScreen=="+isLockScreen);
            //Log.d("kay18", "showBackToPhoneBtn: mNaturalBarHeight:" + mNaturalBarHeight);
            if(!isLockScreen)mWindowManager.updateViewLayout(mStatusBarWindow, wmlp);
		 
        }
        public void hideLandsView(){
                if(landsview.getVisibility()==View.VISIBLE){
			mUpdateHandler.postDelayed(mresetStatusBar, 300);		
		  }
	}		
	class mPhoneStateListener extends PhoneStateListener{

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			// TODO Auto-generated method stub
			super.onCallStateChanged(state, incomingNumber);
			if(state==TelephonyManager.CALL_STATE_IDLE){				
				showBackToPhoneBtn(false);
			}
		}		
		
	}	
 /*End:added by xss for back to phone*/
	  
 /*End:add by yuhuizhong   xss */
   ApplicationPreview mApplicationPreview;//added by xss for ios10
   LinearLayout appPreviewSearchText;
   /*Begin:added by xss for ios10 lunarDate*/		
	public void saveLunarDate(){
              final Calendar mCalendar = Calendar.getInstance(); 
		IphoneLunar lunar = new IphoneLunar(mCalendar);
		if(DEBUG)Log.i(TAG,"saveLunarDate()-----lunar.toString()="+lunar.toString());
		Settings.System.putString(mContext.getContentResolver(),"app_widget_lunar_date_view_text",lunar.toString()); 
	}
 /*End:added by xss for ios10 lunarDate*/


    private boolean callStateIsRinging(Context context){
        boolean calling = false;
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if(telephonyManager.getCallState()==TelephonyManager.CALL_STATE_RINGING){
            calling=true;
        }
        if(DEBUG)Log.i("back_to_phone","PhoneStatusBar  phoneIsUsed()-----calling="+calling);
        return calling;
    }




    private int statusBarHeight;
    AnimatorSet mScreenshotAnimation;
    ValueAnimator oneAnimation;
    ValueAnimator twoAnimation ;
    float mScaleX,mScaleY,lastY;
    long firstTime=0;
    private boolean isGoing = false,closeAppToLauncherEnable = false;
    boolean isAnimationBackOver = true;
    boolean isOpenRecents = false;
    boolean isOpenRecenting = false;
    private  VelocityTracker  vt=null;
    private boolean isUpOrDownSlide;
    ImageView mBlurLauncherIv, mScreenShotIv;
    private int widthPixels,heightPixels;
    Bitmap takeScreenBitmap,noStatusBarBitmap,roundedCornerBitmap;

    private void recycleBitmap(){
        if(takeScreenBitmap!=null && !takeScreenBitmap.isRecycled()){//modified by xss for ios11
            takeScreenBitmap.recycle();
        }
        if(noStatusBarBitmap!=null && !noStatusBarBitmap.isRecycled()){//modified by xss for ios11
            noStatusBarBitmap.recycle();
        }
        if(roundedCornerBitmap!=null && !roundedCornerBitmap.isRecycled()){//modified by xss for ios11
            roundedCornerBitmap.recycle();
        }
    }


    private void CloseScreenShot(){
        if(mScreenShotIv != null && mScreenShotIv.getParent()!=null && mBlurLauncherIv!=null && mBlurLauncherIv.getParent()!=null){

            if(mBlurLauncherIv.getParent()!=null) {
                //mBlurLauncherIv.setBackground(null);
                //mScreenShotIv.setBackground(null);
                mWindowManager.removeView(mScreenShotIv);
                mWindowManager.removeView(mBlurLauncherIv);
                recycleBitmap();
                mScreenShotIv = null;
                Log.d("kay5", "run: CloseScreenShot recycleBitmap() , mScreenShotIv = null");
            }

            /*mScaleX = mScreenShotIv.getScaleX();
            mScaleY = mScreenShotIv.getScaleY();

            mBlurLauncherIv.setAlpha(1f);
            mBlurLauncherIv.setScaleX(mScaleX);
            mBlurLauncherIv.setScaleY(mScaleY);

            final ValueAnimator animx = ValueAnimator.ofFloat(mScaleX, 0f);
            final ValueAnimator animy = ValueAnimator.ofFloat(mScaleY, 0f);
            animx.setDuration(5000);
            animy.setDuration(5000);
            animx.addUpdateListener(new AnimatorUpdateListener() {
                @java.lang.Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float valuex = (Float)animation.getAnimatedValue();
                    mScreenShotIv.setScaleX(valuex);
                    mBlurLauncherIv.setScaleX(valuex);
                }
            });

            animy.addUpdateListener(new AnimatorUpdateListener() {
                @java.lang.Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float valuey = (Float)animation.getAnimatedValue();
                    mScreenShotIv.setScaleY(valuey);
                    mBlurLauncherIv.setScaleY(valuey);
                }
            });
            animy.addListener(new Animator.AnimatorListener() {
                @java.lang.Override
                public void onAnimationStart(Animator animation) {

                }

                @java.lang.Override
                public void onAnimationEnd(Animator animation) {
                    if(mBlurLauncherIv.getParent()!=null) {
                        //mBlurLauncherIv.setBackground(null);
                        //mScreenShotIv.setBackground(null);
                        mWindowManager.removeView(mScreenShotIv);
                        mWindowManager.removeView(mBlurLauncherIv);
                        recycleBitmap();
                        mScreenShotIv = null;
                        Log.d("kay5", "run: CloseScreenShot recycleBitmap() , mScreenShotIv = null");
                    }
                }

                @java.lang.Override
                public void onAnimationCancel(Animator animation) {

                }

                @java.lang.Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            Log.d("kay5", "CloseScreenShot: mScaleX:" +mScaleX + " mScaleY:" + mScaleY );

            ValueAnimator animz = ValueAnimator.ofFloat(1.0f, 0f);
            animz.setDuration(100);
            animz.addListener(new Animator.AnimatorListener() {
                @java.lang.Override
                public void onAnimationStart(Animator animation) {

                }

                @java.lang.Override
                public void onAnimationEnd(Animator animation) {

                    mBlurLauncherIv.setAlpha(0f);
                    animx.start();
                    animy.start();
                }

                @java.lang.Override
                public void onAnimationCancel(Animator animation) {

                }

                @java.lang.Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animz.start();*/

        }

    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,float roundPx){

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private ValueAnimator createOneAnimation() {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
//        anim.setDuration(200);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator arg0) {
                isAnimationBackOver = false;
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                if(closeAppToLauncherEnable){ //added by xss
                    Intent showLuncherIntent = new Intent("CLOSE_APP_TO_LUNCHER");  //by kay
                    mContext.sendBroadcast(showLuncherIntent);
                }
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                float t = (Float) arg0.getAnimatedValue();
                mScreenShotIv.setScaleX(mScaleX- (mScaleX-0.5f)*t);
                mScreenShotIv.setScaleY(mScaleY- (mScaleY-0.5f)*t);
            }
        });
        return anim;
    }

    float mPointX,mPointY;
    //@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private ValueAnimator createTwoAnimation() {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 0.5f);
//        anim.setDuration(200);
        anim.setInterpolator(new AccelerateInterpolator());
        anim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationStart(Animator arg0) {
                mPointX = Settings.Global.getFloat(mContext.getContentResolver(), "launcher_x", 0f);
                mPointY = Settings.Global.getFloat(mContext.getContentResolver(), "launcher_y", 0f);
            }

            @Override
            public void onAnimationRepeat(Animator arg0) {

            }

            @Override
            public void onAnimationEnd(Animator arg0) {
                if(mBlurLauncherIv!=null) {
                    mBlurLauncherIv.setAlpha(1f);
                    mScreenShotIv.setAlpha(1f);
                    mBlurLauncherIv.setScaleX(0f);
                    mBlurLauncherIv.setScaleY(0f);
                    isOpenRecents = false;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(mBlurLauncherIv.getParent()!=null) {
                                mWindowManager.removeView(mBlurLauncherIv);
                                mWindowManager.removeView(mScreenShotIv);
                                recycleBitmap();
                            }
                            isAnimationBackOver = true;
                        }
                    }, 500);
                }
            }

            @Override
            public void onAnimationCancel(Animator arg0) {

            }
        });

        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {
                float t = (Float) arg0.getAnimatedValue();
                mScreenShotIv.setScaleX(0.5f - t);
                mScreenShotIv.setScaleY(0.5f - t);
                mScreenShotIv.setPivotX(t*(mPointX-widthPixels/2)/0.5f+widthPixels/2);
                mScreenShotIv.setPivotY(t*(mPointY-heightPixels/2)/0.5f+heightPixels/2);
            }
        });
        return anim;
    }


    private void startAnimatorBack(ImageView mImageView) {
        //透明动画
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator animatorX = ObjectAnimator.ofFloat(mImageView, "scaleX", mScreenShotIv.getScaleX(), 1f);
        ObjectAnimator animatorY = ObjectAnimator.ofFloat(mImageView, "scaleY", mScreenShotIv.getScaleX(), 1f);
        set.playTogether(animatorX, animatorY);
        set.setInterpolator(new AccelerateInterpolator());
        set.setDuration(100);
        //为动画设置监听
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                isAnimationBackOver = false;
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if(mBlurLauncherIv.getParent()!=null) {
                    mWindowManager.removeView(mBlurLauncherIv);
                    mWindowManager.removeView(mScreenShotIv);
                    recycleBitmap();
                }
                isAnimationBackOver = true;
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        set.start();

    }

    private ObjectAnimator createObjScaleAnimator(View view,String behavier, float src, float dst,int durationTime){
        ObjectAnimator objani = ObjectAnimator.ofFloat(view,behavier,src, dst);
        objani.setInterpolator(new AccelerateInterpolator());
        objani.setDuration(durationTime);
        return objani;
    }

    private void slideDownEvent(MotionEvent ev){
        //Log.i("slide_xss","PhonestatusBar       slideDownEvent()   ev = "+ev);
        WindowManager mWindowManager1 = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display mDisplay = mWindowManager1.getDefaultDisplay();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);
        float[] dims = {mDisplayMetrics.widthPixels, mDisplayMetrics.heightPixels};
        widthPixels = (int) dims[0];
        heightPixels = (int) dims[1];

        if (vt == null) {
            //初始化velocityTracker的对象 vt 用来监测motionevent的动作
            vt = VelocityTracker.obtain();
        } else {
            vt.clear();
        }
        vt.addMovement(ev);

        if (!topAppClassName.equals("com.android.systemui.recent.RecentsActivity")
                && !getTopActivityPackageName().equals("com.mediatek.oobe")/*&&!isInLockScreen()*/
                && !topAppPackageName.equals("com.hskj.hometest")) {

            //mScreenShotIv/
            Log.d("kay5", "slideDownEvent: ");
            takeScreenBitmap = takeScreenshot();
            if(takeScreenBitmap!=null){//added by xss for ios11
                noStatusBarBitmap = Bitmap.createBitmap(takeScreenBitmap, 0, statusBarHeight, widthPixels, heightPixels
                        - statusBarHeight);
                roundedCornerBitmap = getRoundedCornerBitmap(noStatusBarBitmap,50f);

                mScreenShotIv = new ImageView(mContext);
                mScreenShotIv.setImageBitmap(BitmapUtils.scaleBitmap(roundedCornerBitmap,0.5f));
            }
            //mBlurLauncherIv/
            BitmapDrawable blurLauncherDrawable = new BitmapDrawable(mContext.getResources(),
                    "/data/data/com.android.providers.settings/blur_wallpaper.png"); //"/data/blur_wallpaper.png"

            mBlurLauncherIv = new ImageView(mContext);
            mBlurLauncherIv.setBackground(blurLauncherDrawable);//BitmapUtils.scaleBitmap(BitmapUtils.getBlurWallpaper(mContext) mmmbitmap ,0.5f)
            mBlurLauncherIv.setAlpha(0f);
            mBlurLauncherIv.setScaleX(1.0f);

            final int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
            final WindowManager.LayoutParams mScreenShotlp = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                    flags,
                    PixelFormat.TRANSLUCENT);
            mScreenShotlp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            mScreenShotlp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

            final WindowManager.LayoutParams blurLauncherIv = new WindowManager.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                    flags,
                    PixelFormat.TRANSLUCENT);
            blurLauncherIv.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
            blurLauncherIv.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

            if(mBlurLauncherIv!=null && mBlurLauncherIv.getParent()==null) {
                mWindowManager.addView(mBlurLauncherIv, blurLauncherIv);
            }

            if(mScreenShotIv!=null && mScreenShotIv.getParent()==null) {
                mWindowManager.addView(mScreenShotIv, mScreenShotlp);
            }

            /*mScreenshotAnimation = new AnimatorSet();
            oneAnimation = createOneAnimation();
            twoAnimation = createTwoAnimation();
            mScreenshotAnimation.playSequentially(oneAnimation,twoAnimation);
            mScreenshotAnimation.setDuration(120);*/

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBlurLauncherIv.setAlpha(1f);
                }
            },100);

        }
    }

    private void slideMoveCloseBlurScreen(){
        Log.d("kay5", "slideMoveCloseBlurScreen:isOpenRecenting:" + isOpenRecenting);
        if(true /*!isOpenRecenting*/){
            return;
        }

        isOpenRecenting = false;
        if(!topAppPackageName.equals("com.hskj.hometest") && mBlurLauncherIv!=null && mBlurLauncherIv.getParent()!=null && mScreenShotIv!=null && mScreenShotIv.getParent()!=null) {
            //new Handler().postDelayed(new Runnable() {
            //    @Override
            //    public void run() {

                    //mScreenShotIv.setAlpha(0f);
                    //mScreenShotIv.setScaleX(0f);
                    //mScreenShotIv.setScaleY(0f);
                    // mBlurLauncherIv.setScaleX(0f);
                    // mBlurLauncherIv.setScaleY(0f);
                    //        mBlurLauncherIv.setAlpha(1f);
                    //        mScreenShotIv.setAlpha(1f);
                    //          mScaleX = mScreenShotIv.getScaleX();
                    //          mScaleY = mScreenShotIv.getScaleY();
                    Log.d("kay5", "---+++---: mScaleX:" +mScaleX + " mScaleY:" + mScaleY );
                    //          mScreenShotIv.setScaleX(mScaleX );
                    //          mScreenShotIv.setScaleY(mScaleY);
                    //           mBlurLauncherIv.setScaleX(mScaleX );
                    //           mBlurLauncherIv.setScaleY(mScaleY);
                    if(mBlurLauncherIv.getParent()!=null) {

                        mWindowManager.removeView(mScreenShotIv);
                        mWindowManager.removeView(mBlurLauncherIv);
                        recycleBitmap();
                        Log.d("kay5", "run: recycleBitmap()");
                    }
                    //CloseScreenShot();

            //    }
            //}, 10);
        }
    }

    private void slideMoveEvent(MotionEvent ev){
        //Log.i("slide_xss","PhonestatusBar       slideMoveEvent()   ev = "+ev);
        if(isGoing&&isAnimationBackOver&&!topAppClassName.equals("com.android.systemui.recent.RecentsActivity")&& !getTopActivityPackageName().equals("com.mediatek.oobe")){

            if (vt != null) {
                vt.addMovement(ev);
                vt.computeCurrentVelocity(1000);
            }
            int dy = (int) (ev.getRawY() - mLastY);
            int dir = (int) (ev.getRawY() - lastY);    //add by kay
            if (dir  < 0) {//down

                isUpOrDownSlide = false;

            } else if (dir  > 0) {//up

                isUpOrDownSlide = true;

            }
            int instanceY = (int) (ev.getRawY());
            int dis = (int)lastY - instanceY;
            int upRecentDis = heightPixels - 350;
            Log.d("kay5", "slideMoveEvent: mLastY:" + mLastY +" lastY:" + lastY);
            Log.d("kay5", "slideMoveEvent: dis:" + dis + " instanceY:" + instanceY + " heightPixels:" + heightPixels + "  upRecentDis:" + upRecentDis + " dy :" + dy);
            //Log.d("kay5", "slideMoveEvent: " + topAppPackageName + " isOpenRecents:"+isOpenRecents);
            if(topAppPackageName.equals("com.hskj.hometest")){

            }else{
                         /*instanceY<0*/
                if (mScreenShotIv!=null && mScreenShotIv.getParent()!=null && instanceY != 0 && -instanceY < 500 && mScreenShotIv.getScaleX()>=0.5) {
                    float mvRate = 1 - (float)dis/heightPixels;
                    //if(mvRate > 0.9f){
                    //    mvRate = 0.7f;
                    //}
                    mScreenShotIv.setScaleX(mvRate); //1 - (float) 0.001 * (heightPixels-instanceY)
                    mScreenShotIv.setScaleY(mvRate); //1 - (float) 0.001 * (heightPixels-instanceY)
                //    mScreenShotIv.invalidate();
                    //mBlurLauncherIv.setScaleX(mvRate);
                    //mBlurLauncherIv.setScaleY(mvRate);
                    Log.d("kay5", "slideMoveEvent: mvRate:" + mvRate);
                }
            }


            if (!isOpenRecents && dy == 0 && vt.getYVelocity() < 0.1 && instanceY < upRecentDis) {
                Log.d("kay5", "slideMoveEvent: dy:" + dy + " vt.getYVelocity():"+vt.getYVelocity() + "  instanceY:" + instanceY + " upRecentDis:" + upRecentDis);

                Intent showRecentsIntent = new Intent("OPEN_RECENTS_ACTIVITY");
                mContext.sendBroadcast(showRecentsIntent);
                isOpenRecents = true;
                isOpenRecenting = true;

                if(!topAppPackageName.equals("com.hskj.hometest") && mBlurLauncherIv!=null && mBlurLauncherIv.getParent()!=null && mScreenShotIv!=null && mScreenShotIv.getParent()!=null) {
                    //new Handler().postDelayed(new Runnable() {
                    //     @Override
                    //     public void run() {

                             mScreenShotIv.setAlpha(0f);
                             mScreenShotIv.setScaleX(0f);
                             mScreenShotIv.setScaleY(0f);
                           // mBlurLauncherIv.setScaleX(0f);
                           // mBlurLauncherIv.setScaleY(0f);
                    //        mBlurLauncherIv.setAlpha(1f);
                    //        mScreenShotIv.setAlpha(1f);
                  //          mScaleX = mScreenShotIv.getScaleX();
                  //          mScaleY = mScreenShotIv.getScaleY();
                            Log.d("kay5", "---+++---: mScaleX:" +mScaleX + " mScaleY:" + mScaleY );
                            if(mBlurLauncherIv.getParent()!=null) {

                                mWindowManager.removeView(mScreenShotIv);
                                mWindowManager.removeView(mBlurLauncherIv);
                                recycleBitmap();
                                Log.d("kay5", "run: recycleBitmap()");
                            }
                            //CloseScreenShot();

                    //    }
                   // }, 200);
                }

            }
            mLastY = ev.getRawY();
        }
    }


    private void slideUpCancelEvent(MotionEvent ev){
        //Log.i("slide_xss","PhonestatusBar       slideUpCancelEvent()   ev = "+ev);
        closeAppToLauncherEnable = (lastY - ev.getRawY()) > 200;//added by xss
        Log.d("kay5", "slideUpCancelEvent: closeAppToLauncherEnable:" + closeAppToLauncherEnable + " lastY :" + (lastY - ev.getRawY()) + " isOpenRecents:" + isOpenRecents);
        Log.d("kay5", "isGoing: :" + isGoing + " isAnimationBackOver :" + isAnimationBackOver);
        Log.d("kay5", "slideUpCancelEvent: topAppClassName:" + topAppClassName + " topAppPackageName:" + topAppPackageName);
        if(isGoing && isAnimationBackOver) {

            if(isOpenRecents){

            }else{
                Log.d("kay5", "slideUpCancelEvent: isUpOrDownSlide:" + isUpOrDownSlide + " lastY - ev.getRawY():" + (lastY - ev.getRawY()));
                if(mScreenShotIv!=null&&mScreenShotIv.getParent()!=null&&mBlurLauncherIv!=null&&mBlurLauncherIv.getParent()!=null) {
                    /*if (!isUpOrDownSlide && closeAppToLauncherEnable) {// up //modified by xss
                        mScaleX = mScreenShotIv.getScaleX();
                        mScaleY = mScreenShotIv.getScaleY();
                        //mScreenshotAnimation.start();//by kay
                        CloseScreenShot();
                        if (!isOpenRecents) {
                            Intent showLuncherIntent = new Intent("CLOSE_APP_TO_LUNCHER");
                            mContext.sendBroadcast(showLuncherIntent);
                        }
                        return;
                    } else {// down
                        startAnimatorBack(mScreenShotIv);
                    }*/
                    Log.d("kay5", "slideUpCancelEvent: CloseScreenShot()");
                    CloseScreenShot();
                    if(closeAppToLauncherEnable && !isUpOrDownSlide){
                        if (!isOpenRecents) {
                            Intent showLuncherIntent = new Intent("CLOSE_APP_TO_LUNCHER");
                            mContext.sendBroadcast(showLuncherIntent);
                        }
                    }

                }
            }

            /*if(!"1".equals(SystemProperties.get("ro.default_gdc")) || SystemProperties.get("cenon.soft.mode").equals("1")) {//added by xss for gdc
                PackageManager pm = mContext.getPackageManager();
                ComponentName name = new ComponentName("com.mediatek.oobe", "com.mediatek.oobe.WizardActivity");
                int state = pm.getComponentEnabledSetting(name);

                if(getTopActivityPackageName().equals("com.mediatek.oobe") || state != PackageManager.COMPONENT_ENABLED_STATE_DISABLED){
                    Intent showLuncherIntent = new Intent("start_oobe_from_bottom_slide_view");//modified by xss for ios11
                    mContext.sendBroadcast(showLuncherIntent);
                    return;
                }
            }*/
            if(topAppClassName.equals("com.android.systemui.recent.RecentsActivity")&&!isOpenRecents  && closeAppToLauncherEnable){
                Intent showLuncherIntent = new Intent("CLOSE_APP_TO_LUNCHER"); //by kay
                mContext.sendBroadcast(showLuncherIntent);
                return;
            }

            if (topAppPackageName.equals("com.hskj.hometest")  && closeAppToLauncherEnable) {
                if(!isOpenRecents){
                    Intent showLuncherIntent = new Intent("CLOSE_APP_TO_LUNCHER"); //by kay
                    mContext.sendBroadcast(showLuncherIntent);
                    return;
                }
            }
        }

        isOpenRecents = false;
    }

    private View.OnTouchListener setOnTouchSlideListener = new View.OnTouchListener(){

        @Override
        public boolean onTouch(View view, MotionEvent ev) {
            final int action = ev.getAction();
            //Log.i("slide_xss","PhonestatusBar   setOnTouchSlideListener    onTouch()   d0  m2 u1 action = "+action);
            if(topAppClassName.equals("com.android.incallui.InCallActivity") && callStateIsRinging(mContext))return false;
            switch (action){
                case MotionEvent.ACTION_DOWN:
                    lastY = ev.getRawY();
                    long timeNow = System.currentTimeMillis();
                    if(timeNow-firstTime>500){
                        isGoing = true;
                    }else{
                        isGoing = false;
                    }
                    firstTime = timeNow;
                    //Log.i("slide_xss","PhonestatusBar   setOnTouchSlideListener    onTouch()   ACTION_DOWN isGoing = "+isGoing+"     isAnimationBackOver="+isAnimationBackOver);
                    if(isGoing&&isAnimationBackOver) {
                        slideDownEvent(ev);
                    }

                    break;
                case MotionEvent.ACTION_MOVE:
                    slideMoveEvent(ev);
                    if(isOpenRecents){
                        break;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL://added by lzp

                    slideUpCancelEvent(ev);
                    isGoing = false;
                    break;
            }
            return false;
        }
    };

    /*begin add by kay for slide*/
    int x=0,y=0;
    private boolean isAddSlideView= false;
    private ImageView mSlideView = null;
    private void addOrRemoveSlideView(boolean isAdd){
        WindowManager  mWindowManager1 = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display mDisplay = mWindowManager1.getDefaultDisplay();
        DisplayMetrics mDisplayMetrics = new DisplayMetrics();
        mDisplay.getRealMetrics(mDisplayMetrics);
        Matrix mDisplayMatrix = new Matrix();
        float[] dims = { mDisplayMetrics.widthPixels,mDisplayMetrics.heightPixels};

        x = (int)mDisplayMetrics.widthPixels;
        y = (int)mDisplayMetrics.heightPixels;
        //Log.i("slide_xss","PhonestatusBar       addOrRemoveSlideView()   x= "+x+"    y="+y);
        if(isAdd){
            if(topAppPackageName!=null) {
                if (!isAddSlideView) {
                    final int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                            | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
                    final WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                            /*ViewGroup.LayoutParams.MATCH_PARENT*/274,
                            /*ViewGroup.LayoutParams.MATCH_PARENT*/22,
                            WindowManager.LayoutParams.TYPE_STATUS_BAR_PANEL,
                            flags,
                            PixelFormat.TRANSLUCENT);
                    lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
                    lp.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    mSlideView = new ImageView(mContext);
                    mSlideView.setOnTouchListener(setOnTouchSlideListener);
                    if(topAppPackageName.equals("com.hskj.hometest")){
                        mSlideView.setBackgroundResource(0);
                    }else if(topAppPackageName.equals("com.android.mms")) {
                        mSlideView.setBackgroundResource(R.drawable.slide_black);
                    }else{

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap mBitmap = takeScreenshot();
                                if(mBitmap!=null){//added by xss for ios11
                                    int pixel = mBitmap.getPixel(mBitmap.getWidth()/2, mBitmap.getHeight()-1);
                                    int redValue = Color.red(pixel);
                                    int blueValue = Color.blue(pixel);
                                    int greenValue = Color.green(pixel);
                                    int grayLevel = (int) (redValue * 0.299 + blueValue * 0.587 + greenValue * 0.114);
                                    if (grayLevel >= 220) {
                                        mSlideView.setBackgroundResource(R.drawable.slide_black);
                                    } else {
                                        mSlideView.setBackgroundResource(R.drawable.slide_white);
                                    }
                                }
                            }
                        },500);
                    }
                    mWindowManager.addView(mSlideView, lp);
                    isAddSlideView = true;
                }else{
                    if(topAppPackageName.equals("com.hskj.hometest")){
                        mSlideView.setBackgroundResource(0);
                    }else if(topAppPackageName.equals("com.android.mms")) {
                        mSlideView.setBackgroundResource(R.drawable.slide_black);
                    }else {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Bitmap mBitmap = takeScreenshot();
                                if(mBitmap!=null){	//added by xss for ios11
                                    //Log.d("chenshichun"," "+this.getClass().getCanonicalName()+" ::::::mBitmap.getWidth()/2:::  "+mBitmap.getWidth()/2+"   mBitmap.getHeight()-1   "+(mBitmap.getHeight()-1));
                                    int pixel = mBitmap.getPixel(mBitmap.getWidth()/2, mBitmap.getHeight()-1);
                                    //Log.d("chenshichun"," "+this.getClass().getCanonicalName()+" ::::pixel:::::"+pixel);
                                    int redValue = Color.red(pixel);
                                    int blueValue = Color.blue(pixel);
                                    int greenValue = Color.green(pixel);
                                    //Log.d("chenshichun"," "+this.getClass().getCanonicalName()+" ::::redValue:::::"+redValue);

                                    int grayLevel = (int) (redValue * 0.299 + blueValue * 0.587 + greenValue * 0.114);
                                    if (grayLevel >= 220) {
                                        mSlideView.setBackgroundResource(R.drawable.slide_black);
                                    } else {
                                        mSlideView.setBackgroundResource(R.drawable.slide_white);
                                    }
                                }
                            }
                        },500);
                    }
                }
            }
        }else{
            if (isAddSlideView) {
                mWindowManager.removeView(mSlideView);
                isAddSlideView = false;
            }
        }

        if(isAdd){
            mSlideView.setAlpha(1.0f);
            ValueAnimator anim = ValueAnimator.ofFloat(1.0f, 0f);
            anim.setDuration(10000);
            anim.addUpdateListener(new AnimatorUpdateListener() {
                @java.lang.Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float)animation.getAnimatedValue();
                    mSlideView.setAlpha(value);
                }
            });
            //anim.start();
        }
    }

    private void killAppProcess(String packageName){
        ActivityManager activityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        //activityManager.forceStopPackage(packageName);
        activityManager.killBackgroundProcesses(packageName);
    }

    private boolean mIsAppstoreOpened = false;
    private void AppTopActivityChange(){
        topAppPackageName=getTopActivityPackageName();
        topAppClassName=getTopActivityClassName();

        if(topAppPackageName.equals("com.sztuyue.app.store")){
            //killAppProcess(topAppPackageName);
            mIsAppstoreOpened = true;
        }else{
            if(mIsAppstoreOpened){
                Log.d("kay77", "AppTopActivityChange: kill " + topAppPackageName);
                //final Intent intent = new Intent("com.sztuyue.app.store");
                //mContext.sendBroadcast(intent);
                mIsAppstoreOpened = false;
            }
        }

        if(topAppClassName.equals("com.android.systemui.recent.RecentsActivity")) {
            slideMoveCloseBlurScreen();
        }
        //  getAllTasks();
			  /*Begin:added by xss for back to phone*/
        if(DEBUG)Log.i("back_to_phone","PhoneStatusBar top_activity_changes   -----true");
        showBackToPhoneBtn(phoneIsUsed(mContext) && !topAppClassName.equals("com.android.phone.InCallScreen")&& !topAppClassName.equals("com.android.contacts.Dialtacts"));
			  /*End:added by xss for back to phone*/
        // add by kay for slide
        Log.d("kay80", "onReceive: top_activity_changes:" + topAppPackageName + " Classname:" + topAppClassName + " KeyguardLocked:" + mKeyguardManager.isKeyguardLocked()
                + "\n inKeyguardRestrictedInputMode:" + mKeyguardManager.inKeyguardRestrictedInputMode());
        if(topAppPackageName!=null &&  topAppClassName!=null){
            if(!mKeyguardManager.isKeyguardLocked())
            {
                addOrRemoveSlideView(true);
            }
            if((topAppPackageName.equals("com.mediatek.oobe")&&!topAppClassName.equals("com.mediatek.oobe.basic.OobeLockScreenActivity"))
                    ||topAppClassName.equals("com.mediatek.settings.SetupWizardForOobe")||topAppClassName.equals("com.android.settings.OobeDisplayModeSettings")
                    ||(topAppClassName.equals("com.android.incallui.InCallActivity") && callStateIsRinging(mContext))){
                Log.d("kay80", "AppTopActivityChange: addOrRemoveSlideView(false)");
                addOrRemoveSlideView(false);
            }

            //   if(!topAppClassName.equals("com.android.systemui.recents.RecentsActivity"))
            //      isOpenRecents = false;
            //if(topAppPackageName.equals("com.mediatek.camera"))mKeyguardBottomArea.updateFlashBtn();//added by xss for ios11
            //if(!topAppPackageName.equals("com.hskj.hometest") && !topAppPackageName.equals("com.android.packageinstaller"))updateStatusBarContentsViewAndEditFinishParent(false);//added by xss for ios11
        }
        Log.d("backtolastapp", "mBroadcastReceiver() lastAppPackageName="+lastAppPackageName+"     topAppPackageName="+topAppPackageName+"   notificationPanelViewIsShow="+notificationPanelViewIsShow);
        myHandler.removeCallbacks(mRunnable);
        myHandler.postDelayed(mRunnable, 1500);
        Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::topAppPackageName:"+topAppPackageName+"  topAppPackageName::"+topAppPackageName);
        if(topAppPackageName.equals("com.hskj.hometest")){
            Settings.System.putInt(mContext.getContentResolver(),"IS_TOP_APP_LAUNCHER",1);
            //addOrRemoveSlideView(true);
        }else{
            Settings.System.putInt(mContext.getContentResolver(),"IS_TOP_APP_LAUNCHER",0);
        }
    }
}
