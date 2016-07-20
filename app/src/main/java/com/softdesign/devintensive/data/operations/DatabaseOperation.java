package com.softdesign.devintensive.data.operations;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.storage.models.RepositoryEntity;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.models.UserEntityDao;
import com.softdesign.devintensive.utils.Const;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseOperation extends BaseChronosOperation<List<UserEntity>> {

    List<UserListRes> mResponse;

    public DatabaseOperation() {
        this.mAction = Action.LOAD;
    }

    public DatabaseOperation(List<UserListRes> response) {
        this.mResponse = response;
        this.mAction = Action.SAVE;
    }

    @Nullable
    @Override
    public List<UserEntity> run() {
        switch (this.mAction) {
            case SAVE:
                List<RepositoryEntity> allRepositories = new ArrayList<>();
                List<UserEntity> allUsers = new ArrayList<>();

                for (UserListRes user : mResponse) {
                    List<RepositoryEntity> l = user.getRepositories()
                            .getRepoEntitiesList(String.valueOf(user.getId()));
                    allRepositories.addAll(l);
                    allUsers.add(new UserEntity(user));
                }

                DATA_MANAGER.getDaoSession().getRepositoryEntityDao().insertOrReplaceInTx(allRepositories);
                DATA_MANAGER.getDaoSession().getUserEntityDao().insertOrReplaceInTx(allUsers);
                SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
                editor.putLong(Const.DB_UPDATED_TIME_KEY, new Date().getTime());
                editor.apply();
                return null;
            case LOAD:
                List<UserEntity> userList = new ArrayList<>();

                try {
                    userList = DATA_MANAGER.getDaoSession()
                            .queryBuilder(UserEntity.class)
                            .orderDesc(UserEntityDao.Properties.Rating)
                            .build()
                            .list();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return userList;
        }
        return null;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<List<UserEntity>>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<UserEntity>> {
    }
}
