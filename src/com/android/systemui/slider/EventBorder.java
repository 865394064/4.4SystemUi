
package com.android.systemui.slider;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.TypedValue;
import android.widget.TextView;

public class EventBorder extends TextView
{

    public EventBorder(Context context)
    {
        super(context);
        a();
    }

    private void a()
    {
        android.util.DisplayMetrics displaymetrics = getResources().getDisplayMetrics();
        setPadding((int)TypedValue.applyDimension(1, 4F, displaymetrics), 0, 0, 0);
        a = (int)TypedValue.applyDimension(1, 1.5F, displaymetrics);
        setBackgroundColor(0x19fefefe);
        setTextColor(-1);
    }

    protected void onDraw(Canvas canvas)
    {
        canvas.save(2);
        super.onDraw(canvas);
        canvas.restore();
        canvas.drawRect(0F, 0F, a, getHeight(), b);
    }

    public void setStickerColor(int i)
    {
        b = new Paint();
        b.setColor(i);
    }

    private int a;
    private Paint b;
}

