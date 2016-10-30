package com.alfray.conductor.script;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class VarTest {
    @Test
    public void testGetSetValue() throws Exception {
        Var var = new Var("name", 42);
        assertThat(var.getValue()).isEqualTo(42);

        var.setValue(-32);
        assertThat(var.getValue()).isEqualTo(-32);
    }

    @Test
    public void testIsActive() throws Exception {
        Var var = new Var("name", 42);
        assertThat(var.isActive()).isTrue();

        var.setValue(0);
        assertThat(var.isActive()).isFalse();

        var.setValue(-12);
        assertThat(var.isActive()).isTrue();
    }
}
