package com.cisco.cmx.app.gcm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cisco.cmx.app.DemoLaunchActivity;
import com.cisco.cmx.model.CMXNetwork;
import com.cisco.cmx.model.CMXVenue;
import com.cisco.cmx.network.CMXClient;
import com.cisco.cmx.network.CMXVenuesResponseHandler;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;

public class CMXGcmActivity extends Activity {

    private WifiManager mWiFiManager;

    private List<CMXNetwork> mNetworks;

    private boolean completed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWiFiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        Intent intent = getIntent();
        Bundle networkData = intent.getExtras();
        if (networkData != null) {
            NotificationManager notManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notManager.cancel(CMXGcmIntentService.NOTIFICATION_ID);
        }
        if (networkData != null && networkData.containsKey("preferredNetwork")) {
            parseNetworkData(networkData.getString("preferredNetwork"));
            if (mWiFiManager.isWifiEnabled()) {
                if (!isConnectedToVenueWifi()) {
                    new WaitConnecitonFinish().execute(null, null, null);
                }
                else {
                    moveToMapActivity();
                }
            }
            else {
                new WaitConnecitonFinish().execute(null, null, null);
            }

        }
        else {
            moveToMapActivity();
        }
    }

    private void moveToMapActivity() {
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                Intent intent = new Intent(CMXGcmActivity.this, DemoLaunchActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private class WaitConnecitonFinish extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            if (!mWiFiManager.isWifiEnabled()) {
                mWiFiManager.setWifiEnabled(true);
            }

            addNetworks(mNetworks);
            enableNetworks(mNetworks);

            // This is CMX cloud test
            while (!completed) {
                try {
                    Thread.sleep(2000);
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }

                CMXClient.getInstance().loadVenues(new CMXVenuesResponseHandler() {
                    @Override
                    public void onStart() {
                    }

                    @Override
                    public void onSuccess(List<CMXVenue> venues) {
                        completed = true;
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        completed = false;
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            moveToMapActivity();
            finish();
        }
    }

    private boolean isConnectedToVenueWifi() {
        // Check if we are connected to a venue preferred network
        final WifiInfo connectionInfo = mWiFiManager.getConnectionInfo();
        if (connectionInfo != null && connectionInfo.getSSID() != null) {
            if (mNetworks != null) {
                for (CMXNetwork network : mNetworks) {
                    String ssid = connectionInfo.getSSID();
                    String netw = network.getSSID();
                    if (ssid.equalsIgnoreCase("\"" + netw + "\"")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void addNetworks(List<CMXNetwork> networks) {
        if (networks != null) {
            for (CMXNetwork network : networks) {
                WifiConfiguration wc = new WifiConfiguration();
                wc.SSID = "\"".concat(network.getSSID()).concat("\"");
                addWPANetwork(wc, network.getPassword());
                mWiFiManager.addNetwork(wc);
            }
        }
        mWiFiManager.saveConfiguration();
    }

    private int addWPANetwork(WifiConfiguration configuration, String password) {
        configureWPANetwork(configuration, password);
        return mWiFiManager.addNetwork(configuration);
    }

    private void configureWPANetwork(WifiConfiguration configuration, String password) {
        configuration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        configuration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        configuration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        configuration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        configuration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        configuration.preSharedKey = "\"".concat(password).concat("\"");
    }

    private void enableNetworks(List<CMXNetwork> networks) {
        if (networks != null) {
            List<WifiConfiguration> configurations = mWiFiManager.getConfiguredNetworks();
            if (configurations != null) {
                for (CMXNetwork network : networks) {
                    for (WifiConfiguration configuration : configurations) {
                        if (configuration.SSID.equals("\"" + network.getSSID() + "\"")) {
                            mWiFiManager.enableNetwork(configuration.networkId, true);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void parseNetworkData(String networkData) {
        try {
            JSONArray list = new JSONArray(networkData);
            if (list.length() > 0) {
                if (mNetworks == null) {
                    mNetworks = new ArrayList<CMXNetwork>();
                }
                else {
                    mNetworks.clear();
                }
                for (int i = 0; i < list.length(); i++) {
                    JSONObject data = (JSONObject) list.get(i);
                    CMXNetwork network = new CMXNetwork(data.getString("ssid"), data.getString("password"));
                    mNetworks.add(network);
                }
            }
        }
        catch (JSONException e) {

        }
    }

    final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static void trustAllHosts() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(DO_NOT_VERIFY);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InputStream getInputStream(String params) {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL("http://1.1.1.1/login.html?buttonClicked=4&err_flag=0");

            if (url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                urlConnection = https;
                urlConnection.setDoInput(true);
                // HttpsURLConnection.setFollowRedirects(true);
                urlConnection.connect();
            }
            else {
                urlConnection = (HttpURLConnection) url.openConnection();
                // HttpURLConnection.setFollowRedirects(true);
                urlConnection.connect();
            }
            inputStream = urlConnection.getInputStream();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    public static String convertStreamToString(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                is.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
