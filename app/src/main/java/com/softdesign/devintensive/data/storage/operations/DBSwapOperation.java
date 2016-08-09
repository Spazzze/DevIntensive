package com.softdesign.devintensive.data.storage.operations;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.utils.AppUtils;

import static com.softdesign.devintensive.data.storage.operations.DatabaseOperation.findUserInDB;

public class DBSwapOperation extends BaseChronosOperation<Void> {

    final static DaoSession DAO_SESSION = DATA_MANAGER.getDaoSession();

    String mFirstUserRemoteId, mSecondUserRemoteId;

    public DBSwapOperation(String firstUserRemoteId, String secondUserRemoteId) {
        mFirstUserRemoteId = firstUserRemoteId;
        mSecondUserRemoteId = secondUserRemoteId;
    }

    @Nullable
    @Override
    public Void run() {
        swapEntityInternalIds(mFirstUserRemoteId, mSecondUserRemoteId);
        return null;
    }

    private void swapEntityInternalIds(String firstUserRemoteId, String secondUserRemoteId) {
        if (AppUtils.isEmptyOrNull(firstUserRemoteId)) return;
        UserEntity firstEntity = findUserInDB(firstUserRemoteId);
        if (firstEntity == null) return;
        //swap place
        if (secondUserRemoteId != null) {
            int oldInternalId = firstEntity.getInternalId();
            UserEntity secondEntity = findUserInDB(secondUserRemoteId);
            if (secondEntity != null) {
                int newInternalId = secondEntity.getInternalId();
                firstEntity.setInternalId(newInternalId);
                secondEntity.setInternalId(oldInternalId);
                DAO_SESSION.getUserEntityDao().update(firstEntity);
                DAO_SESSION.getUserEntityDao().update(secondEntity);
            }
        } else {
            firstEntity.setInternalId(0);
            DAO_SESSION.getUserEntityDao().update(firstEntity);
        }
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Result
    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<Void>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<Void> {
    }
}
