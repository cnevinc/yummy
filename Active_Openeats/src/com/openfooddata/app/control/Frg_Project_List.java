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

import com.openfooddata.app.adapter.ProjectListArrayAdapter;
import com.openfooddata.app.model.DaoMaster;
import com.openfooddata.app.model.DaoSession;
import com.openfooddata.app.model.Donation;
import com.openfooddata.app.model.DonationDao;
import com.openfooddata.app.model.Product;
import com.openfooddata.app.model.ProductDao;
import com.openfooddata.app.model.TestProject;
import com.openfooddata.app.model.TestProjectDao;
import com.openfooddata.app.model.DaoMaster.DevOpenHelper;
import com.openfooddata.volley.util.MyVolley;

import de.greenrobot.dao.Property;
import de.greenrobot.dao.query.QueryBuilder;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;

public class Frg_Project_List extends Fragment implements
		SearchView.OnQueryTextListener {

	protected static final String TAG = "nevin";

	// Private member for UI
	private ListView mlvTestProject;
	private ArrayList<TestProject> mEntries = new ArrayList<TestProject>();
	private ProjectListArrayAdapter mAdapter;
	private SearchView mSearchView;

	// Private member for DB operation
	private SQLiteDatabase db;
	private DaoMaster mDaoMaster;
	private DaoSession mDaoSession;
	private TestProjectDao mTestProjectsDAO;

	private DonationDao mDonationDao;

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
		View rootView = inflater.inflate(R.layout.frg_project_view,
				container, false);
		rootView.findViewById(R.id.layout_donate).setVisibility(View.GONE);
		
		// Setup DB operation
		DevOpenHelper helper = new DaoMaster.DevOpenHelper(this.getActivity(),
				"foodabc-db", null);
		db = helper.getWritableDatabase();
		mDaoMaster = new DaoMaster(db);
		mDaoSession = mDaoMaster.newSession();
		mTestProjectsDAO = mDaoSession.getTestProjectDao();
		mDonationDao = mDaoSession.getDonationDao();

//		if (this.getArguments()!=null && this.getArguments().getBoolean("donatedbyme")==true){
//			ArrayList<Donation> donates = (ArrayList<Donation> )mDonationDao.queryBuilder().list();
//			ArrayList<Long> pids = new ArrayList<Long>();
//			for (Donation d : donates)
//				pids.add(d.getTest_project_id());
//			mEntries = (ArrayList<TestProject>) mTestProjectsDAO.queryBuilder().where(TestProjectDao.Properties.Id.in(pids)).list();
//			
//		}else{
//			mEntries = (ArrayList<TestProject>) mTestProjectsDAO.queryBuilder().list();
//		}
		mEntries = (ArrayList<TestProject>) mTestProjectsDAO.queryBuilder().list();
		mlvTestProject = (ListView) rootView.findViewById(R.id.lv_project);
		mAdapter = new ProjectListArrayAdapter(this.getActivity(), 0, mEntries,
				MyVolley.getImageLoader());
		mlvTestProject.setAdapter(mAdapter);
		mlvTestProject.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent();
				intent.putExtra("project_id", arg2);
				intent.setClass(Frg_Project_List.this.getActivity(),Act_Project.class);
				Frg_Project_List.this.getActivity().startActivity(intent);
			}
		});
		mlvTestProject.setTextFilterEnabled(true);

		return rootView;
	}
	

	private void setupSearchView() {
		mSearchView.setIconifiedByDefault(true);
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setSubmitButtonEnabled(false);
		// mSearchView.setQueryHint(getString(R.string.TestProject_hunt_hint));
		mSearchView.setQueryHint("please enter a keyword");
	}
	
	@Override
	public boolean onQueryTextChange(String newText) {
//	    Log.v(TAG,"R Init onQueryTextChange:"+newText);

	    if (TextUtils.isEmpty(newText)) {
//	    	Log.v(TAG,"R Init onQueryTextChange, empty items:"+this.mTestProjectsDAO.queryBuilder().list().size());
			this.mAdapter.clear();
		    this.mAdapter.addAll(this.mTestProjectsDAO.queryBuilder().list());
		    this.mAdapter.notifyDataSetChanged();
		  
		} else {

		    List<Product> ps =this.mDaoSession.getProductDao().queryBuilder().where(ProductDao.Properties.Name.like("%"+newText+"%")).list();
		    List<Long> pids = new ArrayList<Long>();
		    for(Product p : ps)
		    	pids.add(p.getId());
//		    Log.v(TAG,"R Init onQueryTextChange, related product:"+ps.size());
		    this.mAdapter.clear();
		    this.mAdapter.addAll(this.mTestProjectsDAO.queryBuilder().where(TestProjectDao.Properties.Product_id.in(pids)).list());
//		    Log.v(TAG,"R Init onQueryTextChange, related project:"+this.mTestProjectsDAO.queryBuilder().where(TestProjectDao.Properties.Product_id.in(pids)).list().size());
		    this.mAdapter.notifyDataSetChanged();
		}
		return false;
	}
	
	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}
}
