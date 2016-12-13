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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

    private SharedPreferences prefs;
    private Intent locationServiceReceiverIntent;
    private BroadcastReceiver locServiceReceiver;
    private Deodorant mHelper;

    // Views
    private Button btnToggleGPS;

    // Local Params
    private int user_id;
    private boolean locationServiceAlarmSet;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide);

        mHelper = new Deodorant(this);

        prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        // Load last GPS update time and update the message
        long lastUpdated = prefs.getLong(Deodorant.LAST_UPDATE_PERF_KEY, 0);
        updateGPSStatusText(lastUpdated);

        // Update device ID field
        user_id = prefs.getInt(Deodorant.USER_ID_PERF_KEY, -1);
        TextView deviceIdTextView = (TextView) findViewById(R.id.device_id);
        deviceIdTextView.setText(String.format(Locale.ENGLISH, "%d", user_id));

        // Check if alarm is registered and set button text accordingly
        locationServiceReceiverIntent = new Intent(ProvideActivity.this, LocationServiceReceiver.class);
        locationServiceAlarmSet = (PendingIntent.getBroadcast(ProvideActivity.this, Deodorant.LOCATION_SERVICE_INTENT_REQ_CODE, locationServiceReceiverIntent, PendingIntent.FLAG_NO_CREATE) != null);
        btnToggleGPS = (Button) findViewById(R.id.btn_toggle_GPS);
        updateToggleGPSText();

        /**
         * Btn Click Listeners
         */
        btnToggleGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If starting alarm, enable location
                if (!locationServiceAlarmSet && !mHelper.isLocationEnabled()) {
                    mHelper.createLocationWarningDialog();
                } else {
                    AlarmManager alarmManager = (AlarmManager) ProvideActivity.this.getSystemService(Context.ALARM_SERVICE);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(ProvideActivity.this, Deodorant.LOCATION_SERVICE_INTENT_REQ_CODE, locationServiceReceiverIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    if (!locationServiceAlarmSet) {
                        // No alarm, create it
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), Deodorant.LOCATION_SERVICE_REPEAT_TIME, pendingIntent); // Millisec * Second * Minute
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

        Button btnDelete = (Button) findViewById(R.id.btn_delete_account);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                deleteDevice();
                                break;
                        }
                    }
                };
                // Create "are you sure" with custom on click
                mHelper.createAreSureWarningDialog(dialogClickListener);
            }
        });

        // Open error dialogue if activity was opened from notification
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int error_req_code = extras.getInt(Deodorant.OPEN_ERROR_DIALOG_INTENT);
            switch (error_req_code) {
                case Deodorant.ERROR_PERMISSION_REQ_CODE:
                    Toast.makeText(this, "Permissions...", Toast.LENGTH_SHORT).show();
                    break;
                case Deodorant.ERROR_LOCATION_REQ_CODE:
                    mHelper.createLocationWarningDialog();
                    break;
                case Deodorant.ERROR_NETWORK_REQ_CODE:
                    mHelper.createNetErrorDialog();
                    break;
            }
        }

        // Receive updates to the UI if locationService is running
        locServiceReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long lastUpdated = intent.getLongExtra(Deodorant.DATA_KEY_LAST_UPDATED, 0);
                updateGPSStatusText(lastUpdated);
            }
        };

    }

    /**
     * Change last updated time, if it exists
     */
    private void updateGPSStatusText(long timeStamp) {
        if (timeStamp > 0) {
            TextView gpsStatus = (TextView) findViewById(R.id.gps_status);
            String lastUpdateStr = DateFormat.getTimeInstance().format(new Date(timeStamp));
            gpsStatus.setText(String.format("Last Updated: %s", lastUpdateStr));
        }
    }

    /**
     * Toggle GPS btn text depending on if LocationService Alarm is set
     */
    private void updateToggleGPSText() {
        if (locationServiceAlarmSet) {
            btnToggleGPS.setText(R.string.gps_off_text);
        } else {
            btnToggleGPS.setText(R.string.gps_on_text);
        }
    }

    /**
     * Make server request to delete device, update perfs and finish
     */
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
                        mHelper.handleNetError(error);
                    }
                });

        // Access the RequestQueue through singleton class.
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    /**
     * Broadcast receiver helper functions to listen to last_updated
     */
    private void startReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver((locServiceReceiver), new IntentFilter(Deodorant.UPDATE_UI_INTENT)
        );
    }
    private void stopReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locServiceReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If the LocationService Alarm is set, listen for LocationService broadcasts
        if (locationServiceAlarmSet) {
            startReceiver();
        }
    }

    @Override
    protected void onStop() {
        stopReceiver();
        super.onStop();
    }

}
