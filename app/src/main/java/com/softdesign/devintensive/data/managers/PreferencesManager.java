package com.softdesign.devintensive.data.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

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

    public void saveAuthorizationSystem(String system) {
        if (system != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.AUTHORIZATION_SYSTEM, system);
            editor.apply();
        }
    }

    public String getAuthorizationSystem() {
        return mSharedPreferences.getString(ConstantManager.AUTHORIZATION_SYSTEM, "");
    }

    public void saveVKAuthorizationInfo(VKAccessToken res) {
        if (res != null) {
            saveAuthorizationSystem(ConstantManager.AUTH_VK);
            res.saveTokenToSharedPreferences(mContext, ConstantManager.VK_ACCESS_TOKEN);
        }
    }

    public VKAccessToken loadVKAuthorizationInfo() {
        return VKAccessToken.tokenFromSharedPreferences(mContext, ConstantManager.VK_ACCESS_TOKEN);
    }

    public void saveGoogleAuthorizationInfo(String accountName, String accountType, String token) {
        if (token != null && accountName != null && accountType != null) {
            saveAuthorizationSystem(ConstantManager.AUTH_GOOGLE);
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.GOOGLE_ACCESS_ACC_NAME, accountName);
            editor.putString(ConstantManager.GOOGLE_ACCESS_ACC_TYPE, accountType);
            editor.putString(ConstantManager.GOOGLE_ACCESS_TOKEN, token);
            editor.apply();
        }
    }

    public List<String> loadGoogleAuthorizationInfo() {
        List<String> authDataList = new ArrayList<>();
        authDataList.add(mSharedPreferences.getString(ConstantManager.GOOGLE_ACCESS_ACC_NAME, ""));
        authDataList.add(mSharedPreferences.getString(ConstantManager.GOOGLE_ACCESS_ACC_TYPE, ""));
        authDataList.add(mSharedPreferences.getString(ConstantManager.GOOGLE_ACCESS_TOKEN, ""));
        return authDataList;
    }

    public Boolean checkAuthorizationStatus() {   //// TODO: 08.07.2016 временно. Переделать на авторизацию сервером
        switch (getAuthorizationSystem()) {
            case ConstantManager.AUTH_VK:
                VKAccessToken vkToken = VKAccessToken.tokenFromSharedPreferences(mContext, ConstantManager.VK_ACCESS_TOKEN);
                return vkToken != null && !vkToken.isExpired();
            case ConstantManager.AUTH_FACEBOOK:
                AccessToken fbToken = AccessToken.getCurrentAccessToken();
                return fbToken != null && !fbToken.isExpired();
            case ConstantManager.AUTH_GOOGLE:
                //а хрен вам а не проверка на expired, мы Гугл, мы шлем вас снова отправить запрос авторизации.
                return !(loadGoogleAuthorizationInfo().get(2)).isEmpty();   //токен пуст, если после последней авторизации вызывался метод onDestroy()
            case ConstantManager.AUTH_BUILTIN:
                //// TODO: 08.07.2016 доделать авторизацию через devIntensive
                return false;
            default:
                return false;
        }
    }

    public void removeGoogleAuthorizationOnDestroy() {
        //removing google token on exit
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(ConstantManager.GOOGLE_ACCESS_TOKEN);
        editor.apply();
    }

    /**
     * totally removes all current users auth data
     */
    public void removeCurrentAuthorization() {
        //removing all received tokens and auth status
        VKSdk.logout();                         //vk logout
        LoginManager.getInstance().logOut();    //fb logout
        VKAccessToken.removeTokenAtKey(mContext, ConstantManager.VK_ACCESS_TOKEN);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(ConstantManager.AUTHORIZATION_SYSTEM);
        editor.remove(ConstantManager.GOOGLE_ACCESS_ACC_NAME);
        editor.remove(ConstantManager.GOOGLE_ACCESS_ACC_TYPE);
        editor.remove(ConstantManager.GOOGLE_ACCESS_TOKEN);
        editor.apply();
    }
}