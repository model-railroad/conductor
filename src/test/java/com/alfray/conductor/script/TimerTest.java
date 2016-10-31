package com.alfray.conductor.script;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class TimerTest {
    @Test
    public void testTimer() throws Exception {
        NowTimer timer = new NowTimer(42, 100*1000);

        assertThat(timer.isActive()).isFalse();

        timer.createFunctionStart().setValue(1);
        assertThat(timer.isActive()).isFalse();

        timer.setNow(141*1000);
        assertThat(timer.isActive()).isFalse();

        timer.setNow(142*1000);
        assertThat(timer.isActive()).isTrue();

        // timer stays active till reset using end
        timer.setNow(200*1000);
        assertThat(timer.isActive()).isTrue();

        timer.createFunctionEnd().setValue(1);
        assertThat(timer.isActive()).isFalse();
    }

    private static class NowTimer extends Timer {
        private long mNow;

        public NowTimer(int durationSec, long now) {
            super(durationSec);
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
