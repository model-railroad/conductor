package com.alfray.conductor.script;

/**
 * A script function-like item that can receive a value.
 */
public interface IFunction <T> {
    /** Executes the function by providing it with a value. */
    void setValue(T value);

    /** A script function-like item that can receive an int value. */
    interface Int extends IFunction<Integer> {}
}
