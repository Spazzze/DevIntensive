package com.softdesign.devintensive.data.network.restmodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class ProfileValues implements Parcelable {

    @SerializedName("homeTask")
    @Expose
    private int homeTask;
    @SerializedName("projects")
    @Expose
    private int projects;
    @SerializedName("linesCode")
    @Expose
    private int codeLines;
    @SerializedName("rait")
    @Expose
    private int rating;
    @SerializedName("updated")
    @Expose
    private String updated;

    //region Getters & Setters
    public String getCodeLines() {
        return String.valueOf(codeLines);
    }

    public String getRating() {
        return String.valueOf(rating);
    }

    public String getProjects() {
        return String.valueOf(projects);
    }

    public String getUpdated() {
        return updated;
    }

    public String getHomeTask() {
        return String.valueOf(homeTask);
    }

    public void setHomeTask(int homeTask) {
        this.homeTask = homeTask;
    }

    public void setProjects(int projects) {
        this.projects = projects;
    }

    public void setLinesCode(int linesCode) {
        this.codeLines = linesCode;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
    //endregion

    //region Parcel
    protected ProfileValues(Parcel in) {
        homeTask = in.readInt();
        projects = in.readInt();
        codeLines = in.readInt();
        rating = in.readInt();
        updated = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(homeTask);
        dest.writeInt(projects);
        dest.writeInt(codeLines);
        dest.writeInt(rating);
        dest.writeString(updated);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ProfileValues> CREATOR = new Parcelable.Creator<ProfileValues>() {
        @Override
        public ProfileValues createFromParcel(Parcel in) {
            return new ProfileValues(in);
        }

        @Override
        public ProfileValues[] newArray(int size) {
            return new ProfileValues[size];
        }
    };
    //endregion
}