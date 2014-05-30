package com.sec.apps.exynos_logcat;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class PreferencesActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	private ListPreference mLogLevelPreference;
	private ListPreference mPatternFormatPreference;
	private ListPreference mLogTypePreference;

	private Preferences mPrefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.layout.prefs);

		mPrefs = new Preferences(this);

		mLogLevelPreference = (ListPreference) getPreferenceScreen().findPreference(Preferences.LEVEL_KEY);
		mPatternFormatPreference = (ListPreference) getPreferenceScreen().findPreference(Preferences.FORMAT_KEY);
		mLogTypePreference = (ListPreference) getPreferenceScreen().findPreference(Preferences.TYPE_KEY);

		setResult(Activity.RESULT_OK);
	}

	private void setLevelTitle() {
		mLogLevelPreference.setTitle("-레벨 (" + mPrefs.getLevel().getTitle() + ")");
	}

	private void setFormatTitle() {
		mPatternFormatPreference.setTitle("-패턴 (" + mPrefs.getFormat().getTitle() + ")");
	}

	private void setTypeTitle() {
		mLogTypePreference.setTitle("-타입 (" + mPrefs.getType().getTitle() + ")");
	}

	@Override
	protected void onResume() {
		super.onResume();

		setLevelTitle();
		setFormatTitle();
		setTypeTitle();

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,String key) {
		if (key.equals(Preferences.LEVEL_KEY)) {
			setLevelTitle();
		} else if (key.equals(Preferences.FORMAT_KEY)) {
			setFormatTitle();
		} else if (key.equals(Preferences.TYPE_KEY)) {
			setTypeTitle();
		}
	}
}
