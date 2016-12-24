package com.apaulling.naloxalocate.activities;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
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
import com.apaulling.naloxalocate.util.Consts;
import com.apaulling.naloxalocate.util.ErrorHandlerHelper;
import com.apaulling.naloxalocate.util.LocationHelper;
import com.apaulling.naloxalocate.util.RequestSingleton;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

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
public class FindActivity extends FragmentActivity implements LocationHelper.Interface, OnMapReadyCallback {

    private static final String TAG = "FindActivity";

    // Keys for storing activity state in the Bundle.
    private final static String LOCATION_KEY = "location-key";
    private final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private final static int DEFAULT_ZOOM = 20;

    // Instances of helper functions
    private ErrorHandlerHelper mErrorHandlerHelper; // helps with creating error dialogs
    private LocationHelper mLocHelper; // helps with location

    // UI Elements
    private CoordinatorLayout coordinatorLayout;
    private TextView mLatitudeText;
    private TextView mLongitudeText;
    private TextView mLastUpdateTimeText;
    private TextView listEmptyText;
    private Snackbar snackbar; // to show if updates are enabled

    // Time when the location was updated represented as a String.
    private String mLastUpdateTime = "";
    // Prevent dialog showing twice is both internet and gps are off
    private boolean netErrorShowing = false;

    // Google Maps Map
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    //
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    boolean mapSet = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);

        mErrorHandlerHelper = new ErrorHandlerHelper(this);
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

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
                mLocHelper.setCurrentLocation((Location) savedInstanceState.getParcelable(LOCATION_KEY));
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
                updateUI();
            }
        }
    }


    /**
     * LocationHelper Interface methods
     */
    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected() {
        // Null is location not requested since reboot
        // Wouldn't be null if loaded from bundle
        if (mLocHelper.getCurrentLocation() == null) {
            mLocHelper.getLastLocation();
            setLastUpdateTime();
            updateUIAndGetNearbyUsers();
        }

        // Start periodic location updates.
        mLocHelper.startLocationUpdates();

        if (snackbar != null && snackbar.isShown()) {
            snackbar.dismiss();
        }
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
     * Callback handled in onActivityResult method
     */
    @Override
    public void locationSettingsResultCallback(Status status) {
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // Location settings are not satisfied. Show the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(FindActivity.this, Consts.SETTINGS_LOCATION_ENABLE_REQ_CODE);
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
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Consts.PERMISSION_LOCATION_START_REQ_CODE);
    }

    /**
     * Updates my location text and get users if location is available
     */
    private void updateUIAndGetNearbyUsers() {
        if (mLocHelper.getCurrentLocation() == null) {
            updateUIWaiting();
        } else {
            updateUI();
            getNearbyUsers();

            if (mMap != null && !mapSet) {
                mapSet = true;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(mLocHelper.getCurrentLocation().getLatitude(),
                                mLocHelper.getCurrentLocation().getLongitude()), DEFAULT_ZOOM));
            } else {
                Log.i(TAG, "Map was null");
            }
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
        mLatitudeText.setText(String.format(Locale.ENGLISH, "Lat: %f", mLocHelper.getCurrentLocation().getLatitude()));
        mLongitudeText.setText(String.format(Locale.ENGLISH, "Long: %f", mLocHelper.getCurrentLocation().getLongitude()));
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
            case Consts.PERMISSION_LOCATION_START_REQ_CODE:
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
        mLocHelper.getGoogleApiClient().connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mLocHelper.getGoogleApiClient().isConnected()) {
            mLocHelper.stopLocationUpdates();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mLocHelper.getGoogleApiClient().isConnected()) {
            mLocHelper.startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLocHelper.getGoogleApiClient().isConnected()) {
            mLocHelper.getGoogleApiClient().disconnect();
        }
    }

    /**
     * Stores activity data in the Bundle.
     */
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mLocHelper.getCurrentLocation());
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
            case Consts.SETTINGS_LOCATION_ENABLE_REQ_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK: {
                        mLocHelper.onConnected(null);
                        break;
                    }
                    case Activity.RESULT_CANCELED: {
                        // If no location, then finish activity.
                        // Otherwise it will operate on old location
                        if (mLocHelper.getCurrentLocation() == null) {
                            finish();
                        }
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
                mLocHelper.getCurrentLocation().getLatitude(), mLocHelper.getCurrentLocation().getLongitude());

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

                            // Dialog not showing
                            netErrorShowing = false;
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
                        if (!netErrorShowing) {
                            mErrorHandlerHelper.handleNetError(error);
                            netErrorShowing = true;
                        }
                    }
                });

        // Access the RequestQueue through singleton class.
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    /**
     * Convert JSON to ArrayList of NearbyUser objects. public for test
     * @param users json string will be parsed from server as array. If len == 0, this isnt reached
     * @return a list of NearbyUser objects. Passed to list adapter
     * @throws JSONException will be thrown if param users cannot be converted to array
     */
    public ArrayList<NearbyUser> JSONToNearbyUsers(JSONArray users) throws JSONException {
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
        if (snackbar == null) {
            snackbar = Snackbar.make(coordinatorLayout, "Location Updates Paused", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("RETRY", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Attempt to restart location updates.
                    // Will fail again if internet or location unavailable and snackbar will reappear
                    mLocHelper.startLocationUpdates();
                    snackbar.dismiss();
                }
            });
            snackbar.setActionTextColor(Color.RED); // Changing message text color
        }
        snackbar.show();
    }

    /**
     * Custom list adapter
     */
    private void addUsersToList(ArrayList<NearbyUser> listItems) {
        FindListAdapter mAdapter = new FindListAdapter(FindActivity.this, R.layout.activity_find_list_item, listItems);
        ListView list = (ListView) findViewById(R.id.users_nearby_list);
        list.setAdapter(mAdapter);

        // Display dialog when list row is clicked
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Get selected provider's id
                NearbyUser user = (NearbyUser) adapterView.getItemAtPosition(i);
                Toast.makeText(FindActivity.this, "User ID Selected: " + Integer.toString(user.getId()), Toast.LENGTH_SHORT).show();

                // Get a layout inflater
                LayoutInflater inflater = FindActivity.this.getLayoutInflater();
                // Build dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(FindActivity.this);
                builder.setMessage("Notify Provider")
                        .setView(inflater.inflate(R.layout.dialog_contact_provider, null))
                        .setPositiveButton("Contact", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                Toast.makeText(FindActivity.this, "Nothing was done", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and show it
                builder.create();
                builder.show();
            }
        });
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    @SuppressWarnings("MissingPermission")
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Add markers for nearby places.
//        updateMarkers();

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents, null);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());

                return infoWindow;
            }
        });
        /*
         * Set the map's camera position to the current location of the device.
         * If the previous state was saved, set the position to the saved state.
         * If the current location is unknown, use a default position and zoom value.
         */
        if (mCameraPosition != null) {
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLocHelper.getCurrentLocation() != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLocHelper.getCurrentLocation().getLatitude(),
                            mLocHelper.getCurrentLocation().getLongitude()), DEFAULT_ZOOM));
        } else {
            Log.d(TAG, "Current location is null. Using defaults.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }
}
