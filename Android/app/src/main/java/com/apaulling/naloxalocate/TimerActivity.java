package com.apaulling.naloxalocate;

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


/**
 * Created by psdco on 08/12/2016.
 */


public class TimerActivity extends AppCompatActivity implements View.OnClickListener {


    static String number_str;
    private static Integer time_int;
    private static String message;
    private String contact_number, emergency_message, contact_name;
    private Button ch;
    private EditText editTextContactNumber, editTextConatctName, editTextMessage, editTextTime;
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

    public static Integer getTime_int() {
        return time_int;
    }

    public static void setTime_int(Integer time_int) {

        TimerActivity.time_int = time_int;
    }

    public static String getNumber() {
        return number_str;
    }

    public static void setNumber(String number_str) {
        TimerActivity.number_str = number_str;
    }

    public static String getMessage() {
        return message;
    }

    public static void setMessage(String message) {
        TimerActivity.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);


        // Code to implement save checkbox

        ch = (Button) findViewById(R.id.btnBeginTimerActivity);
        ch.setOnClickListener(this);

        editTextContactNumber = (EditText) findViewById(R.id.Contact_number_id);
        //number_str = editTextContactNumber.getText().toString();
        setNumber(number_str);

        editTextMessage = (EditText) findViewById(R.id.emergency_message_id);
        //message = editTextMessage.getText().toString();
        setMessage(message);


        editTextTime = (EditText) findViewById(R.id.emergency_message_id);
        //time_int = Integer.parseInt(editTextTime.getText().toString());
        setTime_int(time_int);


        editTextConatctName = (EditText) findViewById(R.id.emergency_contact_name_id);

        saveLoginCheckBox = (CheckBox) findViewById(R.id.checkBox_id);


        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            editTextContactNumber.setText(loginPreferences.getString("contact_number", ""));
            editTextConatctName.setText(loginPreferences.getString("contact_name", ""));
            editTextMessage.setText(loginPreferences.getString("emergency_message", ""));
            saveLoginCheckBox.setChecked(true);
        }
    }

    public void onClick(View view) {
        if (view == ch) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editTextContactNumber.getWindowToken(), 0);

            contact_number = editTextContactNumber.getText().toString();
            contact_name = editTextConatctName.getText().toString();
            emergency_message = editTextMessage.getText().toString();

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
                TimerActivity.this.startActivity(new Intent(TimerActivity.this, TimerBeginActivity.class));

            }
        });
    }
}










