package com.softdesign.devintensive.data.network.restmodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ProfileValues implements Parcelable {

    @SerializedName("homeTask")
    @Expose
    public int homeTask;
    @SerializedName("projects")
    @Expose
    public int projects;
    @SerializedName("linesCode")
    @Expose
    public int codeLines;
    @SerializedName("likesBy")
    @Expose
    public List<String> likesBy = new ArrayList<>();
    @SerializedName("rait")
    @Expose
    public int ratingByAdmin;
    @SerializedName("rating")
    @Expose
    public int rating;
    @SerializedName("updated")
    @Expose
    public String updated;

    //region Getters
    public List<String> getLikesBy() {
        return likesBy;
    }

    public String getRatingByAdmin() {
        return String.valueOf(ratingByAdmin);
    }

    public String getCodeLines() {
        return String.valueOf(codeLines);
    }

    public String getRating() {
        return String.valueOf(rating);
    }

    public String getProjects() {
        return String.valueOf(projects);
    }

    public String getHomeTask() {
        return String.valueOf(homeTask);
    }

    public String getUpdated() {
        return updated;
    }
    //endregion

    //region Setters
    public void setHomeTask(int homeTask) {
        this.homeTask = homeTask;
    }

    public void setProjects(int projects) {
        this.projects = projects;
    }

    public void setCodeLines(int codeLines) {
        this.codeLines = codeLines;
    }

    public void setLikesBy(List<String> likesBy) {
        this.likesBy = likesBy;
    }

    public void setRatingByAdmin(int ratingByAdmin) {
        this.ratingByAdmin = ratingByAdmin;
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
        if (in.readByte() == 0x01) {
            likesBy = new ArrayList<>();
            in.readList(likesBy, String.class.getClassLoader());
        } else {
            likesBy.clear();
        }
        ratingByAdmin = in.readInt();
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
        if (likesBy == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(likesBy);
        }
        dest.writeInt(ratingByAdmin);
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