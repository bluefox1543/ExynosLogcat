package com.sec.apps.exynos_logcat;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class FilterDialog extends AlertDialog {
	private Preferences mPrefs;
	private LogcatMain mLogMain;

	public FilterDialog(LogcatMain logActivity) {
		super(logActivity);

		mLogMain = logActivity;
		mPrefs = new Preferences(mLogMain);

		LayoutInflater factory = LayoutInflater.from(mLogMain);
		final View view = factory.inflate(R.layout.filter_dialog, null);

		final EditText filterEdit = (EditText) view.findViewById(R.id.filter_edit);
		filterEdit.setText(mPrefs.getFilter());

		final TextView patternErrorText = (TextView) view.findViewById(R.id.pattern_error_text);
		patternErrorText.setVisibility(View.GONE);

		final CheckBox patternCheckBox = (CheckBox) view.findViewById(R.id.pattern_checkbox);
		patternCheckBox.setChecked(mPrefs.isFilterPattern());
		CompoundButton.OnCheckedChangeListener occl = new CompoundButton.OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				if (!isChecked) {
					patternErrorText.setVisibility(View.GONE);
				}
			}
		};
		patternCheckBox.setOnCheckedChangeListener(occl);

		setView(view);

		setButton(BUTTON_POSITIVE, "확인",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String f = filterEdit.getText().toString();
				if (patternCheckBox.isChecked()) {
					try {
						Pattern.compile(f);
					} catch (PatternSyntaxException e) {
						patternErrorText.setVisibility(View.VISIBLE);
						return;
					}
				}

				patternErrorText.setVisibility(View.GONE);

				mPrefs.setFilter(filterEdit.getText().toString());
				mPrefs.setFilterPattern(patternCheckBox.isChecked());

				mLogMain.setFilterMenu();
				mLogMain.dismissDialog(LogcatMain.FILTER_DIALOG);
				mLogMain.reset();
			}
		});
		setButton(BUTTON_NEUTRAL, "초기화",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				mPrefs.setFilter(null);
				filterEdit.setText("");

				mPrefs.setFilterPattern(false);
				patternCheckBox.setChecked(false);						

				mLogMain.setFilterMenu();
				mLogMain.dismissDialog(LogcatMain.FILTER_DIALOG);
				mLogMain.reset();
			}
		});
		setButton(BUTTON_NEGATIVE,  "취소",new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				filterEdit.setText(mPrefs.getFilter());
				patternCheckBox.setChecked(mPrefs.isFilterPattern());						

				mLogMain.dismissDialog(LogcatMain.FILTER_DIALOG);
			}
		});
	}
}
