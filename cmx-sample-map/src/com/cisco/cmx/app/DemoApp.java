package com.cisco.cmx.app;

import java.net.MalformedURLException;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.cisco.cmx.network.CMXClient;

public class DemoApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Set default values preferences
        PreferenceManager.setDefaultValues(this, R.xml.cmx_settings, true);

        CMXClient.getInstance().initialize(this);
        CMXClient.getInstance().setConfiguration(getConfiguration());
    }

    public CMXClient.Configuration getConfiguration() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        String serverAddress = settings.getString("prefServerAddress", getResources().getString(R.string.default_settings_server_address));
        String senderId = settings.getString("prefSenderId", getResources().getString(R.string.default_settings_senderid));

        try {
            CMXClient.Configuration config = new CMXClient.Configuration();
            if (CMXClient.getInstance().getServerAddress() != null) {
                if (!serverAddress.equals(CMXClient.getInstance().getServerAddress())) {
                    CMXClient.getInstance().resetClientRegistration();
                }
            }
            config.setServerAddress(serverAddress);
            config.setSenderId(senderId);
            return config;
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
