package com.alfray.conductor.v2.dagger

import com.alflabs.conductor.dagger.CommonModule
import com.alflabs.conductor.jmri.IJmriProvider
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [CommonModule::class])
interface IEngine2kComponent {
    fun getScriptComponentFactory(): IScript2kComponent.Factory

    @Component.Factory
    interface Factory {
        fun createComponent(@BindsInstance jmriProvider: IJmriProvider): IEngine2kComponent
    }
}
