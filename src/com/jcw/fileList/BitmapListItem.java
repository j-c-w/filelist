package com.jcw.fileList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Jackson on 7/4/2015.
 *
 * This is an implementation that deals with bitmaps in particular.
 *
 * It is what I designed this list for, and probably the most common
 * use case for something like this.
 */
public class BitmapListItem extends FileListItem<Bitmap> {
	public BitmapListItem(Bitmap item) {
		super(item);
	}

	@Override
	public void write(Bitmap item, File destination) {
		if (item == null)
			return;

		try {
			FileOutputStream fOut = new FileOutputStream(destination);

			item.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Bitmap get(File from) {
		return BitmapFactory.decodeFile(from.toString());
	}
}
