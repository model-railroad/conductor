package com.alflabs.conductor.dagger;

import dagger.Module;

@Module(includes = {
        ExecutorModule.class,
        FakeClockModule.class,
        FakeFileOpsModule.class,
        FakeKeyValueModule.class,
        LoggerModule.class,
        MockAnalyticsModule.class,
        MockEventLoggerModule.class,
        MockHttpClientModule.class,
        MockJsonSenderModule.class,
        MockRandomModule.class,
})
public abstract class CommonTestModule {
}
