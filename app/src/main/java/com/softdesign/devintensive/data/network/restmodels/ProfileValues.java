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

    public int getLinesCode() {
        return linesCode;
    }

    public int getRating() {
        return rating;
    }

    public int getProjects() {
        return projects;
    }
}
