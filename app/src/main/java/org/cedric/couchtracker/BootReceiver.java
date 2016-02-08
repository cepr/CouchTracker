package org.cedric.couchtracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by cpriscal on 2/6/16.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "CouchTracker";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Starting CouchTracker");
        Intent i = new Intent();
        i.setClass(context, Service.class);
        context.startService(i);
    }
}
