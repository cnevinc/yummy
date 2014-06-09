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
	
	static int DEFAULT_TEXT_SIZE=13;
	static int MAX_WIDTH  = 600;
	static int MAX_HEIGHT = 400;
	static int scale_ratio;
	
	TextView mDetail;
	ProgressBar mProgressBar;
	ScrollView mScrollView;
	String body;
	Handler handler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.act_recipe_detail);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		if (this.getIntent().getStringExtra("body") != null)
			body = this.getIntent().getStringExtra("body");

		mScrollView= (ScrollView)this.findViewById(R.id.scrollView1);
		
		mDetail = (TextView) this.findViewById(R.id.tv_recipe_detail);

		if (this.getIntent().getStringExtra("body") != null)
			body = this.getIntent().getStringExtra("body");

		body= body.replace("<img ", "<img width=80% ");
		
		Log.d(TAG,"HI!!!    "+body);
		
		WebView wv = (WebView)this.findViewById(R.id.webView1);
		WebSettings wb = wv.getSettings();
		wb.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		wb.setJavaScriptEnabled(true);
		wb.setSupportZoom(true);
		
		String baseUrl="file://"+Environment.getExternalStorageDirectory().getPath()+"myimg";
		wv.loadDataWithBaseURL(baseUrl, body, "text/html", "utf-8", null);

		/*
		mDetail.setTextSize(DEFAULT_TEXT_SIZE);
		mDetail.setText(Html.fromHtml(body));

		mProgressBar = (ProgressBar) this.findViewById(R.id.bar);
		mDetail.setMovementMethod(ScrollingMovementMethod.getInstance());// 皛
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				if (msg.what == 0x101) {
					mProgressBar.setVisibility(View.GONE);
					mDetail.setText((CharSequence) msg.obj);
				}
				super.handleMessage(msg);
			}
		};
		Thread t = new Thread(new ImageTask());
		t.start();
		mProgressBar.setVisibility(View.VISIBLE);
*/
	}
	class ImageTask implements Runnable {
		Message msg = Message.obtain();

		@Override
		public void run() {

			ImageGetter imageGetter = new ImageGetter() {
				@Override
				public Drawable getDrawable(String source) {
					// TODO Auto-generated method stub
					URL url;
					Bitmap bitmap=null;
					Drawable drawable=null;
					try {
						url = new URL(source);
						
						// Download the image and tirm it's size
						BitmapFactory.Options o = new BitmapFactory.Options();
			            o.inJustDecodeBounds = true;
			            BitmapFactory.decodeStream(
			                    (InputStream) url.getContent(), null, o);
			            // The new size we want to scale to
			            final int REQUIRED_SIZE = 200;

			            // Find the correct scale value. It should be the power of 2.
			            int scale = 1;
			            while ( //o.outWidth / scale / 2 >= REQUIRED_SIZE &&
			                     o.outHeight / scale / 2 >= REQUIRED_SIZE)
			                scale *= 2;

			            // Decode with inSampleSize
			            BitmapFactory.Options o2 = new BitmapFactory.Options();
			            o2.inSampleSize = scale;
			            bitmap = BitmapFactory.decodeStream(
			                    (InputStream) url.getContent(), null, o2);

			            drawable = new BitmapDrawable(bitmap);
			            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
			                    drawable.getIntrinsicHeight());
			            
						
						// scale up the low quality pics
						int scale_y = MAX_HEIGHT/drawable.getIntrinsicHeight();
						drawable.setBounds(0, 0,
								drawable.getIntrinsicWidth()*scale_y,
								drawable.getIntrinsicHeight()*scale_y);
						
					} catch (MalformedURLException e) {
						Log.e(TAG, "MalformedURLException" + e.toString());
					} catch (IOException e) {
						Log.e(TAG, "IOException" + e.toString());
					}
					return drawable;
				}
			};
			CharSequence test = Html.fromHtml(body, imageGetter, null);
			Log.d(TAG,"~~~~"+test);
			msg.what = 0x101;
			msg.obj = test;
			handler.sendMessage(msg);
		}
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

	private int currentScrollLocation = 0 ;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
		case R.id.action_next_page:
			mScrollView.post(new Runnable() {
				public void run() {
					mScrollView.scrollTo(currentScrollLocation, mScrollView.getBottom());
					currentScrollLocation= mScrollView.getBottom();
				}
			});
			return true;
		case R.id.action_previous_page:
			mScrollView.post(new Runnable() {
				public void run() {
					mScrollView.scrollTo(currentScrollLocation, mScrollView.getTop());
					currentScrollLocation=mScrollView.getTop();
				}
			});
			return true;
		case R.id.action_font_up:
			
			mDetail.setTextSize(++DEFAULT_TEXT_SIZE);
			return true;
		case R.id.action_font_down:
			
			mDetail.setTextSize(--DEFAULT_TEXT_SIZE);
			return true;
		case R.id.action_big_img:
			MAX_HEIGHT += 300;
			Thread t = new Thread(new ImageTask());
			t.start();
			mProgressBar.setVisibility(View.VISIBLE);
			return true;
		case R.id.action_small_img:
			MAX_HEIGHT -= 300;
			Thread t2 = new Thread(new ImageTask());
			t2.start();
			mProgressBar.setVisibility(View.VISIBLE);
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
