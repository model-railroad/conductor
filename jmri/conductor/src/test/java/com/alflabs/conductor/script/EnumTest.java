package com.alflabs.conductor.script;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;

public class EnumTest {

    private Enum_ mEnum;

    @Before
    public void setUp() throws Exception {
        mEnum = new Enum_(Arrays.asList("one", "two", "three"));
        assertThat(mEnum.get()).isEqualTo("one");
    }

    @Test
    public void testGetSetValue() throws Exception {
        mEnum.accept("Three");
        assertThat(mEnum.get()).isEqualTo("three");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetValue_Invalid() throws Exception {
        mEnum.accept("Invalid");
        Assert.fail("Expected IllegalArgumentException");
    }

    @Test
    public void testFunctionEQ() throws Exception {
        IConditional eq = mEnum.createCondition("==", "One");
        assertThat(eq.isActive()).isTrue();

        mEnum.accept("Two");
        assertThat(eq.isActive()).isFalse();
    }

    @Test
    public void testFunctionNEQ() throws Exception {
        IConditional neq = mEnum.createCondition("!=", "One");
        assertThat(neq.isActive()).isFalse();

        mEnum.accept("Two");
        assertThat(neq.isActive()).isTrue();
    }
}
