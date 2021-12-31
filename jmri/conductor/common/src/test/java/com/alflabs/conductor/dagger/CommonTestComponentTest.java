package com.alflabs.conductor.dagger;

import com.alflabs.conductor.jmri.FakeJmriProvider;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.util.Analytics;
import com.alflabs.conductor.util.EventLogger;
import com.alflabs.conductor.util.JsonSender;
import com.alflabs.kv.IKeyValue;
import com.alflabs.utils.FakeClock;
import com.alflabs.utils.FakeFileOps;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import dagger.BindsInstance;
import dagger.Component;
import okhttp3.OkHttpClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.Random;

import static com.google.common.truth.Truth.assertThat;

public class CommonTestComponentTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Inject ILogger mLogger;
    @Inject Random mRandom;
    @Inject IKeyValue mKeyValue;
    @Inject Analytics mAnalytics;
    @Inject JsonSender mJsonSender;
    @Inject EventLogger mEventLogger;
    @Inject OkHttpClient mOkHttpClient;
    @Inject IClock mClock;
    @Inject FakeClock mFakeClock;
    @Inject FileOps mFileOps;
    @Inject FakeFileOps mFakeFileOps;

    @Before
    public void setUp() throws Exception {
        IJmriProvider jmriProvider = new FakeJmriProvider();
        LocalComponent local = DaggerCommonTestComponentTest_LocalComponent.factory().createComponent(jmriProvider);
        local.inject(this);
    }

    @Test
    public void testInjectors() {
        assertThat(mLogger).isNotNull();
        assertThat(mRandom).isNotNull();
        assertThat(mKeyValue).isNotNull();
        assertThat(mAnalytics).isNotNull();
        assertThat(mJsonSender).isNotNull();
        assertThat(mEventLogger).isNotNull();
        assertThat(mOkHttpClient).isNotNull();

        assertThat(mFileOps).isNotNull();
        assertThat(mFakeFileOps).isNotNull();
        assertThat(mFileOps).isSameAs(mFakeFileOps);

        assertThat(mClock).isNotNull();
        assertThat(mFakeClock).isNotNull();
        assertThat(mClock).isSameAs(mFakeClock);
    }

    @Singleton
    @Component(modules = { CommonTestModule.class })
    interface LocalComponent {
        void inject(CommonTestComponentTest commonTestComponentTest);

        @Component.Factory
        interface Factory {
            LocalComponent createComponent(@BindsInstance IJmriProvider jmriProvider);
        }
    }
}
