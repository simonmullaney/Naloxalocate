package com.apaulling.naloxalocate.activities;


import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Checkable;

import com.apaulling.naloxalocate.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.isA;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TimerActivityTest {

    @Rule
    public ActivityTestRule<TimerActivity> mActivityTestRule = new ActivityTestRule<>(TimerActivity.class);
    private static final String nameText = "Name McName";
    private static final String validNumber = "123456789";
    private static final String messageText = "Help";
    private static final String expectedTimerText = "04:59";

    /**
     * Helper method to clear all fields
     */
    private void resetFields() {
        onView(withId(R.id.emergency_contact_name)).perform(replaceText(""));
        onView(withId(R.id.contact_number)).perform(replaceText(""));
        onView(withId(R.id.emergency_message)).perform(replaceText(""));
        // Turn off checkbox
        onView(withId(R.id.checkbox_save)).perform(setCheckedState(false));
    }

    /**
     * Helper method to check the we're still in the timer activity
     */
    public void assertStillHere() {
        // Try to start timer with no data. Check we're still in this activity and that the app didn't proceed
        onView(withId(R.id.btn_start_timer)).perform(click());
        onView(withId(R.id.emergency_contact_name)).check(matches(isDisplayed()));
    }

    @Test
    public void testMissingInput(){
        resetFields();
        // No input to begin with
        // Try to start timer with no data. Check we're still in this activity and that the app didn't proceed
        assertStillHere();

        // Test missing name. Add valid number and message
        onView(withId(R.id.contact_number)).perform(replaceText(validNumber));
        onView(withId(R.id.emergency_message)).perform(replaceText(messageText));
        assertStillHere();

        // Test missing number
        resetFields();
        onView(withId(R.id.emergency_contact_name)).perform(replaceText(nameText));
        onView(withId(R.id.emergency_message)).perform(replaceText(messageText));
        assertStillHere();

        // Test missing message
        resetFields();
        onView(withId(R.id.contact_number)).perform(replaceText(validNumber));
        onView(withId(R.id.emergency_contact_name)).perform(replaceText(nameText));
        assertStillHere();

        // Test message only spaces.
        onView(withId(R.id.emergency_message)).perform(replaceText("   "));
        assertStillHere();
    }

    @Test
    public void testInvalidNumber() {
        resetFields();
        // Fill other fields
        onView(withId(R.id.emergency_contact_name)).perform(replaceText(nameText));
        onView(withId(R.id.emergency_message)).perform(replaceText(messageText));

        // Text is invalid input for number
        onView(withId(R.id.contact_number)).perform(replaceText("Number"));
        assertStillHere();

        // Digits with spaces is invalid input for number
        onView(withId(R.id.contact_number)).perform(replaceText("123 456 789"));
        assertStillHere();
    }



    @Test
    public void test1NormalStartWithoutSave() {
        resetFields();
        onView(withId(R.id.emergency_contact_name)).perform(replaceText(nameText), closeSoftKeyboard()); // Add name
        onView(withId(R.id.contact_number)).perform(replaceText(validNumber), closeSoftKeyboard()); // Add number
        onView(withId(R.id.emergency_message)).perform(replaceText(messageText), closeSoftKeyboard()); // Add help

        // Start timer
        onView(withId(R.id.btn_start_timer)).perform(click());

        // Check that the timer was started with the correct time
        onView(withId(R.id.remaining_time_id)).check(matches(withText(expectedTimerText)));
    }

    @Test
    public void test2NothingWasSaved() {
        onView(withId(R.id.emergency_contact_name)).check(matches(withText("")));
        onView(withId(R.id.contact_number)).check(matches(withText("")));
        onView(withId(R.id.emergency_message)).check(matches(withText("")));
    }

    @Test
    public void test3NormalStartWithSave() {
        resetFields();
        onView(withId(R.id.emergency_contact_name)).perform(replaceText(nameText), closeSoftKeyboard()); // Add name
        onView(withId(R.id.contact_number)).perform(replaceText(validNumber), closeSoftKeyboard()); // Add number
        onView(withId(R.id.emergency_message)).perform(replaceText(messageText), closeSoftKeyboard()); // Add help

        // Check save box
        onView(withId(R.id.checkbox_save)).perform(setCheckedState(true));

        // Start timer
        onView(withId(R.id.btn_start_timer)).perform(click());

        // Check that the timer was started with the correct time
        onView(withId(R.id.remaining_time_id)).check(matches(withText(expectedTimerText)));
    }

    @Test
    public void test4IfSaveWorked() {
        onView(withId(R.id.emergency_contact_name)).check(matches(withText(nameText)));
        onView(withId(R.id.contact_number)).check(matches(withText(validNumber)));
        onView(withId(R.id.emergency_message)).check(matches(withText(messageText)));
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

    /*Helper to set checkbox checked*/
    public static ViewAction setCheckedState(final boolean checked) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return new Matcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return isA(Checkable.class).matches(item);
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {}

                    @Override
                    public void _dont_implement_Matcher___instead_extend_BaseMatcher_() {}

                    @Override
                    public void describeTo(Description description) {}
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Checkable checkableView = (Checkable) view;
                checkableView.setChecked(checked);
            }
        };
    }

}
