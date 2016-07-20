package com.softdesign.devintensive.ui.callbacks;

public interface BaseTaskCallbacks {

    void onRequestStarted();

    void onRequestFinished();

    void onRequestFailed(String error);
}
