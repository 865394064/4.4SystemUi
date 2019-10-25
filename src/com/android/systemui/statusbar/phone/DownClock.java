/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.phone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import java.lang.ref.WeakReference;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import android.util.Log;
import com.android.systemui.R;
import com.hskj.iphone.view.IphoneLunar;
import java.util.Date;
import android.graphics.Color;//added by lzp for bug [2582]
import java.util.Locale;
/**
 * Displays the time
 */
public class DownClock extends RelativeLayout {
    //private static final String ANDROID_CLOCK_FONT_FILE = "/system/fonts/AndroidClock.ttf";//removed by wang 201809113
    private static final String ANDROID_CLOCK_FONT_FILE = "/system/fonts/PhonepadTwo.ttf";
    private final static String M12 = "h:mm";
    private final static String M24 = "kk:mm";
    static View v;
    private Calendar mCalendar;
    private String mFormat;
    static TextView mTimeView;
    public TextView ck_time,ck_am_pm,lunarcalendar,datatext;
    static View mContext1;
    private AmPm mAmPm;
    private TimeView mTimeView1;
    private ContentObserver mFormatChangeObserver;
    private int mAttached = 0; // for debugging - tells us whether attach/detach is unbalanced
    private CharSequence mDateFormatString;
    /* called by system on minute ticks */
    public void initView(Context context){
        View.inflate(context, R.layout.downrela, DownClock.this);
        Resources res = getContext().getResources();
        mDateFormatString =
                res.getText(com.android.internal.R.string.full_wday_month_day_no_year);
        ck_time = (TextView) this.findViewById(R.id.clock_text);
        ck_am_pm = (TextView) this.findViewById(R.id.am_pm);
        lunarcalendar = (TextView) this.findViewById(R.id.lunarcalendar);
        datatext = (TextView) this.findViewById(R.id.datetext);
        if(ck_time!=null){
            ck_time.setTypeface(Typeface.createFromFile(ANDROID_CLOCK_FONT_FILE));
        }
        mAmPm = new AmPm(this, null);
        mCalendar = Calendar.getInstance();
        setDateFormat();
    }

    private final Handler mHandler = new Handler();
    private BroadcastReceiver mIntentReceiver;

    private static class TimeChangedReceiver extends BroadcastReceiver {
        private WeakReference<DownClock> mClock;
        private Context mContext;

