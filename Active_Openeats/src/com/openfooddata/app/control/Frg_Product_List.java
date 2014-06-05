/**
public String getmImgurl() {
		return mImgurl;
	}

	public void setmImgurl(String mImgurl) {
		this.mImgurl = mImgurl;
	}

	public boolean ismCname() {
		return mCname;
	}

	public void setmCname(boolean mCname) {
		this.mCname = mCname;
	}

	public long getmDesc() {
		return mDesc;
	}

	public void setmDesc(long mDesc) {
		this.mDesc = mDesc;
	}

	public boolean ismPrice() {
		return mPrice;
	}

	public void setmPrice(boolean mPrice) {
		this.mPrice = mPrice;
	} * Copyright 2013 Ognyan Bankov
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

package com.openfooddata.app.control;

import java.util.ArrayList;
import java.util.List;

import co.oepeneats.app.R;

import com.openfooddata.app.adapter.ProductArrayAdapter;
import com.openfooddata.app.model.DaoMaster;
import com.openfooddata.app.model.DaoSession;
import com.openfooddata.app.model.Product;
import com.openfooddata.app.model.ProductDao;
import com.openfooddata.app.model.DaoMaster.DevOpenHelper;
import com.openfooddata.volley.util.MyVolley;

import de.greenrobot.dao.Property;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

public class Frg_Product_List extends Fragment implements
		SearchView.OnQueryTextListener {

	protected static final String TAG = "nevin";

	// Private member for UI
	private ListView mlvProduct;
	private ArrayList<Product> mEntries = new ArrayList<Product>();
	private ProductArrayAdapter mAdapter;
	private SearchView mSearchView;

	// Private member for DB operation
	private SQLiteDatabase db;
	private DaoMaster mDaoMaster;
	private DaoSession mDaoSession;
	private ProductDao mProductsDAO;

	public static String SORT_BY = "sortby";
	public static int SORT_BY_HOT = 0 ;
	public static int SORT_BY_RECOMMEND = 1 ;
	

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.menu_main, menu);
	    mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        setupSearchView();
	    super.onCreateOptionsMenu(menu,inflater);
	}
	
	@Override
	public void onCreate (Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.setHasOptionsMenu(true);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.frg_product_view,
				container, false);
		// Setup DB operation
				
				DevOpenHelper helper = new DaoMaster.DevOpenHelper(this.getActivity(), "foodabc-db",
						null);
				db = helper.getWritableDatabase();
				mDaoMaster = new DaoMaster(db);
				mDaoSession = mDaoMaster.newSession();
				mProductsDAO = mDaoSession.getProductDao();
				
				// Put favorite or hot in front of the list
				Property sortBy ;
				if (this.getArguments()!=null && this.getArguments().getInt(SORT_BY)==SORT_BY_HOT)
					sortBy = ProductDao.Properties.Rec_score;
				else if (this.getArguments()!=null && this.getArguments().getInt(SORT_BY)==SORT_BY_RECOMMEND)
					sortBy = ProductDao.Properties.Rec_score;
				else
					sortBy = ProductDao.Properties.Name;
					
				mEntries = (ArrayList<Product>) mProductsDAO.queryBuilder().
						orderAsc(sortBy).list();
				
				// intent from Act_main
				if (this.getArguments()!=null && this.getArguments().getBoolean("favorite")){
					mEntries = (ArrayList<Product>) mProductsDAO.queryBuilder().where(ProductDao.Properties.Is_watched.eq(true)).list();
				}
				
				mlvProduct = (ListView) rootView.findViewById(R.id.lv_product);
				mAdapter = new ProductArrayAdapter(this.getActivity(), 0, mEntries,
						MyVolley.getImageLoader());
				mlvProduct.setAdapter(mAdapter);
				mlvProduct.setTextFilterEnabled(true);

		return rootView;
	}
	

	private void setupSearchView() {
		mSearchView.setIconifiedByDefault(true);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setSubmitButtonEnabled(false);
		// mSearchView.setQueryHint(getString(R.string.product_hunt_hint));
		mSearchView.setQueryHint("please enter a keyword");
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
//	    Log.v(TAG,"Init onQueryTextChsnge");

		if (TextUtils.isEmpty(newText)) {
//			Log.v(TAG,"P Init onQueryTextChange, empty product:"+this.mProductsDAO.queryBuilder().list().size());
			this.mAdapter.clear();
		    this.mAdapter.addAll(this.mProductsDAO.queryBuilder().list());
		    this.mAdapter.notifyDataSetChanged();
		} else {
//			mlvProduct.setFilterText(newText.toString());
//			Log.v(TAG,"P Init onQueryTextChange, related product:"+this.mProductsDAO.queryBuilder().where(ProductDao.Properties.Name.like("%"+newText+"%")).list().size());
		    this.mAdapter.clear();
		    this.mAdapter.addAll(this.mProductsDAO.queryBuilder().where(ProductDao.Properties.Name.like("%"+newText+"%")).list());
		    this.mAdapter.notifyDataSetChanged();
		}
		return false;
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}
}
