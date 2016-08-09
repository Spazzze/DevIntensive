package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AddRepo {

    @SerializedName("git")
    @Expose
    private String git;
    @SerializedName("title")
    @Expose
    private String title;

    public AddRepo(String git) {
        this.git = git;
        this.title = "";
    }

    public String getGit() {
        return git;
    }

    public void setGit(String git) {
        this.git = git;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
