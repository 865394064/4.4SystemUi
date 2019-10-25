package com.android.systemui.statusbar.phone;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
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
import com.android.systemui.statusbar.phone.Weather.Weather_Column;
import com.android.systemui.statusbar.phone.WebAction.WebDownLoadListener;
import com.android.systemui.statusbar.phone.WebActionCityList.DownLoadCityListListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;
public class AppPreviewWeather extends LinearLayout implements OnClickListener,DownLoadCityListListener,WebDownLoadListener{
    String TAG="AppPreviewWeather SystemUI"	;
    boolean DEBUG = false;
    public Context mContext;
    public Intent mAppPreviewIntent;
    public boolean isLockScreen=false;
    private boolean inLockScreen= false;	
    boolean isFirstShow=true;
    public AppPreviewWeather(Context context) {
	 super(context);
         mContext = context;
    }

    public AppPreviewWeather(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
    }

    public AppPreviewWeather(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /// M: Init customize clock plugin
       
    }
    public void setContext(Context context){
        mContext = context;
	if(DEBUG)Log.i("locationapp", "setContext()  ====  packageName=="+mContext.getPackageName()); 
	Resources res = mContext.getResources();
	 
	/*if (mBitmapArray1_8[0] == null) {
		for (int i = 0; i < 8; i++) {
			if(DEBUG)Log.i("locationapp", "setContext()  ====  bitmap=="+res.getIdentifier(("weather0" + (i+1)), "drawable-xhdpi", mContext.getPackageName())); 
			mBitmapArray1_8[i] = BitmapFactory.decodeResource(res,(R.drawable.weather01 + i));
			mSbBitmapArray1_8[i] = BitmapFactory.decodeResource(res,(R.drawable.zzz_weather01 + i));
		}
		for (int i = 0; i < 16; i++) {
			mBitmapArray11_27[i] = BitmapFactory.decodeResource(res,(R.drawable.weather11 + i));
			mSbBitmapArray11_27[i] = BitmapFactory.decodeResource(res,(R.drawable.zzz_weather11 + i));
		}
		for (int i = 0; i < 16; i++) {
			mBitmapArray29_44[i] = BitmapFactory.decodeResource(res,(R.drawable.weather29 + i));
			mSbBitmapArray29_44[i] = BitmapFactory.decodeResource(res,(R.drawable.zzz_weather29 + i));
		}
	}*/
	mDegreeLabel = res.getString(R.string.degree_label);
	mRainfallProbability = res.getString(R.string.default_rainfall_probability);
	mArrayDayOfWeek = getArrayDayOfWeek(System.currentTimeMillis(),false);
	mSbArrayDayOfWeek = getArrayDayOfWeek(System.currentTimeMillis(),true);	
	 if(isFirstShow)getWeather();
        isFirstShow=false;	  	
    }
    
    public void getWeather(){
        if(DEBUG)Log.i("locationapp", "SystemUI AppPreviewWeather  getWeather()  ====");         
	 getlocation();
	//setCity();  
    }      

    @Override
     protected void onFinishInflate() {
	// TODO Auto-generated method stub
	super.onFinishInflate();		   
	  //weather
	  mWeatherApp = (LinearLayout)findViewById(R.id.weather_app);//add by scq
	  mWeatherAppName = (TextView)findViewById(R.id.weather_app_name);
	  mWeatherApp.setOnClickListener(this);//modify by scq
	  mWeatherEmptyView = (TextView)findViewById(R.id.weather_empty_view);
	  mWeatherContentView = (View)findViewById(R.id.weather_content_view);
	  mCurrentWeatherView = (View)findViewById(R.id.current_weather_view);
	  mWeatherContentDivide = (View)findViewById(R.id.weather_content_divide);
	  mFutureWeatherView = (View)findViewById(R.id.future_weather_view);
	  mCurrentCitys = (TextView)findViewById(R.id.current_city);
	  mWeatherExpandOrHide = (TextView)findViewById(R.id.weather_expand_or_hide);
	  mWeatherExpandOrHide.setOnClickListener(mWeatherlistener);
         
    }

