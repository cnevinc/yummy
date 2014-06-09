package com.cgearc.yummy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Fragment that appears in the "content_frame", shows a planet
 */
public class Act_RecipeDetail extends Activity {

	private static final String TAG = "nevin";
	
	static int DEFAULT_TEXT_SIZE=16;
	static int MAX_WIDTH  = 600;
	static int MAX_HEIGHT = 400;
	static int scale_ratio=80;
	
	
	WebView mWebView;
	String mBody;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_recipe_detail);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (this.getIntent().getStringExtra("body") != null)
			mBody = this.getIntent().getStringExtra("body");

		Log.d(TAG,"~~~~~~~~~~"+mBody);
		mBody= mBody.replace("<img ", "<img style=\"max-width:80%; max-height:auto;\" ");
		mBody= mBody.replaceAll("(?i)(font-size): [0-9]+pt","");
		Log.d(TAG,"------------"+mBody);

		mWebView = (WebView)this.findViewById(R.id.webView1);
		
		WebSettings wb = mWebView.getSettings();
		wb.setDefaultFontSize(++DEFAULT_TEXT_SIZE);
		wb.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		wb.setSupportZoom(true);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		String baseUrl="file://"+Environment.getExternalStorageDirectory().getPath()+"/myimg";
		mWebView.loadDataWithBaseURL(baseUrl, mBody, "text/html", "utf-8", null);

	}
	@Override
	public void onBackPressed() {
		// finish() is called in super: we only override this method to be able
		// to override the transition
		super.onBackPressed();

		overridePendingTransition(R.anim.activity_back_in,
				R.anim.activity_back_out);
	}

	@Override 
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.detail, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.action_next_page:
			mWebView.post(new Runnable() {
				public void run() {
					if (mWebView.getContentHeight() * mWebView.getScale() >= mWebView.getScrollY() ){
						mWebView.scrollBy(0, (int)mWebView.getHeight());
					}
				}
			});
			return true;
		case R.id.action_previous_page:
			mWebView.post(new Runnable() {
				public void run() {
					if (mWebView.getScrollY() - mWebView.getHeight() > 0){
						mWebView.scrollBy(0, -(int)mWebView.getHeight());
					}else{
						mWebView.scrollTo(0, 0);
					}
				}
			});
			return true;
		case R.id.action_font_up:
			DEFAULT_TEXT_SIZE=DEFAULT_TEXT_SIZE+5;
			mWebView.getSettings().setDefaultFontSize(DEFAULT_TEXT_SIZE);
//			mWebView.loadDataWithBaseURL(null, mBody, "text/html", "utf-8", null);
			return true;
		case R.id.action_font_down:
			DEFAULT_TEXT_SIZE=DEFAULT_TEXT_SIZE-5;
			mWebView.getSettings().setDefaultFontSize(DEFAULT_TEXT_SIZE);
//			mWebView.loadDataWithBaseURL(null, mBody, "text/html", "utf-8", null);
			return true;
		case R.id.action_big_img:
			int new_ratio = scale_ratio+20;
			mWebView.zoomIn(); 
			return true;
		case R.id.action_small_img:
//			new_ratio = scale_ratio-20;
//			mBody= mBody.replace("<img width=80% ", "<img width=0% ");
//			scale_ratio = new_ratio;
//			mWebView.loadDataWithBaseURL(null, mBody, "text/html", "utf-8", null);
			mWebView.zoomOut();
			return true;

			// up button
		case android.R.id.home:
			finish();
			overridePendingTransition(R.anim.activity_back_in,
					R.anim.activity_back_out);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

}
