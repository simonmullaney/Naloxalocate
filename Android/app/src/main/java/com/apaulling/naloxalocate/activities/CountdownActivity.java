package com.apaulling.naloxalocate.activities;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.apaulling.naloxalocate.R;
import com.apaulling.naloxalocate.util.Consts;

import java.io.IOException;
import java.util.Locale;

/*
 * Created by simonmullaney on 09/12/2016.
 */

public class CountdownActivity extends AppCompatActivity {

    private static final String TIMER_FORMAT = "%02d:%02d";
    private static final int COUNT_DOWN_INTERVAL_MS = 1000;
    private static final int ALARM_COUNTDOWN_DURATION_MS = 60000;

    // UI Elements
    private TextView remainingTimeVal;
    private CountDownTimer timerCountDown;
    private MediaPlayer mMediaPlayer;
    private Button btnStopAlarm;
    private CountDownTimer alarmCountDown;
    private AudioManager audioManager;

    // Save original alarm volume to be restored in onDestroy
    private int originalAlarmVolume = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        // Get UI elements
        remainingTimeVal = (TextView) findViewById(R.id.remaining_time_id);

        // Button to stop the alarm. This is hidden until the timerCountDown completes.
        btnStopAlarm = (Button) findViewById(R.id.btn_stop_alarm);
        btnStopAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // calls onDestroy which takes care of turning off alarm and restoring volume
            }
        });

        // Get the timer duration in minutes to convert to milliseconds
        int timerDurationMins = getIntent().getIntExtra(Consts.TIMER_DURATION_MINS_PERF_KEY, Consts.TIME_PICKER_INTERVAL_MINS);
        int timerDurationMs = timerDurationMins * 1000 * 60;

        // Create countdown objects
        timerCountDown = createTimerCountDown(timerDurationMs);
        alarmCountDown = createAlarmCountDown();

        // Start the timer countdown. This has a call back to start the alarm countdown
        timerCountDown.start();
    }

    /**
     * Creates timer CountDownTimer with callback to start alarm CountDownTimer
     *
     * @param timerDurationMs duration of timer in milliseconds.
     * @return CountDownTimer which is started in main
     */
    private CountDownTimer createTimerCountDown(int timerDurationMs) {
        return new CountDownTimer(timerDurationMs, COUNT_DOWN_INTERVAL_MS) {
            public void onTick(long millisUntilFinished) {
                // Show remaining time on UI
                displayRemainingTime(millisUntilFinished);
            }

            public void onFinish() {
                // Set text to 00:00
                remainingTimeVal.setText(R.string.countdown_finished_text);
                // Start the alarm
                startAlarm();
                // Show the stop button
                btnStopAlarm.setVisibility(View.VISIBLE);
                // Set the countdown to red (argb)
                remainingTimeVal.setTextColor(0xFFFF0000);
                // Start the next countdown
                alarmCountDown.start();
            }
        };
    }

    /**
     * Creates alarm CountDownTimer with callback to send SMS.
     *
     * @return CountDownTimer which is started by timerCountDown
     */
    private CountDownTimer createAlarmCountDown() {
        return new CountDownTimer(ALARM_COUNTDOWN_DURATION_MS, COUNT_DOWN_INTERVAL_MS) {
            public void onTick(long millisUntilFinished) {
                // Show remaining time on UI
                displayRemainingTime(millisUntilFinished);
            }

            public void onFinish() {
                remainingTimeVal.setText(R.string.countdown_finished_text);
                sendSMS();
            }
        };
    }

    /**
     * Show remaining time on UI
     *
     * @param millisUntilFinished is the time remaining for the calling CountDownTimer
     */
    private void displayRemainingTime(long millisUntilFinished) {
        long secsUntilFinished = millisUntilFinished / 1000;
        long minsRemaining = secsUntilFinished / 60;
        long secsRemaining = secsUntilFinished - (minsRemaining * 60);

        String remainingTimeStr = String.format(Locale.ENGLISH, TIMER_FORMAT, minsRemaining, secsRemaining);
        remainingTimeVal.setText(remainingTimeStr);
    }

    /**
     * Changes the alarm volume to max, gets the default alarm and loops it until onDestroy is called
     */
    public void startAlarm() {
        // Change alarm audio to max volume
        // Original volume is stored to it can be restored in onDestroy
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        originalAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);

        try {
            // Get uri to default alarm sound
            Uri alarmSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            // Set media player to play this sound
            mMediaPlayer = new MediaPlayer(); // Shared with onDestroy
            mMediaPlayer.setDataSource(this, alarmSoundUri);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM); // play as alarm
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare(); // get audio stream ready
            mMediaPlayer.start();

        } catch (IOException e) {
            // Will happen if alarm file cannot be found
            Toast.makeText(this, "Could not start alarm", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Gets values from intent and sends sms
     * Displays message to update UI
     */
    protected void sendSMS() {
        // Get contact details as passed in from TimerActivity
        String phoneNumber = getIntent().getStringExtra(Consts.CONTACT_NUMBER_INTENT_KEY);
        String message = getIntent().getStringExtra(Consts.SMS_MESSAGE_INTENT_KEY);
        String name = getIntent().getStringExtra(Consts.CONTACT_NAME_INTENT_KEY);

        // Send the SMS
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);

        // Update UI to show that SMS has been send
        TextView smsSentTextView = (TextView) findViewById(R.id.sms_sent_text);
        String smsSent = String.format(Locale.ENGLISH, "%s %s", getString(R.string.sms_sent_to_name), name);
        smsSentTextView.setText(smsSent);
    }

    /**
     * Cancels any timer already running
     * Cleans up after audio interaction.
     */
    @Override
    protected void onDestroy() {
        // Cancel countdown timers
        if (timerCountDown != null)
            timerCountDown.cancel();
        if (alarmCountDown != null)
            alarmCountDown.cancel();

        // Stop the alarm and release MediaPlayer
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }

            mMediaPlayer.release();
        }

        // Restore original alarm volume
        if (audioManager != null && originalAlarmVolume != -1) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, 0);
        }

        super.onDestroy();
    }

    /**
     * Ignore back button press so that timer countdown cannot be disabled accidentally
     */
    @Override
    public void onBackPressed() {
        // Override the back button press so it is ignored
    }
}