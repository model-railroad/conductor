package com.alflabs.conductor.script;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IntActionTest {
    @Test
    public void testExecute() throws Exception {
        IIntFunction function = mock(IIntFunction.class);
        IIntValue value = mock(IIntValue.class);
        when(value.getAsInt()).thenReturn(42);

        IAction action = new IntAction(function, value);
        action.execute();
        verify(value).getAsInt();
        verify(function).accept(42);
    }
}
