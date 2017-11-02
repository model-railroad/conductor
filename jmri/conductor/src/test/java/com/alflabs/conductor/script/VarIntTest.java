package com.alflabs.conductor.script;

import com.alflabs.kv.IKeyValue;
import dagger.internal.InstanceFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class VarIntTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IKeyValue mKeyValue;

    private VarInt mVarInt;

    @Before
    public void setUp() throws Exception {
        VarIntFactory factory = new VarIntFactory(InstanceFactory.create(mKeyValue));
        mVarInt = factory.create(42, "MyVar");

        mVarInt.onExecStart();
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
    }

    @Test
    public void testGetSetValue() throws Exception {
        assertThat(mVarInt.getAsInt()).isEqualTo(42);

        mVarInt.accept(-32);
        assertThat(mVarInt.getAsInt()).isEqualTo(-32);
    }

    @Test
    public void testSetExported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mVarInt.setExported(true);
        mVarInt.accept(43);
        mVarInt.onExecHandle();
        verify(mKeyValue).putValue("V/MyVar", "43", true);
    }

    @Test
    public void testIsActive() throws Exception {
        assertThat(mVarInt.isActive()).isTrue();

        mVarInt.accept(0);
        assertThat(mVarInt.isActive()).isFalse();

        mVarInt.accept(-12);
        assertThat(mVarInt.isActive()).isTrue();
    }
}
