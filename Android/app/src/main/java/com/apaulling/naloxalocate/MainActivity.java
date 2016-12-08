package com.apaulling.naloxalocate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_activity_main);

        Button btnTimerActivity = (Button) findViewById(R.id.btnTimerActivity);
        Button btnFindActivity = (Button) findViewById(R.id.btnFindActivity);
        Button btnProvideActivity = (Button) findViewById(R.id.btnProvideActivity);

        btnTimerActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, TimerActivity.class));
            }
        });

        btnFindActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, FindActivity.class));
            }
        });

        btnProvideActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if has device id
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                int user_id = prefs.getInt("user_id", -1);
                // If yes, then open the manage screen
                if (user_id == -1) {
                    Toast.makeText(MainActivity.this, "Getting new device id", Toast.LENGTH_SHORT).show();
                    getNewDeviceId();
                } else {
                    MainActivity.this.startActivity(new Intent(MainActivity.this, ProvideActivity.class));
                }
            }
        });
    }

    private void getNewDeviceId() {
        String url = "https://jsonplaceholder.typicode.com/posts/1";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            int user_id = response.getInt("id");
                            Toast.makeText(MainActivity.this, "Response: " + Integer.toString(user_id), Toast.LENGTH_SHORT).show();

                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putInt("user_id", user_id);
                            editor.apply();

                            MainActivity.this.startActivity(new Intent(MainActivity.this, ProvideActivity.class));

                        } catch (JSONException e) {
                            Toast.makeText(MainActivity.this, "JSON Error: " + e.toString(), Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Toast.makeText(MainActivity.this, "Rsp Error: " + error.toString(), Toast.LENGTH_SHORT).show();
                    }
                });

        // Access the RequestQueue through singleton class.
        MySingleton.getInstance(this).addToRequestQueue(jsObjRequest);
    }
}
