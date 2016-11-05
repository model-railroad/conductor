package com.alfray.conductor.util;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class PairTest {

    @Test
    public void testPair() throws Exception {
        String foo = "foo";
        int a = 42;

        Pair<String, Integer> p = Pair.of(foo, a);
        assertThat(p.mFirst).isEqualTo("foo");
        assertThat(p.mSecond).isEqualTo(42);
    }
}
