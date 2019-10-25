package com.android.systemui.slider;

import android.view.View;
import android.net.Uri;
import com.android.systemui.slider.CalendarView;
import android.content.Intent;
import static android.provider.CalendarContract.EXTRA_EVENT_BEGIN_TIME;
import static android.provider.CalendarContract.EXTRA_EVENT_END_TIME;
import android.provider.CalendarContract.Calendars;
//import android.provider.Calendar.Events;
import android.provider.CalendarContract.Events;
import android.content.ContentUris;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import android.app.KeyguardManager;
import android.content.Context;
import android.app.ActivityManagerNative;
import android.os.RemoteException;


class EventClickListener
    implements android.view.View.OnClickListener
{

	private PhoneStatusBar mBar;
	final CalendarView a;
	private final Event b;

   EventClickListener(CalendarView calendarView, Event event)
    {
	 super();
        a = calendarView;
        b = event;
	mBar=a.getBar();
       
    }

    public void onClick(View view)
    {
	System.out.println("onClick 11111111111111   ");
       if (b != null){

		 try {
                // The intent we are sending is for the application, which
                // won't have permission to immediately start an activity after
                // the user switches to home.  We know it is safe to do at this
                // point, so make sure new activity switches are now allowed.
                ActivityManagerNative.getDefault().resumeAppSwitches();
                // Also, notifications can be launched from the lock screen,
                // so dismiss the lock screen when the activity starts.
                ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
            	} catch (RemoteException e) {
           	 }
		System.out.println("onClick 222222   ");

		  Intent intent = new Intent(Intent.ACTION_VIEW);
                    Uri eventUri = ContentUris.withAppendedId(Events.CONTENT_URI,
                            b.getEventId());
                    intent.setData(eventUri);
                    intent.setClassName("com.android.calendar", "com.android.calendar.EventInfoActivity");
		    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(EXTRA_EVENT_BEGIN_TIME, b.getStartCalendar().getTimeInMillis());
                    intent.putExtra(EXTRA_EVENT_END_TIME, b.getEndCalendar().getTimeInMillis());
		   System.out.println("onClick 3333333333   ");
                   (a.getContext()).startActivity(intent);


		KeyguardManager kgm =
                    (KeyguardManager) (a.getContext()).getSystemService(Context.KEYGUARD_SERVICE);
                	if (kgm != null) kgm.exitKeyguardSecurely(null);
		   if(mBar!=null)
       		   mBar.animateCollapsePanels();
        }
    }


}

