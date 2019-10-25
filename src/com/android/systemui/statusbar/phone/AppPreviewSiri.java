package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;

//import com.hskj.iphoneutil.FeatureOption;
public class AppPreviewSiri extends LinearLayout implements OnClickListener{
    String TAG="framework AppPreviewSiri";	
    boolean DEBUG = true;
    public Context mContext;
    public Intent mAppPreviewIntent;
    public boolean isLockScreen=false;
    private boolean inLockScreen = false;
    public String[] siriSuggestAppPackageName;
    private View mSiriSuggestAppsView,mSiriSuggestAppsSecRow;
    private TextView mSiriViewExpandOrHide;
    private ImageView mCameraSuggest,mClockSuggest,mMmsSuggest,mSafariSuggest,mAppStoreSuggest,mPhoneSuggest,mPhotoSuggest,mSettingsSuggest;
    private boolean mSiriViewIsExpand=false;

    public AppPreviewSiri(Context context) {
	 super(context);
         mContext = context;
    }

    public AppPreviewSiri(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
    }

    public AppPreviewSiri(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /// M: Init customize clock plugin
       
    }
    public void setContext(Context context){
        mContext = context;
			  
	
    }
    
    @Override
     protected void onFinishInflate() {
	// TODO Auto-generated method stub
	super.onFinishInflate();		   
	//siri suggest app
	  mSiriSuggestAppsView = (View)findViewById(R.id.siri_suggest_apps_view);
	  mSiriViewExpandOrHide = (TextView)findViewById(R.id.siri_expand_or_hide);
	  mSiriViewExpandOrHide.setOnClickListener(mSiriViewlistener);
	  mSiriSuggestAppsSecRow = (View)findViewById(R.id.siri_suggest_apps_two);
	  mCameraSuggest = (ImageView)findViewById(R.id.siri_suggest_camera);
	  mCameraSuggest.setOnClickListener(this);
	  mClockSuggest = (ImageView)findViewById(R.id.siri_suggest_clock);
	  mClockSuggest.setOnClickListener(this);
	  mMmsSuggest = (ImageView)findViewById(R.id.siri_suggest_mms);
	  mMmsSuggest.setOnClickListener(this);
	  mSafariSuggest = (ImageView)findViewById(R.id.siri_suggest_safari);
	  mSafariSuggest.setOnClickListener(this);
	  mAppStoreSuggest = (ImageView)findViewById(R.id.siri_suggest_app_store);
	  mAppStoreSuggest.setOnClickListener(this);
	  mPhoneSuggest = (ImageView)findViewById(R.id.siri_suggest_phone);
	  mPhoneSuggest.setOnClickListener(this);
	  mPhotoSuggest = (ImageView)findViewById(R.id.siri_suggest_photo);
	  mPhotoSuggest.setOnClickListener(this);
	  mSettingsSuggest = (ImageView)findViewById(R.id.siri_suggest_settings);
	  mSettingsSuggest.setOnClickListener(this);
	  siriSuggestAppPackageName = getResources().getStringArray(
			R.array.siri_suggest_app_package_name);
    }

