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

public class AppPreviewCalendarNotification extends LinearLayout implements OnClickListener{
    String TAG="ApplicationPreview";
    boolean DEBUG = false;
    public Context mContext;
    public Intent mAppPreviewIntent;
    public boolean isLockScreen=false;
    public boolean inLockScreen=false;	
    private ImageView mCalendarNotificationsIcon;
    private TextView mCNExpandOrHide,mCNEmptyContentText,mCNOneViewText,mCNOneViewTime,mCNCountText,mCNMoreViewEnd;
    private View mCalendarNotificationsView,mCNContentView,mCNOneView,mCNMoreView;
    private List<Event> todays;
    private LinearLayout mCalendarNotificationsList;
    private boolean mCNEventViewIsExpand=false;
    
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
    public Drawable mCalendarIcon;
    boolean isFirstShow=true;
     
    public AppPreviewCalendarNotification(Context context) {
	 super(context);
         mContext = context;
    }

    public AppPreviewCalendarNotification(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
    }

    public AppPreviewCalendarNotification(Context context, AttributeSet attrs, int defStyle) {
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
	 //calendar notification		  
	  mCalendarNotificationsView = (View)findViewById(R.id.calendar_notifications_view);
	  mCalendarNotificationsIcon = (ImageView)findViewById(R.id.calendar_notifications_icon);
	  mCNExpandOrHide = (TextView)findViewById(R.id.calendar_notifications_expand_or_hide);
	  mCNExpandOrHide.setOnClickListener(mCalendarNotificationEventlistener);
	  mCNEmptyContentText = (TextView)findViewById(R.id.calendar_notifications_empty_content_text);
	  mCNEmptyContentText.setOnClickListener(this);//added by cfb
	  mCNOneViewText = (TextView)findViewById(R.id.calendar_notifications_one_view_text);
	  mCNOneViewTime = (TextView)findViewById(R.id.calendar_notifications_one_view_time);
	  mCNCountText = (TextView)findViewById(R.id.calendar_notifications_count_text);
	  mCNMoreViewEnd = (TextView)findViewById(R.id.calendar_notifications_more_view_end);
	  mCNContentView = (View)findViewById(R.id.calendar_notifications_content_view);
	  mCNOneView = (View)findViewById(R.id.calendar_notifications_one_view);
	  mCNMoreView = (View)findViewById(R.id.calendar_notifications_more_view);
	  mCalendarNotificationsList = (LinearLayout)findViewById(R.id.calendar_notifications_list); 
          mCalendarEventViewList = (CalendarView)findViewById(R.id.calendar_event_notifications_list); 
    }

