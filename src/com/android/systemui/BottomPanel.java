package com.android.systemui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.WallpaperManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.IRemoteControlDisplay;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IPowerManager;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.android.internal.R;
import com.android.systemui.R;
import com.android.internal.telephony.TelephonyIntents;
import com.android.internal.telephony.TelephonyProperties;
import com.mediatek.common.featureoption.FeatureOption;

import java.lang.ref.WeakReference;
import java.util.Formatter;
import java.util.Locale;

//add by yuhuizhong xss
import android.view.Surface;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.view.View.OnTouchListener;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.graphics.Matrix;
import android.content.res.Configuration;
import java.io.ByteArrayOutputStream;
import android.os.AsyncTask;
import com.hskj.iphone.blur.StackBlur;//added by xss for ios10 blur
/* Begin: added by yuanhuawei 20130826 */
/* End: added by yuanhuawei 20130826 */
//add by xujia 20130922
//end by xujia 20130922
import com.hskj.iphone.view.IphoneMusicSeekbar;
import com.hskj.iphone.view.RoundRectImageView;
import com.hskj.iphone.view.BitmapUtils;

public class BottomPanel extends FrameLayout implements SeekBar.OnSeekBarChangeListener, View.OnClickListener {
	
	private static final String TAG = BottomPanel.class.getSimpleName();
	protected static final boolean DEBUG = true;

	//private PhoneStatusBar mStatusBar;
	private final static String ACTION_DISTURBANCE_MODE_CHANGED="com.android.settings.disturbanceMode";
	private static final String IPO_BOOT = "android.intent.action.ACTION_PREBOOT_IPO";

	private int mContentHeight, mTouchHandleHeight;

	private KeyguardManager mKeyguardManager;
	private ControlCenterCallback mCallback;
	
	public BottomPanel(Context context) {
		this(context, null, 0);
	}
	
	public BottomPanel(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public void setCallback(ControlCenterCallback callback) {
		mCallback = callback;
	}

	public interface ControlCenterCallback {
		public void closeControlCenter();
	}


	private IRemoteControlDisplayWeak mIRCD;
	private int mTransportControlFlags;
	private Handler mSeekHandler = new Handler();
	private static final long DRAG_INTERVAL = 50;
	private static final String ACTION_SEEK_TO_POSITION = "action_seek_to_position";
	private static final String PLAYER_POSITION = "player_position";
	private static final int DISPLAY_TIMEOUT_MS = 5000; // 5s

	private boolean mAttached;
	private PendingIntent mClientIntent;

	private ImageView mBtnPrev, mBtnNext, mBtnPlay;
         private TextView airDrop;

    private ImageView mAirplayImageView;
    private TextView mAirplayTextView;
    private ImageView mAirdropImageView;
    private TextView mAirdropTextView;
    private ImageView mNightshiftImageView;
    private TextView mNightshiftTextView;
    private LinearLayout mAirplayBtn;//add by scq
    private LinearLayout mAirdropBtn;//add by scq
    private LinearLayout mNightShiftBtn;//add by scq
    private boolean mAirplayIsChecked=false; //add by scq
    private int mAirdropIsChecked=0; //add by scq
    private ImageView mMusicHeadstArrow;//add by scq

    /**
     * The metadata which should be populated into the view once we've been attached
     */
    private Bundle mPopulateMetadataWhenAttached = null;

	
	
	public BottomPanel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext=context;//added by xss 
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mCurrentPlayState = RemoteControlClient.PLAYSTATE_NONE; // until we get a callback
        mIRCD = new IRemoteControlDisplayWeak(mHandler);
		mContentHeight = (int) getResources().getDimension(R.dimen.control_center_content_height);
		mTouchHandleHeight = (int) getResources().getDimension(R.dimen.control_center_touch_handle_height);

		mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		
		initViews(R.layout.bottom_panel);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		isLightOn= (Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.FLASH_LIGHT, -1)==1);//IphoneUtils.getFlashLightMode();
        
		if(isChangeBackgroundAttached)//added by lzp for bug [2814]
			changedBackground(false, false);
		
        if (mPopulateMetadataWhenAttached != null) {
            updateMetadata(mPopulateMetadataWhenAttached);
            mPopulateMetadataWhenAttached = null;
        }
        if (!mAttached) {
            if (DEBUG) Log.v(TAG, "Registering TCV " + this);
            mAudioManager.registerRemoteControlDisplay(mIRCD);
        }
        mAttached = true;

		register();
		mHandler.post(updateRunnable);
    
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

        if (mAttached) {
            if (DEBUG) Log.v(TAG, "Unregistering TCV " + this);
            mAudioManager.unregisterRemoteControlDisplay(mIRCD);
        }
        mAttached = false;
		unregister();
	}

	public void resetMusicPlayer() {
        if (mAttached) {
            if (DEBUG) Log.v(TAG, "Unregistering TCV " + this);
            mAudioManager.unregisterRemoteControlDisplay(mIRCD);
        }
        mAttached = false;

       // if (mPopulateMetadataWhenAttached != null) {//del by lzp
       updateMetadata(mPopulateMetadataWhenAttached);
       mPopulateMetadataWhenAttached = null;
       // }
        if (!mAttached) {
            if (DEBUG) Log.v(TAG, "Registering TCV " + this);
            mAudioManager.registerRemoteControlDisplay(mIRCD);
        }
        mAttached = true;
	}


    public int getContentHeight() {
		return mContentHeight;
    }

    public int getTouchHandleHeight() {
		return mTouchHandleHeight;
    }

	public void resetTouchHandle(boolean show) {
		mTouchHandle.setVisibility(show ? VISIBLE : GONE);
	}

    public void resetTouchHandleTwo(boolean show) {// add by csc
        //Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::"+mTouchHandleTwo+"  show::"+show);
//        if(mTouchHandleTwo!=null)
//        mTouchHandleTwo.setVisibility(show ? VISIBLE : GONE);
//        rootLayout.setX(0);
//        mRightBottomScreenFl.setX(getWidth());
        //Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::mTouchHandleTwo::::::"+mTouchHandleTwo.getVisibility());
    }

	private SeekBar mBrightnessSeekBar, mVolumeSeekBar;
    private IphoneMusicSeekbar mMusicProgressSeekBar;
    private Button mFlashlightBtn, mTimerBtn, mCalculatorBtn, mCameraBtn;
    private final int mScreenBrightnessMinimum = 30;
    private final int mScreenBrightnessMaximum =255;
    private int mOldBrightness;
    private AudioManager mAudioManager;
	private LinearLayout mBottomWindow;
	public View mTitleLayout;
	private TextView mTitleView;
	private View mTitleCurveLineView;
	private View mTitleStraightLineView;
	private boolean mStraight = true;

       
    private boolean isLightOn=false; //add by xujia 20130922
          
	private static final int TITLE_ANIMATION_TIME = 100;

	private ImageView mAirplaneBtn, mWifiBtn, mBluetoothBtn, mDisturbanceBtn, mRotationBtn;

	public View mTouchHandle,mTouchHandleTwo;
	private LinearLayout mRightBottomScreenFl;// add by csc
    private LinearLayout rootLayout;//add by csc
    public float mLastX;// add by csc
    private float mTranslationX;// add by csc
    private ImageView dianOneIv,dianTwoIv;
    private void initViews(int layutId) {
        //Log.d("kay3-", "initViews: BottomPanel");
        LayoutInflater.from(getContext()).inflate(R.layout.bottom_panel, this);
        //mRightBottomScreenFl = (LinearLayout)findViewById(R.id.right_bottom_screen);// add by csc
        rootLayout = (LinearLayout) findViewById(R.id.root_layout);// add by csc

		mTouchHandle = findViewById(R.id.touch_handle);

		mAirplaneBtn = (ImageView) findViewById(R.id.airplane_mode_iv);
		mWifiBtn = (ImageView) findViewById(R.id.wifi_iv);
		mBluetoothBtn = (ImageView) findViewById(R.id.bluetooth_iv);
		mDisturbanceBtn = (ImageView) findViewById(R.id.disturbance_iv);
		mRotationBtn = (ImageView) findViewById(R.id.rotation_iv);
        final View btns[] = { mAirplaneBtn, mWifiBtn, mBluetoothBtn, mDisturbanceBtn, mRotationBtn };
        for (View view : btns) {
            view.setOnClickListener(this);
           // Log.d("kay3", "initViews: " + view.toString());
        }

             /*Begin:added by xss for night shift*/
        mAirplayBtn= (LinearLayout) rootLayout.findViewById(R.id.airplay_layout);// add by scq
//        mAirplayImageView = (ImageView) rootLayout.findViewById(R.id.airplay_iv);// add by scq
        mAirplayTextView = (TextView) rootLayout.findViewById(R.id.airplay_text);
        if(mAirplayBtn != null)mAirplayBtn.setOnClickListener(this);// add by scq
        if(mAirplayBtn!=null)setAirplayBtnBg(mAirplayIsChecked); // add by scq

        //mAirplayTextView.setText(mContext.getResources().getString(R.string.status_bar_screen));
        mAirdropBtn= (LinearLayout) rootLayout.findViewById(R.id.airdrop_layout);// modify by scq
        mAirdropImageView = (ImageView) rootLayout.findViewById(R.id.airdrop_iv);// add by scq
//        mAirdropTextView = (TextView) rootLayout.findViewById(R.id.airdrop_text);// add by scq
        if(mAirdropBtn != null)mAirdropBtn.setOnClickListener(this);// add by scq
        if(mAirdropBtn!=null)setAirdropBtnBg(mAirdropIsChecked); // add by scq

        //mAirdropTextView.setText(mContext.getResources().getString(R.string.status_bar_airdrop));
//        mNightShiftBtn= (LinearLayout) rootLayout.findViewById(R.id.nightshift_layout);// modify by scq
//        mNightshiftImageView = (ImageView) rootLayout.findViewById(R.id.nightshift_iv);
//        mNightshiftTextView = (TextView) rootLayout.findViewById(R.id.nightshift_text);

//        if(mNightShiftBtn != null)mNightShiftBtn.setOnClickListener(this);


		mBrightnessSeekBar = (SeekBar) findViewById(R.id.brightness_seek_bar);
		mBrightnessSeekBar.setOnSeekBarChangeListener(this);
		mBrightnessSeekBar.setMax(mScreenBrightnessMaximum - mScreenBrightnessMinimum);
		mOldBrightness = getBrightness(0);
		mBrightnessSeekBar.setProgress(mOldBrightness - mScreenBrightnessMinimum);

//        dianOneIv = (ImageView) findViewById(R.id.dain_one_iv);
//        dianTwoIv = (ImageView) findViewById(R.id.dain_two_iv);
//        dianOneIv.setBackgroundResource(R.drawable.iphone_dian_bai);
//        dianTwoIv.setBackgroundResource(R.drawable.iphone_dian_gray);

		/*mMusicProgressSeekBar = (SeekBar) findViewById(R.id.music_seek_bar);
		mMusicProgressSeekBar.setOnSeekBarChangeListener(this);
		mMusicProgressSeekBar.setProgress(0);

		mVolumeSeekBar = (SeekBar) findViewById(R.id.music_volume_seek_bar);
		// mVolumeSeekBar.setOnSeekBarChangeListener(this);//del by lzp
		int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		mVolumeSeekBar.setMax(maxVolume);
		mVolumeSeekBar.setProgress(currentVolume);
       	        mVolumeSeekBar.setOnSeekBarChangeListener(this);//added by lzp*/
                airDrop=(TextView)findViewById(R.id.airdrop_tv);
        
		mFlashlightBtn = (Button) findViewById(R.id.flashlight_btn);
 			/*isLightOn=IphoneUtils.getFlashLightMode();
            camera=IphoneUtils.getCamera();*/

            	isLightOn= (Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.FLASH_LIGHT, -1)==1);
        //add by xujia 20130922
            if(isLightOn)
                {
                     mFlashlightBtn.setBackgroundResource(R.drawable.iphone_flashlight_white);
                }
            PackageManager pm = getContext().getPackageManager();

        // if device support camera?
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e("err", "Device has no camera!");
           mFlashlightBtn.setEnabled(false);
          
        }
                    
                
        //end by xujia
        /*try{
        		Camera camera = Camera.open();
        		if(camera == null) {
        			mFlashlightBtn.setEnabled(false);
        		} else {
        			mFlashlightBtn.setEnabled(true);
        			camera.release();
        			camera = null;
        		}
            }catch(Exception e)
                {
                    
                }*/

         Log.d("xujia&&&", "mFlashlightBtn"+mFlashlightBtn.isEnabled());
		mTimerBtn = (Button) findViewById(R.id.timer_btn);
		mCalculatorBtn = (Button) findViewById(R.id.calculator_btn);
		mCameraBtn = (Button) findViewById(R.id.camera_btn);
		mFlashlightBtn.setOnClickListener(this);
		mTimerBtn.setOnClickListener(this);
		mCalculatorBtn.setOnClickListener(this);
		mCameraBtn.setOnClickListener(this);


		mTitleLayout = findViewById(R.id.title_layout);

		mTitleView = (TextView) findViewById(R.id.title_tv);
		
		mTitleCurveLineView = findViewById(R.id.title_curve_line_iv);
		/**Begin: added by lzp **/
		mTitleCurveLineView.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v){
				//Log.i("liuzepeng","click " + (mCallback == null));
				if(mCallback != null) {
					mCallback.closeControlCenter();
				}
			}
		});
		/**End: added by lzp **/
		mTitleStraightLineView = findViewById(R.id.title_straight_line_iv);
		
		mBottomWindow = (LinearLayout) findViewById(R.id.content_layout);
		//mBottomWindow.setBackgroundColor(0xffff0000);//del by lzp

