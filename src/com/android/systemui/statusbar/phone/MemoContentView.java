package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.text.TextUtils.TruncateAt;
import com.android.systemui.R;
public class MemoContentView extends LinearLayout{
    public View mEventDivideLine;
    public TextView mEventTime,mEventContent; 	
    public MemoContentView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.memo_content_view,this);		
		mEventTime=(TextView)findViewById(R.id.memo_content_time);				
		mEventContent=(TextView)findViewById(R.id.memo_event_content);
		mEventDivideLine=(View)findViewById(R.id.memo_content_divide);
    }
    
    public void SetTime(String time){
    	mEventTime.setText(time);   	
    }
    public void SetEventContent(String content){
    	mEventContent.setText(content);   	
    }
    public void setDivideLineShow(boolean show){
       mEventDivideLine.setVisibility(show? View.VISIBLE:View.GONE);
    }	
}
