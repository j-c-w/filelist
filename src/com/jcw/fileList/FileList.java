package com.jcw.fileList;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Jackson on 7/3/2015.
 */
public class FileList <T> {
	List<FileListItem<T>> fileItems;
	File storageDir;

	int current;

	/*
	 * This means that only 3 items are kept in memory at any one time.
	 */
	int bufferSize = 3;

	public FileList(File storageDir) {
		this.fileItems = new ArrayList<FileListItem<T>>();
		this.storageDir = storageDir;
		current = -1;

		if (!storageDir.exists()) {
			if (!storageDir.mkdirs()) {
				throw new IOError(
						new IOException("Storage folder " + storageDir + " did not exist and could not be created."));
			}
		}
	}

	public int size() {
		return fileItems.size();
	}

	public void setCurrent(int number) {
		if (number < 0)
			throw new IllegalArgumentException("Can't set current oa number < 0");

		current = number;

		recycleOld();
		loadBuffer();
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

	/*
	 * This replaces the current value with the value provided
	 */
	public void update(T newItem) {
		fileItems.get(current).set(newItem);
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
		if (isOnLast()) {
			throw new Error("Already at last item in list");
		} else {
			current ++;
		}

		recycleOld();
		loadBuffer();
	}

	public void prev() {
		if (isOnFirst()) {
			throw new Error("Already at first item");
		} else {
			current --;
		}

		recycleOld();
		loadBuffer();
	}

	public void remove(int index) {
		fileItems.remove(index);

		if (current <= index) {
			current -= 1;
		}

		// This is to deal with the situations where current is pointing to the first element,
		// and pointing to the element before the first one is not a good idea.
		if (size() == 0) {
			current = -1;
		} else if (current < 0) {
			current = 0;
		}

		recycleOld();
		loadBuffer();
	}

	public void removeCurrent() {
		remove(current);
	}

	public boolean isOnFirst() {
		return current <= 0;
	}

	public boolean isOnLast() {
		return current == fileItems.size() - 1;
	}

	public boolean isEmpty() {
		return current == -1;
	}

	public int getCurrent() {
		return current;
	}

	/*
	 * This recycles all the elements that are loaded in the list,
	 * ensuring that all the files on the disk are up to date.
	 *
	 * This should not be called if you intend to load items from the
	 * list further, as it will flush everything out of memory, so
	 * it will all have to be reloaded again.
	 */
	public void recycleAll() {
		for (int i = 0; i < fileItems.size(); i ++) {
			recycle(i);
		}
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

	public File[] getAllFiles() {
		File[] files = new File[fileItems.size()];

		for (int i = 0; i < files.length; i ++) {
			files[i] = fileItems.get(i).getSaveFile();
		}

		return files;
	}

	/*
	 * Adds an item at the current position
	 */
	public void insertBeforeCurrent(FileListItem<T> item) {
		add(current, item);
	}

	/*
	 * Adds an item after the current item
	 */
	public void insertAfterCurrent(FileListItem<T> item) {
		if (current == size() - 1) {
			// We are at the end of the list, so add normally
			add(item);
		} else {
			// Not at the end of the list,
			add(current + 1, item);
		}
	}

	/*
	 * Adds an item to the end of the list.
	 */
	public void add(FileListItem<T> item) {
		item.setSaveFile(getNewSaveFile());

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
		item.setSaveFile(getNewSaveFile());

		fileItems.add(location, item);

		recycleOld();
		loadBuffer();
	}

	/*
	 * This method cleans out the storage directory used by deleting all the files
	 * that were associated with this list.
	 *
	 * NOTE -- DON'T CALL THIS IF YOU PLAN TO CONTINUE TO USE THE LIST
	 *
	 * A large portion of the data stored in the list will be lost
	 * if you try that
	 */
	public void clear() {
		for (FileListItem<T> item : fileItems) {
			item.getSaveFile().delete();
		}
	}

	private File getNewSaveFile() {
		File saveFile = new File(storageDir + "/" + getFileName());
		try {
			saveFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return saveFile;
	}

	private static String getFileName() {
		return UUID.randomUUID().toString();
	}
}
