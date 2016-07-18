package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class BaseModel<T> {
    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("data")
    @Expose
    private T data;

    public T getData() {
        return data;
    }
}
