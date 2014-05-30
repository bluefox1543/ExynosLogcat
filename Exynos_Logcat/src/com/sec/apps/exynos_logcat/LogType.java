package com.sec.apps.exynos_logcat;

import java.util.HashMap;

public enum LogType {
	MAIN("main", "Main"),
	EVENTS("events", "Events"),
	RADIO("radio", "Radio");
	
	//������ �α� ������ Ÿ��.
	//dev/log�� MAIN,EVENTS,RADIO,SYSTEM 4������ �αװ� ����.
	
	private static LogType[] sorted = {MAIN,EVENTS,RADIO};
	private String mValue;
	private String mTitle;
	private static final HashMap<String,LogType> values = new HashMap<String,LogType>();
	static {
		values.put(MAIN.mValue, MAIN); 
		values.put(EVENTS.mValue, EVENTS); 
		values.put(RADIO.mValue, RADIO); 
	}
	
	private LogType(String value, String title) {
		mValue = value;
		mTitle = title;
	}
	
	public String getTitle() {
		return mTitle;
	}	
	
	public static final LogType byValue(String value) {
		return values.get(value);
	}
	
	public static LogType getOriginal(int what) {
		return sorted[what];
	}
	
	public String getValue() {
		return mValue;
	}
}
