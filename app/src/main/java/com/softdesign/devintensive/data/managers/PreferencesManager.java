package com.softdesign.devintensive.data.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.vk.sdk.VKAccessToken;

import java.util.ArrayList;
import java.util.List;

/**
 * saves and loads Shared Preferences of this app
 */
public class PreferencesManager {

    public static final String TAG = ConstantManager.TAG_PREFIX + "PreferencesManager";
    private final SharedPreferences mSharedPreferences;
    private final Context mContext;
    private static final String[] USER_FIELDS = {ConstantManager.USER_PHONE_KEY, ConstantManager.USER_EMAIL_KEY, ConstantManager.USER_VK_KEY, ConstantManager.USER_GITHUB_KEY, ConstantManager.USER_ABOUT_KEY};

    public PreferencesManager() {
        mSharedPreferences = DevIntensiveApplication.getSharedPreferences();
        mContext = DevIntensiveApplication.getContext();
    }

    public void saveUserProfileData(List<String> userFields) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i < USER_FIELDS.length; i++) {
            editor.putString(USER_FIELDS[i], userFields.get(i));
        }
        editor.apply();
    }

    public List<String> loadUserProfileData() {
        List<String> userFields = new ArrayList<>();
        List<String> defaultUserData = new ArrayList<>();
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.dummy_profile_phone));
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.dummy_profile_email));
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.dummy_profile_vk));
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.dummy_profile_gitHubRepo));
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.dummy_profile_about));
        for (int i = 0; i < USER_FIELDS.length; i++) {
            userFields.add(mSharedPreferences.getString(USER_FIELDS[i], defaultUserData.get(i)));
        }
        return userFields;
    }

    public void saveUserPhoto(Uri uri) {
        if (uri != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.USER_PROFILE_PHOTO_URI, uri.toString());
            editor.apply();
        }
    }

    public Uri loadUserPhoto() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_PROFILE_PHOTO_URI,
                ""));
    }

    public void saveVKAuthorizationInfo(VKAccessToken res) {
        if (res != null) {
            res.saveTokenToSharedPreferences(mContext, ConstantManager.VK_ACCESS_TOKEN);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.AUTHORIZATION_SYSTEM, ConstantManager.VK);
            editor.apply();
        }
    }

    public Boolean checkAuthorizationStatus() {
        switch (mSharedPreferences.getString(ConstantManager.AUTHORIZATION_SYSTEM, "")) {
            case ConstantManager.VK:
                VKAccessToken token = VKAccessToken.tokenFromSharedPreferences(mContext, ConstantManager.VK_ACCESS_TOKEN);
                return token != null && !token.isExpired();
            case ConstantManager.FACEBOOK:
                return false;
            case ConstantManager.BUILTIN:
                return false;
        }
        return false;
    }

    public void removeCurrentAuthorization() {
        //removing all received tokens and auth status
        VKAccessToken.removeTokenAtKey(mContext, ConstantManager.VK_ACCESS_TOKEN);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(ConstantManager.AUTHORIZATION_SYSTEM);
        editor.apply();
    }
}