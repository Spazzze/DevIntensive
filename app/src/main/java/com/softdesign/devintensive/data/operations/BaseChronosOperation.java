package com.softdesign.devintensive.data.operations;

import android.content.SharedPreferences;

import com.redmadrobot.chronos.ChronosOperation;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

public abstract class BaseChronosOperation<Output> extends ChronosOperation<Output> {

    public static final DataManager DATA_MANAGER = DataManager.getInstance();
    public static final SharedPreferences SHARED_PREFERENCES = DevIntensiveApplication.getSharedPreferences();

    public Action mAction;

    public enum Action {
        CLEAR,
        SAVE,
        LOAD,
        SWAP,
    }
}

