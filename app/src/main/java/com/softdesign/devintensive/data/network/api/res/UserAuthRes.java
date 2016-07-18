package com.softdesign.devintensive.data.network.api.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.softdesign.devintensive.data.network.restmodels.User;

public class UserAuthRes {

    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("token")
    @Expose
    private String token;

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }
}
