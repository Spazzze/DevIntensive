package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {
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
    private int v;
    @SerializedName("repositories")
    @Expose
    private Repositories repositories;
    @SerializedName("contacts")
    @Expose
    private Contacts contacts;
    @SerializedName("profileValues")
    @Expose
    private ProfileValues profileValues;
    @SerializedName("publicInfo")
    @Expose
    private PublicInfo PublicInfo;
    @SerializedName("specialization")
    @Expose
    private String specialization;
    @SerializedName("role")
    @Expose
    private String role;
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

    public Contacts getContacts() {
        return contacts;
    }

    public PublicInfo getPublicInfo() {
        return PublicInfo;
    }
}
