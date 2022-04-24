package com.alflabs.conductor.v2;

import com.alflabs.annotations.Null;
import com.alflabs.conductor.dagger.CommonModule;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.util.Pair;
import com.alflabs.utils.ILogger;
import com.alfray.conductor.v2.Script2kLoader;
import com.alfray.conductor.v2.dagger.IEngine2kComponent;
import com.alfray.conductor.v2.script.ConductorImpl;
import com.alfray.conductor.v2.script.ExecEngine;
import com.alfray.conductor.v2.script.ISvgMap;
import com.alfray.conductor.v2.script.impl.Block;
import com.alfray.conductor.v2.script.impl.Sensor;
import com.alfray.conductor.v2.script.impl.Timer;
import com.alfray.conductor.v2.script.impl.Turnout;
import com.google.common.base.Preconditions;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Engine2KotlinAdapter implements IEngineAdapter {
    private static final String TAG = Engine2KotlinAdapter.class.getSimpleName();

    private Optional<File> mScriptFile = Optional.empty();
    private Optional<Script2kLoader> mScript2kLoader = Optional.empty();

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
        Optional<ExecEngine> engine = mScript2kLoader.flatMap(Script2kLoader::execEngineOptional);

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

        engine.get().executeRules();
    }

    @Override
    public Pair<Boolean, File> onReload() throws Exception {
        boolean wasRunning = mScript2kLoader.isPresent();

        // TBD Release any resources from current script component as needed.

        File file = getScriptFile()
                .orElseThrow(() -> new IllegalArgumentException("Script2 File Not Defined"));
        log("Script2 Path: " + file.getPath());

        mScript2kLoader = Optional.of(new Script2kLoader());
        mScript2kLoader.get().loadScriptFromFile(file.getPath());
        Preconditions.checkState(mScript2kLoader.get().getResultErrors().isEmpty());

        return Pair.of(wasRunning, file);
    }

    @Override
    public Optional<com.alflabs.manifest.MapInfo> getLoadedMapName() {
        Optional<ConductorImpl> script = mScript2kLoader.flatMap(Script2kLoader::conductorOptional);
        if (script.isPresent()) {
            Map<String, ISvgMap> svgMaps = script.get().getSvgMaps();
            Optional<ISvgMap> svgMap = svgMaps.values().stream().findFirst();
            if (svgMap.isPresent()) {
                return Optional.of(new com.alflabs.manifest.MapInfo(
                        svgMap.get().getName(),
                        svgMap.get().getSvg(),
                        /* uri= */ svgMap.get().getName()
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

        Optional<ConductorImpl> script = mScript2kLoader.flatMap(Script2kLoader::conductorOptional);
        if (script.isPresent()) {
            try {
                appendVarStatus(status, script.get());
            } catch (ConcurrentModificationException ignore) {}
        }
    }

    private static void appendVarStatus(
            StringBuilder outStatus,
            ConductorImpl script) {

//        outStatus.append("Freq: ");
//        outStatus.append(String.format("%.1f Hz  [%.1f Hz]\n\n",
//                engine.getActualFrequency(),
//                engine.getMaxFrequency()));

        outStatus.append("--- [ TURNOUTS ] ---\n");
        int i = 0;
        for (Map.Entry<String, Turnout> entry : script.getTurnouts().entrySet()) {
            String name = entry.getKey();
            Turnout turnout = entry.getValue();
            outStatus.append(name.toUpperCase()).append(": ").append(turnout.getActive() ? 'N' : 'R');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ BLOCKS ] ---\n");
        i = 0;
        for (Map.Entry<String, Block> entry : script.getBlocks().entrySet()) {
            String name = entry.getKey();
            Block block = entry.getValue();
            outStatus.append(name.toUpperCase()).append(": ").append(block.getActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ SENSORS ] ---\n");
        i = 0;
        for (Map.Entry<String, Sensor> entry : script.getSensors().entrySet()) {
            String name = entry.getKey();
            Sensor sensor = entry.getValue();
            outStatus.append(name.toUpperCase()).append(": ").append(sensor.getActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("--- [ TIMERS ] ---\n");
        i = 0;
        for (Timer timer : script.getTimers()) {
            outStatus.append(timer.getName()).append(':').append(timer.getActive() ? '1' : '0');
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
    public interface LocalComponent2k extends IEngine2kComponent {
        // IScript2Component.Factory getScriptComponentFactory();

        void inject(EntryPoint2 entryPoint);
        void inject(Engine2KotlinAdapter adapter);

        @Component.Factory
        interface Factory {
            LocalComponent2k createComponent(@BindsInstance IJmriProvider jmriProvider);
        }
    }
}
