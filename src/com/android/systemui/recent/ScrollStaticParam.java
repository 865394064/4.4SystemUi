
package com.android.systemui.recent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.widget.LinearLayout;
import android.widget.TextView;

import android.annotation.SuppressLint;

import android.view.View;
import android.view.ViewPropertyAnimator;


public class ScrollStaticParam {

    public static boolean isScrollH = false;
    public static boolean isScrollV = false;
    private static LinearLayout container;

    private static MyScrollView scrollView;

    private static Map<Integer, Float> userinitList;

    public static boolean isShowItem = false;
    
    private static int favuserCount=0;
    private static TextView headerF,headerR;
    private static float startMoveX;
  private static float headerRX;
  
  private static float tempX;
  
  private static int type= -1;

    public static void setContainer(LinearLayout v) {

        container = v;

    }

    public static void setScrollView(MyScrollView view) {

        scrollView = view;
    }
    
    
    
    public static void setFavoriteCount(int count){
      favuserCount=count;
      headerRX=(favuserCount)*114;
      tempX=headerRX;
    }
    
    public static int getFavoriteCount(){
       return favuserCount;
          
      }
    
    public static void setHeaderF(TextView v){
        
        headerF=v;
    }
    
    
    public static float getDistanceHeaders(){
        
        
        return (favuserCount-1)*114-7;
    }
    
    
    @SuppressLint("NewApi")
    public static void setHeaderR(TextView v){
        
        headerR=v;
        
       
    }
    
    public static void setRecentsUserX(){
        
        if(container!=null){
            
          User user=  (User)container.getChildAt(favuserCount);
          if(user!=null)
          user.setorignalX(7+(favuserCount-1)*114);
        }
    }
    
    @SuppressLint("NewApi")
    public static void controlView(float delX,float moveX){
      
        
       // User user=  (User)container.getChildAt(favuserCount);
       // System.out.println(container.getChildAt(favuserCount).getX()-scrollView.getScrollX());
          
        if(headerR!=null&&container.getChildAt(favuserCount)!=null){
            
            if(favuserCount!=0){
           startMoveX =container.getChildAt(favuserCount).getX()-scrollView.getScrollX();
        
           System.out.println("startMoveX<headerRX="+(startMoveX<headerRX)+"  startMOveX="+startMoveX);
               if(startMoveX>=7&&startMoveX<=headerRX){
            
                   
                   
                    
                    final ViewPropertyAnimator viewPropertyAnimator = headerR.animate().x(
                            container.getChildAt(favuserCount).getX()-scrollView.getScrollX()); 
                     
                     viewPropertyAnimator.setDuration(0);
                     viewPropertyAnimator.start();

             
               }else if(startMoveX<7){
                   final ViewPropertyAnimator viewPropertyAnimator = headerR.animate().x(
                          7); 
                    
                    viewPropertyAnimator.setDuration(0);
                    viewPropertyAnimator.start();
                    
                }else if(startMoveX>headerRX){
                    
                    final ViewPropertyAnimator viewPropertyAnimator = headerR.animate().x(
                            headerRX); 
                     
                     viewPropertyAnimator.setDuration(0);
                     viewPropertyAnimator.start();
                }
               
            }else
            {
                
                final ViewPropertyAnimator viewPropertyAnimator = headerR.animate().x(
                        7); 
                  
                  viewPropertyAnimator.setDuration(0);
                  viewPropertyAnimator.start();
                
            }
               
        }
        
        if(headerF!=null&& container.getChildAt(favuserCount-1)!=null){
            
           if( container.getChildAt(favuserCount-1).getX()-scrollView.getScrollX()<=7){
               
               final ViewPropertyAnimator viewPropertyAnimator = headerF.animate().x(
                       container.getChildAt(favuserCount-1).getX()-scrollView.getScrollX()); 
                
                viewPropertyAnimator.setDuration(0);
                viewPropertyAnimator.start();
           }else{
               final ViewPropertyAnimator viewPropertyAnimator = headerF.animate().x(
                       7); 
                 
                 viewPropertyAnimator.setDuration(0);
                 viewPropertyAnimator.start();
               
           }
            
        }
                 
          
            

         
         
        // System.out.println("startMoveX=="+startMoveX+"  headerRX=="+headerRX);
         
  
         
 
         
        // System.out.println("======"+startMoveX+"  =="+moveX+"===="+delX);
        
    }
    
    
    private static void setTempHeadX(float x){
        
        tempX=x;
    }
    public static void setHeardRX(){
        
      //  headerRX=tempX;
    }
    