    @Override
    public void onClick(View view) {
	// TODO Auto-generated method stub
	int id=view.getId();
	if(DEBUG)Log.i("locationapp", "onClick()  id===="+id);
	if(DEBUG)Log.i("locationapp", "onClick()  weather_app_name====");	   	   	
	final Intent homePressedIntent = new Intent("home_key_is_pressed");
	switch(id){
	       case R.id.weather_app://Modify by scq
	           try{
			  //mContext.sendBroadcast(homePressedIntent); 	   	
			 /* if(SystemProperties.get("cenon.soft.mode").equals("1")){
	                      mAppPreviewIntent=mContext.getPackageManager().getLaunchIntentForPackage("com.yahoo.mobile.client.android.weather");
			  }else{*/
	                      mAppPreviewIntent=mContext.getPackageManager().getLaunchIntentForPackage("com.hskj.iphoneweather");
//			  }
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
	
    public void isLockScreen(boolean lock){
	if(DEBUG)Log.i(TAG, "isLockScreen()  lock===="+lock); 	
	isLockScreen=lock;
    }   

    public void lockScreen(){
	inLockScreen=true;	
	mAppPreviewIntent = null;
	if(DEBUG)Log.i(TAG, " locationManager="+locationManager); 	
	if(locationManager != null){
              locationManager.removeUpdates(locationListener);
	       	locationManager = null;	   
	}
    }   
	
    public void unLockScreen(){
	inLockScreen = false;	
	 if(DEBUG)Log.i("widget_edit", "unLockScreen()  mAppPreviewIntent="+mAppPreviewIntent);
	if(mAppPreviewIntent!=null){
		Intent intent = mAppPreviewIntent;
		mAppPreviewIntent = null;
	     getContext().startActivity(intent);  
	}
    } 
    //weather
       private boolean mWeatherIsExpand=false;
       private View mWeatherContentView,mCurrentWeatherView,mWeatherContentDivide,mFutureWeatherView;
       private String cityName="",cityNameEn,mRainfallProbability,mWeatherAppTheFirstCityName;
       private TextView mWeatherAppName,mWeatherEmptyView,mCurrentCitys,mWeatherExpandOrHide;
       private LinearLayout mWeatherApp;//add by scq
       private LocationManager locationManager;  
       private String locationProvider;
       private Geocoder geocoder;   //\u6b64\u5bf9\u8c61\u80fd\u901a\u8fc7\u7ecf\u7eac\u5ea6\u6765\u83b7\u53d6\u76f8\u5e94\u7684\u57ce\u5e02\u7b49\u4fe1\u606f  
	private WebActionCityList mWebActionCityList;
	public static final String NO_CHINISE_CITY = "no_chinise_city";
	private Vector<CityListInfo> mCityListInfo;
	private WebAction mWebAction;
	// \u5929\u6c14\u56fe\u7247\u5c0f
	private String[] mArrayDayOfWeek;
	private Bitmap[] mBitmapArray1_8 = new Bitmap[8];
	private Bitmap[] mBitmapArray11_27 = new Bitmap[16];
	private Bitmap[] mBitmapArray29_44 = new Bitmap[16];
		// \u5929\u6c14\u56fe\u7247\u5927
	private String[] mSbArrayDayOfWeek;
	private Bitmap[] mSbBitmapArray1_8 = new Bitmap[8];
	private Bitmap[] mSbBitmapArray11_27 = new Bitmap[16];
	private Bitmap[] mSbBitmapArray29_44 = new Bitmap[16];	
	private String mDegreeLabel;
        public String[] getArrayDayOfWeek(long millis,boolean statusBar) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(millis);	
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		String[] arrayDayOfWeek = mContext.getResources().getStringArray(statusBar?R.array.strDayOfWeekSb:R.array.strDayOfWeek);
		String[] targetArray = new String[9];
		for (int i = 0; i < 9; i++) {
			int j = (dayOfWeek + i) % 7;
			targetArray[i] = arrayDayOfWeek[j];
		}
		return targetArray;
	}
      
       private void setDefaultWeather(){
              setImageViewBitmap(R.id.current_weather_icon,BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_weather));
	       setTextViewText(R.id.current_city,mContext.getResources().getString(R.string.default_city));
		setTextViewText(R.id.current_weather_text1,"");
		setTextViewText(R.id.current_weather_text2,mContext.getResources().getString(R.string.default_rainfall_probability));
		setTextViewText(R.id.the_highest_and_lowest_temperature,mContext.getResources().getString(R.string.default_highest_and_lowest_temperature));
		setTextViewText(R.id.current_temperature, mContext.getResources().getString(R.string.default_current_temperature));
		if(mWeatherIsExpand)mWeatherExpandOrHide.setClickable(true);
		mWeatherExpandOrHide.setVisibility(View.GONE);
	}

      /*public void setTheFirstCityName(String name){
	      if(DEBUG)Log.i("xss_weather", "setTheFirstCityName()  name===="+name);	
             mWeatherAppTheFirstCityName = name;
	      setCity();		 
      }*/

       public void showWeatherEmptyView(boolean show){
              if(mWeatherIsExpand)mWeatherExpandOrHide.setClickable(true);
		mWeatherExpandOrHide.setVisibility(show?View.GONE:View.VISIBLE);
		mWeatherEmptyView.setVisibility(show?View.VISIBLE:View.GONE);
		mCurrentWeatherView.setVisibility(show?View.GONE:View.VISIBLE);
                if(!isFirstShow && show && mWeatherIsExpand)mWeatherExpandOrHide.performClick();//added by xss for bug[4414]
	}

       public boolean networkIsConnected() {
		boolean isNetworkUp = false;
		// \u76d1\u542c\u7f51\u7edc\u94fe\u63a5\u7684\u72b6\u6001
		ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
	        NetworkInfo info = cm.getActiveNetworkInfo();
	        if (info != null) {
	        	isNetworkUp = info.isAvailable();
	        }
		return isNetworkUp;
	}
		
        public void getlocation(){
	     if(!networkIsConnected())return;		
	     mWeatherAppTheFirstCityName=Settings.System.getString(mContext.getContentResolver(), "iphone_weather_the_first_city_name");		
            if(locationManager==null)locationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE); 
            //\u83b7\u53d6\u6240\u6709\u53ef\u7528\u7684\u4f4d\u7f6e\u63d0\u4f9b\u5668  
            List<String> providers = locationManager.getProviders(true);
            if(DEBUG)Log.i("locationapp", "SystemUI AppPreviewWeather   getlocation()  providers===="+providers);
            if(providers.contains(LocationManager.GPS_PROVIDER)){  
                //\u5982\u679c\u662fGPS  
            	if(DEBUG)Log.i("locationapp", "getlocation()  GPS====");
                locationProvider = LocationManager.GPS_PROVIDER;  
            }else if(providers.contains(LocationManager.NETWORK_PROVIDER)){             	
                //\u5982\u679c\u662fNetwork
            	if(DEBUG)Log.i("locationapp", "getlocation()  Network====");
                locationProvider = LocationManager.NETWORK_PROVIDER;  
            }else{  
                //Toast.makeText(mContext, "\u6ca1\u6709\u53ef\u7528\u7684\u4f4d\u7f6e\u63d0\u4f9b\u5668", Toast.LENGTH_SHORT).show(); 
                if(DEBUG)Log.i("locationapp", "getlocation()  mWeatherAppTheFirstCityName===="+mWeatherAppTheFirstCityName);		 
               // if(networkIsConnected() && mWeatherAppTheFirstCityName!=null && !mWeatherAppTheFirstCityName.equals("") && mWeatherAppTheFirstCityName.length()!=0){//del by xss for bug[4414]
                       setCity();
                //}
                return ; 
            }  
          //\u83b7\u53d6Location  
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if(DEBUG)Log.i("locationapp", "getlocation()  location===="+location);
            if(location!=null){  
                //\u4e0d\u4e3a\u7a7a,\u663e\u793a\u5730\u7406\u4f4d\u7f6e\u7ecf\u7eac\u5ea6  
            	cityName=updateWithNewLocation(location);
            	if(DEBUG)Log.i("locationapp", "getlocation()  cityName===="+cityName);
            	//Toast.makeText(mContext, "cityName="+cityName, Toast.LENGTH_SHORT).show();
            	setCity();
            }else{
		 if(DEBUG)Log.i("locationapp", "getlocation()  location   mWeatherAppTheFirstCityName===="+mWeatherAppTheFirstCityName);		 
               // if(networkIsConnected() && mWeatherAppTheFirstCityName!=null && !mWeatherAppTheFirstCityName.equals("") && mWeatherAppTheFirstCityName.length()!=0){//del by xss for bug[4414]
                       setCity();
                //}
	     }  
            //\u76d1\u89c6\u5730\u7406\u4f4d\u7f6e\u53d8\u5316  
           // locationManager.requestLocationUpdates(locationProvider, 3000, 1, locationListener);  
     }   
      
