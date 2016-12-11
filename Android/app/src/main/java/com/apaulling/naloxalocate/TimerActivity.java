package com.apaulling.naloxalocate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;




/**
 * Created by psdco on 08/12/2016.
 */


public class TimerActivity extends AppCompatActivity implements View.OnClickListener {


    //private String username,password;
    private String contact_number, emergency_message, contact_name;
    //private Button ok;
    private Button ch;
    //private EditText editTextUsername,editTextPassword;
    private EditText editTextContactNumber, editTextConatctName, editTextMessage;

    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;

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






/*
        //code to save inserted details
        final CheckBox ch = (CheckBox) findViewById(R.id.checkBox);

        ch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(ch.isChecked()){
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences settings = getSharedPreferences(String.valueOf(prefs), 0);
                    settings.edit().putBoolean("check",true).commit();
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Uncheck", Toast.LENGTH_SHORT).show();
                }}
        });
*/