    @SuppressLint("NewApi")
    public static void setRecentsViewPosition(float x){
 
      
            final ViewPropertyAnimator viewPropertyAnimator = headerR.animate().x(
                   x); 
            
            viewPropertyAnimator.setDuration(0);
            viewPropertyAnimator.start();
     
    }
    
    private static float headerRNowX,headerFNowX;
    
    @SuppressLint("NewApi")
    public static void moveHeaders(int type){
        
        if(type==0){
            if(headerF.getVisibility()!=View.GONE)
            headerF.setVisibility(View.INVISIBLE);
            headerRNowX=headerR.getX();
           final ViewPropertyAnimator viewPropertyAnimator = headerR.animate().x(
                    7); 
              
              viewPropertyAnimator.setDuration(0);
              viewPropertyAnimator.start();
        }else if(type==1){
            if(headerR.getVisibility()!=View.GONE)
            headerR.setVisibility(View.INVISIBLE);
            
            headerFNowX=headerF.getX();
            final ViewPropertyAnimator viewPropertyAnimator = headerF.animate().x(
                    7); 
              
              viewPropertyAnimator.setDuration(0);
              viewPropertyAnimator.start();
        }
    }
    
    @SuppressLint("NewApi")
    public static void resetMoveHeaders(int type){
        if(type==0){
             if(headerF.getVisibility()==View.INVISIBLE)
            headerF.setVisibility(View.VISIBLE);
            final ViewPropertyAnimator viewPropertyAnimator = headerR.animate().x(
                    headerRNowX); 
              
              viewPropertyAnimator.setDuration(0);
              viewPropertyAnimator.start();
        }else if(type==1){
            if(headerR.getVisibility()==View.INVISIBLE)
            headerR.setVisibility(View.VISIBLE);
            final ViewPropertyAnimator viewPropertyAnimator = headerF.animate().x(
                    headerFNowX); 
              
              viewPropertyAnimator.setDuration(0);
              viewPropertyAnimator.start();
            
        }
        
    }
    
   
    
    

    @SuppressLint("NewApi")
    public static void reLayout(int index) {

        // RecentsPanel recents=rec.get(index);
        if (!isShowItem) {
            if (container != null) {

                if (userinitList == null) {

                    userinitList = new HashMap<Integer, Float>();
                }

                for (int i = 0; i < container.getChildCount(); i++) {
                    
                    userinitList.put(i, container.getChildAt(i).getX());
                }

                for (int i = 0; i < container.getChildCount(); i++) {
                    User temp = (User) container.getChildAt(i);
                    if (temp.getIndex() == index) {
                        type=temp.getType();
                        moveHeaders(temp.getType());
                        
                        final ViewPropertyAnimator viewPropertyAnimator = temp.animate().x(
                                0 + scrollView.getScrollX());

                        viewPropertyAnimator.setDuration(200);
                        // ValueAnimator valueAnimator = ValueAnimator.ofInt(1,
                        // 100);
                        viewPropertyAnimator.start();
                        // container.requestLayout();
                        // return;
                    } else if (temp.getIndex() < index) {

                        final ViewPropertyAnimator viewPropertyAnimator = temp.animate().x(
                                0 + scrollView.getScrollX() - 300);

                        viewPropertyAnimator.setDuration(200);
                        // ValueAnimator valueAnimator = ValueAnimator.ofInt(1,
                        // 100);
                        viewPropertyAnimator.start();
                    } else {
                        final ViewPropertyAnimator viewPropertyAnimator = temp.animate().x(
                                scrollView.getScrollX() + 540);

                        viewPropertyAnimator.setDuration(200);
                        // ValueAnimator valueAnimator = ValueAnimator.ofInt(1,
                        // 100);
                        viewPropertyAnimator.start();

                    }
                }
              
                isShowItem = true;

            }

        }

    }

    @SuppressLint("NewApi")
    public static void resetLayout(int index) {

        // RecentsPanel recents=rec.get(index);
        if (isShowItem) {
            Iterator<Integer> iter = userinitList.keySet().iterator();

            while (iter.hasNext()) {

                Integer key = iter.next();
                User temp = (User) container.getChildAt(key);

                final ViewPropertyAnimator viewPropertyAnimator = temp.animate().x(
                        userinitList.get(key));

                viewPropertyAnimator.setDuration(200);
                // ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
                viewPropertyAnimator.start();
                
                temp.setItemGone();
                
                
            }
            if(type!=-1){
                
                resetMoveHeaders(type);
                type=-1;
                
            }
            
            
            isShowItem=false;

        }
    }
}
