package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.AppWidgetScrollView.OnScrollListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import android.text.format.DateFormat;
import java.util.Date;
import java.util.Locale;//added by xss for ios10
//import android.os.SystemProperties;//added by xss for ios10 map
//import com.android.keyguard.KeyguardStatusView;
//import com.hskj.iphoneutil.FeatureOption;

public class ApplicationPreview extends LinearLayout implements OnClickListener,OnScrollListener,OnTouchListener{
	String TAG="ApplicationPreview";
	boolean DEBUG = false;
       private Context mContext;
	private Intent mAppPreviewIntent;
	private boolean isLockScreen=false;
       private boolean inLockScreen=false;
	private boolean mStatusBarViewBgIsChanged=false; 
	private boolean mClockViewIsShow=false;
	private boolean mKeyguardStatusViewIsShow=true;
	private int mScrollY=0;
	//public String[] siriSuggestAppPackageName;
	private ArrayList<String> mWidgetAddData=new ArrayList<String>();
	private ArrayList<String> mOtherWidgetData=new ArrayList<String>();
	private AppWidgetScrollView mAppWidgetScrollView;
	private LinearLayout mAppPreviewContent;
	private AppPreviewDate mDateView;
//	private KeyguardStatusView mKeyguardStatusView;//dateTimeView
	private PhoneStatusBar mPhoneStatusBar;
//	private Search mSearch;
	private AppPreviewWeather mAppPreviewWeather;
	private AppPreviewCalendarNotification mAppPreviewCalendarNotification;
	private AppPreviewSiri mAppPreviewSiri;
	private AppPreviewCalendarEvent mAppPreviewCalendarEvent;
	private AppPreviewStockMarket mAppPreviewStockMarket;
	private AppPreviewMemo mAppPreviewMemo;
	private AppPreviewEdit mAppPreviewEdit;
	private AppPreviewReminder mAppNote;//added by cfb
	private View mAppMapsDestinations,mAppFavorites,mAppMail,mAppMapsNearby,mAppMapsTransit,mAppPhotosMemories,mAppTips
		,mAppFriends,mAppMusic,mOtherAppWidgetCountView;
	private int mWeatherID=-1,mCalendarNotificationID=-1,mCalendarEventID=-1,mMemoID=-1,mSiriID=-1,mMapDestinationsID=-1,mStockMarketID=-1,mRemindersID=-1;//modified by xss for ios10 map
	private TextView mOtherAppWidgetCount,mOtherAppWidgetCountText,mWeatherInfoFrom,mStocketInfoFrom,mMapDestinationsEmptyText;//modified by xss for ios10 map
       private int otherAppWidgetCount = 0;
	
