package com.jcw.fileList;

/**
 * Created by Jackson on 7/3/2015.
 *
 * This is an interface for the classes to communicate when they finish loading.
 */
public interface OnLoadCompleted <T> {
	public void onCompleted(T item);
}
