package com.example.testnotifiy;

import java.util.Calendar;

public class Event{
    
    
    private Calendar startCalendar;
    private Calendar endCalendar;
    private String eventTitle;
    private String location;
    private long evnetid;
    private boolean isAllDay;
    public Event(long id)
    {
        this.evnetid=id;
    }
    public Calendar getStartCalendar() {
        return startCalendar;
    }
    public void setStartCalendar(Calendar startCalendar) {
        this.startCalendar = startCalendar;
    }
    public Calendar getEndCalendar() {
        return endCalendar;
    }
    public void setEndCalendar(Calendar endCalendar) {
        this.endCalendar = endCalendar;
    }
    public String getEventTitle() {
        return eventTitle;
    }
    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }
    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public long getEventId() {
        return evnetid;
    }
    public boolean isAllDay() {
        return isAllDay;
    }
    public void setAllDay(boolean isAllDay) {
        this.isAllDay = isAllDay;
    }
    
    
    
}