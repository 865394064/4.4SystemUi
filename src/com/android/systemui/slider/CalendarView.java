package com.android.systemui.slider;

import com.android.systemui.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.TextView;



import android.annotation.SuppressLint;
import android.database.Cursor;
import com.android.systemui.slider.EventBorder;
import java.util.ArrayList;
import android.view.View;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import android.content.Intent;

public class CalendarView extends FrameLayout{

    

    private Context mContext;
    private Calendar startCalendar, endCalendar;
    private long eventId;
    private String eventLocation, eventTitle;
    private List<Event> tomorrowEvents,todayEvents,tempTodayEvents;

    private int a,b,d,e,f,g,h,i,j;
    private Rect rect;
    private Paint p,q,o;
    private Vector vector;
    private boolean c;
    private SimpleDateFormat simpleDateFormat;
    private Calendar m;
    
    private List<List<Event>> eventsList;
    private String l;
    private PhoneStatusBar statusBar;
  
    
    
    public CalendarView(Context context) {
        super(context);
   	mContext=context;
        a = 0;
        b = 7;
        rect = new Rect();
        init();
    }
    
    public CalendarView(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
	mContext=context;
        a = 0;
        b = 7;
        rect = new Rect();
        init();
    }

    public CalendarView(Context context, AttributeSet attributeset, int i1)
    {
        super(context, attributeset, i1);
        a = 0;
        b = 7;
        rect = new Rect();
        init();
    }
    
    
    private void init(){
        
        Resources resources = getResources();
        android.util.DisplayMetrics displaymetrics = resources.getDisplayMetrics();
        d = (int)TypedValue.applyDimension(1, 11F, displaymetrics);
        e = (int)TypedValue.applyDimension(1, 49.5F, displaymetrics);
        f = (int)TypedValue.applyDimension(1, 47F, displaymetrics);
        g = (int)TypedValue.applyDimension(1, 54F, displaymetrics);
        h = (int)TypedValue.applyDimension(1, 56F, displaymetrics);
        i = (int)TypedValue.applyDimension(1, 3F, displaymetrics);
        j = (int)TypedValue.applyDimension(1, 5F, displaymetrics);
        simpleDateFormat = new SimpleDateFormat("H", Locale.getDefault());
        p = new Paint();
        p.setColor(0x32fcfcfc);
        q = new Paint();
        q.setColor(0x92ffffff);
        o = new Paint();
        o.setColor(-1);
        o.setTextSize(TypedValue.applyDimension(1, 12F, displaymetrics));
        eventsList = new ArrayList<List<Event>>();
      
        m = GregorianCalendar.getInstance();
        c = false;
        setNoEvents(true);

	tomorrowEvents = new Vector<Event>();
        todayEvents=new Vector<Event>();
	tempTodayEvents=new Vector<Event>();
    }
    
    
    
