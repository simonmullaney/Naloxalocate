package com.apaulling.naloxalocate;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;
import android.app.Activity;


/**
 * Created by simonmullaney on 09/12/2016.
 */

public class TimerBeginActivity extends AppCompatActivity {


    private static final String FORMAT = "%02d:%02d:%02d";


    /*TextView Contact_number = (TextView)findViewById(R.id.Contact_number); */




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_timer_begin);

        TextView Remaining_time_val;
        Remaining_time_val = (TextView) findViewById(R.id.remaining_time_id);

        final TextView finalRemaining_time_val = Remaining_time_val;
        new CountDownTimer(5000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {

                finalRemaining_time_val.setText("" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                finalRemaining_time_val.setText("done!");
            }

        }.start();


        Button btnStopALarm = (Button) findViewById(R.id.btnStopALarm);
        btnStopALarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimerBeginActivity.this.startActivity(new Intent(TimerBeginActivity.this, TimerActivity.class));

            }
        });


    }

}



/*

public class MainActivity extends Activity {


    TextView text1;

    private static final String FORMAT = "%02d:%02d:%02d";

    int seconds , minutes;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        text1=(TextView)findViewById(R.id.textView1);

        new CountDownTimer(16069000, 1000) { // adjust the milli seconds here

            public void onTick(long millisUntilFinished) {

                text1.setText(""+String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                text1.setText("done!");
            }
        }.start();

    }

}

*/