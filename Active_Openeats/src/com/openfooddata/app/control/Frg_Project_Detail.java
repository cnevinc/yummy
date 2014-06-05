/**
public String getmImgurl() {
		return mImgurl;
	}

	public void setmImgurl(String mImgurl) {
		this.mImgurl = mImgurl;
	}

	public boolean ismCname() {
		return mCname;
	}

	public void setmCname(boolean mCname) {
		this.mCname = mCname;
	}

	public long getmDesc() {
		return mDesc;
	}

	public void setmDesc(long mDesc) {
		this.mDesc = mDesc;
	}

	public boolean ismPrice() {
		return mPrice;
	}

	public void setmPrice(boolean mPrice) {
		this.mPrice = mPrice;
	} * Copyright 2013 Ognyan Bankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.openfooddata.app.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import co.oepeneats.app.R;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.util.StringRequest;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.openfooddata.app.adapter.ProjectDetailArrayAdapter;
import com.openfooddata.app.model.CompanyDao;
import com.openfooddata.app.model.DaoMaster;
import com.openfooddata.app.model.DaoSession;
import com.openfooddata.app.model.NewsDao;
import com.openfooddata.app.model.ProductDao;
import com.openfooddata.app.model.TestProject;
import com.openfooddata.app.model.TestProjectDao;
import com.openfooddata.app.model.User;
import com.openfooddata.app.model.UserDao;
import com.openfooddata.app.model.DaoMaster.DevOpenHelper;
import com.openfooddata.iab.util.IabHelper;
import com.openfooddata.iab.util.IabResult;
import com.openfooddata.iab.util.Inventory;
import com.openfooddata.iab.util.Purchase;
import com.openfooddata.volley.util.MyVolley;
import com.openfooddata.volley.util.SyncManager;

import de.greenrobot.dao.Property;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;


public class Frg_Project_Detail extends Fragment  {

	protected static final String TAG = "nevin";

	private ShareActionProvider mShareActionProvider;
	
	// IAB ---- add fields start----
	static final String SKU_PREMIUM = "premium";
    static final String SKU_DONATE_1 = "donate_1";
    static final int RC_REQUEST = 10001;		// (arbitrary) request code for the purchase flow
    
    boolean mIsPremium = false;					// Does the user have the premium upgrade?
    IabHelper mHelper;							// The helper object
 
    // Private menber, Listener that's called when we finish querying the items and subscriptions we own
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            
        	Log.d(TAG, "Query inventory finished.");

            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;

            // Is it a failure?
            if (result.isFailure()) {
                complain("Failed to query inventory: " + result);
                return;
            }

            Log.d(TAG, "Query inventory was successful.");

            /*
             * Check for items we own. Notice that for each purchase, we check  
             * the developer payload to see if it's correct! See verifyDeveloperPayload().
             */

            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null && verifyDeveloperPayload(premiumPurchase));
            Log.d(TAG, "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));


            // Check for donate_1 delivery -- if we own donate_1, we should consume it immediately and log it in local SQLite
            Purchase donate_1Purchase = inventory.getPurchase(SKU_DONATE_1);
            if (donate_1Purchase != null && verifyDeveloperPayload(donate_1Purchase)) {
                Log.d(TAG, "We have donate_1. Consuming it.");
                mHelper.consumeAsync(inventory.getPurchase(SKU_DONATE_1), mConsumeFinishedListener);
                return;
            }

            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "Initial inventory query finished; enabling main UI.");
        }
    };
	// IAB ----add fields end-----
    
	// Private member for UI
	private ListView mlvTestProject;
	private ArrayList<TestProject> mEntries = new ArrayList<TestProject>();
	private ProjectDetailArrayAdapter mAdapter;

	// Private member for DB operation
	private SQLiteDatabase db;
	private DaoMaster mDaoMaster;
	private DaoSession mDaoSession;
	private TestProjectDao mTestProjectsDAO;
	
	
	private View mRootView;

	private TestProject mTestProject;
 
	public static String SORT_BY = "sortby";
	public static int SORT_BY_HOT = 0 ; 
	public static int SORT_BY_RECOMMEND = 1 ;
	

	 // share ---start----
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.menu_share, menu); 
	    MenuItem item = menu.findItem(R.id.menu_item_share);
	    mShareActionProvider = (ShareActionProvider) item.getActionProvider();
	    Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "我正在追蹤["+this.mTestProject.getProduct().getName()+"]\n一起來追蹤吧! https://play.google.com/store/apps/details?id=com.icareyou.food");
        shareIntent.setType("text/plain");
        if (mShareActionProvider != null) 
        	mShareActionProvider.setShareIntent(shareIntent);
	    super.onCreateOptionsMenu(menu,inflater);
	} 
	// share ----end-----
	
	@Override
	public void onStop(){
		this.db.close();
		super.onStop();
	}
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
		
		// IAB----start----
		String base64EncodedPublicKey = 
				"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqUzXdwi1/8uKoM1jonmfIPCbQAyHS9V9b2fxsbn1b15KCCsaO6IG59nriNAQZ57sN+xZmyK7oU7ez8gJgi3kDFAHHAhTqdwcE9X0Zw+2zsFXN0XFlTm5yua1ZjIJBfGe2J5oL2y5YZ1ksmbx74f3l4ajYoRXGS8PIo5ttimpJKhcmw3H4GPf5Udu6HG3ZrcC8m9wgi4sogL7JmB1jTO2Sobl6bPTmVzRR8SFeWv7gXbG1V1ZLj1YDhaCWg/vLpX+W6r+Y+3tNrAgXS+MAZTu8uDpdH+tktLYuHdqPMwYZ8wtZFT84WruPvZM2+whRDmmlovlr6osBz9lpD6qCyyZBQIDAQAB";	
        
		// Create the helper, passing it our context and the public key to verify signatures with
        Log.d(TAG, "Creating IAB helper.");
        mHelper = new IabHelper(this.getActivity(), base64EncodedPublicKey);

        // enable debug logging (for a production application, you should set this to false).
        mHelper.enableDebugLogging(true);

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting setup.");
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "Setup finished.");

                if (!result.isSuccess()) {
                    // Oh noes, there was a problem.
                    complain("Problem setting up in-app billing: " + result);
                    return;
                }

                // Have we been disposed of in the meantime? If so, quit.
                if (mHelper == null) return;
                
                // IAB is fully set up. Now, let's get an inventory of stuff we own.
                Log.d(TAG, "Setup successful. Querying inventory.");
                mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
		// IAB-----end-----
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.frg_project_view,
				container, false);
		mRootView = rootView;
		rootView.findViewById(R.id.layout_donate).setVisibility(View.VISIBLE);
		Button b = (Button)rootView.findViewById(R.id.bt_donate);
		b.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				setWaitScreen(true);
		        Log.d(TAG, "Launching purchase flow for donate_1.");

		        /* TODO: for security, generate your payload here for verification. See the comments on
		         *        verifyDeveloperPayload() for more info. Since this is a SAMPLE, we just use
		         *        an empty string, but on a production app you should carefully generate this. */
		        String payload = "";

		        mHelper.launchPurchaseFlow(Frg_Project_Detail.this.getActivity(), SKU_DONATE_1, RC_REQUEST,
		                mPurchaseFinishedListener, payload);
		        if (Frg_Project_Detail.this.mEntries.size()==1){
		        	makeDonation(mEntries.get(0).getId());
		        }
