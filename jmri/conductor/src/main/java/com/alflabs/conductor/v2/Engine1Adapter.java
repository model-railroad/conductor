package com.alflabs.conductor.v2;

import com.alflabs.annotations.Null;
import com.alflabs.conductor.dagger.CommonModule;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.util.Pair;
import com.alflabs.conductor.v1.Script1Context;
import com.alflabs.conductor.v1.Script1Loader;
import com.alflabs.conductor.v1.dagger.IEngine1Component;
import com.alflabs.conductor.v1.dagger.IScript1Component;
import com.alflabs.conductor.v1.script.Enum_;
import com.alflabs.conductor.v1.script.ExecEngine1;
import com.alflabs.conductor.v1.script.IExecEngine;
import com.alflabs.conductor.v1.script.Script1;
import com.alflabs.conductor.v1.script.Sensor;
import com.alflabs.conductor.v1.script.Timer;
import com.alflabs.conductor.v1.script.Turnout;
import com.alflabs.conductor.v1.script.Var;
import com.alflabs.kv.KeyValueServer;
import com.alflabs.manifest.MapInfo;
import com.alflabs.utils.ILogger;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Engine1Adapter {
    private static final String TAG = Engine1Adapter.class.getSimpleName();

    @Inject ILogger mLogger;
    @Inject Script1Loader mScript1Loader;
    @Inject Script1Context mScript1Context;

    public Optional<File> getScriptFile() {
        return mScript1Context.getScript1File();
    }

    public void setScriptFile(@Null File scriptFile) {
        mScript1Context.setScript1File(scriptFile);
    }

    public void onHandle(AtomicBoolean paused) {
        Optional<ExecEngine1> engine = mScript1Context.getExecEngine1();

        // If we have no engine, or it is paused, just idle-wait.
        if (!engine.isPresent() || paused.get()) {
            // TODO poor man async handling.
            // Consider some kind of CountDownLatch or a long monitor/notify or similar
            // instead of an active wait.
            try {
                Thread.sleep(330 /*ms*/);
            } catch (InterruptedException ignore) {
            }
            return;
        }

        engine.get().onExecHandle();
    }

    public Pair<Boolean, File> onReload() throws Exception {
        boolean wasRunning = mScript1Context.getScript1Component().isPresent();

        // TBD Release any resources from current script component as needed.
        mScript1Context.reset();

        File file = getScriptFile()
                .orElseThrow(() -> new IllegalArgumentException("Script1 File Not Defined"));
        log("Script1 Path: " + file.getPath());
        mScript1Loader.execByPath(mScript1Context);

        return Pair.of(wasRunning, file);
    }

    public Optional<MapInfo> getLoadedMapName() {
        Optional<Script1> script = mScript1Context.getScript1();
        if (script.isPresent()) {
            TreeMap<String, MapInfo> maps = script.get().getMaps();
            return maps.values().stream().findFirst();
        }

        return Optional.empty();
    }

    public void appendToLog(StringBuilder status, KeyValueServer keyValueServer) {
        String lastError = mScript1Context.getError();
        if (lastError.length() > 0) {
            status.append("\n--- [ LAST ERROR ] ---\n");
            status.append(lastError);
        }

        Optional<ExecEngine1> engine = mScript1Context.getExecEngine1();
        Optional<Script1> script = mScript1Context.getScript1();
        if (engine.isPresent() && script.isPresent()) {
            try {
                appendVarStatus(status, script.get(), engine.get(), keyValueServer);
            } catch (ConcurrentModificationException ignore) {}
        }
    }

    private static void appendVarStatus(
            StringBuilder outStatus,
            Script1 script,
            ExecEngine1 engine,
            KeyValueServer kvServer) {

        outStatus.append("Freq: ");
        outStatus.append(String.format("%.1f Hz  [%.1f Hz]\n\n",
                engine.getActualFrequency(),
                engine.getMaxFrequency()));

        outStatus.append("--- [ TURNOUTS ] ---\n");
        int i = 0;
        for (String name : script.getTurnoutNames()) {
            Turnout turnout = script.getTurnout(name);
            outStatus.append(name.toUpperCase()).append(": ").append(turnout.isActive() ? 'N' : 'R');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ SENSORS ] ---\n");
        i = 0;
        for (String name : script.getSensorNames()) {
            Sensor sensor = script.getSensor(name);
            outStatus.append(name.toUpperCase()).append(": ").append(sensor.isActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ TIMERS ] ---\n");
        i = 0;
        for (String name : script.getTimerNames()) {
            Timer timer = script.getTimer(name);
            outStatus.append(name).append(':').append(timer.isActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ ENUMS ] ---\n");
        i = 0;
        for (String name : script.getEnumNames()) {
            Enum_ enum_ = script.getEnum(name);
            outStatus.append(name).append(':').append(enum_.get());
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ VARS ] ---\n");
        i = 0;
        for (String name : script.getVarNames()) {
            Var var = script.getVar(name);
            outStatus.append(name).append(':').append(var.getAsInt());
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ KV Server ] ---\n");
        outStatus.append("Connections: ").append(kvServer.getNumConnections()).append('\n');
        for (String key : kvServer.getKeys()) {
            outStatus.append('[').append(key).append("] = ").append(kvServer.getValue(key)).append('\n');
        }
    }

    private static void appendNewLine(StringBuilder outStatus) {
        if (outStatus.charAt(outStatus.length() - 1) != '\n') {
            outStatus.append('\n');
        }
    }

    private void log(String line) {
        if (mLogger == null) {
            System.out.println(TAG + " " + line);
        } else {
            mLogger.d(TAG, line);
        }
    }

    @Singleton
    @Component(modules = { CommonModule.class })
    public interface LocalComponent1 extends IEngine1Component {
        IScript1Component.Factory getScriptComponentFactory();

        void inject(EntryPoint2 entryPoint);
        void inject(Engine1Adapter adapter);

        @Component.Factory
        interface Factory {
            LocalComponent1 createComponent(@BindsInstance IJmriProvider jmriProvider);
        }
    }
}