    /** 
     * LocationListern\u76d1\u542c\u5668 
     * \u53c2\u6570\uff1a\u5730\u7406\u4f4d\u7f6e\u63d0\u4f9b\u5668\u3001\u76d1\u542c\u4f4d\u7f6e\u53d8\u5316\u7684\u65f6\u95f4\u95f4\u9694\u3001\u4f4d\u7f6e\u53d8\u5316\u7684\u8ddd\u79bb\u95f4\u9694\u3001LocationListener\u76d1\u542c\u5668 
     */  
      
    LocationListener locationListener =  new LocationListener() {  
          
        @Override  
        public void onStatusChanged(String provider, int status, Bundle arg2) {               
        }            
        @Override  
        public void onProviderEnabled(String provider) {                
        }            
        @Override  
        public void onProviderDisabled(String provider) {  
        	cityName=updateWithNewLocation(null);
        	if(DEBUG)Log.i("locationapp", "onProviderDisabled()  cityName===="+cityName);
        	//Toast.makeText(mContext, "cityName="+cityName, Toast.LENGTH_SHORT).show();
        	setCity();
        }  
          
        @Override  
        public void onLocationChanged(Location location) {  
            //\u5982\u679c\u4f4d\u7f6e\u53d1\u751f\u53d8\u5316,\u91cd\u65b0\u663e\u793a 
        	if(DEBUG)Log.i("locationapp", "onLocationChanged====");
            //showLocation(location);  
        	cityName=updateWithNewLocation(location);
        	if(DEBUG)Log.i("locationapp", "onLocationChanged()  cityName===="+cityName);
        	//Toast.makeText(mContext, "cityName="+cityName, Toast.LENGTH_SHORT).show();
        	setCity();
        }  
    };  

