package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by administrator on 16-12-8.
 */
public class MyViewPager extends ViewPager {

    private boolean scrollble = true;
    private Context mContext;
    public MyViewPager(Context context) {
        super(context);
    }

    public MyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        /*if (isTopActivity()||isScreenSecureLocked(mContext)) {
            return false;
        }*/
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
//        if (Settings.System.getInt(mContext.getContentResolver(),"IS_TOP_APP_LAUNCHER",0)==1||isScreenSecureLocked(mContext)) {
//            Log.d("chenshichun", ":::::::::::::::::::::!!!!!::" + Settings.System.getInt(mContext.getContentResolver(), "IS_TOP_APP_LAUNCHER", 0));
//            return false;
//        }
//        else{
//            return super.onInterceptTouchEvent(arg0);
//        }

        if(Settings.System.getInt(mContext.getContentResolver(),"CURRENT_SCREEN_SLIDE",0)==0){
            return false;
        }else{
            return super.onInterceptTouchEvent(arg0);

        }
    }

    public boolean isScrollble() {
        return scrollble;
    }

    public void setScrollble(boolean scrollble) {
        this.scrollble = scrollble;
    }

    @Override
    public void scrollTo(int x, int y) {
        if(scrollble){
            super.scrollTo(x, y);
        }
    }

    private boolean isTopActivity()
    {
        boolean isTop = false;
        ActivityManager am = (ActivityManager)mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn.getClassName().contains("com.hskj.hometest.Launcher")) {
            isTop = true;
        }
        return isTop;
    }

    public final boolean isScreenSecureLocked(Context c) {
        android.app.KeyguardManager mKeyguardManager = (android.app.KeyguardManager) c
                .getSystemService(Context.KEYGUARD_SERVICE);
        if(mKeyguardManager == null) return false;
        return mKeyguardManager.isKeyguardLocked();//mod by lzp
    }


}