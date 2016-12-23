package com.apaulling.naloxalocate.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.apaulling.naloxalocate.R;


/*
 * Created by psdco on 08/12/2016.
 */


public class TimerActivity extends AppCompatActivity implements View.OnClickListener {

    private Button ch;
    private EditText editTextContactNumber, editTextConatctName, editTextMessage;
    private CheckBox saveLoginCheckBox;
    private SharedPreferences.Editor loginPrefsEditor;
    private EditText select_time_val;
    String time_str,contact_number_str,contact_message_str;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);


        // Code to implement save checkbox
        ch = (Button) findViewById(R.id.btnBeginTimerActivity);
        ch.setOnClickListener(this);

        editTextContactNumber = (EditText) findViewById(R.id.Contact_number_id);
        editTextConatctName = (EditText) findViewById(R.id.emergency_contact_name_id);
        editTextMessage = (EditText) findViewById(R.id.emergency_message_id);
        select_time_val = (EditText) findViewById(R.id.select_time);
        saveLoginCheckBox = (CheckBox) findViewById(R.id.checkBox_id);


        SharedPreferences loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        Boolean saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin) {
            editTextContactNumber.setText(loginPreferences.getString("contact_number", ""));
            editTextConatctName.setText(loginPreferences.getString("contact_name", ""));
            editTextMessage.setText(loginPreferences.getString("emergency_message", ""));
            saveLoginCheckBox.setChecked(true);
        }

//
        Button btnBeginTimerActivity = (Button) findViewById(R.id.btnBeginTimerActivity);

        btnBeginTimerActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TimerBeginActivity.class);
                time_str = select_time_val.getText().toString();
                contact_number_str = editTextContactNumber.getText().toString();
                contact_message_str = editTextMessage.getText().toString();
                i.putExtra("time", time_str);
                i.putExtra("number", contact_number_str);
                i.putExtra("message", contact_message_str);
                startActivity(i);
            }
        });
        //
    }

    public void onClick(View view) {
        if (view == ch) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextContactNumber.getWindowToken(), 0);

            String contact_number = editTextContactNumber.getText().toString();
            String contact_name = editTextConatctName.getText().toString();
            String emergency_message = editTextMessage.getText().toString();

            if (saveLoginCheckBox.isChecked()) {
                loginPrefsEditor.putBoolean("saveLogin", true);
                loginPrefsEditor.putString("contact_number", contact_number);
                loginPrefsEditor.putString("contact_name", contact_name);
                loginPrefsEditor.putString("emergency_message", emergency_message);
                loginPrefsEditor.commit();
            } else {
                loginPrefsEditor.clear();
                loginPrefsEditor.commit();
            }
        }
        Button btnBeginTimerActivity = (Button) findViewById(R.id.btnBeginTimerActivity);

        btnBeginTimerActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), TimerBeginActivity.class);
                time_str = select_time_val.getText().toString();
                i.putExtra("time", time_str);
                startActivity(i);
            }
        });
    }
}










