package com.apaulling.naloxalocate.activities;


import android.os.Build;
import android.os.SystemClock;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.apaulling.naloxalocate.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.IsNot.not;

@RunWith(AndroidJUnit4.class)
public class ProvideActivityTest {

    @Before
    public void grantPhonePermission() {
        // Grant location permission before starting app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getInstrumentation().getUiAutomation().executeShellCommand(
                    "pm grant " + getTargetContext().getPackageName()
                            + " android.permission.ACCESS_FINE_LOCATION");
        }
    }

    @Rule
    // This test starts in main to obtain
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void testActivityStartAndDeleteButton() {
        // Open Provide Activity
        onView(withId(R.id.btn_provide_activity)).perform(click());
        SystemClock.sleep(1000);

        // Check that device id is not -1
        onView(withId(R.id.device_id)).check(matches(not(withText("-1"))));

        // Check that GPS has never been uploaded
        onView(withId(R.id.gps_status)).check(matches(withText("Never uploaded")));

        // Press the delete button
        onView(withId(R.id.btn_delete_account)).perform(scrollTo(), click());

        // Check for "are you sure" dialog
        onView(withId(android.R.id.message)).check(matches(withText("Are You Sure?")));

        // Press the delete button.
        onView(withId(android.R.id.button2)).perform(scrollTo(), click());

        // Check that we are still in the activity by checking the button still exists
        onView(withId(R.id.btn_delete_account)).check(matches(isDisplayed()));

        // Press delete again
        onView(withId(R.id.btn_delete_account)).perform(scrollTo(), click());

        // Press yes
        onView(withId(android.R.id.button1)).perform(scrollTo(), click());

        // Check that we left that activity
//        onView(withId(R.id.btn_delete_account)).check(doesNotExist());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
