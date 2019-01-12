package com.example.android.findez;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class FindEzInstrumentedTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.example.android.findez", appContext.getPackageName());
    }

    /**
     * Validates the UI elements of App
     * @throws Exception
     */
    @Test
    public void validateUiElements() throws Exception {
        onView(withId(R.id.search)).check(matches(isDisplayed()));
        ViewInteraction addFabBtn = onView(withId(R.id.add_fab));
        addFabBtn.check(matches(isDisplayed()));
        addFabBtn.perform(click());
        ViewInteraction saveBtn = onView(withId(R.id.action_save));
        saveBtn.check(matches(isDisplayed()));
        Espresso.closeSoftKeyboard();
        ViewInteraction enterNameEditBox = onView(withId(R.id.ev_item_name));
        ViewInteraction enterLocationEditBox = onView(withId(R.id.ev_item_location));
        ViewInteraction enterCommentsEditBox = onView(withId(R.id.ev_item_comments));
        enterNameEditBox.check(matches(isDisplayed()));
        enterNameEditBox.check(matches(withHint(R.string.item_name)));
        enterLocationEditBox.check(matches(isDisplayed()));
        enterLocationEditBox.check(matches(withHint(R.string.item_location)));
        enterCommentsEditBox.check(matches(isDisplayed()));
        enterCommentsEditBox.check(matches(withHint(R.string.item_comments)));
    }
}
