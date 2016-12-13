package com.apaulling.naloxalocate.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apaulling.naloxalocate.R;
import com.apaulling.naloxalocate.util.Deodorant;
import com.apaulling.naloxalocate.util.RequestSingleton;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    protected static final String TAG = "MainActivity";
    // UI elements
    Button btnTimerActivity;
    Button btnFindActivity;
    Button btnProvideActivity;
    private Deodorant mSmellsHelper; // helps with creating error dialogs
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity_main);

        mSmellsHelper = new Deodorant(this);

        btnTimerActivity = (Button) findViewById(R.id.btn_timer_activity);
        btnFindActivity = (Button) findViewById(R.id.btn_find_activity);
        btnProvideActivity = (Button) findViewById(R.id.btn_provide_activity);

        /**
         * Btn click listeners
         */
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
                            Deodorant.PERMISSION_LOCATION_FIND_REQ_CODE);
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
                            Deodorant.PERMISSION_LOCATION_PROVIDE_REQ_CODE);
                } else {
                    // Check for a device id
                    prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    int user_id = prefs.getInt(Deodorant.USER_ID_PERF_KEY, -1);
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

    /**
     * Called first time users enters provide activity
     * Contacts server which generates a new unique id for the device
     */
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
                            prefs.edit().putInt(Deodorant.USER_ID_PERF_KEY, user_id).apply();

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
                        mSmellsHelper.handleNetError(error);
                    }
                });

        // Access the RequestQueue through singleton class.
        RequestSingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }

    /**
     * Callback for location permission requests
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Deodorant.PERMISSION_LOCATION_FIND_REQ_CODE:
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    MainActivity.this.startActivity(new Intent(MainActivity.this, FindActivity.class));
                }
                break;
            case Deodorant.PERMISSION_LOCATION_PROVIDE_REQ_CODE:
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Call onClick callback again
                    btnProvideActivity.callOnClick();
                }
                break;
        }
    }
}
