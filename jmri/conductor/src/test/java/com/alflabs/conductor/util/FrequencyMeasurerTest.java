package com.alflabs.conductor.util;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class FrequencyMeasurerTest {

    private FakeNow mNow;
    private FrequencyMeasurer mFreq;

    @Before
    public void setUp() throws Exception {
        mNow = new FakeNow(1000);
        mFreq = new FrequencyMeasurer(mNow);
    }

    @Test
    public void testFreq1() throws Exception {
        assertThat(mFreq.getFrequency()).isWithin(0.f).of(0.f);

        mFreq.ping();
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(0.f).of(0.f);

        mNow.add(1000);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(1.f);

        mNow.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(2.5f);

        mNow.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(5.f);

        mNow.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(7.5f);

        mNow.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(9.0f);

        mNow.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(9.7f);

        mNow.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(9.9f);

        mNow.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(10.f);

        mNow.add(100);
        mFreq.ping();
        assertThat(mFreq.getFrequency()).isWithin(.1f).of(10.f);
    }
}
