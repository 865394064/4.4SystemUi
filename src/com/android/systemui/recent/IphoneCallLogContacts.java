
package com.android.systemui.recent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;
import android.util.AttributeSet;
import android.view.ViewGroup;
import java.util.HashSet;
import android.content.AsyncQueryHandler;


import com.android.systemui.statusbar.phone.PhoneStatusBar;

public class IphoneCallLogContacts extends FrameLayout {


    private Map<Integer, User> listcallArray;
    private Map<Integer, User> favoriteArray;

    private LinearLayout recentsContainer,headerContainer;
    private Context mContext = null;
    private TextView favorite,recents,noRecents;
    private MyScrollView scrollview = null;
    private boolean isShowF=true;
    private boolean isShowR=true;
    private int count=0;
    private final QueryHandler mQueryHandler;
    private final QueryFavoriteHandler mQueryFavoriteHandler;

    private List<User> listcallUsers;

    public IphoneCallLogContacts(Context context) {
        this(context,null);

       
    }


 public IphoneCallLogContacts(Context context, AttributeSet attrs) {
        super(context, attrs);

 	mContext = context;
        LayoutInflater mInflater = LayoutInflater.from(context);

        View root = mInflater.inflate(R.layout.activity_main, this, true);
        scrollview = (MyScrollView) root.findViewById(R.id.scroll_view);
        recentsContainer = (LinearLayout) root.findViewById(R.id.recents_linear_layout);
        headerContainer=(LinearLayout) root.findViewById(R.id.header_container);
        favorite=(TextView)root.findViewById(R.id.favoriate_user);
        
        recents=(TextView)root.findViewById(R.id.recents_user);
          noRecents=(TextView)root.findViewById(R.id.no_recents_contacts);
        
        ScrollStaticParam.setHeaderF(favorite);
        ScrollStaticParam.setHeaderR(recents);

        mQueryHandler=new QueryHandler(context.getContentResolver());
        mQueryFavoriteHandler= new QueryFavoriteHandler(context.getContentResolver());
        
        mQueryHandler.startQuery(0, null, CallLog.Calls.CONTENT_URI, new String[] {
                CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE, CallLog.Calls.DATE
        }, CallLog.Calls.TYPE+"='"+2+"' AND "+CallLog.Calls.CACHED_NAME+"!='"+null+"'" ,null,CallLog.Calls.DEFAULT_SORT_ORDER+ " limit 10");
    }


    @SuppressLint("NewApi")
    public void initData(Cursor cursor) {

       /*获取最近联系人和常用联系人*/
          
        //listcallArray = getCallLogUser(mContext);

             

       favoriteArray = getKeepedContacts(cursor);
        
           
        
        /*放入布局中*/
        Iterator<Integer> iter1 = favoriteArray.keySet().iterator();

        while (iter1.hasNext()) {
            
            Integer key = iter1.next();
            favoriteArray.get(key).setIndex(count);
            recentsContainer.addView(favoriteArray.get(key));
            count++;

        }
        
        if(count!=0)
        ScrollStaticParam.setRecentsViewPosition((count-1)*114);
       
            
        
        ScrollStaticParam.setFavoriteCount(count);
        
        
        
        
       Iterator<Integer> iter = listcallArray.keySet().iterator();

        while (iter.hasNext()) {

            Integer key = iter.next();
            listcallArray.get(key).setIndex(count);

            if(((ViewGroup) listcallArray.get(key).getParent())!=null)
             ((ViewGroup) listcallArray.get(key).getParent()).removeView(listcallArray.get(key)); 
            recentsContainer.addView(listcallArray.get(key));
            count++;
        }
        
      
        ScrollStaticParam.setRecentsUserX();
        


        /*控制View的移动*/
        ScrollStaticParam.setContainer(recentsContainer);
        ScrollStaticParam.setScrollView(scrollview);
        //ScrollStaticParam.resetLayout(0);
     
        if(count==0){
             noRecents.setVisibility(View.VISIBLE);
          }else{
            noRecents.setVisibility(View.GONE);
         }
    }