//        initMusicViews(mRightBottomScreenFl);
//		initMusicViews();
//              mBlurBg=(ImageView)findViewById(com.android.internal.R.id.blur_bg);//added by xss
//              mBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background);//added by xss for ios10 on 20170214
              //mBlurWallpaperBg=getBlurWallpaperBg();	//added by xss //del by lzp for oom
        mRoundRectImageView = new RoundRectImageView();//add by scq
	}





    private LinearLayout mMusicDeviceList;
    private LinearLayout mMusicDeviceAll;
    private RelativeLayout mMusicDevice;
    private LinearLayout mMusicTitlePictures;
    private ImageView mMusicPictures;

    private RelativeLayout mObtainMusic;
    private ImageView mObtainMusicImageView;

    private ImageView mIphoneMusicImage;
    private TextView mIphoneMusicBeginning;
    private TextView mIphoneMusicNoumenon;
    private TextView mIphoneMusicEnd;

    private ImageView mIphoneMusicOneImage;
    private TextView mIphoneMusicOneBeginning;
    private TextView mIphoneMusicOneNoumenon;
    private TextView mIphoneMusicOneEnd;

    private ImageView mControlMusicPlayBtn;
    private ImageView mControlMusicForwardBtn;
    private LinearLayout mMusicControlLayout;
    private ImageView mBlurBg, mMusicBlurBg;//added by xss  for Blur
    static BitmapDrawable mSavedBlurBg;

    private View mMusicControl;
    private boolean isMusicDevice = false;
    private RelativeLayout mMusicLayout;
    private TextView mObtainMusicTextView;
    private TextView mObtainMusicBodyTextView;
    private void initMusicViews(View bottomWindowMusic) {
        mMusicLayout = (RelativeLayout) bottomWindowMusic.findViewById(R.id.music_layout);
        mPlayingTime = (TextView) bottomWindowMusic.findViewById(R.id.music_playing_time_tv);
        mMusicProgressSeekBar = (IphoneMusicSeekbar) bottomWindowMusic.findViewById(R.id.music_seek_bar);
        if(mMusicProgressSeekBar != null)mMusicProgressSeekBar.setOnSeekBarChangeListener(this);
        if(mMusicProgressSeekBar != null)mMusicProgressSeekBar.setProgress(0);
        mLeftTime = (TextView) bottomWindowMusic.findViewById(R.id.music_left_time_tv);
 //       mObtainMusicTextView = (TextView) bottomWindowMusic.findViewById(R.id.obtain_music_tv);
 //       mObtainMusicTextView.setText(mContext.getResources().getString(R.string.status_bar_music));
 //       mObtainMusicBodyTextView = (TextView) bottomWindowMusic.findViewById(R.id.obtain_music_tvb);
 //       mObtainMusicBodyTextView.setText(mContext.getResources().getString(R.string.status_bar_music_body));
 //       mTouchHandleTwo= findViewById(R.id.touch_handle_two);
        mVolumeSeekBar = (SeekBar) bottomWindowMusic.findViewById(R.id.music_volume_seek_bar);
        // mVolumeSeekBar.setOnSeekBarChangeListener(this);//del by lzp
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if(mVolumeSeekBar != null)mVolumeSeekBar.setMax(maxVolume);
        if(mVolumeSeekBar != null)mVolumeSeekBar.setProgress(currentVolume);
        if(mVolumeSeekBar != null)mVolumeSeekBar.setOnSeekBarChangeListener(this);//added by lzp

        mTrackTitle = (TextView) bottomWindowMusic.findViewById(R.id.title);
        mTrackArtist = (TextView) bottomWindowMusic.findViewById(R.id.artist);
        mNoMusicLayout = bottomWindowMusic.findViewById(R.id.no_music_layout);
        mMusicProgressLayout = bottomWindowMusic.findViewById(R.id.music_progress_layout);
//        mMusicControlLayout = (LinearLayout) bottomWindowMusic.findViewById(R.id.music_control_layout);
//        Log.d("chenshichun",""+this.getClass().getCanonicalName()+":::::::::::::::::::::"+mMusicControlLayout.getHeight());
//        mMusicControl = bottomWindowMusic.findViewById(R.id.control_music);
//        mMusicDeviceList = (LinearLayout) bottomWindowMusic.findViewById(R.id.music_device_list);
//        mMusicDevice = (RelativeLayout) bottomWindowMusic.findViewById(R.id.music_device);
//        mMusicDeviceAll = (LinearLayout) bottomWindowMusic.findViewById(R.id.music_control_all);
//        mMusicHeadstArrow = (ImageView) bottomWindowMusic.findViewById(R.id.iphone_headset_arrow);
//        if(mMusicDevice != null /*&& !isScreenChange()*/){
//            mMusicDevice.setOnClickListener(this);
 //       }
//        mMusicTitlePictures = (LinearLayout) bottomWindowMusic.findViewById(R.id.music_title_pictures);
//        mMusicPictures = (ImageView) bottomWindowMusic.findViewById(R.id.music_pictures);

//        mObtainMusic =  (RelativeLayout) bottomWindowMusic.findViewById(R.id.obtain_music_relative);
//        mObtainMusicImageView = (ImageView) bottomWindowMusic.findViewById(R.id.obtain_music_iv);

//        mIphoneMusicImage = (ImageView) bottomWindowMusic.findViewById(R.id.iphone_music_image);
//        mIphoneMusicBeginning = (TextView) bottomWindowMusic.findViewById(R.id.iphone_music_beginning);
//        mIphoneMusicNoumenon = (TextView) bottomWindowMusic.findViewById(R.id.iphone_music_noumenon);
//        mIphoneMusicEnd = (TextView) bottomWindowMusic.findViewById(R.id.iphone_music_end);

//        mIphoneMusicOneImage = (ImageView) bottomWindowMusic.findViewById(R.id.iphone_music_image_one);
//        mIphoneMusicOneBeginning = (TextView) bottomWindowMusic.findViewById(R.id.iphone_music_one_beginning);
//        mIphoneMusicOneNoumenon = (TextView) bottomWindowMusic.findViewById(R.id.iphone_music_one_noumenon);
//        mIphoneMusicOneEnd = (TextView) bottomWindowMusic.findViewById(R.id.iphone_music_one_end);

//        mControlMusicPlayBtn = (ImageView) bottomWindowMusic.findViewById(R.id.control_music_play_btn);
//        mControlMusicForwardBtn = (ImageView) bottomWindowMusic.findViewById(R.id.control_music_forward_btn);
//        if(mControlMusicPlayBtn != null || mControlMusicForwardBtn != null){
//            mControlMusicPlayBtn.setOnClickListener(this);
 //           mControlMusicForwardBtn.setOnClickListener(this);
 //       }
        mBtnPrev = (ImageView) bottomWindowMusic.findViewById(R.id.music_backward_btn);
        mBtnNext = (ImageView) bottomWindowMusic.findViewById(R.id.music_forward_btn);
        mBtnPlay = (ImageView) bottomWindowMusic.findViewById(R.id.music_play_btn);
        changeMusicViewsWithMeta(0);//add by joyisn
        if(mBtnPrev != null || mBtnPlay != null || mBtnNext != null){
            final View buttons[] = { mBtnPrev, mBtnPlay, mBtnNext };
            for (View view : buttons) {
                view.setOnClickListener(this);
            }
        }
        handler=new Handler();// add by csc
 //       mMusicBlurBg=(ImageView)bottomWindowMusic.findViewById(R.id.music_blur_bg);//added by xss
 //       mMusicBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background); //added by xss for ios10 on 20170214
       // if(mMusicBlurBg != null || mSavedBlurBg != null)mMusicBlurBg.setBackground(mSavedBlurBg);//del by xss for ios10 controlCenter bg
    }





    //begin add by joyisn
    private LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    private void changeMusicViewsWithMeta(int state){
        //Log.v("MusicSeekBar","changeMusicViewsWithMeta state="+state);
       /* switch(state) {
            case 0:
                mNoMusicLayout.setVisibility(View.GONE);
                mMusicProgressLayout.setVisibility(View.GONE);
                mMusicTitlePictures.setVisibility(View.GONE);//add by scq
                mObtainMusic.setVisibility(View.VISIBLE);//add by scq
                lp.setMargins(0, 0, 0, 50);
                mMusicControlLayout.setLayoutParams(lp);
                break;
            default:
                mNoMusicLayout.setVisibility(View.GONE);
                mMusicProgressLayout.setVisibility(View.VISIBLE);
                mMusicTitlePictures.setVisibility(View.VISIBLE);//add by scq
                mObtainMusic.setVisibility(View.GONE);//add by scq
                lp.setMargins(0, 0, 0, 0);
                mMusicControlLayout.setLayoutParams(lp);
                break;
        }*/
    }
    //end add by joyisn

	public void setTouchHandleListener(View.OnTouchListener listener) {
		if(mTitleLayout != null) {
			mTitleLayout.setOnTouchListener(listener);
		}
		
		if(mTouchHandle != null) {
			mTouchHandle.setOnTouchListener(listener);
		}
	}

	public void setInLockScreen() {
      /*  FrameLayout.LayoutParams llp = (FrameLayout.LayoutParams) mTouchHandle.getLayoutParams();
		llp.rightMargin = 120;
		LinearLayout rootLayout = (LinearLayout) findViewById(R.id.root_layout);
		rootLayout.updateViewLayout(mTouchHandle, llp);*/
	}

	private WallpaperManager mWallpaperManager;
	private Bitmap mBgDrawable;
	/**Begin: added by lzp for bug [2814] **/
 	private boolean isChangeBackgroundAttached = true;

	public void setIsChangeBackgroundAttached(boolean bool){
		isChangeBackgroundAttached = bool;
	}
	
	public Bitmap getBgDrawable(){
		return mBgDrawable;
	}

	public void setBgDrawable(Bitmap bgDrawable){
		Log.d("xssblur", "setBgDrawable() xss bgDrawable="+bgDrawable+"   mBottomWindow="+mBottomWindow+"  mMusicBlurBg="+mMusicBlurBg);
		if(bgDrawable == null  || mBottomWindow == null /*|| mBlurBg == null*/ /*|| mMusicBlurBg == null*/)
			return;
		//mBgDrawable = bgDrawable;
		//mBottomWindow.setBackground(mBgDrawable);
		/*Begin:added by xss for ForceTouch*/
		//if(DEBUG)Log.i("xssblur","BottomPanelBar  topActivityIsLauncher()    topActivityIsLauncher()="+topActivityIsLauncher());
		//if(topActivityIsLauncher()){//removed by xss for ios10
			forceTouchBlurBg(bgDrawable);//added by xss for ForceTouch	  
	       //}
		/*End:added by xss for ForceTouch*/
	}
	/**End: added by lzp for bug [2814] **/		
	public void changedBackground(boolean transparentBg, boolean forceChangeToWallpaper) {
		if(transparentBg) {
			//mBottomWindow.setBackgroundColor(0x00000000);//del by xss for ios10
		} else {
			if(forceChangeToWallpaper) {
				mBgDrawable = null;
			}
			//mHandler.postDelayed(mSetBgRunnable, 1500);
		}
	}

	private Runnable mSetBgRunnable = new Runnable() {
		public void run() {
			final boolean portrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
			if(mBgDrawable != null) {
				//mBottomWindow.setBackground(mBgDrawable);
			} else {
				mWallpaperManager = (WallpaperManager) mContext.getSystemService(mContext.WALLPAPER_SERVICE);
			    final int wallPaperWidth = mWallpaperManager.getDesiredMinimumWidth();
				final int wallPaperHeight =	mWallpaperManager.getDesiredMinimumHeight();	
				/* Begin: changed by yuanhuawei 20130826 */
				WindowManager wm = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
         			DisplayMetrics metrics = new DisplayMetrics();
         			wm.getDefaultDisplay().getMetrics(metrics);
				final int screenWidth = portrait ? metrics.widthPixels : metrics.heightPixels;
				final int screenHeight = portrait ? metrics.heightPixels : metrics.widthPixels;
				final int clipLeft = (wallPaperWidth - screenWidth)/2, clipTop = (wallPaperHeight - screenHeight)/2;
				Bitmap wallpaperBitMap = Bitmap.createBitmap(mWallpaperManager.getBitmap(),
									clipLeft, screenHeight - mContentHeight + clipTop, screenWidth, mContentHeight, null,false);
				/* End: changed by yuanhuawei 20130826 */
				/*mBgDrawable = new BitmapDrawable(getResources(), BitmapUtils.BoxBlurFilter(wallpaperBitMap));
				mBottomWindow.setBackground(mBgDrawable);*///del by xss for ios10
			}
		}
	};
	

	private TextView mPlayingTime, mLeftTime, mTrackTitle, mTrackArtist;
	private View mNoMusicLayout, mMusicProgressLayout;
	private void initMusicViews() {
		/*mPlayingTime = (TextView) findViewById(R.id.music_playing_time_tv);
		mLeftTime = (TextView) findViewById(R.id.music_left_time_tv);
		mTrackTitle = (TextView) findViewById(R.id.title);
		mTrackArtist = (TextView) findViewById(R.id.artist);
		mNoMusicLayout = findViewById(R.id.no_music_layout);
		mMusicProgressLayout = findViewById(R.id.music_progress_layout);

		mBtnPrev = (ImageView) findViewById(R.id.music_backward_btn);
		mBtnNext = (ImageView) findViewById(R.id.music_forward_btn);
		mBtnPlay = (ImageView) findViewById(R.id.music_play_btn);
        final View buttons[] = { mBtnPrev, mBtnPlay, mBtnNext };
        for (View view : buttons) {
            view.setOnClickListener(this);
        }*/
	}


    /*Begin:added by scq for airplay */
    private void setAirplayBtnBg(boolean open){
        if(open){
//            if(mAirplayImageView!=null)mAirplayImageView.setBackgroundResource(R.drawable.iphone_airplay);
            if(mAirplayTextView!=null)mAirplayTextView.setText(mContext.getResources().getString(com.android.internal.R.string.status_bar_screen));
        }else{
 //           if(mAirplayImageView!=null)mAirplayImageView.setBackgroundResource(R.drawable.iphone_airplay);
            if(mAirplayTextView!=null)mAirplayTextView.setText(mContext.getResources().getString(com.android.internal.R.string.status_bar_screen));
        }
    }
     /*End:added by scq for airplay */

    /*Begin:added by scq for airdrop */
    private void setAirdropBtnBg(int open){
        if(open == 1){
            if(mAirdropImageView!=null)mAirdropImageView.setBackgroundResource(com.android.internal.R.drawable.iphone_airdrop_blue);
//            if(mAirdropTextView!=null)mAirdropTextView.setText(mContext.getResources().getString(R.string.status_bar_only_contacts));
            mAirdropIsChecked = 2;
        }else if(open == 2){
            if(mAirdropImageView!=null)mAirdropImageView.setBackgroundResource(com.android.internal.R.drawable.iphone_airdrop_blue);
//            if(mAirdropTextView!=null)mAirdropTextView.setText(mContext.getResources().getString(R.string.status_bar_holder));
            mAirdropIsChecked = 0;
        }else{
            if(mAirdropImageView!=null)mAirdropImageView.setBackgroundResource(com.android.internal.R.drawable.iphone_airdrop_black);
//            if(mAirdropTextView!=null)mAirdropTextView.setText(mContext.getResources().getString(R.string.status_bar_airdrop));
            mAirdropIsChecked = 1;
        }
    }
     /*End:added by scq for airdrop */

    /*Begin:added by xss for night shift */
    private void setNightShiftBtnBg(boolean open){
        if(open){
//            if(mNightshiftImageView!=null)mNightshiftImageView.setBackgroundResource(R.drawable.iphone_nightshift_open);
//            if(mNightshiftTextView!=null)mNightshiftTextView.setText(mContext.getResources().getString(R.string.status_bar_turn_on));
        }else{
//            if(mNightshiftImageView!=null)mNightshiftImageView.setBackgroundResource(R.drawable.iphone_nightshift);
//            if(mNightshiftTextView!=null)mNightshiftTextView.setText(mContext.getResources().getString(R.string.status_bar_shut_down));
        }
    }

    /*add by csc*/
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastX = event.getRawX();
                mTranslationX = 0.0f;
                break;
            case MotionEvent.ACTION_MOVE:
                int dx = (int) (event.getRawX() - mLastX);
                if(shouldViscid()) {
//                    dx = dx / 5;
                }
                mTranslationX += dx;
                rootLayout.setX(rootLayout.getX()+dx);
