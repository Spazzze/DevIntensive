package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class PublicInfo {

    @SerializedName("bio")
    @Expose
    private String bio;
    @SerializedName("avatar")
    @Expose
    private String avatar;
    @SerializedName("photo")
    @Expose
    private String photo;
    @SerializedName("updated")
    @Expose
    private String updated;

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getPhoto() {
        return photo;
    }
}
