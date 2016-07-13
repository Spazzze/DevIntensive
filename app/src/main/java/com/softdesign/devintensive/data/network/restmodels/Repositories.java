package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
        import com.google.gson.annotations.SerializedName;

        import java.util.ArrayList;
        import java.util.List;

@SuppressWarnings("unused")
public class Repositories {

    @SerializedName("repo")
    @Expose
    private List<Repo> repo = new ArrayList<>();
    @SerializedName("updated")
    @Expose
    private String updated;

    public List<Repo> getRepo() {
        return repo;
    }
}