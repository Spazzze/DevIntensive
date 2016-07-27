package com.softdesign.devintensive.data.network.restmodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Repo implements Parcelable {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("git")
    @Expose
    private String git;
    @SerializedName("title")
    @Expose
    private String title;

    public Repo(String git) {
        this.id = "";
        this.git = git;
        this.title = "";
    }

    //region Getters & Setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getGit() {
        return git;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setGit(String git) {
        this.git = git;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    //endregion

    //region Parcel
    protected Repo(Parcel in) {
        id = in.readString();
        git = in.readString();
        title = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(git);
        dest.writeString(title);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Repo> CREATOR = new Parcelable.Creator<Repo>() {
        @Override
        public Repo createFromParcel(Parcel in) {
            return new Repo(in);
        }

        @Override
        public Repo[] newArray(int size) {
            return new Repo[size];
        }
    };
    //endregion
}