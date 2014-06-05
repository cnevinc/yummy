package com.openfooddata.app.control;

import java.util.ArrayList; 
import java.util.Locale;

import com.google.android.gms.plus.PlusClient;
import com.openfooddata.app.model.DaoMaster;
import com.openfooddata.app.model.User;
import com.openfooddata.volley.util.MomentUtil;

import com.openfooddata.app.model.*;
import com.openfooddata.app.model.DaoMaster.DevOpenHelper;
import com.openfooddata.volley.util.MyVolley;

import co.oepeneats.app.R;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class Act_Main extends FragmentActivity implements ActionBar.TabListener {

	// NavDrawer ---- start----
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;

	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private String[] mDrawerItem;	//	private ArrayList<DrawerItem> mDrawerItem;
	private ArrayAdapter<String> mArrayAdapter;
	
	// NavDrawer --- end ----

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_main);
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						// Log.d(TAG,"----onPageSelected----"+position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		// NavDrawer --- start--
		mTitle = mDrawerTitle = getTitle();

		setupDrawer();
		

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				setupDrawer();
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				setupDrawer();
				invalidateOptionsMenu(); // creates call to
											// onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
//			selectItem(0);
//			this.mDrawerToggle.onDrawerOpened(mDrawerLayout);
		}
		
		// NavDrawer --- end ---

	}

	private void setupDrawer() {
		// Un Comment below to enable anonymous usage
		// if (Act_SignIn.getCurrentPerson() == null) {
		// mDrawerItem = getResources().getStringArray(
		// R.array.action_array_b4login);
		// } else {
		// mDrawerItem = getResources().getStringArray(
		// R.array.action_array_afterlogin);
		// mDrawerItem[0] = Act_SignIn.getCurrentPerson();
		// }
		mDrawerItem = getResources().getStringArray(
				R.array.action_array_afterlogin);
		mDrawerItem[0] = User.CURRENT_USER_NAME;
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer
		// opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mArrayAdapter = new ArrayAdapter<String>(this,
				R.layout.lv_drawer_list, mDrawerItem);
		mDrawerList.setAdapter(mArrayAdapter);
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
	}

	/* Customized NavDrawer with icon----start----
	private class DrawerItem{
		String mItemName; //int mImageResource;
	}
	private class DrawerAdapter extends ArrayAdapter<DrawerItem>{
		List<DrawerItem> mObjects ;
		public int getCount() {
			if (mObjects == null) {
				return 0;
			}
			return mObjects.size();
		}

		public DrawerItem getItem(int position) {
			if (mObjects == null) {
				return null; 
			}
			return mObjects.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
		
		public DrawerAdapter(Context context, int resource, List<DrawerItem> objects) {
			super(context, resource, objects);
			mObjects = objects;
		}
		 @Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.lv_drawer_list, null);
            EditText et = (EditText)v.findViewById(android.R.id.text1);
            et.setText(this.mObjects.get(position).mItemName);
			return v;
		}
		
	} */
	
	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content
		// view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		// menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}

		// Handle action buttons
		switch (item.getItemId()) {
		// case R.id.action_websearch:
		// // create intent to perform web search for this planet
		// Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
		// intent.putExtra(SearchManager.QUERY, getActionBar().getTitle());
		// // catch event that there's no activity to handle intent
		// if (intent.resolveActivity(getPackageManager()) != null) {
		// startActivity(intent);
		// } else {
		// Toast.makeText(this, R.string.app_not_available,
		// Toast.LENGTH_LONG).show();
		// }
		// return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* The click listner for ListView in the navigation drawer */
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectItem(position);
		} 
	}

	private void selectItem(int position) {
		Intent intent;
			switch (position) {
			case 0:
				// if user is not logged in
				intent = new Intent(this, Act_SignIn.class);
				this.startActivity(intent);
				break;
			case 1:
				// My favorite
				intent = new Intent(this, Act_Product.class);
				intent.putExtra("favorite", true);
				this.startActivity(intent);
				break;
			case 2:
				// Sponsored project
				break;
			case 3:
				//TBD
				
				break;
			case 4:
				// Safety 
				mViewPager.setCurrentItem(1);
				break;
			case 5:
				goScan();
				break;
		}
		// update the main content by replacing fragments
		// Fragment fragment = new PlanetFragment();
		// Bundle args = new Bundle();
		// args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
		// fragment.setArguments(args);
		//
		// FragmentManager fragmentManager = getFragmentManager();
		// fragmentManager.beginTransaction().replace(R.id.content_frame,
		// fragment).commit();

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mDrawerItem[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
		mArrayAdapter.notifyDataSetChanged();
	}

	// NavDrawer -----ened ---

	@Override
	public void onResume() {
		super.onResume();
//		Log.d("nevin", "calling on resume");
		setupDrawer();
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public ArrayList<Fragment> fragments;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			fragments = new ArrayList<Fragment>();
			fragments.add(new Frg_Product_List());
			fragments.add(new Frg_Project_List());
			for (Fragment f : fragments);
//				mOnClearFilterListeners.add(f);
		} 

		@Override
		public Fragment getItem(int position) {
			switch (position){
			case 0:
				Bundle bundle= new Bundle();
				bundle.putBoolean("tested", true);
				fragments.get(position).setArguments(bundle);
				return fragments.get(position);
			case 1:
				return fragments.get(position);
				
			}
			return null;
					
			
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return "檢驗報告";// getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return "焦點抽查";// getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	// -------------Scanner part start--------------
	private static final String SCANNER_APP_PACKAGE_NAME = "com.google.zxing.client.android";

	private static final String TAG = "nevin";

	private void installZXScanner() {
		Intent goToMarket = new Intent(Intent.ACTION_VIEW).setData(Uri
				.parse("market://details?id=" + SCANNER_APP_PACKAGE_NAME));
		startActivity(goToMarket);

	}

	private boolean appInstalledOrNot(String uri) {
		PackageManager pm = this.getPackageManager();
		boolean app_installed = false;
		try {
			pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
			app_installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			app_installed = false;
		}
		return app_installed;
	}

	private void showAlertDialog() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set title
		alertDialogBuilder.setTitle("Scanner APP not found");

		// set dialog message
		alertDialogBuilder
				.setMessage("You must install Barcode scanner APP first. Now?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// if this button is clicked, close current
								// activity
								installZXScanner();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// if this button is clicked, just close ,the dialog box
						// and do nothing
						dialog.cancel();
					}
				});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();
	}

	private static final int REQUEST_BARCODE = 0;

	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_BARCODE) {
			if (resultCode == RESULT_OK) {
				String barcode = intent.getStringExtra("SCAN_RESULT");
				Toast.makeText(this, "BARCODE: " + barcode + "\n",
						Toast.LENGTH_LONG).show();
				mViewPager.setCurrentItem(0); // Move to productFragment
			}
		}
	}

	private void goScan() {
		boolean installed = appInstalledOrNot(SCANNER_APP_PACKAGE_NAME);
		if (installed) {
			// This intent will help you to launch if the package is already
			// installed
			Log.v(TAG, "App already installed om your phone");
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
			startActivityForResult(intent, 0); // 0 is the request code
		} else {
			// Launch barcode scanner.
			Log.v(TAG, "App is not installed om your phone");
			showAlertDialog();
		}
	}

	// -------------Scanner part end-------------
	

}
