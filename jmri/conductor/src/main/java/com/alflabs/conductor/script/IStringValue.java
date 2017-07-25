package com.alflabs.conductor.script;

import java.util.function.Supplier;

/**
 * A script item that can produce an int value.
 * <p/>
 * In the java 8 functional interfaces names, this is called a "supplier".
 */
public interface IStringValue extends Supplier<String> {
}
