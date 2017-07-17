package com.alflabs.conductor;

import com.alflabs.conductor.util.NowProvider;
import com.alflabs.kv.KeyValueServer;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ConductorModule.class})
public interface IConductorComponent {

    NowProvider getNowProvider();

    KeyValueServer getKeyValueServer();

    void inject(EntryPoint entryPoint);
}
