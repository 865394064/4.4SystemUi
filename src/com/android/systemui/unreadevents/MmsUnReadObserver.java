package com.android.systemui.unreadevents;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.format.DateFormat;
import com.android.internal.R;

import android.provider.Telephony.Threads;
import android.provider.Telephony.MmsSms;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Sms;

public class MmsUnReadObserver extends UnReadObserver {

    public static final Uri MMS_URI = Threads.CONTENT_URI;
    
    private static final String NEW_INCOMING_SM_CONSTRAINT =
        "(" + "read" + " = 0 AND "+ "date" + " >= ";
    
	private static final int INDEX_THREAD_ID = 0;
	private static final int INDEX_THREAD_DATE = 1;
	private static final int INDEX_THREAD_MESSAGE_COUNT = 2;
	private static final int INDEX_THREAD_RECIPIENT_IDS = 3;
	private static final int INDEX_THREAD_SNIPPET = 4;
	private static final int INDEX_THREAD_READCOUNT = 5;
	
    
    private static final Uri sAllThreadsUri =
        Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build();
    
    public static final String[] ALL_THREADS_PROJECTION = {
        Threads._ID, Threads.DATE, Threads.MESSAGE_COUNT, Threads.RECIPIENT_IDS,
        Threads.SNIPPET, Threads.READCOUNT
    };

    public static final String[] All_THREADS_ISREAD={Threads.READ};

    public MmsUnReadObserver(Handler handler, UnReadMessageLayout newEventView, long createTime) {
        super(handler, newEventView, createTime);
    }
    
    /*
    public void refreshUnReadMessage() {
        new AsyncTask<Void, Void, List<UnReadMessage>>() {
            @Override
            public List<UnReadMessage> doInBackground(Void... params) {
            	
                Cursor cursor = mNewEventView.getContext().getContentResolver()
                	.query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                        NEW_INCOMING_SM_CONSTRAINT + mCreateTime + ")", null, null);
                List<UnReadMessage> list = new ArrayList<UnReadMessage>();
                String[] names = cursor.getColumnNames();
                for(String str:names)
                	System.out.println(str);

                

                if (cursor != null) {
                    try {
                    	for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
                    		UnReadMessage msg = new UnReadMessage();
                    		msg.mId = cursor.getLong(INDEX_ID);
                    		msg.mTime = formatTime(cursor.getLong(INDEX_DATE));
                    		msg.mNameOrNumber = cursor.getString(INDEX_RECIPIENT_IDS);
                    		msg.mSnippet = cursor.getString(INDEX_SNIPPET);
                    		msg.mType = UnReadMessageLayout.TYPE_SMS;
                    		msg.mIconId = R.drawable.zzzzz_ic_launcher_smsmms;
                    		list.add(msg);
                    	}
                    	System.out.println("...sms.....cursor.getCount()="+cursor.getCount());
                    } finally {
                        cursor.close();
                    }
                }
                //Log.d(TAG, "refreshUnReadNumber mmsCount=" + mmsCount + ", smsCount=" + smsCount + ", mCreateTime=" + mCreateTime);
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
*/

