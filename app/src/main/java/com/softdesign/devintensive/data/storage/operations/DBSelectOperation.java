package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.models.UserEntityDao;

import java.util.ArrayList;
import java.util.List;

import static com.softdesign.devintensive.data.storage.operations.DatabaseOperation.findUserInDB;

public class DBSelectOperation extends BaseChronosOperation<List<UserEntity>> {

    final DaoSession mDaoSession = DATA_MANAGER.getDaoSession();

    List<String> mSelectionUserIds;

    public DBSelectOperation(List<String> userIds) {
        this.mSelectionUserIds = userIds;
    }

    public DBSelectOperation(String id) {
        mSelectionUserIds = findUserInDB(id).getLikesList();
    }

    @Nullable
    @Override
    public List<UserEntity> run() {
        return selectUsersFromDB(mSelectionUserIds);
    }

    private List<UserEntity> selectUsersFromDB(final List<String> selectionUserIds) {
        List<UserEntity> userEntities = new ArrayList<>();
        try {
            userEntities = mDaoSession
                    .queryBuilder(UserEntity.class)
                    .where(UserEntityDao.Properties.RemoteId.in(selectionUserIds))
                    .list();
        } catch (Exception ignored) {
        }

        return userEntities;
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Result
    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<List<UserEntity>>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<UserEntity>> {
    }
//endregion ::::::::::::::::::::::::::::::::::::::::::
}
