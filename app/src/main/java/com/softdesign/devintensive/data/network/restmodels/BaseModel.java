package com.softdesign.devintensive.data.network.restmodels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.softdesign.devintensive.data.network.api.res.BaseResponse;

@SuppressWarnings("unused")
public class BaseModel<T extends BaseResponse> {
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
