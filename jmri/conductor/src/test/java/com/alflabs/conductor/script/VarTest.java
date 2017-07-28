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

public class VarTest {
    public @Rule MockitoRule mRule = MockitoJUnit.rule();

    @Mock IKeyValue mKeyValue;

    private Var mVar;

    @Before
    public void setUp() throws Exception {
        VarFactory factory = new VarFactory(InstanceFactory.create(mKeyValue));
        mVar = factory.create(42, "MyVar");

        mVar.onExecStart();
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
    }

    @Test
    public void testGetSetValue() throws Exception {
        assertThat(mVar.getAsInt()).isEqualTo(42);

        mVar.accept(-32);
        assertThat(mVar.getAsInt()).isEqualTo(-32);
    }

    @Test
    public void testSetExported() throws Exception {
        verify(mKeyValue, never()).putValue(anyString(), anyString(), eq(true));
        mVar.setExported(true);
        mVar.accept(43);
        mVar.onExecHandle();
        verify(mKeyValue).putValue("V:MyVar", "43", true);
    }

    @Test
    public void testIsActive() throws Exception {
        assertThat(mVar.isActive()).isTrue();

        mVar.accept(0);
        assertThat(mVar.isActive()).isFalse();

        mVar.accept(-12);
        assertThat(mVar.isActive()).isTrue();
    }
}
