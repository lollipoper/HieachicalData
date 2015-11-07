package com.sunfun.datapicker;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.sunfun.datapicker.HierachicalView.HierachicalInit;
import com.sunfun.datapicker.HierachicalView.ItemCheckListener;

public class MainActivity extends Activity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		@SuppressWarnings("unchecked")
		HierachicalView<String, String> pickerView = (HierachicalView<String, String>) findViewById(R.id.dataPickerView1);
		List<DataEntity<String, String>> dataSet = new ArrayList<DataEntity<String, String>>();
		List<String> aRight = new ArrayList<String>();
		for (int i = 0; i < 8; i++) {
			aRight.add("A" + (i + 1));
		}
		List<String> bRight = new ArrayList<String>();
		for (int i = 0; i < 8; i++) {
			bRight.add("B" + (i + 1));
		}
		List<String> cRight = new ArrayList<String>();
		for (int i = 0; i < 8; i++) {
			cRight.add("C" + (i + 1));
		}
		List<String> dRight = new ArrayList<String>();
		for (int i = 0; i < 8; i++) {
			dRight.add("D" + (i + 1));
		}
		dataSet.add(new DataEntity<String, String>("A", aRight));
		dataSet.add(new DataEntity<String, String>("B", bRight));
		dataSet.add(new DataEntity<String, String>("C", cRight));
		dataSet.add(new DataEntity<String, String>("D", dRight));

		pickerView.setDataSet(dataSet);
		pickerView.setLeftViewResId(R.layout.left_item);
		pickerView.setRightViewResId(android.R.layout.simple_list_item_1);
		pickerView.setHierachicalInit(new HierachicalInit<String, String>() {

			@Override
			public void initLeftView(ViewHolder holder, String item) {
//				TextView view = (TextView) holder.getView(R.id.textView1);
//				view.setTextColor(getResources().getColor(R.color.text));
//				view.setText(item);
			}

			@Override
			public void initRightView(ViewHolder holder, String item) {
				((TextView) holder.getView(android.R.id.text1)).setText(item);
			}
		});
		pickerView.setItemCheckListener(new ItemCheckListener() {

			@Override
			public void leftItemChecked(ViewHolder holder) {
				TextView tvName = (TextView) holder.getView(R.id.textView1);
				tvName.setTextColor(Color.RED);
			}

			@Override
			public void leftItemUnChecked(ViewHolder holder) {
				TextView tvName = (TextView) holder.getView(R.id.textView1);
				tvName.setTextColor(Color.BLACK);
			}

			@Override
			public void rightItemChecked(ViewHolder holder) {
				TextView tvName = (TextView) holder.getView(android.R.id.text1);
				tvName.setTextColor(Color.RED);
			}

			@Override
			public void rightItemUnChecked(ViewHolder holder) {
				TextView tvName = (TextView) holder.getView(android.R.id.text1);
				tvName.setTextColor(Color.BLACK);
			}
		});
	}


}
