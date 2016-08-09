package com.softdesign.devintensive.data.network.restmodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.softdesign.devintensive.data.network.api.res.BaseResponse;

@SuppressWarnings("unused")
public class User extends BaseResponse implements Parcelable {
    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("second_name")
    @Expose
    private String secondName;
    @SerializedName("__v")
    @Expose
    private int v;
    @SerializedName("repositories")
    @Expose
    private Repositories repositories;
    @SerializedName("contacts")
    @Expose
    private Contacts contacts;
    @SerializedName("profileValues")
    @Expose
    private ProfileValues profileValues;
    @SerializedName("publicInfo")
    @Expose
    private PublicInfo PublicInfo;
    @SerializedName("specialization")
    @Expose
    private String specialization;
    @SerializedName("role")
    @Expose
    private String role;
    @SerializedName("updated")
    @Expose
    private String updated;

    //region Getters
    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public String getUpdated() {
        return updated;
    }

    public Repositories getRepositories() {
        return repositories;
    }

    public ProfileValues getProfileValues() {
        return profileValues;
    }

    public Contacts getContacts() {
        return contacts;
    }

    public PublicInfo getPublicInfo() {
        return PublicInfo;
    }
    //endregion

    //region Setters
    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public void setRepositories(Repositories repositories) {
        this.repositories = repositories;
    }

    public void setContacts(Contacts contacts) {
        this.contacts = contacts;
    }

    public void setProfileValues(ProfileValues profileValues) {
        this.profileValues = profileValues;
    }

    public void setPublicInfo(PublicInfo publicInfo) {
        PublicInfo = publicInfo;
    }
    //endregion

    //region Parcel
    protected User(Parcel in) {
        id = in.readString();
        firstName = in.readString();
        secondName = in.readString();
        v = in.readInt();
        repositories = (Repositories) in.readValue(Repositories.class.getClassLoader());
        contacts = (Contacts) in.readValue(Contacts.class.getClassLoader());
        profileValues = (ProfileValues) in.readValue(ProfileValues.class.getClassLoader());
        PublicInfo = (PublicInfo) in.readValue(PublicInfo.class.getClassLoader());
        specialization = in.readString();
        role = in.readString();
        updated = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(firstName);
        dest.writeString(secondName);
        dest.writeInt(v);
        dest.writeValue(repositories);
        dest.writeValue(contacts);
        dest.writeValue(profileValues);
        dest.writeValue(PublicInfo);
        dest.writeString(specialization);
        dest.writeString(role);
        dest.writeString(updated);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    //endregion
}