	public ApplicationPreview(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

       public ApplicationPreview(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
	    }

	    public ApplicationPreview(Context context, AttributeSet attrs, int defStyle) {
	        super(context, attrs, defStyle);
	        /// M: Init customize clock plugin
	       
	    }
	    
	 public void setContext(Context context,boolean lock ){
	 	
               mContext = context;
		 LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	
		 mDateView = (AppPreviewDate) inflater.inflate(R.layout.unlock_screen_date_view, null);
//		 mKeyguardStatusView= (KeyguardStatusView) inflater.inflate(R.layout.keyguard_status_view_left, null);
		 mAppPreviewWeather = (AppPreviewWeather) inflater.inflate(R.layout.unlock_screen_weather_view, null);
		 mAppPreviewCalendarNotification = (AppPreviewCalendarNotification) inflater.inflate(R.layout.unlock_screen_calendar_notifations_view, null);
		 mAppPreviewSiri = (AppPreviewSiri ) inflater.inflate(R.layout.unlock_screen_siri_view, null);
		 mAppPreviewCalendarEvent = (AppPreviewCalendarEvent) inflater.inflate(R.layout.unlock_screen_calendar_event_view, null);
		 mAppPreviewStockMarket = (AppPreviewStockMarket ) inflater.inflate(R.layout.unlock_screen_stock_market_view, null);
		 mAppPreviewMemo = (AppPreviewMemo) inflater.inflate(R.layout.unlock_screen_memo_view, null);
		 mAppMapsDestinations = inflater.inflate(R.layout.unlock_screen_maps_destinations_view, null);
		 mAppFavorites = inflater.inflate(R.layout.unlock_screen_favorites_view, null);
		 mAppMail = inflater.inflate(R.layout.unlock_screen_mail_view, null);
		 mAppMapsNearby = inflater.inflate(R.layout.unlock_screen_maps_nearby_view, null);
		 mAppMapsTransit = inflater.inflate(R.layout.unlock_screen_maps_transit_view, null);
		 mAppPhotosMemories = inflater.inflate(R.layout.unlock_screen_photos_memories_view, null);
		 mAppTips = inflater.inflate(R.layout.unlock_screen_tips_view, null);
		 mAppNote = (AppPreviewReminder) inflater.inflate(R.layout.unlock_screen_note_view, null);
		 mAppFriends = inflater.inflate(R.layout.unlock_screen_friends_view, null);
		 mAppMusic = inflater.inflate(R.layout.unlock_screen_music_view, null);
		 mAppPreviewEdit = (AppPreviewEdit) inflater.inflate(R.layout.unlock_screen_edit, null);

               mAppPreviewWeather.setContext(context);
		 mAppPreviewCalendarNotification.setContext(context);
		 mAppPreviewSiri.setContext(context);
		 mAppPreviewCalendarEvent.setContext(context);
		 mAppPreviewStockMarket.setContext(context);
		 mAppPreviewMemo.setContext(context);
		 mAppPreviewEdit.setContext(context);
		 mAppNote.setContext(context);
		 
                mAppPreviewEdit.setCalendarIcon(mAppPreviewCalendarNotification.getCalendarIcon());
		 isLockScreen(lock);
		 showAppWidget(context);
		
	}

        public void initData(Context context){
	 	  mWidgetAddData = getAddData(context);
		  if(DEBUG)Log.i(TAG, "ApplicationPreview  initData()  mWidgetAddData===="+mWidgetAddData);
		  if(DEBUG)Log.i("widget_edit", "ApplicationPreview  initData()  mWidgetAddData===="+mWidgetAddData);
		  if(mWidgetAddData.size()==0){
                       String [] Data= getResources().getStringArray(
				R.array.added_widget_app_name);
			  for(String data:Data){
			  	   if(DEBUG)Log.i(TAG, "ApplicationPreview  initData()  data===="+data);
	                        mWidgetAddData.add(data);
			  }
			  String [] otherData= getResources().getStringArray(
				R.array.other_widget_app_name);
			  otherAppWidgetCount = otherData.length;
		  }
                	
        }
	   
	 private ArrayList<String> getAddData(Context  context){		
		ArrayList<String> List = new ArrayList<String>();
		/*SharedPreferences preferDataList = getSharedPreferences("AppPreviewAddData", 0);
		int environNums = preferDataList.getInt("AddDataSize", 0);
		for (int i = 0; i < environNums; i++) 
		{
		    String environItem = preferDataList.getString("item_"+i, null);
		    List.add(environItem);
		}*/
		int size = Settings.System.getInt(context.getContentResolver(), "app_preview_size",0);
		if(DEBUG)Log.i(TAG, "  getAddData()  size===="+size);
		for (int i = 0; i < size; i++) 
		{
		    String item = Settings.System.getString(context.getContentResolver(), "app_preview"+i);
		    if(DEBUG)Log.i(TAG, "  getAddData()    i=="+i+"   item===="+item); 	
		    List.add(item);
		}
		return List;
	}

      public void showAppWidget(Context context){
	       initData(context);	

              mAppPreviewIntent= null;//added by xss for ios10 map
		   
	      mAppPreviewContent.removeAllViewsInLayout();
	      mWeatherID=-1;
	      mCalendarNotificationID=-1;
	      mCalendarEventID=-1;
	      mMemoID=-1;
	      mSiriID=-1;
             mStockMarketID = -1;//added by xss for ios10  Market
             mMapDestinationsID=-1;//added by xss for ios10 map
	      mRemindersID=-1;
		  
	      int appWidgetWidth = 692;
	      int appWidgetMargin = 14;
	     //if(isLockScreen){  
		     /* LayoutParams mKeyguardStatusViewLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
	             mKeyguardStatusViewLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin+20);//(LinearLayout.LayoutParams)mAppPreviewWeather.getLayoutParams();
		      if(DEBUG)Log.i(TAG, "ApplicationPreview  showAppWidget() mKeyguardStatusViewLp=="+mKeyguardStatusViewLp);  
		      mAppPreviewContent.addView(mKeyguardStatusView,mKeyguardStatusViewLp);*/
	      //}else{
	            /*String date =((TextView)mKeyguardStatusView.getDateView()).getText().toString();
		     if(DEBUG)Log.i(TAG, "ApplicationPreview  showAppWidget() date=="+date+"   date.indexOf(,)="+date.indexOf(","));
		     date = date.replace(",",",\n");
		     String  day = mContext.getResources().getString(R.string.date_day);//added by xss for ios10
		     date = date.replace(day,day+" ");  	 //added by xss for ios10
		     if(DEBUG)Log.i(TAG, "ApplicationPreview  showAppWidget() date=="+date);
		     Settings.System.putString(mContext.getContentResolver(),"app_widget_date_view_text",date);	 */
	            mDateView.getDateText().setText(getCurrentDate());
		     /*Begin:added by xss for ios10 lunarDate*/		
		     String lunardate = Settings.System.getString(mContext.getContentResolver(),"app_widget_lunar_date_view_text");//added by xss for ios10 lunarDate 
	            mDateView.getLunarDateText().setText(lunardate);   //added by xss for ios10 lunarDate
		     /*End:added by xss for ios10 lunarDate*/		
                    LinearLayout.LayoutParams mDateViewLp = new LinearLayout.LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);  
	              mDateViewLp.setMargins(14, 14, 14, 24);//(LinearLayout.LayoutParams)mAppPreviewWeather.getLayoutParams();
		       mAppPreviewContent.addView(mDateView,mDateViewLp);
	      //}
          if(DEBUG)Log.i(TAG, "  showAppWidget()  mWidgetAddData.size()===="+mWidgetAddData.size());

          for(int i =0; i<mWidgetAddData.size(); i++){
			 if(DEBUG)Log.i(TAG, "  showAppWidget()  mWidgetAddData.get(i)===="+mWidgetAddData.get(i));  
                       /*switch(mWidgetAddData.get(i)){*/
	                     /*case*/
		  if(mWidgetAddData.get(i).equals("Weather")) {
                     mWeatherID = i + 1;
                     LayoutParams mWeatherLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mWeatherLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppPreviewWeather.getLayoutParams();
                     mAppPreviewContent.addView(mAppPreviewWeather, mWeatherLp);
                 }else if(mWidgetAddData.get(i).equals("Up Next")) {
                  if(DEBUG)Log.i(TAG, "ApplicationPreview  showAppWidget()  ======== Up Next=="+mAppPreviewCalendarNotification);
                  mCalendarNotificationID = i + 1;
                     LayoutParams mCalendarNotificationsLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mCalendarNotificationsLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppPreviewCalendarNotification.getLayoutParams();
                     mAppPreviewContent.addView(mAppPreviewCalendarNotification, mCalendarNotificationsLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Siri App Suggestions")) {
                     mSiriID = i + 1;
                     LayoutParams mSiriLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mSiriLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppPreviewSiri.getLayoutParams();
                     mAppPreviewContent.addView(mAppPreviewSiri, mSiriLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Maps Destinations")) {

                     mMapDestinationsID = i + 1;
                     LayoutParams mAppMapsDestinationsLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppMapsDestinationsLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppMapsDestinations.getLayoutParams();
                     mAppPreviewContent.addView(mAppMapsDestinations, mAppMapsDestinationsLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Stocks")) {

                     mStockMarketID = i + 1;
                     LayoutParams mAppPreviewStockMarketLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppPreviewStockMarketLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppPreviewStockMarket.getLayoutParams();
                     mAppPreviewContent.addView(mAppPreviewStockMarket, mAppPreviewStockMarketLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Favorites")) {

                     LayoutParams mAppFavoritesLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppFavoritesLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppFavorites.getLayoutParams();
                     mAppPreviewContent.addView(mAppFavorites, mAppFavoritesLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Mail")) {

                     LayoutParams mAppMailLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppMailLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppMail.getLayoutParams();
                     mAppPreviewContent.addView(mAppMail, mAppMailLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Maps Nearby")) {
                     LayoutParams mAppMapsNearbyLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppMapsNearbyLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppMapsNearby.getLayoutParams();
                     mAppPreviewContent.addView(mAppMapsNearby, mAppMapsNearbyLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Maps Transit")) {

                     LayoutParams mAppMapsTransitLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppMapsTransitLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppMapsTransit.getLayoutParams();
                     mAppPreviewContent.addView(mAppMapsTransit, mAppMapsTransitLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Photos Memories")) {

                     LayoutParams mAppPhotosMemoriesLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppPhotosMemoriesLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppPhotosMemories.getLayoutParams();
                     mAppPreviewContent.addView(mAppPhotosMemories, mAppPhotosMemoriesLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Notes")) {

                     mMemoID = i + 1;
                     LayoutParams mAppPreviewMemoLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppPreviewMemoLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppPreviewMemo.getLayoutParams();
                     mAppPreviewContent.addView(mAppPreviewMemo, mAppPreviewMemoLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Tips")) {

                     LayoutParams mAppTipsLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppTipsLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppTips.getLayoutParams();
                     mAppPreviewContent.addView(mAppTips, mAppTipsLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Reminders")) {

					mRemindersID= i+1;
					LayoutParams mAppNoteLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);  
                                    mAppNoteLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppNote.getLayoutParams();
				       mAppPreviewContent.addView(mAppNote,mAppNoteLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Calendar")) {

                     mCalendarEventID = i + 1;
                     LayoutParams mAppPreviewCalendarEventLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppPreviewCalendarEventLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppPreviewCalendarEvent.getLayoutParams();
                     mAppPreviewContent.addView(mAppPreviewCalendarEvent, mAppPreviewCalendarEventLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Find Friends")) {

                     LayoutParams mAppFriendsLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppFriendsLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppFriends.getLayoutParams();
                     mAppPreviewContent.addView(mAppFriends, mAppFriendsLp);
                 }
                 else if(mWidgetAddData.get(i).equals("Music")) {

                     LayoutParams mAppMusicLp = new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
                     mAppMusicLp.setMargins(appWidgetMargin, 0, appWidgetMargin, appWidgetMargin);//(LinearLayout.LayoutParams)mAppMusic.getLayoutParams();
                     mAppPreviewContent.addView(mAppMusic, mAppMusicLp);

			}
			
	      }
		LayoutParams layoutParams= new LayoutParams(appWidgetWidth, LinearLayout.LayoutParams.WRAP_CONTENT);  	 
		layoutParams.setMargins(appWidgetMargin,70, appWidgetMargin,70); 
		mAppPreviewContent.addView(mAppPreviewEdit,layoutParams);
		mAppPreviewContent.requestLayout();
		if(DEBUG)Log.i(TAG, "ApplicationPreview  showAppWidget()  ======== mAppPreviewContent.getChildCount()=="+mAppPreviewContent.getChildCount());  

              if(mMapDestinationsID!=-1){//added by xss for ios10 map
                     mMapDestinationsEmptyText = (TextView)mAppPreviewContent.getChildAt(mMapDestinationsID).findViewById(R.id.map_destinations_empty_content_text);
			if(mMapDestinationsEmptyText!=null)
                  mMapDestinationsEmptyText.setOnClickListener(this);
	        } 

		int isFistShow = Settings.System.getInt(mContext.getContentResolver(),"other_widget_count",-1);   
              if(isFistShow==-1){
			mOtherAppWidgetCountView.setVisibility(View.VISIBLE);
			mOtherAppWidgetCount.setText(String.valueOf(otherAppWidgetCount));
	       }else{
                     mOtherAppWidgetCountView.setVisibility(View.GONE);  
		}	
      }

      public void getAppWeather(){
	  	if(DEBUG)Log.i(TAG, "ApplicationPreview  getAppWeather()  ======== mWeatherID=="+mWeatherID);  
              if(mWeatherID!=-1)((AppPreviewWeather)mAppPreviewContent.getChildAt(mWeatherID)).getWeather();
      }

      public void memoStartQuery(){
	  	if(DEBUG)Log.i(TAG, "ApplicationPreview  memoStartQuery()  ========");  
              if(mMemoID!=-1)((AppPreviewMemo)mAppPreviewContent.getChildAt(mMemoID)).startQuery();
       }

      public void calendarEventChanged(){
	  	if(DEBUG)Log.i(TAG, "ApplicationPreview  calendarEventChanged()  ========");  
	  	if(mCalendarNotificationID!=-1)((AppPreviewCalendarNotification)mAppPreviewContent.getChildAt(mCalendarNotificationID)).eventsChanged();
              if(mCalendarEventID!=-1)((AppPreviewCalendarEvent)mAppPreviewContent.getChildAt(mCalendarEventID)).eventsChanged();
       }
	public void calendarEventUpdated(){
		if(DEBUG)Log.i(TAG, "ApplicationPreview  calendarEventUpdate()  ========");
		if(mCalendarNotificationID!=-1){  
		  	((AppPreviewCalendarNotification)mAppPreviewContent.getChildAt(mCalendarNotificationID)).updateTomorrowSchedule();
			((AppPreviewCalendarNotification)mAppPreviewContent.getChildAt(mCalendarNotificationID)).updateTodaySchedule();
		}	
		if(mCalendarEventID!=-1){
	              ((AppPreviewCalendarEvent)mAppPreviewContent.getChildAt(mCalendarEventID)).updateTomorrowSchedule();
		       ((AppPreviewCalendarEvent)mAppPreviewContent.getChildAt(mCalendarEventID)).updateTodaySchedule();
		}
       }	  

	public void showKeyguardStatusView(boolean show){
              if(isLockScreen)mAppPreviewContent.getChildAt(0).setVisibility(show? View.VISIBLE:View.INVISIBLE);
	}

       public void setKeyguardStatusViewBatteryText(String text){//added by xss for ios10 battery
//              if(isLockScreen)((KeyguardStatusView)mAppPreviewContent.getChildAt(0)).setBatteryText(text);
	}

	public void showKeyguardStatusViewBatteryTextView(boolean show){//added by xss for ios10 battery
//              if(isLockScreen)((KeyguardStatusView)mAppPreviewContent.getChildAt(0)).showBatteryView(show);
	}

       public void delayHideKeyguardStatusViewBatteryView(boolean inChargeMode){//added by xss for ios10 battery
//              if(isLockScreen)((KeyguardStatusView)mAppPreviewContent.getChildAt(0)).delayHideBatteryView(inChargeMode);
	}
	
	   
    @Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		mAppWidgetScrollView = (AppWidgetScrollView)findViewById(R.id.app_widget_scrollview);
		mAppWidgetScrollView.setOnScrollListener(this);
		mAppWidgetScrollView.setOnTouchListener(this);
		mAppPreviewContent= (LinearLayout)findViewById(R.id.unlock_screen_app_preview_content);
		mOtherAppWidgetCountView = (View)findViewById(R.id.other_app_widget_count_view);
		mOtherAppWidgetCount = (TextView)findViewById(R.id.other_app_widget_count);
		mOtherAppWidgetCountText = (TextView)findViewById(R.id.other_app_widget_count_text);
		mWeatherInfoFrom= (TextView)findViewById(R.id.weather_info_from);
		mStocketInfoFrom= (TextView)findViewById(R.id.stocket_info_from);
	}

      
	   
    @Override
    public void onClick(View view) {
		// TODO Auto-generated method stub
		/*Begin:added by xss for ios10 map*/
               final Intent homePressedIntent = new Intent("home_key_is_pressed");  
               int id = view.getId();
		switch(id){
                    case R.id.map_destinations_empty_content_text:
				try{		
					/*if(SystemProperties.get("cenon.soft.mode").equals("1")){
	                                   mAppPreviewIntent = new Intent(Intent.ACTION_MAIN);
			                    mAppPreviewIntent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity"); 
					}else{*/
	                                   mAppPreviewIntent = getContext().getPackageManager().getLaunchIntentForPackage("com.autonavi.minimap");
//					}
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
		/*End:added by xss for ios10 map*/
    } 
		
   Handler myHandler= new Handler();
	Runnable myRunnable=new Runnable() {
		
		@Override
		public void run() {
                     mContext.sendBroadcast(new Intent("home_key_is_pressed"));
		}
	};
	
   
    @Override  
    public void onScroll(int scrollY) {
           if(DEBUG)Log.i(TAG, "onScroll() isLockScreen="+isLockScreen+"    scrollY="+scrollY);
	    mScrollY =  scrollY;	  
           if(isLockScreen){
		    if(scrollY >=30){
			   
		          if( !mStatusBarViewBgIsChanged){
				  mStatusBarViewBgIsChanged = true;	
				  mPhoneStatusBar.setStatusBarBg(mContext.getResources().getColor(R.color.search_plate_color));
//				  mSearch.setSearchPlateBackgroundColor(mContext.getResources().getColor(R.color.search_plate_color));
				  if(DEBUG)Log.i(TAG, "onScroll()   mStatusBarViewBgIsChanged="+mStatusBarViewBgIsChanged);
		          }
			   if(scrollY >=300){
                               if(!mClockViewIsShow){
                                       mClockViewIsShow = true;
                                       mPhoneStatusBar.showClock(mClockViewIsShow/*, true*/);
				   }
			   }else{
                                if(mClockViewIsShow){
                                       mClockViewIsShow = false;
                                       mPhoneStatusBar.showClock(mClockViewIsShow/*, true*/);
				   }
			   }
			   	
		    }else{
		          if(mStatusBarViewBgIsChanged){
				  mStatusBarViewBgIsChanged = false;	
		                mPhoneStatusBar.setStatusBarBg(0x00000000);
//				  mSearch.setSearchPlateBackgroundColor(0x00000000);
				  if(DEBUG)Log.i(TAG, "onScroll()   mStatusBarViewBgIsChanged="+mStatusBarViewBgIsChanged);		
		          }
		    }  
           }
    }  

    @Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if(DEBUG)Log.i(TAG, "onTouch()  event.getAction()="+event.getAction()+"   mScrollY=="+mScrollY);
		if(event.getAction() == MotionEvent.ACTION_UP ||event.getAction() == MotionEvent.ACTION_CANCEL){
                       if(DEBUG)Log.i(TAG, "onTouch()  event.getAction()=====up");  
			 if(mScrollY<153){
			 	 mKeyguardStatusViewIsShow = true;
                              mAppWidgetScrollView.scrollTo(0, 0);
			 }else if(mScrollY>=153 && mScrollY<306){
			        mKeyguardStatusViewIsShow = false;
                              mAppWidgetScrollView.scrollTo(0, 306);
			 }else{
                              mKeyguardStatusViewIsShow = false;
			 }
			 detectScrollY(); //added by xss for ios10
		}
		return false;
	}

       public void detectScrollY(){  //added by xss for ios10
            new Handler().postDelayed(new Runnable(){  
                @Override  
                public void run() {  
                    int tempScrollY = mAppWidgetScrollView.getScrollY();  
                    if(DEBUG)Log.i(TAG, "onTouch()  event.getAction()=====up    tempScrollY="+tempScrollY);  
			 if(tempScrollY<153){
			 	 mKeyguardStatusViewIsShow = true;
                              mAppWidgetScrollView.scrollTo(0, 0);
			 }else if(tempScrollY>=153 && tempScrollY<306){
			        mKeyguardStatusViewIsShow = false;
                              mAppWidgetScrollView.scrollTo(0, 306);
			 }else{
                              mKeyguardStatusViewIsShow = false;
			 }		
                }  
            }, 300);  
        }  


       public boolean keyguardStatusViewIsShow(){
	   	if(DEBUG)Log.i(TAG, "keyguardStatusViewIsShow()  mKeyguardStatusViewIsShow="+mKeyguardStatusViewIsShow);
                return mKeyguardStatusViewIsShow;
	}
 
     public void showClock(boolean show){
	     if(DEBUG)Log.i(TAG, "showClock()  mClockViewIsShow="+mClockViewIsShow+"  show="+show); 
            if(mClockViewIsShow)mPhoneStatusBar.showClock(show/*, true*/);
     }   
     public void setPhoneStatusBar(PhoneStatusBar bar){
            mPhoneStatusBar = bar; 
     }

    /* public void setSearch(Search search){
             mSearch = search;
     }*/

     private void isLockScreen(boolean lock){
               isLockScreen=lock;
	        mAppPreviewWeather.isLockScreen(lock); 
		 mAppPreviewCalendarNotification.isLockScreen(lock); 
		 mAppPreviewSiri.isLockScreen(lock); 
		 mAppPreviewCalendarEvent.isLockScreen(lock); 
		 mAppPreviewStockMarket.isLockScreen(lock); 
		 mAppPreviewMemo.isLockScreen(lock); 
		 mAppPreviewEdit.isLockScreen(lock);
		 mAppNote.isLockScreen(lock);
	    // add by csc on 20161012
         if(lock){
             LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
             lp.setMargins(0, 40, 0, 0);
             mAppWidgetScrollView.setLayoutParams(lp);
         }else {
             LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
             lp.setMargins(0, -65, 0, 0);
             mAppWidgetScrollView.setLayoutParams(lp);
         }
     }
	   
     public void lockScreen(){
		 inLockScreen=true;
		 mAppPreviewIntent = null;
                if(mWeatherID!=-1)((AppPreviewWeather)mAppPreviewContent.getChildAt(mWeatherID)).lockScreen(); 
	         if(mCalendarNotificationID!=-1)((AppPreviewCalendarNotification)mAppPreviewContent.getChildAt(mCalendarNotificationID)).lockScreen(); 
	         if(mCalendarEventID!=-1)((AppPreviewCalendarEvent)mAppPreviewContent.getChildAt(mCalendarEventID)).lockScreen(); 
	         if(mMemoID!=-1)((AppPreviewMemo)mAppPreviewContent.getChildAt(mMemoID)).lockScreen(); 
	         if(mSiriID!=-1)((AppPreviewSiri)mAppPreviewContent.getChildAt(mSiriID)).lockScreen(); 
		 if(mStockMarketID!=-1)((AppPreviewStockMarket)mAppPreviewContent.getChildAt(mStockMarketID)).lockScreen();//added by xss for ios10 market	 
		  if(mRemindersID!=-1)((AppPreviewReminder)mAppPreviewContent.getChildAt(mRemindersID)).lockScreen();
		  if(mAppPreviewContent.getChildAt(mWidgetAddData.size()+1) instanceof AppPreviewEdit)((AppPreviewEdit)mAppPreviewContent.getChildAt(mWidgetAddData.size()+1)).lockScreen();
                if(mPhoneStatusBar!=null /*&& mSearch!=null*/){
			 mClockViewIsShow = false;
	                mPhoneStatusBar.showClock(mClockViewIsShow/*, true*/);
			 mStatusBarViewBgIsChanged = false;	
			 mPhoneStatusBar.setStatusBarBg(0x00000000);
//			 mSearch.setSearchPlateBackgroundColor(0x00000000);
	          }	
                // String date = Settings.System.getString(mContext.getContentResolver(),"app_widget_date_view_text");
		 // if(!isLockScreen){
                      ((AppPreviewDate)mAppPreviewContent.getChildAt(0)).getDateText().setText(getCurrentDate());
		  //}
			 
      }   

       public void setDateTextColor(int color){
               if(!isLockScreen) ((AppPreviewDate)mAppPreviewContent.getChildAt(0)).getDateText().setTextColor(color); 
		if(!isLockScreen) ((AppPreviewDate)mAppPreviewContent.getChildAt(0)).getLunarDateText().setTextColor(color); 	//added by  xss for ios10 lunarDate   
		mOtherAppWidgetCountText.setTextColor(color);
		 mWeatherInfoFrom.setTextColor(color);
		 mStocketInfoFrom.setTextColor(color);	   
	}

      public void updateWhiteTheme(boolean isWhite){
//	     if(isLockScreen)((KeyguardStatusView)mAppPreviewContent.getChildAt(0)).updateStatusViewTheme(isWhite);
	}
	 
	public void unLockScreen(){ 
	     if(DEBUG)Log.i(TAG, "unLockScreen()  mAppPreviewIntent="+mAppPreviewIntent+"  mWidgetAddData.size()="+mWidgetAddData.size());
	     inLockScreen=false;	 
	     if(mWeatherID!=-1)((AppPreviewWeather)mAppPreviewContent.getChildAt(mWeatherID)).unLockScreen(); 
	     if(mCalendarNotificationID!=-1)((AppPreviewCalendarNotification)mAppPreviewContent.getChildAt(mCalendarNotificationID)).unLockScreen();
	     if(mCalendarEventID!=-1)((AppPreviewCalendarEvent)mAppPreviewContent.getChildAt(mCalendarEventID)).unLockScreen(); 
	     if(mMemoID!=-1)((AppPreviewMemo)mAppPreviewContent.getChildAt(mMemoID)).unLockScreen(); 
	     if(mSiriID!=-1)((AppPreviewSiri)mAppPreviewContent.getChildAt(mSiriID)).unLockScreen();
	     if(mStockMarketID!=-1)((AppPreviewStockMarket)mAppPreviewContent.getChildAt(mStockMarketID)).unLockScreen();//added by xss for ios10 market
	      if(mRemindersID!=-1)((AppPreviewReminder)mAppPreviewContent.getChildAt(mRemindersID)).unLockScreen();
	     if(mAppPreviewContent.getChildAt(mWidgetAddData.size()+1) instanceof AppPreviewEdit)((AppPreviewEdit)mAppPreviewContent.getChildAt(mWidgetAddData.size()+1)).unLockScreen();
		 if(mPhoneStatusBar!=null /*&& mSearch!=null*/ && mStatusBarViewBgIsChanged){
	                mPhoneStatusBar.setStatusBarBg(0x00000000);
//			  mSearch.setSearchPlateBackgroundColor(0x00000000);
			  if(DEBUG)Log.i(TAG, "unLockScreen()   mStatusBarViewBgIsChanged="+mStatusBarViewBgIsChanged);		
	          }
	   /*Begin:added by xss for ios10 map*/	 
	    if(mAppPreviewIntent!=null){
			Intent intent = mAppPreviewIntent;
			mAppPreviewIntent = null;
                     getContext().startActivity(intent); 
           }
	   /*End:added by xss for ios10 map*/	
	        String lunardate = Settings.System.getString(mContext.getContentResolver(),"app_widget_lunar_date_view_text");//added by xss for ios10 lunarDate 
	       ((AppPreviewDate)mAppPreviewContent.getChildAt(0)).getLunarDateText().setText(lunardate);   //added by xss for ios10 lunarDate
	}
	private  byte[] getBytes(Bitmap bitmap){  
	    	ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	    	bitmap.compress(Bitmap.CompressFormat.PNG, 0, baos);
	    	return baos.toByteArray();
 	}
	private  Bitmap getBitmap(byte[] data){  
	      return BitmapFactory.decodeByteArray(data, 0, data.length);//\u4ece\u5b57\u8282\u6570\u7ec4\u89e3\u7801\u4f4d\u56fe  
	} 

	private String getCurrentDate(){
              CharSequence mDateFormatString = mContext.getResources().getText(com.android.internal.R.string.full_wday_month_day_no_year);
		String date = DateFormat.format(mDateFormatString, new Date()).toString();
		if(isZh(mContext))date = date.replace(" ","");   
	       if(DEBUG)Log.i(TAG, "ApplicationPreview  showAppWidget() date=="+date+"   date.indexOf(,)="+date.indexOf(","));
	      date = date.replace(",",",\n");
	      String  day = mContext.getResources().getString(R.string.date_day);//added by xss for ios10
	      date = date.replace(day,day+" "); 
	      return date;	  
	}
	
	public static boolean isZh(Context context) {  
	    Locale locale = context.getResources().getConfiguration().locale;  
	    String language = locale.getLanguage();  
	    if (language.endsWith("zh"))  
	        return true;  
	    else  
	        return false;  
     }  
}
