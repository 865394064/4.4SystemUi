package com.android.systemui.statusbar.phone;
/**
 * 2011-5-6
 *读取天气信息
 */
import java.io.ByteArrayInputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.android.systemui.statusbar.phone.Weather;

public class WebAction {
	
	private static final String TAG = "WebAction";
	private static final boolean DEBUG = true;

	private static final String ADC_DATABASE = "adc_database";
	private static final String LOCAL = "local";
	private static final String TIME = "time";
	private static final String CURRENTCONDITIONS = "currentconditions";
	private static final String DAYTIME = "daytime";
	private static final String NIGHTTIME = "nighttime";
	private static final String WEATHERICON = "weathericon";
	private static final String HIGHTEMPERATURE = "hightemperature";
	private static final String LOWTEMPERATURE = "lowtemperature";	
	private static final String TEMPERATURE = "temperature";
	
	private static final String BASE_URI = "http://forecastfox.accuweather.com/adcbin/forecastfox/weather_data.asp?location=";
	private static final String BASE_METRIC = "&metric=";
	
	private String mLocalTime; //当地时间 2011-5-10
	//初始化需要传入的值
	private Context mContext;
	private Uri mUri;
	private String mLocation;
	private int mMetric;
	//private DatabaseAction mDatabaseAction;
	private Vector<CurrentWeatherInfo> mCurrentWeatherInfo;//当前信息
	private Vector<WeatherInfoDay1ToDay6> mDayTimeInfoDay1ToDay6;//白天
	private Vector<WeatherInfoDay1ToDay6> mNightTimeInfoDay1ToDay6;//晚上
	
	public WebAction(Context context,String location, int metric) {
		mContext = context;
		//mUri = uri;
		mLocation = location;
		mMetric = metric;
		
		//mDatabaseAction = new DatabaseAction(context);
	  	//初始化向量	
		mCurrentWeatherInfo = new Vector<CurrentWeatherInfo>();
		mDayTimeInfoDay1ToDay6 = new Vector<WeatherInfoDay1ToDay6>();
		mNightTimeInfoDay1ToDay6 = new Vector<WeatherInfoDay1ToDay6>();
	}
	
