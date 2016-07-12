package com.softdesign.devintensive.data.network;

import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserModelRes;
import com.softdesign.devintensive.data.network.api.res.UserPhotoRes;
import com.softdesign.devintensive.data.network.api.res.UserUpdRes;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface RestService {
    @POST("login")
    Call<UserModelRes> loginUser(@Body UserLoginReq req);

    @GET("user/{userId}")
    Call<UserUpdRes> getUserData(@Path("userId") String userId);

    @Multipart
    @POST("user/{userId}/publicValues/profilePhoto")
    Call<UserPhotoRes> uploadUserPhoto(@Path("userId") String userId,
                                       @Part MultipartBody.Part file);
    @Multipart
    @POST("user/{userId}/publicValues/profileAvatar")
    Call<UserPhotoRes> uploadUserAvatar(@Path("userId") String userId,
                                       @Part MultipartBody.Part file);
}
