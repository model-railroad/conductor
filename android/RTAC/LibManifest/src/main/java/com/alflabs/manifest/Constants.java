/*
 * Project: LibManifest
 * Copyright (C) 2017 alf.labs gmail com,
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

package com.alflabs.manifest;

/**
 * Constants used by {@code Conductor} when exporting KV values to RTAC.
 */
public final class Constants {
    public static final String KV_SERVER_SERVICE_TYPE = "_kv-conductor._tcp.";
    public static final int KV_SERVER_PORT = 20006;

    public static final String On = "ON";
    public static final String Off = "OFF";

    public static final String Normal = "N";
    public static final String Reverse = "R";

    public static final String MapsKey = Prefix.Map + "maps";
    public static final String RoutesKey = Prefix.Route + "routes";

    public static final String RtacMotion  = Prefix.Var + "rtac-motion";
    public static final String RtacPsaText = Prefix.Var + "rtac-psa-text";

    public static final String GAId= Prefix.Var + "$ga-id$";

    public static final String EStopKey = Prefix.Var + "$estop-state$";
    public enum EStopState {
        NORMAL,
        ACTIVE,
        RESET
    }
}
