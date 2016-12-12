package com.apaulling.naloxalocate;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/**
 * Created by psdco on 08/12/2016.
 */
public class ProvideActivity extends AppCompatActivity {

    private boolean locationServiceAlarmSet;
    private TextView deviceIdTextView;
    final int LOCATION_SERVICE_REQ_CODE = 1001;
    Intent LocSerRecIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int user_id = prefs.getInt("user_id", -1);
//        locationServiceAlarmSet = prefs.getBoolean("locationServiceAlarmSet", false);

        // Register alarm if not registered already
        LocSerRecIntent = new Intent(ProvideActivity.this, LocationServiceReceiver.class);
        locationServiceAlarmSet = (PendingIntent.getBroadcast(ProvideActivity.this, LOCATION_SERVICE_REQ_CODE, LocSerRecIntent, PendingIntent.FLAG_NO_CREATE) != null);

        final Button btnToggleGPS = (Button) findViewById(R.id.btnToggleGPS);
        if (locationServiceAlarmSet){
            btnToggleGPS.setText("Turn Off GPS Tracking");
        }
        else {
            btnToggleGPS.setText("Turn On GPS Tracking");
        }

        deviceIdTextView = (TextView) findViewById(R.id.device_id);
        deviceIdTextView.setText(String.format(Locale.ENGLISH, "%d", user_id));


        btnToggleGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Register alarm if not registered already
                AlarmManager alarmManager =(AlarmManager) ProvideActivity.this.getSystemService(Context.ALARM_SERVICE);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(ProvideActivity.this, LOCATION_SERVICE_REQ_CODE, LocSerRecIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                if (!locationServiceAlarmSet){
                    // No alarm, create it
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 1, pendingIntent); // Millisec * Second * Minute

                    Toast.makeText(ProvideActivity.this, "Started", Toast.LENGTH_SHORT).show();
                    btnToggleGPS.setText("Turn Off GPS Tracking");
                }
                else {
                    // If the alarm has been set, cancel it
                    alarmManager.cancel(pendingIntent);
                    pendingIntent.cancel();

                    Toast.makeText(ProvideActivity.this, "Disabled", Toast.LENGTH_SHORT).show();
                    btnToggleGPS.setText("Turn On GPS Tracking");
                }

                locationServiceAlarmSet = !locationServiceAlarmSet;
            }
        });

    }
}
