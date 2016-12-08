package com.apaulling.naloxalocate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by psdco on 08/12/2016.
 */
public class ProvideActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provide);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        int user_id = prefs.getInt("user_id", -1);

        TextView deviceIdTV = (TextView) findViewById(R.id.deviceId);
        deviceIdTV.setText(String.format(Locale.ENGLISH, "%d", user_id));
    }
}
