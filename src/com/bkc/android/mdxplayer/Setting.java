package com.bkc.android.mdxplayer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Setting extends PreferenceActivity  {

	public static final String PCMPATH  = "PCMPath";   
	public static final String LASTFILE = "lastFile";
	public static final String LASTPATH = "lastPath";
	public static final String VOLUME = "volume";

	public static final String APPVER = "app_version";
	public static final String LOGDATE = "log_date";
	public static final String PAUSE_DIAG = "pause_diag";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}

	/*
	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		return true;
	}*/

}
