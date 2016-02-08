package org.cedric.couchtracker;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by cpriscal on 2/6/16.
 */
public class Service extends android.app.Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String TAG = "CouchTracker";
    private static final String SERVER_ADDRESS = "https://cedric.cloudant.com/bike";
    //private static final String SERVER_ADDRESS = "http://192.168.1.175:5984/motorcycle";
    private static final long MIN_INTERVAL_MS = 5000;
    private static final float MIN_DISTANCE_M = 50;

    private HandlerThread thread;

    private LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Location received");
            // Create the Document
            JSONObject o = new JSONObject();
            try {
                o.put("lon", location.getLongitude());
                o.put("lat", location.getLatitude());
                o.put("time", location.getTime());
                if (location.hasBearing()) {
                    o.put("bearing", location.getBearing());
                }
                if (location.hasSpeed()) {
                    o.put("speed", location.getSpeed());
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            try {
                // Post the document to the server
                //  Open the connection
                URL url = new URL(SERVER_ADDRESS);
                HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
                httpCon.setDoOutput(true);
                //httpCon.setRequestMethod("POST"); // no need to specify POST when using setDoOutput(true)
                //httpCon.setRequestProperty("Accept", "application/json");
                httpCon.setRequestProperty("Content-Type", "application/json");
                OutputStreamWriter out = new OutputStreamWriter(httpCon.getOutputStream());
                //  Send the POST
                Log.d(TAG, o.toString());
                out.write(o.toString());
                out.close();
                //  Check the response
                int ret = httpCon.getResponseCode();
                if (ret >= 400) {
                    Log.e(TAG, "Server " + SERVER_ADDRESS + " returned " + ret);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        // Creating a looper thread to receive location updated
        thread = new HandlerThread("LocationListener");
        thread.start();
        // Start the GPS
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_INTERVAL_MS, MIN_DISTANCE_M, listener, thread.getLooper());
            Log.d(TAG, "Listening for location updates");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        // Stop the GPS
        LocationManager lm = (LocationManager)getSystemService(LOCATION_SERVICE);
        try {
            lm.removeUpdates(listener);
        } catch(SecurityException e) {
            e.printStackTrace();
        }
        // Stop the thread
        thread.quit();
        try {
            thread.join(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // Finish
        super.onDestroy();
    }
}
