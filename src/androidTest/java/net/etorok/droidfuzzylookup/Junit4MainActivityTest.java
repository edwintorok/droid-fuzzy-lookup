package net.etorok.droidfuzzylookup;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class Junit4MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> activityRule =
            new ActivityTestRule<MainActivity>(MainActivity.class);

    @Test public void getActivity() {
        Assert.assertNotNull(activityRule.getActivity());
        Assert.assertTrue(activityRule.getActivity() instanceof MainActivity);
    }
}