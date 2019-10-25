/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.telephony.TelephonyManager;
import com.android.systemui.R;
import com.mediatek.systemui.ext.IconIdWrapper;
import com.mediatek.systemui.ext.PluginFactory;
import com.android.systemui.statusbar.policy.NetworkController;
import android.widget.FrameLayout;
import com.mediatek.xlog.Xlog;

import android.widget.TextView;//added by wang 20130725
import com.android.internal.telephony.IccCardConstants;
import android.widget.ProgressBar;
import android.content.Intent;

// Intimately tied to the design of res/layout/signal_cluster_view.xml
public class SignalCl
        extends LinearLayout
        implements NetworkController.SignalCluster {

    static final boolean DEBUG = false;
    static final String TAG = "SignalClusterView";

    NetworkController mNC;
    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0;
    private int mWifiActivityId = 0;
    private boolean mMobileVisible = true;
    private IconIdWrapper mMobileStrengthId = new IconIdWrapper();
    private IconIdWrapper mMobileActivityId = new IconIdWrapper(0);
    private IconIdWrapper[][] mdoubleMobileStrengthId;
    private int mMobileTypeId = 0;
    private boolean mIsAirplaneMode = false;
    private int mAirplaneIconId = 0;

    private String mWifiDescription, mMobileDescription, mMobileTypeDescription;
    int sim2=0;
    /// M: Support SIM Indicator. @{
    final TelephonyManager tm;
    public String im1,im2,im3;
    private boolean mShowSimIndicator = false;
    private int mSimIndicatorResource = 0;
    private ViewGroup mSignalClusterCombo;
    String operName =null;
    /// M: Support SIM Indicator. }@
    
    /// M: Support Roam Data Icon both show. @{
    
    private boolean mRoaming = false;
    private int mRoamingId = 0;
    private ImageView mMobileRoam;
    private int mPostype = 0;
    public static final int SIGNAL_POSTYPE_TOP = 1;

    /// M: Support Roam Data Icon both show. }@

    ViewGroup mWifiGroup,mWifiGroup1,mMobileGroup;
    ImageView mWifi,mWifi1, mMobile,mobile_signal_2, mWifiActivity, mWifiActivity1,mMobileType, mAirplane;//mMobileActivity
    FrameLayout no_sims_combo;
    ProgressBar mMobileActivity;
    View mSpacer;
    View mWifiSpacer;
	//added by wang 20130725 start
	TextView mMobileNameView,mMobileNameView2;
    TextView no_sims_text1;
	String mMobileNameStr;
    LinearLayout signal_cluster_comboLinar;
	//added by wang 20130725 end

    public SignalCl(Context context) {
        this(context, null);
        Log.d("hjz3","3");
        //im1=tm.getSubscriberIdGemini(0);
    }

    public SignalCl(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        Log.d("hjz3","2");
        //im1=tm.getSubscriberIdGemini(0);
    }

    public SignalCl(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SignalClusterViewAttr);
        //int postype = a.getInt(R.styleable.SignalClusterViewAttr_postype, 0);
        //Log.d("kay18", "SignalClusterView: postype:" + postype);
        //a.recycle();

        tm=(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        //im1=tm.getSubscriberIdGemini(0);
    }

    public void setNetworkController(NetworkController nc) {
        if (DEBUG) Slog.d(TAG, "NetworkController=" + nc);

        mNC = nc;
    }

    public void setPosType(int nPostype){
        mPostype = nPostype;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //add by hjz
        no_sims_text1   = (TextView) findViewById(R.id.no_sims_text1);
        no_sims_combo   = (FrameLayout) findViewById(R.id.no_sims_combo);
        mWifiGroup      = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi           = (ImageView) findViewById(R.id.wifi_signal);
        mWifiActivity   = (ImageView) findViewById(R.id.wifi_inout);
        mWifiGroup1      = (ViewGroup) findViewById(R.id.wifi_combo1);
        mWifi1           = (ImageView) findViewById(R.id.wifi_signal1);
        mWifiActivity1   = (ImageView) findViewById(R.id.wifi_inout1);
        mMobileGroup    = (ViewGroup) findViewById(R.id.mobile_combo);
        mMobile         = (ImageView) findViewById(R.id.mobile_signal);
        mobile_signal_2 = (ImageView) findViewById(R.id.mobile_signal_x);
        //mMobileActivity = (ImageView) findViewById(R.id.mobile_inout);
        mMobileActivity=(ProgressBar)findViewById(R.id.mobile_inout);
        mMobileType     = (ImageView) findViewById(R.id.mobile_type);
        mSpacer         =             findViewById(R.id.spacer);
        mWifiSpacer     =             findViewById(R.id.wifispacer);
        mAirplane       = (ImageView) findViewById(R.id.airplane);
		mMobileNameView       = (TextView) findViewById(R.id.mobile_name);//added by wang 20130725
        mMobileNameView2= (TextView) findViewById(R.id.mobile_name_2);
        signal_cluster_comboLinar=(LinearLayout) findViewById(R.id.signal_cluster_comboLinar);
        /// M: Support SIM Indicator. 
        mSignalClusterCombo           = (ViewGroup) findViewById(R.id.signal_cluster_combo);

        /// M: Support Roam Data Icon both show.
        mMobileRoam     =  (ImageView) findViewById(R.id.mobile_roaming);
        
        apply();
    }

    @Override
    protected void onDetachedFromWindow() {
        mWifiGroup      = null;
        mWifiGroup1      = null;
        mWifi           = null;
        mWifi1           = null;
        mWifiActivity   = null;
        mWifiActivity1   = null;
        mMobileGroup    = null;
        mMobile         = null;
        mMobileActivity = null;
        mMobileType     = null;
        mSpacer         = null;
        mWifiSpacer     = null;
        mAirplane       = null;
		mMobileNameView     = null;//added by wang 20130725
        
        /// M: Support Roam Data Icon both show.
        mMobileRoam     = null;

        super.onDetachedFromWindow();
    }

    @Override
    public void setWifiIndicators(boolean visible, int strengthIcon, int activityIcon,
            String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiActivityId = activityIcon;
        mWifiDescription = contentDescription;

        apply();
    }

    public void setMobileDataIndicators(boolean visible, IconIdWrapper strengthIcon, IconIdWrapper activityIcon,
            int typeIcon, String contentDescription, String typeContentDescription, String displayName) {
        mMobileVisible = visible;
        mMobileStrengthId = strengthIcon.clone();
        mMobileActivityId = activityIcon.clone();
        mMobileTypeId = typeIcon;
        mMobileDescription = contentDescription;
        mMobileTypeDescription = typeContentDescription;
       //modified by xujia 20130911
       if(displayName!=null)
		mMobileNameStr = displayName.replaceAll("\\d+","").trim();//added by wang 20130726
        Log.d("hjz6","zzzz="+mMobileNameStr);
        apply();
    }

    @Override
    public void setIsAirplaneMode(boolean is, int airplaneIconId) {
        mIsAirplaneMode = is;
        mAirplaneIconId = airplaneIconId;

        apply();
    }

    /// M: Support SIM Indicator. @{
    
    public void setShowSimIndicator(boolean showSimIndicator, int simIndicatorResId) {
        mShowSimIndicator = showSimIndicator;
        mSimIndicatorResource = simIndicatorResId;
        
        apply();
    }
    
    /// M: Support SIM Indicator. }@
    
    /// M: Support Roam Data Icon both show.
    public void setRoamingFlagandResource(boolean roaming, int roamingId) {
        mRoaming = roaming;
        mRoamingId = roamingId;
    }
    public void setdoubsim(int sim2) {
        sim2 = sim2;
    }
    
    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // Standard group layout onPopulateAccessibilityEvent() implementations
        // ignore content description, so populate manually
        if (mWifiVisible && mWifiGroup.getContentDescription() != null)
            event.getText().add(mWifiGroup.getContentDescription());
        if (mMobileVisible && mMobileGroup.getContentDescription() != null)
            event.getText().add(mMobileGroup.getContentDescription());
        return super.dispatchPopulateAccessibilityEvent(event);
    }

