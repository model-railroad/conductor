package com.alflabs.conductor.util;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class RateLimiterTest {

    private FakeNow mNow;

    @Before
    public void setUp() throws Exception {
        mNow = new FakeNow(1000);
    }

    @Test
    public void testRateLimit10Hz() throws Exception {
        // 10 Hz: target loop time is 100 ms
        RateLimiter limiter = new RateLimiter(10.0f, mNow);

        assertThat(mNow.now()).isEqualTo(1000);

        limiter.limit();
        assertThat(mNow.now()).isEqualTo(1000);

        // A loop that does nothing... sleeps till target time
        limiter.limit();
        assertThat(mNow.now()).isEqualTo(1100);

        // A short loop less than target time, limiter should compensate
        mNow.add(20);
        assertThat(mNow.now()).isEqualTo(1120);
        limiter.limit();
        assertThat(mNow.now()).isEqualTo(1200);

        // A short loop longer than target time, limiter should skip
        mNow.add(200);
        limiter.limit();
        assertThat(mNow.now()).isEqualTo(1400);
    }
}
