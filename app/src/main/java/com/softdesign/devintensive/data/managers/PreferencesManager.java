package com.softdesign.devintensive.data.managers;

import android.content.SharedPreferences;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.util.ArrayList;
import java.util.List;

public class PreferencesManager {

    private SharedPreferences mSharedPreferences;
    public static final String[] USER_FIELDS = {ConstantManager.USER_PHONE_KEY, ConstantManager.USER_EMAIL_KEY, ConstantManager.USER_VK_KEY, ConstantManager.USER_GITHUB_KEY, ConstantManager.USER_ABOUT_KEY};

    public PreferencesManager() {
        mSharedPreferences = DevIntensiveApplication.getSharedPreferences();
    }

    public void saveUserProfileData (List<String> userFields){
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        for (int i = 0; i <USER_FIELDS.length; i++){
            editor.putString(USER_FIELDS[i], userFields.get(i));
        }
        editor.apply();
    }

    public List<String> loadUserProfileData (){
        List<String> userFields = new ArrayList<>();
        List<String> defaultUserData = new ArrayList<>();
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.profile_phone_dummy));
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.profile_email_dummy));
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.profile_vk_dummy));
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.profile_gitHubRepo_dummy));
        defaultUserData.add(DevIntensiveApplication.getContext().getResources().getString(R.string.profile_about_dummy));
        for (int i = 0; i < USER_FIELDS.length ; i++) {
            userFields.add(mSharedPreferences.getString(USER_FIELDS[i], defaultUserData.get(i)));
        }
        return userFields;
    }
}