        public void generateList(Cursor cursor){

     
             listcallArray = getCallLogUser(cursor);
             
               mQueryFavoriteHandler.startQuery( 0,null,ContactsContract.Contacts.CONTENT_URI, null,
                ContactsContract.Contacts.STARRED + " =  1 ", null, null);
        }

 
    private final class QueryHandler extends AsyncQueryHandler {  
        public QueryHandler(ContentResolver cr) {  
            super(cr);  
        }  
  
        @Override  
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {  
            super.onQueryComplete(token, cookie, cursor);  
       
            generateList(cursor);
         
        }  
    }  


     private final class QueryFavoriteHandler extends AsyncQueryHandler {  
        public QueryFavoriteHandler(ContentResolver cr) {  
            super(cr);  
        }  
  
        @Override  
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {  
            super.onQueryComplete(token, cookie, cursor);  
           
           initData(cursor);

            
        }  
    }  

    
    
    
    /*判断是否是已拨号号码*/
    
    public boolean isCallLog(String name,Context context){
        
        ContentResolver cr = context.getContentResolver();
        final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, new String[] {
                CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE, CallLog.Calls.DATE
        }, CallLog.Calls.CACHED_NAME+"='"+name+"'", null,
                CallLog.Calls.DEFAULT_SORT_ORDER);
        int num=cursor.getCount();
        cursor.close();
        if(num>0){
            
            return true;
        }
        
        return false;
    }


    /* 获取已拨电话中的联系人列表 */
    @SuppressLint("NewApi")
    public Map<Integer, User> getCallLogUser(Cursor cursor) {

        List<User> listcallUsers = new ArrayList<User>();
        ContentResolver cr = mContext.getContentResolver();
      /*  final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, new String[] {
                CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME
        },CallLog.Calls.TYPE+"='"+2+"' AND "+CallLog.Calls.CACHED_NAME+"!='"+null+"'", null,
                CallLog.Calls.DEFAULT_SORT_ORDER+" limit 1");*/
        
        for (int i = 0; i < cursor.getCount(); i++) {
            User user = new User(mContext);
            cursor.moveToPosition(i);
            String str = cursor.getString(0);
            //int type = cursor.getInt(2);
            String name = cursor.getString(1);
           
                Bitmap icon = getContactIcon(name, cr);
                user.setPhoneNumber(str);
                user.setContactIcon(icon);
                user.setContact(icon);
                user.setLabel(name);
                user.setUserName(name);
                user.setType(0);
    
            if (user.getContactIcon() != null)
                listcallUsers.add(user);

        } // 取得值

        cursor.close();

          removeOrder(listcallUsers);

        if (listcallArray == null) {

            listcallArray = new HashMap<Integer, User>();
        }

        for (int i = 0; i < listcallUsers.size(); i++) {
            User user = listcallUsers.get(i);
            user.setIndex(i);
            if(isShowR){
                
                isShowR=false;
               recents.setVisibility(View.VISIBLE);
       
             // user.setHeaderVisibility(true);
               // user.setHeaderTitle(context.getResources().getString(R.string.recents_user));
            }
            listcallArray.put(i, user);
        }

        // return listcallUsers;
        return listcallArray;
    }





    

    /* 获取已拨电话中的联系人列表 */
    @SuppressLint("NewApi")
    public Map<Integer, User> getCallLogUser(Context context) {

        List<User> listcallUsers = new ArrayList<User>();
        ContentResolver cr = context.getContentResolver();
        final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, new String[] {
                CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME
        },CallLog.Calls.TYPE+"='"+2+"' AND "+CallLog.Calls.CACHED_NAME+"!='"+null+"'", null,
                CallLog.Calls.DEFAULT_SORT_ORDER+" limit 1");
        
        for (int i = 0; i < cursor.getCount(); i++) {
            User user = new User(mContext);
            cursor.moveToPosition(i);
            String str = cursor.getString(0);
            //int type = cursor.getInt(2);
            String name = cursor.getString(1);
           
                Bitmap icon = getContactIcon(name, cr);
                user.setPhoneNumber(str);
                user.setContactIcon(icon);
                user.setContact(icon);
                user.setLabel(name);
                user.setUserName(name);
                user.setType(0);
    
            if (user.getContactIcon() != null)
                listcallUsers.add(user);

        } // 取得值

        cursor.close();

          removeOrder(listcallUsers);

        if (listcallArray == null) {

            listcallArray = new HashMap<Integer, User>();
        }

        for (int i = 0; i < listcallUsers.size(); i++) {
            User user = listcallUsers.get(i);
            user.setIndex(i);
            if(isShowR){
                
                isShowR=false;
               recents.setVisibility(View.VISIBLE);
       
             // user.setHeaderVisibility(true);
               // user.setHeaderTitle(context.getResources().getString(R.string.recents_user));
            }
            listcallArray.put(i, user);
        }

        // return listcallUsers;
        return listcallArray;
    }


     /** 
     * \u5220\u9664\u5e76\u4fdd\u6301\u539f\u6765\u7684\u987a\u5e8f 
     *  
     * @param list 
     */  
    public static void removeOrder(List<User> list) {  
        HashSet<User> set = new HashSet<User>();  
        ArrayList<User> newList = new ArrayList<User>();  
        for (Iterator<User> iter = list.iterator(); iter.hasNext();) {  
            User element = iter.next();  
            if (set.add(element)) {  
                newList.add(element);  
            }  
        }  
        list.clear();  
        list.addAll(newList);  
    }  
    



