package com.alflabs.rtac.activity;

import android.view.View;
import android.widget.TextView;
import com.alflabs.rtac.BuildConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

/**
 * A version of the MainActivity test that used the real IAppComponent and the real IMainActivityComponent.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class MainActivityTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock View mMockView;

    private MainActivity mActivity;

    @Before
    public void setUp() throws Exception {
        mActivity = Robolectric.buildActivity(MainActivity.class).setup().get();
    }

    @Test
    public void testSomething() throws Exception {
        assertThat(mActivity.isDestroyed()).isFalse();
        when(mMockView.callOnClick()).thenReturn(true);
        mMockView.callOnClick();
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
