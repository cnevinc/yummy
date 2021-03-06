package com.cgearc.yummy;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.util.Log;

/* 
 *  
 * Provide interface to default preference 
 * 
 */
public class Setting implements OnSharedPreferenceChangeListener {
  
	public static final String EXTRA_PACKAGE_NAME = "com.chocolabs.applocker.extra.package.name";
	public static final String BlockedPackageName = "locked package name";
	public static final String BlockedActivityName = "locked activity name";
	public static final String ACTION_APPLICATION_PASSED = "com.chocolabs.airlocker.applicationpassedtest";
	public static final int ERROR_SHRESHOLD = 3;
	public static final String SERVICE_MAIL_ACCOUNT = "airlocker@chocolabs.com";
	public static final String SERVICE_MAIL_PASSWORD = "Choco321";
	public static final String ACTION_WEEKLYREPORT_SEND = "ACTION_WEEKLYREPORT_SEND";
	
	public static ArrayList<Article> allArticles;
	
	
	// TODO: Setup below to avoid hard coded package/activity name
//	public static final String DEFAULT_LOCK_SCREEN_ACTIVITY = Act_GestureTest.class.getName();//  Act_GestureTest.class.getName();
//	public static final String DEFAULT_LOCK_SCREEN_ACTIVITY_SIMPLE = Act_GestureTest.class.getSimpleName();//  Act_GestureTest.class.getName();

	public static Setting getInstance(Context context) {
		return mInstance == null ? (mInstance = new Setting(context))
				: mInstance;
	}
	
	public boolean isWeeklyReport() {
		return mWeeklyReport;
	}

	public boolean isAutoStart() {
		return mAutoStart;
	}

	public boolean isServiceEnabled() {
		return mServiceEnabled;
	}

	public boolean isFirstUse() {
		return mFirstUse;
	}

	public void setFirstUse(boolean first) {
		mFirstUse = first;
		mPref.edit().putBoolean(PREF_FIRST_USE, first).commit();
		this.reloadPreferences();
	}
	
	public String[] getApplicationList() {
		return mApplicationList;
	}

	public int getRelockTimeout() {
		return mRelockTimeout;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String password) {
		mPassword = password;
		mPref.edit().putString(PREF_PASSWORD, password).commit();
		this.reloadPreferences();
	}

	public String getGestureSequence() {
		return mGestureSequence;
	}

	public void setGestureSequence(String gesture) {
		Log.d(Setting.TAG,"Saving pass====="+gesture); //LRD
		mGestureSequence = gesture;
		mPref.edit().putString(PREF_GESTURE, gesture).commit();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		reloadPreferences();
	}


	/** Private Members **/
	private static final String PREF_SERVICE_ENABLED = "service_enabled";
	private static final String PREF_APPLICATION_LIST = "application_list";
	private static final String PREF_AUTO_START = "start_service_after_boot";
	private static final String PREF_PASSWORD = "password";
	private static final String PREF_GESTURE = "gesture";
	private static final String PREF_RELOCK_POLICY = "relock_policy";
	private static final String PREF_RELOCK_TIMEOUT = "relock_timeout";
	private static final String PREF_FIRST_USE = "first_use";
	private static final String PREF_WEEKLY_REPORT = "weekly_report";

	public static final String TAG = "nevin";
	public static final String APP_VERSION = "App Version";
	public static final String LOCKED_APP = "Locked App";
	
	private SharedPreferences mPref;
	private static Setting mInstance;
	private boolean mServiceEnabled, mAutoStart,mWeeklyReport ;
	private String[] mApplicationList;
	private String mPassword;
	private int mRelockTimeout;
	private String mGestureSequence;
	private boolean mFirstUse;
	private Context mContext;
	
	
	private Setting(Context context) {
		mPref = PreferenceManager.getDefaultSharedPreferences(context);
		mPref.registerOnSharedPreferenceChangeListener(this);
		reloadPreferences();
		mContext = context;
	}

	private void reloadPreferences() {
		mServiceEnabled = mPref.getBoolean(PREF_SERVICE_ENABLED, false);
		mApplicationList = mPref.getString(PREF_APPLICATION_LIST, "")
				.split(";");
		mAutoStart = mPref.getBoolean(PREF_AUTO_START, false);
		mPassword = mPref.getString(PREF_PASSWORD, "");						// Default password is empty
		mGestureSequence = mPref.getString(PREF_GESTURE, "DOWN DOWN DOWN");	// Default questure is Down*3
		mFirstUse = mPref.getBoolean(PREF_FIRST_USE, true);
		mWeeklyReport = mPref.getBoolean(PREF_WEEKLY_REPORT, true);
		
		if (mPref.getBoolean(PREF_RELOCK_POLICY, true)) {
			try {
				mRelockTimeout = Integer.parseInt(mPref.getString(
						PREF_RELOCK_TIMEOUT, "-1"));
			} catch (Exception e) {
				mRelockTimeout = -1;
			}
		} else {
			mRelockTimeout = -1;
		}
	}
	 
	

}
