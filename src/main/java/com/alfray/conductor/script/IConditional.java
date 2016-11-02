package com.alfray.conductor.script;

/**
 * A script item that can be conditionally active or inactive.
 */
public interface IConditional {
    /** Returns true if the condition is active or true. */
    boolean isActive();
}