    /**  
     * \u66f4\u65b0location  
     * @param location  
     * @return cityName  
     */    
    private  String updateWithNewLocation(Location location) {    
        String mcityName = "";    
        double lat = 0;    
        double lng = 0;    
        List<Address> addList = null;    
        if (location != null) {    
            lat = location.getLatitude();    
            lng = location.getLongitude();    
        } else {    
    
            System.out.println("\u65e0\u6cd5\u83b7\u53d6\u5730\u7406\u4fe1\u606f");    
        }
        String locationStr = "\u7ef4\u5ea6\uff1a" + lat +"\n"   
                + "\u7ecf\u5ea6\uff1a" + lng; 
        if(DEBUG)Log.i("locationapp", "updateWithNewLocation()  locationStr===="+locationStr);       
        try {    
            geocoder = new Geocoder(mContext);    
            addList = geocoder.getFromLocation(lat, lng, 1);    //\u89e3\u6790\u7ecf\u7eac\u5ea6    
            
        } catch (IOException e) {    
            // TODO Auto-generated catch block    
            e.printStackTrace();    
        }
        if(DEBUG)Log.i("locationapp", "updateWithNewLocation()  addList===="+addList);
        if (addList != null && addList.size() > 0) {    
            for (int i = 0; i < addList.size(); i++) {    
                Address add = addList.get(i);    
                mcityName += add.getLocality();
                if(DEBUG)Log.i("locationapp", "updateWithNewLocation()  mcityName===="+mcityName);
            }    
        }    
        if(mcityName.length()!=0){                    
            return mcityName.substring(0, (mcityName.length()-1));    
        } else {    
            return mcityName;    
        }   
    }   
    
    private void setCity(){
	   mWebActionCityList = new WebActionCityList(mContext);
	   mWebActionCityList.addDownLoadCityListListener(this);	
	   if(cityName.equals("") ||cityName.length()==0){
	   	if(DEBUG)Log.i("locationapp", "setCity()  mWeatherAppTheFirstCityName===="+mWeatherAppTheFirstCityName+"   networkIsConnected()="+networkIsConnected());
              if(networkIsConnected() &&mWeatherAppTheFirstCityName!=null && !mWeatherAppTheFirstCityName.equals("") && mWeatherAppTheFirstCityName.length()!=0){
                     showWeatherEmptyView(false);
	              mCurrentCitys.setText(mWeatherAppTheFirstCityName);
		    	cityNameEn = getChineseCityEn(mWeatherAppTheFirstCityName);
		    	if(DEBUG)Log.i("locationapp", "setCity()  mWeatherAppTheFirstCityName===="+mWeatherAppTheFirstCityName+"   cityNameEn="+cityNameEn);
		    	//cityNameEn="hangzhou";
		    	mWebActionCityList.startLoadCityList(cityNameEn);
		}else{
                    setDefaultWeather();
		      showWeatherEmptyView(true);
		}
	   }else{
	       showWeatherEmptyView(false);
              mCurrentCitys.setText(cityName);
	    	cityNameEn = getChineseCityEn(cityName);
	    	if(DEBUG)Log.i("locationapp", "setCity()  cityName===="+cityName+"   cityNameEn="+cityNameEn);
	    	//cityNameEn="hangzhou";
	    	if(networkIsConnected())mWebActionCityList.startLoadCityList(cityNameEn);
	   }
    }   
    
    
    public String getChineseCityEn(String cityState) {
		String city=cityState;
		String[] arrayCitiesCn = mContext.getResources().getStringArray(
				R.array.arrayCitiesCn);
		String[] arrayCitiesEn = mContext.getResources().getStringArray(
				R.array.arrayCitiesEn);
		String cityEn = NO_CHINISE_CITY;
		for (int i = 0; i < arrayCitiesEn.length; i++) {
			if (arrayCitiesCn[i].contains(cityState)) {
				cityEn = arrayCitiesEn[i];
				break;
			}
		}
		if(!cityEn.equals(NO_CHINISE_CITY))city = cityEn.substring(0,cityEn.indexOf("China"));
		if(DEBUG)Log.i("locationapp", "getChineseCity()  cityEn===="+cityEn+"  city===="+city+"   index="+cityEn.indexOf("China"));
		return city;
	}   	

