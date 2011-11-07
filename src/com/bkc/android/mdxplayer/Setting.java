package com.bkc.android.mdxplayer;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Setting extends PreferenceActivity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}

	/*
	@Override
	public boolean onPreferenceChange(Preference arg0, Object arg1) {
		// TODO Auto-generated method stub
		return true;
	}*/

}
