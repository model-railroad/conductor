package com.alflabs.conductor.parser;

class TestReporter extends Reporter {
    private String report = "";

    public TestReporter() {
        super(null);
    }

    @Override
    public void log(String msg) {
        report += msg;
    }

    @Override
    public String toString() {
        return report;
    }
}
