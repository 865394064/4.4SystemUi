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

public class AppPreviewCalendarEvent extends LinearLayout implements OnClickListener{
    String TAG="ApplicationPreview"; 
    boolean DEBUG = false;
    public Context mContext;
    public Intent mAppPreviewIntent;
    public boolean isLockScreen=false;
    public boolean inLockScreen=false;	
    public Drawable mCalendarIcon;

    private View mCalendarEventView;
    private ImageView mCalendarEventIcon;
    private TextView mCalendarEventEmptyView;
    private CalendarView mCalendarEventViewList;

     private List<Event> todays;
    private LinearLayout mCalendarNotificationsList;

    private int currIndex = 0;
    private CalendarView mCalendarView;
    private TextView nextScheduleText;
    private TextView tomorrowScheduleText;
    private long remainingMinutes=0;
    private long eventTime=0;
    private  StringBuilder titles=new StringBuilder();
    private  SimpleDateFormat nDateFormat = new SimpleDateFormat("H:mm");
    private int earlyEventId=0;
    private List<Event> todayEventsList;
    private int[] earlyEventsID;
    private List<Event> earlyTodayEvents =new ArrayList<Event>();  
    boolean isFirstShow=true;	
    public AppPreviewCalendarEvent(Context context) {
	 super(context);
         mContext = context;
    }

    public AppPreviewCalendarEvent(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
    }