//                mRightBottomScreenFl.setX(mRightBottomScreenFl.getX()+dx);
                mLastX = event.getRawX();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                int dx1 = (int) (event.getRawX() - mLastX);
                if(shouldViscid()) {
//                    dx1 = dx1 / 5;
                }

                mTranslationX += dx1;

                rootLayout.setX(rootLayout.getX() + dx1);
//                mRightBottomScreenFl.setX(mRightBottomScreenFl.getX() + dx1);
//                final float rootLayoutX = rootLayout.getX();
//                final float mRightBottomScreenFlX = mRightBottomScreenFl.getX();

//                if(/*(rootLayoutX > 0 && rootLayoutX < 720 /4) ||*/(rootLayoutX < 0 && (rootLayoutX+getWidth() /4) >= 0&&mTranslationX<0)||(rootLayoutX > 0&&mTranslationX>0)||(mRightBottomScreenFlX>=getWidth()/4&&mTranslationX>0)
//                        ){
//                    resetChallengeLayoutLocation();//1
//                    dianOneIv.setBackgroundResource(R.drawable.iphone_dian_bai);
//                    dianTwoIv.setBackgroundResource(R.drawable.iphone_dian_gray);

//                }else if(/*(rootLayoutX < 0 && (rootLayoutX+720 /3) <=0 && mTranslationX<0)||*/((mRightBottomScreenFlX<getWidth()/4)&&mTranslationX>0)||(mRightBottomScreenFlX<0&&mTranslationX<0)||(rootLayoutX+getWidth()/4<0&&mTranslationX<0)){
//                    resetPasswordLayoutLocation();//2
//                    dianOneIv.setBackgroundResource(R.drawable.iphone_dian_gray);
//                    dianTwoIv.setBackgroundResource(R.drawable.iphone_dian_bai);
//                }
                break;
        }

        return super.onTouchEvent(event);
    }

    private boolean shouldViscid() {
        if(rootLayout.getX() < 0 /*|| mRightBottomScreenFl.getX() < getWidth()*/) {//modified by xss from mKeyguardBouncerView to mLeftPreviewScreenView for ios10
            return true;
        }
        return false;
    }

    private void resetPasswordLayoutLocation() {
        ObjectAnimator resetChallengeLayoutAnim = createXAnimation(rootLayout, -getWidth());
        resetChallengeLayoutAnim.setDuration(500);
        resetChallengeLayoutAnim.setInterpolator(new LinearInterpolator());
        resetChallengeLayoutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
            }
        });
        resetChallengeLayoutAnim.start();

//        ObjectAnimator resetPwdLayoutAnim = createXAnimation(mRightBottomScreenFl, 0);//modified by xss from mKeyguardBouncerView to mLeftPreviewScreenView for ios10
 //       resetPwdLayoutAnim.setDuration(500);
//        resetPwdLayoutAnim.setInterpolator(new LinearInterpolator());
//        resetPwdLayoutAnim.start();
    }

    private void resetChallengeLayoutLocation() {
        ObjectAnimator resetChallengeLayoutAnim = createXAnimation(rootLayout, 0);
        resetChallengeLayoutAnim.setDuration(500);
        resetChallengeLayoutAnim.setInterpolator(new LinearInterpolator());
        resetChallengeLayoutAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

            }
        });
        resetChallengeLayoutAnim.start();
        //if(mKeyguardBouncer != null && mKeyguardBouncer.isSecure()) {//mod by lzp
