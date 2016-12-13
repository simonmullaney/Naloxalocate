package com.apaulling.naloxalocate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;

/**
 * Created by psdco on 12/12/2016.
 */

class Deodorant {

    // MainActivity
    static final int PERMISSION_LOCATION_FIND_REQ_CODE = 11;
    static final int PERMISSION_LOCATION_PROVIDE_REQ_CODE = 22;
    static final String USER_ID_PERF_KEY = "user_id";

    // Find Activity Request Codes
    static final int PERMISSION_LOCATION_START_REQ_CODE = 111;
    static final int SETTINGS_LOCATION_ENABLE_REQ_CODE = 222;

    // Provide Activity Req
    static final int LOCATION_SERVICE_INTENT_REQ_CODE = 1111;
    static final int LOCATION_SERVICE_REPEAT_TIME = 1000 * 60 * 1;

    // Location Service
    static final int ERROR_PERMISSION_REQ_CODE = 333;
    static final int ERROR_LOCATION_REQ_CODE = 444;
    static final int ERROR_NETWORK_REQ_CODE = 555;
    static final String OPEN_ERROR_DIALOG_INTENT = "open-dialog-error-key";
    static final String DATA_KEY_LAST_UPDATED = "update-ui-broadcast-key";
    static final String UPDATE_UI_INTENT = "something-something";
    static final String LAST_UPDATE_PERF_KEY = "keys-open-doors";

    // The desired interval for location updates. Inexact. Updates may be more or less frequent.
    static final long UPDATE_INTERVAL_IN_MS = 10000;
    // The fastest rate for active location updates. Exact. Updates will never be more frequent than this value
    static final long FASTEST_UPDATE_INTERVAL_IN_MS = UPDATE_INTERVAL_IN_MS / 2;

    /**
     * Local Vars
     */
    private Context mContext;
    private String TAG;

    /**
     * The calling Activity's context is required for creating dialogs
     */
    Deodorant(Context context) {
        mContext = context;
        TAG = context.getClass().getSimpleName();
    }

    /**
     * Makes errors prettier and handles WIFI disabled
     * Called in VolleyError handlers
     */
    void handleNetError(VolleyError error) {
        Log.i(TAG, "Error: " + error.toString());
        if (error instanceof NetworkError) {
            createNetErrorDialog();
        } else {
            if (error.networkResponse != null && error.networkResponse.data != null) {
                // response.data is really a byte array
                error = new VolleyError(new String(error.networkResponse.data));
            }
            Toast.makeText(mContext, "Network Error" + error.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Prompt to enable WIFI
     */
    void createNetErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                mContext.startActivity(i);
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Prompt to enable location services
     */
    void createLocationWarningDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Unable to get location");
        builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(myIntent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Asks "Are you sure"
     *
     * @param dialogClickListener Takes click handler as argument so that this functionality can be customised
     */
    void createAreSureWarningDialog(DialogInterface.OnClickListener dialogClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    /*
    * Gets location status
    */
    boolean isLocationEnabled() {
        int locationMode;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(mContext.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
}
