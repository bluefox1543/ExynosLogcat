package com.sec.apps.exynos_logcat;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum PatternFormat {
	BRIEF("brief", "Brief", Pattern.compile("^([VDIWEF])/")),
	PROCESS("process", "Process", Pattern.compile("^([VDIWEF])\\(")),
	TAG("tag", "Tag", Pattern.compile("^([VDIWEF])/")),
	THREAD("thread", "Thread", Pattern.compile("^([VDIWEF])\\(")),
	TIME("time", "Time", Pattern.compile(" ([VDIWEF])/")),
	THREADTIME("threadtime", "ThreadTime", Pattern.compile(" ([VDIWEF]) ")),
	LONG("long", "Long", Pattern.compile("([VDIWEF])/")),
	RAW("raw", "Raw", null);

	//brief = 레벨/태그/PID 포커싱(기본 포맷)
	//process = PID만 출력
	//tag = 레벨/태그만 출력
	//raw = raw 로그 메시지 출력. 다른 메타데이터 필드는 포함하지 않는다.
	//time = 프로세스에 대한 날짜, 시행날짜, 레벨/태그/PID 포함.
	//thread = 레벨/태그/PID/TID 포함
	//threadtime = 날짜, 시행날짜, 레벨/태그/PID/TID 포함
	//long = 모든 메타데이터 포함.

	private static PatternFormat[] sorted = {BRIEF,PROCESS,TAG,THREAD,TIME,THREADTIME,LONG,RAW};
	private String mValue;
	private String mTitle;
	private Pattern mLevelPattern;
	private static final HashMap<String,PatternFormat> values = new HashMap<String,PatternFormat>();
	static {
		values.put(BRIEF.mValue, BRIEF); 
		values.put(PROCESS.mValue, PROCESS); 
		values.put(TAG.mValue, TAG); 
		values.put(THREAD.mValue, THREAD); 
		values.put(THREADTIME.mValue, THREAD); 
		values.put(TIME.mValue, TIME); 
		values.put(RAW.mValue, RAW); 
		values.put(LONG.mValue, LONG); 
	}

	private PatternFormat(String value, String title, Pattern levelPattern) {
		mValue = value;
		mTitle = title;
		mLevelPattern = levelPattern;
	}

	public String getTitle() {
		return mTitle;
	}	

	public static final PatternFormat byValue(String value) {
		return values.get(value);
	}

	public LogLevel getLevel(String line) {
		if (mLevelPattern == null) {
			return null;
		}
		Matcher m = mLevelPattern.matcher(line);
		if (m.find()) {
			return LogLevel.valueOf(m.group(1));
		}
		return null;
	}

	public static PatternFormat getOriginal(int what) {
		return sorted[what];
	}

	public String getValue() {
		return mValue;
	}
}