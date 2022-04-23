package com.alflabs.conductor.v2;

import com.alflabs.annotations.Null;
import com.alflabs.conductor.dagger.CommonModule;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.util.Pair;
import com.alflabs.conductor.v2.dagger.IEngine2Component;
import com.alflabs.conductor.v2.script.IBlock;
import com.alflabs.conductor.v2.script.ISensor;
import com.alflabs.conductor.v2.script.RootScript;
import com.alflabs.conductor.v2.script.impl.MapInfo;
import com.alflabs.conductor.v2.script.impl.Timer;
import com.alflabs.conductor.v2.script.impl.Turnout;
import com.alflabs.utils.ILogger;
import dagger.BindsInstance;
import dagger.Component;
import groovy.lang.Binding;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class Engine2GroovyAdapter implements IEngineAdapter {
    private static final String TAG = Engine2GroovyAdapter.class.getSimpleName();

    private Optional<File> mScriptFile = Optional.empty();
    private Optional<Script2Loader> mScript2Loader = Optional.empty();

    @Inject ILogger mLogger;

    @Override
    public Optional<File> getScriptFile() {
        return mScriptFile;
    }

    @Override
    public void setScriptFile(@Null File scriptFile) {
        mScriptFile = Optional.ofNullable(scriptFile);
    }

    @Override
    public void onHandle(AtomicBoolean paused) {
        Optional<RootScript> script = mScript2Loader.flatMap(Script2Loader::getScript);

        // If we have no engine, or it is paused, just idle-wait.
        if (!script.isPresent() || paused.get()) {
            // TODO poor man async handling.
            // Consider some kind of CountDownLatch or a long monitor/notify or similar
            // instead of an active wait.
            try {
                Thread.sleep(330 /*ms*/);
            } catch (InterruptedException ignore) {
            }
            return;
        }

        script.get().executeRules();
    }

    @Override
    public Pair<Boolean, File> onReload() throws Exception {
        boolean wasRunning = mScript2Loader.isPresent();

        // TBD Release any resources from current script component as needed.

        File file = getScriptFile()
                .orElseThrow(() -> new IllegalArgumentException("Script2 File Not Defined"));
        log("Script2 Path: " + file.getPath());

        mScript2Loader = Optional.of(new Script2Loader());
        mScript2Loader.get().loadScriptFromFile(file.getPath());

        return Pair.of(wasRunning, file);
    }

    @Override
    public Optional<com.alflabs.manifest.MapInfo> getLoadedMapName() {
        Optional<RootScript> script = mScript2Loader.flatMap(Script2Loader::getScript);
        if (script.isPresent()) {
            Map<String, MapInfo> maps = script.get().maps();
            Optional<MapInfo> info = maps.values().stream().findFirst();
            if (info.isPresent()) {
                return Optional.of(new com.alflabs.manifest.MapInfo(
                        info.get().getName(),
                        info.get().getSvg(),
                        /* uri= */ info.get().getName()
                ));
            }
        }

        return Optional.empty();
    }

    @Override
    public void appendToLog(StringBuilder status) {
//        String lastError = mScript1Context.getError();
//        if (lastError.length() > 0) {
//            status.append("\n--- [ LAST ERROR ] ---\n");
//            status.append(lastError);
//        }

        Optional<Binding> binding = mScript2Loader.flatMap(Script2Loader::getBinding);
        Optional<RootScript> script = mScript2Loader.flatMap(Script2Loader::getScript);
        if (binding.isPresent() && script.isPresent()) {
            try {
                appendVarStatus(status, script.get(), binding.get());
            } catch (ConcurrentModificationException ignore) {}
        }
    }

    private static void appendVarStatus(
            StringBuilder outStatus,
            RootScript script,
            Binding binding) {

//        outStatus.append("Freq: ");
//        outStatus.append(String.format("%.1f Hz  [%.1f Hz]\n\n",
//                engine.getActualFrequency(),
//                engine.getMaxFrequency()));

        outStatus.append("--- [ TURNOUTS ] ---\n");
        int i = 0;
        for (Map.Entry<String, Turnout> entry : script.turnouts().entrySet()) {
            String name = entry.getKey();
            Turnout turnout = entry.getValue();
            outStatus.append(name.toUpperCase()).append(": ").append(turnout.isActive() ? 'N' : 'R');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ BLOCKS ] ---\n");
        i = 0;
        for (Map.Entry<String, IBlock> entry : script.blocks().entrySet()) {
            String name = entry.getKey();
            IBlock block = entry.getValue();
            outStatus.append(name.toUpperCase()).append(": ").append(block.isActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ SENSORS ] ---\n");
        i = 0;
        for (Map.Entry<String, ISensor> entry : script.sensors().entrySet()) {
            String name = entry.getKey();
            ISensor sensor = entry.getValue();
            outStatus.append(name.toUpperCase()).append(": ").append(sensor.isActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ TIMERS ] ---\n");
        i = 0;
        for (Map.Entry<String, Timer> entry : script.timers().entrySet()) {
            String name = entry.getKey();
            Timer timer = entry.getValue();
            outStatus.append(name).append(':').append(timer.isActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ ROUTES ] ---\n");
//        i = 0;
//        for (Map.Entry<String, ActiveRoute> entry : script.activeRoutes().entrySet()) {
//            String name = entry.getKey();
//            ActiveRoute activeRoute = entry.getValue();
//            outStatus.append(name).append(':').append(activeRoute.get());
//            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
//        }
        appendNewLine(outStatus);

        outStatus.append("--- [ VARS ] ---\n");
//        i = 0;
//        for (String name : script.getVarNames()) {
//            Var var = script.getVar(name);
//            outStatus.append(name).append(':').append(var.getAsInt());
//            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
//        }
        appendNewLine(outStatus);
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
    public interface LocalComponent2 extends IEngine2Component {
        // IScript2Component.Factory getScriptComponentFactory();

        void inject(EntryPoint2 entryPoint);
        void inject(Engine2GroovyAdapter adapter);

        @Component.Factory
        interface Factory {
            LocalComponent2 createComponent(@BindsInstance IJmriProvider jmriProvider);
        }
    }
}
