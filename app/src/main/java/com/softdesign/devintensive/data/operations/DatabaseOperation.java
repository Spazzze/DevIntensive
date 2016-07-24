package com.softdesign.devintensive.data.operations;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.RepositoryEntity;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.models.UserEntityDao;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.UiHelper;

import org.greenrobot.greendao.Property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class DatabaseOperation extends BaseChronosOperation<List<UserEntity>> {
    private static final String TAG = Const.TAG_PREFIX + "DatabaseOperation";

    public enum Sort {
        RATING,
        CODE,
        CUSTOM,
    }

    final DaoSession mDaoSession = DATA_MANAGER.getDaoSession();
    Sort mSort = Sort.CUSTOM;
    Action mAction = Action.LOAD;

    List<UserListRes> mResponse;
    String mFirstUserRemoteId, mSecondUserRemoteId;

    public DatabaseOperation() {
        this.mAction = Action.LOAD;
    }

    public DatabaseOperation(Sort sort) {
        this.mAction = Action.LOAD;
        mSort = sort;
    }

    public DatabaseOperation(List<UserListRes> response) {
        this.mResponse = response;
        this.mAction = Action.SAVE;
    }

    public DatabaseOperation(Action action) {
        if (action == Action.SAVE) return; //only CLEAR and LOAD allowed this way
        this.mAction = action;
    }

    public DatabaseOperation(String firstUserRemoteId, String secondUserRemoteId) {
        mFirstUserRemoteId = firstUserRemoteId;
        mSecondUserRemoteId = secondUserRemoteId;
        this.mAction = Action.SWAP;
    }

    @Nullable
    @Override
    public List<UserEntity> run() {

        switch (this.mAction) {

            case CLEAR:
                clearDB();
                break;

            case SAVE:
                saveIntoDB();
                break;

            case SWAP:
                swapEntityInternalIds(mFirstUserRemoteId, mSecondUserRemoteId);
                break;

            case LOAD:
                return sortDB(mSort);
        }
        return null;
    }

    //region Main methods

    private void saveIntoDB() {
        List<RepositoryEntity> allRepositories = new ArrayList<>();
        List<UserEntity> allUsers = new ArrayList<>();
        List<UserEntity> curDB = sortDB(Sort.CUSTOM);

        if (curDB.size() != 0) {
            int maxIndex = findMaxInternalId(curDB);
            for (UserListRes user : mResponse) {
                int userIndexInDB = findUserInternalId(user.getId(), curDB);
                if (userIndexInDB == -1) {
                    userIndexInDB = maxIndex + 1;
                    maxIndex++;
                }
                List<RepositoryEntity> l = user.getRepositories().getRepoEntitiesList(user.getId());
                allRepositories.addAll(l);
                allUsers.add(new UserEntity(user, userIndexInDB));
            }
        } else {
            for (int i = 0; i < mResponse.size(); i++) {
                UserListRes user = mResponse.get(i);
                List<RepositoryEntity> l = user.getRepositories().getRepoEntitiesList(user.getId());
                allRepositories.addAll(l);
                allUsers.add(new UserEntity(user, i));
            }
        }

        mDaoSession.getRepositoryEntityDao().insertOrReplaceInTx(allRepositories);
        mDaoSession.getUserEntityDao().insertOrReplaceInTx(allUsers);

        SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
        editor.putLong(Const.DB_UPDATED_TIME_KEY, new Date().getTime());
        editor.apply();
    }

    private List<UserEntity> sortDB(Sort sorting) {
        List<UserEntity> userList = new ArrayList<>();
        Property sortProperty = UserEntityDao.Properties.Rating;

        switch (sorting) {
            case CODE:
                sortProperty = UserEntityDao.Properties.CodeLines;
                break;
            case CUSTOM:
                sortProperty = UserEntityDao.Properties.InternalId;
                break;
        }

        if (sorting == Sort.CUSTOM) {
            try {
                userList = mDaoSession
                        .queryBuilder(UserEntity.class)
                        .orderAsc(sortProperty)
                        .build()
                        .list();
            } catch (Exception e) {
                Log.e("DEV", "sortDB: " + e.getMessage());
            }
        } else {
            try {
                userList = mDaoSession
                        .queryBuilder(UserEntity.class)
                        .orderDesc(sortProperty)
                        .build()
                        .list();
            } catch (Exception e) {
                Log.e("DEV", "sortDB: " + e.getMessage());
            }
        }

        return userList;
    }

    private void clearDB() {
        mDaoSession.getRepositoryEntityDao().deleteAll();
        mDaoSession.getUserEntityDao().deleteAll();
        mDaoSession.clear();
    }

    private void swapEntityInternalIds(@NonNull String firstUserRemoteId, String secondUserRemoteId) {
        UserEntity firstEntity = findUserInDB(firstUserRemoteId);
        if (firstEntity == null) return;

        int oldInternalId = firstEntity.getInternalId();

        //swap place
        if (secondUserRemoteId != null) {
            UserEntity secondEntity = findUserInDB(secondUserRemoteId);
            if (secondEntity != null) {
                int newInternalId = secondEntity.getInternalId();
                firstEntity.setInternalId(newInternalId);
                secondEntity.setInternalId(oldInternalId);
                mDaoSession.getUserEntityDao().update(firstEntity);
                mDaoSession.getUserEntityDao().update(secondEntity);
            }
        } else { //move card to the lowest place
            List<UserEntity> curDB = sortDB(Sort.CUSTOM);
            if (UiHelper.isEmptyOrNull(curDB)) return;
            int newInternalId = findMaxInternalId(curDB) + 1;
            firstEntity.setInternalId(newInternalId);
            mDaoSession.getUserEntityDao().update(firstEntity);
        }
    }
    //endregion

    //region Functional methods
    private int findMaxInternalId(List<UserEntity> curDB) {
        int maxInternalId = 0;
        for (UserEntity u : curDB) {
            if (u.getInternalId() > maxInternalId) maxInternalId = u.getInternalId();
        }
        return maxInternalId;
    }

    private UserEntity findUserInDB(String userId) {
        return mDaoSession
                .queryBuilder(UserEntity.class)
                .where(UserEntityDao.Properties.RemoteId.eq(userId))
                .build()
                .unique();
    }

    private int findUserInternalId(String userId, List<UserEntity> DB) {
        for (UserEntity u : DB) {
            if (u.getRemoteId().equals(userId)) return u.getInternalId();
        }
        return -1;
    }
    //endregion

    //region Result
    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<List<UserEntity>>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<List<UserEntity>> {
    }
    //endregion
}
