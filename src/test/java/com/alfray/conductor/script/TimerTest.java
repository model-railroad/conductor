package com.alfray.conductor.script;

import com.alfray.conductor.util.NowProvider;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TimerTest {
    @Test
    public void testTimer() throws Exception {
        TestableNowProvider provider = new TestableNowProvider(100*1000);
        Timer timer = new Timer(42, provider);

        assertThat(timer.isActive()).isFalse();

        timer.createFunctionStart().setValue(1);
        assertThat(timer.isActive()).isFalse();

        provider.setNow(141*1000);
        assertThat(timer.isActive()).isFalse();

        provider.setNow(142*1000);
        assertThat(timer.isActive()).isTrue();

        // timer stays active till reset using end
        provider.setNow(200*1000);
        assertThat(timer.isActive()).isTrue();

        timer.createFunctionEnd().setValue(1);
        assertThat(timer.isActive()).isFalse();
    }

    private static class TestableNowProvider extends NowProvider {
        private long mNow;

        public TestableNowProvider(long now) {
            mNow = now;
        }

        public void setNow(long now) {
            mNow = now;
        }

        @Override
        public long now() {
            return mNow;
        }
    }
}
