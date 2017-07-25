package com.alflabs.conductor.script;

import java.util.function.IntSupplier;

/**
 * A script item that can produce an int value.
 * <p/>
 * The effective signature of a script value is () -> int.
 * <p/>
 * <p/>
 * In the java 8 functional interfaces names, this is called an "int supplier".
 */
public interface IIntValue extends IntSupplier {
}
