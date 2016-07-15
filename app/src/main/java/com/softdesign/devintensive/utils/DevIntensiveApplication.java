package com.softdesign.devintensive.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

/**
 * Used to get SharedPreferences
 */
public class DevIntensiveApplication extends Application {
    private static SharedPreferences sSharedPreferences;
    private static Context sContext;

    public static Context getContext() {
        return sContext;
    }

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                // VKAccessToken is invalid
                VKAccessToken.removeTokenAtKey(sContext, ConstantManager.VK_ACCESS_TOKEN);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sContext = this;
        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);
    }

    public static SharedPreferences getSharedPreferences() {
        return sSharedPreferences;
    }
}