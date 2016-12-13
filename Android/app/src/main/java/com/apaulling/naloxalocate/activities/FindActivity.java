package com.apaulling.naloxalocate.activities;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import com.apaulling.naloxalocate.R;
import com.apaulling.naloxalocate.adapters.FindListAdapter;
import com.apaulling.naloxalocate.adapters.NearbyUser;
import com.apaulling.naloxalocate.util.Deodorant;
import com.apaulling.naloxalocate.util.LocationHelper;
import com.apaulling.naloxalocate.util.RequestSingleton;
import com.google.android.gms.common.api.Status;
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
public class FindActivity extends AppCompatActivity implements LocationHelper.Interface {

    private static final String TAG = "FindActivity";
    // Keys for storing activity state in the Bundle.
    private final static String LOCATION_KEY = "location-key";
    private final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    // Instances of helper functions
    private Deodorant mSmellsHelper; // helps with creating error dialogs
    private LocationHelper mLocHelper; // helps with location
    // UI Labels
    private CoordinatorLayout coordinatorLayout;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mLastUpdateTimeText;
    private TextView listEmptyText;

    // Time when the location was updated represented as a String.
    private String mLastUpdateTime = "";
    /**
     * NOT USED. CAUSED MEMORY LEAK. May fix in future
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

        mSmellsHelper = new Deodorant(this);
        mLocHelper = new LocationHelper(TAG, this, this);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mLatitudeText = (TextView) findViewById((R.id.latitude_text));
        mLongitudeText = (TextView) findViewById((R.id.longitude_text));
        mLastUpdateTimeText = (TextView) findViewById(R.id.last_update_time_text);

        // Set list empty text
        ListView list = (ListView) findViewById(R.id.users_nearby_list);
        listEmptyText = (TextView) findViewById(R.id.activity_find_list_empty_text);
        list.setEmptyView(listEmptyText);

        // Update values using data stored in the Bundle.
        updateValuesFromBundle(savedInstanceState);

        // Caused a memory leak, I think. May use in future
        // Register listener of location state change
//        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }


    /** LocationHelper Interface methods */

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
                mLocHelper.mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
                updateUI();
            }
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected() {
        // Null is location not requested since reboot
        // Wouldn't be null if loaded from bundle
        if (mLocHelper.mCurrentLocation == null) {
            mLocHelper.getLastLocation();
            setLastUpdateTime();
            updateUIAndGetNearbyUsers();
        }

        // Start periodic location updates.
        mLocHelper.startLocationUpdates();
    }

    /**
     * Callback that fires when the location changes
     */
    @Override
    public void onLocationChanged() {
        setLastUpdateTime();
        updateUIAndGetNearbyUsers();
    }

    /**
     * Callback if no location permission
     * Creates Google like prompt to enable GPS, if that's possible
     */
    @Override
    public void locationSettingsResultCallback(Status status) {
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

    /**
     * Present permission dialog
     */
    @Override
    public void handleNoLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Deodorant.PERMISSION_LOCATION_START_REQ_CODE);
    }

    /**
     * Updates my location text and get users if location is available
     */
    public void updateUIAndGetNearbyUsers() {
        if (mLocHelper.mCurrentLocation == null) {
            updateUIWaiting();
        } else {
            updateUI();
            getNearbyUsers();
        }
    }

    /**
     * Update the lastUpdates time. Only called when new location is available.
     * mLastUpdateTime also changed when loading from bundle.
     */
    private void setLastUpdateTime() {
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    /**
     * Updates the latitude, the longitude, and the last location time in the UI.
     */
    private void updateUI() {
        mLatitudeText.setText(String.format(Locale.ENGLISH, "Lat: %f", mLocHelper.mCurrentLocation.getLatitude()));
        mLongitudeText.setText(String.format(Locale.ENGLISH, "Long: %f", mLocHelper.mCurrentLocation.getLongitude()));
        mLastUpdateTimeText.setText(String.format("Last Updated: %s", mLastUpdateTime));
    }

    /**
     * Updates current status to indicate location is not yet available.
     * Can happen after device boot
     */
    private void updateUIWaiting() {
        Log.i(TAG, "UpdateUI null location");
        mLatitudeText.setText("Waiting for location...");
        mLongitudeText.setText("");
        mLastUpdateTimeText.setText("");
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
                    mLocHelper.onConnected(null);
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocHelper.mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mLocHelper.mGoogleApiClient.isConnected()) {
            mLocHelper.stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mLocHelper.mGoogleApiClient.isConnected()) {
            mLocHelper.startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocHelper.mGoogleApiClient.isConnected()) {
            mLocHelper.mGoogleApiClient.disconnect();
        }
    }

    /**
     * Stores activity data in the Bundle.
     */
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mLocHelper.mCurrentLocation);
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
                        mLocHelper.onConnected(null);
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
                mLocHelper.mCurrentLocation.getLatitude(), mLocHelper.mCurrentLocation.getLongitude());

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        // Do something with data
                        try {
                            // Get the users(id, relative_dist) from the response
                            JSONArray users = response.getJSONArray("users");
                            if (users.length() == 0) {
                                listEmptyText.setText(R.string.find_empty_list_text);
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
                        mLocHelper.stopLocationUpdates();
                        // Indicate that updates have stopped and prompt to retry
                        showLocUpdatesOffSnackBar();

                        mSmellsHelper.handleNetError(error);
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
        final Snackbar snackbar = Snackbar.make(coordinatorLayout, "Location Updates Paused", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("RETRY", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Attempt to restart location updates.
                // Will fail again if internet or location unavailable and snackbar will reappear
                mLocHelper.startLocationUpdates();
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
