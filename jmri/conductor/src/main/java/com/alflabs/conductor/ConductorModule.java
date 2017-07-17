package com.alflabs.conductor;

import com.alflabs.conductor.util.Logger;
import com.alflabs.kv.KeyValueServer;
import dagger.Module;

@Module
public class ConductorModule {
    private final IJmriProvider mJmriProvider;
    private final KeyValueServer mKvServer;

    public ConductorModule(IJmriProvider jmriProvider, KeyValueServer kvServer) {
        mJmriProvider = jmriProvider;
        mKvServer = kvServer;
    }

    public Logger provideLogger() {
        return mJmriProvider;
    }

    public IJmriProvider provideJmriProvider() {
        return mJmriProvider;
    }

    public KeyValueServer provideKvServer() {
        return mKvServer;
    }
}
