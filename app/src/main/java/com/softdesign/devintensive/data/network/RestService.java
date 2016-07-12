package com.softdesign.devintensive.data.network;

import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserModelRes;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RestService {
    //// TODO: 10.07.2016 secure auth?
    @POST("login")
    Call<UserModelRes> loginUser (@Body UserLoginReq req);
}
