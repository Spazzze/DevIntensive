package com.softdesign.devintensive.data.operations;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.RepositoryEntity;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.models.UserEntityDao;
import com.softdesign.devintensive.utils.Const;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DatabaseOperation extends BaseChronosOperation<List<UserEntity>> {

    public enum Sort {
        RATING,
        CODE,
        CUSTOM,
    }

    List<UserListRes> mResponse;
    DaoSession mDaoSession = DATA_MANAGER.getDaoSession();
    Sort mSort;

    public DatabaseOperation() {
        this.mAction = Action.LOAD;
        mSort = Sort.CUSTOM;
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
        this.mAction = action;
    }

    @Nullable
    @Override
    public List<UserEntity> run() {

        switch (this.mAction) {

            case CLEAR:

                clearDB();

                return null;

            case SAVE:

                saveIntoDB();

                return null;

            case LOAD:

                List<UserEntity> userList = null;

                switch (mSort) {
                    case RATING:
                        userList = sortDBbyRating();
                        break;
                    case CODE:
                        userList = sortDBbyCode();
                        break;
                    case CUSTOM:
                        userList = sortDBbyCustom();
                        break;
                }

                return userList;
        }
        return null;
    }

    private List<UserEntity> sortDBbyCustom() {
        List<UserEntity> userList = new ArrayList<>();

        try {
            userList = DATA_MANAGER.getDaoSession()
                    .queryBuilder(UserEntity.class)
                    .orderAsc(UserEntityDao.Properties.InternalId)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    private List<UserEntity> sortDBbyCode() {
        List<UserEntity> userList = new ArrayList<>();

        try {
            userList = DATA_MANAGER.getDaoSession()
                    .queryBuilder(UserEntity.class)
                    .orderDesc(UserEntityDao.Properties.CodeLines, UserEntityDao.Properties.InternalId)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    private List<UserEntity> sortDBbyRating() {
        List<UserEntity> userList = new ArrayList<>();

        try {
            userList = DATA_MANAGER.getDaoSession()
                    .queryBuilder(UserEntity.class)
                    .orderDesc(UserEntityDao.Properties.Rating, UserEntityDao.Properties.InternalId)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userList;
    }

    private void clearDB() {
        mDaoSession.getRepositoryEntityDao().deleteAll();
        mDaoSession.getUserEntityDao().deleteAll();
        mDaoSession.clear();
    }

    private void saveIntoDB() {
        List<RepositoryEntity> allRepositories = new ArrayList<>();
        List<UserEntity> allUsers = new ArrayList<>();
        List<UserEntity> curDB = getUsersDB();
        int nextIndex = 0;  //4..0
        if (curDB.size() != 0) {
            int maxIndex = 0;
            for (UserEntity u: curDB){
                if (u.getInternalId() > maxIndex) maxIndex = u.getInternalId();
            }
            for (UserListRes user : mResponse) {
                int userIndexInDB = maxIndex+1;
                UserEntity curUser = findUserInDB(user);
                if (curUser != null) {
                    userIndexInDB = curUser.getInternalId();
                } else {
                    maxIndex++;
                }
                List<RepositoryEntity> l = user.getRepositories()
                        .getRepoEntitiesList(String.valueOf(user.getId()));
                allRepositories.addAll(l);
                allUsers.add(new UserEntity(user, userIndexInDB));
            }
        } else {
            for (UserListRes user : mResponse) {
                List<RepositoryEntity> l = user.getRepositories()
                        .getRepoEntitiesList(String.valueOf(user.getId()));
                allRepositories.addAll(l);
                allUsers.add(new UserEntity(user, nextIndex));
                nextIndex++;
            }
        }

        mDaoSession.getRepositoryEntityDao().insertOrReplaceInTx(allRepositories);
        mDaoSession.getUserEntityDao().insertOrReplaceInTx(allUsers);

        SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
        editor.putLong(Const.DB_UPDATED_TIME_KEY, new Date().getTime());
        editor.apply();
    }

    private UserEntity findUserInDB(UserListRes user) {
        return mDaoSession.queryBuilder(UserEntity.class)
                .where(UserEntityDao.Properties.RemoteId.eq(user.getId()))
                .build()
                .unique();
    }

    public List<UserEntity> getUsersDB() {

        try {
            return mDaoSession.queryBuilder(UserEntity.class)
                    .build()
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
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
