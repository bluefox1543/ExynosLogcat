package com.sec.apps.exynos_logcat;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Preferences {
	public static final String LEVEL_KEY = "level";
	public static final String FORMAT_KEY = "format";
	public static final String TYPE_KEY = "type";
	public static final String FILTER_PATTERN_KEY = "filterPattern";
	public static final String SIZE_W_KEY = "sizew";
	public static final String SIZE_H_KEY = "sizeh";

	private SharedPreferences sharedPrefs = null;

	public Preferences(Context context) {
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	private String getString(String key, String def) {
		String s = sharedPrefs.getString(key, def);
		return s;
	}

	private void setString(String key, String val) {
		Editor e = sharedPrefs.edit();
		e.putString(key, val);
		e.commit();
	}

	private boolean getBoolean(String key, boolean def) {
		boolean b = sharedPrefs.getBoolean(key, def);
		return b;
	}

	private void setBoolean(String key, boolean val) {
		Editor e = sharedPrefs.edit();
		e.putBoolean(key, val);
		e.commit();
	}
	
	private int getInt(String key, int def) {
		int i = sharedPrefs.getInt(key, def);
		return i;
	}

	private void setInt(String key, int val) {
		Editor e = sharedPrefs.edit();
		e.putInt(key, val);
		e.commit();
	}

	public LogLevel getLevel() {
		return LogLevel.valueOf(getString(LEVEL_KEY, "V"));
	}

	public void setLevel(LogLevel level) {
		setString(LEVEL_KEY, level.toString());
	}

	public PatternFormat getFormat() {
		String f = getString(FORMAT_KEY, "BRIEF");

		if (!f.equals(f.toUpperCase())) {
			f = f.toUpperCase();
			setString(FORMAT_KEY, f);
		}

		return PatternFormat.valueOf(f);
	}

	public void setFormat(PatternFormat format) {
		setString(FORMAT_KEY, format.toString());
	}

	public LogType getType() {
		return LogType.valueOf(getString(TYPE_KEY, "MAIN"));
	}

	public void setType(LogType buffer) {
		setString(TYPE_KEY, buffer.toString());
	}

	public String getFilter() {
		return getString("filter", null);
	}

	public Pattern getFilterPattern() {
		if (!isFilterPattern()) {
			return null;
		}

		String p = getString("filter", null);
		if (p == null) {
			return null;
		}
		try {
			return Pattern.compile(p, Pattern.CASE_INSENSITIVE);
		} catch (PatternSyntaxException e) {
			setString("filter", null);
			return null;
		}
	}

	public void setFilter(String filter) {
		setString("filter", filter);
	}

	public boolean isFilterPattern() {
		return getBoolean(FILTER_PATTERN_KEY, false);
	}

	public void setFilterPattern(boolean filterPattern) {
		setBoolean(FILTER_PATTERN_KEY, filterPattern);
	}
	
	public OverlaySize getOverlaySize(){
		return new OverlaySize(getInt(SIZE_W_KEY, 240),getInt(SIZE_H_KEY, 320));
	}
	
	public void setOverlaySize(OverlaySize size){
		setInt(SIZE_W_KEY, size.getWidth());
		setInt(SIZE_H_KEY, size.getHeight());
	}
}
