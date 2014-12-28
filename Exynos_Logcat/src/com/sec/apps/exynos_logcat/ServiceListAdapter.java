package com.sec.apps.exynos_logcat;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ServiceListAdapter extends ArrayAdapter<LogElement> {

	private Context mContext;
	private List<LogElement> elements;

	public ServiceListAdapter(Context context,int resourceId,List<LogElement> elements) {
		super(context, resourceId, elements);
		this.mContext = context;
		this.elements = elements;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LogElement entry = elements.get(position);
		TextView tv;
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
