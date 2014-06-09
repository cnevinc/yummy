package com.cgearc.yummy;

import java.net.URLEncoder;
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
import com.cgearc.yummy.DaoMaster.DevOpenHelper;
import com.cgearc.yummy.Frg_RecipeList.ArticleAdapter;
import com.cgearc.yummy.Frg_RecipeList.SearchCompletedListener;

public class SyncManager {
	private static final String TAG = "nevin";
	private static SyncManager mSyncManager;
	private Context mContext;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");// 'T'hh:mm:ss.SSS'Z'"); ex. 2013-11-11T12:21:48.033Z
	private SimpleDateFormat sdf_rest = new SimpleDateFormat("yyyyMMdd"); // example:
	

	public static SyncManager getInstance(Context context) {
		if (mSyncManager == null)
			return new SyncManager(context);
		else
			return mSyncManager;
	}

	
	public void getArticlesByKeyword(final ArticleAdapter adapter, String keyword, final SearchCompletedListener mCallback){

		String api_url = "http://emma.pixnet.cc/blog/articles/search?key="
				+ URLEncoder.encode(keyword+" ����") + "&per_page=40";

		JsonObjectRequest jsonRequest = new JsonObjectRequest(Method.GET,
				api_url, null, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						mCallback.onSearchCompleted();
						try {

							// JSONArray entries =
							// response.getJSONObject("article");
							// JSONObject allArticles=
							// response.getJSONObject("articles");
							JSONArray entries = response
									.getJSONArray("articles");
							for (int i = 0; i < entries.length(); i++) {

								JSONObject entry = entries.getJSONObject(i);

								getArticlesByAIDUID(
										adapter,
										entry.getString("id"),
										entry.getJSONObject("user").getString(
												"name"),mCallback);
								Log.d(TAG,
										"getArticlesByKeyword: "
												+ entry.getString("title")
												+ "\n"
												+ entry.getString("id")
												+ "~"
												+ entry.getJSONObject("user")
														.getString("name"));
							}
						} catch (JSONException e) {
							showErrorDialog(e);
						} catch (NumberFormatException e) {
							showErrorDialog(e);
						}
					}
				},new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mCallback.onSearchCompleted();
						Log.e(TAG, "updateTableReqError:" + error.toString());
						showErrorDialog(error);
					}
				});
			MyVolley.getRequestQueue().add(jsonRequest);
		}

	public void getArticlesByFavorite(final ArticleAdapter adapter,  final SearchCompletedListener mCallback){

		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this.mContext,
				"foodabc-db", null);
		SQLiteDatabase db = helper.getWritableDatabase();
		DaoMaster mDaoMaster = new DaoMaster(db);
		DaoSession mDaoSession = mDaoMaster.newSession();
		FavoriteDao fdao = mDaoSession.getFavoriteDao();
		List<Favorite> a = fdao.queryBuilder().list();
		mDaoSession.clear();
		db.close();

		for (int i = 0; i < a.size(); i++) {

			getArticlesByAIDUID(adapter, String.valueOf(a.get(i)
					.getArticle_id()), a.get(i).getBlogger_id(), mCallback);
		}
	}
	public void getArticlesByAIDUID(final ArticleAdapter adapter,final String article_id, String user_name, final SearchCompletedListener mCallback) {
		// Update User Table
		String api_url = "http://emma.pixnet.cc/blog/articles/"+article_id+"?user="+user_name;
		JsonObjectRequest jsonRequest = new JsonObjectRequest(Method.GET,
				api_url, null, new Response.Listener<JSONObject>() {
					@Override
					public void onResponse(JSONObject response) {
						try {
							mCallback.onSearchCompleted();
							//JSONArray entries = response.getJSONObject("article");
							JSONObject entry= response.getJSONObject("article");
//							for (int i = 0; i < entries.length(); i++) {
								//entry = entries.getJSONObject(i);
								Article article = new Article();
								article.setId(Long.valueOf(article_id));
								article.setArticle_id(article_id);
								article.setBody(entry.getString("body"));
								article.setComments_count(entry.getJSONObject("info").getString("comments_count"));
								article.setHits_daily(entry.getJSONObject("hits").getString("daily"));
								if (entry.getJSONObject("hits").getInt("total")<500)
									return;
								article.setHits_total(entry.getJSONObject("hits").getString("total"));
								article.setLink(entry.getString("link"));
								article.setPublic_at(entry.getString("public_at"));
								if (entry.getString("thumb").equals("http://s.pixfs.net/mobile/images/blog/article-image.png"))
									return;	// don't want a recipe without thumb
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
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						mCallback.onSearchCompleted();
						Log.e(TAG, "updateTableReqError:" + error.toString());
						showErrorDialog(error);
					}
				});
		MyVolley.getRequestQueue().add(jsonRequest);
	}

	
	private SyncManager(Context context) {

		mContext = context;
	}

	
	private void showErrorDialog(Exception e) {
		AlertDialog.Builder b = new AlertDialog.Builder(this.mContext);
		b.setMessage("Error! " + e.getLocalizedMessage());
		// b.show(); 		// Don't toast. Will leak the windows. try dump the error
		// to somewhere else
		Log.e(TAG, "SyncManager Error!! : " + e.toString());
	}


	
}
