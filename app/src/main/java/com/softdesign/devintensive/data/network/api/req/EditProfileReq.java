package com.softdesign.devintensive.data.network.api.req;

import android.support.annotation.NonNull;
import android.util.Log;

import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.UiHelper;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class EditProfileReq {
    private static final String TAG = ConstantManager.TAG_PREFIX + "EditProfileReq";
    @NonNull
    private Map<String, String> mParamsMap = new HashMap<>();

    public EditProfileReq(User user) {
        setFirstName(user.getFirstName());
        setLastName(user.getSecondName());
        setPhoneNumber(user.getContacts().getPhone());
        setVk(user.getContacts().getVk());
        setBio(user.getPublicInfo().getBio());
        setGithub(UiHelper.repoListIntoJson(user.getRepositories().getRepo()));
    }

    //region Setters
    public void setPhoneNumber(String phoneNumber) {
        mParamsMap.put("phone", phoneNumber);
    }

    public void setVk(String vk) {
        mParamsMap.put("vk", vk);
    }

    public void setFirstName(String firstName) {
        mParamsMap.put("name", firstName);
    }

    public void setLastName(String lastName) {
        mParamsMap.put("surname", lastName);
    }

    public void setGithub(String github) {
        mParamsMap.put("github", github);
    }

    public void setBio(String bio) {
        mParamsMap.put("bio", bio);
    }
    //endregion

    public HashMap<String, RequestBody> createReqBody() {
        HashMap<String, RequestBody> requestBodyMap = new HashMap<>();
        for (Map.Entry<String, String> pair : mParamsMap.entrySet()) {
            String value = pair.getValue();
            if (value == null) continue;
            Log.d(TAG, "createReqBody: " + value);
            requestBodyMap.put(pair.getKey(),
                    RequestBody.create(MediaType.parse("multipart/form-data"), value));
        }
        return requestBodyMap;
    }
}
