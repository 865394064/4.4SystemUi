package com.android.systemui.statusbar.phone;
/*create by xss on 20170630*/
import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.android.systemui.R;

public class LineBreakLayout extends ViewGroup {
    boolean DEBUG = true;
    String TAG ="app_lable";
    float childViewSpacing = 0;
    int childViewWidth,childViewHeight;
	private ArrayList<String> mAppLables;
	
	public LineBreakLayout(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public LineBreakLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
    
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();
		childViewSpacing = getContext().getResources().getDimension(R.dimen.control_center_child_view_spacing);
		childViewWidth = (int)getContext().getResources().getDimension(R.dimen.control_center_child_view_width);
		childViewHeight = childViewWidth;
		initData(getContext());		
	}

	public void initData(Context context){
		//mAppLables = getAddData(context);
		//Log.i("kay3", "ApplicationPreview  initData()  mAppLables===="+mAppLables);
		mAppLables = new ArrayList<String>();
		//if(mAppLables.size()==0){
            String [] Data= getResources().getStringArray(R.array.added_control_center_lables);
		    for(String data:Data){
		  	 mAppLables.add(data);
		    }		     
		//}
		setLables(mAppLables);     	
      }
	   
	/*private ArrayList<String> getAddData(Context  context){			
		int size = Settings.Global.getInt(context.getContentResolver(), "app_lables_size",0);
		if(DEBUG)Log.i(TAG, "  getAddData()  size===="+size);
		for (int i = 0; i < size; i++) 
		{
		    String item = Settings.Global.getString(context.getContentResolver(), "app_lables"+i);
		    if(DEBUG)Log.i(TAG, "  getAddData()    i=="+i+"   item===="+item); 	
		    List.add(item);
		}
		return List;
	}*/
	
