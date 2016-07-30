package com.softdesign.devintensive.data.network.api.req;

import android.support.annotation.NonNull;

import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;

public class EditProfileReq {
    @NonNull
    private final Map<String, String> mParamsMap = new HashMap<>();

    public EditProfileReq(User user) {
        setFirstName(user.getFirstName());
        setLastName(user.getSecondName());
        setPhoneNumber(user.getContacts().getPhone());
        setVk(user.getContacts().getVk());
        setBio(user.getPublicInfo().getBio());
        setGithub(AppUtils.repoListIntoJson(user.getRepositories().getRepo()));
    }

    public EditProfileReq(ProfileViewModel model) {

        String[] name = model.getFullName().split("\\s");
        setFirstName(name[0]);
        setLastName(name[1]);
        setPhoneNumber(model.getPhone());
        setVk(model.getVK());
        setBio(model.getBio());
        setGithub(AppUtils.repoListIntoJson(model.repoListFromModel()));
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Setters
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
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    public HashMap<String, RequestBody> createReqBody() {
        HashMap<String, RequestBody> requestBodyMap = new HashMap<>();
        for (Map.Entry<String, String> pair : mParamsMap.entrySet()) {
            String value = pair.getValue();
            if (value == null) continue;
            requestBodyMap.put(pair.getKey(),
                    RequestBody.create(Const.MEDIATYPE_MULTIPART_FORM_DATA, value));
        }
        return requestBodyMap;
    }
}
