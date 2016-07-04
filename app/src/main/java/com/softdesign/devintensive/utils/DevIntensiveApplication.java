package com.softdesign.devintensive.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Used to get SharedPreferences
 */
public class DevIntensiveApplication extends Application{
    private static SharedPreferences sSharedPreferences;
    private static Context mContext;

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mContext = this;

    }

    public static SharedPreferences getSharedPreferences() {
        return sSharedPreferences;
    }
}
