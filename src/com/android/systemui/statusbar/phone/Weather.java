package com.android.systemui.statusbar.phone;

import android.net.Uri;
import android.provider.BaseColumns;

public class Weather {
	private Weather() {

	}

	public static final class Weather_Column implements BaseColumns {
		private Weather_Column() {
		};

		/*public static final Uri CONTENT_URI = Uri.parse("content://"
				+ WeatherProvider.AUTHORITY + "/weather");*/
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.weather";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.weather";
		
		public static final String ASC = " asc";
		public static final String DESC = " desc";
		// 默认排序
		public static final String DEFAULT_SORT_ORDER = "_id asc";
		// 城市
		public static final String CITY = "city";
		public static final String STATE = "state";
		public static final String LOCTION = "location";
		public static final String CITY_CN = "city_cn";
		// 单位
		public static final String METRIC = "metric";
		// 位置
		public static final String POSITION = "position";
		// 更新时间
		public static final String REFRESH_TIME = "refresh_time";
		// 当地时间
	    public static final String LOCAL_TIME = "local_time";
		// 当前时间
		public static final String CURRENT_WEATHERICON = "current_weathericon";
		public static final String CURRENT_TEMPERATURE_F = "current_temperature_f";
		public static final String CURRENT_TEMPERATURE_C = "current_temperature_c";
		// 第一天
		public static final String WEATHER_ICON_DAY1 = "weather_icon_day1";
		public static final String HIGH_TEMPERATURE_DAY1_F = "high_temperature_day1_f";
		public static final String HIGH_TEMPERATURE_DAY1_C = "high_temperature_day1_c";
		public static final String LOW_TEMPERATURE_DAY1_F = "low_temperature_day1_f";
		public static final String LOW_TEMPERATURE_DAY1_C = "low_temperature_day1_c";
		// 第二天
		public static final String WEATHER_ICON_DAY2 = "weather_icon_day2";
		public static final String HIGH_TEMPERATURE_DAY2_F = "high_temperature_day2_f";
		public static final String HIGH_TEMPERATURE_DAY2_C = "high_temperature_day2_c";
		public static final String LOW_TEMPERATURE_DAY2_F = "low_temperature_day2_f";
		public static final String LOW_TEMPERATURE_DAY2_C = "low_temperature_day2_c";
		// 第三天
		public static final String WEATHER_ICON_DAY3 = "weather_icon_day3";
		public static final String HIGH_TEMPERATURE_DAY3_F = "high_temperature_day3_f";
		public static final String HIGH_TEMPERATURE_DAY3_C = "high_temperature_day3_c";
		public static final String LOW_TEMPERATURE_DAY3_F = "low_temperature_day3_f";
		public static final String LOW_TEMPERATURE_DAY3_C = "low_temperature_day3_c";
		// 第四天
		public static final String WEATHER_ICON_DAY4 = "weather_icon_day4";
		public static final String HIGH_TEMPERATURE_DAY4_F = "high_temperature_day4_f";
		public static final String HIGH_TEMPERATURE_DAY4_C = "high_temperature_day4_c";
		public static final String LOW_TEMPERATURE_DAY4_F = "low_temperature_day4_f";
		public static final String LOW_TEMPERATURE_DAY4_C = "low_temperature_day4_c";
		// 第五天
		public static final String WEATHER_ICON_DAY5 = "weather_icon_day5";
		public static final String HIGH_TEMPERATURE_DAY5_F = "high_temperature_day5_f";
		public static final String HIGH_TEMPERATURE_DAY5_C = "high_temperature_day5_c";
		public static final String LOW_TEMPERATURE_DAY5_F = "low_temperature_day5_f";
		public static final String LOW_TEMPERATURE_DAY5_C = "low_temperature_day5_c";
		// 第六天
		public static final String WEATHER_ICON_DAY6 = "weather_icon_day6";
		public static final String HIGH_TEMPERATURE_DAY6_F = "high_temperature_day6_f";
		public static final String HIGH_TEMPERATURE_DAY6_C = "high_temperature_day6_c";
		public static final String LOW_TEMPERATURE_DAY6_F = "low_temperature_day6_f";
		public static final String LOW_TEMPERATURE_DAY6_C = "low_temperature_day6_c";
		// 第七天
		public static final String WEATHER_ICON_DAY7 = "weather_icon_day7";
		public static final String HIGH_TEMPERATURE_DAY7_F = "high_temperature_day7_f";
		public static final String HIGH_TEMPERATURE_DAY7_C = "high_temperature_day7_c";
		public static final String LOW_TEMPERATURE_DAY7_F = "low_temperature_day7_f";
		public static final String LOW_TEMPERATURE_DAY7_C = "low_temperature_day7_c";
	}

}
