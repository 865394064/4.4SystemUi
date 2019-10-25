package com.android.systemui.unreadevents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.systemui.R;


import android.database.Cursor;
import android.os.Handler;
import android.database.sqlite.SqliteWrapper;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.content.ContentUris;
import android.provider.ContactsContract.Contacts;
import android.net.Uri;
import android.view.MotionEvent;
import android.util.Log;
import android.provider.Telephony.Threads;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.app.ActivityManagerNative;
import android.os.RemoteException;
import android.widget.Toast;

import com.mediatek.common.featureoption.FeatureOption;
public class UnReadMessageView extends RelativeLayout implements View.OnTouchListener, View.OnClickListener {
	private static final String TAG = "UnReadMessageView";
    
	private ImageView mIcon;
	private TextView mNameOrNumber;
	private TextView mTime;
	private TextView mSnippet;
	private TextView mHint;
    public  View divderLine;
	
	private UnReadMessage mData;

    private static final ComponentName CALL_INTENT_DESTINATION = new ComponentName(
            "com.android.phone", "com.android.phone.PrivilegedOutgoingCallBroadcaster");
    
    public UnReadMessageView(Context context) {
        this(context, null);
    }
    
    public UnReadMessageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public UnReadMessageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		mIcon = (ImageView) findViewById(R.id.unread_message_icon);
		mNameOrNumber = (TextView) findViewById(R.id.unread_message_name_or_number);
		mTime = (TextView) findViewById(R.id.unread_message_time);
		mSnippet = (TextView) findViewById(R.id.unread_message_snippet);
		mHint = (TextView) findViewById(R.id.unread_message_hint);
        	divderLine=(View) findViewById(R.id.zzzzzzzzzzz_view_divider);
		setOnClickListener(this);
		//setOnTouchListener(this);
	}

	public void updateView(UnReadMessage msg) {
		if(msg == null) {
			setVisibility(View.GONE);
		}
		
		mData = msg;
		
    	mIcon.setImageResource(msg.mIconId);
		if(isSmsMessage(msg) || isMmsMessage(msg)) {
    		mNameOrNumber.setText(msg.mNameOrNumber);
			updateMmsNameView(mNameOrNumber, getContext(), msg.mNameOrNumber);
		} else {
			mNameOrNumber.setText(msg.mNameOrNumber);
		}
    	mTime.setText(msg.mTime);
    	mSnippet.setText(msg.mSnippet);
    	mHint.setText(msg.mHint);
    }

	//this only used by sms or mms, when not in preview mode(set by settings)
	public void updateSnippet(UnReadMessage msg) {
		if(msg == null || msg.mSnippet == null) {
			return;
		}
		
		mData.mSnippet = msg.mSnippet;
		
    	mSnippet.setText(msg.mSnippet);
    }
	
    
    public UnReadMessage getData() {
    	return mData;
    }

	private boolean isSmsMessage(UnReadMessage msg) {
		return msg.mType.equals(UnReadMessageLayout.TYPE_SMS);
	}

	private boolean isMmsMessage(UnReadMessage msg) {
		return msg.mType.equals(UnReadMessageLayout.TYPE_MMS);
	}


	private Handler mHandler = new Handler();
	private void updateMmsNameView(final TextView textView, final Context context, final String id) {
		mHandler.post(new Runnable() {
			public void run() {
				String number = getSingleAddressFromCanonicalAddressInDb(context, id);
				if(TextUtils.isEmpty(number)) {
					return;
				}
				String name = getNameForPhoneNumber(number);
				textView.setText(name);
			}
		});
	}



	public void onClick(View v) {
		Intent intent = null;
		if(isSmsMessage(getData())) {//open mms/sms
			intent = createMmsIntent(getData().mId);
		} else {//open call log
			intent = createCallIntent();
		}
		startActivityDismissingKeyguard(intent, true);
		v.setVisibility(View.GONE);
	}

    private Intent createMmsIntent(long threadId) {
        Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity"));
        if (threadId > 0) {
            intent.setData(ContentUris.withAppendedId(Threads.CONTENT_URI, threadId));
        }

        return intent;
    }
	//
    private Intent createCallIntent() {
		Uri uri = Uri.fromParts("tel", getData().mNumber, null);
		Intent intent = getCallIntent(uri, 0);


        return intent;
    }


    private Intent getCallIntent(Uri uri, int type) {
        final Intent intent = new Intent(Intent.ACTION_CALL_PRIVILEGED, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Set phone as an explicit component of CALL_PRIVILEGED intent.
        // Setting destination explicitly prevents other apps from capturing this Intent since,
        // unlike SendBroadcast, there is no API for specifying a permission on startActivity.
        intent.setComponent(CALL_INTENT_DESTINATION);
        
        return intent;
    }

	
    public void startActivityDismissingKeyguard(Intent intent, boolean onlyProvisioned) {
        //if (onlyProvisioned && !isDeviceProvisioned()) return;

        final Intent closeExpand=new Intent("close_status_bar_expand");//added by xujia
        try {
            // Dismiss the lock screen when Settings starts.
            ActivityManagerNative.getDefault().dismissKeyguardOnNextActivity();
        } catch (RemoteException e) {
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	try {
    		getContext().startActivity(intent);

             getContext().sendBroadcast(closeExpand);//added by xujia 
    	} catch (ActivityNotFoundException e) {
    		Toast.makeText(getContext(), "not found target...", Toast.LENGTH_LONG).show();
    		e.printStackTrace();
    	}
    }









    /**
     * getSingleNumberFromCanonicalAddresses looks up the recipientId in the canonical_addresses
     * table and returns the associated number or email address.
     * @param context needed for the ContentResolver
     * @param recipientId of the contact to look up
     * @return phone number or email address of the recipientId
     */
    private String getSingleAddressFromCanonicalAddressInDb(final Context context,
            final String recipientId) {
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(),
                ContentUris.withAppendedId(sSingleCanonicalAddressUri, Long.parseLong(recipientId)),
                null, null, null, null);
        if (c == null) {
            Log.d(TAG, "null Cursor looking up recipient: " + recipientId);
            return null;
        }
        try {
            if (c.moveToFirst()) {
                String number = c.getString(0);
                return number;
            }
        } finally {
            c.close();
        }
        return null;
    }



    private static Uri sSingleCanonicalAddressUri =
            Uri.parse("content://mms-sms/canonical-address");
	
    private static final String[] CALLER_ID_PROJECTION = new String[] {
            Phone._ID,                      // 0
            Phone.NUMBER,                   // 1
            Phone.LABEL,                    // 2
            Phone.DISPLAY_NAME,             // 3
            Phone.CONTACT_ID,               // 4
            Phone.CONTACT_PRESENCE,         // 5
            Phone.CONTACT_STATUS,           // 6
            Phone.NORMALIZED_NUMBER,        // 7
            Contacts.SEND_TO_VOICEMAIL      // 8
    };

    private String getNameForPhoneNumber(String number) {
        boolean isValidNumber = isWellFormedSmsAddress(number);
		String name = null;

        if (isValidNumber) {
            number = PhoneNumberUtils.stripSeparators(number);
        }

        String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
        String matchNumber = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber);
        if (!TextUtils.isEmpty(normalizedNumber) && !TextUtils.isEmpty(matchNumber)) {
            Cursor cursor = getContext().getContentResolver().query(
                    Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, normalizedNumber), CALLER_ID_PROJECTION, null, null, null);
            if (cursor == null) {
                return number;
            }
			
            try {
                if (cursor.moveToFirst()) {
					name = cursor.getString(3);
                }
            } finally {
                cursor.close();
            }
        }
        return TextUtils.isEmpty(name) ? number : name;
    }






    /** M: Code analyze 022, For fix bug ALPS00281094, Can not send and receive Sms.
     * Return true iff the network portion of <code>address</code> is,
     * as far as we can tell on the device, suitable for use as an SMS
     * destination address.
     */
    private boolean isWellFormedSmsAddress(String address) {
        //MTK-START [mtk04070][120104][ALPS00109412]Solve "can't send MMS with MSISDN in international format"
        //Merge from ALPS00089029
        if (!isDialable(address)) {
            return false;
        }
        //MTK-END [mtk04070][120104][ALPS00109412]Solve "can't send MMS with MSISDN in international format"

        String networkPortion =
                PhoneNumberUtils.extractNetworkPortion(address);

        return (!(networkPortion.equals("+")
                  || TextUtils.isEmpty(networkPortion)))
               && isDialable(networkPortion);
    }

    private boolean isDialable(String address) {
        for (int i = 0, count = address.length(); i < count; i++) {
            if (!isDialable(address.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** M: True if c is ISO-LATIN characters 0-9, *, # , +, WILD  */
    private boolean isDialable(char c) {
        return (c >= '0' && c <= '9') || c == '*' || c == '#' || c == '+' || c == 'N' || c == '(' || c == ')';
    }








	private float mLastX;
	private float mTranslationX;
	private boolean mInSecurityModel = false;//is in 'SecurityMode.PIN or SecurityMode.Password' mode;
	private static final int SCREEN_WIDTH = FeatureOption.LCM_WIDTH;
	private boolean mDragging = false;


	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		final int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			requestDisallowInterceptTouchEvent(true);
			mLastX = ev.getRawY();
			Log.d(TAG, "*****************onTouch ACTION_DOWN*******************");
			break;
		}
		case MotionEvent.ACTION_OUTSIDE:
		case MotionEvent.ACTION_MOVE:
			Log.d(TAG, "onTouch ACTION_OUTSIDE ACTION_MOVE");
			int dx = (int) (ev.getRawX() - mLastX);
			mTranslationX += dx;

			Log.d(TAG, "mSlidingChallengeLayout.getX()=");
			//if(getX() >= 0) {
				setX(getX() + dx);
				mLastX = ev.getRawX();
			//}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			requestDisallowInterceptTouchEvent(false);
			Log.d(TAG, "onTouch ACTION_UP ACTION_CANCEL");
/*
			float maxVelocity = MAX_DISMISS_VELOCITY * mDensityScale;
			mVelocityTracker.computeCurrentVelocity(1000 ,// px/sec
					maxVelocity);
			float escapeVelocity = SWIPE_ESCAPE_VELOCITY * mDensityScale;
			float velocity = mVelocityTracker.getXVelocity();
			float perpendicularVelocity = mVelocityTracker.getYVelocity();

			// Fast swipe = > escapeVelocity and translation of .1 *
			// width
			boolean childSwipedFastEnough = (Math.abs(velocity) > escapeVelocity)
					&& (Math.abs(velocity) > Math.abs(perpendicularVelocity));

			
			final float challengeLayoutX = mSlidingChallengeParentLayout.getX();
			final float passwordLayoutX = mPasswordLayout.getX();
			if(mInSecurityModel) {
				if(challengeLayoutX < 0 || (challengeLayoutX > 0 && challengeLayoutX < SCREEN_WIDTH / 2)) {
					resetChallengeLayoutLocation();
				} else if(challengeLayoutX > SCREEN_WIDTH / 2 && challengeLayoutX < SCREEN_WIDTH) {
					resetPasswordLayoutLocation();
				}  else if(passwordLayoutX > 0) {
					resetPasswordLayoutLocation();
				}
			} else {
				if(challengeLayoutX < 0) {//reset to original location
					resetChallengeLayoutLocation();
				} else if(challengeLayoutX > 0 && challengeLayoutX < SCREEN_WIDTH / 2) {//reset to original location
					resetChallengeLayoutLocation();
				} else if(challengeLayoutX > SCREEN_WIDTH / 2) {//unlock screen
					unlockScreen();
				}
			}
*/
			break;
		}
		return true;
	}









	
}





























