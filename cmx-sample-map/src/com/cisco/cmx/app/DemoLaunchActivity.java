package com.cisco.cmx.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.cisco.cmx.network.CMXClient;
import com.cisco.cmx.network.CMXLocationFeedbackResponseHandler;
import com.cisco.cmx.ui.CMXLaunchActivity;
import com.cisco.cmx.ui.CMXSettingsActivity;

public class DemoLaunchActivity extends CMXLaunchActivity implements OnSharedPreferenceChangeListener {

    static final int RESULT_SETTINGS = 1;

    boolean mMustReloadData;

    String ratingValue = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Register preferences listener
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        settings.registerOnSharedPreferenceChangeListener(this);
        mMustReloadData = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMustReloadData) {
            mMustReloadData = false;

            DemoApp app = (DemoApp) this.getApplication();
            CMXClient.getInstance().setConfiguration(app.getConfiguration());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Unregister preferences listener
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        settings.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.launch_actionbar, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_settings:
                showSettings();
                return true;
            case R.id.action_feedback:
                showFeedback();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showFeedback() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater =  this.getLayoutInflater();
        View alertView = inflater.inflate(R.layout.feedback, null);
        final EditText feedbackText = (EditText) alertView.findViewById(R.id.txtFeedback);
        final RatingBar rating = (RatingBar) alertView.findViewById(R.id.ratingBar1);
        
        rating.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
            
           @Override
           public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
               ratingValue = String.valueOf(rating);
           }
       });
        builder.setTitle("Feedback");
        builder.setView(alertView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                CMXClient.getInstance().postClientLocationFeedback(ratingValue, feedbackText.getText().toString(), new FeedbackHanlder());
            }
        });
        
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        
        builder.create().show();
        
   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case RESULT_SETTINGS: {
                CMXClient.getInstance().setConfiguration(((DemoApp) this.getApplication()).getConfiguration());
                break;
            }
        }
    }

    private void showSettings() {
        Intent i = new Intent();
        i.setAction(CMXSettingsActivity.ACTION);
        this.startActivityForResult(i, RESULT_SETTINGS);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {

        if (key.equals("prefBaseUrl") || key.equals("prefSenderId")) {
            mMustReloadData = true;
        }
    }
    
    private class FeedbackHanlder extends CMXLocationFeedbackResponseHandler {
        @Override
        public void onSuccess() {
            Toast abc = Toast.makeText(getApplicationContext(), "Successfully Submitted Feedback", Toast.LENGTH_SHORT);
            abc.show();
        }
        
        @Override
        public void onFailure(Throwable error) {
            Toast abc = Toast.makeText(getApplicationContext(), "Failed To Submit Feedback", Toast.LENGTH_SHORT);
            abc.show();
        }
    }
}