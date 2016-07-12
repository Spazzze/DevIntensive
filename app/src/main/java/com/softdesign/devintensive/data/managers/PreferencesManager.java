package com.softdesign.devintensive.data.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * saves and loads Shared Preferences of this app
 */
public class PreferencesManager {

    public static final String TAG = ConstantManager.TAG_PREFIX + "PreferencesManager";
    private final SharedPreferences mSharedPreferences;
    private final Context mContext;
    private static final String[] USER_FIELDS = {
            ConstantManager.USER_PHONE_KEY,
            ConstantManager.USER_EMAIL_KEY,
            ConstantManager.USER_VK_KEY,
            ConstantManager.USER_GITHUB_KEY,
            ConstantManager.USER_ABOUT_KEY};
    private static final String[] USER_PROFILE_VALUES = {
            ConstantManager.USER_PROFILE_RATING_KEY,
            ConstantManager.USER_PROFILE_LINES_CODE_KEY,
            ConstantManager.USER_PROFILE_PROJECTS_KEY};

    //// TODO: 12.07.2016 ревью save&load

    public PreferencesManager() {
        mSharedPreferences = DevIntensiveApplication.getSharedPreferences();
        mContext = DevIntensiveApplication.getContext();
    }

    public Boolean isEmpty() {
        return mSharedPreferences.getAll().isEmpty();
    }

    //region User Data save & load
    public String loadLogin() {
        if (isLoginSavingEnabled())
            return mSharedPreferences.getString(ConstantManager.SAVED_LOGIN, "");
        else return "";
    }
    public Boolean isLoginSavingEnabled(){
        return mSharedPreferences.getBoolean(ConstantManager.SAVE_LOGIN, false);
    }