//added by wang 20130731 start
	public void updateSignalColor(boolean white) {
		if(mMobileNameView!=null)
		    mMobileNameView.setTextColor(white ? 0xFFFFFFFF : 0xFF000000);

        if(mMobileNameView2!=null)
            mMobileNameView2.setTextColor(white ? 0xFFFFFFFF : 0xFF000000);
		/*Begin:added by xss for back to last app*/
        Intent intent	=new Intent("back_button_text_color");
		 intent.putExtra("isWhite",white);
		 getContext().sendBroadcast(intent);
	}
    private void apply() {
        im1=tm.getSubscriberIdGemini(0);
        im2=tm.getSubscriberIdGemini(1);
        Log.d("hjz3","apply=="+im1+"im2==="+im2);

        if (mWifiGroup == null) return;
        mWifiGroup1.setVisibility(View.GONE);
        if (mWifiVisible) {
            mWifiGroup.setVisibility(View.VISIBLE);
            mWifi.setImageResource(mWifiStrengthId);
            mWifi1.setImageResource(mWifiStrengthId);
            mWifiActivity.setImageResource(mWifiActivityId);
            mWifiActivity1.setImageResource(mWifiActivityId);
            mWifiGroup.setContentDescription(mWifiDescription);
            mWifiGroup1.setContentDescription(mWifiDescription);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }
        if (mWifiVisible && PluginFactory.getStatusBarPlugin(mContext).supportDataTypeAlwaysDisplayWhileOn()) {
            mWifiSpacer.setVisibility(View.INVISIBLE);
        } else {
            mWifiSpacer.setVisibility(View.GONE);
        }
        if (DEBUG) Slog.d(TAG,
                String.format("wifi: %s sig=%d act=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId, mWifiActivityId));
        if(im1!=null || im2!=null){
            //no_sims_text1.setVisibility(View.GONE);
          //  Log.d("hjz3","mMobileStrengthId.getIconId()==="+mMobileStrengthId.getIconId());
            String imj=lookupOperatorName(tm.getNetworkOperator());
            Log.d("hjz","im3="+im3);
            mMobileNameView.setText(imj);
            mMobile.setImageResource(mMobileStrengthId.getIconId());
            no_sims_combo.setVisibility(View.GONE);
        }
        if(im1!=null && im2!=null){
            //no_sims_text1.setVisibility(View.GONE);
           // Log.d("hjz3","mMobileStrengthId.getIconId()==="+mMobileStrengthId.getIconId());
            mMobile.setImageResource(mMobileStrengthId.getIconId());
            no_sims_combo.setVisibility(View.GONE);
            no_sims_text1.setVisibility(View.VISIBLE);
            mobile_signal_2.setImageResource(mMobileStrengthId.getIconId());
            mMobileNameView2.setVisibility(View.VISIBLE);

            if (im2 != null) {
                if ((im2.startsWith("46000")) || (im2.startsWith("46002")) || (im2.startsWith("46007")) || (im2.startsWith("46004"))) {
                    operName = mContext.getText(R.string.oper_long_46000).toString();
                } else if ((im2.startsWith("46001")) || (im2.startsWith("46006")) || (im2.startsWith("46009"))) {
                    operName = mContext.getText(R.string.oper_long_46001).toString();
                } else if ((im2.startsWith("46003"))||(im2.startsWith("46005"))||(im2.startsWith("46011"))) {
                    operName = mContext.getText(R.string.oper_long_46003).toString();
                } else if (im2.startsWith("46601")) {
                    operName = mContext.getText(R.string.oper_long_46601).toString();
                } else if (im2.startsWith("46692")) {
                    operName = mContext.getText(R.string.oper_long_46692).toString();
                } else if (im2.startsWith("46697")) {
                    operName = mContext.getText(R.string.oper_long_46697).toString();
                } else if (im2.startsWith("99998")) {
                    operName = mContext.getText(R.string.oper_long_99998).toString();
                } else if (im2.startsWith("99999")) {
                    operName = mContext.getText(R.string.oper_long_99999).toString();
                } else {
                }
            }
            Log.d("hjz6","operName="+operName +"im2=="+im2);
            mMobileNameView2.setText(""+operName);
        }else{
            no_sims_text1.setVisibility(View.GONE);
            mobile_signal_2.setImageResource(0);
            mMobileNameView2.setVisibility(View.GONE);
        }
        if ((im1==null && im2==null)){
            //mMobile.setImageDrawable(null);
            no_sims_text1.setVisibility(View.VISIBLE);
            no_sims_combo.setVisibility(View.VISIBLE);
        }
        if (!mIsAirplaneMode) {//delete by hjz
        //if (!mIsAirplaneMode) {
            /// M: Support Roam Data Icon both show. @{
            if (mRoaming) {
                mMobileRoam.setImageResource(mRoamingId);
                mMobileRoam.setVisibility(View.VISIBLE);
            } else {
                mMobileRoam.setImageResource(0);
                mMobileRoam.setVisibility(View.GONE);
            }
            /// M: Support Roam Data Icon both show. }@
              //Log.d("hjz3","apply=="+mMobileStrengthId.getResources()+"mMobileStrengthId.getIconId()= "+mMobileStrengthId.getIconId());
              mMobileGroup.setVisibility(View.VISIBLE);
                String im3=lookupOperatorName(tm.getNetworkOperator());
                Log.d("hjz","im3="+im3);
			    mMobileNameView.setText(im3);
            //mMobileNameView2.setText(mMobileNameStr);
            if (mMobileStrengthId.getResources() != null) {
                Log.d("hjz3","mMobile==1");
                mMobile.setImageDrawable(mMobileStrengthId.getDrawable());
            } else {
                if (mMobileStrengthId.getIconId() == 0) {
                    Log.d("hjz3","mMobile==2");
                    mMobile.setImageDrawable(null);
                } else {
                    Log.d("hjz3","mMobile==3");
                    mMobile.setImageResource(mMobileStrengthId.getIconId());
                }
            }
            if(!mNC.getWifiState())
                {
                    if (mMobileActivityId.getResources() != null) {
                        mMobileActivity.setVisibility(View.VISIBLE);
                    } else {
                        if (mMobileActivityId.getIconId() == 0) {
                            mMobileActivity.setVisibility(View.GONE);
                        } else {
                              mMobileActivity.setVisibility(View.VISIBLE);
                        }
                    }
                }else
                    {
                          mMobileActivity.setVisibility(View.GONE);
                    }
       
            mMobileType.setImageResource(mMobileTypeId);
               if(!mNC.isMobileConnected())
                {     
                       mMobileType.setVisibility(View.GONE);
                }
            Xlog.d(TAG, "apply() setImageResource(mMobileTypeId) mShowSimIndicator = " + mShowSimIndicator);
            mMobileGroup.setContentDescription(mMobileTypeDescription + " " + mMobileDescription);
            
            /// M: Airplane mode: Shouldn't show data type icon (L1)
            ///    OP01 project: Not in airplane mode, the data type icon should be always displayed (L2)
            ///    WifiVisible: Not in airplane mode, not OP01 project, the data type icon should follow the wifi visible status (L3)
            if (PluginFactory.getStatusBarPlugin(mContext).supportDataTypeAlwaysDisplayWhileOn()) {
                mMobileType.setVisibility(View.VISIBLE);
            } else {
                mMobileType.setVisibility(!mWifiVisible ? View.VISIBLE : View.GONE);
            }
            
            /// M: When the signal strength icon id is null should hide the data type icon, this including several status
            if (mMobileStrengthId.getIconId() == R.drawable.stat_sys_signal_null) {
                mMobileType.setVisibility(View.GONE);
            }
            
            /// M: Support SIM Indicator. @{
            if (mShowSimIndicator) {
                Log.d("hjz","mSimIndicatorResource="+mSimIndicatorResource);
                mSignalClusterCombo.setBackgroundResource(mSimIndicatorResource);
            } else {
                mSignalClusterCombo.setPadding(0, 0, 0, 0);
                mSignalClusterCombo.setBackgroundDrawable(null);
            }
            /// M: Support SIM Indicator. }@
        } else {
            if((im1==null && im2==null)){
            mMobileGroup.setVisibility(View.GONE);
            }else{
                mMobileGroup.setVisibility(View.VISIBLE);
            }
            String im4=lookupOperatorName(tm.getNetworkOperator());
            Log.d("hjz","im3="+im4);
            mMobileNameView.setText(im4);;
            mMobileNameView.setVisibility(View.VISIBLE);
            if(mNC.mSimState==IccCardConstants.State.ABSENT)
            {
                 mMobileType.setVisibility(View.GONE);
                if(mMobileNameStr!=null&& !("".equals(mMobileNameStr))){
                    mMobileNameView.setText(mMobileNameStr);
                }else{
                    Log.d("hjz6","无ka");
                    mMobileNameView.setText(R.string.no_sim);
                }
            }else {
                if(!mNC.hasService()) {
                    mMobileType.setVisibility(View.GONE);
                    Log.d("hjz6", "无服务");
                    if (mMobileNameStr != null && !("".equals(mMobileNameStr))) {
                        mMobileNameView.setText(mMobileNameStr);
                    } else {
                        Log.d("hjz6", "无服务1");
                        mMobileNameView.setText(R.string.no_service);
                    }
                }
            }
        }
        if(mNC.mSimState==IccCardConstants.State.ABSENT)
        {
            Log.d("hjz6","无ka");
             mMobileGroup.setVisibility(View.GONE);
             mMobileType.setVisibility(View.GONE);
             mMobileNameView.setText(R.string.no_sim);
        }

        if (mIsAirplaneMode) {
            mAirplane.setVisibility(View.VISIBLE);
            mMobileNameView.setVisibility(View.GONE);
            mMobileType.setVisibility(View.GONE);
            mAirplane.setImageResource(mAirplaneIconId);
            mMobileActivity.setVisibility(View.GONE);
            signal_cluster_comboLinar.setVisibility(View.GONE);
        } else {
            signal_cluster_comboLinar.setVisibility(View.VISIBLE);
            mMobileNameView.setVisibility(View.VISIBLE);
            mAirplane.setVisibility(View.GONE);
            //hjz6
        }

        if (mMobileVisible && mWifiVisible && mIsAirplaneMode) {
            mSpacer.setVisibility(View.INVISIBLE);
        } else {
            mSpacer.setVisibility(View.GONE);
        }

        //Log.d("kay19", "apply: mPostype:" + mPostype );
        try{
            if(mPostype == SIGNAL_POSTYPE_TOP){
                //if(mNC.mSimState==IccCardConstants.State.ABSENT)
                {
                    mMobileNameView.setText(R.string.mobile_name_empty);
                }
               // mWifiGroup.setVisibility(View.GONE);
               // mWifiGroup1.setVisibility(View.VISIBLE);
            }else{
               // mWifiGroup1.setVisibility(View.GONE);
               // mWifiGroup.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){

        }

        if (DEBUG) Slog.d(TAG,
                String.format("mobile: %s sig=%d act=%d typ=%d",
                    (mMobileVisible ? "VISIBLE" : "GONE"),
                    mMobileStrengthId.getIconId(), mMobileActivityId.getIconId(), mMobileTypeId));

    }
    public String lookupOperatorName(String numeric) {
        Context context = mContext;
        String operName = numeric;
        Log.d("hjz6","operName==========="+operName);
        if (numeric != null) { // MVNO-API
            if ((numeric.equals("46000")) || (numeric.equals("46002")) || (numeric.equals("46007")) || (numeric.equals("46004"))) {
                operName = context.getText(R.string.oper_long_46000).toString();
            } else if ((numeric.equals("46001")) || (numeric.equals("46006")) || (numeric.equals("46009"))) {
                operName = context.getText(R.string.oper_long_46001).toString();
            } else if ((numeric.equals("46003"))||(numeric.equals("46005"))||(numeric.equals("46011"))) {
                operName = context.getText(R.string.oper_long_46003).toString();
            } else if (numeric.equals("46601")) {
                operName = context.getText(R.string.oper_long_46601).toString();
            } else if (numeric.equals("46692")) {
                operName = context.getText(R.string.oper_long_46692).toString();
            } else if (numeric.equals("46697")) {
                operName = context.getText(R.string.oper_long_46697).toString();
            } else if (numeric.equals("99998")) {
                operName = context.getText(R.string.oper_long_99998).toString();
            } else if (numeric.equals("99999")) {
                operName = context.getText(R.string.oper_long_99999).toString();
            } else {
                operName = tm.getNetworkOperatorName();
            }
        }
        if(operName==null||operName.equals("")){
            operName="无服务";
        }
        return operName;
    }


}

