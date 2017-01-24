package net.etorok.droidfuzzylookup;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class Junit4MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Test
    public void getActivity() {
        Assert.assertNotNull(activityRule.getActivity());
        Assert.assertTrue(activityRule.getActivity() instanceof MainActivity);
    }

    @Test
    public void emptySearch() {
        MainActivity activity = activityRule.getActivity();
        int id = activity.getListViewId();
        ProgressIdlingResource r = new ProgressIdlingResource(activity);
        Espresso.registerIdlingResources(r);
        onView(withHint(R.string.search_hint))
                .perform(typeText(""), pressImeActionButton(), closeSoftKeyboard());
        Espresso.unregisterIdlingResources(r);
        onView(withId(id))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    @Test
    public void dotSearch() {
        MainActivity activity = activityRule.getActivity();
        int id = activity.getListViewId();
        ProgressIdlingResource r = new ProgressIdlingResource(activity);
        Espresso.registerIdlingResources(r);
        onView(withHint(R.string.search_hint))
                .perform(typeText("."), pressImeActionButton(), closeSoftKeyboard());
        Espresso.unregisterIdlingResources(r);
        onView(withId(id))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)));
    }

    class ProgressIdlingResource implements IdlingResource {
        private ResourceCallback resourceCallback;
        private boolean isIdle;
        private final MainActivity activity;

        public ProgressIdlingResource(MainActivity m) {
            this.activity = m;
        }

        @Override
        public String getName() {
            return ProgressIdlingResource.class.getName();
        }

        @Override
        public boolean isIdleNow() {
            Log.d("ANDROIDTEST", "isIdle: " + isIdle);
            Boolean oldIdle = isIdle;
            isIdle = activity.isIdle();
            if (!oldIdle && isIdle) {
                Log.d("ANDROIDTEST", "triggering");
                resourceCallback.onTransitionToIdle();
            }
            return isIdle;
        }

        @Override
        public void registerIdleTransitionCallback(
                ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }
}
