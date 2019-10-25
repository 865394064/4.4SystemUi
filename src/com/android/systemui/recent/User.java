
package com.android.systemui.recent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.R;



public class User extends FrameLayout implements View.OnClickListener {
    private String telNum;
    private String contactName;
    private Bitmap contactIcon;

    private ImageView contacts,phone,message;

    private TextView label,headerTitle;
    private LinearLayout requestItem;
    private Context mContext;
    

    private RelativeLayout recentItem;

 

    private int index = -1;
    
    private int type=-1;
    
    private float orignalX;

    


    public User(Context context) {
        super(context);
        mContext=context;
        initViews(R.layout.simple_adapter);
    }

    private void initViews(int res) {

        LayoutInflater.from(getContext()).inflate(R.layout.simple_adapter, this);
        contacts = (ImageView) findViewById(R.id.app_icon);

        label = (TextView) findViewById(R.id.app_label);

        requestItem = (LinearLayout) findViewById(R.id.requst_item);
        recentItem=(RelativeLayout)findViewById(R.id.recent_item);
        
        phone=(ImageView) findViewById(R.id.phone);
        message=(ImageView) findViewById(R.id.message);
        
       // headerTitle=(TextView)findViewById(R.id.header_title);
        
       // headerView=(HeaderView) findViewById(R.id.header);
        
        recentItem.setOnClickListener(this);
        phone.setOnClickListener(this);
        message.setOnClickListener(this);
        
        
    }
    
    
    

    /*@Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        // return super.onInterceptTouchEvent(ev);
      //  android.util.Log.d("recentsPanel", "onInterceptTouchEvent");
       

         return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 閹稿绗?
   
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            System.out.println("11111====="+event.getX());
              
            
        } else if (event.getAction() == MotionEvent.ACTION_UP) {

           // handleUp(event);
         
            
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {

            //handleMove(event);
           
        }
        else if (event.getAction() == MotionEvent.ACTION_CANCEL) {

            android.util.Log.d("recentsPanel", "MotionEvent.ACTION_CANCEL");
            //handleUp(event);

            
        }
      return  super.onTouchEvent(event);
    }*/
    
    
    public void setorignalX(float x)
    {
        this.orignalX=x;
    }
    
    public float getorignalX( )
    {
       return orignalX;
    }
    
    public void setType(int type){
        this.type=type;
    }
    
    
    public int getType(){
    return type;
    }
    
    
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
   
    
    
    public void setItemGone( ){
        
        requestItem.setVisibility(View.GONE);
        
    }

    public void setLabel(String labelName) {

        if (label != null)
            label.setText(labelName);
    }

    public void setContact(Bitmap bitmap) {

        if (contacts != null)
            contacts.setImageBitmap(bitmap);
    }

    public void setPhoneNumber(String number) {
        telNum = number;
    }

    public void setUserName(String name) {
        contactName = name;
    }

    public String getPhoneNumber() {
        return telNum;
    }

    public String getUserName() {
        return contactName;
    }

    public Bitmap getContactIcon() {
        return contactIcon;
    }

    public void setContactIcon(Bitmap contactIcon) {
        this.contactIcon = contactIcon;
    }
    
    
  
    
    
    public void setHeaderTitle(int  header) {
        headerTitle.setText(header);
    }
    
    @Override  
    public boolean equals(java.lang.Object obj) {  
        if (!(obj instanceof User)) {  
            return false;  
        }  
        User workItem = (User) obj;  
        return contactName.equals(workItem.contactName);  
    }  
  
    @Override  
    public int hashCode() {  
        //hashCode \u53ea\u662f\u4e00\u4e2a\u6570\u503c\uff0c\u6709N\u79cd\u5b9e\u73b0\u65b9\u5f0f\uff0c\u8fd9\u53ea\u662f\u666e\u901a\u7684\u4e00\u79cd  
        return contactName.hashCode();  
    } 

   /* public boolean equals(User user) {

        if (user.getUserName().equals(this.contactName)
                && user.getPhoneNumber().equals(this.telNum)) {
            return true;
        } else {

            return false;
        }

    }*/

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
        if(v.getId()==R.id.phone){
            Intent callIntent  = new Intent();   
            
            callIntent.setAction("android.intent.action.CALL");  
             callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            callIntent.setData(Uri.parse("tel:"+telNum)); 
            mContext.startActivity(callIntent);
        }else if(v.getId()==R.id.message){
            Uri uri = Uri.parse("smsto://"+telNum);  
            Intent it = new Intent(Intent.ACTION_SENDTO, uri);  
            
            mContext.startActivity(it); 
            
            
        }else if(v.getId()==R.id.recent_item){
            
            if(requestItem.getVisibility()==View.GONE){
                ScrollStaticParam.reLayout(index);
                //ScrollStaticParam.moveHeaders(type);
             requestItem.setVisibility(View.VISIBLE);
            }else{
                  
                ScrollStaticParam.resetLayout(index);
               // ScrollStaticParam.resetMoveHeaders(type);
                requestItem.setVisibility(View.GONE);
            }
        }
    }
}
