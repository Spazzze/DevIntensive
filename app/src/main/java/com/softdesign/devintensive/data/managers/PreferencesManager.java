package com.softdesign.devintensive.data.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.vk.sdk.VKAccessToken;

import java.util.Date;

import static com.softdesign.devintensive.utils.AppUtils.getObjectFromJson;

/**
 * saves and loads Shared Preferences of this app
 */
public class PreferencesManager {
    private final SharedPreferences mSharedPreferences;
    private final Context mContext;

    public PreferencesManager() {
        mSharedPreferences = DevIntensiveApplication.getSharedPreferences();
        mContext = DevIntensiveApplication.getContext();
    }

    //region User Data save & load

    public User loadAllUserData() {
        String json = mSharedPreferences.getString(Const.USER_JSON_OBJ, null);
        if (json != null) return (User) getObjectFromJson(json, User.class);
        else return null;
    }

    public String loadUserPhoto() {
        return mSharedPreferences.getString(Const.USER_PROFILE_PHOTO_URI, "");
    }

    public String loadUserAvatar() {
        return mSharedPreferences.getString(Const.USER_PROFILE_AVATAR_URI, "");
    }
    //endregion

    //region Our Primary Auth
    public void saveBuiltInAuthInfo(String id, String token) {
        if (id != null && token != null && !id.isEmpty() && !token.isEmpty()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Const.BUILTIN_ACCESS_USER_ID, id);
            editor.putString(Const.BUILTIN_ACCESS_TOKEN, token);
            editor.apply();
        }
    }

    public String loadBuiltInAuthId() {
        return mSharedPreferences.getString(Const.BUILTIN_ACCESS_USER_ID, "");
    }

    public String loadBuiltInAuthToken() {
        return mSharedPreferences.getString(Const.BUILTIN_ACCESS_TOKEN, "");
    }
    //endregion

    //region Vk Auth
    public void saveVKAuthorizationInfo(VKAccessToken res) {
        if (res != null) {
            res.saveTokenToSharedPreferences(mContext, Const.VK_ACCESS_TOKEN);
        }
    }

    @SuppressWarnings("unused")
    public VKAccessToken loadVKToken() {
        return VKAccessToken.tokenFromSharedPreferences(mContext, Const.VK_ACCESS_TOKEN);
    }
    //endregion

    //region DB
    public boolean isDBNeedsUpdate() {
        long updatedTime = mSharedPreferences.getLong(Const.DB_UPDATED_TIME_KEY, 0);
        return (new Date().getTime() - updatedTime) > AppConfig.DB_REFRESH_RATE;
    }
    //endregion
}