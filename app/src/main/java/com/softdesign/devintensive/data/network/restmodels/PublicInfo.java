package com.softdesign.devintensive.data.network.restmodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class PublicInfo implements Parcelable {

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

    //region Getters & Setters
    public String getBio() {
        return bio;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getPhoto() {
        return photo;
    }

    public String getUpdated() {
        return updated;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
    //endregion

    //region Parcel
    protected PublicInfo(Parcel in) {
        bio = in.readString();
        avatar = in.readString();
        photo = in.readString();
        updated = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bio);
        dest.writeString(avatar);
        dest.writeString(photo);
        dest.writeString(updated);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PublicInfo> CREATOR = new Parcelable.Creator<PublicInfo>() {
        @Override
        public PublicInfo createFromParcel(Parcel in) {
            return new PublicInfo(in);
        }

        @Override
        public PublicInfo[] newArray(int size) {
            return new PublicInfo[size];
        }
    };
    //endregion
}
