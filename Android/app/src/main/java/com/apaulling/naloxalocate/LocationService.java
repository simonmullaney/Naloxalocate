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

    private static final String TAG = "LocationService";
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
        mLocationRequest.setInterval(Deodorant.UPDATE_INTERVAL_IN_MS);
        mLocationRequest.setFastestInterval(Deodorant.FASTEST_UPDATE_INTERVAL_IN_MS);
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
                        createWarningNotification("Location off", "Tap to fix", Deodorant.ERROR_LOCATION_REQ_CODE);

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
        targetIntent.putExtra(Deodorant.OPEN_ERROR_DIALOG_INTENT, requestCode);
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
            createWarningNotification("Missing Permission", "Tap to fix", Deodorant.ERROR_PERMISSION_REQ_CODE);
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
        Intent intent = new Intent(Deodorant.UPDATE_UI_INTENT);
        intent.putExtra(Deodorant.DATA_KEY_LAST_UPDATED, data);
        broadcaster.sendBroadcast(intent);
    }

    private void uploadLocation() {
        // Get id to identify this device
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int user_id = prefs.getInt(Deodorant.USER_ID_PERF_KEY, -1);

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
                        prefs.edit().putLong(Deodorant.LAST_UPDATE_PERF_KEY, System.currentTimeMillis()).commit();

                        finishService();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error instanceof NetworkError) {
                            createWarningNotification("Network Error", "Please enable internet access", Deodorant.ERROR_NETWORK_REQ_CODE);
                            finishService();
                        } else {
                            if (error.networkResponse != null && error.networkResponse.data != null) {
                                // response.data is really a byte array
                                error = new VolleyError(new String(error.networkResponse.data));
                            }
                            createWarningNotification("Network Error", error.toString(), Deodorant.ERROR_NETWORK_REQ_CODE);
                        }
                    }
                });

        // Access the RequestQueue through singleton class.
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }
}