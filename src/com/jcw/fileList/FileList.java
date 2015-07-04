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

	/*
	 * This means that only 3 items are kept in memory at any one time.
	 */
	int bufferSize = 3;

	public FileList() {
		fileItems = new ArrayList<FileListItem<T>>();

		current = -1;
	}

	public void setCurrent(int number) {
		if (number < 0)
		current = number;
	}

	/*
	 * Since the loading of list items is done asynchronously,
	 * it can't be guaranteed the item is already loaded. Thus, the
	 * listener is fired when the item finishes loading.
	 */
	public void get(OnLoadCompleted<T> onLoadCompleted) {
		if (current == -1) {
			throw new Error("current points to before the first item. Nothing to get from list" +
					". Try adding");
		}

		fileItems.get(current).get(onLoadCompleted);
	}

	public void setBufferSize(int size) {
		this.bufferSize = size;
	}

	protected void buffer(int index) {
		fileItems.get(index).preload();
	}

	protected void recycle(int index) {
		if (!fileItems.get(index).isRecycled()) {
			fileItems.get(index).recycle();
		}
	}

	public void next() {
		if (current < (fileItems.size() - 1)) {
			current ++;
		} else {
			throw new Error("Already at last item in list");
		}

		recycleOld();
		loadBuffer();
	}

	public void prev() {
		if (current <= 0) {
			throw new Error("Already at first item");
		} else {
			current --;
		}

		recycleOld();
		loadBuffer();
	}

	/*
	 * This goes through and recycles all the old items in the list.
	 * (i.e. the ones not covered by the buffer.
	 */
	public void recycleOld() {
		for (int i = 0; i < current - (bufferSize / 2); i ++) {
			recycle(i);
		}

		for (int i = current + 1 + (bufferSize / 2); i < fileItems.size(); i ++) {
			recycle(i);
		}
	}

	public void loadBuffer() {
		for (int i = (current - (bufferSize / 2));
				i < (current + 1 + (bufferSize / 2)); i ++) {
			// obviously can't load anything that isn't in the range
			// of the list.
			if (i >= 0 && i < fileItems.size()) {
				buffer(i);
			}
		}
	}

	/*
	 * Adds an item to the end of the list.
	 */
	public void add(FileListItem<T> item) {
		fileItems.add(item);

		if (current == -1) {
			// this is the only item in the list, so
			// update current to point to 0
			next();
		}

		if (fileItems.size() > current + (bufferSize / 2)) {
			// if the item is out of the range of the buffer, then we
			// need to recycle it.
			item.recycle();
		}
	}

	public void add(int location, FileListItem<T> item) {
		fileItems.add(location, item);

		recycleOld();
		loadBuffer();
	}
}
