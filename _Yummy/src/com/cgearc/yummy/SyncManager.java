package com.cgearc.yummy;

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
import com.cgearc.yummy.RecipeHotFragment.ArticleAdapter;

public class SyncManager {
	private static final String TAG = "nevin";
	private static SyncManager mSyncManager;
	private Activity mActivity;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 'T'hh:mm:ss.SSS'Z'"); ex. 2013-11-11T12:21:48.033Z
	private SimpleDateFormat sdf_rest = new SimpleDateFormat("yyyyMMdd"); // example:
	

	public static SyncManager getInstance(Activity activity) {
		if (mSyncManager == null)
			return new SyncManager(activity);
		else
			return mSyncManager;
	}


	public void downloadArticle(final ArticleAdapter adapter,final String article_id, String user_name) {
		// Update User Table
		
		String product_url = "http://emma.pixnet.cc/blog/articles/"+article_id+"?user="+user_name;
		JsonObjectRequest jsonRequest = new JsonObjectRequest(Method.GET,
				product_url, null, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							
							//JSONArray entries = response.getJSONObject("article");
							JSONObject entry= response.getJSONObject("article");
//							for (int i = 0; i < entries.length(); i++) {
								//entry = entries.getJSONObject(i);
								Article article = new Article();
								article.setId(Long.valueOf(article_id));
								article.setBody(entry.getString("body"));
								article.setComments_count(entry.getJSONObject("info").getString("comments_count"));
								article.setHits_daily(entry.getJSONObject("hits").getString("daily"));
								article.setHits_total(entry.getJSONObject("hits").getString("total"));
								article.setLink(entry.getString("link"));
								article.setPublic_at(entry.getString("public_at"));
								article.setThumb(entry.getString("thumb"));
								article.setTitle(entry.getString("title"));
								article.setUser_name(entry.getJSONObject("user").getString("name"));
								adapter.getArticles().add(article);
								adapter.notifyDataSetChanged();
								
								
								JSONArray imgArray = entry.getJSONArray("images");
								for (int j = 0; j < imgArray.length(); j++) {
									JSONObject imgJ = imgArray.getJSONObject(j);
									Picture p = new Picture() ;
									p.setHeight(imgJ.getString("height"));
									p.setWidth(imgJ.getString("width"));
									p.setUri(imgJ.getString("url"));
									p.setArticle_id(Long.valueOf(article_id));
								}
//							}
						} catch (JSONException e) {
							showErrorDialog(e);
						} catch (NumberFormatException e) {
							showErrorDialog(e);
						}
					}
				},
				updateTableReqErrorListener());
		MyVolley.getRequestQueue().add(jsonRequest);
	}

	
	private SyncManager(Activity activity) {

		mActivity = activity;
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
