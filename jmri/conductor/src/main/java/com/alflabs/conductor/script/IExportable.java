package com.alflabs.conductor.script;

/**
 * A script data-type that can be optionally exported to the KV server.
 */
public interface IExportable {
    /**
     * True if this data type should be exported to the KV server during
     * each {@link ExecEngine#onExecHandle()} loop.
     */
    void setExported(boolean exported);
}
