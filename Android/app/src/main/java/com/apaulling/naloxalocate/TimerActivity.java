package com.apaulling.naloxalocate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import static com.apaulling.naloxalocate.R.id.checkBox;

/**
 * Created by psdco on 08/12/2016.
 */


public class TimerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        Button btnBeginTimerActivity = (Button) findViewById(R.id.btnBeginTimerActivity);


        btnBeginTimerActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerActivity.this.startActivity(new Intent(TimerActivity.this, TimerBeginActivity.class));


            }
        });


        EditText Contact_number = (EditText) findViewById(R.id.Contact_number);

        Contact_number.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction (TextView textview,int i, KeyEvent keyEvent){
                boolean handled = false;
                if (i == EditorInfo.IME_ACTION_NEXT) {
                    String inputText = textview.getText().toString();
                    Toast.makeText(TimerActivity.this, "" + inputText, Toast.LENGTH_SHORT).show();

                }
                return handled;
            }
        });

        /*
        //code to save inserted details
        ch = (CheckBox)findViewById(R.id.checkBox);

        ch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if(ch.isChecked()){

                    SharedPreferences settings = getSharedPreferences(PREFRENCES_NAME, 0);
                    settings.edit().putBoolean("check",true).commit();

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Uncheck", Toast.LENGTH_SHORT).show();
                }}
        });

    }
    */

    }

}
