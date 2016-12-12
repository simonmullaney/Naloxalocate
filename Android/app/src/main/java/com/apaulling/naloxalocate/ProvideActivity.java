package com.apaulling.naloxalocate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by psdco on 08/12/2016.
 */
public class ProvideActivity extends AppCompatActivity {

    protected static final String TAG = "ProvideActivity";
    private static final int LOCATION_SERVICE_REQ_CODE = 1001;
    Intent locationServiceReceiverIntent;
    Button btnToggleGPS;
    private boolean locationServiceAlarmSet;
    private TextView deviceIdTextView;

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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int user_id = prefs.getInt("user_id", -1);
//        locationServiceAlarmSet = prefs.getBoolean("locationServiceAlarmSet", false);

        // Register alarm if not registered already
        locationServiceReceiverIntent = new Intent(ProvideActivity.this, LocationServiceReceiver.class);
        locationServiceAlarmSet = (PendingIntent.getBroadcast(ProvideActivity.this, LOCATION_SERVICE_REQ_CODE, locationServiceReceiverIntent, PendingIntent.FLAG_NO_CREATE) != null);

        btnToggleGPS = (Button) findViewById(R.id.btnToggleGPS);
        updateToggleGPSText();

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
                    } else {
                        // If the alarm has been set, cancel it
                        alarmManager.cancel(pendingIntent);
                        pendingIntent.cancel();
                    }

                    locationServiceAlarmSet = !locationServiceAlarmSet;
                    updateToggleGPSText();
                }
            }
        });
    }

    private void updateToggleGPSText() {
        if (locationServiceAlarmSet) {
            btnToggleGPS.setText("Turn Off GPS Tracking");
        } else {
            btnToggleGPS.setText("Turn On GPS Tracking");
        }
    }

    public void createLocationWarningDialog(Context context) {
        // notify user
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
}
