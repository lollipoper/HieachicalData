package com.sunfun.datapicker;

import java.util.List;

public class DataEntity<T, V> {
	T left;
	List<V> right;
	public DataEntity(T left, List<V> right) {
		if (left == null) {
			new Throwable("error:the data left of entity can't be null");
		}
		this.left = left;
		this.right = right;
	}
}
