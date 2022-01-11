package com.alflabs.conductor.v2.ui;

public interface IWindowCallback {
    void onQuit();
    void onWindowReload();
    void onWindowPause();
    void onWindowSvgLoaded();
    void onWindowSvgClick(String itemId);
}