	// 开始下载
	public void startLoadData() {
	       //Log.i("xss_weather", "WebAction    startLoadData()====");
		if (mListener != null) {
			mListener.onStartWebDownLoad();
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					HttpClient client = new DefaultHttpClient();					
					HttpGet getMethod = new HttpGet(BASE_URI
							+ encodeUrlData(mLocation) + BASE_METRIC + mMetric);
					 /*Log.i("locationapp", "WebAction    startLoadData()====URI="+BASE_URI
								+ encodeUrlData(mLocation) + BASE_METRIC + mMetric);*/
					ResponseHandler<String> responseHandler = new BasicResponseHandler();
					String responseBody = client.execute(getMethod,
							responseHandler);
					parseData(encodeUrlData(responseBody));
				} catch (Exception e) {
					Log.e(TAG, "run  network error ......... ");
					//这里在网络断开的时候会报错
					//mWebDownLoadListener.failWebDownLoad();
				}
			}
		}).start();
	}
	
	private static String encodeUrlData(String urldata) {
		return urldata.replace("&#36;", "$").replace("&#39;", "'")
				.replace("&#94", "^").replace("%3A", ":").replace("%2F", "/")
				.replace("&#60;", "%3c").replace("|", "%7C");
	}
	
	// 解析xml
	private void parseData(String inform) {
	    //Log.i("locationapp", "WebAction    parseData()====inform="+inform);
		mCurrentWeatherInfo.removeAllElements();
		mDayTimeInfoDay1ToDay6.removeAllElements();
		mNightTimeInfoDay1ToDay6.removeAllElements();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setIgnoringElementContentWhitespace(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new ByteArrayInputStream(inform.getBytes()));
			//Log.i("locationapp", "WebAction    parseData()______document="+document);
			Element root = document.getDocumentElement();
			if (root.getNodeName().equals(ADC_DATABASE)) {
				// 当地时间
				NodeList nodesLocal = document.getElementsByTagName(LOCAL);
				for (int i = 0; i < nodesLocal.getLength(); i++) {
					NodeList nodeitems = nodesLocal.item(0).getChildNodes();
					for (int j = 0; j < nodeitems.getLength(); j++)
						pushLocalValue(nodeitems.item(j));
				}
				// 当前天气
				NodeList nodesCurrentconditions = document.getElementsByTagName(CURRENTCONDITIONS);
				for (int i = 0; i < nodesCurrentconditions.getLength(); i++) {
					mCurrentWeatherInfo.addElement(new CurrentWeatherInfo());
					NodeList nodeitems = nodesCurrentconditions.item(i).getChildNodes();
					for (int j = 0; j < nodeitems.getLength(); j++)
						pushCurrentconditionsValue(nodeitems.item(j));
				}
				// 白天
				NodeList nodesDaytime = document.getElementsByTagName(DAYTIME);
				for (int i = 0; i < nodesDaytime.getLength(); i++) {
					mDayTimeInfoDay1ToDay6.addElement(new WeatherInfoDay1ToDay6());
					NodeList nodeitems = nodesDaytime.item(i).getChildNodes();
					for (int j = 0; j < nodeitems.getLength(); j++)
						pushDaytimeValue(nodeitems.item(j));
				}
				// 晚上
				NodeList nodesNightime = document.getElementsByTagName(NIGHTTIME);
				for (int i = 0; i < nodesNightime.getLength(); i++) {
					mNightTimeInfoDay1ToDay6.addElement(new WeatherInfoDay1ToDay6());
					NodeList nodeitems = nodesNightime.item(i).getChildNodes();
					for (int j = 0; j < nodeitems.getLength(); j++)
						pushNighttimeValue(nodeitems.item(j));
				}
				// 成功下载数据,将数据存入数据库
				// 然后在DisplayWeatherView做画图的工作
				updateDatabase();
				return;
			}
		} catch (Exception e) {
			Log.e("error", e.toString());
		}
	}
	
	// 将节点数据放入向量
	private void pushLocalValue(Node node) {
		String nodeName = node.getNodeName();
		if (nodeName.equals(TIME)) {
			mLocalTime = getNodeValue(node);
		}
	}
	
	// 将节点数据放入向量
	private void pushCurrentconditionsValue(Node node) {
		String nodeName = node.getNodeName();
		if (nodeName.equals(WEATHERICON)) {
			mCurrentWeatherInfo.lastElement().setWeathericon(getNodeValue(node));
		} else if (nodeName.equals(TEMPERATURE)) {
			mCurrentWeatherInfo.lastElement().setTemperature(getNodeValue(node));
		}
	}
	
	// 将节点数据放入向量
	private void pushDaytimeValue(Node node) {
		String nodeName = node.getNodeName();
		if (nodeName.equals(WEATHERICON)) {
			mDayTimeInfoDay1ToDay6.lastElement().setWeathericon(getNodeValue(node));
		} else if (nodeName.equals(HIGHTEMPERATURE)) {
			mDayTimeInfoDay1ToDay6.lastElement().setHightemperature(getNodeValue(node));
		} else if (nodeName.equals(LOWTEMPERATURE)) {
			mDayTimeInfoDay1ToDay6.lastElement().setLowtemperature(getNodeValue(node));
		}
	}
	
	// 将节点数据放入向量
	private void pushNighttimeValue(Node node) {
		String nodeName = node.getNodeName();
		if (nodeName.equals(WEATHERICON)) {
			mNightTimeInfoDay1ToDay6.lastElement().setWeathericon(getNodeValue(node));
		} else if (nodeName.equals(HIGHTEMPERATURE)) {
			mNightTimeInfoDay1ToDay6.lastElement().setHightemperature(getNodeValue(node));
		} else if (nodeName.equals(LOWTEMPERATURE)) {
			mNightTimeInfoDay1ToDay6.lastElement().setLowtemperature(getNodeValue(node));
		}
	}

	private String getNodeValue(Node node) {
		String nodeValue;
		StringBuffer sb = new StringBuffer();
		if (node.hasChildNodes()) {
			NodeList chidList = node.getChildNodes();
			for (int m = 0; m < chidList.getLength(); m++) {
				nodeValue = chidList.item(m).getNodeValue();
				sb.append(nodeValue == null ? " " : nodeValue);
			}
		} else {
			sb.append(node.getNodeValue());
		}
		return sb.toString().trim();
	}
	
	private void updateDatabase() {
		//Log.i("locationapp", "WebAction    updateDatabase()====");
		
		ContentValues values = null;
		values = getContentValues(mCurrentWeatherInfo, mDayTimeInfoDay1ToDay6);
        //Log.i("locationapp", "WebAction    updateDatabase()====   values="+values);
		//mDatabaseAction.updateValuesByUri(mUri, values);
		if (mListener != null) {			
			mListener.onFinishWebDownLoad(values); 
		}
	}
	
	private ContentValues getContentValues(
			Vector<CurrentWeatherInfo> currWeatherInfo,
			Vector<WeatherInfoDay1ToDay6> infoDay1ToDay6) {
		Long now = Long.valueOf(System.currentTimeMillis());
		ContentValues values = new ContentValues();
		values.put(Weather.Weather_Column.REFRESH_TIME, now);
		values.put(Weather.Weather_Column.LOCAL_TIME, mLocalTime);
		// 当前天气
		values.put(Weather.Weather_Column.CURRENT_WEATHERICON, currWeatherInfo.get(0).getWeathericon());
		int currentTemp = Integer.parseInt(currWeatherInfo.get(0).getTemperature()); 
		values.put(Weather.Weather_Column.CURRENT_TEMPERATURE_F, getFTmep(currentTemp));
		values.put(Weather.Weather_Column.CURRENT_TEMPERATURE_C, currentTemp);
		// 第一天
		values.put(Weather.Weather_Column.WEATHER_ICON_DAY1, infoDay1ToDay6.get(0).getWeathericon());
		int highTempDay1 = Integer.parseInt(infoDay1ToDay6.get(0).getHightemperature()); 
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY1_F, getFTmep(highTempDay1));
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY1_C, highTempDay1);
		int lowTempDay1 = Integer.parseInt(infoDay1ToDay6.get(0).getLowtemperature()); 
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY1_F, getFTmep(lowTempDay1));
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY1_C, lowTempDay1);
		// 第二天
		values.put(Weather.Weather_Column.WEATHER_ICON_DAY2, infoDay1ToDay6.get(1).getWeathericon());
		int highTempDay2 = Integer.parseInt(infoDay1ToDay6.get(1).getHightemperature()); 
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY2_F, getFTmep(highTempDay2));
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY2_C, highTempDay2);
		int lowTempDay2 = Integer.parseInt(infoDay1ToDay6.get(1).getLowtemperature()); 
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY2_F, getFTmep(lowTempDay2));
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY2_C, lowTempDay2);
		// 第三天
		values.put(Weather.Weather_Column.WEATHER_ICON_DAY3, infoDay1ToDay6.get(2).getWeathericon());
		int highTempDay3 = Integer.parseInt(infoDay1ToDay6.get(2).getHightemperature()); 
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY3_F, getFTmep(highTempDay3));
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY3_C, highTempDay3);
		int lowTempDay3 = Integer.parseInt(infoDay1ToDay6.get(2).getLowtemperature()); 
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY3_F, getFTmep(lowTempDay3));
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY3_C, lowTempDay3);
		// 第四天
		values.put(Weather.Weather_Column.WEATHER_ICON_DAY4, infoDay1ToDay6.get(3).getWeathericon());
		int highTempDay4 = Integer.parseInt(infoDay1ToDay6.get(3).getHightemperature()); 
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY4_F, getFTmep(highTempDay4));
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY4_C, highTempDay4);
		int lowTempDay4 = Integer.parseInt(infoDay1ToDay6.get(3).getLowtemperature()); 
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY4_F, getFTmep(lowTempDay4));
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY4_C, lowTempDay4);
		// 第五天
		values.put(Weather.Weather_Column.WEATHER_ICON_DAY5, infoDay1ToDay6.get(4).getWeathericon());
		int highTempDay5 = Integer.parseInt(infoDay1ToDay6.get(4).getHightemperature()); 
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY5_F, getFTmep(highTempDay5));
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY5_C, highTempDay5);
		int lowTempDay5 = Integer.parseInt(infoDay1ToDay6.get(4).getLowtemperature()); 
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY5_F, getFTmep(lowTempDay5));
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY5_C, lowTempDay5);
		// 第六天
		values.put(Weather.Weather_Column.WEATHER_ICON_DAY6, infoDay1ToDay6.get(5).getWeathericon());
		int highTempDay6 = Integer.parseInt(infoDay1ToDay6.get(5).getHightemperature()); 
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY6_F, getFTmep(highTempDay6));
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY6_C, highTempDay6);
		int lowTempDay6 = Integer.parseInt(infoDay1ToDay6.get(5).getLowtemperature()); 
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY6_F, getFTmep(lowTempDay6));
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY6_C, lowTempDay6);
		// 第六天
		values.put(Weather.Weather_Column.WEATHER_ICON_DAY7, infoDay1ToDay6.get(6).getWeathericon());
		int highTempDay7 = Integer.parseInt(infoDay1ToDay6.get(6).getHightemperature()); 
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY7_F, getFTmep(highTempDay7));
		values.put(Weather.Weather_Column.HIGH_TEMPERATURE_DAY7_C, highTempDay7);
		int lowTempDay7 = Integer.parseInt(infoDay1ToDay6.get(6).getLowtemperature()); 
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY7_F, getFTmep(lowTempDay7));
		values.put(Weather.Weather_Column.LOW_TEMPERATURE_DAY7_C, lowTempDay7);
		return values;
	}	
	// 获得华氏温度
		public static String getFTmep(int temp) {
			return String.valueOf(Math.round((double) temp * 9 / 5 + 32));
		}
	/**
	 * 2011-5-13S
	 * 下载网络天气信息的监听
	 */
	public WebDownLoadListener mListener;
	
	public void setWebDownLoadListener(WebDownLoadListener listener) {
		mListener = listener;
	}
	
	public interface WebDownLoadListener {
		public void onStartWebDownLoad();
	    public void onFinishWebDownLoad(ContentValues values);
	}
}
