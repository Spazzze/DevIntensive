package com.softdesign.devintensive.data.network.api.res;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SoftDesignApiErrorRes extends BaseResponse {

    @SerializedName("err")
    @Expose
    private String err;

    public SoftDesignApiErrorRes() {
    }

    public SoftDesignApiErrorRes(String error) {
        err = error;
    }

    public String getError() {
        return err;
    }

    public void setError(String error) {
        err = error;
    }
}
