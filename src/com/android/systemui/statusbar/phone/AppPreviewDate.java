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
import java.util.Locale;//added by xss for ios10 lunarDate

public class AppPreviewDate extends LinearLayout implements OnClickListener{

    private Context mContext;
    private TextView mDateText;
    private TextView mLunarDateText;//added by xss for ios10 lunarDate
    
    public AppPreviewDate(Context context) {
	 super(context);
         mContext = context;
    }

    public AppPreviewDate(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
    }

    public AppPreviewDate(Context context, AttributeSet attrs, int defStyle) {
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
	mDateText = (TextView)findViewById(R.id.unlock_screen_date_text);	
	mLunarDateText = (TextView)findViewById(R.id.unlock_screen_lunar_date_text);//added by xss for ios10 lunarDate
	mLunarDateText.setVisibility(isZh(getContext())? View.VISIBLE : View.GONE);//added by xss for ios10 lunarDate
    }

    @Override
    public void onClick(View view) {
	// TODO Auto-generated method stub		
    }

    public TextView getDateText(){
        return mDateText;
    }

   public TextView getLunarDateText(){//added by xss for ios10 lunarDate
       return mLunarDateText;
    }	

   public static boolean isZh(Context context) {  //added by xss for ios10 lunarDate
	    Locale locale = context.getResources().getConfiguration().locale;  
	    String language = locale.getLanguage();  
	    if (language.endsWith("zh"))  
	        return true;  
	    else  
	        return false;  
     }  	
}
   
      
