package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.TextUtils.TruncateAt;
import com.android.systemui.R;
public class CalendarEventView extends LinearLayout{
    public View mEventTimeView,mEventDivideLine;
    public TextView mEventStartTime,mEventEndTime,mEventContent; 
    public LinearLayout mCNEventItemView;	
    public CalendarEventView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.calendar_event_view,this);
		mEventTimeView=(View)findViewById(R.id.calendar_notifations_event_time_view);
		mEventStartTime=(TextView)findViewById(R.id.calendar_notifations_event_start_time);
		mEventEndTime=(TextView)findViewById(R.id.calendar_notifations_event_end_time);
		mCNEventItemView=(LinearLayout)findViewById(R.id.calendar_notifations_event_item_view);
		mEventContent=(TextView)findViewById(R.id.calendar_notifations_event_content);
		mEventDivideLine=(View)findViewById(R.id.calendar_notifations_event_divide);
    }
    public void hideEventTimeView(boolean hide){
    	mEventTimeView.setVisibility(hide? View.GONE:View.VISIBLE);
    	mEventContent.setTextSize(hide? 13:15);
       LayoutParams lp=(LayoutParams) mCNEventItemView.getLayoutParams();
    	lp.height=hide? 34:80;
	lp.leftMargin=hide?24:0;	
    	mCNEventItemView.setLayoutParams(lp);
    	mEventContent.setHeight(hide? 34:80);
	mEventContent.setSingleLine(hide);
	if(!hide){
            mEventContent.setLines(2);
    	     mEventContent.setEllipsize(TruncateAt.END);
	}
       mEventDivideLine.setVisibility(hide? View.GONE:View.VISIBLE);
    }
    public void SetStartTime(String time){
    	mEventStartTime.setText(time);   	
    }
    public void SetEndTime(String time){
    	mEventEndTime.setText(time);   	
    }
    public void SetEventContent(String content){
    	mEventContent.setText(content);   	
    }
    public void setDivideLineShow(boolean show){
       mEventDivideLine.setVisibility(show? View.VISIBLE:View.GONE);
    }	
}
