package com.softdesign.devintensive.data.network.restmodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Contacts implements Parcelable {

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

    //region Getters & Setters
    public String getVk() {
        return vk;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getUpdated() {
        return updated;
    }

    public void setVk(String vk) {
        this.vk = vk;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
    //endregion

    //region Parcel
    protected Contacts(Parcel in) {
        vk = in.readString();
        phone = in.readString();
        email = in.readString();
        updated = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vk);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeString(updated);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Contacts> CREATOR = new Parcelable.Creator<Contacts>() {
        @Override
        public Contacts createFromParcel(Parcel in) {
            return new Contacts(in);
        }

        @Override
        public Contacts[] newArray(int size) {
            return new Contacts[size];
        }
    };
    //endregion
}