package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.format.DateFormat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.util.AttributeSet;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.view.animation.Animation.AnimationListener;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import com.android.systemui.slider.Event;
import com.android.systemui.slider.CalendarView;
import com.android.systemui.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.lang.reflect.Method;

import com.android.systemui.statusbar.phone.CityListInfo;
import com.android.systemui.statusbar.phone.Weather.Weather_Column;
import com.android.systemui.statusbar.phone.WebAction.WebDownLoadListener;
import com.android.systemui.statusbar.phone.WebActionCityList.DownLoadCityListListener;

import android.content.ContentValues;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Message;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.os.Build;
public class AppPreviewEdit extends LinearLayout implements OnClickListener{
    boolean DEBUG = false;
    public Context mContext;
    public Intent mAppPreviewIntent;
    public boolean isLockScreen=false;
    public boolean inLockScreen=false;	
    public TextView mUnLockScreenEdit;
    public Drawable mCalendarIcon;    
  
    public AppPreviewEdit(Context context) {
	 super(context);
         mContext = context;
    }

    public AppPreviewEdit(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
    }

    public AppPreviewEdit(Context context, AttributeSet attrs, int defStyle) {
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
	mUnLockScreenEdit = (TextView)findViewById(R.id.unlock_screen_edit_text);
	mUnLockScreenEdit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	// TODO Auto-generated method stub
	int id=view.getId();
	if(DEBUG)Log.i("widget_edit", "onClick()  id===="+id);
	if(DEBUG)Log.i("widget_edit", "onClick()  isLockScreen="+isLockScreen+"    inLockScreen="+inLockScreen);	   	   	
	final Intent homePressedIntent = new Intent("home_key_is_pressed");
	switch(id){
	     case R.id.unlock_screen_edit_text:
		  try{	
			mAppPreviewIntent= new Intent(Intent.ACTION_MAIN);
			mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mAppPreviewIntent.setClassName("com.hskj.hometest","com.hskj.hometest.DragListActivity");
			BitmapDrawable bd = (BitmapDrawable) mCalendarIcon;
			mAppPreviewIntent.putExtra("calendar_iocn",getBytes(bd.getBitmap()));
			//collapseStatusBar(mContext);
			if(isLockScreen){
			     mContext.sendBroadcast(homePressedIntent);
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
		      if(DEBUG)Log.i("widget_edit", "onClick()  weather_app_name====");
                     mContext.sendBroadcast(new Intent("home_key_is_pressed"));
		}
	};
    public void isLockScreen(boolean lock){
	isLockScreen=lock;
    }   

    public void lockScreen(){
	inLockScreen=true;
	mAppPreviewIntent = null;
    } 
	
    public void unLockScreen(){
	 if(DEBUG)Log.i("widget_edit", "unLockScreen()  mAppPreviewIntent="+mAppPreviewIntent);
	 inLockScreen=false;
	if(mAppPreviewIntent!=null){
		Intent intent = mAppPreviewIntent;
		mAppPreviewIntent = null;
	       mContext.startActivity(intent);  
	}
    } 
    public void setCalendarIcon(Drawable icon){
	  if(DEBUG)Log.i("widget_edit", "setCalendarIcon()  icon===="+icon);	
         mCalendarIcon =  icon;
    }
    private  byte[] getBytes(Bitmap bitmap){  
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	    	bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
	    	return baos.toByteArray();
 	}
	private  Bitmap getBitmap(byte[] data){  
	      return BitmapFactory.decodeByteArray(data, 0, data.length);//\u4ece\u5b57\u8282\u6570\u7ec4\u89e3\u7801\u4f4d\u56fe  
	} 
	public  void collapseStatusBar(Context context) {
	    try {
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
	    }
	}
}
   
      
