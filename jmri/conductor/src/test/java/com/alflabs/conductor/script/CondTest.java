package com.alflabs.conductor.script;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CondTest {
    @Test
    public void testCond() throws Exception {
        CondCache cc = new CondCache();
        IConditional conditional = mock(IConditional.class);
        Cond cond1 = new Cond(conditional, false /*negated*/);
        Cond cond2 = new Cond(conditional, true /*negated*/);

        when(conditional.isActive()).thenReturn(true);

        assertThat(cond1.eval(cc)).isTrue();
        assertThat(cond2.eval(cc)).isFalse();

        when(conditional.isActive()).thenReturn(false);
        // still returning the same value from the cache
        assertThat(cond1.eval(cc)).isTrue();
        assertThat(cond2.eval(cc)).isFalse();

        cc.clear();
        assertThat(cond1.eval(cc)).isFalse();
        assertThat(cond2.eval(cc)).isTrue();
    }
}
