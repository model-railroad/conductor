package com.alflabs.conductor;

import com.alflabs.conductor.util.NowProvider;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ConductorModule.class})
public interface IConductorComponent {

    NowProvider provideNowProvider();
}
