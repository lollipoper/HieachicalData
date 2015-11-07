package com.sunfun.datapicker;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public final class ViewHolder {
	SparseArray<View> views;
	View convertView;
	int position;

	public View getConvertView() {
		return convertView;
	}

	public ViewHolder(View convertView, int position) {
		this.views = new SparseArray<View>();
		this.convertView = convertView;
		this.position = position;
		convertView.setTag(this);
	}

	public ViewHolder(Context context, int layoutId, ViewGroup parent, int position) {
		this(LayoutInflater.from(context).inflate(layoutId, parent, false), position);
	}

	public static ViewHolder getViewHolder(Context context, int position,
			View convertView, ViewGroup parent, int layoutId) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder(context, layoutId, parent, position);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		return holder;
	}

	public int getPosition() {
		return position;
	}

	@SuppressWarnings("unchecked")
	public <T extends View> T getView(int resId) {
		View view = views.get(resId);
		if (view == null) {
			view = convertView.findViewById(resId);
			views.put(resId, view);
		}
		return (T) view;
	}

}