	public void setLables(ArrayList<String> lables){

	   if(lables!=null && lables.size()>0){
	      LayoutInflater inflater = LayoutInflater.from(getContext());
	      for (final String lable : lables) {
	         
	         final ImageView childView = (ImageView) inflater.inflate(R.layout.control_center_child_view, null);
	         LayoutParams lp = new LayoutParams((int)childViewWidth, (int)childViewHeight);
	         childView.setLayoutParams(lp);
	         if(lable.equals("Flashlight")){
			 	 childView.setId(R.id.control_center_child_view_flashlight);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_flashlight_off));
	         }else if(lable.equals("Timer")){
				 childView.setId(R.id.control_center_child_view_timer);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_timer));
	         }else if(lable.equals("Calculator")){
				 childView.setId(R.id.control_center_child_view_calculator);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_calculator));
	         }else if(lable.equals("Camera")){
				 childView.setId(R.id.control_center_child_view_camera);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_camera));
	         }else if(lable.equals("QRCode")){
				 childView.setId(R.id.control_center_child_view_qr_code);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_qr_code));
	         }else if(lable.equals("Low Power mode")){
				 childView.setId(R.id.control_center_child_view_low_power_mode);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_low_power_mode_off));
	         }else if(lable.equals("Magnifier")){
				 childView.setId(R.id.control_center_child_view_magnifier);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_magnifier));
	         }else if(lable.equals("Do Not Disturb While Driving")){
				 childView.setId(R.id.control_center_child_view_driving);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_do_not_disturb_while_driving_off));
	         }else if(lable.equals("Accessibility Shortcuts")){
				 childView.setId(R.id.control_center_child_view_accessibility_shortcuts);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_accessibility_shortcuts));
	         }else if(lable.equals("Alarm")){
				 childView.setId(R.id.control_center_child_view_alarm);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_alarm));
	         }else if(lable.equals("Apple TV Remote")){
				 childView.setId(R.id.control_center_child_view_apple_tv_remote);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_apple_tv_remote));
	         }else if(lable.equals("Guided Access")){
				 childView.setId(R.id.control_center_child_view_guided_access);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_guided_access));
	         }else if(lable.equals("Notes")){
				 childView.setId(R.id.control_center_child_view_notes);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_notes));
	         }else if(lable.equals("Screen Recording")){
				 childView.setId(R.id.control_center_child_view_screen_recording);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_screen_recording_off));
	         }else if(lable.equals("Stopwatch")){
				 childView.setId(R.id.control_center_child_view_stopwatch);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_stop_watch));
	         }else if(lable.equals("Text Size")){
				 childView.setId(R.id.control_center_child_view_text_size);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_text_size));
	         }else if(lable.equals("Voice Memos")){
				 childView.setId(R.id.control_center_child_view_voice_memos);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_voice_memos));
	         }else if(lable.equals("Wallet")){
				 childView.setId(R.id.control_center_child_view_wallet);
	        	 childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_wallet));
	         }

	         /*switch (lable) {
				case "Flashlight":
					childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_flashlight));
					break;
                case "Timer":
                	childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_timer));
					break;
                case "Calculator":
                	childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_calculator));
					break;
                case "Camera":
                	childView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.control_center_child_camera));
					break;
                case "Flashlight":
					
					break;
                case "Flashlight":
					
					break;
                case "Flashlight":
					
					break;
                case "Flashlight":
					
					break;
                case "Flashlight":
					
					break;
                case "Flashlight":
					
					break;
                case "Flashlight":
					
					break;
                case "Flashlight":
					
					break;
	
				default:
					break;
			 }*/
	         addView(childView);
	      }
	   }
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	   
	   measureChildren(widthMeasureSpec, heightMeasureSpec);

	   
	   int heightMode = MeasureSpec.getMode(heightMeasureSpec);
	   int widthMode = MeasureSpec.getMode(widthMeasureSpec);
	   int heightSize = MeasureSpec.getSize(heightMeasureSpec);
	   
	   int widthSize = MeasureSpec.getSize(widthMeasureSpec);
	   Log.i(TAG , "onMeasure()     widthSize=:"+widthSize+"   heightSize=:"+heightSize+"    isLandScreen()="+isLandScreen());
	   int height = 0,width = 0;
	   if(isLandScreen()){
                 if (widthMode == MeasureSpec.EXACTLY) {
		      
		      width = widthSize;
		   } else {
		      
		      int childCount = getChildCount();
		      if(childCount<=0){
		         width = 0;   
		      }else{
		         int column = 1;  
		         int heightSpace = heightSize;
		         for(int i = 0;i<childCount; i++){
		            View view = getChildAt(i);
		            
		            int childW = view.getMeasuredWidth();
		            Log.i(TAG , "childW=:"+childW +" column=："+column+"  heightSpace=："+heightSpace);
		            if(heightSpace >= childW ){
		               
		               heightSpace -= childW;
		            }else{
		               column ++;    
		               
		               heightSpace = heightSize-childW;
		            }
		            
		            heightSpace -= childViewSpacing;
		         }
		         
		         int childH = getChildAt(0).getMeasuredHeight();
		         
		         width = (childH * column) + (int)childViewSpacing * (column-1);
                       height = heightSize;
		         Log.i(TAG , "height=:"+height +" column=："+column+"  childH=："+childH);
		      }
	         }
	   }else{
		   if (heightMode == MeasureSpec.EXACTLY) {
		      
		      height = heightSize;
		   } else {
		      
		      int childCount = getChildCount();
		      if(childCount<=0){
		         height = 0;   
		      }else{
		         int row = 1;  
		         int widthSpace = widthSize;
		         for(int i = 0;i<childCount; i++){
		            View view = getChildAt(i);
		            
		            int childW = view.getMeasuredWidth();
		            Log.i(TAG , "childW=:"+childW +" row=："+row+"  widthSpace=："+widthSpace);
		            if(widthSpace >= childW ){
		               
		               widthSpace -= childW;
		            }else{
		               row ++;    
		               
		               widthSpace = widthSize-childW;
		            }
		            
		            widthSpace -= childViewSpacing;
		         }
		         
		         int childH = getChildAt(0).getMeasuredHeight();
		         
		         height = (childH * row) + (int)childViewSpacing * (row-1);
                       width = widthSize;   
		         Log.i(TAG , "height=:"+height +" row=："+row+"  childH=："+childH);
		      }
	         }
	   }
	   Log.i(TAG , "onMeasure()   end  width=:"+width+"  height=:"+height);
	   setMeasuredDimension(width, height);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
	   int row = 0,column = 0;
	   int right = 0;   
	   int bottom = 0; 
	    Log.i(TAG, "onLayout()    isLandScreen()="+isLandScreen()+"   left="+l+"  top="+t+"   right="+r+"   bottom="+b);
	   for (int i = 0; i < getChildCount(); i++) {
	      View childView = getChildAt(i);
	      int childW = childView.getMeasuredWidth();
	      int childH = childView.getMeasuredHeight();
	      if(isLandScreen()){
                     bottom += childH;
	      
		      right = column * (childW + (int)childViewSpacing) + childW;
		      
		      // if it can't drawing on a same line , skip to next line
		      if (bottom > b){
		         column++;
		         bottom = childH;
		         right = column * (childW + (int)childViewSpacing) + childW;
		      }
		      Log.i(TAG, "child  i="+i+"   left = " + (right - childW) +" top = " + (bottom - childH)+
		            " right = " + right + " bottom= " + bottom);
		      childView.layout(right - childW, bottom - childH,right,bottom);

		       bottom += (int)childViewSpacing;
	      }else{
                     right += childW;
	      
		      bottom = row * (childH + (int)childViewSpacing) + childH;
		      
		      // if it can't drawing on a same line , skip to next line
		      if (right > r){
		         row++;
		         right = childW;
		         bottom = row * (childH + (int)childViewSpacing) + childH;
		      }
		      Log.i(TAG, "child  i="+i+"   left = " + (right - childW) +" top = " + (bottom - childH)+
		            " right = " + right + " bottom = " + bottom);
		      childView.layout(right - childW, bottom - childH,right,bottom);

		       right += (int)childViewSpacing;
	      }
	     
	   }
	}

	public boolean isLandScreen() {

		Configuration mConfiguration = getContext().getResources().getConfiguration(); 
		int ori = mConfiguration.orientation ; 

		if(ori == mConfiguration.ORIENTATION_LANDSCAPE){
		    return true;
		}else if(ori == mConfiguration.ORIENTATION_PORTRAIT){
		    return false;
		} 
	      return false;
    }	
}