//        ObjectAnimator resetKeyguardBouncerViewAnim = createXAnimation(mRightBottomScreenFl, getWidth());//modified by xss from mKeyguardBouncerView to mLeftPreviewScreenView for ios10
//        resetKeyguardBouncerViewAnim.setDuration(500);
//        resetKeyguardBouncerViewAnim.setInterpolator(new LinearInterpolator());
//        resetKeyguardBouncerViewAnim.start();
    }
    private ObjectAnimator createXAnimation(View v, float newPos) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(v,  "x", newPos);
        return anim;
    }
    

    public boolean isTitleLineStraight() {
		return mStraight;
	}


	//animate to show the title straight line
	public void animateCurveToStraightLine() {
		if(mStraight) {
			return;
		}
        ObjectAnimator curveAlphaAnim = createAlphaAnimation(mTitleCurveLineView, 1.0f, 0.0f);
        curveAlphaAnim.setDuration(TITLE_ANIMATION_TIME);
        curveAlphaAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				mStraight = true;
			}
		});
        curveAlphaAnim.start();
        
        ObjectAnimator straightAlphaAnim = createAlphaAnimation(mTitleStraightLineView, 0.0f, 1.0f);
        straightAlphaAnim.setDuration(TITLE_ANIMATION_TIME);
        straightAlphaAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				mStraight = true;
			}
		});
        straightAlphaAnim.start();
	}
	//animate to show the title curve line
	public void animateStraightToCurveLine() {
		if(!mStraight) {
			return;
		}
        ObjectAnimator straightAlphaAnim = createAlphaAnimation(mTitleStraightLineView, 1.0f, 0.0f);
        straightAlphaAnim.setDuration(TITLE_ANIMATION_TIME);
        straightAlphaAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				mStraight = false;
			}
		});
        straightAlphaAnim.start();
		
        ObjectAnimator curveAlphaAnim = createAlphaAnimation(mTitleCurveLineView, 0.0f, 1.0f);
        curveAlphaAnim.setDuration(TITLE_ANIMATION_TIME);
        curveAlphaAnim.addListener(new AnimatorListenerAdapter() {
			public void onAnimationEnd(Animator animation) {
				mStraight = false;
			}
		});
        curveAlphaAnim.start();
	}

    private ObjectAnimator createAlphaAnimation(View v, float oldAlpha, float newAlpha) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(v,  "alpha", oldAlpha, newAlpha);
        return anim;
    }
	
    private ObjectAnimator createTranslationYAnimation(View v, float newPos) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(v,  "y", newPos);
        return anim;
    }

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(seekBar == mBrightnessSeekBar) {
			setBrightness(progress + mScreenBrightnessMinimum);
			Log.d(TAG, "onProgressChanged mBrightnessSeekBar");
		} else if(seekBar == mMusicProgressSeekBar) {
			if (DEBUG) Log.d(TAG, "onProgressChanged mMusicProgressSeekBar progress="+progress);
			final long pr = mMetadata.duration * progress / 1000;
			if(fromUser) {
				if (DEBUG) Log.d(TAG, "=================onProgressChanged fromUser mTimeSeekBar progress="+progress);
				mSeekHandler.removeCallbacks(mTimeRunnable);
				mSeekHandler.postDelayed(mTimeRunnable, DRAG_INTERVAL);
			}
		} else if(seekBar == mVolumeSeekBar) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
			getContext().sendBroadcast(new Intent("action_volume_changed"));
			Log.d(TAG, "onProgressChanged mVolumeSeekBar");
		}
	}


	private Runnable mTimeRunnable = new Runnable() {
		public void run() {
			final long progress = mMetadata.duration * mMusicProgressSeekBar.getProgress() / 1000;
			Intent intent = new Intent(ACTION_SEEK_TO_POSITION);
			intent.putExtra(PLAYER_POSITION, progress);
			getContext().sendBroadcast(intent);
		}
	};


	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		if(seekBar == mBrightnessSeekBar) {
			Log.d(TAG, "onStartTrackingTouch mBrightnessSeekBar");
                
		} else if(seekBar == mMusicProgressSeekBar) {
			Log.d(TAG, "onStartTrackingTouch mMusicProgressSeekBar");
		} else if(seekBar == mVolumeSeekBar) {
			Log.d(TAG, "onStartTrackingTouch mVolumeSeekBar");
		}
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		if(seekBar == mBrightnessSeekBar) {
			Log.d(TAG, "onStopTrackingTouch mBrightnessSeekBar");
                             Settings.System.putInt(getContext()
					.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, seekBar.getProgress() + mScreenBrightnessMinimum);
		} else if(seekBar == mMusicProgressSeekBar) {
			Log.d(TAG, "onStopTrackingTouch mMusicProgressSeekBar");
		} else if(seekBar == mVolumeSeekBar) {
			Log.d(TAG, "onStopTrackingTouch mVolumeSeekBar");
		}
	}
	
    /**
     *Set the brightness of devices
     */
	private void setBrightness(int brightness) {
		try {
			IPowerManager power = IPowerManager.Stub.asInterface(ServiceManager
					.getService("power"));
			// Only set backlight value when screen is on
			if (power != null) {
				if (power.isScreenOn()) {
					power.setTemporaryScreenBrightnessSettingOverride(brightness);
				} else {
					power.setTemporaryScreenBrightnessSettingOverride(-1);
				}
			}
		} catch (RemoteException doe) {

		}
	}
	
    private void onBrightnessChanged() {
        Log.i(TAG, "onBrightnessChanged");
        int brightness = getBrightness(mScreenBrightnessMaximum);
        mBrightnessSeekBar.setProgress(brightness - mScreenBrightnessMinimum);
        mOldBrightness = brightness;
    }
	
    /** 
     * Get the current brightness value from data base
     * @param defaultValue 
     * @return current brightness in database
     */
	private int getBrightness(int defaultValue) {
		int brightness = defaultValue;
		try {
			brightness = Settings.System.getInt(getContext()
					.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
		} catch (SettingNotFoundException snfe) {
			Log.d(TAG, "SettingNotFoundException");
		}
		return brightness;
	}
	
    private ContentObserver mBrightnessObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            onBrightnessChanged();
        }
    };

//added by xujia 20130922

	public void openLight() {
	/*try{
		Camera camera = Camera.open();
		if(camera == null) {
	    	if(mFlashlightBtn != null) {
	        	mFlashlightBtn.setEnabled(false);
	        }
		} else {
	    	if(mFlashlightBtn != null) {
	        	mFlashlightBtn.setEnabled(true);
	        }
			camera.release();
			camera = null;
		}
	  }catch(Exception e)
	    {*/
	        if(mFlashlightBtn != null) {
	        	mFlashlightBtn.setEnabled(true);
	        }
	   // }
    }
public void releaseCamera()
{
    /*camera.release();
    camera=null;*/
    isLightOn=false;
}
    public void closeCamera()
        {

              
            mFlashlightBtn.setEnabled(false);
           mFlashlightBtn.setBackgroundResource(R.drawable.iphone_flashlight_black);      
        
        }
