/*
 * Project: RTAC
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.rtac.activity;

import android.view.View;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.RtacTestConfig;
import com.alflabs.rtac.app.AppPrefsValues;
import com.alflabs.rtac.app.DigisparkHelper;
import com.alflabs.rtac.fragment.IFragmentComponent;
import com.alflabs.rtac.fragment.MockFragmentComponent;
import com.alflabs.rtac.service.AnalyticsMixin;
import com.alflabs.rtac.service.DataClientMixin;
import com.alflabs.utils.FakeClock;
import com.alflabs.utils.IClock;
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

import javax.inject.Inject;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * A version of the MainActivity test that uses a custom IMainActivityComponent injecting mock objects
 * into the activity. The IAppComponent is still the real one.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = RtacTestConfig.ROBOELECTRIC_SDK, manifest = "src/main/AndroidManifest.xml")
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
                    // Inject is called in onCreate() but we want to setup the expectations
                    // (e.g. the when() calls) before the injection occurs, which is why this one is created earlier.
                    // Another option is to setup the mock expectations here rather than in setup() above.
                    mainActivity.mLogger = mock(ILogger.class);
                    mainActivity.mAppPrefsValues = mock(AppPrefsValues.class);
                    mainActivity.mMotionSensorMixin = mock(MotionSensorMixin.class);

                    // One fragility of this pattern is that when new @Inject fields are added to the class
                    // we want this to break, otherwise some new injected fields would remain unset. This
                    // can be achieved using reflection by verifying all injected fields are not null.
                    InjectionValidator.check(mainActivity);
                }

                @Override
                public void inject(MotionSensorMixin motionSensorMixin) {
                    // TODO Replace by usable test implementations.
                    motionSensorMixin.mDigispark = mock(DigisparkHelper.class);
                    motionSensorMixin.mDataClientMixin = mock(DataClientMixin.class);
                    motionSensorMixin.mAnalyticsMixin = mock(AnalyticsMixin.class);
                    motionSensorMixin.mAppPrefsValues = mock(AppPrefsValues.class);
                    motionSensorMixin.mClock = new FakeClock(42);

                    InjectionValidator.check(motionSensorMixin);
                }

                @Override
                public IFragmentComponent create() {
                    return new MockFragmentComponent();
                }
            };
        }
    }
}
