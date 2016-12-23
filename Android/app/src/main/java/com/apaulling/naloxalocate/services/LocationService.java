package com.apaulling.naloxalocate.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apaulling.naloxalocate.R;
import com.apaulling.naloxalocate.activities.ProvideActivity;
import com.apaulling.naloxalocate.util.Consts;
import com.apaulling.naloxalocate.util.LocationHelper;
import com.apaulling.naloxalocate.util.RequestSingleton;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by psdco on 10/12/2016.
 */

public class LocationService extends IntentService implements LocationHelper.Interface {

    private static final String TAG = "LocationService";

    private Intent mIntent;
    private LocalBroadcastManager broadcaster;
    private LocationHelper mLocHelper;

    // Constructor that passes TAG to base class
    public LocationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mIntent = intent;
        // Start location
        mLocHelper.getGoogleApiClient().connect();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLocHelper = new LocationHelper(TAG, this, this);

        // For updating ProvideActivity
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected() {
        mLocHelper.getLastLocation();
        // Check if previous location is available. Wouldn't after a reboot.
        if (mLocHelper.getCurrentLocation() != null) {
            uploadLocation();
        }
        // Wait for new location
        else {
            mLocHelper.startLocationUpdates();
        }
    }

    /**
     * Callback that fires when the location changes
     * Stops updates and sends result to server
     */
    @Override
    public void onLocationChanged() {
        // Got updated location. Can stop asking for it.
        mLocHelper.stopLocationUpdates();
        // Send coordinates to server
        uploadLocation();
    }

    /**
     * Notify if no location permission
     */
    @Override
    public void handleNoLocationPermission() {
        Log.i(TAG, "No have permissions!");
        createNotification("Missing Permission", "Touch to fix", Consts.ERROR_PERMISSION_REQ_CODE);
        finishService();
    }

    /**
     * Notify if location disabled
     */
    @Override
    public void locationSettingsResultCallback(Status status) {
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                // Location settings are not satisfied.
                Log.i(TAG, "Location not turned on");
                // Prompt to turn it on
                createNotification("Location off", "Touch to fix", Consts.ERROR_LOCATION_REQ_CODE);

                finishService();
            }
        }
    }

    /**
     * Uploads GPS coordinates associate with this user id
     */
    private void uploadLocation() {
        // Get id to identify this device
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int user_id = prefs.getInt(Consts.USER_ID_PERF_KEY, -1);
        if (user_id == -1) { // just in case
            return;
        }

        @SuppressLint("DefaultLocale")
        String url = String.format("http://apaulling.com/naloxalocate/api/v1.0/users/%d", user_id);

        // Data to be sent to the server
        HashMap<String, String> params = new HashMap<>();
        params.put("latitude", Double.toString(mLocHelper.getCurrentLocation().getLatitude()));
        params.put("longitude", Double.toString(mLocHelper.getCurrentLocation().getLongitude()));
        params.put("accuracy", Double.toString(mLocHelper.getCurrentLocation().getAccuracy()));
        params.put("last_updated", Long.toString(System.currentTimeMillis()));
        Log.i(TAG, params.toString());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, new JSONObject(params), new Response.Listener<JSONObject>() {

                    @SuppressLint("CommitPrefEdits")
                    @Override
                    public void onResponse(JSONObject response) {
                        // Destroy notification is service beings to work again
                        destroyNotification();

                        // Broadcast this time so that if Provide is listening, it can update
                        sendBroadcast(System.currentTimeMillis());

                        // Save for next time screen is opened
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        prefs.edit().putLong(Consts.LAST_UPDATE_PERF_KEY, System.currentTimeMillis()).commit();

                        finishService();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError) {
                            createNotification("Network Error", "Please enable internet access", Consts.ERROR_NETWORK_REQ_CODE);
                            finishService();
                        } else {
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                // response.data is really a byte array
                                error = new VolleyError(new String(error.networkResponse.data));
                            }
                            createNotification("Network Error", error.toString(), Consts.ERROR_NETWORK_REQ_CODE);
                        }
                    }
                });

        // Access the RequestQueue through singleton class.
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    /**
     * Tell the ProvideActivity, if it's open, last update time.
     * @param data last update time (unix timestamp) in milliseconds
     */
    public void sendBroadcast(long data) {
        Intent intent = new Intent(Consts.UPDATE_UI_INTENT_KEY);
        intent.putExtra(Consts.DATA_KEY_LAST_UPDATED, data);
        broadcaster.sendBroadcast(intent);
    }

    /**
     * Create notification that when tapped will open the Provide activity
     * @param title       The type of error
     * @param content     Tell user what to do
     * @param requestCode allows provide activity to show corresponding prompt
     */
    private void createNotification(String title, String content, int requestCode) {
        // Add notification contents
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_app_icon_status_bar)
                        .setContentTitle(title)
                        .setContentText(content)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                        .setAutoCancel(true);

        // Set tap target activity
        Intent targetIntent = new Intent(this, ProvideActivity.class);
        targetIntent.putExtra(Consts.OPEN_ERROR_DIALOG_INTENT_KEY, requestCode);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Notify
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(Consts.ERROR_NOTIFICATION_ID, builder.build());
    }

    /**
     * Clear notification. Used to automatically clear if a successful request occurs without being opened
     */
    private void destroyNotification() {
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.cancel(Consts.ERROR_NOTIFICATION_ID);
    }

    /**
     * Release wake lock
     */
    void finishService() {
        Log.i(TAG, "Finished");
        LocationServiceReceiver.completeWakefulIntent(mIntent);
    }
}