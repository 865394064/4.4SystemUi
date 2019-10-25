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

public class AppPreviewMemo extends LinearLayout implements OnClickListener{
    String TAG="ApplicationPreview";
    boolean DEBUG = false;
    public Context mContext;
    public Intent mAppPreviewIntent;
    public boolean isLockScreen=false;
    public boolean inLockScreen=false;
    boolean isFirstShow=true;
    private View mEditView,mSelectedView,mCameraView,mDrawView;
	
    public AppPreviewMemo(Context context) {
	 super(context);
         mContext = context;
    }

    public AppPreviewMemo(Context context, AttributeSet attrs) {
	        this(context, attrs, 0);
    }

    public AppPreviewMemo(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        /// M: Init customize clock plugin
       
    }
    public void setContext(Context context){
        mContext = context;
	 //memo
	  mQueryHandler= new QueryHandler(mContext.getContentResolver());
	  if(isFirstShow)startQuery();
          isFirstShow=false;		  	
    }
    
    @Override
     protected void onFinishInflate() {
	// TODO Auto-generated method stub
	super.onFinishInflate();		   
	 //memo
	  memoEmptyView = (TextView)findViewById(R.id.memo_empty_content_text);
	  memoExpandOrHide = (TextView)findViewById(R.id.memo_expand_or_hide);
	  memoExpandOrHide.setOnClickListener(memoContentViewlistener);
	  memoContentDivide = (View)findViewById(R.id.memo_content_divide);
	  memoContentView = (LinearLayout)findViewById(R.id.memo_content_view);
	  memoContentListView = (LinearLayout)findViewById(R.id.memo_content_list);
	  memoEditView = (LinearLayout)findViewById(R.id.memo_edit_view);
	  mEditView = (View) findViewById(R.id.new_memo_edit);
	  mEditView.setOnClickListener(this);
	  mSelectedView = (View) findViewById(R.id.new_memo_selected);
	  mSelectedView.setOnClickListener(this);
	  mCameraView= (View) findViewById(R.id.new_memo_camera);
	  mCameraView.setOnClickListener(this);
	  mDrawView= (View) findViewById(R.id.new_memo_draw);
	  mDrawView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
	// TODO Auto-generated method stub
	int id = view.getId();
	final Intent homePressedIntent = new Intent("home_key_is_pressed");
	switch(id){
              case R.id.new_memo_edit:
		case R.id.new_memo_selected:
		case R.id.new_memo_camera:
		case R.id.new_memo_draw:
			   try{
				 mAppPreviewIntent = getContext().getPackageManager().getLaunchIntentForPackage("com.hskj.memo");
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
	 if(DEBUG)Log.i(TAG, "memo unLockScreen()  mAppPreviewIntent="+mAppPreviewIntent);
	 inLockScreen=false;
	if(mAppPreviewIntent!=null){
		Intent intent = mAppPreviewIntent;
		mAppPreviewIntent = null;
	     getContext().startActivity(intent);  
	}
    } 
    
    //Memo
	   private TextView memoExpandOrHide,memoEmptyView;
	   private View memoContentDivide;
	   private LinearLayout memoContentView,memoContentListView,memoEditView;
	   private boolean memoContentViewIsExpand=false;
	   private ArrayList<String> MemoContent = new ArrayList<String>();
	   private ArrayList<String> MemoContentDate = new ArrayList<String>();
	   private QueryHandler mQueryHandler;
	   private static final int QUERY_TOKEN = 1000;
	   public static final String AUTHORITY = "com.hskj.provider.memo";
	   public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/memo");
	   public static final String MEMOCONTENT = "content";
	   public static final String MEMODATETIME = "datetime";	
	   public void startQuery(){
			mQueryHandler.startQuery(QUERY_TOKEN, null,CONTENT_URI, null, "", null, null);

		}
		
		private final class QueryHandler extends AsyncQueryHandler{

			public QueryHandler(ContentResolver cr) {
				super(cr);
				// TODO Auto-generated constructor stub
			}

			@Override
			protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
				// TODO Auto-generated method stub
				switch(token){
					case QUERY_TOKEN:
						int count = cursor.getCount();
						memoContentListView.removeAllViewsInLayout();
						while(cursor.moveToNext()){
	                                         MemoContentView contentView=new MemoContentView(mContext);
						     String content = subString(cursor.getString(cursor.getColumnIndex(MEMOCONTENT)),mContext.getResources().getInteger(R.integer.iphonememo_memolistitem_content_width));
			
							long datetime = cursor.getLong(cursor.getColumnIndex(MEMODATETIME));
							Calendar calendar = Calendar.getInstance();
							calendar.clear();//added by lzp
							calendar.setTimeInMillis(datetime);
							
							String datetimeStr = formatCalendar(calendar);
							contentView.SetEventContent(content);
							contentView.SetTime(datetimeStr);
							if(DEBUG)Log.i(TAG,"onQueryComplete()----count="+count+"   content="+content+"   datetimeStr="+datetimeStr);
							memoContentListView.addView(contentView);
						}
						memoContentListView.setVisibility(count > 0 ? View.VISIBLE : View.GONE);
						memoEmptyView.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
						memoContentDivide.setVisibility(count > 0 ? View.GONE : View.VISIBLE);
						LinearLayout.LayoutParams lp=(LinearLayout.LayoutParams) memoContentView.getLayoutParams();
						if(memoContentViewIsExpand){
	                                            if(count > 0){
	                                                 lp.height=memoContentListView.getChildCount()*161+80;
							 }else{
	                                                 lp.height=memoEmptyView.getHeight()+81;
							 }
						}else{
	                                             if(count > 0){
	                                                 lp.height=201;//(161+40)
							 }else{
	                                                 lp.height=195; 
							 }
						}
						memoContentView.setLayoutParams(lp);
						for(int i=0;i<memoContentListView.getChildCount();i++){
			        		       MemoContentView content=(MemoContentView)memoContentListView.getChildAt(i);
							if(i>0){
			                                      if(memoContentViewIsExpand){
			                                              content.setVisibility(View.VISIBLE);
								  }else{
			                                              content.setVisibility(View.GONE);
								  }
							}
						}
					break;
				}
			}				
		}
		private OnClickListener memoContentViewlistener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                              if(DEBUG)Log.i(TAG, "memo   memoContentViewlistener()  memoContentView="+memoContentView+"  memoContentListView="+memoContentListView+"    memoExpandOrHide="+memoExpandOrHide);
			      if(memoContentView!=null && memoContentListView!=null &&memoExpandOrHide!=null ){
				  	memoExpandOrHide.setEnabled(false);
	                             memoContentViewIsExpand=!memoContentViewIsExpand;
					memoExpandOrHide.setText(memoContentViewIsExpand?R.string.siri_suggest_hide:R.string.siri_suggest_expand);
					ResizeAnimation a = new ResizeAnimation(memoContentView);
			               a.setDuration(500);
					int startHeight=memoContentView.getHeight();
				       int endHeight=memoContentViewIsExpand?(startHeight+(memoContentListView.getChildCount()< 2 ? 40 : (memoContentListView.getChildCount()-1)*161+40)) : (startHeight-(memoContentListView.getChildCount()< 2 ? 40 : (memoContentListView.getChildCount()-1)*161+40));		
					a.setParams(startHeight, endHeight);
					memoContentView.startAnimation(a);
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
							for(int i=0;i<memoContentListView.getChildCount();i++){
				        		       MemoContentView content=(MemoContentView)memoContentListView.getChildAt(i);
								if(i>0){
				                                      if(memoContentViewIsExpand){
				                                              content.setVisibility(View.VISIBLE);
									  }else{
				                                              content.setVisibility(View.GONE);
									  }
								}
							}
							memoEditView.setVisibility(memoContentViewIsExpand ? View.VISIBLE : View.GONE);
							memoExpandOrHide.setEnabled(true);
						}
					});
			      }
			}
	    };
		public String subString(String string,int length){	
			//if(DEBUG) Log.d(TAG, "subString str:"+str);
			String[] splitStr = string.trim().split("\n");
			String str = "";
			if(splitStr.length > 0) str = splitStr[0];
			
			String returnStr = "";
			String tempStr = null;
			int count = 0;
			for(int i=0;i<str.length();i++){
				if(count > length){
					return str.substring(0,i-1) + "...";
				}
				
				int charLength = str.substring(i, i+1).getBytes().length;
				if(charLength >= 2){
					charLength = 2;
				}
				count = count + charLength;
				
			}
			return str;
		}
		
		//閺嶇厧绱￠崠鏍ㄦ闂?閺堝牄鈧焦妫╅妴浣哥毈閺冭翰鈧礁鍨?
		private String formatCalendar(Calendar calendar){
			int basic = 24*60*60*1000;
			
			Calendar calendarToday = Calendar.getInstance();
			calendarToday.set(calendarToday.get(Calendar.YEAR), calendarToday.get(Calendar.MONTH), calendarToday.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			long diffMills = calendarToday.getTimeInMillis() - calendar.getTimeInMillis();
			
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;
			int day = calendar.get(Calendar.DAY_OF_MONTH);
			int week = calendar.get(Calendar.DAY_OF_WEEK);
			
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			String hourStr = "";
			if(hour < 10){
				hourStr = "0"+String.valueOf(hour);
			}else{
				hourStr = String.valueOf(hour);
			}
			int minute = calendar.get(Calendar.MINUTE);
			String minuteStr = "";
			if(minute < 10){
				minuteStr = "0"+String.valueOf(minute);
			}else{
				minuteStr = String.valueOf(minute);
			}
			
			String str = "";
			//if(DEBUG) Log.d(TAG, "formatCalendar days:"+diffMills/basic);
			if(diffMills < 0){
				str = hourStr + mContext.getResources().getString(R.string.iphonememo_tv_colon) + minuteStr;
			}else{
				long days = diffMills/basic;
				
				if(days == 0){
					str = mContext.getResources().getString(R.string.iphonememo_tv_yesterday);
				}else if(judgeThisWeek(calendar)){
					str = getWeek(week);
				}else{
					/**Begin: modified by lzp for bug [2674] **/
					//str = month + mContext.getResources().getString(R.string.iphonememo_tv_month) + day + mContext.getResources().getString(R.string.iphonememo_tv_day);
					str = (""+year).substring(2)+"-"+ month + "-" + day;
					/**End: modified by lzp for bug [2674] **/
				}
			}
			return str;
			
		}
		//閺嶇厧绱￠崠鏍翻閸戠儤妲﹂張?
		public String getWeek(int week){
			String str = "";
			if(week == 2){
				str = mContext.getResources().getString(R.string.iphonememo_tv_monday);
			}else if(week == 3){
				str = mContext.getResources().getString(R.string.iphonememo_tv_tuesday);
			}else if(week == 4){
				str = mContext.getResources().getString(R.string.iphonememo_tv_wednesday);
			}else if(week == 5){
				str = mContext.getResources().getString(R.string.iphonememo_tv_thursday);
			}else if(week == 6){
				str = mContext.getResources().getString(R.string.iphonememo_tv_friday);
			}else if(week == 7){
				str = mContext.getResources().getString(R.string.iphonememo_tv_saturday);
			}else if(week == 1){
				str = mContext.getResources().getString(R.string.iphonememo_tv_sunday);
			}
			
			
			return str;
		}
		
		//閸掋倖鏌囬幍鈧紒娆愭闂傚瓨妲搁崥锕€婀張顒€鎳?
		public boolean judgeThisWeek(Calendar calendar){
			int basic = 24*60*60*1000;
			
			Calendar calendarToday = Calendar.getInstance();
			calendarToday.set(calendarToday.get(Calendar.YEAR), calendarToday.get(Calendar.MONTH), calendarToday.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
			long diffMills = calendarToday.getTimeInMillis() - calendar.getTimeInMillis();
			long days = diffMills/basic;
			int weekOfToday = calendarToday.get(Calendar.DAY_OF_WEEK);
			
			int weekOfDay = calendar.get(Calendar.DAY_OF_WEEK);
			
			if(days > 5){										//閺冨爼妫块惄鎼佹婢堆傜艾娑撯偓閸?
				return false;
			}else if(days <= 5 && days >= 0){
				if(weekOfToday == 1){							//瑜版挷绮栨径鈺傛Ц閸涖劍妫╅弮?
					return true;
				}else if(weekOfDay == 1){						//瑜版挻澧嶇紒娆愭闂傚瓨妲搁崨銊︽）閺?
					return false;
				}else if(weekOfToday >= weekOfDay){				
					return true;
				}else{
					return false;
				}
				
			}
			
			return true;
			
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
