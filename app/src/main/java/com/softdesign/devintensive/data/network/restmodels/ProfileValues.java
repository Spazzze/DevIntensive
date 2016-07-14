package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class ProfileValues {

    @SerializedName("homeTask")
    @Expose
    private int homeTask;
    @SerializedName("projects")
    @Expose
    private int projects;
    @SerializedName("linesCode")
    @Expose
    private int linesCode;
    @SerializedName("rait")
    @Expose
    private int rating;
    @SerializedName("updated")
    @Expose
    private String updated;

    public String getLinesCode() {
        return String.valueOf(linesCode);
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

    public void setUpdated(String updated) {
        this.updated = updated;
    }
}
