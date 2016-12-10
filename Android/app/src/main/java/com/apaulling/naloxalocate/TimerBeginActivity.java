package com.apaulling.naloxalocate;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;
import android.app.Activity;
import android.widget.Toast;




/**
 * Created by simonmullaney on 09/12/2016.
 */

public class TimerBeginActivity extends AppCompatActivity {


    private static final String FORMAT = "%02d:%02d:%02d";


    /*TextView Contact_number = (TextView)findViewById(R.id.Contact_number); */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer_begin);

        final TextView number;
        final TextView message;
        final String number_sms;
        final String message_sms;
        final TextView Remaining_time_val;

        Remaining_time_val = (TextView) findViewById(R.id.remaining_time_id);


        number = (TextView) findViewById(R.id.Contact_number_id);
        message = (TextView) findViewById(R.id.emergency_message_id);
/*
        number_sms = number.getText().toString();
        message_sms = message.getText().toString();
        Remaining_time_val.setText("done!");

*/
        number_sms = "0838394290";
        message_sms = "Hello";




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
                Remaining_time_val.setText("done!");
                playSound();
                sendSMS(number_sms,message_sms);
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


    private void sendSMS(String phoneNumber, String message){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public void playSound() {

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
        r.play();

    }

}