    public void saveLogin(String login) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(ConstantManager.SAVE_LOGIN, true);
        editor.putString(ConstantManager.SAVED_LOGIN, login);
        editor.apply();
    }

    public void saveUserName(String userFirstName, String userLastName) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_FIRST_NAME_KEY, userFirstName);
        editor.putString(ConstantManager.USER_LAST_NAME_KEY, userLastName);
        editor.putString(ConstantManager.USER_FULL_NAME_KEY, String.format("%s %s", userLastName, userFirstName));
        editor.apply();
    }

    public Map<String, String> loadUserName() {
        Resources res = DevIntensiveApplication.getContext().getResources();
        Map<String, String> namesMap = new HashMap<>();
        namesMap.put(ConstantManager.USER_FIRST_NAME_KEY, mSharedPreferences.getString(ConstantManager.USER_FIRST_NAME_KEY, ""));
        namesMap.put(ConstantManager.USER_LAST_NAME_KEY, mSharedPreferences.getString(ConstantManager.USER_FIRST_NAME_KEY, ""));
        namesMap.put(ConstantManager.USER_FULL_NAME_KEY, mSharedPreferences.getString(ConstantManager.USER_FULL_NAME_KEY, res.getString(R.string.dummy_app_name)));
        return namesMap;
    }

    public void saveUserProfileData(Map<String, String> userFieldsMap) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (Map.Entry<String, String> pair : userFieldsMap.entrySet()) {
            editor.putString(pair.getKey(), pair.getValue());
        }

        editor.apply();
    }

    public List<String> loadUserProfileData() {
        Log.d(TAG, "loadUserProfileData: ");
        List<String> userFields = new ArrayList<>();
        List<String> defaultUserData = new ArrayList<>();
        Resources res = DevIntensiveApplication.getContext().getResources();
        defaultUserData.add(res.getString(R.string.dummy_profile_phone));
        defaultUserData.add(res.getString(R.string.dummy_profile_email));
        defaultUserData.add(res.getString(R.string.dummy_profile_vk));
        defaultUserData.add(res.getString(R.string.dummy_profile_gitHubRepo));
        defaultUserData.add(res.getString(R.string.dummy_profile_about));
        for (int i = 0; i < USER_FIELDS.length; i++) {
            userFields.add(mSharedPreferences.getString(USER_FIELDS[i], defaultUserData.get(i)));
        }
        return userFields;
    }

    public List<String> loadUserAdditionalGitHubRepo() {
        List<String> userFields = new ArrayList<>();

        //достаем дополнительные ссылки на репо Гитхаба
        for (int i = 1; i < mSharedPreferences.getAll().size(); i++) {
            String key = ConstantManager.USER_GITHUB_KEY + i;
            if (mSharedPreferences.contains(key)) {
                String s = mSharedPreferences.getString(key, "");
                if (!s.isEmpty()) userFields.add(mSharedPreferences.getString(key, ""));
            } else break;
        }

        return userFields;
    }

    public void saveUserProfileValues(int[] userProfileValues) {

        if (USER_PROFILE_VALUES.length != userProfileValues.length) return;

        SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (int i = 0; i < USER_PROFILE_VALUES.length; i++) {
            editor.putString(USER_PROFILE_VALUES[i], String.valueOf(userProfileValues[i]));
        }
        editor.apply();
    }

    public List<String> loadUserProfileValues() {
        List<String> userFields = new ArrayList<>();
        for (String s : USER_PROFILE_VALUES) {
            userFields.add(mSharedPreferences.getString(s, "0"));
        }
        return userFields;
    }

    public void saveUserPhoto(Uri uri) {
        Log.d(TAG, "saveUserPhoto: " + uri);
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

    public void saveUserAvatar(String uri) {
        Log.d(TAG, "saveUserAvatar: " + uri);
        if (uri != null) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.USER_PROFILE_AVATAR_URI, uri);
            editor.apply();
        }
    }

    public String loadUserAvatar() {
        return mSharedPreferences.getString(ConstantManager.USER_PROFILE_AVATAR_URI, "");
    }

    //endregion

    //region Our Primary Auth
    public void saveBuiltInAuthInfo(String id, String token) {
        if (id != null && token != null && !id.isEmpty() && !token.isEmpty()) {
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putString(ConstantManager.BUILTIN_ACCESS_ID, id);
            editor.putString(ConstantManager.BUILTIN_ACCESS_TOKEN, token);
            editor.apply();
        }
    }

    public String loadBuiltInAuthId() {
        return mSharedPreferences.getString(ConstantManager.BUILTIN_ACCESS_ID, "");
    }

    public String loadBuiltInAuthToken() {
        return mSharedPreferences.getString(ConstantManager.BUILTIN_ACCESS_TOKEN, "");
    }
    //endregion

    //region Vk Auth
    public void saveVKAuthorizationInfo(VKAccessToken res) {
        if (res != null) {
            saveAuthorizationSystem(ConstantManager.AUTH_VK);
            res.saveTokenToSharedPreferences(mContext, ConstantManager.VK_ACCESS_TOKEN);
        }
    }

    public VKAccessToken loadVKToken() {
        return VKAccessToken.tokenFromSharedPreferences(mContext, ConstantManager.VK_ACCESS_TOKEN);
    }
    //endregion

    //region Google Auth
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

    public void removeGoogleAuthorizationOnDestroy() {
        //removing google token on exit
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(ConstantManager.GOOGLE_ACCESS_TOKEN);
        editor.apply();
    }
    //endregion

    //region General auth methods
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
                return false;
            default:
                return false;
        }
    }

    public void softLogout() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        List<String> exclusionKeys = new ArrayList<>();
        if (mSharedPreferences.getBoolean(ConstantManager.SAVE_LOGIN, false)) {
            exclusionKeys.add(ConstantManager.SAVE_LOGIN);
            exclusionKeys.add(ConstantManager.SAVED_LOGIN);
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
        LoginManager.getInstance().logOut();    //fb logout
        VKAccessToken.removeTokenAtKey(mContext, ConstantManager.VK_ACCESS_TOKEN);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear().apply();
    }
    //endregion
}