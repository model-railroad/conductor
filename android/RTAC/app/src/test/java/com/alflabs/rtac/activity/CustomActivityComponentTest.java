package com.alflabs.rtac.activity;

import android.view.View;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rtac.fragment.IAutomationFragmentComponent;
import com.alflabs.rtac.fragment.MockAutomationFragmentComponent;
import com.alflabs.utils.ILogger;
import com.alflabs.utils.InjectionValidator;
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
import org.robolectric.util.ActivityController;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A version of the MainActivity test that uses a custom IMainActivityComponent injecting mock objects
 * into the activity. The IAppComponent is still the real one.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class CustomActivityComponentTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @Mock View mMockView;

    private TestMainActivity mActivity;

    @Before
    public void setUp() throws Exception {
        ActivityController<TestMainActivity> activityController = Robolectric.buildActivity(TestMainActivity.class);
        mActivity = activityController.get();
        // Setup the mockito expectations before onCreate() gets called.
        //--when(mActivity.mMockNetworkSomething.fetchAValue()).thenReturn(43);
        activityController.setup();
    }

    @Test
    public void testSomething() throws Exception {
        assertThat(mActivity.isDestroyed()).isFalse();
        when(mMockView.callOnClick()).thenReturn(true);
        mMockView.callOnClick();
    }

//    @Test
//    public void testPresenterValue() throws Exception {
//
//        TextView text = (TextView) mActivity.findViewById(R.id.text);
//        assertThat(text).isNotNull();
//
//        verify(mActivity.mNetworkSomething).fetchAValue();
//        verify(mActivity.mPresenterSomething).present(any(TextView.class), eq(43));
//    }

    public static class TestMainActivity extends MainActivity {

//        final NetworkSomething mMockNetworkSomething;

        public TestMainActivity() {
//            mMockNetworkSomething = mock(NetworkSomething.class);
        }

        @Override
        protected IMainActivityComponent createComponent() {
            return new IMainActivityComponent() {
                @Override
                public void inject(MainActivity mainActivity) {
                    // Inject is called in onCreate() but we want to setup the mMockNetworkSomething expectations
                    // (e.g. the when() calls) before the injection occurs, which is why this one is created earlier.
                    // Another option is to setup the mock expectations here rather than in setup() above.
//                    mainActivity.mNetworkSomething = mMockNetworkSomething;
//                    mainActivity.mPresenterSomething = mock(PresenterSomething.class);
                    mainActivity.mLogger = mock(ILogger.class);
                    mainActivity.mAppPrefsValues = mock(AppPrefsValues.class);

                    // One fragility of this pattern is that when new @Inject fields are added to the class
                    // we want this to break, otherwise some new injected fields would remain unset. This
                    // can be achieved using reflection by verifying all injected fields are not null.
                    InjectionValidator.check(mainActivity);
                }

                @Override
                public IAutomationFragmentComponent create() {
                    return new MockAutomationFragmentComponent();
                }
            };
        }
    }
}
