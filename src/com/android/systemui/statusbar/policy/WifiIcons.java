/*
 * Copyright (C) 2008 The Android Open Source Project
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

package com.android.systemui.statusbar.policy;

import com.android.systemui.R;

class WifiIcons {
/* //removed by wang 20130731
    static final int[][] WIFI_SIGNAL_STRENGTH = {
            { R.drawable.stat_sys_wifi_signal_0,
              R.drawable.stat_sys_wifi_signal_1,
              R.drawable.stat_sys_wifi_signal_2,
              R.drawable.stat_sys_wifi_signal_3,
              R.drawable.stat_sys_wifi_signal_4 },
            { R.drawable.stat_sys_wifi_signal_0,
              R.drawable.stat_sys_wifi_signal_1_fully,
              R.drawable.stat_sys_wifi_signal_2_fully,
              R.drawable.stat_sys_wifi_signal_3_fully,
              R.drawable.stat_sys_wifi_signal_4_fully }
        };
*/

//added by wang 20130731 start
    static final int[][] WIFI_SIGNAL_BLACK_STRENGTH = {
            { R.drawable.iphone_wifi_black_0,
              R.drawable.iphone_wifi_black_1,
              R.drawable.iphone_wifi_black_2,
              R.drawable.iphone_wifi_black_3 },
            { R.drawable.iphone_wifi_black_0,
              R.drawable.iphone_wifi_black_1,
              R.drawable.iphone_wifi_black_2,
              R.drawable.iphone_wifi_black_3 }
        };
    static final int[][] WIFI_SIGNAL_WHITE_STRENGTH = {
            { R.drawable.iphone_wifi_white_0,
              R.drawable.iphone_wifi_white_1,
              R.drawable.iphone_wifi_white_2,
              R.drawable.iphone_wifi_white_3 },
            { R.drawable.iphone_wifi_white_0,
              R.drawable.iphone_wifi_white_1,
              R.drawable.iphone_wifi_white_2,
              R.drawable.iphone_wifi_white_3 }
        };
	public static int getWifiSignalStrength(int netCondition, int level, boolean white) {
		return white ? WIFI_SIGNAL_WHITE_STRENGTH[netCondition][level] : WIFI_SIGNAL_BLACK_STRENGTH[netCondition][level];
	}
//added by wang 20130731 end



    static final int[][] QS_WIFI_SIGNAL_STRENGTH = {
            { R.drawable.ic_qs_wifi_0,
              R.drawable.ic_qs_wifi_1,
              R.drawable.ic_qs_wifi_2,
              R.drawable.ic_qs_wifi_3,
              R.drawable.ic_qs_wifi_4 },
            { R.drawable.ic_qs_wifi_0,
              R.drawable.ic_qs_wifi_full_1,
              R.drawable.ic_qs_wifi_full_2,
              R.drawable.ic_qs_wifi_full_3,
              R.drawable.ic_qs_wifi_full_4 }
        };

    //static final int WIFI_LEVEL_COUNT = WIFI_SIGNAL_STRENGTH[0].length;//removed by wang 20130731
    static final int WIFI_LEVEL_COUNT = WIFI_SIGNAL_WHITE_STRENGTH[0].length;//added by wang 20130731
}
