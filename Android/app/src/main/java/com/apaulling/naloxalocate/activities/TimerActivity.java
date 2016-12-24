package com.apaulling.naloxalocate.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.apaulling.naloxalocate.R;
import com.apaulling.naloxalocate.util.Consts;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/*
 * Created by psdco on 08/12/2016.
 */


public class TimerActivity extends AppCompatActivity {

    // UI Elements
    private EditText contactNumberEditText, contactNameEditText, smsMessageEditText;
    private CheckBox saveTimerDetailsCheckBox;
    private NumberPicker minuteNumberPicker;

    // Other private vars
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        // Populate the time picker with 5 minute intervals starting at 5, 55 max
        minuteNumberPicker = (NumberPicker) findViewById(R.id.selected_time);
        minuteNumberPicker.setMinValue(0);
        minuteNumberPicker.setMaxValue((60 / Consts.TIME_PICKER_INTERVAL_MINS) - 2);
        List<String> displayedValues = new ArrayList<>();
        for (int i = Consts.TIME_PICKER_INTERVAL_MINS; i < 60; i += Consts.TIME_PICKER_INTERVAL_MINS) {
            displayedValues.add(String.format(Locale.ENGLISH, "%d minutes", i));
        }
        minuteNumberPicker.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));

        // Get UI elements
        contactNameEditText = (EditText) findViewById(R.id.emergency_contact_name);
        contactNumberEditText = (EditText) findViewById(R.id.contact_number);
        smsMessageEditText = (EditText) findViewById(R.id.emergency_message);
        saveTimerDetailsCheckBox = (CheckBox) findViewById(R.id.checkbox_save);

        // Get shared preferences
        prefs = getSharedPreferences(Consts.TIMER_DETAILS_PERF_KEY, MODE_PRIVATE);

        // If details saved last time, update UI
        boolean timerDetailsSaved = prefs.getBoolean(Consts.SAVE_DETAILS_PERF_KEY, false);
        if (timerDetailsSaved) {
            contactNameEditText.setText(prefs.getString(Consts.CONTACT_NAME_PERF_KEY, ""));
            contactNumberEditText.setText(prefs.getString(Consts.CONTACT_NUMBER_PERF_KEY, ""));
            smsMessageEditText.setText(prefs.getString(Consts.SMS_MESSAGE_PERF_KEY, ""));
            minuteNumberPicker.setValue(prefs.getInt(Consts.TIMER_DURATION_INEDX_PERF_KEY, Consts.TIME_PICKER_INTERVAL_MINS));
            saveTimerDetailsCheckBox.setChecked(true);
        }

        // Open contacts picker activity. Response handled in onActivityResult
        Button contactsBtn = (Button) findViewById(R.id.contacts_btn);
        contactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(i, Consts.PICK_CONTACT_REQ_CODE);
            }
        });

        // Smells of long method...
        // Start timer
        // Validates input
        // Saves user details if required
        // Begins countdown activity
        Button startTimerBtn = (Button) findViewById(R.id.btn_start_timer);
        startTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check for empty strings and add error
                if (!isAllInputValid())
                    return;

                // Get values
                String contactNameStr = contactNameEditText.getText().toString().trim();
                String contactNumberStr = contactNumberEditText.getText().toString().trim();
                String emergencyMessageStr = smsMessageEditText.getText().toString().trim();
                int timerDurationIndex = minuteNumberPicker.getValue();
                // Convert minute number picker index to minutes. Only index is stored in perfs
                int timerDurationMins = (timerDurationIndex+1) * Consts.TIME_PICKER_INTERVAL_MINS;

                // Save or delete prefs depending on checkbox
                SharedPreferences.Editor loginPrefsEditor = prefs.edit();
                if (saveTimerDetailsCheckBox.isChecked()) {
                    loginPrefsEditor.putString(Consts.CONTACT_NAME_PERF_KEY, contactNameStr);
                    loginPrefsEditor.putString(Consts.CONTACT_NUMBER_PERF_KEY, contactNumberStr);
                    loginPrefsEditor.putString(Consts.SMS_MESSAGE_PERF_KEY, emergencyMessageStr);
                    loginPrefsEditor.putInt(Consts.TIMER_DURATION_INEDX_PERF_KEY, timerDurationIndex);
                    loginPrefsEditor.putBoolean(Consts.SAVE_DETAILS_PERF_KEY, true);
                } else {
                    loginPrefsEditor.clear();
                }
                loginPrefsEditor.apply();

                // Pass relevant input to the countdown activity
                Intent i = new Intent(getApplicationContext(), CountdownActivity.class);
                i.putExtra(Consts.TIMER_DURATION_MINS_INTENT_KEY, timerDurationMins);
                i.putExtra(Consts.CONTACT_NUMBER_INTENT_KEY, contactNumberStr);
                i.putExtra(Consts.SMS_MESSAGE_INTENT_KEY, emergencyMessageStr);
                i.putExtra(Consts.CONTACT_NAME_INTENT_KEY, contactNameStr);
                startActivity(i);
            }
        });
    }

    /**
     * Check all input fields for valid input
     *
     * @return false if any 1 field is not valid.
     */
    private boolean isAllInputValid() {
        boolean contactNameIsValid = isTextViewValid(contactNameEditText);
        boolean contactNumberIsValid = isContactNumberValid(contactNumberEditText);
        boolean msgIsValid = isTextViewValid(smsMessageEditText);

        // Don't proceed if any input fails to validate
        if (!contactNumberIsValid || !contactNameIsValid || !msgIsValid)
            return false;
        else
            return true;
    }

    /**
     * Check if input has a value. Check if this value is a valid number
     *
     * @param et EditText to be checked for valid number
     * @return true if valid number.
     */
    private boolean isContactNumberValid(EditText et) {
        String str = et.getText().toString().trim();
        if (!str.isEmpty()) {
            // Validate text as number
            boolean numIsValid = PhoneNumberUtils.isGlobalPhoneNumber(str);
            if (!numIsValid) {
                contactNumberEditText.setError("Invalid Number");
                return false;
            } else {
                // All good
                contactNumberEditText.setError(null);
                return true;
            }
        } else {
            contactNumberEditText.setError("Required");
            return false;
        }
    }

    /**
     * Check if EditText has a value
     *
     * @param et view to be checked
     * @return true if has a value
     */
    private boolean isTextViewValid(EditText et) {
        // Name
        String str = et.getText().toString().trim();
        if (!str.isEmpty()) {
            et.setError(null);
            return true;
        } else {
            et.setError("Required");
            return false;
        }
    }

    /**
     * Contact picker callback. Gets name and number and updates the UI
     *
     * @param requestCode passed in when called
     * @param resultCode  check if a contact was picked
     * @param data        which is passed between activities
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Consts.PICK_CONTACT_REQ_CODE && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            Cursor cursor = getContentResolver().query(contactUri, null, null, null, null);
            cursor.moveToFirst();

            // Get column numbers
            int numberColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            int nameColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

            // Update UI
            contactNumberEditText.setText(cursor.getString(numberColumnIndex));
            contactNameEditText.setText(cursor.getString(nameColumnIndex));
        }
    }
}