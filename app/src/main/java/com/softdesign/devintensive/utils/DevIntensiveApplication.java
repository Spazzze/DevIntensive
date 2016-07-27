package com.softdesign.devintensive.utils;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.stetho.Stetho;
import com.softdesign.devintensive.data.storage.models.DaoMaster;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKSdk;

/**
 * Used to get SharedPreferences
 */
public class DevIntensiveApplication extends Application {
    private static SharedPreferences sSharedPreferences;
    private static Context sContext;
    private static Activity sActivity;
    private static DaoSession sDaoSession;
    private static int sScreenWidth;

    public static Context getContext() {
        return sContext;
    }

    /**
     * Tracks if vk token is valid
     */
    private final VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                // VKAccessToken is invalid
                VKAccessToken.removeTokenAtKey(sContext, Const.VK_ACCESS_TOKEN);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sContext = this;
        sScreenWidth = AppUtils.getScreenWidth();

        vkAccessTokenTracker.startTracking();
        VKSdk.initialize(this);

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, AppConfig.DB_NAME);
        sDaoSession = new DaoMaster(helper.getWritableDb()).newSession();

        Stetho.initializeWithDefaults(this);
    }

    public static SharedPreferences getSharedPreferences() {
        return sSharedPreferences;
    }

    public static DaoSession getDaoSession() {
        return sDaoSession;
    }

    public static int getScreenWidth() {
        return sScreenWidth;
    }

    public static void setCurrentActivity(Activity currentActivity) {
        sActivity = currentActivity;
    }

    public static Activity getCurrentActivity() {
        return sActivity;
    }
}