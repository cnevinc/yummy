package com.openfooddata.volley.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.util.JsonArrayRequest;
import com.android.volley.util.JsonObjectRequest;
import com.android.volley.util.StringRequest;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.openfooddata.app.control.Act_Main;
import com.openfooddata.app.control.Act_Splash;
import com.openfooddata.app.model.Company;
import com.openfooddata.app.model.CompanyDao;
import com.openfooddata.app.model.DaoMaster;
import com.openfooddata.app.model.DaoSession;
import com.openfooddata.app.model.Donation;
import com.openfooddata.app.model.DonationDao;
import com.openfooddata.app.model.News;
import com.openfooddata.app.model.NewsDao;
import com.openfooddata.app.model.Product;
import com.openfooddata.app.model.ProductDao;
import com.openfooddata.app.model.TestProject;
import com.openfooddata.app.model.TestProjectDao;
import com.openfooddata.app.model.User;
import com.openfooddata.app.model.UserDao;
import com.openfooddata.app.model.DaoMaster.DevOpenHelper;

public class SyncManager {
	private static final String TAG = "nevin";
	private static SyncManager mSyncManager;
	private static final Class<?> NEXT_PAGE = Act_Main.class;
	private Activity mActivity;
	
	// DB operation ----start----
	private SQLiteDatabase mDB;
	private DaoMaster mDaoMaster;
	private DaoSession mDaoSession;
	private ProductDao mProductDao;
	private CompanyDao mCompanyDao;
	private NewsDao mNewsDao;
	private TestProjectDao mTestProjectDao;
	private UserDao mUserDao;
	private DonationDao mDonationDao;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 'T'hh:mm:ss.SSS'Z'"); ex. 2013-11-11T12:21:48.033Z
	private SimpleDateFormat sdf_rest = new SimpleDateFormat("yyyyMMdd"); // example:
	

	public static SyncManager getInstance(Activity activity) {
		if (mSyncManager == null)
			return new SyncManager(activity);
		else
			return mSyncManager;
	}

	public boolean isFirstUsed() {
		// If no user is in database, means first time execution. Add Short Cut
		// to Desktop
		if (mUserDao.loadAll().size() == 0)
			return true;
		else
			return false;
	}