    @Override
    public void onClick(View view) {
	// TODO Auto-generated method stub
	int id=view.getId();
	if(DEBUG)Log.i(TAG, "onClick()  id===="+id);
	if(DEBUG)Log.i(TAG, "onClick()  ====   inLockScreen="+inLockScreen);	   	   	
	final Intent homePressedIntent = new Intent("home_key_is_pressed");
	 Intent intent1 = new Intent("STATUS_BAR_RESTORE");
	switch(id){
	      case R.id.siri_suggest_camera:
			try{
				 mAppPreviewIntent = new Intent(Intent.ACTION_MAIN);
				  mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
				  mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			         mAppPreviewIntent.setClassName("com.android.gallery3d","com.android.camera.CameraLauncher");
				 if(isLockScreen){
				       mContext.sendBroadcast(homePressedIntent);
					break; 
				  }
				 //collapseStatusBar(mContext);//added by xss for ios10
                             mContext.sendBroadcast(intent1);
				 if(inLockScreen){
	                              myHandler.removeCallbacks(myRunnable);
		                      myHandler.postDelayed(myRunnable, 500);
					break; 		  
				 }
				  sendNotificationIntent();//added by xss for ios10 back to last app
				 mContext.startActivity(mAppPreviewIntent);  	
			}catch(Exception e){
				}
			break;
		case R.id.siri_suggest_clock:
			try{
				mAppPreviewIntent = new Intent(Intent.ACTION_MAIN);
				mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			        mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				 mAppPreviewIntent.putExtra("launch_from_control_center", true);	
				 mAppPreviewIntent.setClassName("com.android.deskclock", "com.android.deskclock.DeskClockGroupActivity");
				 if(isLockScreen){
				       mContext.sendBroadcast(homePressedIntent);
					break; 
				  }
				//collapseStatusBar(mContext);//added by xss for ios10
                             mContext.sendBroadcast(intent1);
				 if(inLockScreen){
	                              myHandler.removeCallbacks(myRunnable);
		                      myHandler.postDelayed(myRunnable, 500);
					break; 		  
				 }
				  sendNotificationIntent();//added by xss for ios10 back to last app
				 mContext.startActivity(mAppPreviewIntent); 	
			}catch(Exception e){
				}
			break;
		case R.id.siri_suggest_mms:
			try{
				 mAppPreviewIntent = getContext().getPackageManager().getLaunchIntentForPackage(siriSuggestAppPackageName[0]);
				 mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			        mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				 if(isLockScreen){
				       mContext.sendBroadcast(homePressedIntent);
					break; 
				  }
				 //collapseStatusBar(mContext);//added by xss for ios10
				  mContext.sendBroadcast(intent1);
				 if(inLockScreen){
	                             myHandler.removeCallbacks(myRunnable);
		                      myHandler.postDelayed(myRunnable, 500);
					break; 		  
				 }
				  sendNotificationIntent();//added by xss for ios10 back to last app
				 mContext.startActivity(mAppPreviewIntent); 	
			}catch(Exception e){
				}
			break;
		case R.id.siri_suggest_safari:
			try{
				mAppPreviewIntent = getContext().getPackageManager().getLaunchIntentForPackage(siriSuggestAppPackageName[1]);
				mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			        mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				 if(isLockScreen){
				       mContext.sendBroadcast(homePressedIntent);
					break; 
				  }
				 //collapseStatusBar(mContext);//added by xss for ios10
				  mContext.sendBroadcast(intent1);
				 if(inLockScreen){
	                              myHandler.removeCallbacks(myRunnable);
		                      myHandler.postDelayed(myRunnable, 500);
					break; 		  
				 }
				  sendNotificationIntent();//added by xss for ios10 back to last app
				 mContext.startActivity(mAppPreviewIntent); 	
			}catch(Exception e){
				}
			break;
		case R.id.siri_suggest_app_store:
			try{
				  mAppPreviewIntent = new Intent(Intent.ACTION_MAIN); 
			       /*if(SystemProperties.get("cenon.soft.mode").equals("1")){
                                    mAppPreviewIntent.setClassName("com.android.vending", "com.android.vending.AssetBrowserActivity");
				}else{*/
                                    mAppPreviewIntent.setClassName("com.sztuyue.app.store", "com.sztuyue.app.store.ui.MainActivity");
//				}
				mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			        mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				 if(isLockScreen){
				       mContext.sendBroadcast(homePressedIntent);
					break; 
				  }
				 //collapseStatusBar(mContext);//added by xss for ios10
				  mContext.sendBroadcast(intent1);
				 if(inLockScreen){
	                             myHandler.removeCallbacks(myRunnable);
		                      myHandler.postDelayed(myRunnable, 500);
					break; 		  
				 }
				  sendNotificationIntent();//added by xss for ios10 back to last app
				 mContext.startActivity(mAppPreviewIntent); 	
			}catch(Exception e){
				}
			break;
		case R.id.siri_suggest_phone:
			try{
				mAppPreviewIntent = getContext().getPackageManager().getLaunchIntentForPackage(siriSuggestAppPackageName[2]);
				mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			        mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				if(isLockScreen){
				       mContext.sendBroadcast(homePressedIntent);
					break; 
				  }
				 //collapseStatusBar(mContext);//added by xss for ios10
				  mContext.sendBroadcast(intent1);
				 if(inLockScreen){
	                              myHandler.removeCallbacks(myRunnable);
		                      myHandler.postDelayed(myRunnable, 500);
					break; 		  
				 }
				  sendNotificationIntent();//added by xss for ios10 back to last app
				 mContext.startActivity(mAppPreviewIntent); 	
			}catch(Exception e){
				}
			break;
		case R.id.siri_suggest_photo:
			  mAppPreviewIntent = new Intent(Intent.ACTION_MAIN);
			  mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			  mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		         mAppPreviewIntent.setClassName("com.android.gallery3d","com.android.gallery3d.app.Gallery");
			  if(isLockScreen){
			       mContext.sendBroadcast(homePressedIntent);
				break; 
			  }
			  //collapseStatusBar(mContext);//added by xss for ios10
			   mContext.sendBroadcast(intent1);
			 if(inLockScreen){
                             myHandler.removeCallbacks(myRunnable);
	                      myHandler.postDelayed(myRunnable, 500);
				break; 		  
			 }
			  sendNotificationIntent();//added by xss for ios10 back to last app
			 mContext.startActivity(mAppPreviewIntent);  	 
			break;
		case R.id.siri_suggest_settings:
			try{
				mAppPreviewIntent = getContext().getPackageManager().getLaunchIntentForPackage(siriSuggestAppPackageName[3]);
				mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			        mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					
				if(isLockScreen){
				       mContext.sendBroadcast(homePressedIntent);
					break; 
				  }
                             mContext.sendBroadcast(intent1);
				 //collapseStatusBar(mContext);//added by xss for ios10
				 if(inLockScreen){
                                     myHandler.removeCallbacks(myRunnable);
		                      myHandler.postDelayed(myRunnable, 500);
					break; 		  
				 }
				 sendNotificationIntent();//added by xss for ios10 back to last app
				 mContext.startActivity(mAppPreviewIntent); 	
			}catch(Exception e){
				}
			break; 
        }
    }
   
