package com.softdesign.devintensive.data.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.softdesign.devintensive.utils.UiHelper.getJsonFromObject;
import static com.softdesign.devintensive.utils.UiHelper.getObjectFromJson;

/**
 * saves and loads Shared Preferences of this app
 */
public class PreferencesManager {
    private static final String TAG = Const.TAG_PREFIX + "PreferencesManager";

    private final SharedPreferences mSharedPreferences;
    private final Context mContext;

    public PreferencesManager() {
        mSharedPreferences = DevIntensiveApplication.getSharedPreferences();
        mContext = DevIntensiveApplication.getContext();
    }
    //region User Data save & load

    public void saveAllUserData(User res) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(Const.USER_JSON_OBJ, getJsonFromObject(res, User.class));
        editor.apply();
    }

    public User loadAllUserData() {
        String json = mSharedPreferences.getString(Const.USER_JSON_OBJ, null);
        if (json != null) return (User) getObjectFromJson(json, User.class);
        else return null;
    }

    //endregion

    //region User Photo
    public void saveUserPhoto(Uri uri) {
        if (uri != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Const.USER_PROFILE_PHOTO_URI, uri.toString());
            editor.apply();
        }
    }

    public Uri loadUserPhoto() {
        return Uri.parse(mSharedPreferences.getString(Const.USER_PROFILE_PHOTO_URI,
                ""));
    }
    //endregion

    //region User Avatar
    public void saveUserAvatar(String uri) {
        if (uri != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(Const.USER_PROFILE_AVATAR_URI, uri);
            editor.apply();
        }
    }

    public String loadUserAvatar() {
        return mSharedPreferences.getString(Const.USER_PROFILE_AVATAR_URI, "");
    }
    //endregion

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

    //region General auth methods

    public void softLogout() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        List<String> exclusionKeys = new ArrayList<>();
        if (mSharedPreferences.getBoolean(Const.SAVE_LOGIN, false)) {
            exclusionKeys.add(Const.SAVE_LOGIN);
            exclusionKeys.add(Const.SAVED_LOGIN_NAME);
            exclusionKeys.add(Const.BUILTIN_ACCESS_USER_ID);
            exclusionKeys.add(Const.BUILTIN_ACCESS_TOKEN);
        }

        Map<String, ?> spMap = mSharedPreferences.getAll();
        for (String key : spMap.keySet()) {
            if (!exclusionKeys.contains(key)) {
                editor.remove(key);
            }
        }
        editor.apply();
    }

    /**
     * totally removes all current users auth data
     */
    public void totalLogout() {
        //removing all received tokens and auth status
        VKSdk.logout();                         //vk logout
        VKAccessToken.removeTokenAtKey(mContext, Const.VK_ACCESS_TOKEN);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear().apply();
        DataManager.getInstance().clearDatabases();
    }
    //endregion

    //region DB
    public void saveDBUpdateTime() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(Const.DB_UPDATED_TIME_KEY, new Date().getTime());
        editor.apply();
    }

    public boolean isDBNeedsUpdate() {
        long updatedTime = mSharedPreferences.getLong(Const.DB_UPDATED_TIME_KEY, 0);
        return (new Date().getTime() - updatedTime) > AppConfig.DB_REFRESH_RATE;
    }
    //endregion
}