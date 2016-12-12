package com.apaulling.naloxalocate;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by psdco on 10/12/2016.
 */

public class LocationService extends IntentService implements ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener {

    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    // The fastest rate for active location updates. Exact. Updates will never be more frequent than this value
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    public static final int PERMISSION_ERROR_REQUEST_CODE = 333;
    public static final int LOCATION_ERROR_REQUEST_CODE = 444;
    public static final int NETWORK_ERROR_REQUEST_CODE = 555;
    public static final String OPEN_ERROR_DIALOG_KEY = "open-dialog-error-key";
    public static final String UPDATE_UI_DATA_KEY = "update-ui-broadcast-key";
    public static final String UPDATE_UI_INTENT = "something-something";
    public static final String LAST_UPDATE_PREF_KEY = "OTHIASDF";

    private static String TAG = "LocationService";
    // Provides the entry point to Google Play services.
    protected GoogleApiClient mGoogleApiClient;
    // Stores parameters for requests to the FusedLocationProviderApi.
    protected LocationRequest mLocationRequest;

    // Represents a geographical location.
    protected Location mCurrentLocation;

    // Intent for releasing WAKE_LOCK
    Intent mIntent;
    private LocalBroadcastManager broadcaster;

    public LocationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mIntent = intent;
        mGoogleApiClient.connect();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        createLocationRequest();

        // For updating provideActivity
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Check location setting
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED: {
                        // Location settings are not satisfied.
                        Log.i(TAG, "Location not turned on");
                        // Prompt to turn it on
                        createWarningNotification("Location off", "Tap to fix", LOCATION_ERROR_REQUEST_CODE);

                        finishService();
                    }
                }
            }
        });
    }

    void finishService() {
        Log.i(TAG, "Finished");
        LocationServiceReceiver.completeWakefulIntent(mIntent);
    }

    private void createWarningNotification(String title, String content, int requestCode) {
        int NOTIFICATION_ID = 12345;

        // Add notification contents
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                        .setSmallIcon(R.drawable.ic_stat_app_icon_status_bar)
                        .setContentTitle(title)
                        .setContentText(content);

        // Set tap target activity
        Intent targetIntent = new Intent(this, ProvideActivity.class);
        targetIntent.putExtra(OPEN_ERROR_DIALOG_KEY, requestCode);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, targetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        // Notify
        NotificationManager nManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "No have permissions!");
            createWarningNotification("Missing Permission", "Tap to fix", PERMISSION_ERROR_REQUEST_CODE);
            finishService();
        } else {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            // Check if previous location is available
            if (mCurrentLocation != null) {
                uploadLocation();
            }
            // Wait for new location
            else {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        // Got updated location. Can stop asking for it.
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mCurrentLocation = location;
        // Send coordinates to server
        uploadLocation();
    }

    public void sendBroadcast(long data) {
        Intent intent = new Intent(UPDATE_UI_INTENT);
        intent.putExtra(UPDATE_UI_DATA_KEY, data);
        broadcaster.sendBroadcast(intent);
    }

    private void uploadLocation() {
        // Get id to identify this device
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int user_id = prefs.getInt(MainActivity.USER_ID_PERF_KEY, -1);

        @SuppressLint("DefaultLocale")
        String url = String.format("http://apaulling.com/naloxalocate/api/v1.0/users/%d", user_id);

        // Data to be sent to the server
        HashMap<String, String> params = new HashMap<>();
        params.put("latitude", Double.toString(mCurrentLocation.getLatitude()));
        params.put("longitude", Double.toString(mCurrentLocation.getLongitude()));
        params.put("accuracy", Double.toString(mCurrentLocation.getAccuracy()));
        params.put("last_updated", Long.toString(System.currentTimeMillis()));
        Log.i(TAG, params.toString());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.PUT, url, new JSONObject(params), new Response.Listener<JSONObject>() {

                    @SuppressLint("CommitPrefEdits")
                    @Override
                    public void onResponse(JSONObject response) {
                        // Broadcast if listening
                        sendBroadcast(System.currentTimeMillis());
                        // Save for next time screen is opened
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        prefs.edit().putLong(LAST_UPDATE_PREF_KEY, System.currentTimeMillis()).commit();

                        finishService();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError) {
                            createWarningNotification("Network Error", "Please enable internet access", NETWORK_ERROR_REQUEST_CODE);
                            finishService();
                        } else {
                            VolleyError btrError = new VolleyError(new String(error.networkResponse.data));
                            createWarningNotification("Network Error", btrError.toString(), NETWORK_ERROR_REQUEST_CODE);
                        }
                    }
                });

        // Access the RequestQueue through singleton class.
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }
}