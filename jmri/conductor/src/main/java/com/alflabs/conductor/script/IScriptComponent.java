package com.alflabs.conductor.script;

import com.alflabs.conductor.parser.ScriptParser2;
import dagger.Subcomponent;

@ScriptScope
@Subcomponent(modules = { ScriptModule.class })
public interface IScriptComponent {

    ScriptParser2 getScriptParser2();
    ExecEngine getScriptExecEngine();
}
