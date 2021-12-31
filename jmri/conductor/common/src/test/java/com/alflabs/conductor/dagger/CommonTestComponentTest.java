package com.alflabs.conductor.dagger;

import com.alflabs.conductor.jmri.FakeJmriProvider;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.utils.FileOps;
import dagger.Component;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.google.common.truth.Truth.assertThat;

public class CommonTestComponentTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    private IJmriProvider mJmriProvider;
    private ICommonTestComponent mComponent;
    private LocalComponent mLocal;

    //@Inject FileOps mFileOps;

    @Before
    public void setUp() throws Exception {
        mJmriProvider = new FakeJmriProvider();
        mComponent = DaggerICommonTestComponent.factory().createTestComponent(mJmriProvider);
        mLocal = DaggerCommonTestComponentTest_LocalComponent.factory().createComponent(mComponent);
        mLocal.inject(this);
    }

    @Test
    public void test1() {
        assertThat(false).isTrue();
    }

    @Singleton
    @Component(dependencies = { ICommonTestComponent.class })
    interface LocalComponent {
        void inject(CommonTestComponentTest commonTestComponentTest);

        @Component.Factory
        interface Factory {
            LocalComponent createComponent(ICommonTestComponent component);
        }
    }
}
