package com.cisco.cmx.sample.core;

import java.util.ArrayList;

import com.cisco.cmx.model.CMXFloor;
import com.cisco.cmx.model.CMXPoi;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class FloorPoisActivity extends ActionBarActivity {

	public static String EXTRA_FLOOR = "FLOOR";

	public static String EXTRA_POIS = "POIS";

	Bundle parentExtras;

	ArrayList<CMXPoi> mPois;

	ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_floor_pois);
		parentExtras = getIntent().getExtras();

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		CMXFloor floor = (CMXFloor) getIntent().getExtras().get(
				FloorPoisActivity.EXTRA_FLOOR);
		TextView floorName = (TextView) findViewById(R.id.textFloorName);
		TextView floorId = (TextView) findViewById(R.id.textFloorId);
		TextView floorHierarchy = (TextView) findViewById(R.id.textFloorHierarchy);

		floorName.setText(floor.getName());
		floorId.setText(floor.getId());
		floorHierarchy.setText(floor.getHierarchy());

		mListView = (ListView) findViewById(R.id.listViewPois);
		mPois = getIntent().getExtras().getParcelableArrayList(
				FloorPoisActivity.EXTRA_POIS);
		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < mPois.size(); ++i) {
			list.add(mPois.get(i).getName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		mListView.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.floor_pois, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		if (id == android.R.id.home) {
			Intent upIntent = NavUtils.getParentActivityIntent(this);
			upIntent.putExtras(parentExtras);
			NavUtils.navigateUpTo(this, upIntent);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_floor_pois,
					container, false);
			return rootView;
		}
	}

}
