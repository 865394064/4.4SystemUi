package com.android.systemui.statusbar.phone;

public class WeatherInfoModel {

}

/**
 * 2011-5-6
 * 当前天气
 */
class CurrentWeatherInfo {
	String weathericon, temperature;

	public String getWeathericon() {
		return weathericon;
	}

	public void setWeathericon(String weathericon) {
		this.weathericon = weathericon;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
}

/**
 * 2011-5-6
 *第一天到第六天
 */
class WeatherInfoDay1ToDay6 {
	String daycode, weathericon, hightemperature, lowtemperature;

	public String getDaycode() {
		return daycode;
	}

	public void setDaycode(String daycode) {
		this.daycode = daycode;
	}

	public String getWeathericon() {
		return weathericon;
	}

	public void setWeathericon(String weathericon) {
		this.weathericon = weathericon;
	}

	public String getHightemperature() {
		return hightemperature;
	}

	public void setHightemperature(String hightemperature) {
		this.hightemperature = hightemperature;
	}

	public String getLowtemperature() {
		return lowtemperature;
	}

	public void setLowtemperature(String lowtemperature) {
		this.lowtemperature = lowtemperature;
	}
}

