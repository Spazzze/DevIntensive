package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.softdesign.devintensive.data.network.api.res.BaseResponse;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class BaseListModel<T extends BaseResponse> {

    @SerializedName("success")
    @Expose
    public Boolean success;
    @SerializedName("data")
    @Expose
    public final List<T> data = new ArrayList<>();

    public Boolean getSuccess() {
        return success;
    }

    public List<T> getData() {
        return data;
    }
}