//end by xujia
    private void startSettingsActivity(String action) {
        Intent intent = new Intent(action);
        startSettingsActivity(intent);
    }

    private void startSettingsActivity(Intent intent) {
        startSettingsActivity(intent, true);
    }

    private void startSettingsActivity(Intent intent, boolean onlyProvisioned) {
		Log.d(TAG, "startSettingsActivity intent="+intent.toString());
        if (onlyProvisioned) return;
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivityAsUser(intent, new UserHandle(UserHandle.USER_CURRENT));
        //getService().animateCollapsePanels();
    }
	
	@Override
	public void onClick(View v) {
		Log.d("kay3-", "btl onClick v="+v);
    	final int id = v.getId();
    	switch(id) {
    	case R.id.flashlight_btn:
    	case R.id.timer_btn:
    	case R.id.calculator_btn:
    	case R.id.camera_btn: {
    		onClickAppControl(id);
    		break;
    	}
    	case R.id.music_backward_btn:
    	case R.id.music_play_btn:
    	case R.id.music_forward_btn: {
    		onClickMusicControl(id);
    		break;
    	}
    	case R.id.airplane_mode_iv: {
    		updateAirplaneView(true);
    		break;
    	}
    	case R.id.wifi_iv: {
			updateWifiView(true);
    		break;
    	}
    	case R.id.bluetooth_iv: {
    		mHandler.post(new BluetoothRunnable(true));
    		break;
    	}
    	case R.id.disturbance_iv: {
    		updateDisturbanceView(true);
    		break;
    	}
    	case R.id.rotation_iv: {
    		updateRotationView(true);
    		break;
    	}
        case R.id.airdrop_layout: {
                setAirdropBtnBg(mAirdropIsChecked); // add by scq
                break;
            }
        case R.id.airplay_layout: {
            if(!mAirplayIsChecked){
                mAirplayIsChecked = true;
            if(mAirplayBtn!=null)setAirplayBtnBg(mAirplayIsChecked); // add by scq
                }else{
                    mAirplayIsChecked = false;
                    if(mAirplayBtn!=null)setAirplayBtnBg(mAirplayIsChecked); // add by scq
                }
                break;
            }
//            case R.id.music_device:{
//                if(!isMusicDevice){
 //                   mMusicHeadstArrow.setImageResource(R.drawable.iphone_headset_up);
//                    mMusicDeviceList.setVisibility(View.VISIBLE);
//                    mMusicControl.setVisibility(View.VISIBLE);//add by scq
//                    mMusicDeviceAll.setVisibility(View.GONE);
//                    isMusicDevice = true;
//                }else{
//                    mMusicHeadstArrow.setImageResource(R.drawable.iphone_headset_down);
 //                   mMusicDeviceList.setVisibility(View.GONE);
//                    mMusicControl.setVisibility(View.GONE);//add by scq
//                    mMusicDeviceAll.setVisibility(View.VISIBLE);
//                    isMusicDevice = false;
//                }
//                break;
//            }
        }

	}

	private static final int MSG_SHOW_TITLE_TEXT = 0;

	private ObjectAnimator mShowTitleTextAnim, mHideTitleTextAnim, mShowCurveLineAnim;
	private final AnimatorSet mTitleAnimSet = new AnimatorSet();

	private Handler mTitleHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_SHOW_TITLE_TEXT:
                mTitleView.setText(msg.arg1);
				mTitleCurveLineView.setAlpha(0.f);

				showTitleViews();
                break;
            }
        }
	};

	private void showTitleViews() {
		mTitleAnimSet.cancel();
        mShowTitleTextAnim = createAlphaAnimation(mTitleView, 0.f, 1.f);
        mShowTitleTextAnim.setDuration(2000);

        mHideTitleTextAnim = createAlphaAnimation(mTitleView, 1.f, 0.f);
        mHideTitleTextAnim.setDuration(2000);

        mShowCurveLineAnim = createAlphaAnimation(mTitleCurveLineView, 0.f, 1.f);
        mShowCurveLineAnim.setDuration(TITLE_ANIMATION_TIME);

        mTitleAnimSet.play(mShowTitleTextAnim).before(mHideTitleTextAnim);
        mTitleAnimSet.play(mShowCurveLineAnim).after(mHideTitleTextAnim);

		mTitleAnimSet.start();
	}

	private Runnable updateRunnable = new Runnable() {
		public void run() {
			updateAirplaneView(false);
			updateWifiView(false);
			mHandler.post(new BluetoothRunnable(false));
			updateDisturbanceView(false);
			updateRotationView(false);
		}
	};


	private void updateWifiView(boolean fromUser) {
		Log.d(TAG, "updateWifiView");
        WifiManager mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager == null) {
            Log.d(TAG, "updateWifiView No wifiManager.");
            return;
        }
		
       	final int wifiApState = mWifiManager.getWifiApState();
        if (fromUser && ((wifiApState == WifiManager.WIFI_AP_STATE_ENABLING)
           	|| (wifiApState == WifiManager.WIFI_AP_STATE_ENABLED))) {
           	mWifiManager.setWifiApEnabled(null, false);
        }
		final int wifiState = mWifiManager.getWifiState();
		Log.d(TAG, "updateWifiView wifiApState="+wifiApState + " wasItTheMonkey()="+wasItTheMonkey()+ "  fromUser="+fromUser+" wifiState="+wifiState);
		if (!wasItTheMonkey() && fromUser) {
			if(wifiState == WifiManager.WIFI_STATE_DISABLED) {
				mWifiManager.setWifiEnabled(true);
				final int arg1 = R.string.wifi_on;
				mTitleHandler.obtainMessage(MSG_SHOW_TITLE_TEXT, arg1, 0).sendToTarget();
			} else if(wifiState == WifiManager.WIFI_STATE_ENABLED) {
				mWifiManager.setWifiEnabled(false);
				final int arg1 = R.string.wifi_off;
				mTitleHandler.obtainMessage(MSG_SHOW_TITLE_TEXT, arg1, 0).sendToTarget();
			} else {//WIFI_STATE_DISABLING, WIFI_STATE_ENABLING, WIFI_STATE_UNKNOWN
				mWifiBtn.setEnabled(false);
			}
        }

		if(wifiState == WifiManager.WIFI_STATE_ENABLED) {
			mWifiBtn.setEnabled(true);
			mWifiBtn.setImageResource(R.drawable.iphone_wifi_white);
		} else if(wifiState == WifiManager.WIFI_STATE_DISABLED) {
			mWifiBtn.setEnabled(true);
			mWifiBtn.setImageResource(R.drawable.iphone_wifi_black);
		} else {//WIFI_STATE_DISABLING, WIFI_STATE_ENABLING, WIFI_STATE_UNKNOWN
			//mWifiBtn.setEnabled(false);
		}
	}

    private boolean wasItTheMonkey() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        if (activityManager.isUserAMonkey()) {
              Log.d(TAG,"it was the monkey");
              return true;
        }
        Log.d(TAG,"it was an user");
        return false;
    }

	private void updateAirplaneView(boolean fromUser) {
		if (Boolean.parseBoolean(SystemProperties.get(TelephonyProperties.PROPERTY_INECM_MODE))) {
            /// M: Launch ECM exit dialog
            Intent ecmDialogIntent = new Intent(TelephonyIntents.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS, null);
            ecmDialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(ecmDialogIntent);
        } else {
            boolean airlineMode = isAirplaneModeOn(mContext);

            Log.d(TAG, "Airplane toogleState: " + isClickable() + ", current airlineMode is " + airlineMode);
			if(fromUser) {
				mAirplaneBtn.setImageResource(airlineMode ? R.drawable.iphone_airplane_mode_black : R.drawable.iphone_airplane_mode_white);
	            Settings.Global.putInt(mContext.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON,
	                    airlineMode ? 0 : 1);
	            Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
	            intent.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
	            intent.putExtra("state", !airlineMode);
	            mContext.sendBroadcast(intent);

				final int arg1 = (!airlineMode) ? R.string.airplane_mode_on : R.string.airplane_mode_off;
				mTitleHandler.obtainMessage(MSG_SHOW_TITLE_TEXT, arg1, 0).sendToTarget();
			} else {
				mAirplaneBtn.setImageResource(airlineMode ? R.drawable.iphone_airplane_mode_white : R.drawable.iphone_airplane_mode_black);
			}
        }
	}

    public static boolean isAirplaneModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }



	class BluetoothRunnable implements Runnable {
		boolean mFromUser = false;
		public BluetoothRunnable(boolean fromUser) {
			mFromUser = fromUser;
		}
		public void run() {
			final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        if (bluetoothAdapter == null) {
	            return;
	        }
			final int state = bluetoothAdapter.getState();
			if (state == BluetoothAdapter.STATE_OFF) {
				mBluetoothBtn.setEnabled(true);
				
				if(mFromUser) {
					updateBluetoothView(true);
	        		bluetoothAdapter.enable();
					final int arg1 = R.string.bluetooth_on;
					mTitleHandler.obtainMessage(MSG_SHOW_TITLE_TEXT, arg1, 0).sendToTarget();
				} else {
					updateBluetoothView(false);
				}
	        } else if(state == BluetoothAdapter.STATE_ON) {
				mBluetoothBtn.setEnabled(true);
				
				if(mFromUser) {
					updateBluetoothView(false);
	        		bluetoothAdapter.disable();
					final int arg1 = R.string.bluetooth_off;
					mTitleHandler.obtainMessage(MSG_SHOW_TITLE_TEXT, arg1, 0).sendToTarget();
				} else {
					updateBluetoothView(true);
				}
	        } else {//BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_TURNING_ON
	        	mBluetoothBtn.setEnabled(false);
	        }
		}
	}

	private void updateBluetoothView(boolean bluetoothOn) {
		mBluetoothBtn.setImageResource(bluetoothOn ? R.drawable.iphone_bluetooth_white : R.drawable.iphone_bluetooth_black);
	}
	
	private void updateDisturbanceView(boolean fromUser) {
		final boolean disturbOn = Settings.System.getInt(mContext.getContentResolver(), "disturbanceMode", 0) == 1;
		if(fromUser) {
			Settings.System.putInt(mContext.getContentResolver(), "disturbanceMode", !disturbOn ? 1 : 0);
			//updateDisturbanceView(!disturbOn);
			mDisturbanceBtn.setImageResource(disturbOn ? R.drawable.iphone_disturbance_black : R.drawable.iphone_disturbance_white);
			Intent intent = new Intent(ACTION_DISTURBANCE_MODE_CHANGED);
	       	intent.putExtra("state", !disturbOn);
	       	mContext.sendBroadcast(intent);

			final int arg1 = (!disturbOn) ? R.string.do_not_disturb_on : R.string.do_not_disturb_off;
			mTitleHandler.obtainMessage(MSG_SHOW_TITLE_TEXT, arg1, 0).sendToTarget();
		} else {
			mDisturbanceBtn.setImageResource(disturbOn ? R.drawable.iphone_disturbance_white : R.drawable.iphone_disturbance_black);
		}
	}

	   //modified by xujia for bug[2349]
	private void updateRotationView(boolean fromUser) {
		boolean locked = Settings.System.getInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, -1) == 0;
		if(fromUser) {
			Settings.System.putInt(mContext.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, locked ? 1 : 0);
			mRotationBtn.setImageResource(locked ? R.drawable.iphone_rotation_white_bigger : R.drawable.iphone_rotation_black_bigger);
			//mStatusBar.updateRotationStatus(!rotationOn);

			final int arg1 = (!locked) ? R.string.portrait_orientation_lock_on : R.string.portrait_orientation_lock_off;
			mTitleHandler.obtainMessage(MSG_SHOW_TITLE_TEXT, arg1, 0).sendToTarget();			
		} else {
			mRotationBtn.setImageResource(locked ? R.drawable.iphone_rotation_white_bigger : R.drawable.iphone_rotation_black_bigger);
			//mStatusBar.updateRotationStatus(!rotationOn);
		}
	}
//end by xujia for bug[2349]

    private ContentObserver mAutoRotationChangeObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            //to process rotation
            updateRotationView(false);
        }
    };



    /**
     * M: Used to check weather this device is wifi only.
     */
    private boolean isWifiOnlyDevice() {
      ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
      return  !(cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE));
    }

    private BroadcastReceiver mLocaleReceiver=new BroadcastReceiver()
        {
        @Override
            public void onReceive(Context context,final Intent intent)
            {	
				mHandler.post(new Runnable(){//mod by lzp
					public void run(){
                		String action = intent.getAction();
                   		if(Intent.ACTION_LOCALE_CHANGED.equals(action))
                        {
                        if(airDrop!=null)
                            airDrop.setText(R.string.airdrop_everyone);
                        }
                 	}
				});
            }
        };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (DEBUG) {
                Log.d(TAG, "onReceive called, action is " + action);
            }
			mUpdateViewHandler.obtainMessage(0, intent).sendToTarget();
