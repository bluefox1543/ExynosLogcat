package com.sec.apps.exynos_logcat;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class LogListAdapter extends ArrayAdapter<LogElement> {

	private Activity mActivity;
	private List<LogElement> elements;

	public LogListAdapter(Activity activity, int resourceId,List<LogElement> elements) {
		super(activity, resourceId, elements);
		this.mActivity = activity;
		this.elements = elements;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LogElement entry = elements.get(position);
		TextView tv;
		if (convertView == null) {
			LayoutInflater inflater = mActivity.getLayoutInflater();
			tv = (TextView) inflater.inflate(R.layout.listelement, null);
		} else {
			tv = (TextView) convertView;
		}

		tv.setText(entry.getText());
		tv.setTextColor(entry.getLevel().getColor());
		tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
		
		return tv;
	}

	public void remove(int position) {
		LogElement elem = elements.get(position);
		remove(elem);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	public LogElement get(int position) {
		return elements.get(position);
	}

	public List<LogElement> getElements() {
		return Collections.unmodifiableList(elements);
	}
}
