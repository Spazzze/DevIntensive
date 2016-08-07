package com.softdesign.devintensive.data.network.api.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class UserPhotoRes extends BaseResponse {

    @SerializedName("photo")
    @Expose
    private String photo;

    @SerializedName("updated")
    @Expose
    private String updated;

    public UserPhotoRes() {
    }

    public UserPhotoRes(String photo, String updated) {
        this.photo = photo;
        this.updated = updated;
    }

    public String getPhoto() {
        return photo;
    }

    public String getUpdated() {
        return updated;
    }
}
