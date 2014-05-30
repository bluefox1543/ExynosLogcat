package com.sec.apps.exynos_logcat;

import android.graphics.Color;

public enum LogLevel {
	V(0, "#121212","Verbose"), 
	D(1, "#00006C","Debug"), 
	I(2, "#20831B","Information"), 
	W(3,"#FD7916","Warning"), 
	E(4, "#FD0010","Error"), 
	F(5, "#ff0066","Fatal");

	private static LogLevel[] sorted = {V,D,I,W,E,F};
	private String mHexColor;
	private String mTitle;
	private int mColor;
	private int mValue;

	private LogLevel(int value, String hexColor, String title) {
		mValue = value;
		mHexColor = hexColor;
		mColor = Color.parseColor(hexColor);
		mTitle = title;
	}

	public String getHexColor() {
		return mHexColor;
	}

	public int getColor() {
		return mColor;
	}

	public int getValue() {
		return mValue;
	}

	public static LogLevel getOriginal(int what) {
		return sorted[what];
	}

	public String getTitle() {
		return mTitle;
	}
}
