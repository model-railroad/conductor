package com.alflabs.conductor.dagger;

import dagger.Module;

@Module(includes = {
        AnalyticsModule.class,
        ClockModule.class,
        EventLoggerModule.class,
        ExecutorModule.class,
        FileOpsModule.class,
        HttpClientModule.class,
        KeyValueModule.class,
        JsonSenderModule.class,
        LoggerModule.class,
        RandomModule.class,
})
public abstract class CommonModule {
}
