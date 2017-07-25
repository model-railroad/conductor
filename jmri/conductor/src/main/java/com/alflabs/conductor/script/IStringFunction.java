package com.alflabs.conductor.script;

import java.util.function.Consumer;

/**
 * A script function-like item that can receive a string value.
 * <p/>
 * Note: In the java 8 functional interfaces names, a "Function" always returns a value.
 * The equivalent of "(string) -> void" is a "consumer".
 */
public interface IStringFunction extends Consumer<String> {
}
