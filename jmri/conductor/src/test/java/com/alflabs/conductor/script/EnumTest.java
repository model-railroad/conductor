package com.alflabs.conductor.script;

import com.alflabs.kv.IKeyValue;
import dagger.internal.InstanceFactory;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class EnumTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IKeyValue mKeyValue;

    private Enum_ mEnum;

    @Before
    public void setUp() throws Exception {
        EnumFactory factory = new EnumFactory(InstanceFactory.create(mKeyValue));
        mEnum = factory.create(Arrays.asList("one", "two", "three"), "MyVar");
        assertThat(mEnum.get()).isEqualTo("one");

        mEnum.onExecStart();
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
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
    public void testSetExported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mEnum.setExported(true);
        mEnum.accept("two");
        mEnum.onExecHandle();
        verify(mKeyValue).putValue("V/MyVar", "two", true);
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
