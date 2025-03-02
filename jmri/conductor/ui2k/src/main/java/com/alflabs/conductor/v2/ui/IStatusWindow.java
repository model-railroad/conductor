/*
 * Project: Conductor
 * Copyright (C) 2024 alf.labs gmail com,
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

package com.alflabs.conductor.v2.ui;

import com.alflabs.conductor.v2.IActivableDisplayAdapter;
import com.alflabs.conductor.v2.ISensorDisplayAdapter;
import com.alflabs.conductor.v2.IThrottleDisplayAdapter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;

public interface IStatusWindow {
    void open(@Nonnull IWindowCallback windowCallback);
    void updateScriptName(@Nonnull String scriptName);

    void setSimulationMode(boolean isSimulation);
    void enterKioskMode();

    /**
     * Fill SVG using svgDocument (as text).
     * If svgDocument is null or empty, rely only on mapUrl.
     * Note: the mapUrl is only used as a string below, however we use java.net.URI
     * to force callers to provide a valid URI.
     */
    void displaySvgMap(@Nullable String svgDocument, @Nonnull URI mapUrl);

    void updateUI();
    void updateMainLog(@Nonnull String logText);
    void updateSimuLog(@Nonnull String logText);
    void updatePause(boolean isPaused);
    void clearUpdates();

    void registerThrottles(@Nonnull List<IThrottleDisplayAdapter> throttles);

    void registerActivables(
            @Nonnull List<ISensorDisplayAdapter> sensors,
            @Nonnull List<IActivableDisplayAdapter> blocks,
            @Nonnull List<IActivableDisplayAdapter> turnouts);
}
