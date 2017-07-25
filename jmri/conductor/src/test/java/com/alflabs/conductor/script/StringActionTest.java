package com.alflabs.conductor.script;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StringActionTest {
    @Test
    public void testExecute() throws Exception {
        IStringFunction function = mock(IStringFunction.class);
        IStringValue value = mock(IStringValue.class);
        when(value.get()).thenReturn("42");

        IAction action = new StringAction(function, value);
        action.execute();
        verify(value).get();
        verify(function).accept("42");
    }
}