    public void isLockScreen(boolean lock){
	  isLockScreen=lock;
    }   

    public void lockScreen(){
	  if(DEBUG)Log.i(TAG, "lockScreen() ====");	
	  inLockScreen = true;	
	  mAppPreviewIntent = null;
    }  
	
    public void unLockScreen(){
	 if(DEBUG)Log.i(TAG, "unLockScreen()  mAppPreviewIntent="+mAppPreviewIntent);
	 inLockScreen = false;	
	if(mAppPreviewIntent!=null){
		Intent intent = mAppPreviewIntent;
		mAppPreviewIntent = null;
	     getContext().startActivity(intent);  
	}
    } 
    
   

   private OnClickListener mSiriViewlistener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
                      
		      if(mSiriSuggestAppsView!=null && mSiriSuggestAppsSecRow!=null && mSiriViewExpandOrHide!=null){
			  	mSiriViewExpandOrHide.setEnabled(false);
                            mSiriViewIsExpand=!mSiriViewIsExpand;
				mSiriViewExpandOrHide.setText(mSiriViewIsExpand?R.string.siri_suggest_hide:R.string.siri_suggest_expand);
				ResizeAnimation a = new ResizeAnimation(mSiriSuggestAppsView);
		               a.setDuration(500);
				int startHeight=mSiriSuggestAppsView.getHeight();
			       int endHeight=mSiriViewIsExpand?(startHeight+(173)):(startHeight-(173));
				a.setParams(startHeight, endHeight);
				mSiriSuggestAppsView.startAnimation(a);
				a.setAnimationListener(new AnimationListener() {
					@Override
					public void onAnimationStart(Animation arg0) {
						// TODO Auto-generated method stub
					}
					@Override
					public void onAnimationRepeat(Animation arg0) {
						// TODO Auto-generated method stub
					}
					@Override
					public void onAnimationEnd(Animation arg0) {
						// TODO Auto-generated method stub
						mSiriSuggestAppsSecRow.setVisibility(mSiriViewIsExpand?View.VISIBLE:View.GONE);
						mSiriViewExpandOrHide.setEnabled(true);
					}
				});                               
		      }
		}
    };		   
       class ResizeAnimation extends Animation {

		    private int startHeight;
		    private int deltaHeight; // distance between start and end height
		    private View view;

		    /**
		     * constructor, do not forget to use the setParams(int, int) method before
		     * starting the animation
		     * @param v
		     */
		    public ResizeAnimation (View v) {
		        this.view = v;
		    }

		    @Override
		    protected void applyTransformation(float interpolatedTime, Transformation t) {

		        view.getLayoutParams().height = (int) (startHeight + deltaHeight * interpolatedTime);
		        view.requestLayout();
		    }

		    /**
		     * set the starting and ending height for the resize animation
		     * starting height is usually the views current height, the end height is the height
		     * we want to reach after the animation is completed
		     * @param start height in pixels
		     * @param end height in pixels
		     */
		    public void setParams(int start, int end) {

		        this.startHeight = start;
		        deltaHeight = end - startHeight;
		    }

		    @Override
		    public boolean willChangeBounds() {
		        return true;
		    }
	}

      Handler myHandler= new Handler();
	Runnable myRunnable=new Runnable() {
		
		@Override
		public void run() {
                     mContext.sendBroadcast(new Intent("home_key_is_pressed"));
		}
	};

       public  void collapseStatusBar(Context context) {//added by xss for ios10
           if(DEBUG)Log.i(TAG, "collapseStatusBar()  =====");
	    /*try {
	        Object statusBarManager = context.getSystemService("statusbar");
	        Method collapse;

	        if (Build.VERSION.SDK_INT <= 16) {
	            collapse = statusBarManager.getClass().getMethod("collapse");
	        } else {
	            collapse = statusBarManager.getClass().getMethod("collapsePanels");
	        }
	        collapse.invoke(statusBarManager);
	    } catch (Exception localException) {
	        localException.printStackTrace();
	    }*/
	    Intent intent1 = new Intent("STATUS_BAR_RESTORE");
           context.sendBroadcast(intent1);
	}

	/*Begin:added by xss for back to last app*/    
	private void sendNotificationIntent(){

	     Intent notifipanelIsClickIntent=new Intent("notificationContentView_is_click");
	     notifipanelIsClickIntent.putExtra("is_click",true);
           mContext.sendBroadcast(notifipanelIsClickIntent);
	    
	}  
	/*End:added by xss for back to last app*/
}
