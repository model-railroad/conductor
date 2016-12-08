package com.alflabs.conductor.parser;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class LineCounterTest {
    @Test
    public void testLineGen() throws Exception {
        LineCounter lg = new LineCounter("\n" +
                "second line\n" +
                "third line\n" +
                "last line");

        assertThat(lg.getLine(0)).isEmpty(); // first line is 1
        assertThat(lg.getLine(1)).isEqualTo("\n");
        assertThat(lg.getLine(3)).isEqualTo("third line\n");
        assertThat(lg.getLine(2)).isEqualTo("second line\n");
        assertThat(lg.getLine(4)).isEqualTo("last line");
        assertThat(lg.getLine(5)).isEmpty();
    }
}
