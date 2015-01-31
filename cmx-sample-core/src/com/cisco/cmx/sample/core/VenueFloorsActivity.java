package com.cisco.cmx.sample.core;

import java.util.ArrayList;
import java.util.List;

import com.cisco.cmx.model.CMXFloor;
import com.cisco.cmx.model.CMXPoi;
import com.cisco.cmx.model.CMXVenue;
import com.cisco.cmx.network.CMXClient;
import com.cisco.cmx.network.CMXPoisResponseHandler;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class VenueFloorsActivity extends ActionBarActivity {

	public static String EXTRA_VENUE = "VENUE";

	public static String EXTRA_FLOORS = "FLOORS";

	private ProgressDialog dialog;

	private Bundle parentExtras;

	private ArrayList<CMXFloor> mFloors;

	private CMXVenue mVenue;

	private ListView mListView;

	private final Context context = this;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_venue_floors);
		parentExtras = getIntent().getExtras();
		dialog = new ProgressDialog(VenueFloorsActivity.this);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mVenue = (CMXVenue) getIntent().getExtras().get(
				VenueFloorsActivity.EXTRA_VENUE);
		TextView venueName = (TextView) findViewById(R.id.textVenueName);
		TextView venueId = (TextView) findViewById(R.id.textVenueId);
		TextView venueStreetAddress = (TextView) findViewById(R.id.textVenueStreetAddress);

		venueName.setText(mVenue.getName());
		venueId.setText(mVenue.getId());
		venueStreetAddress.setText(mVenue.getStreetAddress());

		mListView = (ListView) findViewById(R.id.listViewFloor);
		mFloors = getIntent().getExtras().getParcelableArrayList(
				VenueFloorsActivity.EXTRA_FLOORS);
		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < mFloors.size(); ++i) {
			list.add(mFloors.get(i).getName());
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				final CMXFloor floor = (CMXFloor) mFloors.get(position);
				dialog.setMessage("Loading POI Information");
				dialog.show();
				CMXClient.getInstance().loadPois(floor.getVenueId(),
						floor.getId(), new CMXPoisResponseHandler() {
							@Override
							public void onSuccess(List<CMXPoi> pois) {
								ArrayList<CMXPoi> activityPois = new ArrayList<CMXPoi>();
								Intent intent = new Intent(context,
										FloorPoisActivity.class);
								intent.putExtras(parentExtras);
								activityPois.addAll(pois);
								intent.putExtra(
										VenueFloorsActivity.EXTRA_VENUE, mVenue);
								intent.putParcelableArrayListExtra(
										VenueFloorsActivity.EXTRA_FLOORS,
										mFloors);
								intent.putExtra(FloorPoisActivity.EXTRA_FLOOR,
										floor);
								intent.putParcelableArrayListExtra(
										FloorPoisActivity.EXTRA_POIS,
										activityPois);
								startActivity(intent);
							}

							@Override
							public void onFailure(Throwable error) {
								if (dialog.isShowing()) {
									dialog.dismiss();
								}
								AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
										context);
								alertDialogBuilder
										.setMessage("Faied To Load POIs For Venue: "
												+ error);
								alertDialogBuilder.setPositiveButton(
										R.string.cmx_ok_dialog_bt,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
											}
										});
								alertDialogBuilder.create();
								alertDialogBuilder.show();
							}
						});
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.venue_floors, menu);
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
			View rootView = inflater.inflate(R.layout.fragment_venue_floors,
					container, false);
			return rootView;
		}
	}

}
