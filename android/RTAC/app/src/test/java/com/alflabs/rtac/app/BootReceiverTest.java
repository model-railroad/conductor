package com.alflabs.rtac.app;

import android.content.Intent;
import com.alflabs.rtac.BuildConfig;
import com.alflabs.rtac.activity.MainActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

import static com.google.common.truth.Truth.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 19, manifest = "src/main/AndroidManifest.xml")
public class BootReceiverTest {

    @Test
    public void testIntentStart() throws Exception {
        BootReceiver receiver = new BootReceiver();
        Intent intent = new Intent(BootReceiver.ACTION_OPEN_RTAC);
        receiver.onReceive(RuntimeEnvironment.application, intent);

        Intent startedActivity = Shadows.shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo(MainActivity.class.getName());
        assertThat(startedActivity.getComponent().getPackageName()).isEqualTo("com.alflabs.rtac");
    }

    @Test
    public void testBootCompleted_BootNoAction() throws Exception {
        MainApp.getAppComponent(RuntimeEnvironment.application)
                .getAppPrefsValues()
                .setSystem_BootAction(AppPrefsValues.BootAction.NO_ACTION);

        BootReceiver receiver = new BootReceiver();
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(RuntimeEnvironment.application, intent);

        Intent startedActivity = Shadows.shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertThat(startedActivity).isNull();
    }

    @Test
    public void testBootCompleted_BootStartRTAC() throws Exception {
        MainApp.getAppComponent(RuntimeEnvironment.application)
                .getAppPrefsValues()
                .setSystem_BootAction(AppPrefsValues.BootAction.START_RTAC);

        BootReceiver receiver = new BootReceiver();
        Intent intent = new Intent(Intent.ACTION_BOOT_COMPLETED);
        receiver.onReceive(RuntimeEnvironment.application, intent);

        Intent startedActivity = Shadows.shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
        assertThat(startedActivity).isNotNull();
        assertThat(startedActivity.getComponent().getClassName()).isEqualTo(MainActivity.class.getName());
    }
}