    @Override
    public void onClick(View view) {
	// TODO Auto-generated method stub
	int id=view.getId();
	if(DEBUG)Log.i(TAG, "calendar_notification   onClick()  id===="+id);
	if(DEBUG)Log.i(TAG, "calendar_notification   onClick()  weather_app_name====");	   	   	
	final Intent homePressedIntent = new Intent("home_key_is_pressed");
	switch(id){
	       case R.id.calendar_notifications_empty_content_text://added by cfb
	          try{
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
	inLockScreen=true;	
	mAppPreviewIntent = null;
    } 
    	
    public void unLockScreen(){
	 if(DEBUG)Log.i(TAG, "calendar_notification   unLockScreen()  mAppPreviewIntent="+mAppPreviewIntent);
	 inLockScreen=false;
	if(mAppPreviewIntent!=null){
		Intent intent = mAppPreviewIntent;
		mAppPreviewIntent = null;
	     getContext().startActivity(intent);  
	}
    } 
    
//Calendar notification
       	 private OnClickListener mCalendarNotificationEventlistener = new OnClickListener() {
		@Override
		public void onClick(View arg0) {
			if(DEBUG)Log.i(TAG,"calendar_notification   mCalendarNotificationEventlistener()----mCNEventViewIsExpand="+mCNEventViewIsExpand);
		        if(mCalendarNotificationsView!=null){
				mCNExpandOrHide.setEnabled(false);
				if(mCNExpandOrHide!=null)mCNExpandOrHide.setText(mCNEventViewIsExpand?R.string.siri_suggest_expand:R.string.siri_suggest_hide);
			       ResizeAnimation a = new ResizeAnimation(mCalendarNotificationsView);
		               a.setDuration(500);
				int intiHeight=254;	   
				int allChildExpandHeight=mCalendarNotificationsList.getChildCount()*81+60;
				if(allChildExpandHeight<intiHeight)allChildExpandHeight=intiHeight;
                             int startHeight=mCNEventViewIsExpand ? allChildExpandHeight:intiHeight;
				int endHeight=mCNEventViewIsExpand ? intiHeight:allChildExpandHeight;
				if(DEBUG)Log.i(TAG,"calendar_notification   mCalendarNotificationEventlistener()----startHeight="+startHeight+"   endHeight="+endHeight);
				// set the starting height (the current height) and the new height that the view should have after the animation
				a.setParams(startHeight, endHeight);
				mCalendarNotificationsView.startAnimation(a);
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
						mCNExpandOrHide.setEnabled(true);
					}
				});
		    	}
			mCNCountText.setVisibility(mCNEventViewIsExpand?View.VISIBLE:View.GONE);
			for(int i=0;i<mCalendarNotificationsList.getChildCount();i++){
        		       CalendarEventView event=(CalendarEventView)mCalendarNotificationsList.getChildAt(i);
				event.hideEventTimeView(mCNEventViewIsExpand);
				if(i==mCalendarNotificationsList.getChildCount()-1)event.setDivideLineShow(false);
				LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams) event.getLayoutParams();
				lp.height=mCNEventViewIsExpand?34:81;
				event.setLayoutParams(lp);
				if(i>2){
                                      if(mCNEventViewIsExpand){
                                              event.setVisibility(View.GONE);
						  mCNMoreViewEnd.setVisibility(View.VISIBLE);
					  }else{
                                              event.setVisibility(View.VISIBLE);
						   mCNMoreViewEnd.setVisibility(View.GONE);						
					  }
				}
			}
			mCNEventViewIsExpand = !mCNEventViewIsExpand;
		}
	};

       public Drawable getCalendarIcon(){
               return  mCalendarIcon;
	}
   
       public void updateCalendarIcon(){
	       final String date = String.valueOf(getLauncherDayOfMonth());
	    	final String week = DateUtils.formatDateTime(mContext, System.currentTimeMillis(), DateUtils.FORMAT_SHOW_WEEKDAY);				
		Drawable icon = mContext.getResources().getDrawable(R.drawable.app_calendar);
		mCalendarIcon =createCalendarIcon(date, week, icon, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
		if(mCalendarNotificationsIcon!=null)mCalendarNotificationsIcon.setBackground(mCalendarIcon);		
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
             if(DEBUG)Log.i(TAG,"calendar_notification updateTodaySchedule() todayEventsList="+todayEventsList.size()+" earlyTodayEvents=="+earlyTodayEvents.size());     
                 
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
	            if(DEBUG)Log.i(TAG,"calendar_notification  updateToday()----remainingMinutes="+remainingMinutes+"   is24=="+is24+"  isAm="+isAm+"    nDateFormat.format(beginHour)="+nDateFormat.format(beginHour));
	            if(remainingMinutes>0){
			      if(todayEarlyEventsCount==1){
				    mCNExpandOrHide.setVisibility(View.GONE);	  
	                          mCNEmptyContentText.setVisibility(View.GONE);				    
				    mCNContentView.setVisibility(View.VISIBLE);
				    mCNOneView.setVisibility(View.VISIBLE);
				    mCNMoreView.setVisibility(View.GONE);
				    mCNOneViewText.setText(todayEarlyEvents.get(0).getEventTitle());
				    if(is24){
			                      mCNOneViewTime.setText(mContext.getString(R.string.calendar_notifations_one_view_time,nDateFormat.format(beginHour),nDateFormat.format(todayEarlyEvents.get(0).getEndCalendar().getTimeInMillis())));
			           }else{
			                      if(isAm){
			                              mCNOneViewTime.setText(mContext.getString(R.string.calendar_notifations_one_view_time_am,nDateFormat.format(beginHour),nDateFormat.format(todayEarlyEvents.get(0).getEndCalendar().getTimeInMillis())));
			                      }else{
			                              mCNOneViewTime.setText(mContext.getString(R.string.calendar_notifations_one_view_time_pm,nDateFormat.format(beginHour),nDateFormat.format(todayEarlyEvents.get(0).getEndCalendar().getTimeInMillis())));
				               } 
				    }				
				    
			      }else if(todayEarlyEventsCount>1){
			           mCNExpandOrHide.setVisibility(View.VISIBLE);
	                          mCNEmptyContentText.setVisibility(View.GONE);
				    mCNContentView.setVisibility(View.VISIBLE);
				    mCNOneView.setVisibility(View.GONE);
				    mCNMoreView.setVisibility(View.VISIBLE);
				    String startTime,endTime;	
				    if(is24){
					     startTime=nDateFormat.format(beginHour);
					     endTime=nDateFormat.format(todayEarlyEvents.get(0).getEndCalendar().getTimeInMillis());	 
	                                  mCNCountText.setText(mContext.getString(R.string.calendar_notifations_count_text,nDateFormat.format(beginHour),todayEarlyEventsCount));
				    }else{
	                                 if(isAm){
							startTime=mContext.getString(R.string.calendar_notifations_more_view_time_am,nDateFormat.format(beginHour));
					              endTime=mContext.getString(R.string.calendar_notifations_more_view_time_am,nDateFormat.format(todayEarlyEvents.get(0).getEndCalendar().getTimeInMillis()));			 	
	                                          mCNCountText.setText(mContext.getString(R.string.calendar_notifations_count_text_am,nDateFormat.format(beginHour),todayEarlyEventsCount));
	                                  }else{
	                                          startTime=mContext.getString(R.string.calendar_notifations_more_view_time_pm,nDateFormat.format(beginHour));
					             endTime=mContext.getString(R.string.calendar_notifations_more_view_time_pm,nDateFormat.format(todayEarlyEvents.get(0).getEndCalendar().getTimeInMillis()));	
	                                          mCNCountText.setText(mContext.getString(R.string.calendar_notifations_count_text_pm,nDateFormat.format(beginHour),todayEarlyEventsCount));
					     } 
				    }
				    //for(int i=0;i<mCalendarNotificationsList.getChildCount();i++){
						mCalendarNotificationsList.removeAllViewsInLayout();			
				    //}	
				    for(int i=0;i<todayEarlyEventsCount;i++){
	                                   CalendarEventView event=new CalendarEventView(mContext);
						event.SetStartTime(startTime);
						event.SetEndTime(endTime);
						event.SetEventContent(todayEarlyEvents.get(i).getEventTitle());
						mCalendarNotificationsList.addView(event); 
				    }	
				   
				    mCNMoreViewEnd.setVisibility(View.GONE);
				    
					   if(todayEarlyEventsCount>3){
					   	for(int i=0;i<mCalendarNotificationsList.getChildCount();i++){
							if(i>2)mCalendarNotificationsList.getChildAt(i).setVisibility(View.GONE);			
						}	
					   	mCNMoreViewEnd.setVisibility(View.VISIBLE);
	                                    mCNMoreViewEnd.setText(mContext.getString(R.string.c_n_more_view_end_text,String.valueOf(todayEarlyEventsCount-3)));
					   }
			          
			      }		
	                    
	              }else
	                    {   
	                         mCNExpandOrHide.setVisibility(View.GONE);
	                         mCNEmptyContentText.setVisibility(View.VISIBLE);
				 mCNContentView.setVisibility(View.GONE);
				    mCNOneView.setVisibility(View.GONE);
				    mCNMoreView.setVisibility(View.GONE);	
	                          for(int i=0;i<mCalendarNotificationsList.getChildCount();i++){
						mCalendarNotificationsList.removeAllViewsInLayout();			
				    }

	                          
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

          if(DEBUG)Log.i(TAG,"calendar_notification getTodayEarlyEvents()   list="+list.size());
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
    public void setCalendarView(CalendarView calendarView){
          mCalendarView=calendarView;
   }
	
   	  
   private CalendarView mCalendarEventViewList;	
   
   public void eventsChanged(){
         if(DEBUG)Log.i(TAG,"calendar_notification eventsChanged()   ");  
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
