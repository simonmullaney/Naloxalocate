package com.apaulling.naloxalocate.util;

/**
 * Created by psdco on 23/12/2016.
 */

public class Consts {

    // Main Activity
    public static final int PERMISSION_LOCATION_FIND_REQ_CODE = 11;
    public static final int PERMISSION_LOCATION_PROVIDE_REQ_CODE = 22;
    public static final String USER_ID_PERF_KEY = "user_id";
    public static final int USER_ID_DEFAULT = 0; // This id is never assigned by the server

    // Timer Activity
    public static final int PICK_CONTACT_REQ_CODE = 12017;
    public static final int PERMISSION_SMS_TIMER_REQ_CODE = 54311;
    public static final String TIMER_DETAILS_PERF_KEY = "timer-details-key";
    public static final String CONTACT_NUMBER_PERF_KEY = "contact-number-key";
    public static final String CONTACT_NAME_PERF_KEY = "contact-name-key";
    public static final String SMS_MESSAGE_PERF_KEY = "emergency-message-key";
    public static final String TIMER_DURATION_INEDX_PERF_KEY = "timer-duration-mins-key";
    public static final String SAVE_DETAILS_PERF_KEY = "save-details-key";

    // Shared between TimerActivity and CountdownActivity
    public static final int TIME_PICKER_INTERVAL_MINS = 5;
    public static final String TIMER_DURATION_MINS_INTENT_KEY = "thissa";
    public static final String CONTACT_NAME_INTENT_KEY = "namenamename";
    public static final String CONTACT_NUMBER_INTENT_KEY = "numbernumbernumber";
    public static final String SMS_MESSAGE_INTENT_KEY = "messagemessagemessage";

    // Find Activity Request Codes
    public static final int PERMISSION_LOCATION_START_REQ_CODE = 111;
    public static final int SETTINGS_LOCATION_ENABLE_REQ_CODE = 222;

    // Provide Activity
    public static final int LOCATION_SERVICE_INTENT_REQ_CODE = 1111;
    public static final int LOCATION_SERVICE_REPEAT_TIME_MS = 1000 * 60; // minimum is 60s

    // Location Service
    public static final int ERROR_PERMISSION_REQ_CODE = 333;
    public static final int ERROR_LOCATION_REQ_CODE = 444;
    public static final int ERROR_NETWORK_REQ_CODE = 555;
    public static final String OPEN_ERROR_DIALOG_INTENT_KEY = "open-dialog-error-key";
    public static final String DATA_KEY_LAST_UPDATED = "update-ui-broadcast-key";
    public static final String UPDATE_UI_INTENT_KEY = "thunder-birds-are-something";
    public static final String LAST_UPDATE_PERF_KEY = "keys-open-doors";

    // Notification IDs
    public static final int ERROR_NOTIFICATION_ID = 12345;
    public static final int SERVICE_ON_NOTIFICATION_ID = 43232;

}
