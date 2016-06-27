package com.softdesign.devintensive.data.managers;

public class DataManager {
    private static DataManager ourInstance = new DataManager();
    private PreferencesManager mPreferencesManager;

    public static DataManager getInstance() {
        return ourInstance;
    }

    private DataManager() {
        this.mPreferencesManager = new PreferencesManager();
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }
}
