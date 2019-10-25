package com.android.systemui.unreadevents;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.CallLog.Calls;
import android.text.format.DateFormat;
import android.util.Log;
import com.android.internal.R;

public class MissCallUnReadObserver extends UnReadObserver {
    
    private static final String TAG = "MissCallUnReadObserver";

	private static final int INDEX_ID = 0;
	private static final int INDEX_NEW = 1;
	private static final int INDEX_DATE = 2;
	private static final int INDEX_NUMBER = 3;
	private static final int INDEX_NAME = 4;
    
    public static final Uri MISS_CALL_URI = Calls.CONTENT_URI;
    private static final String[] MISS_CALL_PROJECTION = new String[] {Calls._ID, Calls.NEW, Calls.DATE, Calls.NUMBER, Calls.CACHED_NAME};
    private static final String[] MISS_CALL_ISREAD=new String[]{Calls.IS_READ};
    private static final String MISS_CALL_SELECTION = "(" + Calls.NEW + " = ? AND " +
            Calls.TYPE + " = ? AND " + Calls.IS_READ  + " = ? AND " + Calls.DATE + " >= ";
    private static final String[] MISS_CALL_SELECTION_ARGS = new String[] {"1", Integer.toString(Calls.MISSED_TYPE), Integer.toString(0)};
    
    public MissCallUnReadObserver(Handler handler, UnReadMessageLayout newEventView, long createTime) {
        super(handler, newEventView, createTime);
    }
	

    public void refreshAllMessage(){
        
        new AsyncTask<Void, Void, List<UnReadMessageView>>() {
               @Override
               public List<UnReadMessageView> doInBackground(Void... params) {
    		   List<UnReadMessageView> list = new ArrayList<UnReadMessageView>();
                   final int size = mNewEventView.mContentLayout.getChildCount();
                   for(int i=0;i<size;i++) {
                   	 UnReadMessageView view = (UnReadMessageView) mNewEventView.mContentLayout.getChildAt(i);
                   if(view!=null){//added by xujia for view=null
                   	 UnReadMessage m = view.getData();
		            if(m.mType.equals("call")){
                   Cursor cursor = mNewEventView.getContext().getContentResolver()
                           .query(MISS_CALL_URI, MISS_CALL_ISREAD,
                                  "Calls._ID="+m.mId, null, null);
                   if(cursor!=null){
                       try{
			                for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				           boolean isRead=(cursor.getInt(0)==1);
				           System.out.println("IncallisRead="+isRead+" numbername="+m.mNameOrNumber);
    				           if(isRead){
    				              list.add(view);
    				           }
				            }
                         }finally{
                           cursor.close();
                        }
                   }
	        }
                  }
                   
               }
		return list;
            }

       @Override
           public void onPostExecute(final List<UnReadMessageView> result) {
               mRefreshHandler.post(new Runnable() {
                   public void run() {
                       //upateNewEventMessage(result);
                      removeAllMessage(result);
                   }
               });
           }
       }.execute(null, null, null);
   }




    
    public void refreshUnReadMessage() {
        new AsyncTask<Void, Void, List<UnReadMessage>>() {
            @Override
            public List<UnReadMessage> doInBackground(Void... params) {
                Cursor cursor = mNewEventView.getContext().getContentResolver()
                        .query(MISS_CALL_URI, MISS_CALL_PROJECTION,
                                MISS_CALL_SELECTION + mCreateTime + " )", MISS_CALL_SELECTION_ARGS, null);
                List<UnReadMessage> list = new ArrayList<UnReadMessage>();
                if (cursor != null) {
                    try {
                    	for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
                    		//System.out.println("........");
                    		UnReadMessage msg = new UnReadMessage();
                    		msg.mId = cursor.getLong(INDEX_ID);
							long date = cursor.getLong(INDEX_DATE);
                    		msg.mTime = formatTime(date);
							msg.mAccurateTime = date;
							final String name = cursor.getString(INDEX_NAME);
							final boolean hasName = name != null && !"".equals(name);
                    		msg.mNameOrNumber = hasName ? name : cursor.getString(INDEX_NUMBER);
							msg.mNumber = cursor.getString(INDEX_NUMBER);
                    		msg.mType = "call";
							msg.mSnippet = mNewEventView.getContext().getResources().getString(R.string.missed_calls);
                    		msg.mIconId = R.drawable.zzzzz_ic_launcher_phone;
                    		//System.out.println("........msg.mId="+msg.mId+" msg.mTime="+msg.mTime);
                    		list.add(msg);
                    	}
                    } finally {
                        cursor.close();
                    }
                }
                Log.d(TAG, "MissCallUnReadObserver refreshUnReadNumber");
                return list;
            }

            @Override
            public void onPostExecute(final List<UnReadMessage> result) {
				mRefreshHandler.post(new Runnable() {
					public void run() {
						upateNewEventMessage(result);
					}
				});
            }
        }.execute(null, null, null);
    }
    private String formatTime(long time) {
    	return DateFormat.format("kk:mm:ss", time).toString();
    }
}
