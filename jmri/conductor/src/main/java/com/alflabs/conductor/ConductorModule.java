package com.alflabs.conductor;

import com.alflabs.conductor.util.Logger;
import com.alflabs.conductor.util.Now;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.utils.ILogger;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class ConductorModule {
    private final IJmriProvider mJmriProvider;

    public ConductorModule(IJmriProvider jmriProvider) {
        mJmriProvider = jmriProvider;
    }

    @Singleton
    @Provides
    public Now provideNowProvider() {
        return new Now();
    }

    @Singleton
    @Provides
    public IJmriProvider provideJmriProvider() {
        return mJmriProvider;
    }

    @Singleton
    @Provides
    public Logger provideLogger() {
        return mJmriProvider;
    }

    @Singleton
    @Provides
    public KeyValueServer provideKeyValueServer(ILogger logger) {
        return new KeyValueServer(logger);
    }

    @Singleton
    @Provides
    public ILogger provideILogger(Logger logger) {
        return new ILogger() {
            @Override
            public void d(String tag, String message) {
                logger.log(tag + ": " + message);
            }

            @Override
            public void d(String tag, String message, Throwable tr) {
                logger.log(tag + ": " + message + ": " + tr);
            }
        };
    }
}
