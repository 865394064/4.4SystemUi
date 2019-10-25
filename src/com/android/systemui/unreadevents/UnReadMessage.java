package com.android.systemui.unreadevents;


public class UnReadMessage {
	public long mId;
	public String mType;
	public int mIconId;
	//if sms message, this param save person id
	public String mNameOrNumber;
	public String mNumber;
	public String mTime;
	public String mSnippet;
	public String mHint;

	public long mAccurateTime;//only for compare purpose

	public UnReadMessage() {

	}
	
	public UnReadMessage(int iconId, String nameOrNumber, String time, String snippet, String hint) {
		mIconId = iconId;
		mNameOrNumber = nameOrNumber;
		mTime = time;
		mSnippet = snippet;
		mHint = hint;
	}
	
	public boolean equals(UnReadMessage msg) {
		if(msg == null || msg.mType == null)
			return false;
		if(msg.mId == this.mId && msg.mType.equals(this.mType) && msg.mAccurateTime ==this.mAccurateTime)
			return true;
		
		return false;
	}
}
