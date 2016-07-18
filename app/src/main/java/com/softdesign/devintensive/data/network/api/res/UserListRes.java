package com.softdesign.devintensive.data.network.api.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.softdesign.devintensive.data.network.restmodels.ProfileValues;
import com.softdesign.devintensive.data.network.restmodels.PublicInfo;
import com.softdesign.devintensive.data.network.restmodels.Repositories;

public class UserListRes {


    @SerializedName("_id")
    @Expose
    private String id;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("second_name")
    @Expose
    private String secondName;
    @SerializedName("__v")
    @Expose
    private Integer v;
    @SerializedName("repositories")
    @Expose
    private Repositories repositories;
    @SerializedName("profileValues")
    @Expose
    private ProfileValues profileValues;
    @SerializedName("publicInfo")
    @Expose
    private PublicInfo publicInfo;
    @SerializedName("specialization")
    @Expose
    public String specialization;
    @SerializedName("updated")
    @Expose
    private String updated;

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public Repositories getRepositories() {
        return repositories;
    }

    public ProfileValues getProfileValues() {
        return profileValues;
    }

    public PublicInfo getPublicInfo() {
        return publicInfo;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getUpdated() {
        return updated;
    }

    public Integer getV() {
        return v;
    }

    public String getFullName(){
        return String.format("%s %s", firstName, secondName);
    }
}
