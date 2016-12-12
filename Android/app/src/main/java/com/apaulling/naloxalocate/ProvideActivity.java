package com.apaulling.naloxalocate;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by psdco on 08/12/2016.
 */
public class ProvideActivity extends AppCompatActivity {

    protected static final String TAG = "ProvideActivity";

    // Codes from location service
    private static final int LOCATION_SERVICE_REQ_CODE = 1001;
    SharedPreferences prefs;
    private Intent locationServiceReceiverIntent;
    // Views
    private Button btnToggleGPS;
    private Button btnDelete;
    private TextView deviceIdTextView;
    // Local Params
    private int user_id;
    private boolean locationServiceAlarmSet;
    private BroadcastReceiver locServiceReceiver;

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        user_id = prefs.getInt(MainActivity.USER_ID_PERF_KEY, -1);

        btnToggleGPS = (Button) findViewById(R.id.btnToggleGPS);
        btnDelete = (Button) findViewById(R.id.btnDeleteAccount);

        // Register alarm if not registered already
        locationServiceReceiverIntent = new Intent(ProvideActivity.this, LocationServiceReceiver.class);
        locationServiceAlarmSet = (PendingIntent.getBroadcast(ProvideActivity.this, LOCATION_SERVICE_REQ_CODE, locationServiceReceiverIntent, PendingIntent.FLAG_NO_CREATE) != null);
        // Set the text value based on locationServiceAlarmSet
        updateToggleGPSText();
        // Load last gps update time
        long lastUpdated = prefs.getLong(LocationService.LAST_UPDATE_PREF_KEY, 0);
        updateGPSStatus(lastUpdated);

        deviceIdTextView = (TextView) findViewById(R.id.device_id);
        deviceIdTextView.setText(String.format(Locale.ENGLISH, "%d", user_id));

        btnToggleGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If starting alarm, enable location
                if (!locationServiceAlarmSet && !isLocationEnabled(ProvideActivity.this)) {
                    createLocationWarningDialog(ProvideActivity.this);
                } else {
                    AlarmManager alarmManager = (AlarmManager) ProvideActivity.this.getSystemService(Context.ALARM_SERVICE);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(ProvideActivity.this, LOCATION_SERVICE_REQ_CODE, locationServiceReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (!locationServiceAlarmSet) {
                        // No alarm, create it
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 1, pendingIntent); // Millisec * Second * Minute
                        startReceiver();
                    } else {
                        // If the alarm has been set, cancel it
                        alarmManager.cancel(pendingIntent);
                        pendingIntent.cancel();
                        stopReceiver();
                    }

                    locationServiceAlarmSet = !locationServiceAlarmSet;
                    updateToggleGPSText();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAreSureWarningDialog(ProvideActivity.this);
            }
        });

        // Open error dialogue if activity was opened from notification
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int error_req_code = extras.getInt(LocationService.OPEN_ERROR_DIALOG_KEY);
            switch (error_req_code) {
                case LocationService.PERMISSION_ERROR_REQUEST_CODE: {
                    Toast.makeText(this, "Permissions...", Toast.LENGTH_SHORT).show();
                }
                case LocationService.LOCATION_ERROR_REQUEST_CODE: {
                    createLocationWarningDialog(this);
                }
                case LocationService.NETWORK_ERROR_REQUEST_CODE: {
                    createNetErrorDialog();
                }
            }
        }

        // Update the UI if locationService is running
        locServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long lastUpdated = intent.getLongExtra(LocationService.UPDATE_UI_DATA_KEY, 0);
                updateGPSStatus(lastUpdated);
            }
        };

    }

    private void updateGPSStatus(long timeStamp) {
        TextView gpsStatus = (TextView) findViewById(R.id.gps_status);

        if (timeStamp != 0) {
            String lastUpdateStr = DateFormat.getTimeInstance().format(new Date(timeStamp));
            gpsStatus.setText("Last Updated: " + lastUpdateStr);
        } else {
            gpsStatus.setText("Never uploaded");
        }
    }

    private void startReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver((locServiceReceiver),
                new IntentFilter(LocationService.UPDATE_UI_INTENT)
        );
    }

    private void stopReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locServiceReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (locationServiceAlarmSet) {
            startReceiver();
        }
    }

    @Override
    protected void onStop() {
        stopReceiver();
        super.onStop();
    }

    private void updateToggleGPSText() {
        if (locationServiceAlarmSet) {
            btnToggleGPS.setText("Turn Off GPS Tracking");
        } else {
            btnToggleGPS.setText("Turn On GPS Tracking");
        }
    }

    private void deleteDevice() {
        @SuppressLint("DefaultLocale")
        String url = String.format("http://apaulling.com/naloxalocate/api/v1.0/users/%d", user_id);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {

                    @SuppressLint("CommitPrefEdits")
                    @Override
                    public void onResponse(JSONObject response) {
                        // Delete device id
                        prefs.edit().remove("user_id").commit();

                        // Return to home screen
                        finish();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "Error: " + error.toString());
                        if (error instanceof NetworkError) {
                            createNetErrorDialog();
                        } else {
                            VolleyError btrError = new VolleyError(new String(error.networkResponse.data));
                            Toast.makeText(ProvideActivity.this, "Network Error" + btrError.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Access the RequestQueue through singleton class.
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    private void createAreSureWarningDialog(Context context) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        deleteDevice();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure?")
                .setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener)
                .show();
    }

    public void createLocationWarningDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Unable to get location");
        builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
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

    protected void createNetErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(i);
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
}
