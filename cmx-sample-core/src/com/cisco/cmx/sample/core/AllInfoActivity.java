package com.cisco.cmx.sample.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cisco.cmx.model.CMXFloor;
import com.cisco.cmx.model.CMXPoi;
import com.cisco.cmx.model.CMXVenue;
import com.cisco.cmx.network.CMXClient;
import com.cisco.cmx.network.CMXFloorsResponseHandler;
import com.cisco.cmx.network.CMXPoisResponseHandler;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AllInfoActivity extends ActionBarActivity {

    public static String EXTRA_VENUES = "VENUES";
    
    private List<CMXVenue> mVenues;
    
    private Map<String, List<CMXFloor>> mFloors = new HashMap<String, List<CMXFloor>>();
    
    private Map<String, List<CMXPoi>> mPois = new HashMap<String, List<CMXPoi>>();
    
    private TextView mTextViewAllInfo;

    private int mVenueCount = -1;
    
    private int mFloorCount = -1;

    private static CMXClient cmxClientInstance;
    
    private Bundle parentExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_info);
        parentExtras = getIntent().getExtras();
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        cmxClientInstance = CMXClient.getInstance();
        mTextViewAllInfo = (TextView) findViewById(R.id.textViewAllInfo);
        
        mVenues = getIntent().getExtras().getParcelableArrayList(VenueActivity.EXTRA_VENUES);
        loadVenues();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.all_info, menu);
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
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_all_info, container, false);
            return rootView;
        }
    }
    
    private void loadVenues() {
        mVenueCount = mVenues.size();
        mFloorCount = 0;
        new WaitforTaskFinish().execute(null, null, null);
        for (CMXVenue venue : mVenues) {
            loadFloors(venue);
        }
    }

    private void loadFloors(final CMXVenue venue) {
        cmxClientInstance.loadMaps(venue.getId(), new CMXFloorsResponseHandler() {
            @Override
            public void onSuccess(List<CMXFloor> floors) {
                synchronized (mFloors) {
                    if (floors != null && floors.size() > 0) {
                        mFloors.put(floors.get(0).getVenueId(), floors);
                        mFloorCount += floors.size();
                    }
                    mVenueCount--;
                }
                for (CMXFloor floor : floors) {
                    loadPois(floor);
                }
            }
        });
    }

    private void loadPois(final CMXFloor floor) {
        cmxClientInstance.loadPois(floor.getVenueId(), floor.getId(), new CMXPoisResponseHandler() {
            @Override
            public void onSuccess(List<CMXPoi> pois) {
                synchronized (mPois) {
                    if (pois != null && pois.size() > 0) {
                        mPois.put(pois.get(0).getFloorId(), pois);
                    }
                    mFloorCount--;
                }
            }
        });
    }
    
    private class WaitforTaskFinish extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            while (mVenueCount != 0 || mFloorCount != 0) {
                try {
                    Thread.sleep(2000);
                }
                catch (Exception e) {

                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mTextViewAllInfo.setText("----  ALL Venue Information ---\n");
            for (CMXVenue venue : mVenues) {
                mTextViewAllInfo.append("Venue Name                    : " + venue.getName() + "\n");
                mTextViewAllInfo.append("Venue ID                      : " + venue.getId() + "\n");
                mTextViewAllInfo.append("Venue Street Address          : " + venue.getStreetAddress() + "\n");
                mTextViewAllInfo.append("----  Floor Info For Venue " + venue.getName() + " ----\n");
                if (mFloors.get(venue.getId()) != null) {
                    for (CMXFloor floor : mFloors.get(venue.getId())) {
                        mTextViewAllInfo.append("   Floor Name                 : " + floor.getName() + "\n");
                        mTextViewAllInfo.append("   Floor ID                   : " + floor.getId() + "\n");
                        mTextViewAllInfo.append("   Floor Hierarchy            : " + floor.getHierarchy() + "\n");
                        mTextViewAllInfo.append("-----  POI Info For Venue " + venue.getName() + " -----\n");
                        if (mPois.get(floor.getId()) != null) {
                            for (CMXPoi poi : mPois.get(floor.getId())) {
                                mTextViewAllInfo.append("      POI Name                : " + poi.getName() + "\n");
                                mTextViewAllInfo.append("      POI ID                  : " + poi.getId() + "\n");
                            }
                        }                    
                    }
                }
            }
        }
    }

}
