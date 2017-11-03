package com.alflabs.conductor.parser;

public class TestReporter extends Reporter {
    private String mReport = "";

    public TestReporter() {
        super(null);
    }

    @Override
    public void log(String msg) {
        if (!mReport.isEmpty() && !mReport.endsWith("\n")) {
            mReport += "\n";
        }
        mReport += msg;
    }

    @Override
    public String toString() {
        return mReport;
    }
}