public Map<Integer, User> getCallLogUser(List<User> listcallUsers) {

      /*  List<User> listcallUsers = new ArrayList<User>();
        ContentResolver cr = context.getContentResolver();
        final Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, new String[] {
                CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME,
                CallLog.Calls.TYPE, CallLog.Calls.DATE
        },null, null,
                CallLog.Calls.DEFAULT_SORT_ORDER);
        
        for (int i = 0; i < cursor.getCount(); i++) {
            User user = new User(mContext);
            cursor.moveToPosition(i);
            String str = cursor.getString(0);
            int type = cursor.getInt(2);
            String name = cursor.getString(1);
            if (type == 2) {
                Bitmap icon = getContactIcon(name, cr);
                user.setPhoneNumber(str);
                user.setContactIcon(icon);
                user.setContact(icon);
                user.setLabel(name);
                user.setUserName(name);
                user.setType(0);
            }
            if (user.getContactIcon() != null)
                listcallUsers.add(user);

        } // 取得值

        cursor.close();

        for (int i = 0; i < listcallUsers.size(); i++) {
            for (int j = i + 1; j < listcallUsers.size(); j++) {
                if (listcallUsers.get(i).equals(listcallUsers.get(j))) {

                    listcallUsers.remove(j);
                }

            }

        }*/

     
        if (listcallArray == null) {

            listcallArray = new HashMap<Integer, User>();
        }

        for (int i = 0; i < listcallUsers.size(); i++) {
            User user = listcallUsers.get(i);
            user.setIndex(i);
            if(isShowR){
                
                isShowR=false;
               recents.setVisibility(View.VISIBLE);
       
             // user.setHeaderVisibility(true);
               // user.setHeaderTitle(context.getResources().getString(R.string.recents_user));
            }
            listcallArray.put(i, user);
        }

        // return listcallUsers;
        return listcallArray;
    }


    public void setIphoneCallLogUsers(List<User> iphoneCallUsers){
        listcallUsers=iphoneCallUsers;


        }







         /**
     * 获得收藏夹的联系人
     */
    private Map<Integer, User> getKeepedContacts(Cursor cur) {
        
        if (favoriteArray == null)
            favoriteArray = new HashMap<Integer, User>();
        ContentResolver cr = mContext.getContentResolver();
      /*  Cursor cur = cr.query(
                ContactsContract.Contacts.CONTENT_URI, null,
                ContactsContract.Contacts.STARRED + " =  1 ", null, null);*/

        int size=0;
        if(cur.getCount()>=5){
            size=5;
            }else{
            size=cur.getCount();
          }
        
       for(int i=0;i<size;i++){
           User user = new User(mContext);
           
            cur.moveToPosition(i);
            
            
            long id = cur.getLong(cur.getColumnIndex("_id")); 
            Cursor pcur = cr.query( 
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                    null, 
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" 
                            + Long.toString(id), null, null); 
  
            // 处理多个号码的情况 
            String phoneNumbers = ""; 
            while (pcur.moveToNext()) { 
                phoneNumbers = pcur 
                        .getString(pcur 
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)); 
            }
            
            pcur.close();
    
        
            int type = cur.getInt(2);
            String name = cur.getString(cur.getColumnIndex("display_name"));
            System.out.println(" favoriteArray==="+phoneNumbers+" ==== "+name+" "+" type=="+type);
           if (!isCallLog(name,mContext)) {
             
                Bitmap icon = getContactIcon(name,cr);
                user.setPhoneNumber(phoneNumbers);
                user.setContactIcon(icon);
                user.setContact(icon);
                user.setLabel(name);
                user.setUserName(name);
                user.setType(1);
                if(isShowF){
                    
                    isShowF=false;
                    favorite.setVisibility(View.VISIBLE);
                    
                    user.setorignalX(8);
                    //user.setHeaderVisibility(true);
                    //user.setHeaderTitle(context.getResources().getString(R.string.favoriate_user));
                }
                
               if (user.getContactIcon() != null)
                   favoriteArray.put(i, user);
            
            }

        }
        cur.close();
        

        
        
        

        return favoriteArray;
    }
    
    
    
    
    /**
     * 获得收藏夹的联系人
     */
    private Map<Integer, User> getKeepedContacts(Context context) {
        
        if (favoriteArray == null)
            favoriteArray = new HashMap<Integer, User>();
        ContentResolver cr = context.getContentResolver();
        Cursor cur = cr.query(
                ContactsContract.Contacts.CONTENT_URI, null,
                ContactsContract.Contacts.STARRED + " =  1 ", null, null);

 
       for(int i=0;i<cur.getCount();i++){
           User user = new User(context);
           
            cur.moveToPosition(i);
            
            
            long id = cur.getLong(cur.getColumnIndex("_id")); 
            Cursor pcur = cr.query( 
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI, 
                    null, 
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" 
                            + Long.toString(id), null, null); 
  
            // 处理多个号码的情况 
            String phoneNumbers = ""; 
            while (pcur.moveToNext()) { 
                phoneNumbers = pcur 
                        .getString(pcur 
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)); 
            }
            
            pcur.close();
    
        
            int type = cur.getInt(2);
            String name = cur.getString(cur.getColumnIndex("display_name"));
            System.out.println(" favoriteArray==="+phoneNumbers+" ==== "+name+" "+" type=="+type);
           if (!isCallLog(name,context)) {
             
                Bitmap icon = getContactIcon(name,cr);
                user.setPhoneNumber(phoneNumbers);
                user.setContactIcon(icon);
                user.setContact(icon);
                user.setLabel(name);
                user.setUserName(name);
                user.setType(1);
                if(isShowF){
                    
                    isShowF=false;
                    favorite.setVisibility(View.VISIBLE);
                    
                    user.setorignalX(8);
                    //user.setHeaderVisibility(true);
                    //user.setHeaderTitle(context.getResources().getString(R.string.favoriate_user));
                }
                
               if (user.getContactIcon() != null)
                   favoriteArray.put(i, user);
            
            }

        }
        cur.close();
        

        
        
        

        return favoriteArray;
    }
    
    
    /*根据联系人姓名获取联系人头像*/

    public Bitmap getContactIcon(String name, ContentResolver cr) {

        Bitmap btContactImage = null;
        // 通话电话号码获取头像uri
        Uri uriNumber2Contacts = Uri
                .parse("content://com.android.contacts/"
                        + "data/phones/filter/" + name);
        Cursor cursorCantacts = cr.query(uriNumber2Contacts, null, null,
                null, null);
        if (cursorCantacts.getCount() > 0) { // 若游标不为0则说明有头像,游标指向第一条记录
            cursorCantacts.moveToFirst();
            Long contactID = cursorCantacts.getLong(cursorCantacts
                    .getColumnIndex("contact_id"));
            Uri uri = ContentUris.withAppendedId(
                    ContactsContract.Contacts.CONTENT_URI, contactID);
            InputStream input = ContactsContract.Contacts
                    .openContactPhotoInputStream(
                            cr, uri);
            btContactImage = BitmapFactory.decodeStream(input);

            if (btContactImage != null) {
                btContactImage = getRoundedCornerBitmap(btContactImage);

            } else {

                btContactImage = BitmapFactory.decodeResource(getResources(),
                        R.drawable.contacts_icon);
            }

        }

        cursorCantacts.close();

        return btContactImage;
    }

    /*   public boolean isAContacts(String mNumber) {

        Cursor cursor = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, // 返回字段
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = '" + mNumber + "'", //
                null, // WHERE clause value substitution
                null); // Sort order.
        if (cursor.getCount() > 0) {
            cursor.close();
            return true;
        }

        cursor.close();
        return false;
    }

  public static boolean getContact(Context context, String num) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);// 查询通讯录
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));// 联系人id
                String name = cursor.getString(cursor
                        .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));// 联系人名称
                System.out.println("name:" + name);
                if (cursor
                        .getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    // Query phone here. Covered next 在该处查询电话号码
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[] {
                                id
                            }, null);
                    while (pCur.moveToNext()) {
                        // Do something with phones
                        String phoneNumber = pCur.getString(pCur
                                .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        if (phoneNumber.equals(num)) {

                            return true;
                        }
                        System.out.println("number:" + phoneNumber);
                    }
                    pCur.close();

                }

            }
        }

        return false;
    }*/

    /* get All Contacts */

    /* public List<User> getUsers(Context context) {
        List<User> listUser = new ArrayList<User>();
        ContentResolver resolver = context.getContentResolver();
        String phoneNumber = "";
        // 获得所有的联系人
        Cursor cur = resolver.query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.Contacts.DISPLAY_NAME
                        + " COLLATE LOCALIZED ASC");
        // 循环遍历
        if (cur.moveToFirst()) {

            int idColumn = cur.getColumnIndex(ContactsContract.Contacts._ID);

            int displayNameColumn = cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            do {

                User user = new User(mContext);
                String contactId = cur.getString(idColumn);
                System.out.println("contactId:" + contactId);
                // 获得联系人姓名
                String disPlayName = cur.getString(displayNameColumn);
                user.setUserName(disPlayName);
                System.out.println("disPlayName:" + disPlayName);
                // 查看该联系人有多少个电话号码。如果没有这返回值为0
                int phoneCount = cur.getInt(cur
                        .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                if (phoneCount > 0) {
                    // 获得联系人的电话号码
                    Cursor phones = resolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                                    + " = " + contactId, null, null);
                    if (phones.moveToFirst()) {
                        do {
                            // 遍历所有的电话号码
                            int id = phones.getInt(phones
                                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                            phoneNumber = phones
                                    .getString(phones
                                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        } while (phones.moveToNext());
                    }
                }
                System.out.println("phoneNumber:" + phoneNumber);
                user.setPhoneNumber(phoneNumber);
                listUser.add(user);

            } while (cur.moveToNext());
        }
        return listUser;
    }*/

    /* generate rounded bitmap 生成圆形联系人图片 */

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = bitmap.getWidth() / 2;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static String queryNameByNum(String num, Context context)
    {
        Cursor cursorOriginal =
                context.getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[] {
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                        },
                        ContactsContract.CommonDataKinds.Phone.NUMBER + "='" + num + "'", null,
                        null);
        if (null != cursorOriginal)
        {
            if (cursorOriginal.getCount() > 1)
            {
                return null;
            } else {
                if (cursorOriginal.moveToFirst())
                {
                    return cursorOriginal.getString(cursorOriginal
                            .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                } else
                {
                    return null;
                }
            }
        } else
        {
            return null;
        }
    }



}
