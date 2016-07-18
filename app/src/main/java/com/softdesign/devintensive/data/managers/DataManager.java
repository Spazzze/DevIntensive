package com.softdesign.devintensive.data.managers;

import com.softdesign.devintensive.data.network.RestService;
import com.softdesign.devintensive.data.network.ServiceGenerator;
import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserAuthRes;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.network.api.res.UserPhotoRes;
import com.softdesign.devintensive.data.network.restmodels.BaseListModel;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.models.UserEntityDao;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Used to manage all data
 */
public class DataManager {
    private static final DataManager INSTANCE = new DataManager();
    private final PreferencesManager mPreferencesManager;
    private final RestService mRestService;
    private final DaoSession mDaoSession;

    public static DataManager getInstance() {
        return INSTANCE;
    }

    private DataManager() {
        this.mPreferencesManager = new PreferencesManager();
        this.mRestService = ServiceGenerator.createService(RestService.class);
        this.mDaoSession = DevIntensiveApplication.getDaoSession();
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }

    //region ========== Network ===========
    public Call<BaseModel<UserAuthRes>> loginUser(@Body UserLoginReq req) {
        return mRestService.loginUser(req);
    }

    public Call<BaseModel<User>> getUserData(@Path("userId") String userId) {
        return mRestService.getUserData(userId);
    }

    public Call<BaseModel<UserPhotoRes>> uploadUserPhoto(@Path("userId") String userId,
                                                         @Part MultipartBody.Part file) {
        return mRestService.uploadUserPhoto(userId, file);
    }

    public Call<BaseModel<UserPhotoRes>> uploadUserAvatar(@Path("userId") String userId,
                                                          @Part MultipartBody.Part file) {
        return mRestService.uploadUserAvatar(userId, file);
    }

    public Call<BaseListModel<UserListRes>> getUserListFromNetwork() {
        return mRestService.getUserList();
    }
    //endregion

    //region ========== DataBase ==========
    public List<UserEntity> getUserListFromDb() {
        List<UserEntity> userList = new ArrayList<>();

        try {
            userList = mDaoSession.queryBuilder(UserEntity.class)
                    .where(UserEntityDao.Properties.Rating.gt(0))
                    .orderDesc(UserEntityDao.Properties.Rating)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userList;
    }

    //endregion
}