package com.sunfun.datapicker;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * 层级数据显示视图，支持显示2层的数据显示
 * @author wenxc
 *
 * @param <T> 第一层视图实体类
 * @param <V> 第二层视图实体类
 */
public class HierachicalView<T, V> extends ViewGroup {
	/**
	 * 初始化左右视图数据，保证控件数据的一致性
	 * 
	 * @param <T> 左视图实体类
	 * @param <V> 右视图实体类
	 */
	public interface HierachicalInit<T, V> {
		
		/**
		 * 初始化左视图
		 * 
		 * @param holder 复用视图
		 * @param item 左视图实体类
		 */
		void initLeftView(ViewHolder holder, T item);

		/**
		 * 初始化右视图
		 * 
		 * @param holder 复用视图
		 * @param item 右视图实体类
		 */
		void initRightView(ViewHolder holder, V item);
	}
	
	/**
	 * 自定义数据持久化,可选
	 */
	public interface DataPersistence{
		void saveLeftSelectedPosition(int posi);
		int loadLeftSelectedPosition();
		
		void saveRightSelectedPosition(int posi);
		int loadRightSelectedPosition();
	}
	
	/**
	 * 选中后页面处理
	 */
	public interface ItemCheckListener{
		void leftItemChecked(ViewHolder holder);
		void leftItemUnChecked(ViewHolder holder);
		void rightItemChecked(ViewHolder holder);
		void rightItemUnChecked(ViewHolder holder);
	}
	
	public final static String TAG = HierachicalView.class.getSimpleName();
	public final static int INVALID_POSITION = -1;

	// 左右视图资源id
	private int leftViewResId, rightViewResId;
	// 左右视图ListView
	private ListView leftView, rightView;
	// 控件数据接口
	private List<DataEntity<T, V>> dataSet;// 注意：保证T数据不为空
	// 控件初始化回调
	private HierachicalInit<T, V> hierachicalInit;
	
	private DataPersistence persistence;
	private ItemCheckListener itemCheckListener;
	private int curLeftSelectedPosi = INVALID_POSITION;
	private int curRightSelectedPosi = INVALID_POSITION;
	
	private int lastLeftSelectedPosi = INVALID_POSITION ,lastRightSelectedPosi = INVALID_POSITION;
	// 由控件数据接口生成的左右视图数据
	private List<T> leftDataSet;
	private List<V> setectedRightDataSet;
	private CommonAdapter<T> leftAdapter;
	private CommonAdapter<V> rightAdapter;
	
	private int selectedBGColor = Color.CYAN;//选中背景颜色
	private int righViewBG = Color.GRAY;
	
	/**
	 * 此回调事件主要预防左视图有值，与之关联的右视图无值得情况下使用，保证每个list item都可以处理其事件
	 */
	private OnItemClickListener leftItemClickListener;

	/**
	 * 右视图的行点击事件回调
	 */
	private OnItemClickListener rightItemClickListener;
	

	public void setHierachicalInit(HierachicalInit<T, V> hierachicalInit) {
		this.hierachicalInit = hierachicalInit;
	}

	public void setPersistence(DataPersistence persistence) {
		this.persistence = persistence;
	}
	
	public void setItemCheckListener(ItemCheckListener itemCheckListener) {
		this.itemCheckListener = itemCheckListener;
	}

	/**
	 * 设置左视图行点击事件
	 * 
	 * @param leftItemClickListener
	 */
	public void setLeftItemClickListener(OnItemClickListener leftItemClickListener) {
		this.leftItemClickListener = leftItemClickListener;
	}

	/**
	 * 设置右视图行点击事件
	 * 
	 * @param rightItemClickListener
	 */
	public void setRightItemClickListener(OnItemClickListener rightItemClickListener) {
		this.rightItemClickListener = rightItemClickListener;
	}

