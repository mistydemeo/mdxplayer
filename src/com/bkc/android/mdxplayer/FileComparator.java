package com.bkc.android.mdxplayer;

import java.io.File;
import java.util.Comparator;

public class FileComparator implements Comparator<Object> {

	@Override
	public int compare(Object arg0, Object arg1) {
		File f0 = (File)arg0;
		File f1 = (File)arg1;
		
		return f0.getName().compareTo(f1.getName());
	}

}