/*            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                updateWifiView(false);
            } else if (action.equals(ACTION_DISTURBANCE_MODE_CHANGED)) {
				updateDisturbanceView(false);
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				mHandler.post(new BluetoothRunnable(false));
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            	updateAirplaneView(false);
                boolean enabled = intent.getBooleanExtra("state", false);
                if (DEBUG) {
                    Log.d(TAG, "airline mode changed: state is " + enabled);
                }
                // only apply if NOT wifi-only device @{
                if (!isWifiOnlyDevice()) {
                    //Set icon directly while turning off the airplane mode
                    if (!enabled) {
                        Intent mAirlineintent = new Intent();
                        mAirlineintent.putExtra("state", enabled);
                        //mAirlineModeStateTracker.onActualStateChange(mContext, mAirlineintent);
                        //mAirlineModeStateTracker.setImageViewResources(mContext);
                    }
                }

                if (isWifiOnlyDevice()) {
                  Intent intent2 = new Intent();
                  intent2.putExtra("state", enabled);
                  //mAirlineModeStateTracker.onActualStateChange(mContext, intent2);
                  //mAirlineModeStateTracker.setImageViewResources(mContext);
                }
            } else if (action.equals(IPO_BOOT)) {
                Log.d(TAG, "IPO Boot: initConfigurationState()");
                //initConfigurationState();
            } else if (action.equals(Intent.ACTION_WALLPAPER_CHANGED)) {
                Log.d(TAG, "Intent.ACTION_WALLPAPER_CHANGED");
                changedBackground(false, true);
            } else if("action_volume_changed".equals(action)) {
            	updateVolumeControlProgressHandler.sendEmptyMessage(0);
            }*/
        }
    };



	private Handler mUpdateViewHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Intent intent = (Intent) msg.obj;
			String action = intent.getAction();
            if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                updateWifiView(false);
            } else if (action.equals(ACTION_DISTURBANCE_MODE_CHANGED)) {
				updateDisturbanceView(false);
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				mHandler.post(new BluetoothRunnable(false));
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            	updateAirplaneView(false);
                boolean enabled = intent.getBooleanExtra("state", false);
                if (DEBUG) {
                    Log.d(TAG, "airline mode changed: state is " + enabled);
                }
                // only apply if NOT wifi-only device @{
                if (!isWifiOnlyDevice()) {
                    //Set icon directly while turning off the airplane mode
                    if (!enabled) {
                        Intent mAirlineintent = new Intent();
                        mAirlineintent.putExtra("state", enabled);
                        //mAirlineModeStateTracker.onActualStateChange(mContext, mAirlineintent);
                        //mAirlineModeStateTracker.setImageViewResources(mContext);
                    }
                }

                if (isWifiOnlyDevice()) {
                  Intent intent2 = new Intent();
                  intent2.putExtra("state", enabled);
                  //mAirlineModeStateTracker.onActualStateChange(mContext, intent2);
                  //mAirlineModeStateTracker.setImageViewResources(mContext);
                }
            } else if (action.equals(IPO_BOOT)) {
                Log.d(TAG, "IPO Boot: initConfigurationState()");
                //initConfigurationState();
            } else if (action.equals(Intent.ACTION_WALLPAPER_CHANGED)) {
                Log.d(TAG, "Intent.ACTION_WALLPAPER_CHANGED");
                changedBackground(false, true);
            } else if("action_volume_changed".equals(action)) {
            	updateVolumeControlProgressHandler.sendEmptyMessage(0);
            }
		}
	};


  /*begin added by xujia for FlashLight*/
     private BroadcastReceiver mFlashLightReceiver=new BroadcastReceiver()
        {
        @Override
            public void onReceive(Context context,final Intent intent)
            {
				mHandler.post(new Runnable(){//mod by lzp
					public void run(){
						String action = intent.getAction();
				
                      	Log.d("xujia&&&","action=="+action);
		                if("flash_light_off".equals(action)){
		                    
		                    mFlashlightBtn.setBackgroundResource(R.drawable.iphone_flashlight_black);
		                 }else if("flash_light_on".equals(action)){
		                       
		                     mFlashlightBtn.setBackgroundResource(R.drawable.iphone_flashlight_white);
		                 }else if("enable_flash_light".equals(action)){
		                      openLight();
		                 }
					}
				});
            }
        };

    /*end by xujia for FlashLight*/





    private Handler updateVolumeControlProgressHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (DEBUG) {
				Log.d(TAG, "updateVolumeControlProgressHandler handleMessage msg.what " + msg.what);
			}
			int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	        if (currentVolume > maxVolume) {
	        	currentVolume = maxVolume;
	        }
	        mVolumeSeekBar.setProgress(currentVolume);
		}
    	
    };



	private void register() {
		//for brightness
        mContext.getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS), true,
                mBrightnessObserver);
		
		//for rotation
		mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION),
			true, mAutoRotationChangeObserver);

		//for wifi, bluetooth, airplane mode
		IntentFilter filter = new IntentFilter();
        if (FeatureOption.MTK_WLAN_SUPPORT) {
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        }
        if (FeatureOption.MTK_BT_SUPPORT) {
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        }
        filter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		filter.addAction(ACTION_DISTURBANCE_MODE_CHANGED);
        filter.addAction(IPO_BOOT);
		filter.addAction(Intent.ACTION_WALLPAPER_CHANGED);
		filter.addAction("action_volume_changed");
        IntentFilter localeFilter=new IntentFilter();
             localeFilter.addAction(Intent.ACTION_LOCALE_CHANGED);
		mContext.registerReceiver(mIntentReceiver, filter);
                   mContext.registerReceiver(mLocaleReceiver,localeFilter);

         /*added by xujia for FlashLight*/
         IntentFilter flashFilter=new IntentFilter();
         flashFilter.addAction("flash_light_on");
         flashFilter.addAction("flash_light_off");
         flashFilter.addAction("enable_flash_light");
         mContext.registerReceiver(mFlashLightReceiver,flashFilter);
         /*end by xujia for FlashLight*/
		 
          /*Begin:added by xss  for blur*/
         IntentFilter wallpaperFilter=new IntentFilter();
         wallpaperFilter.addAction(Intent.ACTION_HOMESCREEN_WALLPAPER_CASHE_FINISHED);
         wallpaperFilter.addAction("set_live_wallpaper_cenon"); 	
	  //mContext.registerReceiver(mWallpaperBg,wallpaperFilter);//del by lzp for oom
		 
	  /*End:added by xss for blur*/
		 
	}

	private void unregister() {
		//for brightness
		mContext.getContentResolver().unregisterContentObserver(mBrightnessObserver);
		//for rotation
		mContext.getContentResolver().unregisterContentObserver(mAutoRotationChangeObserver);
		//for wifi, bluetooth, airplane mode
		mContext.unregisterReceiver(mIntentReceiver);
              mContext.unregisterReceiver(mLocaleReceiver);
              mContext.unregisterReceiver(mFlashLightReceiver);//added by xujia 20131014
              //mContext.unregisterReceiver(mWallpaperBg);//added by xss for blur // del by lzp 
	}

    private void onClickAppControl(int id) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("launch_from_control_center", true);

        final Intent closeFlahLight= new Intent("closeFlashLight");
        final Intent openFlahLight= new Intent("openFlashLight");
		if(mKeyguardManager.isKeyguardSecure()) {
			intent.putExtra("is_keyguard_secure", true);
		}
         Log.d("xujia&&&", "mFlashlightBtn12"+mFlashlightBtn.isEnabled());
	 /*Begin:added by xss for back to last app*/ 
        Intent notifipanelIsClickIntent=new Intent("notificationContentView_is_click");
	 notifipanelIsClickIntent.putExtra("is_click",true);
	 /*End:added by xss for back to last app*/ 	 
        if (id == R.id.flashlight_btn) {
           isLightOn=(Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.FLASH_LIGHT, -1)==1);

            Log.d("xujia&&&","isLightOn"+isLightOn);
            //add by xujia 20130922
            if (isLightOn) {
            	getContext().sendBroadcast(closeFlahLight);   
                mFlashlightBtn.setBackgroundResource(R.drawable.iphone_flashlight_black);
            } else {
                getContext().sendBroadcast(openFlahLight); 
                mFlashlightBtn.setBackgroundResource(R.drawable.iphone_flashlight_white);
		    }
