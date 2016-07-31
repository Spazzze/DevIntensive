package com.softdesign.devintensive.data.storage.operations;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.network.restmodels.ProfileValues;
import com.softdesign.devintensive.data.storage.models.DaoSession;
import com.softdesign.devintensive.data.storage.models.LikeEntity;
import com.softdesign.devintensive.data.storage.models.RepositoryEntity;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.models.UserEntityDao;
import com.softdesign.devintensive.utils.Const;

import org.greenrobot.greendao.Property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("unused")
public class DatabaseOperation extends BaseChronosOperation<List<UserEntity>> {

    public enum Sort {
        RATING,
        CODE,
        CUSTOM,
        FAVOURITES,
    }

    final DaoSession mDaoSession = DATA_MANAGER.getDaoSession();
    Sort mSort = Sort.CUSTOM;
    Action mAction = Action.LOAD;

    List<UserListRes> mUserListRes;
    ProfileValues mLikeResponse;
    String mLikedUserId;

    String mFirstUserRemoteId, mSecondUserRemoteId;

    public DatabaseOperation() {
        this.mAction = Action.LOAD;
    }

    public DatabaseOperation(Sort sort) {
        this.mAction = Action.LOAD;
        mSort = sort;
    }

    public DatabaseOperation(List<UserListRes> userListRes) {
        this.mUserListRes = userListRes;
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

    public DatabaseOperation(String userId, ProfileValues data) {
        this.mAction = Action.SAVE;
        this.mLikedUserId = userId;
        this.mLikeResponse = data;
    }

    @Nullable
    @Override
    public List<UserEntity> run() {

        switch (this.mAction) {

            case CLEAR:
                clearDB();
                return null;

            case SAVE:
                if (mUserListRes != null) {
                    saveIntoDB(mUserListRes);
                } else if (mLikeResponse != null && mLikedUserId != null) {
                    updateUserInDB(mLikedUserId, mLikeResponse);
                }
                return null;

            case SWAP:
                swapEntityInternalIds(mFirstUserRemoteId, mSecondUserRemoteId);
                return null;

            case LOAD:
                return sortDB(mSort);

            case RESET:
                return resetCustomSorting();
        }
        return null;
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Main methods

    private void updateUserInDB(String likedUserId, ProfileValues values) {
        UserEntity likedEntity = findUserInDB(likedUserId);
        if (likedEntity != null) {
            likedEntity.setRating(values.getIntRating());
            likedEntity.setLikesBy(values.getLikeEntitiesList(likedUserId));
            mDaoSession.getUserEntityDao().update(likedEntity);
        }
    }

    private void saveIntoDB(List<UserListRes> response) {
        List<RepositoryEntity> allRepositories = new ArrayList<>();
        List<UserEntity> allUsers = new ArrayList<>();
        List<LikeEntity> allLikes = new ArrayList<>();
        List<UserEntity> curDB = sortDB(Sort.RATING);

        if (curDB.size() != 0) {
            int maxIndex = findMaxInternalId(curDB);
            for (UserListRes user : response) {
                int userIndexInDB = findUserInternalId(user.getId(), curDB);
                if (userIndexInDB == -1) {
                    userIndexInDB = maxIndex + 1;
                    maxIndex++;
                }
                allUsers.add(new UserEntity(user, userIndexInDB));

                List<RepositoryEntity> rl = user.getRepositories().getRepoEntitiesList(user.getId());
                allRepositories.addAll(rl);

                List<LikeEntity> ll = user.getProfileValues().getLikeEntitiesList(user.getId());
                allLikes.addAll(ll);
            }
        } else {
            for (int i = 0; i < response.size(); i++) {
                UserListRes user = response.get(i);
                allUsers.add(new UserEntity(user, i + 1));

                List<RepositoryEntity> l = user.getRepositories().getRepoEntitiesList(user.getId());
                allRepositories.addAll(l);

                List<LikeEntity> ll = user.getProfileValues().getLikeEntitiesList(user.getId());
                allLikes.addAll(ll);
            }
        }
        mDaoSession.getRepositoryEntityDao().deleteAll();
        mDaoSession.getRepositoryEntityDao().insertInTx(allRepositories);
        mDaoSession.getUserEntityDao().deleteAll();
        mDaoSession.getUserEntityDao().insertInTx(allUsers);
        mDaoSession.getLikeEntityDao().deleteAll();
        mDaoSession.getLikeEntityDao().insertInTx(allLikes);

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
                        .where(UserEntityDao.Properties.InternalId.gt(0))
                        .orderAsc(sortProperty)
                        .build()
                        .list();
            } catch (Exception e) {
                Log.e(TAG, "sortDB: " + e.getMessage());
            }
        } else if (sorting == Sort.FAVOURITES) {
            String myId = DataManager.getInstance().getPreferencesManager().loadBuiltInAuthId();
            try {
                userList = mDaoSession
                        .queryBuilder(UserEntity.class)
                        .orderDesc(sortProperty)
                        .build()
                        .list();
                List<UserEntity> likedUserList = new ArrayList<>();
                for (UserEntity u : userList) {
                    if (u.getLikesList().size() > 0 && u.getLikesList().contains(myId)) {
                        likedUserList.add(u);
                    }
                }
                return likedUserList;
            } catch (Exception e) {
                Log.e(TAG, "sortDB: " + e.getMessage());
            }
        } else {
            try {
                userList = mDaoSession
                        .queryBuilder(UserEntity.class)
                        .orderDesc(sortProperty)
                        .build()
                        .list();
            } catch (Exception e) {
                Log.e(TAG, "sortDB: " + e.getMessage());
            }
        }

        return userList;
    }

    private void clearDB() {
        mDaoSession.getRepositoryEntityDao().deleteAll();
        mDaoSession.getUserEntityDao().deleteAll();
        mDaoSession.getLikeEntityDao().deleteAll();
        mDaoSession.clear();
    }

    private void swapEntityInternalIds(@NonNull String firstUserRemoteId, String secondUserRemoteId) {
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
                mDaoSession.getUserEntityDao().update(firstEntity);
                mDaoSession.getUserEntityDao().update(secondEntity);
            }
        } else {
            firstEntity.setInternalId(0);
            mDaoSession.getUserEntityDao().update(firstEntity);
        }
    }

    private List<UserEntity> resetCustomSorting() {
        List<UserEntity> curDB = sortDB(Sort.RATING);
        for (int i = 0; i < curDB.size(); i++) {
            UserEntity user = curDB.get(i);
            user.setInternalId(i + 1);
        }
        mDaoSession.getUserEntityDao().updateInTx(curDB);
        return curDB;
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Functional methods
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
    //endregion ::::::::::::::::::::::::::::::::::::::::::

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
