package com.alfray.conductor.script;

/**
 * A script item that can produce a value.
 */
public interface IValue<T> {
    /** Retrieves the value. */
    T getValue();

    /** A script item that can produce an int value. */
    public interface Int extends IValue<Integer> {}
}
