package com.softdesign.devintensive.data.network.api.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserAvatarRes extends BaseResponse {

    @SerializedName("photo")
    @Expose
    private String avatar;

    @SerializedName("updated")
    @Expose
    private String updated;

    public UserAvatarRes() {
    }

    public UserAvatarRes(String avatar, String updated) {
        this.avatar = avatar;
        this.updated = updated;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getUpdated() {
        return updated;
    }
}

