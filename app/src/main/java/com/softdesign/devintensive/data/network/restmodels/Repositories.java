package com.softdesign.devintensive.data.network.restmodels;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.softdesign.devintensive.data.storage.models.RepositoryEntity;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class Repositories implements Parcelable {

    @SerializedName("repo")
    @Expose
    private List<Repo> repo = new ArrayList<>();
    @SerializedName("updated")
    @Expose
    private String updated;

    //region Getters & Setters
    public List<Repo> getRepo() {
        return repo;
    }

    public String getUpdated() {
        return updated;
    }

    public void setRepo(List<Repo> repo) {
        this.repo = repo;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
    //endregion

    public List<RepositoryEntity> getRepoEntitiesList(final String userId) {
        return new ArrayList<RepositoryEntity>() {{
            for (Repo r : repo) {
                add(new RepositoryEntity(r, userId));
            }
        }};
    }

    //region Parcel
    protected Repositories(Parcel in) {
        if (in.readByte() == 0x01) {
            repo = new ArrayList<Repo>();
            in.readList(repo, Repo.class.getClassLoader());
        } else {
            repo = null;
        }
        updated = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (repo == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(repo);
        }
        dest.writeString(updated);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Repositories> CREATOR = new Parcelable.Creator<Repositories>() {
        @Override
        public Repositories createFromParcel(Parcel in) {
            return new Repositories(in);
        }

        @Override
        public Repositories[] newArray(int size) {
            return new Repositories[size];
        }
    };
    //endregion
}