//end by xujia 20130922
            
        } else if (id == R.id.timer_btn) {
            getContext().sendBroadcast(notifipanelIsClickIntent);//added by xss for back to last app 
            intent.setComponent(new ComponentName("com.android.deskclock", "com.android.deskclock.DeskClockGroupActivity"));
        } else if (id == R.id.calculator_btn) {
            getContext().sendBroadcast(notifipanelIsClickIntent);//added by xss for back to last app 
            intent.setComponent(new ComponentName("com.hskj.iphonecalculator", "com.hskj.iphonecalculator.Calculator"));
        } else if (id == R.id.camera_btn) {
            if (isLightOn) {
                mFlashlightBtn.setEnabled(false);
                getContext().sendBroadcast(closeFlahLight);     
            }
            intent.setComponent(new ComponentName("com.android.gallery3d", "com.android.camera.CameraLauncher"));
        }
		Log.d(TAG, "onClickAppControl intent="+intent.toString());
		if(mCallback != null && id != R.id.flashlight_btn) {
			mCallback.closeControlCenter();
		}
        if(id != R.id.flashlight_btn) {
            try {
            	getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
            	Toast.makeText(getContext(), "not found target...", Toast.LENGTH_LONG).show();
            	e.printStackTrace();
            }
        }
    }

    private void onClickMusicControl(int id) {
        int keyCode = -1;
        if (id == R.id.music_backward_btn) {
            keyCode = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
        } else if (id == R.id.music_forward_btn) {
            keyCode = KeyEvent.KEYCODE_MEDIA_NEXT;
        } else if (id == R.id.music_play_btn) {
            keyCode = KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;

        }
        if (keyCode != -1) {
            sendMediaButtonClick(keyCode);
        }
    }



    private static final int MSG_UPDATE_STATE = 100;
    private static final int MSG_SET_METADATA = 101;
    private static final int MSG_SET_TRANSPORT_CONTROLS = 102;
    private static final int MSG_SET_ARTWORK = 103;
    private static final int MSG_SET_GENERATION_ID = 104;
    private static final int MSG_SET_POSITION = 105;

    private int mClientGeneration;
    private Metadata mMetadata = new Metadata();
	private int mCurrentPlayState;
	


    // This handler is required to ensure messages from IRCD are handled in sequence and on
    // the UI thread.
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MSG_UPDATE_STATE:
                if (mClientGeneration == msg.arg1) updatePlayPauseState(msg.arg2);
                break;

            case MSG_SET_METADATA:
                if (mClientGeneration == msg.arg1) updateMetadata((Bundle) msg.obj);
                break;

            case MSG_SET_TRANSPORT_CONTROLS:
                if (mClientGeneration == msg.arg1) updateTransportControls(msg.arg2);
                break;

            case MSG_SET_ARTWORK:
                if (mClientGeneration == msg.arg1) {
                    if (mMetadata.bitmap != null) {
                        mMetadata.bitmap.recycle();
                    }
                    mMetadata.bitmap = (Bitmap) msg.obj;
                    //mAlbumArt.setImageBitmap(mMetadata.bitmap);
                }
                break;

            case MSG_SET_GENERATION_ID:
                if (DEBUG) Log.v(TAG, "New genId = " + msg.arg1 + ", clearing = " + msg.arg2);
                mClientGeneration = msg.arg1;
                mClientIntent = (PendingIntent) msg.obj;
                break;
            case MSG_SET_POSITION:
                refreshNow(msg.arg1);
                break;
            }
        }
    };


    /**
     * This class is required to have weak linkage to the current TransportControlView
     * because the remote process can hold a strong reference to this binder object and
     * we can't predict when it will be GC'd in the remote process. Without this code, it
     * would allow a heavyweight object to be held on this side of the binder when there's
     * no requirement to run a GC on the other side.
     */
    private static class IRemoteControlDisplayWeak extends IRemoteControlDisplay.Stub {
        private WeakReference<Handler> mLocalHandler;

        IRemoteControlDisplayWeak(Handler handler) {
            mLocalHandler = new WeakReference<Handler>(handler);
        }

        public void setPlaybackState(int generationId, int state, long stateChangeTimeMs) {
            Handler handler = mLocalHandler.get();
            if (handler != null) {
                handler.obtainMessage(MSG_UPDATE_STATE, generationId, state).sendToTarget();
            }
        }

        public void setMetadata(int generationId, Bundle metadata) {
            Handler handler = mLocalHandler.get();
            if (handler != null) {
                handler.obtainMessage(MSG_SET_METADATA, generationId, 0, metadata).sendToTarget();
            }
        }

        public void setTransportControlFlags(int generationId, int flags) {
            Handler handler = mLocalHandler.get();
            if (handler != null) {
                handler.obtainMessage(MSG_SET_TRANSPORT_CONTROLS, generationId, flags)
                        .sendToTarget();
            }
        }

        public void setArtwork(int generationId, Bitmap bitmap) {
            Handler handler = mLocalHandler.get();
            if (handler != null) {
                handler.obtainMessage(MSG_SET_ARTWORK, generationId, 0, bitmap).sendToTarget();
            }
        }

        public void setAllMetadata(int generationId, Bundle metadata, Bitmap bitmap) {
            Handler handler = mLocalHandler.get();
            if (handler != null) {
                handler.obtainMessage(MSG_SET_METADATA, generationId, 0, metadata).sendToTarget();
                handler.obtainMessage(MSG_SET_ARTWORK, generationId, 0, bitmap).sendToTarget();
            }
        }

        public void setCurrentClientId(int clientGeneration, PendingIntent mediaIntent,
                boolean clearing) throws RemoteException {
            Log.d(TAG, "setCurrentClientId clientGeneration="+clientGeneration);
            Handler handler = mLocalHandler.get();
            if (handler != null) {
                handler.obtainMessage(MSG_SET_GENERATION_ID,
                    clientGeneration, (clearing ? 1 : 0), mediaIntent).sendToTarget();
            }
        }

		public void setPosition(int position) {
			if (DEBUG) Log.d(TAG, "setPosition position="+position);
			//refreshNow(position);
            Handler handler = mLocalHandler.get();
            if (handler != null) {
                handler.obtainMessage(MSG_SET_POSITION, position, 0).sendToTarget();
            }
		}
		public void seekTo(int position) {
			
		}

    };

    private void updatePlayPauseState(int state) {
        if (DEBUG) Log.v(TAG,
                "updatePlayPauseState(), old=" + mCurrentPlayState + ", state=" + state);
        if (state == mCurrentPlayState) {
            return;
        }
        final int imageResId;
        final int imageDescId;
        switch (state) {
            case RemoteControlClient.PLAYSTATE_ERROR:
                imageResId = com.android.internal.R.drawable.stat_sys_warning;
                // TODO use more specific image description string for warning, but here the "play"
                //      message is still valid because this button triggers a play command.
                imageDescId = com.android.internal.R.string.lockscreen_transport_play_description;
                break;

            case RemoteControlClient.PLAYSTATE_PLAYING:
                imageResId = R.drawable.iphone_music_pause;
                imageDescId = com.android.internal.R.string.lockscreen_transport_pause_description;
                break;

            case RemoteControlClient.PLAYSTATE_BUFFERING:
                imageResId = R.drawable.iphone_music_pause;
                imageDescId = com.android.internal.R.string.lockscreen_transport_stop_description;
                break;

            case RemoteControlClient.PLAYSTATE_PAUSED:
            default:
                imageResId = R.drawable.iphone_music_play;
                imageDescId = com.android.internal.R.string.lockscreen_transport_play_description;
                break;
        }
        mBtnPlay.setImageResource(imageResId);
        mBtnPlay.setContentDescription(getResources().getString(imageDescId));
		mNoMusicLayout.setVisibility(state == 0 ? View.VISIBLE : View.GONE);
		mMusicProgressLayout.setVisibility(state == 0 ? View.GONE : View.VISIBLE);
        mCurrentPlayState = state;
    }


    class Metadata {
        private String artist;
        private String trackTitle;
        private String albumTitle;
        private Bitmap bitmap;
		private long duration;

        public String toString() {
            return "Metadata[artist=" + artist + " trackTitle=" + trackTitle + " albumTitle=" + albumTitle + " duration=" + duration + "]";
        }
    }


    private String getMdString(Bundle data, int id) {
        return data.getString(Integer.toString(id));
    }

    private long getMdLong(Bundle data, int id) {
        return data.getLong(Integer.toString(id));
    }

    private void updateMetadata(Bundle data) {
	/**Begin: added by lzp **/
	if(data == null) {//added by lzp
	    mMetadata.artist = "";
            mMetadata.trackTitle = "";
            mMetadata.albumTitle =  "";
	    mMetadata.duration = 0;
	    mTransportControlFlags &=  RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS;
	    mTransportControlFlags &=  RemoteControlClient.FLAG_KEY_MEDIA_NEXT;	
	    mTransportControlFlags &= ( RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_STOP);
        if(mMusicProgressSeekBar != null)
	   mMusicProgressSeekBar.getThumb().setAlpha(0);//added by lzp for bug [3017]
	     mPlayingTime.setText("--:--");
	    mLeftTime.setText("--:--");
			//Log.d(TAG, "updateMetadata duration="+duration);
            populateMetadata();
	    return;
	}
		/**End: added by lzp **/
        if (mAttached) {
            mMetadata.artist = getMdString(data, MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
            mMetadata.trackTitle = getMdString(data, MediaMetadataRetriever.METADATA_KEY_TITLE);
            mMetadata.albumTitle = getMdString(data, MediaMetadataRetriever.METADATA_KEY_ALBUM);
			mMetadata.duration = getMdLong(data, MediaMetadataRetriever.METADATA_KEY_DURATION);
			//Log.d(TAG, "updateMetadata duration="+duration);
            populateMetadata();
        } else {
            mPopulateMetadataWhenAttached = data;
        }
    }
	
    private void updateTransportControls(int transportControlFlags) {
        mTransportControlFlags = transportControlFlags;
    }


    /**
     * Populates the given metadata into the view
     */
    private void populateMetadata() {
        StringBuilder sb = new StringBuilder();
        int trackTitleLength = 0;
        if (!TextUtils.isEmpty(mMetadata.artist)) {
            sb.append(mMetadata.artist);
			trackTitleLength = mMetadata.artist.length();
        }
        if (!TextUtils.isEmpty(mMetadata.albumTitle)) {
            if (sb.length() != 0) {
                sb.append(" - ");
            }
            sb.append(mMetadata.albumTitle);
        }
		mTrackArtist.setText(sb.toString());
		mTrackTitle.setText(mMetadata.trackTitle);
        if(mMusicProgressSeekBar != null)
		mMusicProgressSeekBar.setMax(1000);
		/*if (DEBUG) Log.d(TAG, "populateMetadata mMusicProgressSeekBar.getMax()"+mMusicProgressSeekBar.getMax());
		if (DEBUG) Log.d(TAG, "populateMetadata mMetadata="+mMetadata.toString());*/

        //mAlbumArt.setImageBitmap(mMetadata.bitmap);
        final int flags = mTransportControlFlags;
        /*setVisibilityBasedOnFlag(mBtnPrev, flags, RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS);
        setVisibilityBasedOnFlag(mBtnNext, flags, RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        setVisibilityBasedOnFlag(mBtnPlay, flags,
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                | RemoteControlClient.FLAG_KEY_MEDIA_STOP);*/

        updatePlayPauseState(mCurrentPlayState);
    }

    private static void setVisibilityBasedOnFlag(View view, int flags, int flag) {
        if ((flags & flag) != 0) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    static class SavedState extends BaseSavedState {
        boolean clientPresent;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.clientPresent = in.readInt() != 0;
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(this.clientPresent ? 1 : 0);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
    }



    private void sendMediaButtonClick(int keyCode) {
        if (mClientIntent == null) {
            // Shouldn't be possible because this view should be hidden in this case.
            Log.e(TAG, "sendMediaButtonClick(): No client is currently registered");
            return;
        }
        // use the registered PendingIntent that will be processed by the registered
        //    media button event receiver, which is the component of mClientIntent
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        try {
            mClientIntent.send(getContext(), 0, intent);
        } catch (CanceledException e) {
            Log.e(TAG, "Error sending intent for media button down: "+e);
            e.printStackTrace();
        }

        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        try {
            mClientIntent.send(getContext(), 0, intent);
        } catch (CanceledException e) {
            Log.e(TAG, "Error sending intent for media button up: "+e);
            e.printStackTrace();
        }
    }


    private boolean wasPlayingRecently(int state, long stateChangeTimeMs) {
        switch (state) {
            case RemoteControlClient.PLAYSTATE_PLAYING:
            case RemoteControlClient.PLAYSTATE_FAST_FORWARDING:
            case RemoteControlClient.PLAYSTATE_REWINDING:
            case RemoteControlClient.PLAYSTATE_SKIPPING_FORWARDS:
            case RemoteControlClient.PLAYSTATE_SKIPPING_BACKWARDS:
            case RemoteControlClient.PLAYSTATE_BUFFERING:
                // actively playing or about to play
                return true;
            case RemoteControlClient.PLAYSTATE_NONE:
                return false;
            case RemoteControlClient.PLAYSTATE_STOPPED:
            case RemoteControlClient.PLAYSTATE_PAUSED:
            case RemoteControlClient.PLAYSTATE_ERROR:
                // we have stopped playing, check how long ago
                if (DEBUG) {
                    if ((SystemClock.elapsedRealtime() - stateChangeTimeMs) < DISPLAY_TIMEOUT_MS) {
                        Log.v(TAG, "wasPlayingRecently: time < TIMEOUT was playing recently");
                    } else {
                        Log.v(TAG, "wasPlayingRecently: time > TIMEOUT");
                    }
                }
                return ((SystemClock.elapsedRealtime() - stateChangeTimeMs) < DISPLAY_TIMEOUT_MS);
            default:
                Log.e(TAG, "Unknown playback state " + state + " in wasPlayingRecently()");
                return false;
        }
    }
	


    private void refreshNow(int position) {
    	long pos = (long) position;
        long remaining = 1000 - (pos % 1000);
        if ((pos >= 0) && (mMetadata.duration > 0)) {
          	String currentTime = makeTimeString(getContext(), pos / 1000);
            mPlayingTime.setText(currentTime);

            mPlayingTime.setVisibility(View.VISIBLE);
            if(mMusicProgressSeekBar != null)mMusicProgressSeekBar.getThumb().setAlpha(255);//added by lzp for bug [3017]
            if(mMusicProgressSeekBar != null)mMusicProgressSeekBar.setProgress((int) (1000 * pos / mMetadata.duration));
        } else {
            mPlayingTime.setText("--:--");
            if(mMusicProgressSeekBar != null)mMusicProgressSeekBar.setProgress(1000);
        }

        mLeftTime.setText("-" + makeTimeString(getContext(), (mMetadata.duration - pos + 500) / 1000));
    }

    private StringBuilder sFormatBuilder = new StringBuilder();
    private Formatter sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
    private final Object[] sTimeArgs = new Object[5];
    public String makeTimeString(Context context, long secs) {
		
        String durationformat = context.getString(
                secs < 3600 ? com.android.internal.R.string.durationformatshort : com.android.internal.R.string.durationformatlong);
        
        // Provide multiple arguments so the format can be changed easily
        // by modifying the xml.
        //
        sFormatBuilder.setLength(0);

        final Object[] timeArgs = sTimeArgs;
        timeArgs[0] = secs / 3600;
        timeArgs[1] = secs / 60;
        timeArgs[2] = (secs / 60) % 60;
        timeArgs[3] = secs;
        timeArgs[4] = secs % 60;
        /// M: use local format
        return sFormatter.format(Locale.getDefault(), durationformat, timeArgs).toString();
		
		//return secs + "";
    }
	/**Begin: added by lzp **/
    public void clear(){
		//mHandler = null;
		//mSeekHandler = null;
		mWallpaperManager = null;
		mKeyguardManager = null;
		//mSeekHandler = null;
		mCallback = null;
    }
	/**End: added by lzp **/

      /*Begin:add by xss for force touch */
	private Context mContext;
	private View mBlurActivityBgView=null;
	private View mBlurWallpaperBgView=null;
	private Drawable mMusicBlurWallpaperBg;
	private Drawable mBlurWallpaperBg;
    private RoundRectImageView mRoundRectImageView;
	
	private void setForceTouchBlurBg(Bitmap blurbg){
	        Intent it=new Intent();
		 it.setAction("set_force_touch_blur_bg");
		 it.putExtra("ForceTouchBlurBg",getBytes(blurbg));
                //it.putExtra("BottomPanelBG",getBytes(bottomBG.getBitmap()));// add by csc   //del by xss for ios10 forceTouch
		 getContext().sendBroadcast(it);	  
	}
 	private  byte[] getBytes(Bitmap bitmap){  
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	    	bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
	    	return baos.toByteArray();
     	}
       //final Bitmap myBKG;//del by xss 
	private BitmapDrawable bottomBG;
	private BitmapDrawable statusBarBlurBg;//modified by xss for ios10 StausBarBlurBg
	 private void forceTouchBlurBg(Bitmap bkg) {
	 	//if(DEBUG)Log.d(TAG, "blur() xss bkg="+bkg+"   view="+view);
	        final Bitmap myBKG=bkg;//modified by xss
		 new AsyncTask<Void, Void, Bitmap>(){//added by xss 
			@Override
			//main non-UI thread
			 protected Bitmap doInBackground(Void... values){
			      long startMs = System.currentTimeMillis();
			       float scaleFactor = FeatureOption.CENON_HD?15:10;//modified by xss from 10 for ios10
	                     float radius = 5f;//modified by xss from 2.25f for ios10
				Configuration mConfiguration = mContext.getResources().getConfiguration(); //获取设置的配置信息
				int ori = mConfiguration.orientation ; //获取屏幕方向
                             int width=0,height=0;
				if(ori == mConfiguration.ORIENTATION_LANDSCAPE){
				//横屏
				     width=FeatureOption.CENON_HD?1280:1920;
				     height=FeatureOption.CENON_HD?720:1080;
					/*width = width/2;
				       height = height/2;*/
				}else if(ori == mConfiguration.ORIENTATION_PORTRAIT){
				//竖屏
				     width=FeatureOption.CENON_HD?720:1080;
				     height=FeatureOption.CENON_HD?1280:1920;
				}			 
				
                              if(DEBUG)Log.d("xssblur", "forceTouchBlurBg() xss  myBKG.getWidth()="+myBKG.getWidth()+"   myBKG.getHeight()="+myBKG.getHeight());
				 //if(DEBUG)Log.d("xssblur", "blur() xss  mView.getMeasuredWidth()="+mView.getMeasuredWidth()+"   mView.getMeasuredHeight()="+mView.getMeasuredHeight());		
			        Bitmap overlay = Bitmap.createBitmap((int) (width/ scaleFactor),
			                (int) ( height/ scaleFactor), Bitmap.Config.ARGB_8888);
		               if(DEBUG)Log.d("xssblur", "forceTouchBlurBg() xss  overlay.getWidth()="+overlay.getWidth()+"   overlay.getHeight()="+overlay.getHeight());
			        Canvas canvas = new Canvas(overlay);

			        //canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
			        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
			        Paint paint = new Paint();
			        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
			        if(!myBKG.isRecycled())canvas.drawBitmap(myBKG, 0, 0, paint);
			       
				overlay = StackBlur.blurNatively(overlay, (int) radius, true);
				long endMs = System.currentTimeMillis();  
				if(DEBUG)Log.d("xssblur", "forceTouchBlurBg() xss  time="+(endMs-startMs));
				//bottomBG= new BitmapDrawable(mRoundRectImageView.getRoundBitmap(cropBitmap(overlay, (1),false), 20));//del by xss for ios10 forceTouch
				statusBarBlurBg = new BitmapDrawable(cropBitmap(overlay, (1),true));//added by xss for ios10 StausBarBlurBg
			      return overlay; 				
			}

			@Override
			protected void onPostExecute(Bitmap overlay){
                               if(DEBUG)Log.d("xssblur", "forceTouchBlurBg() xss overlay.getWidth()="+overlay.getWidth()+"   overlay.getHeight()="+overlay.getHeight()+"   mBlurWallpaperBgView="+mBlurWallpaperBgView);	
						 if(overlay!=null){
                                            setForceTouchBlurBg(overlay);
							/*if(mState != PhoneStatusBar.DIRECTION_NONE) 
								return;*/
						/*Begin:added by xss for ios10*/	
						if(myBKG!=null && !myBKG.isRecycled())myBKG.recycle();//addedd by xss for ios10
						BitmapDrawable bg=new BitmapDrawable(overlay);
						setViewRecycle(mBlurWallpaperBgView);
						if(mBlurWallpaperBgView!=null)mBlurWallpaperBgView.setBackground(bg);
						/*End:added by xss for ios10*/	
						//mBlurBg.setBackgroundResource(com.android.internal.R.drawable.rect_gray_background_b);//add by scq
						//setViewRecycle(mBlurBg);
						//if(mBlurBg!=null){
                                                //mBlurBg.setBackground(bottomBG);//add by scq
                         //                       mBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background);
						 //    mBlurBg.invalidate();//add by scq
						//}
                         /*if(mMusicBlurBg!=null){
                             //mMusicBlurBg.setBackground(bottomBG);//add by scq
                             mMusicBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background);
						     mMusicBlurBg.invalidate();//add by scq  
						}*///modified by xss for ios10 statusBarBlurBg
										   
				}
			}
	    	}.execute();
	 }
/*
* add by csc
* */

    private Handler handler=null;
    BitmapDrawable mBottomBG;
    public void setBottomPanBg(/*BitmapDrawable mBottomBG*/){//modified by xss for ios10 on 20170214
            // this.mBottomBG = mBottomBG;//modified by xss for ios10 on 20170214
            handler.post(runnableUi);
        }

    Runnable   runnableUi=new  Runnable(){
        @Override
        public void run() {
           // if(mBlurBg!=null){
                //mBlurBg.setBackground(mBottomBG);//add by scq
           //     mBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background);//added by xss for ios10 on 20170214
           //     mBlurBg.invalidate();//add by scq

           // }
            /*if(mMusicBlurBg!=null){
                //mMusicBlurBg.setBackground(mBottomBG);//add by scq
                mBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background);//added by xss for ios10 on 20170214
                mMusicBlurBg.invalidate();//add by scq
            }*/
        }

    };


	 private BroadcastReceiver mWallpaperBg=new BroadcastReceiver()
        {
        @Override
            public void onReceive(Context context,final Intent intent)
            {
                  String action = intent.getAction();
		    Handler handler = new Handler();		  
		    if(action.equals(Intent.ACTION_HOMESCREEN_WALLPAPER_CASHE_FINISHED)){
		           handler.postDelayed(new Runnable(){
				@Override
				public void run() {
	                            mBlurWallpaperBg=getBlurWallpaperBg();
	                            //if(mBlurBg==null)mBlurBg=(ImageView)findViewById(com.android.internal.R.id.blur_bg);
	                           // if(mMusicBlurBg==null)mMusicBlurBg=(ImageView)findViewById(com.android.internal.R.id.music_blur_bg);//added by scq
					/*mBlurBg.setBackground(mBlurWallpaperBg);
					//mMusicBlurBg.setBackground(mBlurWallpaperBg);*/
					//mBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background);//added by xss for ios10 on 20170214
					//mMusicBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background);//added by xss for ios10  on 20170214
					if(mBlurWallpaperBgView!=null)mBlurWallpaperBgView.setBackground(mBlurWallpaperBg);//del by xss for ios10
				}
			   }, 6000);
	           }else if(action.equals("set_live_wallpaper_cenon")){
	                   handler.postDelayed(new Runnable(){
					@Override
					public void run() {
			                     mBlurWallpaperBg=getBlurWallpaperBg();
			                     //if(mBlurBg==null)mBlurBg=(ImageView)findViewById(com.android.internal.R.id.blur_bg);
			        //             if(mMusicBlurBg==null)mMusicBlurBg=(ImageView)findViewById(com.android.internal.R.id.music_blur_bg);//added by scq
						/*mBlurBg.setBackground(mBlurWallpaperBg);
						mMusicBlurBg.setBackground(mBlurWallpaperBg);*/
					//	mBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background);//added by xss for ios10 on 20170214
					//mMusicBlurBg.setBackgroundResource(com.android.internal.R.drawable.control_center_view_background);//added by xss for ios10 on 20170214
						if(mBlurWallpaperBgView!=null)mBlurWallpaperBgView.setBackground(mBlurWallpaperBg);//del by xss for ios10
					}
			     }, 6000);
	              }
	     }
        };

    public Drawable getBlurWallpaperBg(){
            BitmapDrawable bd=BitmapUtils.getBlurWallpaper(mContext); 		
	     return bd;
     }

     public void setBlurWallpaperBgView(View tab){
	     if(mBlurWallpaperBgView==null || !mBlurWallpaperBgView.equals(tab))mBlurWallpaperBgView=tab;//mdified by xss for ios10
     }
	 
     public void setBlurActivityBgView(View mView){
	      if(mBlurActivityBgView==null || !mBlurActivityBgView.equals(mView))mBlurActivityBgView=mView;//mdified by xss for ios10
     }
	
     private void setViewRecycle(View view){
           if(view!=null && view.getBackground() != null 
	 	&& (view.getBackground() instanceof BitmapDrawable)
		&& ((BitmapDrawable)view.getBackground()).getBitmap() != null) {
		((BitmapDrawable)view.getBackground()).getBitmap().recycle();
	}
     }

     private static final int SCREEN_WIDTH = FeatureOption.CENON_HD?720:1080;
    private static final int SCREEN_HEIGHT = FeatureOption.CENON_HD?1280:1920;
    private Bitmap cropBitmap(Bitmap bitmap, float proportion,boolean isStatusBarBlurBg) {//modified by xss for ios10 for statusBarBlurBg
	 boolean mPortrait = mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        int startPointX = isStatusBarBlurBg? 0 : (FeatureOption.CENON_HD?10:15); // Starting point X
        int startPointY = isStatusBarBlurBg? 0 : (FeatureOption.CENON_HD?470:675);// Starting point Y
        int cropWidth = isStatusBarBlurBg? 720 : (FeatureOption.CENON_HD?700:1050); // Interception Width
        int cropHeight = isStatusBarBlurBg? 10 : (FeatureOption.CENON_HD?780:1170);// Interception Height
	 Bitmap mProcessBitmap = (FeatureOption.CENON_HD?BigAndSmallBitmap(bitmap, SCREEN_WIDTH, SCREEN_HEIGHT):bitmap);
        if(!mPortrait){
	 	startPointX = isStatusBarBlurBg? 0 : (FeatureOption.CENON_HD?10:15);
		startPointY = isStatusBarBlurBg? 0 : (FeatureOption.CENON_HD?10:15);
		cropWidth = isStatusBarBlurBg? 1280 : (FeatureOption.CENON_HD?1260:1890);
		cropHeight = isStatusBarBlurBg? 10 : (FeatureOption.CENON_HD?700:1050);
		mProcessBitmap = (FeatureOption.CENON_HD?BigAndSmallBitmap(bitmap, SCREEN_HEIGHT, SCREEN_WIDTH):bitmap);;
	 }
        //android.util.Log.i("Alin--scq","startPointX = " + startPointX + " , startPointY = " + startPointY + " , cropWidth = " + cropWidth + " , cropHeight = " + cropHeight);
        int smallStartPointX = (int)(startPointX/proportion);
        int smallStartPointY = (int)(startPointY/proportion);
        int smallCropWidth = (int)(cropWidth/proportion);
        int smallCropHeight = (int)(cropHeight/proportion);
        Bitmap mBitmap = Bitmap.createBitmap(mProcessBitmap, smallStartPointX, smallStartPointY, smallCropWidth, smallCropHeight, null, false);
        return mBitmap;

    }
	
    public static Bitmap BigAndSmallBitmap(Bitmap b, float x, float y){
        int w=b.getWidth();
        int h=b.getHeight();
        float sx=(float)x/w;
        float sy=(float)y/h;
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy);
        Bitmap resizeBmp = Bitmap.createBitmap(b, 0, 0, w,h, matrix, true);
        return resizeBmp;
    }

    public BitmapDrawable getStatusBarBlurBg(){//added by xss for ios10 statusBarBlurBg
       return statusBarBlurBg;
    }	  
/*End:add by xss for force touch */		
}