//		        setWaitScreen(false);
				
			}});
		// Setup DB operation
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this.getActivity(),
				"foodabc-db", null);
		db = helper.getWritableDatabase();
		mDaoMaster = new DaoMaster(db);
		mDaoSession = mDaoMaster.newSession();
		mTestProjectsDAO = mDaoSession.getTestProjectDao();
		mEntries = (ArrayList<TestProject>) mTestProjectsDAO.queryBuilder().list();
		mlvTestProject = (ListView) rootView.findViewById(R.id.lv_project);
		
		int pid = 0;
		if (this.getArguments() != null)
			pid = this.getArguments().getInt("project_id");
		this.mTestProject = mEntries.get(pid);
		mAdapter = new ProjectDetailArrayAdapter(this.getActivity(), 0,
				mEntries.subList(pid, pid + 1), // project_id start from 1
				MyVolley.getImageLoader());
		mlvTestProject.setAdapter(mAdapter);
		mlvTestProject.setTextFilterEnabled(true);

		return rootView;
	}
	
	// IAB----start----
	// Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            if (result.isFailure()) {
                complain("Error purchasing: " + result);
                setWaitScreen(false);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
                complain("Error purchasing. Authenticity verification failed.");
                setWaitScreen(false);
                return;
            }

            Log.d(TAG, "Purchase successful.");

            if (purchase.getSku().equals(SKU_DONATE_1)) {
                // bought 1/4 tank of donate_1. So consume it.
                Log.d(TAG, "Purchase is donate_1. Starting donate_1 consumption.");
                mHelper.consumeAsync(purchase, mConsumeFinishedListener);
                // Make a donation on server
                // Update TestPorject table
                
            }
            else if (purchase.getSku().equals(SKU_PREMIUM)) {
                // bought the premium upgrade!
                Log.d(TAG, "Purchase is premium upgrade. Congratulating user.");
                alert("Thank you for upgrading to premium!");
                mIsPremium = true;
                updateUi();
                setWaitScreen(false);
            }
        }
    };
	
    /** Verifies the developer payload of a purchase. */
    boolean verifyDeveloperPayload(Purchase p) {
        String payload = p.getDeveloperPayload();

        /*
         * TODO: verify that the developer payload of the purchase is correct. It will be
         * the same one that you sent when initiating the purchase.
         *
         * WARNING: Locally generating a random string when starting a purchase and
         * verifying it here might seem like a good approach, but this will fail in the
         * case where the user purchases an item on one device and then uses your app on
         * a different device, because on the other device you will not have access to the
         * random string you originally generated.
         *
         * So a good developer payload has these characteristics:
         *
         * 1. If two different users purchase an item, the payload is different between them,
         *    so that one user's purchase can't be replayed to another user.
         *
         * 2. The payload must be such that you can verify it even when the app wasn't the
         *    one who initiated the purchase flow (so that items purchased by the user on
         *    one device work on other devices owned by the user).
         *
         * Using your own server to store and verify developer payloads across app
         * installations is recommended.
         */

        return true;
    }
    
    // Called when consumption is complete
    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);

            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;

            // We know this is the "donate_1" sku because it's the only one we consume,
            // so we don't check which sku was consumed. If you have more than one
            // sku, you probably should check...
            if (result.isSuccess()) {
                // successfully consumed, so we apply the effects of the item in our
                // game world's logic, which in our case means you can donate next time
                Log.d(TAG, "Consumption successful. Provisioning.");
                saveData();
            }
            else {
                complain("Error while consuming: " + result);
            }
            updateUi();
            setWaitScreen(false);
            Log.d(TAG, "End consumption flow.");
        }
    };
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        if (mHelper == null) return;

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
        	Log.d(TAG,"super.onActivityResult(requestCode, resultCode, data);");
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d(TAG, "onActivityResult handled by IABUtil.");
        }
    }
    
    // private helper method
    private static int s =0;
    void saveData() {
    	// save donate to local db and sync to serve
        /*
         * WARNING: on a real application, we recommend you save data in a secure way to
         * prevent tampering. For simplicity in this sample, we simply store the data using a
         * SharedPreferences.
         */
    	s++;
        Log.d(TAG, "Saved data: credit " + s);
    }
    // updates UI to reflect model
    public void updateUi() {
    	if(s>0){
    		TextView et = 	(TextView)mRootView.findViewById(R.id.donate_hint);
    		et.setVisibility(View.VISIBLE);
    		et.setText("您已捐款了"+s+"次");
    	}
    	
    }
    void loadData() {
        SharedPreferences sp = this.getActivity().getPreferences(Activity.MODE_PRIVATE);
//        mTank = sp.getInt("tank", 2);
//        Log.d(TAG, "Loaded data: tank = " + String.valueOf(mTank));
    }
    void setWaitScreen(boolean set) {
//		mRootView.findViewById(R.id.progressBar1).setVisibility(set ? View.VISIBLE : View.GONE);
//		mRootView.findViewById(R.id.lv_project).setVisibility(set ? View.GONE : View.VISIBLE);
//		mRootView.findViewById(R.id.layout_donate).setVisibility(set ? View.GONE : View.VISIBLE);
    }
	void complain(String message) {
        Log.e(TAG, "**** Donate Error: " + message);
        alert("Error: " + message);
    }

    void alert(String message) {
        AlertDialog.Builder bld = new AlertDialog.Builder(this.getActivity());
        bld.setMessage(message);
        bld.setNeutralButton("OK", null);
        Log.d(TAG, "Showing alert dialog: " + message);
        bld.create().show();
    }
    // IAB----end----
    
    private class MyStringRequest extends StringRequest{
    	private long mTestproject_id;
		public MyStringRequest(int method, String url,
				Listener<String> listener, ErrorListener errorListener,long testproject_id) {
			super(method, url, listener, errorListener);
			mTestproject_id = testproject_id;
		}
		@Override
		protected Map<String, String> getParams() {
			// Use Default Account in Android as first app_soical_Id 
			Map<String, String> params = new HashMap<String, String>();
				params.put("user_id", String.valueOf(User.CURRENT_USER_ID));
				params.put("testproject_id", String.valueOf(mTestproject_id));
				params.put("amount", "100");
			return params;
		}
		
    }
    
    private void makeDonation(long testproject_id){
    	// Update User Table
		Log.d(TAG, "Donating Project:" + testproject_id);
		
		RequestQueue queue = MyVolley.getRequestQueue();
		String product_url = "";
		product_url = "http://foodopendata-api.herokuapp.com/api/donation/"+String.valueOf(User.CURRENT_USER_ID);
		StringRequest postRequest = new MyStringRequest(Request.Method.POST,
				product_url, new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						SyncManager syncMan = SyncManager.getInstance(getActivity());
						syncMan.syncDonation();
						syncMan.syncTestProject();
						syncMan.syncProduct();
						Log.d(TAG, "Donating Project completed");
						Toast.makeText(getActivity(), "感謝您的贊助!", Toast.LENGTH_SHORT).show();
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Error.Response:" + error);
					}
				}, testproject_id);
		queue.add(postRequest);
    }
 
}
