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
import com.openfooddata.app.model.Donation;
import com.openfooddata.app.model.DonationDao;
import com.openfooddata.app.model.Product;
import com.openfooddata.app.model.ProductDao;
import com.openfooddata.app.model.TestProject;
import com.openfooddata.app.model.TestProject;
import com.openfooddata.app.model.TestProjectDao;
import com.openfooddata.app.model.DaoMaster.DevOpenHelper;
import com.openfooddata.app.model.User;
import com.openfooddata.app.model.UserDao;

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


public class ProjectListArrayAdapter extends ArrayAdapter<TestProject> {
    private static final String TAG = "nevin";

	private ImageLoader mImageLoader;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private ArrayList<TestProject> mOriginalValues;
    private ArrayFilter mFilter;
    private List<TestProject> mObjects;
    
    // Private member for DB operation
 	private SQLiteDatabase db;
 	private DaoMaster mDaoMaster;
 	private DaoSession mDaoSession;
 	private TestProjectDao mTestProjectsDao;
 	private ProductDao mProductsDao;
 	
    /**
    * Lock used to modify the content of {@link #mObjects}. Any write operation
    * performed on the array should be synchronized on this lock. This lock is also
    * used by the filter (see {@link #getFilter()} to make a synchronized copy of
    * the original array of data.
    */
   private final Object mLock = new Object();

	private UserDao mUserDao;

	private DonationDao mDonationDao;
    
    public ProjectListArrayAdapter(Context context, 
                              int textViewResourceId, 
                              ArrayList<TestProject> entries,
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
		mProductsDao = mDaoSession.getProductDao();
		mTestProjectsDao = mDaoSession.getTestProjectDao();
		mUserDao = mDaoSession.getUserDao();
		mDonationDao = mDaoSession.getDonationDao();
    }

    public int getCount() {
		if (mObjects == null) {
			return 0;
		}
		return mObjects.size();
	}

	public TestProject getItem(int position) {
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
            v = vi.inflate(R.layout.lv_project_list, null);
        }
        
        ViewHolder holder = (ViewHolder) v.getTag(R.id.id_holder);       
         
        if (holder == null) {
            holder = new ViewHolder(v);
            v.setTag(R.id.id_holder, holder);
        }        
         
        final TestProject entry = mObjects.get(position);
        
        
        if (entry.getProduct().getImg_uri() != null) {
            holder.image.setImageUrl(entry.getProduct().getImg_uri(), mImageLoader);
        } else {
            holder.image.setImageResource(R.drawable.no_image); 
        } 
        // TODO: extract strings
        holder.product_name.setText(entry.getProduct().getName());
        holder.tv_test_item.setText(this.getContext().getResources().getString(R.string.test_option)+entry.getTest_items());
        holder.tv_people_count.setText(entry.getCurrent_donators()+"§H\n√ŸßU");
        double perc = Double.valueOf(entry.getCurrent_amount()) / Double.valueOf(entry.getTarget_amount())*100;
        holder.tv_achieve_percentage.setText(perc*100+"\n%");
        holder.tv_days_b4_due.setText(entry.getDeadline()+"\n∫I§Ó");
        return v;
    }
     
     
    private class ViewHolder {
        NetworkImageView image;
        TextView product_name; 
        TextView tv_test_item; 
        TextView tv_achieve_percentage;
        TextView tv_people_count;
        TextView tv_days_b4_due;
        
        
        public ViewHolder(View v) {
            image = (NetworkImageView) v.findViewById(R.id.iv_thumb);
            product_name = (TextView) v.findViewById(R.id.tv_product_name);
            tv_test_item = (TextView) v.findViewById(R.id.tv_test_item);
            tv_achieve_percentage = (TextView) v.findViewById(R.id.tv_achieve_percentage);
            tv_people_count = (TextView) v.findViewById(R.id.tv_people_count);
            tv_days_b4_due = (TextView) v.findViewById(R.id.tv_days_b4_due);
            v.setTag(this);
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
                    mOriginalValues = new ArrayList<TestProject>(mObjects);
                }
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<TestProject> list;
                synchronized (mLock) {
                    list = new ArrayList<TestProject>(mOriginalValues);
                }  
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<TestProject> values;
                synchronized (mLock) {
                    values = new ArrayList<TestProject>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<TestProject> newValues = new ArrayList<TestProject>();

                for (int i = 0; i < count; i++) {
                    final TestProject value = values.get(i);
                    final String valueText = value.getProduct().getName().toLowerCase();	// 20131108 Nevin changed the match criteria

                    // First match against the whole, non-splitted value
                    if (value.getProduct().getName().contains(prefixString) ||value.getProduct().getCompany().getName().contains(prefixString) ) {
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
            mObjects = (List<TestProject>) results.values;
//            TestProjectArrayAdapter.this.clear();			// nevin modify this line
//            TestProjectArrayAdapter.this.addAll(mObjects);	// nevin modify this line
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
        
        
    }
}
