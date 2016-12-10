package com.apaulling.naloxalocate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

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

    }

}