	public HierachicalView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public HierachicalView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public HierachicalView(Context context) {
		super(context);
		init(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.i(TAG, "onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		leftView.measure(widthMeasureSpec, heightMeasureSpec);
		rightView.measure(widthMeasureSpec, heightMeasureSpec);
//		final int width = MeasureSpec.getSize(widthMeasureSpec);
//		
//		leftView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//			
//			@SuppressWarnings("deprecation")
//			@Override
//			public void onGlobalLayout() {
//				int height = leftView.getHeight();
//				Log.i(TAG, "onGlobalLayout:leftView = "+height);
//				if (view_height < height) {
//					view_height = height;
//				}
//				view_height = Math.min(view_height, MAX_HEIGHT);
//				setMeasuredDimension(width, view_height);
//				getViewTreeObserver().removeGlobalOnLayoutListener(this);
//			}
//		});
//		rightView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
//			
//			@SuppressWarnings("deprecation")
//			@Override
//			public void onGlobalLayout() {
//				int height = rightView.getHeight();
//				Log.i(TAG, "onGlobalLayout:rightView = "+height);
//
//				if (view_height < height) {
//					view_height = height;
//				}
//				view_height = Math.min(view_height, MAX_HEIGHT);
//				setMeasuredDimension(width, view_height);
//				invalidate();
//				getViewTreeObserver().removeGlobalOnLayoutListener(this);
//			}
//
//		});
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		leftView.layout(left, top, right / 2, bottom);
		rightView.layout(right / 2, top, right, bottom);
	}

	/**
	 * 设置控件数据
	 * 
	 * @param dataSet
	 */
	public void setDataSet(List<DataEntity<T, V>> dataSet) {
		this.dataSet = dataSet;
		initData();
	}
	
	public void setLeftViewResId(int leftViewResId) {
		this.leftViewResId = leftViewResId;
		initData();
	}

	public void setRightViewResId(int rightViewResId) {
		this.rightViewResId = rightViewResId;
		initData();
	}
	
	public void notifyDataSetChanged(){
		if (leftAdapter != null) {
			leftAdapter.notifyDataSetChanged();
		}
		if (rightAdapter != null) {
			rightAdapter.notifyDataSetChanged();
		}
	}

	private void loadLeftDataSet(List<DataEntity<T, V>> list) {
		if (list != null) {
			List<T> dataSet = new ArrayList<T>();
			for (int i = 0; i < list.size(); i++) {
				DataEntity<T, V> entity = list.get(i);
				dataSet.add(entity.left);
			}
			leftDataSet = dataSet;
		}
	}

	private void loadSelectedRightDataSet(List<DataEntity<T, V>> list, T index) {
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				DataEntity<T, V> entity = list.get(i);
				if (entity.left.hashCode() == index.hashCode()) {
					setectedRightDataSet = entity.right;
				}
			}
		}
	}

	@SuppressLint("NewApi") 
	private void init(Context context) {
		leftView = new ListView(context);
		leftView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		leftView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
		
		rightView = new ListView(context);
		rightView.setBackgroundColor(righViewBG);
		rightView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		rightView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
		rightView.setVisibility(GONE);
		initData();
		addView(leftView);
		addView(rightView);
	}

