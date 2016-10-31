package com.alfray.conductor.script;

public interface IValue<T> {
    T getValue();

    public interface Int extends IValue<Integer> {}
}
