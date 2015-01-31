package com.cisco.cmx.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;
import com.cisco.cmx.network.CMXClient;
import com.cisco.cmx.network.CMXLocationFeedbackResponseHandler;
import com.cisco.cmx.ui.CMXMainActivity;

/**
 * Activity that manages fragments to load & display maps from CMX server, with
 * left & right menus.
 */
public class DemoCMXMainActivity extends CMXMainActivity {
    
    String ratingValue = "";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_feedback) {
            showFeedback();
            return true;
        }
        else {
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