    public AppPreviewCalendarEvent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /// M: Init customize clock plugin
       
    }
    public void setContext(Context context){
        mContext = context;
	updateCalendarIcon();		  
        if(isFirstShow)eventsChanged();
        isFirstShow=false;
    }
    
    @Override
     protected void onFinishInflate() {
	// TODO Auto-generated method stub
	super.onFinishInflate();		   
	//calendar event
	  mCalendarEventView = (View)findViewById(R.id.calendar_event_view);
	  mCalendarEventIcon = (ImageView)findViewById(R.id.calendar_event_icon);
	  mCalendarEventEmptyView = (TextView)findViewById(R.id.calendar_event_empty_text);
	  mCalendarEventViewList = (CalendarView)findViewById(R.id.calendar_event_list); 
	  mCalendarNotificationsList = (LinearLayout)findViewById(R.id.calendar_app);//added by scq
	  mCalendarNotificationsList.setOnClickListener(this);//added by scq
    }

    @Override
    public void onClick(View view) {
	// TODO Auto-generated method stub
	int id=view.getId();
	if(DEBUG)Log.i("locationapp", "onClick()  id===="+id);
	if(DEBUG)Log.i("locationapp", "onClick()  weather_app_name====");	   	   	
	final Intent homePressedIntent = new Intent("home_key_is_pressed");
	switch(id){
	       case R.id.calendar_app://Modify by scq
	           try{
			  //mContext.sendBroadcast(homePressedIntent); 	   	
			  mAppPreviewIntent=mContext.getPackageManager().getLaunchIntentForPackage("com.android.calendar");
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
	isLockScreen=lock;
    }   

     public void lockScreen(){
	 inLockScreen= true;
	 mAppPreviewIntent = null;
    } 
	
    public void unLockScreen(){
	 if(DEBUG)Log.i(TAG, "Calendar_Event  unLockScreen()  mAppPreviewIntent="+mAppPreviewIntent);
	 inLockScreen=false;
	if(mAppPreviewIntent!=null){
		Intent intent = mAppPreviewIntent;
		mAppPreviewIntent = null;
	     getContext().startActivity(intent);  
	}
    } 
    
    public void updateCalendarIcon(){
       final String date = String.valueOf(getLauncherDayOfMonth());
    	final String week = DateUtils.formatDateTime(mContext, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_WEEKDAY);				
	Drawable icon = mContext.getResources().getDrawable(R.drawable.app_calendar);
	mCalendarIcon =createCalendarIcon(date, week, icon, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());	
	if(mCalendarEventIcon!=null)mCalendarEventIcon.setBackground(mCalendarIcon);
    }

    public Drawable createCalendarIcon(String date, String week, Drawable background, int width, int height) {
    	int mCalendarWeakTextSize = 12;//(int) mContext.getResources().getInteger(R.integer.zzzz_calendar_weak_text_size);
    	int mCalendarWeakTextTop = (int) mContext.getResources().getDimension(R.dimen.zzzz_calerdar_weak_text_top);
    	int mCalendarDateTextSize = 30;//(int) mContext.getResources().getInteger(R.integer.zzzz_calendar_weak_date_size);
    	int mCalendarDateTextTop = (int) mContext.getResources().getDimension(R.dimen.zzzz_calerdar_weak_date_top);	
    	final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    	final Canvas canvas = new Canvas(bitmap);
    	background.setBounds(0, 0, width, height);
    	background.draw(canvas);
    	
    	final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    	paint.setColor(0xffff0000);
    	float textSize = TypedValue.applyDimension(
    			TypedValue.COMPLEX_UNIT_SP, mCalendarWeakTextSize, mContext.getResources().getDisplayMetrics());
    	paint.setTextSize(textSize);
    	float textWidth = paint.measureText(week);
    	canvas.drawText(week, width/2 - textWidth/2, mCalendarWeakTextTop, paint);
    	
    	paint.setColor(0xff000000);
    	textSize = TypedValue.applyDimension(
    			TypedValue.COMPLEX_UNIT_SP, mCalendarDateTextSize, mContext.getResources().getDisplayMetrics());
    	paint.setTextSize(textSize);
	paint.setTypeface(Typeface.createFromFile("/system/fonts/AndroidClock.ttf"));
    	//paint.setTypeface(Typeface.DEFAULT_BOLD);
    	textWidth = paint.measureText(date);
    	canvas.drawText(date, width/2 - textWidth/2, mCalendarDateTextTop, paint);
    	return new BitmapDrawable(mContext.getResources(), bitmap);
    }

    public static int getLauncherDayOfMonth() {
    	final Calendar calendar = Calendar.getInstance();   
    	calendar.setTime(new Date());  
        return calendar.get(Calendar.DAY_OF_MONTH);
    }
    	
   public void updateTodaySchedule(){
             //if(mCalendarView==null)return;	
             todayEventsList=mCalendarEventViewList.getTodayEvents();
             titles.delete(0, titles.length());
             if(DEBUG)Log.i(TAG,"Calendar_Event updateTodaySchedule()  todayEventsList="+todayEventsList.size()+" earlyTodayEvents=="+earlyTodayEvents.size());     
                 
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
		     		  
                    todays=null;
                    todays=getTodayEarlyEvents(todayEventsList, beginHour,earlyEventId);
                    Log.d("joyisn","today="+todays.size());
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
                   
                    updateToday(todays,beginHour);//updateToday(titles.toString(),beginHour);
                       
             }else{
                    
                    handler.removeCallbacks(runnable);
                    
              }
        }		 
	    public void updateToday(List<Event> todayEarlyEvents,long beginHour){//(String title,long beginHour)
	            int todayEarlyEventsCount=todayEarlyEvents.size();
		     boolean is24 =  DateFormat.is24HourFormat(mContext);
		     boolean isAm = todayEarlyEvents.get(0).getStartCalendar().get(Calendar.AM_PM)==0?true:false;	 
		     nDateFormat = new SimpleDateFormat(is24?"H:mm":"h:mm");
	            if(DEBUG)Log.i(TAG,"Calendar_Event  updateToday()----remainingMinutes="+remainingMinutes+"   is24=="+is24+"  isAm="+isAm+"    nDateFormat.format(beginHour)="+nDateFormat.format(beginHour));
	            if(remainingMinutes>0){
			      if(todayEarlyEventsCount==1){
				   
				    mCalendarEventEmptyView.setVisibility(View.GONE);
				    mCalendarEventViewList.setVisibility(View.VISIBLE);				    		
				    
			      }else if(todayEarlyEventsCount>1){
			          
				    mCalendarEventEmptyView.setVisibility(View.GONE);
				    mCalendarEventViewList.setVisibility(View.VISIBLE);	
    
			      }		
	                    
	              }else
	                    {   	                         
				   mCalendarEventEmptyView.setVisibility(View.VISIBLE);
				   mCalendarEventViewList.setVisibility(View.GONE);
           
		                if(todayEventsList!=null){
		                 for(int i=0;i<earlyTodayEvents.size();i++){
		                        Event event =earlyTodayEvents.get(i);
		                     for(int j=0;j<todayEventsList.size();j++){
		                            if((todayEventsList.get(j)).getEventId()==event.getEventId()){
		                                todayEventsList.remove(j);
						    todays.remove(j);			
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
                 updateToday(todays,eventTime);//modified (titles.toString(),eventTime) by xss for ios10 calendar notification
            }
        };
       
        private List<Event> getTodayEarlyEvents(List<Event> list,long earlyHour,int j){

          if(DEBUG)Log.i(TAG,"Calendar_Event getTodayEarlyEvents() list="+list.size());
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

        public void updateTomorrowSchedule(){
	     //if(mCalendarView==null)return;		
            List<Event> list=mCalendarEventViewList.getTomorrowEvents();
               if(list!=null){
                    switch(list.size()){
                        case 0:
                                  break;
                        case 1:

                                   
                                   Event event=list.get(0);
                                   String startTime=nDateFormat.format(event.getStartCalendar().getTime());
                                  break;
                        default:
                                    Calendar start=list.get(0).getStartCalendar();
                            	   long beginHour=start.getTimeInMillis();
                            	  for(int i=1;i<list.size();i++){
                                		long hour=list.get(i).getStartCalendar().getTimeInMillis();
                                		if(beginHour>hour)
                                		{
                                			beginHour=hour;
                                		}
                            		
                            	  }
                                  
                                 break;

                        }

                }
            }
   public void eventsChanged(){

	 if(mCalendarEventViewList!=null){
	      mCalendarEventViewList.clearEvents();
		mCalendarEventViewList.getEventsLists();
	 }
 	  updateTodaySchedule();
        updateTomorrowSchedule();
     }
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
