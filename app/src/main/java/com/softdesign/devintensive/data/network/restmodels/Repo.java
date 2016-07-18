package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class Repo {

    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("git")
    @Expose
    private String git;
    @SerializedName("title")
    @Expose
    private String title;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setGit(String git) {
        this.git = git;
    }

    public String getGit() {
        return git;
    }
}