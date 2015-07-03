package com.jcw.fileList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jackson on 7/3/2015.
 */
public class FileList <T> {
	List<FileListItem<T>> fileItems;

	int current;

	T prev;
	T next;
	T curr;

	public FileList() {
		fileItems = new ArrayList<FileListItem<T>>();

		current = -1;
	}

	public void setCurrent(int number) {
		if (number < 0)
		current = number;
	}

	/*
	 * This should be used as a
	 */
	public void get(OnLoadCompleted<T> onLoadCompleted) {
		if (current == -1) {
			throw new Error("current points to before the first item. Nothing to get from list" +
					". Try adding");
		}


	}
}
