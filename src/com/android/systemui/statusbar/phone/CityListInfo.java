package com.android.systemui.statusbar.phone;

/**
 * 2011-5-9
 * 城市列表
 */
public class CityListInfo {
	String city, state, location;

	public CityListInfo(String city, String state, String location) {
		this.city = city;
		this.state = state;
		this.location = location;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
}
