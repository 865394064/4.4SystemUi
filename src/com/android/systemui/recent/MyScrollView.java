package com.android.systemui.recent;



import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.HorizontalScrollView;

public class MyScrollView extends HorizontalScrollView{
	 private GestureDetector mGestureDetector; 
	 
	 private float startX;
	 private float moveX;
	 
	 private float movedX=-1;


        /**
         * @function CustomHScrollView constructor
         * @param context  Interface to global information about an application environment. 
         * 					
         */
        public MyScrollView(Context context) {
                super(context);
                // TODO Auto-generated constructor stub
            	mGestureDetector = new GestureDetector(new HScrollDetector()); 
        	    setFadingEdgeLength(0); 
        }
        
        
        /**
         * @function CustomHScrollView constructor  
         * @param context Interface to global information about an application environment. 
         * @param attrs A collection of attributes, as found associated with a tag in an XML document.
         */
        public MyScrollView(Context context, AttributeSet attrs) {
            super(context, attrs);
            // TODO Auto-generated constructor stub
        	mGestureDetector = new GestureDetector(new HScrollDetector()); 
    	    setFadingEdgeLength(0); 
        }

        /**
         * @function  CustomHScrollView constructor  
         * @param context Interface to global information about an application environment. 
         * @param attrs A collection of attributes, as found associated with a tag in an XML document.
         * @param defStyle style of view
         */
        public MyScrollView(Context context, AttributeSet attrs,
                        int defStyle) {
                super(context, attrs, defStyle);
                // TODO Auto-generated constructor stub
            	mGestureDetector = new GestureDetector(new HScrollDetector()); 
        	    setFadingEdgeLength(0); 
        }
        
        @Override
   	 public boolean onInterceptTouchEvent(MotionEvent ev) { 
            
        /*  switch(ev.getAction()){
              case  MotionEvent.ACTION_DOWN:
                  
                  startX=ev.getX();
                  
                  System.out.println("event down="+startX);
                  break;
                  
         case  MotionEvent.ACTION_UP:
                  
           
                 
                  break;
              
          }*/
		android.util.Log.d("recentsPanel"," mSCROLL.isScrollV="+ScrollStaticParam.isScrollV);
           /*  if(ScrollStaticParam.isScrollV){
                return false;
            }else{
                return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev); 
            }*/
          
          return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev); 
   	    
   	 } 
        
        @Override
        public boolean onTouchEvent(MotionEvent event){
            
            //System.out.println("event.getAction=="+event.getAction());
            switch(event.getAction()){
         
                case   MotionEvent.ACTION_DOWN:
                    startX=event.getX();
                  System.out.println("event down="+startX);
                    break;
            case MotionEvent.ACTION_MOVE:  
           
                moveX=event.getX();
              // System.out.println("moveX=="+moveX);
                if(!ScrollStaticParam.isShowItem)
                ScrollStaticParam.controlView(startX,moveX);
              /*  if(movedX==-1){
                    if(Math.abs(startX-moveX)>=10){
                        movedX=moveX;
                        ScrollStaticParam.controlView(Math.abs(startX-event.getRawX()));
                    }
                }else if(movedX==moveX){
                    
                    ScrollStaticParam.controlView(Math.abs(startX-event.getRawX()));
                }
                
                System.out.println(" movedX=="+movedX+"  moveX=="+moveX);
                
              
                
                //ScrollStaticParam.controlView(Math.abs(startX-event.getRawX()));*/
                if(Math.abs((startX-event.getRawX()))>3&&ScrollStaticParam.isShowItem){
                   
                    ScrollStaticParam.resetLayout(0);
                }
                   
                break;
                
            case MotionEvent.ACTION_UP:
               // ScrollStaticParam.controlView(Math.abs(startX-event.getRawX()));
                
                System.out.println("action up");
                ScrollStaticParam.setHeardRX();
                ScrollStaticParam.controlView(0, 0);
                break;
            }
            super.onTouchEvent(event);
            
            
            
            return true;
        }
        
   	  
   	    // Return false if we're scrolling in the y direction   
   	 class HScrollDetector extends SimpleOnGestureListener { 
   	        @Override
   	    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {        
   	         if(Math.abs(distanceX) > Math.abs(distanceY)) { 
   	                return true; 
   	         } 
   	        	
   	         return false; 
   	    } 
   	 } 

}
