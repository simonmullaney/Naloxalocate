package com.apaulling.naloxalocate.util;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;

/**
 * Created by psdco on 12/12/2016.
 */

public class ErrorHandlerHelper {

    /**
     * Local Vars
     */
    private Context mContext;
    private String TAG;

    /**
     * The calling Activity's context is required for creating dialogs
     */
    public ErrorHandlerHelper(Context context) {
        mContext = context;
        TAG = context.getClass().getSimpleName();
    }

    /**
     * Makes errors prettier and handles WIFI disabled
     * Called in VolleyError handlers
     * @param error used to decide how to display error
     */
    public void handleNetError(VolleyError error) {
        Log.i(TAG, "Error: " + error.toString());
        if (error instanceof NetworkError) {
            // If cannot connect to the internet, prompt to enable WIFI
            createNetErrorDialog();
        } else { // Other type of error. Show this in a toast.
            // Parse error for server response
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
    public void createNetErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                mContext.startActivity(i);
                                dialog.dismiss();
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
    public void createLocationWarningDialog() {
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
     * Asks "Are You Sure"
     *
     * @param dialogClickListener Takes click handler as argument so that this functionality can be customised
     */
    public void createAreSureWarningDialog(DialogInterface.OnClickListener dialogClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("Are You Sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }
}
