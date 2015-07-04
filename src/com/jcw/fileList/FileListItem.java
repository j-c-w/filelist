package com.jcw.fileList;

import java.io.File;

/**
 * Created by Jackson on 7/3/2015.
 */
public abstract class FileListItem <T> {
	public boolean updating = false;
	public boolean loaded = true;
	public boolean needsUpdating = false;

	T item;
	File savePoint;

	public abstract void write(T item, File destination);
	public abstract T get(File from);


	public FileListItem(T item) {
		set(item);
	}

	synchronized public void load(final File from, final OnLoadCompleted<T> callback) {
		Thread loader = new Thread(new Runnable() {
			@Override
			public void run() {
				if (item == null) {
					item = get(from);
				}

				loaded = true;
				if (callback != null) {
					callback.onCompleted(item);
				}
			}
		});

		loader.run();
	}

	synchronized public void save(final T item, final File destination) {
		updating = true;
		Thread saver = new Thread(new Runnable() {
			@Override
			public void run() {
				write(item, destination);
				updating = false;
			}
		});

		saver.run();
	}

	public void set(T item) {
		this.item = item;

		needsUpdating = true;
	}

	/*
	 * This directly returns the item.
	 *
	 * However, this will only return the item if it is already loaded.
	 * Use isLoaded() to check this.
	 *
	 * Will return null otherwise
	 */
	public T getOrNull() {
		if (loaded) {
			return item;
		} else {
			return null;
		}
	}

	public boolean isLoaded() {
		return loaded;
	}

	public boolean isRecycled() {
		return !isLoaded();
	}

	/*
	 * This returns the requested item in the callback
	 */
	public void get(OnLoadCompleted<T> callback) {
		load(savePoint, callback);
	}

	/*
	 * This loads in the item from a file for later use.
	 */
	public void preload() {
		load(savePoint, null);
	}

	/*
	 * This should only be used by the file list which
	 * allocates a file for this object to be saved in.
	 */
	protected void setSaveFile(File saveFile) {
		this.savePoint = saveFile;
	}

	protected File getSaveFile() {
		return savePoint;
	}

	/*
	 * This takes T out of memory by writing it to the
	 * disk.
	 */
	public void recycle() {
		if (needsUpdating) {
			save(item, savePoint);
		}

		item = null;
		loaded = false;
	}
}