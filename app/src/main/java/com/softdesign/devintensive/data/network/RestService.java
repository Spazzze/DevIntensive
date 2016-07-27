package com.softdesign.devintensive.data.network;

import android.support.annotation.NonNull;

import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.EditProfileRes;
import com.softdesign.devintensive.data.network.api.res.UserAuthRes;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.network.api.res.UserPhotoRes;
import com.softdesign.devintensive.data.network.restmodels.BaseListModel;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;

public interface RestService {
    @POST("login")
    Call<BaseModel<UserAuthRes>> loginUser(@Body UserLoginReq req);

    @GET("user/{userId}")
    Call<BaseModel<User>> getUserData(@Path("userId") String userId);

    @Multipart
    @POST("user/{userId}/publicValues/profilePhoto")
    Call<BaseModel<UserPhotoRes>> uploadUserPhoto(@Path("userId") String userId,
                                                  @Part MultipartBody.Part file);

    @Multipart
    @POST("user/{userId}/publicValues/profileAvatar")
    Call<BaseModel<UserPhotoRes>> uploadUserAvatar(@Path("userId") String userId,
                                                   @Part MultipartBody.Part file);

    @GET("user/list?orderBy=rating")
    Call<BaseListModel<UserListRes>> getUserList();

    @Multipart
    @POST("profile/edit")
    Call<BaseModel<EditProfileRes>> uploadUserInfo(@NonNull @PartMap Map<String, RequestBody> map);
}