        public TimeChangedReceiver(DownClock clock) {
            mClock = new WeakReference<DownClock>(clock);
            mContext = clock.getContext();
        }
        @Override
        public void onReceive(Context context, Intent intent) {
            // Post a runnable to avoid blocking the broadcast.
            final boolean timezoneChanged =
                    intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED);
            final DownClock clock = mClock.get();
            if (clock != null) {
                clock.mHandler.post(new Runnable() {
                    public void run() {
                        if (timezoneChanged) {
                            clock.mCalendar = Calendar.getInstance();
                        }
                        clock.updateTime();
                    }
                });
            } else {
                try {
                    mContext.unregisterReceiver(this);
                } catch (RuntimeException e) {
                }
            }
        }
    };
    static class TimeView {
        TimeView(View parent, Typeface tf){

            mTimeView = (TextView)parent.findViewById(R.id.clock_text);
            if(mTimeView==null){
            }
            else{
                mTimeView.setTypeface(Typeface.createFromFile(ANDROID_CLOCK_FONT_FILE));
            }

        }
    }
    static class AmPm {
        private TextView mAmPmTextView;
        private String mAmString, mPmString;

        AmPm(View parent, Typeface tf) {
            // No longer used, uncomment if we decide to use AM/PM indicator again
            mAmPmTextView = (TextView) parent.findViewById(R.id.am_pm);
            if (mAmPmTextView != null && tf != null) {
                mAmPmTextView.setTypeface(tf);
            }

            String[] ampm = new DateFormatSymbols().getAmPmStrings();
            mAmString = ampm[0];
            mPmString = ampm[1];
        }

        void setShowAmPm(boolean show) {
            if (mAmPmTextView != null) {
                mAmPmTextView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }

        void setIsMorning(boolean isMorning) {
            if (mAmPmTextView != null) {
                mAmPmTextView.setText(isMorning ? mAmString : mPmString);
            }
        }
        TextView getTextView() {
            return mAmPmTextView;
	}
    }

    private static class FormatChangeObserver extends ContentObserver {
        private WeakReference<DownClock> mClock;
        private Context mContext;
        public FormatChangeObserver(DownClock clock) {
            super(new Handler());
            mClock = new WeakReference<DownClock>(clock);
            mContext = clock.getContext();
        }
        @Override
        public void onChange(boolean selfChange) {
            DownClock digitalClock = mClock.get();
            if (digitalClock != null) {
                digitalClock.setDateFormat();
                digitalClock.updateTime();
            } else {
                try {
                    mContext.getContentResolver().unregisterContentObserver(this);
                } catch (RuntimeException e) {
                    // Shouldn't happen
                }
            }
        }
    }
    public DownClock(Context context) {
        this(context, null);
        initView(context);
    }

    public DownClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
       // mTimeView = (TextView) findViewById(R.id.clock_text);
            Log.d("hjz","mTimeView==null");
        //mTimeView.setTypeface(Typeface.createFromFile(ANDROID_CLOCK_FONT_FILE));

       // mTimeView1=new TimeView(this, null);

        //mAmPm = new AmPm(this, null);
       // mCalendar = Calendar.getInstance();
       // setDateFormat();
    }

    public void setWhiteTheme(boolean isWhiteTheme) {
	final int textColor = (isWhiteTheme ? Color.WHITE:0xff000000);//0xff657179
	if(ck_time != null)
        ck_time.setTextColor(textColor);
        if(lunarcalendar!=null){
            lunarcalendar.setTextColor(textColor);
        }
	if(mAmPm != null &&  mAmPm.getTextView() != null)
	    mAmPm.getTextView().setTextColor(textColor);
    }
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mAttached++;

        if (mIntentReceiver == null) {
            mIntentReceiver = new TimeChangedReceiver(this);
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            mContext.registerReceiverAsUser(mIntentReceiver, UserHandle.OWNER, filter, null, null );
        }
        /* monitor 12/24-hour display preference */
        if (mFormatChangeObserver == null) {
            mFormatChangeObserver = new FormatChangeObserver(this);
            mContext.getContentResolver().registerContentObserver(
                    Settings.System.CONTENT_URI, true, mFormatChangeObserver);
        }
        updateTime();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mAttached--;

        if (mIntentReceiver != null) {
            mContext.unregisterReceiver(mIntentReceiver);
        }
        if (mFormatChangeObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(
                    mFormatChangeObserver);
        }

        mFormatChangeObserver = null;
        mIntentReceiver = null;
    }
    void updateTime(Calendar c) {
        mCalendar = c;
        updateTime();
    }

    public void updateTime() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        CharSequence newTime = DateFormat.format(mFormat, mCalendar);
        IphoneLunar lunar = new IphoneLunar(mCalendar);
        lunarcalendar.setText(lunar.toString());
        String date = DateFormat.format(mDateFormatString, new Date()).toString();
        if(isZh(getContext()))date = date.replace(" ","");
        String  day = getContext().getResources().getString(R.string.date_day);
        date = date.replace(day,day+" ");
        datatext.setText(date);
        if(mTimeView==null){
        }else{
            mTimeView.setText(newTime);
        }
        ck_time.setText(newTime);
    }
    private void setDateFormat() {
        mFormat = android.text.format.DateFormat.is24HourFormat(getContext()) ? M24 : M12;
        //mAmPm.setShowAmPm(mFormat.equals(M12));//removed by w never show am or pm
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
