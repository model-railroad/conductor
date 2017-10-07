package com.alflabs.manifest;

/**
 * Constants used by {@code Conductor} when exporting KV values to RTAC.
 */
public final class Constants {
    public static final String KV_SERVER_SERVICE_TYPE = "_kv-conductor._tcp.";
    public static final int KV_SERVER_PORT = 8080;

    public static final String On = "ON";
    public static final String Off = "OFF";

    public static final String Normal = "N";
    public static final String Reverse = "R";

    public static final String MapsKey = Prefix.Map + "maps";
    public static final String RoutesKey = Prefix.Route + "routes";
}
