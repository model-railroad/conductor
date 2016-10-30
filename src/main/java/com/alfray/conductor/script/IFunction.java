package com.alfray.conductor.script;

public interface IFunction <T> {
    void setValue(T value);

    public interface Int extends IFunction<Integer> {}
}
