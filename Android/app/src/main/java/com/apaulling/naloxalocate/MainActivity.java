package com.apaulling.naloxalocate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NetworkError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    public static final String USER_ID_PERF_KEY = "user_id";
    protected static final String TAG = "MainActivity";
    /*
    * Permission Location Callback
    */
    private static final int LOCATION_PERMISSION_REQUEST_CODE_FIND = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE_PROVIDE = 102;
    Button btnTimerActivity;
    Button btnFindActivity;
    Button btnProvideActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity_main);

        btnTimerActivity = (Button) findViewById(R.id.btnTimerActivity);
        btnFindActivity = (Button) findViewById(R.id.btnFindActivity);
        btnProvideActivity = (Button) findViewById(R.id.btnProvideActivity);

        btnTimerActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, TimerActivity.class));
            }
        });

        btnFindActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check for location permissions
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE_FIND);
                } else {
                    // Location permission already granted, start activity
                    MainActivity.this.startActivity(new Intent(MainActivity.this, FindActivity.class));
                }
            }
        });

        btnProvideActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE_PROVIDE);
                } else {
                    // Check for a device id
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    int user_id = prefs.getInt(USER_ID_PERF_KEY, -1);
                    // If yes, then open the manage screen
                    if (user_id == -1) {
                        // New user. Must get an id for the device to identify it with the server
                        getNewDeviceId();
                    } else {
                        MainActivity.this.startActivity(new Intent(MainActivity.this, ProvideActivity.class));
                    }
                }
            }
        });
    }

    private void getNewDeviceId() {
        String url = "http://apaulling.com/naloxalocate/api/v1.0/users";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.POST, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Get the id from the JSON response
                            int user_id = response.getInt("user_id");

                            // Store it for next time
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt(USER_ID_PERF_KEY, user_id);
                            editor.apply();

                            // Start the activity
                            MainActivity.this.startActivity(new Intent(MainActivity.this, ProvideActivity.class));

                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "JSON Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "Error: " + error.toString());
                        if (error instanceof NetworkError) {
                            createNetErrorDialog();
                        } else {
                            Toast.makeText(MainActivity.this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Access the RequestQueue through singleton class.
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE_FIND: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    MainActivity.this.startActivity(new Intent(MainActivity.this, FindActivity.class));
                }
            }
            case LOCATION_PERMISSION_REQUEST_CODE_PROVIDE: {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Call onClick callback again
                    btnProvideActivity.callOnClick();
                }
            }
        }
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
