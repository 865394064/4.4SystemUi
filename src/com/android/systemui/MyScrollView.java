package com.android.systemui;

import android.content.Context;  
import android.graphics.Rect;  
import android.util.AttributeSet;  
import android.view.MotionEvent;  
import android.view.View;  
import android.view.animation.TranslateAnimation;  
import android.widget.ScrollView;  
  
/** 
 * 鑷畾涔塖crollView 
 */  
public class MyScrollView extends ScrollView {  
    private View inner;// 瀛╁瓙  
  
    private float y;// 鍧愭爣  
  
    private Rect normal = new Rect();// 鐭╁舰绌虹櫧  
  
    public MyScrollView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    /*** 
     * 鏍规嵁 XML 鐢熸垚瑙嗗浘宸ヤ綔瀹屾垚.璇ュ嚱鏁板湪鐢熸垚瑙嗗浘鐨勬渶鍚庤皟鐢紝鍦ㄦ墍鏈夊瓙瑙嗗浘娣诲姞瀹屼箣鍚? 鍗充娇瀛愮被瑕嗙洊浜?onFinishInflate 
     * 鏂规硶锛屼篃搴旇璋冪敤鐖剁被鐨勬柟娉曪紝浣胯鏂规硶寰椾互鎵ц. 
     */  
    @Override  
    protected void onFinishInflate() {  
        if (getChildCount() > 0) {  
            inner = getChildAt(0);// 鑾峰彇鍏跺瀛? 
        }  
    }  
  
    @Override  
    public boolean onTouchEvent(MotionEvent ev) {  
        if (inner != null) {  
            commOnTouchEvent(ev);  
        }  
        return super.onTouchEvent(ev);  
    }  
  
    /*** 
     * 瑙︽懜浜嬩欢 
     *  
     * @param ev 
     */  
    public void commOnTouchEvent(MotionEvent ev) {  
        int action = ev.getAction();  
        switch (action) {  
        case MotionEvent.ACTION_DOWN:  
            y = ev.getY();// 鑾峰彇鐐瑰嚮y鍧愭爣  
            break;  
        case MotionEvent.ACTION_UP:  
            if (isNeedAnimation()) {  
                animation();  
            }  
            break;  
        case MotionEvent.ACTION_MOVE:  
            final float preY = y;  
            float nowY = ev.getY();  
            int deltaY = (int) (preY - nowY);// 鑾峰彇婊戝姩璺濈  
  
            y = nowY;  
            // 褰撴粴鍔ㄥ埌鏈€涓婃垨鑰呮渶涓嬫椂灏变笉浼氬啀婊氬姩锛岃繖鏃剁Щ鍔ㄥ竷灞€  
            if (isNeedMove()) {  
                if (normal.isEmpty()) {  
                    // 濉厖鐭╁舰锛岀洰鐨勶細灏辨槸鍛婅瘔this:鎴戠幇鍦ㄥ凡缁忔湁浜嗭紝浣犳澗寮€鐨勬椂鍊欒寰楄鎵ц鍥炲綊鍔ㄧ敾.  
                    normal.set(inner.getLeft(), inner.getTop(),  
                            inner.getRight(), inner.getBottom());  
                }  
                // 绉诲姩甯冨眬  
                inner.layout(inner.getLeft(), inner.getTop() - deltaY / 2,  
                        inner.getRight(), inner.getBottom() - deltaY / 2);  
            }  
            break;  
  
        default:  
            break;  
        }  
    }  
  
    /*** 
     * 寮€鍚姩鐢荤Щ鍔?
     */  
    public void animation() {  
        // 寮€鍚Щ鍔ㄥ姩鐢? 
        TranslateAnimation ta = new TranslateAnimation(0, 0, inner.getTop(),  
                normal.top);  
        ta.setDuration(300);  
        inner.startAnimation(ta);  
        // 璁剧疆鍥炲埌姝ｅ父鐨勫竷灞€浣嶇疆  
        inner.layout(normal.left, normal.top, normal.right, normal.bottom);  
        normal.setEmpty();// 娓呯┖鐭╁舰  
  
    }  
  
    /*** 
     * 鏄惁闇€瑕佸紑鍚姩鐢?
     *  
     * 濡傛灉鐭╁舰涓嶄负绌猴紝杩斿洖true锛屽惁鍒欒繑鍥瀎alse. 
     *  
     *  
     * @return 
     */  
    public boolean isNeedAnimation() {  
        return !normal.isEmpty();  
    }  
  
    /*** 
     * 鏄惁闇€瑕佺Щ鍔ㄥ竷灞€ inner.getMeasuredHeight():鑾峰彇鐨勬槸鎺т欢鐨勯珮搴?
     * getHeight()锛氳幏鍙栫殑鏄綋鍓嶆帶浠跺湪灞忓箷涓樉绀虹殑楂樺害 
     *  
     * @return 
     */  
    public boolean isNeedMove() {  
        int offset = inner.getMeasuredHeight() - getHeight();  
        int scrollY = getScrollY();  
        // 0鏄《閮紝鍚庨潰閭ｄ釜鏄簳閮? 
        if (scrollY == 0 || scrollY == offset) {  
            return true;  
        }  
        return false;  
    }  
  
} 
