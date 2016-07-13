package com.softdesign.devintensive.data.managers;

import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserModelRes;
import com.softdesign.devintensive.data.network.api.res.UserPhotoRes;
import com.softdesign.devintensive.data.network.api.res.UserUpdRes;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Used to manage all data
 */
public class DataManager{
    private static final DataManager INSTANCE = new DataManager();
    private final PreferencesManager mPreferencesManager;
    private final RestService mRestService;

    public static DataManager getInstance() {
        return INSTANCE;
    }

    private DataManager() {
        this.mPreferencesManager = new PreferencesManager();
        this.mRestService = ServiceGenerator.createService(RestService.class);
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }

    //region Network
    public Call<UserModelRes> loginUser(@Body UserLoginReq req) {
        return mRestService.loginUser(req);
    }

    public Call<UserUpdRes> getUserData(@Path("userId") String userId) {
        return mRestService.getUserData(userId);
    }

    public Call<UserPhotoRes> uploadUserPhoto(@Path("userId") String userId,
                                              @Part MultipartBody.Part file) {
        return mRestService.uploadUserPhoto(userId, file);
    }

    public Call<UserPhotoRes> uploadUserAvatar(@Path("userId") String userId,
                                              @Part MultipartBody.Part file) {
        return mRestService.uploadUserAvatar(userId, file);
    }
    //endregion
}