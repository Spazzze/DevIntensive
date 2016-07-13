package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Contacts {

    @SerializedName("vk")
    @Expose
    private String vk;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("updated")
    @Expose
    private String updated;

    public void setVk(String vk) {
        this.vk = vk;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getVk() {
        return vk;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}