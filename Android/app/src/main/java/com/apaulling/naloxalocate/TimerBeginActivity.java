package com.apaulling.naloxalocate;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import static com.apaulling.naloxalocate.R.id.select_time;


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
        TextView inputText = (TextView) findViewById(R.id.select_time);
/*
        number_sms = number.getText().toString();
        message_sms = message.getText().toString();
        Remaining_time_val.setText("done!");
*/
        number_sms = "0838394290";
        message_sms = "Hello";

        //String input_Text_str = inputText.getText().toString();

        //int input_Text_int = Integer.parseInt(input_Text_str);

        final TextView finalRemaining_time_val = Remaining_time_val;


        final CountDownTimer timer = new CountDownTimer(5000, 1000) { // adjust the milli seconds here
            public void onTick(long millisUntilFinished) {

                finalRemaining_time_val.setText("" + String.format(FORMAT,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(millisUntilFinished)),
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
            }

            public void onFinish() {
                Remaining_time_val.setText("----!");
                final MediaPlayer mMediaPlayer = new MediaPlayer();


                final CountDownTimer cntr_aCounter = new CountDownTimer(3000, 1000) {
                    public void onTick(long millisUntilFinished) {

                        //mMediaPlayer.start();
                        playSound(mMediaPlayer);
                    }

                    public void onFinish() {
                        //code fire after finish
                        mMediaPlayer.stop();
                        sendSMS(number_sms,message_sms);

                    }
                };cntr_aCounter.start();



                Button btnStopALarm = (Button) findViewById(R.id.btnStopALarm);
                btnStopALarm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mMediaPlayer.stop();
                        cntr_aCounter.cancel();
                        finish();

                    }
                });


            }

        }.start();


        Button btnStopALarm = (Button) findViewById(R.id.btnStopALarm);
        btnStopALarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timer.cancel();
                finish();
            }
        });


    }

    //external function to send sms and play sound

    private void sendSMS(String phoneNumber, String message){
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public void playSound(MediaPlayer med) {

        MediaPlayer mMediaPlayer = med;

        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            mMediaPlayer.setDataSource(this, alert);
            final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (Exception e) {
        }
    }

}