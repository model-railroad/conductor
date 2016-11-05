package com.alfray.conductor.script;

import java.util.function.IntSupplier;

/**
 * A script item that can produce a value.
 * <p/>
 * The effective signature of a script value is () -> int.
 * <p/>
 * <p/>
 * In the java 8 functional interfaces names, this is called an "int supplier".
 */
public interface IIntValue extends IntSupplier {
}
