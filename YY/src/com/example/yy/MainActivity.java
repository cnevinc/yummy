package com.example.yy;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.method.ScrollingMovementMethod;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {
	private Handler handler;
	private String html; 
	private TextView tv;
	private ProgressBar bar; 

	@Override  
	 protected void onCreate(Bundle savedInstanceState) {  
	  super.onCreate(savedInstanceState);  
	  setContentView(R.layout.activity_main);  
	  // 网上找的html数据  
	  String span;
	html = "<html><head><title>TextView使用HTML</title></head><body><p><strong>强调</strong></p><p><em>斜体</em></p>"  
	    + "<p><a href=\"http://www.csdn.net\"><span style=\"font-family: Arial, Helvetica, sans-serif;\">找的html数据</span></a>学习</p><p><font color=\"#aabb00\">颜色1"  
	    + "</p><p><font color=\"#00bbaa\">颜色2</p><h1>标题1</h1><h3>标题2</h3><h6>标题3</h6><p>大于>小于<</p><p>"  
	    + "下面是网络图片</p><img src=\"http://th03.deviantart.net/fs70/PRE/i/2012/247/2/4/png_blood_by_paradise234-d5dkt3t.png\"/><br>"  
	    + "下面是网络图片<br><img src=\"http://th09.deviantart.net/fs71/PRE/i/2012/340/7/b/png_bird_by_moonglowlilly-d5n88u2.png\"/></body></html>";  
	  tv = (TextView) this.findViewById(R.id.id);  
	  bar = (ProgressBar) this.findViewById(R.id.bar);  
	  tv.setMovementMethod(ScrollingMovementMethod.getInstance());// 滚动  
	  handler = new Handler() {  
	   @Override  
	   public void handleMessage(Message msg) {  
	    // TODO Auto-generated method stub  
	    if (msg.what == 0x101) {  
	     bar.setVisibility(View.GONE);  
	     tv.setText((CharSequence) msg.obj);  
	    }  
	    super.handleMessage(msg);  
	   }  
	  };  
	  // 因为从网上下载图片是耗时操作 所以要开启新线程  
	  Thread t = new Thread(new Runnable() {  
	   Message msg = Message.obtain();  
	   @Override  
	   public void run() {  
	    // TODO Auto-generated method stub  
	    bar.setVisibility(View.VISIBLE);  
	    /** 
	     * 要实现图片的显示需要使用Html.fromHtml的一个重构方法：public static Spanned 
	     * fromHtml (String source, Html.ImageGetterimageGetter, 
	     * Html.TagHandler 
	     * tagHandler)其中Html.ImageGetter是一个接口，我们要实现此接口，在它的getDrawable 
	     * (String source)方法中返回图片的Drawable对象才可以。 
	     */  
	    ImageGetter imageGetter = new ImageGetter() {  
	     @Override  
	     public Drawable getDrawable(String source) {  
	      // TODO Auto-generated method stub  
	      URL url;  
	      Drawable drawable = null;  
	      try {  
	       url = new URL(source);  
	       drawable = Drawable.createFromStream(  
	         url.openStream(), null);  
	       drawable.setBounds(0, 0,  
	         drawable.getIntrinsicWidth(),  
	         drawable.getIntrinsicHeight());  
	      } catch (MalformedURLException e) {  
	       // TODO Auto-generated catch block  
	       e.printStackTrace();  
	      } catch (IOException e) {  
	       // TODO Auto-generated catch block  
	       e.printStackTrace();  
	      }  
	      return drawable;  
	     }  
	    };  
	    CharSequence test = Html.fromHtml(html, imageGetter, null);  
	    msg.what = 0x101;  
	    msg.obj = test;  
	    handler.sendMessage(msg);  
	   }  
	  });  
	  t.start();  
	 }	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
