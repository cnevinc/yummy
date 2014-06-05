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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.util.ImageLoader;
import com.android.volley.util.NetworkImageView;
import com.android.volley.util.StringRequest;

import co.oepeneats.app.R;

import com.openfooddata.app.control.Frg_Project_Detail;
import com.openfooddata.app.model.DaoMaster;
import com.openfooddata.app.model.DaoSession;
import com.openfooddata.app.model.DonationDao;
import com.openfooddata.app.model.ProductDao;
import com.openfooddata.app.model.TestProject;
import com.openfooddata.app.model.TestProjectDao;
import com.openfooddata.app.model.User;
import com.openfooddata.app.model.DaoMaster.DevOpenHelper;
import com.openfooddata.app.model.UserDao;
import com.openfooddata.volley.util.MyVolley;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class ProjectDetailArrayAdapter extends ArrayAdapter<TestProject> {
    private static final String TAG = "nevin";

	private ImageLoader mImageLoader;

    // A copy of the original mObjects array, initialized from and then used instead as soon as
    // the mFilter ArrayFilter is used. mObjects will then only contain the filtered values.
    private ArrayList<TestProject> mOriginalValues;
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
    
    public ProjectDetailArrayAdapter(Context context, 
                              int textViewResourceId, 
                              List<TestProject> entries,
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
            v = vi.inflate(R.layout.lv_project_detail, null);
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
        holder.tv_TestProject_desc.setText(entry.getDescription());
        holder.tv_amount_required.setText(entry.getTarget_amount()+"\n所需金額");
        holder.tv_total_doation.setText(entry.getCurrent_amount()+"\n已募得金額");
        holder.tv_people_count.setText(entry.getCurrent_donators()+"人\n贊助");
        double perc = Double.valueOf(entry.getCurrent_amount()) / Double.valueOf(entry.getTarget_amount())*100;
        holder.tv_achieve_percentage.setText(perc+"\n%");
        holder.tv_days_b4_due.setText(entry.getDeadline()+"\n截止");
        
        // TODO: add null check 
        holder.tl_test_items.removeAllViews();
        for (String test:entry.getTest_items().split(",") ){
           TableRow tr = new TableRow(this.getContext());
           tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
           tr.setBackgroundColor(this.getContext().getResources().getColor(android.R.color.white));
           tr.setPadding(1, 1, 1, 1);
           TextView tv = new TextView(this.getContext());
           tv.setText(test);
           tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
           tr.addView(tv);
           holder.tl_test_items.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
    	// TODO: add null check
        holder.tl_tester.removeAllViews();
        for (String test:entry.getTesters().split(",") ){
            TableRow tr = new TableRow(this.getContext());
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.setBackgroundColor(this.getContext().getResources().getColor(android.R.color.white));
            tr.setPadding(1, 1, 1, 1);
            TextView tv = new TextView(this.getContext());
            tv.setText(test);
            tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
            tr.addView(tv);
            holder.tl_tester.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }
        
        return v;
    }
     
    private class ViewHolder {
        NetworkImageView image;
        TextView product_name; 
        TextView tv_TestProject_desc; 
        TableLayout  tl_test_items; 
        TextView tv_amount_required;
        TextView tv_total_doation;
        TextView tv_achieve_percentage;
        TextView tv_people_count;
        TextView tv_days_b4_due;
        TableLayout tl_tester;
        
        public ViewHolder(View v) {
            image = (NetworkImageView) v.findViewById(R.id.iv_thumb);
            product_name = (TextView) v.findViewById(R.id.tv_product_name);
            tv_TestProject_desc = (TextView) v.findViewById(R.id.tv_project_desc);
            tl_test_items = (TableLayout ) v.findViewById(R.id.tl_test_items);
            tv_amount_required = (TextView) v.findViewById(R.id.tv_amount_required);
            tv_total_doation = (TextView) v.findViewById(R.id.tv_total_doation);
            tv_achieve_percentage = (TextView) v.findViewById(R.id.tv_achieve_percentage);
            tv_people_count = (TextView) v.findViewById(R.id.tv_people_count);
            tv_days_b4_due = (TextView) v.findViewById(R.id.tv_days_b4_due);
            tl_tester = (TableLayout) v.findViewById(R.id.tl_tester);
            v.setTag(this);
        } 
    }
    
}
