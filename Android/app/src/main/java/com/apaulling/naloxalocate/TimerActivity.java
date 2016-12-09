package com.apaulling.naloxalocate;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.app.Activity;

import java.util.concurrent.TimeUnit;

import static com.apaulling.naloxalocate.R.string.Remaining_time_val;

/**
 * Created by psdco on 08/12/2016.
 */


public class TimerActivity extends AppCompatActivity {

    private static final String FORMAT = "%02d:%02d:%02d";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        Button btnBeginTimerActivity = (Button) findViewById(R.id.btnBeginTimerActivity);


        btnBeginTimerActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerActivity.this.startActivity(new Intent(TimerActivity.this, TimerBeginActivity.class));



                 /*
                TextView Remaining_time_val = Integer.parseInt(Remaining_time_val)


                Remaining_time_val=(TextView)findViewById(R.id.remaining_time_id);

                new CountDownTimer(16069000, 1000) { // adjust the milli seconds here

                    public void onTick(long millisUntilFinished) {

                        Remaining_time_val.setText(""+String.format(FORMAT,
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                                TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
                    }

                    public void onFinish() {
                        Remaining_time_val.setText("done!");
                    }
                }.start();


*/
            }
        });

    }

}
