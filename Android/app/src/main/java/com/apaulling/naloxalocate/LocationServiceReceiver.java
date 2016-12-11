package com.apaulling.naloxalocate;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by psdco on 10/12/2016.
 */
public class LocationServiceReceiver extends WakefulBroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, LocationService.class);

        // Start the service, keeping the device awake while it is launching.
        Log.i("LocationServiceReceiver", "Starting service");
        startWakefulService(context, service);
    }
}
