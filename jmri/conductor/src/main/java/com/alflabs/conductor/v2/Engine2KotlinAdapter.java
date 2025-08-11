/*
 * Project: Conductor
 * Copyright (C) 2022 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alflabs.conductor.v2;

import com.alflabs.annotations.NonNull;
import com.alflabs.annotations.Null;
import com.alflabs.conductor.dagger.CommonModule;
import com.alflabs.conductor.jmri.IJmriProvider;
import com.alflabs.conductor.util.Pair;
import com.alflabs.manifest.MapInfo;
import com.alflabs.utils.FileOps;
import com.alflabs.utils.IClock;
import com.alflabs.utils.ILogger;
import com.alfray.conductor.v2.Script2kLoader;
import com.alfray.conductor.v2.dagger.IEngine2kComponent;
import com.alfray.conductor.v2.dagger.IScript2kComponent;
import com.alfray.conductor.v2.dagger.Script2kContext;
import com.alfray.conductor.v2.dagger.Script2kModule;
import com.alfray.conductor.v2.script.ConductorImpl;
import com.alfray.conductor.v2.script.ExecEngine2k;
import com.alfray.conductor.v2.script.dsl.IRoutesContainer;
import com.alfray.conductor.v2.script.dsl.IBlock;
import com.alfray.conductor.v2.script.dsl.ISequenceRoute;
import com.alfray.conductor.v2.script.dsl.ISensor;
import com.alfray.conductor.v2.script.dsl.ITimer;
import com.alfray.conductor.v2.script.dsl.ITurnout;
import com.alfray.conductor.v2.script.dsl.SvgMapTarget;
import com.alfray.conductor.v2.script.impl.IExecEngine;
import com.alfray.conductor.v2.script.dsl.ISvgMap;
import com.alfray.conductor.v2.script.impl.SvgMap;
import com.alfray.conductor.v2.simulator.SimulRouteGraph;
import com.alfray.conductor.v2.simulator.Simul2k;
import com.alfray.conductor.v2.simulator.dagger.ISimul2kComponent;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Engine2KotlinAdapter implements IEngineAdapter {
    private static final String TAG = Engine2KotlinAdapter.class.getSimpleName();

    @Inject IClock mClock;
    @Inject ILogger mLogger;
    @Inject FileOps mFileOps;
    @Inject Script2kContext mScript2kContext;
    private Optional<File> mScriptToLoad = Optional.empty();
    private Optional<ISimul2kComponent> mSimul2kComponent = Optional.empty();

    /**
     * Gets the script file to load or reload.
     * <br/>
     * At startup, this is the file path that we will load later, before the Script scope is
     * even created. Once the script scope has been created, there is an equivalence with
     * <pre>
     *   mScript2kContext.getScript2kComponent().map(
     *     c -> c.getScript2kLoader().getScriptSource().scriptPath()
     *   );
     * </pre>
     * but before that, it represents the initial argument given before the component creation.
     */
    @Override
    public Optional<File> getScriptFile() {
        Optional<File> file = mScript2kContext.getScript2kComponent().map(
                c -> c.getScript2kLoader().getScriptSource().scriptPath()
        );
        return file.isPresent() ? file : mScriptToLoad;
    }

    @Override
    public void setScriptFile(@Null File scriptFile) {
        mScriptToLoad = Optional.ofNullable(scriptFile);
    }

    public void setSimulator(@Null ISimul2kComponent simulator) {
        mSimul2kComponent = Optional.ofNullable(simulator);
    }

    @Override
    public void onHandle(AtomicBoolean paused) {
        Optional<IExecEngine> engine = mScript2kContext.getScript2kComponent().map(
                c -> c.getScript2kLoader().getExecEngine());

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

        mSimul2kComponent.ifPresent(simul -> simul.getSimul2k().onExecHandle());
        engine.get().onExecHandle();
    }

    @Override
    public Pair<Boolean, File> onReload() {
        long nowMs = mClock.elapsedRealtime();
        boolean wasRunning = mScript2kContext.getScript2kComponent().isPresent();

        if (wasRunning) {
            // TBD Release any resources from current script component as needed.
            //
            // The current mScript2kContext implementation (IScript2kComponent, Script2kLoader,
            // and ConductorScriptHost) does not need any specific shutdown beside a reset() call.
            // Factories and instances just get garbage-collected when a new mScript2kContext
            // createComponent() is called below.
        }

        mScript2kContext.reset();
        mSimul2kComponent.ifPresent(simul ->
                simul.getSimul2k().onReload());

        File file = getScriptFile()
                .orElseThrow(() -> new IllegalArgumentException("Script2 File Not Defined"));
        log("Script2 Path: " + file.getPath());

        IScript2kComponent script2kComponent = mScript2kContext.createComponent(mSimul2kComponent.isPresent());
        Script2kLoader loader = script2kComponent.getScript2kLoader();
        ConductorImpl conductorImpl = loader.getConductorImpl();
        mSimul2kComponent.ifPresent(simul -> {
            conductorImpl.setSimulCallback(simul.getSimul2k());
        });

        loader.loadScriptFromFile(file.getPath());
        log("Loaded in " + (mClock.elapsedRealtime() - nowMs) + " ms");
        log(loader.getResultOutputs());
        Preconditions.checkState(loader.getResultErrors().isEmpty());

        loader.getExecEngine().onExecStart();

        mSimul2kComponent.ifPresent(simul -> {
            Simul2k simul2k = simul.getSimul2k();
            sendRoutesToSimulator(conductorImpl.getRoutesContainers(), simul2k);
            simul2k.onExecStart();
        });

        return Pair.of(wasRunning, file);
    }

    private void sendRoutesToSimulator(List<IRoutesContainer> routesContainers, Simul2k routeManager) {
        routesContainers.forEach(active ->
                active.getRoutes()
                        .stream()
                        .filter(r -> r instanceof ISequenceRoute)
                        .findFirst()
                        .ifPresent(route -> {
                ISequenceRoute sequenceRoute = (ISequenceRoute) route;
                SimulRouteGraph graph = sequenceRoute.toSimulGraph();
                routeManager.setRoute(
                        sequenceRoute.getThrottle().getDccAddress(),
                        sequenceRoute.getMinSecondsOnBlock(),
                        sequenceRoute.getMaxSecondsOnBlock(),
                        graph);
        }));
    }

    @NonNull
    @Override
    public List<IThrottleDisplayAdapter> getThrottles() {
        List<IThrottleDisplayAdapter> list = new ArrayList<>();
        mScript2kContext.getScript2kComponent().ifPresent(component ->
            component.getScript2kLoader().getConductorImpl().getThrottles().forEach(
                (address, throttle) -> list.add(new IThrottleDisplayAdapter() {
                    @Override
                    public String getName() {
                        return throttle.getName();
                    }

                    @Override
                    public int getDccAddress() {
                        return address;
                    }

                    @Override
                    public int getSpeed() {
                        return throttle.getSpeed().getSpeed();
                    }

                    @Override
                    public boolean isLight() {
                        return throttle.getLight();
                    }

                    @Override
                    public boolean isSound() {
                        return throttle.getSound();
                    }

                    @Override
                    public int getActivationsCount() {
                        return throttle.getActivationCount();
                    }

                    @Override
                    public String getStatus() {
                        IRoutesContainer container = component
                                .getScript2kLoader()
                                .getConductorImpl()
                                .routesContainerForThrottle(throttle);
                        if (container == null) return "";
                        return container.getStatus().invoke();
                    }
                })));
        return list;
    }

    @Override
    public List<ISensorDisplayAdapter> getSensors() {
        List<ISensorDisplayAdapter> list = new ArrayList<>();
        mScript2kContext.getScript2kComponent().ifPresent(component ->
                component.getScript2kLoader().getConductorImpl().getSensors().forEach(
                        (name, sensor) -> list.add(new ISensorDisplayAdapter() {
                            @Override
                            public String getName() {
                                return sensor.getName();
                            }

                            @Override
                            public boolean isActive() {
                                return sensor.getActive();
                            }

                            @Override
                            public void setActive(boolean isActive) {
                                sensor.active(isActive);
                            }

                            @Override
                            public Optional<BlockState> getBlockState() {
                                return Optional.empty();
                            }
                        })));
        return list;
    }

    @Override
    public List<IActivableDisplayAdapter> getBlocks() {
        List<IActivableDisplayAdapter> list = new ArrayList<>();
        mScript2kContext.getScript2kComponent().ifPresent(component ->
                component.getScript2kLoader().getConductorImpl().getBlocks().forEach(
                        (name, block) -> list.add(new IActivableDisplayAdapter() {
                            @Override
                            public String getName() {
                                return name;
                            }

                            @Override
                            public boolean isActive() {
                                return block.getActive();
                            }

                            @Override
                            public Optional<BlockState> getBlockState() {
                                switch (block.getState()) {
                                    case EMPTY: return Optional.of(BlockState.BLOCK_EMPTY);
                                    case OCCUPIED: return Optional.of(BlockState.BLOCK_OCCUPIED);
                                    case TRAILING: return Optional.of(BlockState.BLOCK_TRAILING);
                                }
                                throw new IllegalStateException("Missing Block State");
                            }
                        })));
        return list;
    }

    @Override
    public List<IActivableDisplayAdapter> getTurnouts() {
        List<IActivableDisplayAdapter> list = new ArrayList<>();
        mScript2kContext.getScript2kComponent().ifPresent(component ->
                component.getScript2kLoader().getConductorImpl().getTurnouts().forEach(
                        (name, turnout) -> list.add(new IActivableDisplayAdapter() {
                            @Override
                            public String getName() {
                                return name;
                            }

                            @Override
                            public boolean isActive() {
                                return turnout.getActive();
                            }
                        })));
        return list;
    }

    @Override
    public Optional<MapInfo> getLoadedMapName() {
        Optional<ConductorImpl> script = mScript2kContext.getScript2kComponent().map(
                c -> c.getScript2kLoader().getConductorImpl());
        File scriptDir = mScript2kContext.getScript2kComponent().map(
                c -> c.getScript2kLoader().getScriptSource().scriptDir()).orElse(null);

        if (script.isPresent()) {
            List<ISvgMap> svgMaps = script.get().getSvgMaps();
            Optional<ISvgMap> svgMap = svgMaps
                    .stream()
                    // Only Conductor maps are displayed here
                    .filter(map -> map.getDisplayOn() == SvgMapTarget.Conductor)
                    .findFirst();
            if (svgMap.isPresent()) {
                try {
                    return Optional.of(((SvgMap) (svgMap.get())).toMapInfo(mFileOps, scriptDir));
                } catch (IOException e) {
                    String error = "SvgMap[" + svgMap.get().getName() + "]: Failed to read file '" + svgMap.get().getSvg() + "'.";
                    mLogger.d(TAG, error, e);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public void appendToLog(StringBuilder status) {
        Optional<Script2kLoader> loader = mScript2kContext.getScript2kComponent()
                .map(IScript2kComponent::getScript2kLoader);
        if (!loader.isPresent()) {
            return;
        }

        String scriptName = getScriptFile()
                .map(File::getPath)
                .orElse("<Undefined>");

        switch (loader.get().getStatus()) {
        case NotLoaded:
            status.append("--- [ NO SCRIPT LOADED ] ---\n");
            break;
        case Loading:
            status.append("--- [ LOADING ] ---\n");
            status.append("Script: ")
                    .append(scriptName)
                    .append('\n')
                    .append('\n');
            break;
        case Loaded:
            status.append("--- [ SCRIPT ] ---\n");
            List<String> errors = loader.get().getResultErrors();
            status.append("Loaded ")
                    .append(scriptName)
                    .append(errors.isEmpty() ? " without" : " with")
                    .append(" errors\n");

            if (!errors.isEmpty()) {
                status.append("\n--- [ LAST ERROR ] ---\n\u2022");
                status.append(Joiner.on("\n\u2022 ").join(errors))
                        .append('\n')
                        .append('\n');
            }

            try {
                appendVarStatus(status,
                        loader.get().getConductorImpl(),
                        loader.get().getExecEngine());
            } catch (ConcurrentModificationException ignore) {
            }
        }
    }

    private static void appendVarStatus(
            StringBuilder outStatus,
            ConductorImpl script,
            ExecEngine2k engine) {

        outStatus.append("Freq: ");
        outStatus.append(String.format("%.1f Hz  [%.1f Hz]\n",
                engine.getActualFrequency(),
                engine.getMaxFrequency()));

        outStatus.append("\n--- [ ROUTES ] ---\n");
        for (IRoutesContainer routesContainer : script.getRoutesContainers()) {
            outStatus.append(routesContainer.getName())
                    .append(": ")
                    .append(routesContainer.getLogStatus())
                    .append('\n');
        }
        appendNewLine(outStatus);

        outStatus.append("\n--- [ TURNOUTS ] ---\n");
        int i = 0;
        for (Map.Entry<String, ITurnout> entry : script.getTurnouts().entrySet()) {
            String name = entry.getKey();
            ITurnout turnout = entry.getValue();
            outStatus.append(name.toUpperCase()).append(": ").append(turnout.getActive() ? 'N' : 'R');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("\n--- [ BLOCKS ] ---\n");
        i = 0;
        for (Map.Entry<String, IBlock> entry : script.getBlocks().entrySet()) {
            String name = entry.getKey();
            IBlock block = entry.getValue();
            outStatus.append(name.toUpperCase()).append(": ").append(block.getActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("\n--- [ SENSORS ] ---\n");
        i = 0;
        for (Map.Entry<String, ISensor> entry : script.getSensors().entrySet()) {
            String name = entry.getKey();
            ISensor sensor = entry.getValue();
            outStatus.append(name.toUpperCase()).append(": ").append(sensor.getActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("\n--- [ TIMERS ] ---\n");
        i = 0;
        for (ITimer timer : script.getTimers()) {
            outStatus.append(timer.getName()).append(':').append(timer.getActive() ? '1' : '0');
            outStatus.append((i++) % 4 == 3 ? "\n" : "   ");
        }
        appendNewLine(outStatus);

        outStatus.append("\n--- [ VARS ] ---\n");
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
    @Component(modules = { CommonModule.class, Script2kModule.class })
    public interface LocalComponent2k extends IEngine2kComponent {
        void inject(EntryPoint2 entryPoint);
        void inject(Engine2KotlinAdapter adapter);

        @Component.Factory
        interface Factory {
            LocalComponent2k createComponent(@BindsInstance IJmriProvider jmriProvider);
        }
    }
}
