package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.view.MotionEvent;

/**
 * Created by hjz on 19-10-11.
 */

public class Myrelas extends RelativeLayout{
    public Myrelas(Context context){
        super(context);
    }
    public Myrelas(Context context,AttributeSet attrs,int deefStyle ){
        super(context,attrs,deefStyle);
    }
    public Myrelas(Context context,AttributeSet attrs ){
        super(context,attrs);
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


}
