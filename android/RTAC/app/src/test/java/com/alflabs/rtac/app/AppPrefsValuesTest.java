package com.alflabs.rtac.app;

import com.alflabs.rtac.BuildConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class AppPrefsValuesTest {

    @Test
    public void testAppsPrefsValuesAvailable() throws Exception {
        IAppComponent component = MainApp.getAppComponent(RuntimeEnvironment.application);
        AppPrefsValues prefsValues1 = component.getAppPrefsValues();
        AppPrefsValues prefsValues2 = component.getAppPrefsValues();
        assertThat(prefsValues1).isNotNull();
        assertThat(prefsValues2).isSameAs(prefsValues1);
    }
}
