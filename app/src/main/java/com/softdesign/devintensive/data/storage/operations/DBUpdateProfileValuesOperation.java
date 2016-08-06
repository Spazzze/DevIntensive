package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.network.restmodels.ProfileValues;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.UserEntity;

import static com.softdesign.devintensive.data.storage.operations.DatabaseOperation.findUserInDB;

public class DBUpdateProfileValuesOperation extends BaseChronosOperation<UserEntity> {

    final DaoSession mDaoSession = DATA_MANAGER.getDaoSession();

    ProfileValues mLikeResponse;
    String mLikedUserId;

    public DBUpdateProfileValuesOperation(String userId, ProfileValues data) {
        this.mLikedUserId = userId;
        this.mLikeResponse = data;
    }

    @Nullable
    @Override
    public UserEntity run() {
        return updateUserInDB(mLikedUserId, mLikeResponse);
    }

    private UserEntity updateUserInDB(String likedUserId, ProfileValues values) {
        UserEntity likedEntity = findUserInDB(likedUserId);
        if (likedEntity != null) {
            likedEntity.setRating(values.getIntRating());
            likedEntity.setLikesBy(values.getLikeEntitiesList(likedUserId));
            mDaoSession.getUserEntityDao().update(likedEntity);
        }
        return likedEntity;
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Result
    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<UserEntity>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<UserEntity> {
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
