package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class BaseListModel<T> {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("data")
    @Expose
    public List<T> data = new ArrayList<>();

    public Boolean getSuccess() {
        return success;
    }

    public List<T> getData() {
        return data;
    }
}