	private void initData() {
		if (leftViewResId == 0 || rightViewResId == 0) {
			Log.e(TAG, "not res layout found!!, please add res layout for left and right view");
			return;
		}
		
		//initialize left ListView
		loadLeftDataSet(dataSet);
		loadData();
		leftAdapter = new CommonAdapter<T>(getContext(), leftDataSet, leftViewResId) {

			@Override
			public void convert(ViewHolder holder, T item) {
				//保证list item的宽度与list view的宽度一致
				View view = holder.getConvertView();
				LayoutParams v = view.getLayoutParams(); 
				v.width = leftView.getWidth();
				view.setLayoutParams(v);
				if (hierachicalInit != null) {
					hierachicalInit.initLeftView(holder, item);
				}
				if(leftView.isItemChecked(holder.getPosition())){
					holder.getConvertView().setBackgroundColor(selectedBGColor);
					if (itemCheckListener != null) {
						itemCheckListener.leftItemChecked(holder);
					}
				}else{
					holder.getConvertView().setBackgroundColor(getResources().getColor(android.R.color.transparent));
					if (itemCheckListener != null) {
						itemCheckListener.leftItemUnChecked(holder);
					}
				}
			}
		};
		leftView.setAdapter(leftAdapter);
		//initialize left checked item
		if (curLeftSelectedPosi != INVALID_POSITION) {
			leftView.setItemChecked(curLeftSelectedPosi, true);
		}
		leftView.setOnItemClickListener(defaultLeftItemClickListerner);
		
		
		//initialize right ListVew
		if (curLeftSelectedPosi != INVALID_POSITION) {
			final T key = leftDataSet.get(curLeftSelectedPosi);
			loadSelectedRightDataSet(dataSet, key);
		}
		rightAdapter = new CommonAdapter<V>(getContext(), setectedRightDataSet, rightViewResId) {

			@Override
			public void convert(ViewHolder holder, V item) {
				//保证list item的宽度与list view的宽度一致
				View view = holder.getConvertView();
				LayoutParams v = view.getLayoutParams(); 
				v.width = rightView.getWidth();
				view.setLayoutParams(v);
				if (hierachicalInit != null) {
					hierachicalInit.initRightView(holder, item);
				}
				
				if(rightView.isItemChecked(holder.getPosition())){
					holder.getConvertView().setBackgroundColor(selectedBGColor);
					if (itemCheckListener != null) {
						itemCheckListener.rightItemChecked(holder);
					}
				}else{
					holder.getConvertView().setBackgroundColor(getResources().getColor(android.R.color.transparent));
					if (itemCheckListener != null) {
						itemCheckListener.rightItemUnChecked(holder);
					}
				}
			}
		};
		rightView.setAdapter(rightAdapter);
		//initialize right checked item
		if (curRightSelectedPosi != INVALID_POSITION) {
			rightView.setVisibility(VISIBLE);
			rightView.setItemChecked(curRightSelectedPosi, true);
		}
		rightView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent,
					View view, int position, long id) {
				if (curRightSelectedPosi == position) {
					return;
				}
				curRightSelectedPosi = position;
				rightAdapter.notifyDataSetChanged();
				saveData(curLeftSelectedPosi, position);
				initData();
				if (rightItemClickListener != null) {
					rightItemClickListener.onItemClick(parent, view, position, id);
				}
			}
		});
	}
	/**
	 * 默认点击事件，点击左视图后经过设置右视图的数据资源，刷新适配器
	 */
	private OnItemClickListener defaultLeftItemClickListerner = new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			if (leftDataSet == null) {
				return;
			}
			final T key = leftDataSet.get(position);
			if (dataSet != null && dataSet.size() > 0) {
				//设置右视图所要显示的数据
				if (curLeftSelectedPosi == position) {
					return;
				}
				curLeftSelectedPosi = position;
				if (rightView.getVisibility() == GONE) {
					rightView.setVisibility(VISIBLE);
				}
				//平滑滚动到顶端
				rightView.smoothScrollToPosition(0);
				loadSelectedRightDataSet(dataSet, key);
				leftAdapter.notifyDataSetChanged();
				rightAdapter.setData(setectedRightDataSet);
				curRightSelectedPosi = INVALID_POSITION;
				//重置选中状态
				if (position == lastLeftSelectedPosi) {
					resetRightCheckPosi(lastRightSelectedPosi);
				}else{
					resetRightCheckPosi(INVALID_POSITION);
				}
				rightAdapter.notifyDataSetChanged();
			} else {
				saveData(position, INVALID_POSITION);
				initData();
				//设置左视图点击事件（右视图没有对应值得情况）
				if (leftItemClickListener != null) {
					leftItemClickListener.onItemClick(parent, view, position, id);
				}
			}
		}

		//reset right view item state when left item position change 
		private void resetRightCheckPosi(int invalidPosition) {
			for (int i = 0; i < rightView.getCount(); i++) {
				if (invalidPosition == i) {
					rightView.setItemChecked(invalidPosition, true);
				}else{
					rightView.setItemChecked(i, false);
				}
			}
		}
	};
	
	//保存当前数据
	private void saveData(int leftPosi, int rightPosi) {
		if (persistence != null) {
			persistence.saveLeftSelectedPosition(leftPosi);
			persistence.saveRightSelectedPosition(rightPosi);
		}else{
			SharedPreferences sp = getContext().getSharedPreferences(HierachicalView.class.getSimpleName(), Context.MODE_PRIVATE);
			Editor edit = sp.edit();
			edit.putInt("left_selected_posi", leftPosi);
			edit.putInt("right_selected_posi", rightPosi);
			boolean commit = edit.commit();
			if (commit) {
				Log.i(TAG, "save data successful!");
			}else{
				Log.e(TAG, "save data fail!");
			}
		}
	}
	
	//加载上次记录数据
	private void loadData(){
		Log.i(TAG, "loadData()");
		if (persistence != null) {
			lastLeftSelectedPosi = curLeftSelectedPosi = persistence.loadLeftSelectedPosition();
			lastRightSelectedPosi = curRightSelectedPosi = persistence.loadLeftSelectedPosition();
		}else{
			SharedPreferences sp = getContext().getSharedPreferences(HierachicalView.class.getSimpleName(), Context.MODE_PRIVATE);
			lastLeftSelectedPosi = curLeftSelectedPosi = sp.getInt("left_selected_posi", INVALID_POSITION);
			lastRightSelectedPosi = curRightSelectedPosi = sp.getInt("right_selected_posi", INVALID_POSITION);
		}
	}

	@SuppressWarnings("hiding")
	private abstract class CommonAdapter<V> extends BaseAdapter {
		List<V> data;
		int layoutId;
		Context context;

		public CommonAdapter(Context context, List<V> data, int layoutId) {
			super();
			this.context = context;
			this.data = data;
			this.layoutId = layoutId;
		}
		
		public void setData(List<V> list){
			this.data = list;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = getViewHolder(position, convertView, parent);
			convert(holder, getItem(position));
			return holder.getConvertView();
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public V getItem(int position) {
			return data.get(position);
		}

		@Override
		public int getCount() {
			return data != null ? data.size() : 0;
		}

		public abstract void convert(ViewHolder holder, V item);

		protected ViewHolder getViewHolder(int position, View convertView, ViewGroup parent) {
			return ViewHolder.getViewHolder(context, position, convertView, parent, layoutId);
		}
	};
}
