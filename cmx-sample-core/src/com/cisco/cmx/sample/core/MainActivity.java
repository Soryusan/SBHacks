package com.cisco.cmx.sample.core;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import com.cisco.cmx.model.CMXVenue;
import com.cisco.cmx.network.CMXClient;
import com.cisco.cmx.network.CMXVenuesResponseHandler;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
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
import android.widget.EditText;

public class MainActivity extends ActionBarActivity {

    public static String EXTRA_SERVER_IP = "SERVER_IP";

    public static String EXTRA_SERVER_PORT = "SERVER_PORT";

    private final Context context = this;
    
    private ProgressDialog dialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dialog = new ProgressDialog(MainActivity.this);
        CMXClient.getInstance().initialize(this);
        
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        EditText editServerIp = (EditText) findViewById(R.id.editTextServerAddress);
        EditText editServerPort = (EditText) findViewById(R.id.editTextServerPort);
        if (getIntent() != null && getIntent().getExtras() != null) {
            String serverIp  = (String) getIntent().getExtras().get(EXTRA_SERVER_IP);
            String serverPort  = (String) getIntent().getExtras().get(EXTRA_SERVER_PORT);
            if (serverIp != null) {
                editServerIp.setText(serverIp);
            }
            if (serverPort != null) {
                editServerPort.setText(serverPort);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    public void submitServer(View v) {
        final EditText serverAddressText = (EditText) findViewById(R.id.editTextServerAddress);
        final EditText serverPortText = (EditText) findViewById(R.id.editTextServerPort);
        CMXClient.Configuration configuration = new CMXClient.Configuration();
        try {
            configuration.setServerAddress(serverAddressText.getText().toString());
            configuration.setServerPort(serverPortText.getText().toString());
        } catch (MalformedURLException ex) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Faied To Configure Server: " + ex);
            alertDialogBuilder.setPositiveButton(R.string.cmx_ok_dialog_bt, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alertDialogBuilder.create();
            alertDialogBuilder.show();
        }
        CMXClient.getInstance().setConfiguration(configuration);
        dialog.setMessage("Loading Venue Information");
        dialog.show();
        CMXClient.getInstance().loadVenues(new CMXVenuesResponseHandler() {
            @Override
            public void onSuccess(List<CMXVenue> venues) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                ArrayList<CMXVenue> activityVenues = new ArrayList<CMXVenue>();
                Intent intent = new Intent(context, AllInfoActivity.class);
                activityVenues.addAll(venues);
                intent.putParcelableArrayListExtra(VenueActivity.EXTRA_VENUES, activityVenues);
                intent.putExtra(EXTRA_SERVER_IP, serverAddressText.getText().toString());
                intent.putExtra(EXTRA_SERVER_PORT, serverPortText.getText().toString());
                startActivity(intent);
            }

            @Override
            public void onFailure(Throwable error) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage("Faied To Load Venue: " + error);
                alertDialogBuilder.setPositiveButton(R.string.cmx_ok_dialog_bt, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                alertDialogBuilder.create();
                alertDialogBuilder.show();
            }
        });
    }
    
    public void submitServerList(View v) {
        final EditText serverAddressText = (EditText) findViewById(R.id.editTextServerAddress);
        final EditText serverPortText = (EditText) findViewById(R.id.editTextServerPort);
        CMXClient.Configuration configuration = new CMXClient.Configuration();
        try {
            configuration.setServerAddress(serverAddressText.getText().toString());
            configuration.setServerPort(serverPortText.getText().toString());
        } catch (MalformedURLException ex) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage("Faied To Configure Server: " + ex);
            alertDialogBuilder.setPositiveButton(R.string.cmx_ok_dialog_bt, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            alertDialogBuilder.create();
            alertDialogBuilder.show();
        }
        CMXClient.getInstance().setConfiguration(configuration);
        dialog.setMessage("Loading Venue Information");
        dialog.show();
        CMXClient.getInstance().loadVenues(new CMXVenuesResponseHandler() {
            @Override
            public void onSuccess(List<CMXVenue> venues) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                ArrayList<CMXVenue> activityVenues = new ArrayList<CMXVenue>();
                Intent intent = new Intent(context, VenueActivity.class);
                activityVenues.addAll(venues);
                intent.putParcelableArrayListExtra(VenueActivity.EXTRA_VENUES, activityVenues);
                intent.putExtra(EXTRA_SERVER_IP, serverAddressText.getText().toString());
                intent.putExtra(EXTRA_SERVER_PORT, serverPortText.getText().toString());
                startActivity(intent);
            }

            @Override
            public void onFailure(Throwable error) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setMessage("Faied To Load Venue: " + error);
                alertDialogBuilder.setPositiveButton(R.string.cmx_ok_dialog_bt, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                alertDialogBuilder.create();
                alertDialogBuilder.show();
            }
        });
    }
}
