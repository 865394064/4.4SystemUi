package com.android.systemui.recent;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * Created by kay on 19-7-5.
 */

public class RecentCircleLayout extends FrameLayout{
    public RecentCircleLayout(Context context){
        super(context);
    }

    public RecentCircleLayout(Context context, AttributeSet attrs){
        super(context, attrs);
    }

    public RecentCircleLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
    }

    @java.lang.Override
    public void draw(Canvas canvas) {
        Path path = new Path();
        Log.d("kay6", "draw: " + getMeasuredWidth()+"  " + getMeasuredHeight());
        path.addRoundRect(new RectF(0,0,getMeasuredWidth(),getMeasuredHeight()),
                20, 20, Path.Direction.CW);
        canvas.clipPath(path, Region.Op.REPLACE);
        super.draw(canvas);
    }

    /*@java.lang.Override
    protected void dispatchDraw(Canvas canvas) {
        Path path = new Path();
        path.addRoundRect(new RectF(0,0,getMeasuredWidth(),getMeasuredHeight()),
                dip2px(getContext(), 8.0f),dip2px(getContext(), 8.0f), Path.Direction.CW);
        canvas.clipPath(path, Region.Op.REPLACE);
        super.dispatchDraw(canvas);
    }*/

    private int dip2px(Context context, float dps){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dps/scale + 0.5f);
    }
}
