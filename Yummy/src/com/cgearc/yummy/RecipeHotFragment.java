package com.cgearc.yummy;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



















import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.util.ImageLoader;
import com.android.volley.util.NetworkImageView;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * Fragment that appears in the "content_frame", shows a planet
 */
public  class RecipeHotFragment extends BaseFragment implements OnItemClickListener {
    public static final String ARG_PLANET_NUMBER = "planet_number";
	private static final String TAG = "nevin";
	private RelativeLayout mRelativeLayout;
	ArticleAdapter mAdapter ;

    public RecipeHotFragment() {
        // Empty constructor required for fragment subclasses
    } 

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hot_recipe, container, false);
 
        mRelativeLayout = (RelativeLayout)rootView.findViewById(R.id.recipe_container);
        GridView gridView = (GridView) rootView.findViewById(R.id.recipe_grid);
        mAdapter = new ArticleAdapter(inflater, new ArrayList<Article>());
        gridView.setAdapter(mAdapter);
        readHotFromLocal();
        
        gridView.setOnItemClickListener(this);
        return rootView;
    }
    
    public void stepBack(float slideOffset){
    	if(mRelativeLayout!=null){
    		float min = 0.9f;
            float max = 1.0f;
            float scaleFactor = (max - ((max - min) * slideOffset));

            mRelativeLayout.setScaleX(scaleFactor);
            mRelativeLayout.setScaleY(scaleFactor);
    	}
    }
    
    
    /**
     * Adapter for grid of coupons.
     */
    public  class ArticleAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        
        public ArticleAdapter(LayoutInflater inflater, ArrayList<Article> articles) {
            if (articles == null) {
                throw new IllegalStateException("Can't have null list of coupons");
            }
            Setting.allArticles = articles;
            mInflater = inflater;
        }
        
        public ArrayList<Article> getArticles(){
        	return Setting.allArticles;
        }

        @Override
        public int getCount() {
            return Setting.allArticles.size();
        }

        @Override
        public Article getItem(int position) {
            return Setting.allArticles.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View result = convertView;
            if (result == null) {
                result = mInflater.inflate(R.layout.grid_item, parent, false);
            }

            // Try to get view cache or create a new one if needed
            ViewCache viewCache = (ViewCache) result.getTag();
            if (viewCache == null) {
                viewCache = new ViewCache(result);
                result.setTag(viewCache);
            } 

            // Fetch item
            Article tmp = getItem(position);

            // Bind the data
            viewCache.mTitleView.setText(tmp.getArticle_id() + "-"+tmp.getTitle());
            viewCache.mSubtitleView.setText("By "+tmp.getUser_name());
            viewCache.mImageView.setImageUrl(tmp.getThumb().replace("90x90","320x240"), MyVolley.getImageLoader());

            
            return result;
        }
        
    }

    /**
     * Cache of views in the grid item view to make recycling of views quicker. This avoids
     * additional {@link View#findViewById(int)} calls after the {@link ViewCache} is first
     * created for a view. See
     * {@link ArticleAdapter#getView(int position, View convertView, ViewGroup parent)}.
     */
    private  class ViewCache {

        /** View that displays the title of the coupon */
        private final TextView mTitleView;

        /** View that displays the subtitle of the coupon */
        private final TextView mSubtitleView;

        /** View that displays the image associated with the coupon */
        private final NetworkImageView mImageView;

        /**
         * Constructs a new {@link ViewCache}.
         *
         * @param view which contains children views that should be cached.
         */
        private ViewCache(View view) {
            mTitleView = (TextView) view.findViewById(R.id.title);
            mSubtitleView = (TextView) view.findViewById(R.id.subtitle);
            mImageView = (NetworkImageView) view.findViewById(R.id.image);
        }
    } 
    
    private void readHotFromLocal(){
    		Log.d(TAG,"updatTable!!!" );
    		String json = null;
            try {
                InputStream is = this.getActivity().getAssets().open("hot.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                SyncManager mgr = SyncManager.getInstance(this.getActivity());
                
                json = new String(buffer, "UTF-8");
                JSONArray entries = new JSONArray(json);
                for (int i = 0; i < entries.length(); i++) {
                	
                    
                	JSONObject entry = entries.getJSONObject(i);
                	
                	
                	mgr.downloadArticle(mAdapter, entry.getString("id"), entry.getString("user_name"));
                }
//    				Log.d(TAG, "Inserted new Reports : " + obj.getReportid() + "/: " +obj.getResult() );
            } catch (JSONException e) {
    			Log.v(TAG,e.toString());
    		} catch (NumberFormatException e) {
    			Log.v(TAG,e.toString());
    		} catch (ParseException e) {
    			Log.v(TAG,e.toString());
    		} catch (IOException e) {
    			Log.v(TAG,e.toString());
            } 
    	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		Article a = (Article)arg0.getItemAtPosition(arg2);
		Intent intent = new Intent(this.getActivity(), Act_RecipeDetail.class);
		intent.putExtra("body", a.getBody());
		this.startActivity(intent);
		this.getActivity().overridePendingTransition(R.anim.activity_in, R.anim.activity_out);
		
	}
    
   
    
}