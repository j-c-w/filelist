package com.jcw.fileList;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by Jackson on 7/4/2015.
 *
 * Since this is the primary use of the FileList setup,
 * I have designated it as its own class to make use of
 * it less verbose (i.e. no FileList<Bitmap>)
 */
public class BitmapList extends FileList<Bitmap> {
	public BitmapList(File storageDir) {
		super(storageDir);
	}
}
