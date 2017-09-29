package com.alflabs.rtac.activity;

import com.alflabs.rtac.BuildConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

/**
 * Test for {@link PrefsActivity}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class PrefsActivityTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private PrefsActivity mActivity;

    @Before
    public void setUp() throws Exception {
        mActivity = Robolectric.buildActivity(PrefsActivity.class).setup().get();
    }

    @Test
    public void testSomething() throws Exception {
        assertThat(mActivity.isDestroyed()).isFalse();
    }

//    @Test
//    public void testPresenterValue() throws Exception {
//        TextView text = (TextView) mActivity.findViewById(R.id.text);
//        assertThat(text).isNotNull();
//        // The "42" is not in the layout XML or strings XML, it is added by the
//        // mPresenterSomething.present(..., mNetworkSomething.fetchAValue()) call
//        // in the activity onCreate and indicates there's a whole dagger graph
//        // operational at that point.
//        assertThat(text.getText().toString()).contains("42");
//    }
}
