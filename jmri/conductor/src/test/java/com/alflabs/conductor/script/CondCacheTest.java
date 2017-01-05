package com.alflabs.conductor.script;


import org.junit.Test;

import static org.mockito.Mockito.mock;
import static com.google.common.truth.Truth.assertThat;

public class CondCacheTest {
    @Test
    public void testCondCache() throws Exception {
        IConditional cond1 = mock(IConditional.class);
        IConditional cond2 = mock(IConditional.class);
        IConditional cond3 = mock(IConditional.class);

        CondCache cc = new CondCache();

        cc.put(cond1, false);
        cc.put(cond2, true);

        assertThat(cc.get(cond1)).isEqualTo(false);
        assertThat(cc.get(cond2)).isEqualTo(true);
        assertThat(cc.get(cond3)).isNull();
    }
}
