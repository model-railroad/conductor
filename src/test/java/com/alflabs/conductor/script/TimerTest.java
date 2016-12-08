package com.alflabs.conductor.script;

import com.alflabs.conductor.util.NowProviderTest;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TimerTest {
    @Test
    public void testTimer() throws Exception {
        NowProviderTest.TestableNowProvider now =
                new NowProviderTest.TestableNowProvider(100*1000);
        Timer timer = new Timer(42, now);

        assertThat(timer.isActive()).isFalse();

        timer.createFunction(Timer.Function.START).accept(1);
        assertThat(timer.isActive()).isFalse();

        now.setNow(141*1000);
        assertThat(timer.isActive()).isFalse();

        now.setNow(142*1000);
        assertThat(timer.isActive()).isTrue();

        // timer stays active till reset using end
        now.setNow(200*1000);
        assertThat(timer.isActive()).isTrue();

        timer.createFunction(Timer.Function.END).accept(1);
        assertThat(timer.isActive()).isFalse();
    }
}
