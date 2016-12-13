package com.apaulling.naloxalocate;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by psdco on 08/12/2016.
 */
public class FindActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    private static final String TAG = "FindActivity";

    // Keys for storing activity state in the Bundle.
    private final static String LOCATION_KEY = "location-key";
    private final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    Deodorant mHelper;
    // Provides the entry point to Google Play services.
    private GoogleApiClient mGoogleApiClient;
    // Stores parameters for requests to the FusedLocationProviderApi.
    private LocationRequest mLocationRequest;
    // Represents a geographical location.
    private Location mCurrentLocation;
    // Time when the location was updated represented as a String.
    // UI Labels
    private CoordinatorLayout coordinatorLayout;
    private String mLastUpdateTime;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mLastUpdateTimeText;
    private TextView listEmptyText;
    /**
     * Following broadcast receiver is to listen to the Location button toggle state in Android.
     */
    private BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                // Make an action or refresh an already managed state.
                try {
                    int locationMode = 0;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                        locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
                    }
                    if (locationMode == android.provider.Settings.Secure.LOCATION_MODE_OFF) {
                        Toast.makeText(FindActivity.this, "Oii! Turn it back on!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Settings.SettingNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        mHelper = new Deodorant(this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));
        mLastUpdateTimeText = (TextView) findViewById(R.id.last_update_time_text);

        mLastUpdateTime = "";

        // Set list empty text
        ListView list = (ListView) findViewById(R.id.users_nearby_list);
        listEmptyText = (TextView) findViewById(R.id.activity_find_list_empty_text);
        list.setEmptyView(listEmptyText);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Register listener of location state change
//        This causes a memory leak i think
//        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));

        // Kick off the process of building a GoogleApiClient and requesting the LocationServices API.
        buildGoogleApiClient();
        createLocationRequest();
        // Ask user to turn on location
        locationSettingsRequest();
    }

    /**
     * Updates fields based on data stored in the bundle.
     *
     * @param savedInstanceState The activity state saved in the Bundle.
     */
    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
                updateUI();
            }
        }
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

    /**
     * Sets up the location request.
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(Deodorant.UPDATE_INTERVAL_IN_MS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(Deodorant.FASTEST_UPDATE_INTERVAL_IN_MS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Prompt user to enable location settings
     */
    // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsApi
    public void locationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. Show the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(FindActivity.this, Deodorant.SETTINGS_LOCATION_ENABLE_REQ_CODE);
                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Toast.makeText(FindActivity.this, "Could not enable location", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
            }
        });
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Deodorant.PERMISSION_LOCATION_START_REQ_CODE);
        } else {
            if (mCurrentLocation == null) {
                mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
                updateUIAndGetNearbyUsers();
            }

            // Start periodic location updates.
            startLocationUpdates();
        }
    }

    /**
     * Updates my location text and get users if location is available
     */
    public void updateUIAndGetNearbyUsers() {
        if (mCurrentLocation == null) {
            updateUIWaiting();
        } else {
            updateUI();
            getNearbyUsers();
        }
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        mLatitudeText.setText(String.format(Locale.ENGLISH, "Lat: %f", mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.format(Locale.ENGLISH, "Long: %f", mCurrentLocation.getLongitude()));
        mLastUpdateTimeText.setText(String.format("Last Updated: %s", mLastUpdateTime));
    }

    /**
     * Updates current status to show that location is not yet available.
     * Can happen after device boot
     */
    private void updateUIWaiting() {
        Log.i(TAG, "UpdateUI null location");
        mLatitudeText.setText("Waiting for location...");
        mLongitudeText.setText("");
        mLastUpdateTimeText.setText("");
    }

    /**
     * Removes location updates from the FusedLocationApi.
     * Must first check permissions
     */
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Deodorant.PERMISSION_LOCATION_START_REQ_CODE);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    /**
     * Location Permissions Dialogue Box callback
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Deodorant.PERMISSION_LOCATION_START_REQ_CODE:
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    this.onConnected(null);
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Callback that fires when the location changes.
     */
    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        updateUIAndGetNearbyUsers();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    /**
     * Stores activity data in the Bundle.
     */
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Callback for prompting user to enable location settings
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case Deodorant.SETTINGS_LOCATION_ENABLE_REQ_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        this.onConnected(null);
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        finish();
                        break;
                    }
                }
                break;
        }
    }

    /**
     * Getting and showing list of users
     */
    private void getNearbyUsers() {
        String url = String.format(Locale.ENGLISH, "http://apaulling.com/naloxalocate/api/v1.0/users?latitude=%f&longitude=%f",
                mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Do something with data
                        try {
                            // Get the users(id, relative_dist) from the response
                            JSONArray users = response.getJSONArray("users");
                            if (users.length() == 0) {
                                listEmptyText.setText("No Nearby Carriers");
                            }

                            // Convert from JSON to NearbyUser Objects
                            ArrayList<NearbyUser> nearbyUsers = JSONToNearbyUsers(users);

                            // Add users to UI after converting from JSON
                            addUsersToList(nearbyUsers);
                        } catch (JSONException e) {
                            Toast.makeText(FindActivity.this, "JSON Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Network error so stop trying to receive location updates
                        stopLocationUpdates();
                        // Indicate that updates have stopped and prompt to retry
                        showLocUpdatesOffSnackBar();

                        mHelper.handleNetError(error);
                    }
                });

        // Access the RequestQueue through singleton class.
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    /**
     * Convert JSON to ArrayList of NearbyUser objects
     */
    private ArrayList<NearbyUser> JSONToNearbyUsers(JSONArray users) throws JSONException {
        ArrayList<NearbyUser> nearbyUsers = new ArrayList<>(users.length());
        for (int i = 0; i < users.length(); i++) {
            JSONArray user = users.getJSONArray(i);
            nearbyUsers.add(new NearbyUser(user.getInt(0), user.getDouble(1)));
        }
        return nearbyUsers;
    }

    /**
     * Notify user updates have stopped.
     */
    private void showLocUpdatesOffSnackBar() {
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, "Location Updates Stopped!", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocationUpdates();
                snackbar.dismiss();
            }
        });

        // Changing message text color
        snackbar.setActionTextColor(Color.RED);
        snackbar.show();
    }

    /**
     * Custom list adapter
     */
    void addUsersToList(ArrayList<NearbyUser> listItems) {
        FindListAdapter mAdapter = new FindListAdapter(FindActivity.this, R.layout.activity_find_list_item, listItems);
        ListView list = (ListView) findViewById(R.id.users_nearby_list);
        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                NearbyUser user = (NearbyUser) adapterView.getItemAtPosition(i);
                Toast.makeText(FindActivity.this, "User ID Selcted: " + Integer.toString(user.getId()), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