	public void syncUser() {
		// Update User Table
		String product_url = "http://foodopendata-api.herokuapp.com/api/login";
		StringRequest postRequest = new StringRequest(Request.Method.POST,
				product_url, new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						try {
							JSONObject entry = new JSONObject(response);
							User u = new User();
							u.setEmail(entry.getString("email"));
							u.setId(entry.getLong("id"));
							SyncManager.this.mUserDao.insert(u);
							User.CURRENT_USER_ID = entry.getLong("id");
							User.CURRENT_USER_NAME = entry.getString("email");
						} catch (JSONException e) {
							Log.e(TAG, "JSONException:" + e);
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e(TAG, "Error.Response:" + error);
					}
				}) {
			@Override
			protected Map<String, String> getParams() {
				// Use Default Account in Android as first app_soical_Id
				AccountManager mAccountManager = AccountManager
						.get(SyncManager.this.mActivity);
				Account[] accounts = mAccountManager
						.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
				String[] names = new String[accounts.length];
				Map<String, String> params = new HashMap<String, String>();
				if (names.length > 0)
					params.put("email", accounts[0].name);
				params.put("account_type", accounts[0].type);
				return params;
			}
		};
		MyVolley.getRequestQueue().add(postRequest);
	}

	public void syncTestProject() {
		String product_url = "http://foodopendata-api.herokuapp.com/api/testproject";
		JsonObjectRequest projectReq = new JsonObjectRequest(Method.GET,
				product_url, null,new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							JSONArray entries = response.getJSONArray("result");
							Log.v(TAG, "update TestProject Table Start:+"+entries.length());
							JSONObject entry;
							mTestProjectDao.deleteAll();
							for (int i = 0; i < entries.length(); i++) {
								entry = entries.getJSONObject(i);
								TestProject p = new TestProject();
								p.setId(entry.getLong("id")); 
								p.setTitle(entry.getString("title"));
								p.setTarget_amount(entry.getDouble("target_amount"));
							    p.setCurrent_amount(entry.getDouble("current_amount"));
								p.setCurrent_donators(Integer.valueOf(entry.getString("current_donators")));
								p.setDeadline(entry.getString("deadline").substring(0, 10)); 
								p.setProduct_id(Integer.valueOf(entry.getString("food_id"))); // TODO :change to product_id
								p.setDescription(entry.getString("description"));
								p.setTesters(entry.getString("testers"));
								p.setTest_items(entry.getString("test_items"));
								p.setProposer(entry.getString("proposer"));
								mTestProjectDao.insert(p);
							} 
							
							Log.v(TAG, "update TestProject Table Completed");

						} catch (JSONException e) {
							showErrorDialog(e);
						} catch (NumberFormatException e) {
							showErrorDialog(e);
						}
					}
				}, updateTableReqErrorListener());
		MyVolley.getRequestQueue().add(projectReq);

	}

	public void syncDonation() {
		String product_url = "http://foodopendata-api.herokuapp.com/api/product";
		JsonObjectRequest DonationHistoryReq = new JsonObjectRequest(
				Method.GET, product_url, null,
				 new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							mDonationDao.deleteAll();
							JSONArray entries = response.getJSONArray("result");
							Log.v(TAG,"update Donation Table Start:"+ entries.length());
							JSONObject entry;
							for (int i = 0; i < entries.length(); i++) {
								entry = entries.getJSONObject(i);
								Donation d = new Donation();
								d.setAmount(entry.getDouble("amount"));
								d.setCreated_at(entry.getString("created_at"));
								d.setId(entry.getLong("id"));
								// d.setStatus(status); //TBD
								d.setTest_project_id(entry.getLong("testproject_id"));
								d.setUpdated_at(entry.getString("updated_at"));
								d.setUser_id(entry.getLong("user_id"));
								mDonationDao.insertOrReplace(d);
							}
							Log.e(TAG, "update Donation Table completed");
						} catch (JSONException e) {
							showErrorDialog(e);
						} catch (NumberFormatException e) {
							showErrorDialog(e);
						}
					}
				},
				updateTableReqErrorListener());
		MyVolley.getRequestQueue().add(DonationHistoryReq);
	}
	
	public void syncProduct(){
		// Update Product
		String product_url="";
		List<Product> p = mProductDao.queryBuilder()
				.orderDesc(ProductDao.Properties.Updated_at).list();
		Log.v(TAG, "start updateFoodTable. current size is : " + p.size());
		if (p.size() != 0) { // if product table not empty, download incremental
								// updated product. If empty, download the full
								// set of data.
			Date latestUpdateDate = p.get(0).getUpdated_at();
			Date nextDateToFetch = new Date(latestUpdateDate.getTime()
					+ (1000 * 60 * 60 * 24));
			product_url = "http://foodopendata-api.herokuapp.com/api/product?update_after="
					+ sdf_rest.format(nextDateToFetch);
		} else {
			product_url = "http://foodopendata-api.herokuapp.com/api/product";
		}
		JsonObjectRequest foodReq = new JsonObjectRequest(Method.GET,
				product_url, null, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							Log.v(TAG, "update Product Table Start ");
							JSONArray entries = response.getJSONArray("result");
							JSONObject entry;
							for (int i = 0; i < entries.length(); i++) {
								entry = entries.getJSONObject(i);
								Product p = new Product();
								p.setCalory_kcal(entry.getString("calory_kcal"));
								p.setCapacity(entry.getString("capacity"));
								p.setCapacity_unit(entry.getString("capacity_unit"));
								p.setCarbohydrate_g(entry.getString("carbohydrate_g"));
								if (!entry.getString("company_id").equals("null")) p.setCompany_id(Long.valueOf(entry.getString("company_id")));
								if (!entry.isNull("img_url"))	p.setImg_uri(entry.getString("img_url"));
								p.setFat_g(entry.getString("fat_g"));
								p.setFat_saturated_g(entry.getString("fat_saturated_g"));
								p.setFat_trans_g(entry.getString("fat_trans_g"));
								p.setIs_watched(Boolean.FALSE);
								p.setName(entry.getString("name"));
								p.setProtein_g(entry.getString("protein_g"));
								p.setServing_size(entry.getString("serving_size"));
								p.setServing_vol(entry.getString("serving_vol"));
								p.setSodium_mg(entry.getString("sodium_mg"));
								p.setCreated_at(sdf.parse(entry.getString("created_at")));
								p.setUpdated_at(sdf.parse(entry.getString("updated_at")));
								p.setHot_score((int) (Math.random() * 10));
								if (!entry.getString("rec_score").equals("null")) p.setRec_score(Integer.valueOf(entry.getString("rec_score")));	
								mProductDao.insert(p);
							}
							Log.v(TAG, "update Product Table Completed ");
							SyncManager.this.closeSelf();
						} catch (JSONException e) {
							showErrorDialog(e);
						} catch (NumberFormatException e) {
							showErrorDialog(e);
						} catch (ParseException e) {
							showErrorDialog(e);
						}
					}
				},
				updateTableReqErrorListener());
				MyVolley.getRequestQueue().add(foodReq);
	}
	
	public void syncNews(){
		JsonArrayRequest newsReq = new JsonArrayRequest(
				"http://foodopendata-api.herokuapp.com/api/news",
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						try {
							Log.v(TAG, "update News Table Start");
							// JSONArray entries =
							// response.getJSONArray("");//get the
							// array of the news
							for (int i = 0; i < response.length(); i++) {
								JSONObject entry = response.getJSONObject(i);
								News n = new News();
								n.setTitle(entry.getString("title"));
								n.setTimestamp(entry.getString("timestamp"));
								n.setNews_abstract(entry.getString("abstract"));
								n.setText(entry.getString("text"));
								n.setLink(entry.getString("link"));
								n.setSource(entry.getString("source"));
								n.setCreated_at(sdf.parse(entry
										.getString("created_at")));
								n.setUpdated_at(sdf.parse(entry
										.getString("updated_at")));

								// mNewsDao.insertOrReplace(n);
								mNewsDao.insert(n);
								// Log.d(TAG, "Inserted new News : " +
								// n.getTitle());
							}
							Log.v(TAG, "update News Table Completed");
						} catch (JSONException e) {
							showErrorDialog(e);
						} catch (NumberFormatException e) {
							showErrorDialog(e);
						} catch (ParseException e) {
							showErrorDialog(e);
						}
					}
				}, updateTableReqErrorListener());
		MyVolley.getRequestQueue().add(newsReq);
	}
	
	public void syncCompany(){
		JsonObjectRequest companyReq = new JsonObjectRequest(Method.GET,
				"http://foodopendata-api.herokuapp.com/api/company", null,
				new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {

							JSONArray entries = response.getJSONArray("result");
							Log.v(TAG,"update Company Table Start :"+ entries.length());
							JSONObject entry;
							for (int i = 0; i < entries.length(); i++) {
								entry = entries.getJSONObject(i);
								Company c = new Company();
								c.setAddress(entry.getString("address"));
								c.setCompany_no(entry.getString("company_no"));
								c.setName(entry.getString("name"));
								c.setOwner_name(entry.getString("owner_name"));
								c.setPhone_no(entry.getString("phone_no"));
								c.setCreated_at(sdf.parse(entry.getString("created_at")));
								c.setUpdated_at(sdf.parse(entry.getString("updated_at")));
								mCompanyDao.insertOrReplace(c);
							}
							Log.v(TAG, "update Company Table Completed");
						} catch (JSONException e) {
							showErrorDialog(e);
						} catch (NumberFormatException e) {
							showErrorDialog(e);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}, updateTableReqErrorListener());
		MyVolley.getRequestQueue().add(companyReq);
	}
	
	public void clearAll(){
		mCompanyDao.deleteAll();
		mNewsDao.deleteAll();
		mProductDao.deleteAll();
		mTestProjectDao.deleteAll();
		mDonationDao.deleteAll();
		mUserDao.deleteAll();
	}
	
	private SyncManager(Activity activity) {
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(activity,
				"foodabc-db", null);
		mDB = helper.getWritableDatabase();
		mDaoMaster = new DaoMaster(mDB);
		mDaoSession = mDaoMaster.newSession();
		mProductDao = mDaoSession.getProductDao();
		mCompanyDao = mDaoSession.getCompanyDao();
		mNewsDao = mDaoSession.getNewsDao();
		mTestProjectDao = mDaoSession.getTestProjectDao();
		mUserDao = mDaoSession.getUserDao();
		mDonationDao = mDaoSession.getDonationDao();
		mActivity = activity;
	}

	private void closeSelf() {
		// Start Product View
		Intent intent = new Intent(this.mActivity, NEXT_PAGE);
		this.mActivity.startActivity(intent);
		this.mActivity.finish();

	}
	
	private void showErrorDialog(Exception e) {
		AlertDialog.Builder b = new AlertDialog.Builder(this.mActivity);
		b.setMessage("Error! " + e.getLocalizedMessage());
		// b.show(); 		// Don't toast. Will leak the windows. try dump the error
		// to somewhere else
		Log.v(TAG, "ERROR!! : " + e.toString());
	}

	private Response.ErrorListener updateTableReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				Log.v(TAG, "updateTableReqError:" + error.toString());
				showErrorDialog(error);
			}
		};
	}

	
}
