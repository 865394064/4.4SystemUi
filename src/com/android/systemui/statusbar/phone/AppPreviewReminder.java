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

import com.android.systemui.R;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Vector;
import java.lang.reflect.Method;


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
public class AppPreviewReminder extends LinearLayout implements OnClickListener{
   String TAG="ApplicationPreview";	
     boolean DEBUG = false;
    public Context mContext;
    private TextView mNoReminderText;
    public Intent mAppPreviewIntent;
    public boolean isLockScreen=false;
    public boolean inLockScreen=false;
	
    public AppPreviewReminder(Context context) {
	 super(context);
         mContext = context;
    }

    public AppPreviewReminder(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
    }

    public AppPreviewReminder(Context context, AttributeSet attrs, int defStyle) {
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
	mNoReminderText = (TextView)findViewById(R.id.note_empty_content_text);	
	mNoReminderText.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	// TODO Auto-generated method stub	
	int id=view.getId();
	final Intent homePressedIntent = new Intent("home_key_is_pressed");
	switch(id){
	       case R.id.note_empty_content_text://added by cfb
	          try{
			  mAppPreviewIntent=mContext.getPackageManager().getLaunchIntentForPackage("org.espier.reminder");
			  mAppPreviewIntent.addCategory(Intent.CATEGORY_LAUNCHER);
			  mAppPreviewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

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
                     mContext.sendBroadcast(new Intent("home_key_is_pressed"));
		}
	};
	
    public TextView getNoReminderText(){
       return mNoReminderText;
    }
 
   public void isLockScreen(boolean lock){
	isLockScreen=lock;
    }   

    public void lockScreen(){
	inLockScreen=true;	
	mAppPreviewIntent = null;
    }  
	
    public void unLockScreen(){
	 if(DEBUG)Log.i(TAG, "AppPreviewReminder unLockScreen()  mAppPreviewIntent="+mAppPreviewIntent);
	 inLockScreen=false;
	if(mAppPreviewIntent!=null){
		Intent intent = mAppPreviewIntent;
		mAppPreviewIntent = null;
	     getContext().startActivity(intent);  
	}
    } 	
}
   
      
