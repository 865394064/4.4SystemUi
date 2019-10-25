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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.systemui.R;

public class AppPreviewStockMarket extends LinearLayout implements OnClickListener{
    boolean DEBUG = false;
    public Context mContext;
    public Intent mAppPreviewIntent;
    public boolean isLockScreen=false;
    private boolean inLockScreen= false;//added by xss for ios10 market
  

    public AppPreviewStockMarket(Context context) {
	 super(context);
         mContext = context;
    }

    public AppPreviewStockMarket(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
    }

    public AppPreviewStockMarket(Context context, AttributeSet attrs, int defStyle) {
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
	//stock market
	  mStockMarketView = (View)findViewById(R.id.stock_market_view);
	  mStockMarketViewFir = (LinearLayout)findViewById(R.id.stock_market_view_fi);
	  mStockMarketViewSec = (View)findViewById(R.id.stock_market_view_se);
	  mStockMarketViewExpandOrHide = (TextView)findViewById(R.id.stock_market_expand_or_hide);
	  mStockMarketViewExpandOrHide.setOnClickListener(mStockMarketViewlistener);
	  mSMRelayoutFir = (RelativeLayout)findViewById(R.id.stock_market_view_fir);//added by cfb
	  mSMRelayoutFir.setOnClickListener(this);
	  mSMRelayoutSec = (RelativeLayout)findViewById(R.id.stock_market_view_sec);
	  mSMRelayoutSec.setOnClickListener(this);
	  mSMRelayoutThi = (RelativeLayout)findViewById(R.id.stock_market_view_thi);
	  mSMRelayoutThi.setOnClickListener(this);
	  mSMRelayoutFor = (RelativeLayout)findViewById(R.id.stock_market_view_fou);
	  mSMRelayoutFor.setOnClickListener(this);
	  mSMRelayoutFif = (RelativeLayout)findViewById(R.id.stock_market_view_fif);
	  mSMRelayoutFif.setOnClickListener(this);
	  mSMRelayoutSix = (RelativeLayout)findViewById(R.id.stock_market_view_six);
	  mSMRelayoutSix.setOnClickListener(this);
	  mSMRelayoutSev = (RelativeLayout)findViewById(R.id.stock_market_view_sev);
	  mSMRelayoutSev.setOnClickListener(this);
	  mSMRelayoutEig = (RelativeLayout)findViewById(R.id.stock_market_view_eig);
	  mSMRelayoutEig.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	// TODO Auto-generated method stub
	int id=view.getId();
	if(DEBUG)Log.i("locationapp", "onClick()  id===="+id);
	if(DEBUG)Log.i("locationapp", "onClick()  weather_app_name====");	   	   	
	final Intent homePressedIntent = new Intent("home_key_is_pressed");
	switch(id){
	      case R.id.stock_market_view_fir://added by cfb
	      case R.id.stock_market_view_sec:
	      case R.id.stock_market_view_thi:
	      case R.id.stock_market_view_fou:
	      case R.id.stock_market_view_fif:
	      case R.id.stock_market_view_six:
	      case R.id.stock_market_view_sev:
	      case R.id.stock_market_view_eig:
		  try{	
			  /*if(SystemProperties.get("cenon.soft.mode").equals("1")){//added by xss for ios10 market
	                       mAppPreviewIntent=mContext.getPackageManager().getLaunchIntentForPackage("com.yahoo.mobile.client.android.finance");
			  }else{*/
	                       mAppPreviewIntent=mContext.getPackageManager().getLaunchIntentForPackage("com.android.iphonestock");
//			  }
			  mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			  mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

			  if(isLockScreen){//added by xss for ios10 market
	                       mContext.startActivity(homePressedIntent);
				 break;		   
			  }
			  
			   Intent intent1 = new Intent("STATUS_BAR_RESTORE");
	                mContext.sendBroadcast(intent1);
			 if(inLockScreen){
	                      myHandler.removeCallbacks(myRunnable);
	                      myHandler.postDelayed(myRunnable, 500);
				break; 		  
			 }
			 /*Begin:added by xss for back to last app*/    
			     Intent notifipanelIsClickIntent=new Intent("notificationContentView_is_click");
			     notifipanelIsClickIntent.putExtra("is_click",true);
		           mContext.sendBroadcast(notifipanelIsClickIntent);
			/*End:added by xss for back to last app*/
			  mContext.startActivity(mAppPreviewIntent);
		 }catch(Exception e){
				} 
		break;
        }
    }
   	
   Handler myHandler= new Handler();
	Runnable myRunnable=new Runnable() {
		
		@Override
		public void run() {
                     mContext.sendBroadcast(new Intent("home_key_is_pressed"));
		}
	};
	

    public void isLockScreen(boolean lock){
	isLockScreen=lock;
    }   

    public void lockScreen(){
	inLockScreen=true;//added by xss for ios10 market
	mAppPreviewIntent = null;
    }
	
    public void unLockScreen(){
	inLockScreen = false;	
	 if(DEBUG)Log.i("locationapp", "unLockScreen()  mAppPreviewIntent="+mAppPreviewIntent);
	if(mAppPreviewIntent!=null){
		Intent intent = mAppPreviewIntent;
		mAppPreviewIntent = null;
	     getContext().startActivity(intent);  
	}
    } 
    
     //stock market
	   private View mStockMarketView,mStockMarketViewSec;
	   private LinearLayout mStockMarketViewFir;
	   private RelativeLayout mSMRelayoutFir,mSMRelayoutSec,mSMRelayoutThi,mSMRelayoutFor,mSMRelayoutFif,mSMRelayoutSix,mSMRelayoutSev,mSMRelayoutEig;//added by cfb
	   private TextView mStockMarketViewExpandOrHide;
	   private boolean mStockMarketViewIsExpand=false;
	   private OnClickListener mStockMarketViewlistener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                              
			      if(mStockMarketView!=null && mStockMarketViewSec!=null && mStockMarketViewExpandOrHide!=null){
				  	mStockMarketViewExpandOrHide.setEnabled(false);
	                            mStockMarketViewIsExpand=!mStockMarketViewIsExpand;
					mStockMarketViewExpandOrHide.setText(mStockMarketViewIsExpand?R.string.siri_suggest_hide:R.string.siri_suggest_expand);
					ResizeAnimation a = new ResizeAnimation(mStockMarketView);
			               a.setDuration(500);
					int startHeight=mStockMarketView.getHeight();
				       int endHeight=mStockMarketViewIsExpand?(startHeight+(/*FeatureOption.CENON_HD?*/480/*:640*/)):(startHeight-(/*FeatureOption.CENON_HD?*/480/*:640*/));
					a.setParams(startHeight, endHeight);
					mStockMarketView.startAnimation(a);
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
							LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams) mStockMarketViewFir.getLayoutParams();
							lp.setMargins(30, 0, 0, mStockMarketViewIsExpand?0:34);	
						    	mStockMarketViewFir.setLayoutParams(lp);
							mStockMarketViewSec.setVisibility(mStockMarketViewIsExpand?View.VISIBLE:View.GONE);
							mStockMarketViewExpandOrHide.setEnabled(true);
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
}
