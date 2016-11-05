package com.alfray.conductor.script;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

public class VarTest {
    @Test
    public void testGetSetValue() throws Exception {
        Var var = new Var(42);
        assertThat(var.getAsInt()).isEqualTo(42);

        var.accept(-32);
        assertThat(var.getAsInt()).isEqualTo(-32);
    }

    @Test
    public void testIsActive() throws Exception {
        Var var = new Var(42);
        assertThat(var.isActive()).isTrue();

        var.accept(0);
        assertThat(var.isActive()).isFalse();

        var.accept(-12);
        assertThat(var.isActive()).isTrue();
    }
}
