package com.openfooddata.app.control;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

public class Act_Project extends FragmentActivity  {

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	    	// NavUtils.navigateUpFromSameTask(this);
	        this.finish(); // ugly but quick, change to follow dev-guidelin http://developer.android.com/training/implementing-navigation/ancestral.html
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar bar = this.getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
//		setContentView(R.layout.act_project);
		if (savedInstanceState == null) {
            // During initial setup, plug in the details fragment.
			Frg_Project_Detail details = new Frg_Project_Detail();
            details.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add(
                    android.R.id.content, details,"detailFrag").commit();
        }

	}
	
	protected void onActivityResult(int requestCode, int resultCode,Intent data)
	{
	    super.onActivityResult(requestCode, resultCode, data);
//	    Log.d("iabh","onActivityResult from Act_project");
	    FragmentManager fragmentManager = getSupportFragmentManager();
	    Fragment fragment = fragmentManager.findFragmentByTag("detailFrag");
	    if (fragment != null)
	    {
//	    	Log.d("iabh","calling frag onActivityResult");
	        ((Frg_Project_Detail)fragment).onActivityResult(requestCode, resultCode,data);
	    }
	}
	



}
