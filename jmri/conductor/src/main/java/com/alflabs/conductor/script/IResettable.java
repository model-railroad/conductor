package com.alflabs.conductor.script;

/**
 * A script data-type that can be optionally reset.
 */
public interface IResettable {
    /**
     * Called by the {@link ExecEngine} to reset the data type to whatever
     * is its initial value when the script first starts.
     */
    void reset();
}
