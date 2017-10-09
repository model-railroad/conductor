package com.alflabs.rtac.fragment;

import android.app.Fragment;
import android.view.View;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.R;
import com.alflabs.rtac.activity.MainActivity;
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
 * A version of the {@link EStopFragment} test that uses the real IAppComponent and the real IMainActivityComponent.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class EStopFragmentTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    private MainActivity mActivity;
    private Fragment mFragment;

    @Before
    public void setUp() throws Exception {
        // Setup the activity including create() and visible()
        mActivity = Robolectric.buildActivity(MainActivity.class).setup().get();
        mFragment = mActivity.getFragmentManager().findFragmentById(R.id.estop_fragment);
        assertThat(mFragment).isNotNull();
    }

    @Test
    public void testFragmentVisible() throws Exception {
        assertThat(mFragment).isNotNull();
        assertThat(mFragment.getView()).isNotNull();
        assertThat(mFragment.getView().getVisibility()).isEqualTo(View.VISIBLE);
    }
}
