package com.alflabs.conductor;

import com.alflabs.conductor.util.NowProvider;
import com.alflabs.kv.KeyValueServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.File;

import static com.google.common.truth.Truth.assertThat;

public class IConductorComponentTest {
    @Rule public MockitoRule mRule = MockitoJUnit.rule();

    @Mock IJmriProvider mJmriProvider;

    private IConductorComponent mComponent;

    @Before
    public void setUp() throws Exception {
        File file = File.createTempFile("conductor_tests", "tmp");
        file.deleteOnExit();

        mComponent = DaggerIConductorComponent
                .builder()
                .conductorModule(new ConductorModule(mJmriProvider))
                .scriptFile(file)
                .build();
    }

    @Test
    public void testKeyValueServerIsSingleton() throws Exception {
        KeyValueServer kv1 = mComponent.getKeyValueServer();
        KeyValueServer kv2 = mComponent.getKeyValueServer();
        assertThat(kv1).isNotNull();
        assertThat(kv1).isSameAs(kv2);
    }

    @Test
    public void testNowProviderIsSingleton() throws Exception {
        NowProvider np1 = mComponent.getNowProvider();
        NowProvider np2 = mComponent.getNowProvider();
        assertThat(np1).isNotNull();
        assertThat(np1).isSameAs(np2);
    }
}
