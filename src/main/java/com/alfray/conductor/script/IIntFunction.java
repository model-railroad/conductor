package com.alfray.conductor.script;

import java.util.function.IntConsumer;

/**
 * A script function-like item that can receive a value.
 * <p/>
 * The effective signature of a script function is (int) -> void.
 * <p/>
 * In the java 8 functional interfaces names, a "Function" always returns a value.
 * The equivalent of "(int) -> void" is a "consumer".
 */
public interface IIntFunction extends IntConsumer {
}
