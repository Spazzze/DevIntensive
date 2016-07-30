package com.softdesign.devintensive.data.network.api.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.softdesign.devintensive.data.network.restmodels.ProfileValues;
import com.softdesign.devintensive.data.network.restmodels.PublicInfo;
import com.softdesign.devintensive.data.network.restmodels.Repositories;

@SuppressWarnings("unused")
public class UserListRes extends BaseResponse{

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

    //region :::::::::::::::::::::::::::::::::::::::::: Getters
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

    public String getFullName() {
        return String.format("%s %s", firstName, secondName);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public void setV(Integer v) {
        this.v = v;
    }

    public void setRepositories(Repositories repositories) {
        this.repositories = repositories;
    }

    public void setProfileValues(ProfileValues profileValues) {
        this.profileValues = profileValues;
    }

    public void setPublicInfo(PublicInfo publicInfo) {
        this.publicInfo = publicInfo;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