       Handler mLocationHandler= new Handler();
	Runnable mLocationRunnable=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(DEBUG)Log.i("locationapp","    mLocationRunnable() ======");
			if(locationManager != null)locationManager.removeUpdates(locationListener);
		 }
	};
	
	Handler mDownLoadCityHandler= new Handler();
	Runnable mDownLoadCityRunnable=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(DEBUG)Log.i("locationapp","    mDownLoadCityRunnable() ======");
			mWebActionCityList.addDownLoadCityListListener(null);
		 } 
	};
	
       Handler mWebDownLoadHandler= new Handler();
	Runnable mWebDownLoadRunnable=new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(DEBUG)Log.i("locationapp","    mWebDownLoadRunnable() ======");
			mWebAction.setWebDownLoadListener(null);
		 }
	};

	@Override
	public void onStartDownLoadCityList() {
		// TODO Auto-generated method stub	
		if(DEBUG)Log.i("locationapp", "onStartDownLoadCityList() ====");
		//mDownLoadCityHandler.removeCallbacks(mDownLoadCityRunnable); 		 
	       mDownLoadCityHandler.postDelayed(mDownLoadCityRunnable,5000);
	}

	@Override
	public void onFinishDownLoadCityList(Vector<CityListInfo> cityListInfo) {
		// TODO Auto-generated method stub
		if(DEBUG)Log.i("locationapp", "onFinishDownLoadCityList()  cityListInfo===="+cityListInfo);
		mWebActionCityList.addDownLoadCityListListener(null);
		mCityListInfo = cityListInfo;
		if(cityListInfo.size()!=0){			
			String city = cityListInfo.get(0).getCity();
			String state = cityListInfo.get(0).getState();
			String location = cityListInfo.get(0).getLocation();
			mWebAction = new WebAction(mContext, location, 1);
			mWebAction.setWebDownLoadListener(this);
			mWebAction.startLoadData();
		}
		
	}  

	 @Override
	public void onStartWebDownLoad() {
		// TODO Auto-generated method stub
		if(DEBUG)Log.i("locationapp", "onStartWebDownLoad() ====");
		//mWebDownLoadHandler.removeCallbacks(mWebDownLoadRunnable); 		 
	       mWebDownLoadHandler.postDelayed(mWebDownLoadRunnable,5000);
	}

	@Override
	public void onFinishWebDownLoad(ContentValues values) {
		// TODO Auto-generated method stub
		if(DEBUG)Log.i("locationapp", "onFinishWebDownLoad()  values===="+values);
		showWeather(values);
		mWebAction.setWebDownLoadListener(null);
	}
	private void showWeather(ContentValues contentValues)
	{
		if(DEBUG)Log.i("locationapp", "updateRemoteView()  ====contentValues="+contentValues);
		if (contentValues == null) 	return;		

		int[] weatherIconArray = getWeatherIconArray(contentValues);
		setImageViewBitmap(R.id.current_weather_icon,getSbDrawBitmap(weatherIconArray[0]));

		int[] tempArray = getTempArray(contentValues);
		setTextViewText(R.id.current_weather_text1,mContext.getResources().getString(getTodayWeather(weatherIconArray)));
		setTextViewText(R.id.current_weather_text2,mRainfallProbability+"%");
		setTextViewText(R.id.the_highest_and_lowest_temperature,Integer.toString(tempArray[1])+ mDegreeLabel+"/"+Integer.toString(tempArray[2])+ mDegreeLabel);
		setTextViewText(R.id.current_temperature, Integer.toString(tempArray[0]) + mDegreeLabel);

		setTextViewText(R.id.weather_future_weekday_1, mArrayDayOfWeek[1]);
		setTextViewText(R.id.weather_future_weekday_2, mArrayDayOfWeek[2]);
	       setTextViewText(R.id.weather_future_weekday_3, mArrayDayOfWeek[3]);
		setTextViewText(R.id.weather_future_weekday_4, mArrayDayOfWeek[4]);
		setTextViewText(R.id.weather_future_weekday_5, mArrayDayOfWeek[5]);
		setTextViewText(R.id.weather_future_weekday_6, mArrayDayOfWeek[6]);
		//setTextViewText(R.id.weather_future_weekday_7, mArrayDayOfWeek[7]);

		setTextViewText(R.id.weather_future_temperature_1, Integer.toString(tempArray[2 * 1 + 1]) + mDegreeLabel+"/"+Integer.toString(tempArray[2 * 1 + 2]) + mDegreeLabel);
		setTextViewText(R.id.weather_future_temperature_2, Integer.toString(tempArray[2 * 2 + 1]) + mDegreeLabel+"/"+Integer.toString(tempArray[2 * 2 + 2]) + mDegreeLabel);
		setTextViewText(R.id.weather_future_temperature_3, Integer.toString(tempArray[2 * 3 + 1]) + mDegreeLabel+"/"+Integer.toString(tempArray[2 * 3 + 2]) + mDegreeLabel );
		setTextViewText(R.id.weather_future_temperature_4, Integer.toString(tempArray[2 * 4 + 1]) + mDegreeLabel+"/"+Integer.toString(tempArray[2 * 4 + 2]) + mDegreeLabel);
		setTextViewText(R.id.weather_future_temperature_5, Integer.toString(tempArray[2 * 5 + 1]) + mDegreeLabel+"/"+Integer.toString(tempArray[2 * 5 + 2]) + mDegreeLabel);
		setTextViewText(R.id.weather_future_temperature_6, Integer.toString(tempArray[2 * 6 + 1]) + mDegreeLabel+"/"+Integer.toString(tempArray[2 * 6 + 2]) + mDegreeLabel);
		

		setImageViewBitmap(R.id.weather_future_icon_1,getDrawBitmap(weatherIconArray[1 + 1]));
		setImageViewBitmap(R.id.weather_future_icon_2,getDrawBitmap(weatherIconArray[2 + 1]));
		setImageViewBitmap(R.id.weather_future_icon_3,getDrawBitmap(weatherIconArray[3 + 1]));
		setImageViewBitmap(R.id.weather_future_icon_4,getDrawBitmap(weatherIconArray[4 + 1]));
		setImageViewBitmap(R.id.weather_future_icon_5,getDrawBitmap(weatherIconArray[5 + 1]));
		setImageViewBitmap(R.id.weather_future_icon_6,getDrawBitmap(weatherIconArray[6 + 1]));
	}
	private  int[] getWeatherIconArray(ContentValues contentValues) {
		if(DEBUG)Log.i("locationapp", "getWeatherIconArray()  ====");
		int curWeathericon = Integer.parseInt(String.valueOf(contentValues
				.get(Weather_Column.CURRENT_WEATHERICON)));
		int weatherIconDay1 = Integer.parseInt(String.valueOf(contentValues
				.get(Weather_Column.WEATHER_ICON_DAY1)));
		int weatherIconDay2 = Integer.parseInt(String.valueOf(contentValues
				.get(Weather_Column.WEATHER_ICON_DAY2)));
		int weatherIconDay3 = Integer.parseInt(String.valueOf(contentValues
				.get(Weather_Column.WEATHER_ICON_DAY3)));
		int weatherIconDay4 = Integer.parseInt(String.valueOf(contentValues
				.get(Weather_Column.WEATHER_ICON_DAY4)));
		int weatherIconDay5 = Integer.parseInt(String.valueOf(contentValues
				.get(Weather_Column.WEATHER_ICON_DAY5)));
		int weatherIconDay6 = Integer.parseInt(String.valueOf(contentValues
				.get(Weather_Column.WEATHER_ICON_DAY6)));
		int weatherIconDay7 = Integer.parseInt(String.valueOf(contentValues
				.get(Weather_Column.WEATHER_ICON_DAY7)));
		int[] intArray = { curWeathericon, weatherIconDay1, weatherIconDay2,
				weatherIconDay3, weatherIconDay4, weatherIconDay5,
				weatherIconDay6,weatherIconDay7 };
		if(DEBUG)Log.i("locationapp", "getWeatherIconArray()  intArray===="+intArray);
		return intArray;
	}
	
	private Bitmap getDrawBitmap(int weatherIcon) {
		if(DEBUG)Log.i("locationapp", "getDrawBitmap()  ====");
		Bitmap bitmap = null;
		Resources res = mContext.getResources();
		if (weatherIcon <= 8) {
			bitmap = BitmapFactory.decodeResource(res,(R.drawable.weather01 + (weatherIcon - 1)));//mBitmapArray1_8[weatherIcon - 1];
		} else if (weatherIcon >= 29) {
			bitmap = BitmapFactory.decodeResource(res,(R.drawable.weather11 +(weatherIcon - 29)));//mBitmapArray29_44[weatherIcon - 29];
		} else if (weatherIcon <= 26 && weatherIcon >= 11) {
			bitmap = BitmapFactory.decodeResource(res,(R.drawable.weather29 +(weatherIcon - 11)));//mBitmapArray11_27[weatherIcon - 11];
		}
		
		return bitmap;
	}
	
	private Bitmap getSbDrawBitmap(int weatherIcon) {
		if(DEBUG)Log.i("locationapp", "getSbDrawBitmap()  ====");
		Bitmap bitmap = null;
		Resources res = mContext.getResources();
		if (weatherIcon <= 8) {
			bitmap = BitmapFactory.decodeResource(res,(R.drawable.zzz_weather01 + (weatherIcon - 1)));//mSbBitmapArray1_8[weatherIcon - 1];
		} else if (weatherIcon >= 29) {
			bitmap = BitmapFactory.decodeResource(res,(R.drawable.zzz_weather11 +(weatherIcon - 29)));//mSbBitmapArray29_44[weatherIcon - 29];
		} else if (weatherIcon <= 26 && weatherIcon >= 11) {
			bitmap = BitmapFactory.decodeResource(res,(R.drawable.zzz_weather29 +(weatherIcon - 11)));//mSbBitmapArray11_27[weatherIcon - 11];
		}
		return bitmap;
	}
	private int[] getTempArray(ContentValues contentValues) {
		if(DEBUG)Log.i("locationapp", "getTempArray()  ====");
		int curTemp = 0;
		int highTempDay1 = 0;
		int lowTempDay1 = 0;
		int highTempDay2 = 0;
		int lowTempDay2 = 0;
		int highTempDay3 = 0;
		int lowTempDay3 = 0;
		int highTempDay4 = 0;
		int lowTempDay4 = 0;
		int highTempDay5 = 0;
		int lowTempDay5 = 0;
		int highTempDay6 = 0;
		int lowTempDay6 = 0;
		int highTempDay7 = 0;
		int lowTempDay7 = 0;
		int metric =1;// (Integer) contentValues.get(Weather_Column.METRIC);
		switch (metric) {
		case 1: // \u6444\u6c0f\u5ea6?
			curTemp = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.CURRENT_TEMPERATURE_C)));

			highTempDay1 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY1_C)));
			lowTempDay1 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY1_C)));

			highTempDay2 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY2_C)));
			lowTempDay2 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY2_C)));

			highTempDay3 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY3_C)));
			lowTempDay3 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY3_C)));

			highTempDay4 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY4_C)));
			lowTempDay4 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY4_C)));

			highTempDay5 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY5_C)));
			lowTempDay5 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY5_C)));

			highTempDay6 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY6_C)));
			lowTempDay6 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY6_C)));
			highTempDay7 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY7_C)));
			lowTempDay7 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY7_C)));
			break;
		case 2://\u534e\u6c0f\u5ea6?
			curTemp = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.CURRENT_TEMPERATURE_F)));

			highTempDay1 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY1_F)));
			lowTempDay1 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY1_F)));

			highTempDay2 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY2_F)));
			lowTempDay2 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY2_F)));

			highTempDay3 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY3_F)));
			lowTempDay3 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY3_F)));

			highTempDay4 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY4_F)));
			lowTempDay4 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY4_F)));

			highTempDay5 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY5_F)));
			lowTempDay5 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY5_F)));

			highTempDay6 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY6_F)));
			lowTempDay6 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY6_F)));
			highTempDay7 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.HIGH_TEMPERATURE_DAY7_F)));
			lowTempDay7 = Integer.parseInt(String.valueOf(contentValues
					.get(Weather_Column.LOW_TEMPERATURE_DAY7_F)));
			break;
		}
		int[] intArray = { curTemp, highTempDay1, lowTempDay1, highTempDay2,
				lowTempDay2, highTempDay3, lowTempDay3, highTempDay4,
				lowTempDay4, highTempDay5, lowTempDay5, highTempDay6,
				lowTempDay6,highTempDay7,lowTempDay7 };
		return intArray;
	}
	private int getTodayWeather(int[] weatherIconArray){
		int mTodayWeather=R.string.txt_sunny_day;
		mRainfallProbability = mContext.getString(R.string.rainfall_probability,"0");
		if(weatherIconArray[0]==2||
			weatherIconArray[0]==3||
			weatherIconArray[0]==4||
			weatherIconArray[0]==6||
			weatherIconArray[0]==34||
			weatherIconArray[0]==35||
			weatherIconArray[0]==36||
			weatherIconArray[0]==38){
			mTodayWeather=R.string.txt_cloudy_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"20");
		}else if(weatherIconArray[0]==7||
				weatherIconArray[0]==8){
			mTodayWeather=R.string.txt_shade_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"50");
		}else if(weatherIconArray[0]==11){
			mTodayWeather=R.string.txt_fog_day;   
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"30");
		}else if(weatherIconArray[0]==12||
				weatherIconArray[0]==18||
				weatherIconArray[0]==25){
			mTodayWeather=R.string.txt_rain_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"90");
		}else if(weatherIconArray[0]==13||
				weatherIconArray[0]==14||
				weatherIconArray[0]==39||
				weatherIconArray[0]==40){
			mTodayWeather=R.string.txt_sunny_to_rain_day;  
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"80");
		}else if(weatherIconArray[0]==15){
			mTodayWeather=R.string.txt_thundershower_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"90");
		}else if(weatherIconArray[0]==16||
				weatherIconArray[0]==17||
				weatherIconArray[0]==41||
				weatherIconArray[0]==42){
			mTodayWeather=R.string.txt_sunny_to_thundershower_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"80");
		}else if(weatherIconArray[0]==19||
				weatherIconArray[0]==22||
				weatherIconArray[0]==24){
			mTodayWeather=R.string.txt_snow_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"50");
		}else if(weatherIconArray[0]==20||
		              weatherIconArray[0]==21||
		              weatherIconArray[0]==23||
		              weatherIconArray[0]==43||
		              weatherIconArray[0]==44){
			mTodayWeather=R.string.txt_sunny_to_snow_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"40");
		}else if(weatherIconArray[0]==26||   
				weatherIconArray[0]==29){
			mTodayWeather=R.string.txt_rain_and_snow_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"70");
		}else if(weatherIconArray[0]==31){
			mTodayWeather=R.string.txt_ice_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"20");
		}else if(weatherIconArray[0]==32){
			mTodayWeather=R.string.txt_windy_day;
			mRainfallProbability = mContext.getString(R.string.rainfall_probability,"10");
		}
		return mTodayWeather;
	}
	private void setTextViewText(int id,String text){
		if(DEBUG)Log.i("locationapp", "setTextViewText()  ====");
		Message msg = mHandler.obtainMessage(0);
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		bundle.putString("text", text);
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}
	
	private void setImageViewBitmap(int id,Bitmap  bm){
		if(DEBUG)Log.i("locationapp", "setImageViewBitmap()  ====");
		Message msg = mHandler.obtainMessage(1);
		Bundle bundle = new Bundle();
		bundle.putInt("id", id);
		bundle.putByteArray("bitmap", getBytes(bm));
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}
	Handler mHandler=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(DEBUG)Log.i("locationapp", "handleMessage()  ====msg.what"+msg.what);
			switch (msg.what) {
			case 0:
				Bundle bundle = msg.getData();
				int id1 = bundle.getInt("id");
				String text = bundle.getString("text");
				TextView tv=(TextView)findViewById(id1);
				tv.setText(text);
				break;
			case 1:
				Bundle bundler = msg.getData();
				int id2 = bundler.getInt("id");
				Bitmap bm =getBitmap( bundler.getByteArray("bitmap"));
				ImageView img=(ImageView)findViewById(id2);
				img.setImageBitmap(bm);
			
				break;
			}
		}
	};  
	
	 private  byte[] getBytes(Bitmap bitmap){  
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	    	bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
	    	return baos.toByteArray();
 	}
	private  Bitmap getBitmap(byte[] data){  
	      return BitmapFactory.decodeByteArray(data, 0, data.length);//\u4ece\u5b57\u8282\u6570\u7ec4\u89e3\u7801\u4f4d\u56fe  
	}  

        private OnClickListener mWeatherlistener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                              
			      if(mWeatherContentView!=null && mFutureWeatherView!=null && mWeatherExpandOrHide!=null){
				  	mWeatherExpandOrHide.setEnabled(false);
	                            mWeatherIsExpand=!mWeatherIsExpand;
					mWeatherExpandOrHide.setText(mWeatherIsExpand?R.string.siri_suggest_hide:R.string.siri_suggest_expand);
					ResizeAnimation a = new ResizeAnimation(mWeatherContentView);
			               a.setDuration(500);
					int startHeight=mWeatherContentView.getHeight();
				       int endHeight=mWeatherIsExpand?(startHeight+194):(startHeight-194);		
					a.setParams(startHeight, endHeight);
					mWeatherContentView.startAnimation(a);
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
							mFutureWeatherView.setVisibility(mWeatherIsExpand?View.VISIBLE:View.GONE);
							mWeatherContentDivide.setVisibility(mWeatherIsExpand?View.VISIBLE:View.GONE);
							mWeatherExpandOrHide.setEnabled(true);
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
