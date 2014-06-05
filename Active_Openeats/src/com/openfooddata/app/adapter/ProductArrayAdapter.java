/**
 * Copyright 2013 Ognyan Bankov
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

package com.openfooddata.app.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.android.volley.util.ImageLoader;
import com.android.volley.util.NetworkImageView;

import co.oepeneats.app.R;
import com.openfooddata.app.control.Act_Splash;
import com.openfooddata.app.model.DaoMaster;
import com.openfooddata.app.model.DaoSession;
import com.openfooddata.app.model.Product;
import com.openfooddata.app.model.Product;
import com.openfooddata.app.model.ProductDao;
import com.openfooddata.app.model.DaoMaster.DevOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;


public class ProductArrayAdapter extends ArrayAdapter<Product> {
    private static final String TAG = "nevin";
 
	private ImageLoader mImageLoader;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private ArrayList<Product> mOriginalValues;
    private ArrayFilter mFilter;
    private List<Product> mObjects;
    
    // Private member for DB operation
 	private SQLiteDatabase db;
 	private DaoMaster mDaoMaster;
 	private DaoSession mDaoSession;
 	private ProductDao mProductDao;
 	
    /**
    * Lock used to modify the content of {@link #mObjects}. Any write operation
    * performed on the array should be synchronized on this lock. This lock is also
    * used by the filter (see {@link #getFilter()} to make a synchronized copy of
    * the original array of data.
    */
   private final Object mLock = new Object();
    
    public ProductArrayAdapter(Context context, 
                              int textViewResourceId, 
                              ArrayList<Product> entries,
                              ImageLoader imageLoader
                              ) {
        super(context, textViewResourceId, entries);
        this.mObjects = entries;
        mImageLoader = imageLoader;
     // Setup DB operation 
     		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this.getContext(),
     				"foodabc-db", null);
     		db = helper.getWritableDatabase();
     		mDaoMaster = new DaoMaster(db);
     		mDaoSession = mDaoMaster.newSession();
     		mProductDao = mDaoSession.getProductDao();
    }

    public int getCount() {
		if (mObjects == null) {
			return 0;
		}
		return mObjects.size();
	}

	public Product getItem(int position) {
		if (mObjects == null) {
			return null; 
		}
		return mObjects.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
	
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.lv_product_list, null);
        }
        
        ViewHolder holder = (ViewHolder) v.getTag(R.id.id_holder);       
         
        if (holder == null) {
            holder = new ViewHolder(v);
            v.setTag(R.id.id_holder, holder);
        }        
         
        final Product entry = mObjects.get(position);
        if (mObjects.size()==0) return v;
        
        if (entry.getImg_uri() != null) {
            holder.image.setImageUrl(entry.getImg_uri(), mImageLoader);
        } else {
            holder.image.setImageResource(R.drawable.no_image); 
        } 
         
        holder.product_name.setText(entry.getName());
        if (entry.getCompany()!=null)
        	holder.company_name.setText(entry.getCompany().getName());
        else
        	holder.company_name.setText("");
        holder.watchListButton.setImageResource(android.R.drawable.star_big_off);
        if(entry.getIs_watched()!=null && entry.getIs_watched())
        	 holder.watchListButton.setImageResource(android.R.drawable.star_big_on);
        holder.watchListButton.setOnClickListener(new WatchButtonnOnClickListener(position));
        return v;
    }
     
     
    private class ViewHolder {
        NetworkImageView image;
        TextView product_name; 
        TextView company_name; 
        ImageView watchListButton;
        
        public ViewHolder(View v) {
            image = (NetworkImageView) v.findViewById(R.id.iv_thumb);
            product_name = (TextView) v.findViewById(R.id.tv_product_name);
            company_name = (TextView) v.findViewById(R.id.tv_company);
            watchListButton = (ImageView) v.findViewById(R.id.wathclist_ib);
            v.setTag(this);
        } 
    }
    class WatchButtonnOnClickListener implements OnClickListener{

		int mCurrentPosition; 

		public WatchButtonnOnClickListener(int currentPosition){

			this.mCurrentPosition = currentPosition;
		}
		public void onClick(View v) {
			Log.v(TAG,"Product ["+mCurrentPosition+"] clicked!: "+mObjects.get(mCurrentPosition).getIs_watched());
		        
			Product entry = ProductArrayAdapter.this.getItem(mCurrentPosition) ; 
			if(entry.getIs_watched()!=null && entry.getIs_watched())
				((ImageView)v).setImageResource(android.R.drawable.star_big_on);
			else
				((ImageView)v).setImageResource(android.R.drawable.star_big_off);
			entry.setIs_watched(!entry.getIs_watched());
			mProductDao.update(entry); 
			Log.v(TAG,"Product ["+mCurrentPosition+"] clicked!: "+mObjects.get(mCurrentPosition).getIs_watched());
//			ProductArrayAdapter.this.notifyDataSetChanged();
			ProductArrayAdapter.this.notifyDataSetInvalidated();
		}
	}
    
    // Setup filter for searchView , overwrite parent's(ArrayAdapter<T>'s) Filter 
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    /**
     * <p>An array filter constrains the content of the array adapter with
     * a prefix. Each item that does not start with the supplied prefix
     * is removed from the list.</p>
     */
    private class ArrayFilter extends Filter {

		@Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<Product>(mObjects);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<Product> list;
                synchronized (mLock) {
                    list = new ArrayList<Product>(mOriginalValues);
                }  
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<Product> values;
                synchronized (mLock) {
                    values = new ArrayList<Product>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<Product> newValues = new ArrayList<Product>();

                for (int i = 0; i < count; i++) {
                    final Product value = values.get(i);
                    final String valueText = value.getName().toLowerCase();	// 20131108 Nevin changed the match criteria

                    // First match against the whole, non-splitted value
                    if (value.getName().contains(prefixString) ||value.getCompany().getName().contains(prefixString) ) {
                        newValues.add(value);
                    } else {
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        // Start at index 0, in case valueText starts with space(s)
                        for (int k = 0; k < wordCount; k++) {
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            //no inspection unchecked
            mObjects = (List<Product>) results.values;
//            ProductArrayAdapter.this.clear();			// nevin modify this line
//            ProductArrayAdapter.this.addAll(mObjects);	// nevin modify this line
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
        
        
    }
}
