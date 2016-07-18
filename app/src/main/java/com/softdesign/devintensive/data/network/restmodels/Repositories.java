package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.softdesign.devintensive.data.storage.models.RepositoryEntity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Repositories {

    @SerializedName("repo")
    @Expose
    private final List<Repo> repo = new ArrayList<>();
    @SerializedName("updated")
    @Expose
    private String updated;

    public List<Repo> getRepo() {
        return repo;
    }

    public List<RepositoryEntity> getRepoEntitiesList(final String userId) {
        return new ArrayList<RepositoryEntity>() {{
            for (Repo r : repo) {
                add(new RepositoryEntity(r, userId));
            }
        }};
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
}