    public void setNoEvents(boolean flag)
    {
        if(flag != c)
        {
            c = flag;
           clearAll();
            if(flag)
            {
                TextView textview = new TextView(getContext());
                textview.setText(R.string.no_events);
                textview.setTextColor(0x5dfefefe);
                textview.setTextSize(1, 15F);
            
                android.widget.FrameLayout.LayoutParams layoutparams = new android.widget.FrameLayout.LayoutParams(-2, -2, 17);
                layoutparams.leftMargin = g / 2;
                addView(textview, layoutparams);
            }
            requestLayout();
            invalidate();
        }
    }
    
    
    public void clearAll()
    {
        if(eventsList!=null)
        eventsList.clear();
        removeAllViews();
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    protected void dispatchDraw(Canvas canvas)
    {
        int i1 = -1;
        int j1;
        int k1;
        int l1;
        int i2;
        int j2;
        
        int k2;
        int l2;
        float f1;
        float f2;
        float f3;
        float f4;
        Paint paint;
        int k3;
        int l3;
        String s1;
        int i4;
        int j4;
        int k4;
        if(c)
            j1 = 4;
        else
            j1 = 1 + (Math.min(24, -1 + (a + b)) - a);
        k1 = d / 2;
        l1 = 0;
        i2 = 0;
        j2 = i1;
        do
        {
            int i3;
            int j3;
            if(l1 >= j1)
            {
                if(j2 >= 0 || i1 >= 0)
                {
                 
                    if(j2 < 0)
                        j2 = 0;
                    k4 = getWidth();
                    if(i1 < 0)
                        i1 = getHeight();
                    canvas.clipRect(0, j2, k4, i1);
                }
                super.dispatchDraw(canvas);
                return;
            }
            if(l1 > 0 && !c)
            {
                l3 = l1 + a;
                if(l3 == 12)
                {
                    s1 = getNoonString();
                } else
                {
                    m.clear();
                    m.set(11, l3);
                    s1 = simpleDateFormat.format(m.getTime());
                }
                o.getTextBounds(s1, 0, s1.length(), rect);
                i4 = -rect.left + (g - j - rect.width());
                j4 = (i2 - rect.top) + (d - rect.height()) / 2;
                canvas.drawText(s1, i4, j4, o);
            }
            k2 = i2 + k1;
            if(l1 == 0)
                l2 = f;
            else
                l2 = g;
            f1 = l2;
            f2 = k2;
            f3 = getWidth();
            f4 = k2;
            if(l1 == 0)
                paint = q;
            else
                paint = p;
            canvas.drawLine(f1, f2, f3, f4, paint);
            if(l1 == 0)
            {
                j3 = k2 + 1;
                i3 = i1;
            } else
            if(l1 == j1 + -1)
            {
                i3 = k2 + -1;
                j3 = j2;
            } else
            {
                i3 = i1;
                j3 = j2;
            }
            k3 = i2 + e;
            l1++;
            i2 = k3;
            i1 = i3;
            j2 = j3;
        } while(true);
    }
    
    




	 private int calcul(int i1, boolean flag)
	    {
		int j1 = (int)TypedValue.applyDimension(1, 1F, getResources().getDisplayMetrics());
		int k1 = i1 / 60;
		int l1 = i1 % 60;
		int i2;
		if(!flag || l1 != 0)
		    i2 = Math.min((int)((float)e * ((float)l1 / 60F)), (-1 + e) - j1) + (j1 + (1 + (d / 2 + (k1 - a) * e)));
		else
		    i2 = (-1 + (d / 2 + (k1 - a) * e)) - j1;
		return i2;
	    }



   
	  public void addEvents(List<Event> list)
	    {
		Calendar calendar=Calendar.getInstance();
		getEventsInSame(list);
		System.out.println("eventsList==="+eventsList.size());
		for(int m=0;m<eventsList.size();m++){
			List<Event> todayList=eventsList.get(m);
			System.out.println("todayListSize=="+todayList.size());
			CalendarParmeter calendarParmeter=new CalendarParmeter(null);

			for(int n=0;n<todayList.size();n++){
				Event event=todayList.get(n);
				int k3=n+1;
				

				EventBorder d1 = new EventBorder(getContext());
				d1.setStickerColor(0xff1e90ff);
				d1.setText(event.getEventTitle());
				d1.setOnClickListener(new EventClickListener(this,event));

	 			int i1 = (int)((event.getStartCalendar().getTimeInMillis() - calendar.getTimeInMillis()) /(60000*60*60));
				int j1 = (int)((event.getEndCalendar().getTimeInMillis() - calendar.getTimeInMillis()) /(60000*60*60));

		

				int k1 = event.getStartCalendar().get(11) + i1 * 24;
				int l1 = event.getStartCalendar().get(12);
				int i2 = event.getEndCalendar().get(11) + j1 * 24;
				int j2 = event.getEndCalendar().get(12);
			
				int l2 = l1 + k1 * 60;
				int i3 = j2 + i2 * 60;


				int i4 = calcul(l2, false);
			        int j4 = calcul(i3, true);

				if(n>=1){

			 		
					android.util.DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
		
					android.widget.FrameLayout.LayoutParams layoutparams = new android.widget.FrameLayout.LayoutParams(-1, 1 + (j4 - i4));					
					
					layoutparams.leftMargin = h +  calendarParmeter.c;
					calendarParmeter.c=layoutparams.leftMargin;
					layoutparams.topMargin = i4;
					layoutparams.rightMargin = (int)TypedValue.applyDimension(1, 1F, displaymetrics);
					addView(d1, layoutparams);	

				}else{
					
					android.util.DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
		
					android.widget.FrameLayout.LayoutParams layoutparams = new android.widget.FrameLayout.LayoutParams(-1, 1 + (j4 - i4));
					layoutparams.leftMargin = h + k3* i;
					calendarParmeter.c=layoutparams.leftMargin;
					layoutparams.topMargin = i4;
					layoutparams.rightMargin = (int)TypedValue.applyDimension(1, 1F, displaymetrics);
					addView(d1, layoutparams);	
				}

				
			}
		}
		
	
    }

	

	private void getEventsInSame(List<Event> list){
		
		for(int i=0;i<list.size();i++){
			Event event=list.get(i);
			List<Event> sameEventList=new Vector<Event>();
				sameEventList.add(event);
			for(int j=1;j<list.size();){
				
				Event event1=list.get(j);
				if(event.equals(event1)){
	
				  	sameEventList.add(event1);
					list.remove(j);
					
				}else{
					j++;
				
				}
				
			}
			
			eventsList.add(sameEventList);

		}
		
		
		

	}


    
    protected void onMeasure(int i1, int j1)
    {
        super.onMeasure(i1, j1);
        int k1;
        int l1;
        if(c)
            k1 = 4;
        else
            k1 = 1 + (Math.min(24, -1 + (a + b)) - a);
        l1 = d + e * (k1 + -1);
        setMeasuredDimension(getMeasuredWidth(), l1);
    }
    
    private String getNoonString()
    {
        if(l == null)
            l = getResources().getString(R.string.noon);
        return l;
    }
    
    
    public void setBeginHour(int i1)
    {
        int j1 = Math.max(0, Math.min(i1, 23));
        if(a != j1)
        {
            a = j1;
         
            invalidate();
            requestLayout();
        }
    }

    public void setHours(int i1)
    {
        if(b != i1)
        {
            b = i1;
            invalidate();
            requestLayout();
        }
    }

    

    
    public int getBeginHour()
    {
        return a;
    }

    public int getEndHour()
    {
        return -1 + (a + b);
    }

    public int getHours()
    {
        return b;
    }


/* get the events in today */
    public void getEventsLists() {

        
      
        Cursor eventCursor = mContext.getContentResolver().query(
                android.provider.CalendarContract.Events.CONTENT_URI,
                null,
                (new StringBuilder("deleted")).append("=0").append(" and dtstart").append(">=").
                        append(Calendar.getInstance().getTimeInMillis()).append(" and allDay")
                        .append("=0").toString(), null, null);

        Calendar todayCalendar = Calendar.getInstance();
        SimpleDateFormat nDateFormat = new SimpleDateFormat("yyyy MMMM d");
        String now = nDateFormat.format(todayCalendar.getTime());

        todayCalendar.roll(java.util.Calendar.DAY_OF_YEAR, 1);
        String tomorrow = nDateFormat.format(todayCalendar.getTime());

        System.out.println("Today=" + now + "\n tormorrow=" + tomorrow);
        while (eventCursor.moveToNext()) {

            int event_id = eventCursor.getColumnIndex("_id");
            if (event_id >= 0)
                eventId = eventCursor.getLong(event_id);

            eventTitle = eventCursor.getString(eventCursor.getColumnIndex("title"));
            
            int start_time = eventCursor.getColumnIndex("dtstart");

            if (start_time >= 0)
            {
                Calendar calendar1 = GregorianCalendar.getInstance();
                calendar1.setTimeInMillis(eventCursor.getLong(start_time));
                startCalendar = calendar1;

            }

            int end_time= eventCursor.getColumnIndex("dtend");
            if (end_time >= 0)
            {
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTimeInMillis(eventCursor.getLong(end_time));
                endCalendar = calendar;

            }

            int evnet_location = eventCursor.getColumnIndex("eventLocation");
            if (evnet_location >= 0)
                eventLocation = eventCursor.getString(evnet_location);



            String eventTime = nDateFormat.format(startCalendar.getTime());
            if (eventTime.equals(now)) {
                Event event = new Event(eventId);
                event.setStartCalendar(startCalendar);
                event.setEndCalendar(endCalendar);
                event.setEventTitle(eventTitle);
                event.setLocation(eventLocation);
                todayEvents.add(event);
		tempTodayEvents.add(event);
            } else if (eventTime.equals(tomorrow)) {
                Event event = new Event(eventId);
                event.setStartCalendar(startCalendar);
                event.setEndCalendar(endCalendar);
                event.setEventTitle(eventTitle);
                event.setLocation(eventLocation);
                tomorrowEvents.add(event);
            }
        }
        eventCursor.close();

      System.out.println(tomorrowEvents.size()+"   "+todayEvents.size());

	if(todayEvents.size()!=0){
	 setNoEvents(false);
	  Calendar start=todayEvents.get(0).getStartCalendar();
	   int beginHour=start.getTime().getHours();
	  for(int i=1;i<todayEvents.size();i++){
		int hour=todayEvents.get(i).getStartCalendar().getTime().getHours();
		if(beginHour>hour)
		{
			beginHour=hour;
		}
		
	  }

	 
	if(beginHour>0){
	  setBeginHour(beginHour-1);
	}else{
          setBeginHour(beginHour);
	}
	  setHours(6);

	addEvents(todayEvents);
          
      }else{

		clearEvents();
		setNoEvents(true);
	}

	final Intent intent=new Intent("phone_schedule_change");
	  mContext.sendBroadcast(intent);
    }
    
    public void clearEvents()
    {
        tomorrowEvents.clear();
        todayEvents.clear();
	tempTodayEvents.clear();
	 clearAll();
    }

    public List<Event> getTomorrowEvents() {
        return tomorrowEvents;
    }

    public List<Event> getTodayEvents() {
        return tempTodayEvents;
    }

	public List<List<Event>> getAllEventsToday(){

		return eventsList;
	}


	public PhoneStatusBar getBar(){
		return statusBar;
	}

	public void setBar(PhoneStatusBar bar){
		this.statusBar=bar;
	}
    
    
}
