package com.softdesign.devintensive.data.managers;

/**
 * Used to manage all data
 */
public class DataManager {
    private static final DataManager ourInstance = new DataManager();
    private final PreferencesManager mPreferencesManager;

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