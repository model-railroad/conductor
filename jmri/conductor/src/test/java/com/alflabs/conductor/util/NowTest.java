package com.alflabs.conductor.util;

import org.junit.Before;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class NowTest {

    private FakeNow mNow;

    @Before
    public void setUp() throws Exception {
        mNow = new FakeNow(1000);
    }

    @Test
    public void testSetNow() throws Exception {
        assertThat(mNow.now()).isEqualTo(1000);

        mNow.setNow(2000);
        assertThat(mNow.now()).isEqualTo(2000);
    }

    @Test
    public void testAdd() throws Exception {
        assertThat(mNow.now()).isEqualTo(1000);

        mNow.add(2000);
        assertThat(mNow.now()).isEqualTo(3000);
    }

    @Test
    public void testSleep() throws Exception {
        assertThat(mNow.now()).isEqualTo(1000);

        mNow.sleep(2000);
        assertThat(mNow.now()).isEqualTo(3000);

        mNow.sleep(-500);
        assertThat(mNow.now()).isEqualTo(3000);
    }

}
