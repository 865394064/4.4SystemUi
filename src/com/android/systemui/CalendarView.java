
package com.example.testnotifiy;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;

@SuppressLint("NewApi")
public class CalendarView {
    private Context mContext;
    private Calendar startCalendar, endCalendar;
    private long eventId;
    private String eventLocation, eventTitle;
    private List<Event> tomorrowEvents,todayEvents;
    
    
    
    
    

    public CalendarView(Context context) {
        mContext = context;
       
    }

    /* get the events in today */
    public void getEventsLists() {

        
        tomorrowEvents = new Vector<Event>();
        todayEvents=new Vector<Event>();
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
    }
    
    public void clearEvents()
    {
        tomorrowEvents=null;
        todayEvents=null;
    }

    public List<Event> getTomorrowEvents() {
        return tomorrowEvents;
    }

    public List<Event> getTodayEvents() {
        return todayEvents;
    }

}
