package com.openfooddata.app.control;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.util.JsonArrayRequest;
import com.android.volley.util.JsonObjectRequest;
import com.android.volley.util.StringRequest;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.plus.PlusClient;

import co.oepeneats.app.R;
import com.openfooddata.app.model.*;
import com.openfooddata.app.model.DaoMaster.DevOpenHelper;
import com.openfooddata.volley.util.MomentUtil;
import com.openfooddata.volley.util.MyVolley;
import com.openfooddata.volley.util.SyncManager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

// THis activity is the entrance point to the APP
// It quickly load Core and see its setup,
//   start the correspond Activity as a decision maker 
//		then pass the core to the next Activity
public class Act_Splash extends Activity  {

	String TAG = "nevin";


	SyncManager mSyncManager;
	

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_splash);

		final ActionBar actionBar = getActionBar();
		actionBar.hide();
		
		mSyncManager = SyncManager.getInstance(this);


		// If no user is in database, means first time execution. Add Short Cut to Desktop
		if (mSyncManager.isFirstUsed())
			addShortcutToDesktop();

		// Disable below two lines to enable social login at the splash page
		syncWithServer(); 
	}


	private void syncWithServer() {
		// enable below line for debugging purpose
		this.mSyncManager.clearAll();

		// sync with server, the longest job should implement the close/start activity job
		this.mSyncManager.syncCompany();
		this.mSyncManager.syncDonation();
		this.mSyncManager.syncNews();
		this.mSyncManager.syncTestProject();
		this.mSyncManager.syncUser();
		this.mSyncManager.syncProduct();		// currently it's here
	}
	
	private void addShortcutToDesktop() {
		Intent shortcutIntent = new Intent(getApplicationContext(),
				Act_Splash.class);

		shortcutIntent.setAction(Intent.ACTION_MAIN);

		Intent addIntent = new Intent();
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, this.getResources()
				.getString(R.string.app_name));
		addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
				Intent.ShortcutIconResource.fromContext(
						getApplicationContext(), R.drawable.ic_launcher));
		addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
		getApplicationContext().sendBroadcast(addIntent);

	}

}