	//just show a simple message and unread count
	private List<UnReadMessage> findNewEvents() {
        Cursor cursor = mNewEventView.getContext().getContentResolver().query(sAllThreadsUri, ALL_THREADS_PROJECTION,
            NEW_INCOMING_SM_CONSTRAINT + mCreateTime + ")", null, null);
		
        List<UnReadMessage> list = new ArrayList<UnReadMessage>();

        if (cursor != null) {
            try {
               	for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
					final int unreadCount = (int)(cursor.getLong(INDEX_THREAD_MESSAGE_COUNT) - cursor.getLong(INDEX_THREAD_READCOUNT));
               		UnReadMessage msg = new UnReadMessage();
               		msg.mId = cursor.getLong(INDEX_THREAD_ID);
               		msg.mTime = formatTime(cursor.getLong(INDEX_THREAD_DATE));
               		msg.mNameOrNumber = cursor.getString(INDEX_THREAD_RECIPIENT_IDS);
					if(unreadCount > 1) {
               			msg.mSnippet = mNewEventView.getContext().getResources().getString(R.string.iphone_sms_preview_show_content, unreadCount);
					} else {
						msg.mSnippet = mNewEventView.getContext().getResources().getString(R.string.iphone_sms_preview_show_content_one);
					}
               		msg.mType = UnReadMessageLayout.TYPE_SMS;
               		msg.mIconId = R.drawable.zzzzz_ic_launcher_smsmms;
               		list.add(msg);
               	}
               	//System.out.println("...sms.....cursor.getCount()="+cursor.getCount());
            } finally {
                cursor.close();
            }
        }
		return list;
    }




	public void refreshAllMessage(){
        
        new AsyncTask<Void, Void, List<UnReadMessageView>>() {
               @Override
               public List<UnReadMessageView> doInBackground(Void... params) {
    		   List<UnReadMessageView> list = new ArrayList<UnReadMessageView>();
                   final int size = mNewEventView.mContentLayout.getChildCount();
                   for(int i=0;i<size;i++) {
                   	 UnReadMessageView view = (UnReadMessageView) mNewEventView.mContentLayout.getChildAt(i);
                   	 UnReadMessage m = view.getData();
		if(!m.mType.equals("call")){
                   Cursor cursor = mNewEventView.getContext().getContentResolver()
                           .query(sAllThreadsUri,All_THREADS_ISREAD,
                                  "Threads._ID="+m.mId, null, null);
                   if(cursor!=null){
                       try{
			for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
				           boolean isRead=(cursor.getInt(0)==1);
				           System.out.println("mmsisRead="+isRead);
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








	//first, we get new sms or mms and save it by tempList
	//then, we get new sms or mms's recipient ids idList
	//finally, we compare these two list by thread id, if
	//thread id are same, then copy msg(from idList)'s mNameOrNumber to corespond msg(from tempList)
    public void refreshUnReadMessage() {
        new AsyncTask<Void, Void, List<UnReadMessage>>() {
            @Override
            public List<UnReadMessage> doInBackground(Void... params) {
            	List<UnReadMessage> result = new ArrayList<UnReadMessage>();

				if(!mShowMessageContent) {
					return findNewEvents();
				}

				
				findNewSms(result);
				findNewMms(result);

				//just query the 'recipient ids', as we use recipient ids to
				//find recipient number or name
                Cursor cursor = mNewEventView.getContext().getContentResolver()
                	.query(sAllThreadsUri, ALL_THREADS_PROJECTION,
                        NEW_INCOMING_SM_CONSTRAINT + mCreateTime + ")", null, null);
                List<UnReadMessage> list = new ArrayList<UnReadMessage>();

                if (cursor != null) {
                    try {
                    	for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
                    		UnReadMessage msg = new UnReadMessage();
                    		msg.mId = cursor.getLong(INDEX_THREAD_ID);
                    		//msg.mTime = formatTime(cursor.getLong(INDEX_DATE));
                    		msg.mNameOrNumber = cursor.getString(INDEX_THREAD_RECIPIENT_IDS);
                    		//msg.mSnippet = cursor.getString(INDEX_SNIPPET);
                    		//msg.mType = UnReadMessageLayout.TYPE_SMS;
                    		//msg.mIconId = R.drawable.zzzzz_ic_launcher_smsmms;
                    		list.add(msg);
                    	}
                    	//System.out.println("...sms.....cursor.getCount()="+cursor.getCount());
                    } finally {
                        cursor.close();
                    }
                }
                //Log.d(TAG, "refreshUnReadNumber mmsCount=" + mmsCount + ", smsCount=" + smsCount + ", mCreateTime=" + mCreateTime);
                List<UnReadMessage> returnList = new ArrayList<UnReadMessage>();
                for(int i=0;i<result.size();i++) {
					final UnReadMessage uMsg = result.get(i);
					for(int j=0;j<list.size();j++) {
						final UnReadMessage msg = list.get(j);
						if(uMsg.mId == msg.mId) {
							uMsg.mNameOrNumber = msg.mNameOrNumber;
							break;
						}
					}
					returnList.add(uMsg);
                }
                return returnList;
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





	private static final int INDEX_SMS_ID = 0;
	private static final int INDEX_SMS_THREAD_ID = 1;
	private static final int INDEX_SMS_DATE = 2;
	private static final int INDEX_SMS_BODY = 3;

	private static final int INDEX_MMS_ID = 0;
	private static final int INDEX_MMS_THREAD_ID = 1;
	private static final int INDEX_MMS_DATE = 2;

    private static final String[] NEW_SMS_PROJECTION = {
        Sms._ID, Sms.THREAD_ID, Sms.DATE, Sms.BODY
    };
    private static final String[] NEW_MMS_PROJECTION = {
        Mms._ID, Mms.THREAD_ID, Mms.DATE
    };
	//to find new sms and save it by list
	private void findNewSms(List<UnReadMessage> list) {
		Cursor cursor = null;
		try {
			cursor = mNewEventView.getContext().getContentResolver().query(Uri.parse("content://sms"),
					NEW_SMS_PROJECTION, "type = 1 and read = 0 " + " and date" + " >= " + mCreateTime, null, null);
			if(cursor != null) {
				for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
					UnReadMessage msg = new UnReadMessage();
					long msgId = cursor.getLong(INDEX_SMS_ID);
					long threadId = cursor.getLong(INDEX_SMS_THREAD_ID);
					long date = cursor.getLong(INDEX_SMS_DATE);
					String time = formatTime(date);
					msg.mId = threadId;
					msg.mTime = time;
					msg.mAccurateTime = date;
					msg.mSnippet = cursor.getString(INDEX_SMS_BODY);
					msg.mType = UnReadMessageLayout.TYPE_SMS;
					msg.mIconId = R.drawable.zzzzz_ic_launcher_smsmms;
					list.add(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
	}

	//to find new mms and save it by list
	private void findNewMms(List<UnReadMessage> list) {
		Cursor cursor = null;
		try {
			cursor = mNewEventView.getContext().getContentResolver().query(
					Uri.parse("content://mms/inbox"), NEW_MMS_PROJECTION, "read = 0" + " and date" + " >= " + mCreateTime, null,
					null);
			if(cursor != null) {
				for(cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()) {
					UnReadMessage msg = new UnReadMessage();
					long msgId = cursor.getLong(INDEX_MMS_ID);
					long threadId = cursor.getLong(INDEX_MMS_THREAD_ID);
					long date = cursor.getLong(INDEX_MMS_DATE);
					String time = formatTime(date);
					msg.mId = threadId;
					msg.mTime = time;
					msg.mAccurateTime = date;
					msg.mType = UnReadMessageLayout.TYPE_MMS;
					msg.mIconId = R.drawable.zzzzz_ic_launcher_smsmms;
					list.add(msg);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			cursor.close();
		}
	}

    private String formatTime(long time) {
    	return DateFormat.format("kk:mm:ss", time).toString();
    }
}
