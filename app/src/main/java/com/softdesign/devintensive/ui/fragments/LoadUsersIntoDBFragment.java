package com.softdesign.devintensive.ui.fragments;

import android.os.Bundle;
import android.util.Log;

import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.network.restmodels.BaseListModel;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.ProfileValues;
import com.softdesign.devintensive.data.operations.DatabaseOperation;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import retrofit2.Call;
import retrofit2.Response;

/**
 * This Fragment manages a single background task and retains
 * itself across configuration changes.
 */
public class LoadUsersIntoDBFragment extends BaseNetworkFragment {
    private static final String TAG = Const.TAG_PREFIX + "getUsersIntoDBFrag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadUserListIntoDB();
    }

    //region Requests

    /**
     * The Activity can call this when it wants to start the task
     */
    public void downloadUserListIntoDB() {

        if (this.mStatus == Status.RUNNING) {
            return;
        } else if (!DATA_MANAGER.getPreferencesManager().isDBNeedsUpdate()) {
            this.mStatus = Status.FINISHED;
            return;
        } else if (!isExecutePossible()) return;

        onRequestStarted();

        Log.d(TAG, "downloadUserListIntoDB: ");

        getUserListFromServer();
    }

    public void forceRefreshUserListIntoDB() {
        if (this.mStatus == Status.RUNNING) {
            return;
        } else if (!isExecutePossible()) return;

        onRequestStarted();

        Log.d(TAG, "forceRefreshUserListIntoDB: ");

        getUserListFromServer();
    }

    private void getUserListFromServer() {
        DATA_MANAGER.getUserListFromNetwork().enqueue(new NetworkCallback<BaseListModel<UserListRes>>() {
            @Override
            public void onResponse(Call<BaseListModel<UserListRes>> call, Response<BaseListModel<UserListRes>> response) {
                if (response.isSuccessful()) {
                    if (AppUtils.isEmptyOrNull(response.body())) {
                        onRequestResponseEmpty();
                    } else {
                        runOperation(new DatabaseOperation(response.body().getData()));
                        onRequestComplete(response);
                    }
                } else {
                    onRequestHttpError(AppUtils.parseHttpError(response));
                }
            }
        });
    }

    public void likeUser(final String userId, boolean isLiked) {

        if (!isExecutePossible()) return;

        NetworkCallback<BaseModel<ProfileValues>> callback = new NetworkCallback<BaseModel<ProfileValues>>() {
            @Override
            public void onResponse(Call<BaseModel<ProfileValues>> call, Response<BaseModel<ProfileValues>> response) {
                if (response.isSuccessful()) {
                    if (AppUtils.isEmptyOrNull(response.body())) {
                        onRequestResponseEmpty();
                    } else {
                        runOperation(new DatabaseOperation(response.body().getData(), userId));
                        onRequestComplete(response);
                    }
                } else {
                    onRequestHttpError(AppUtils.parseHttpError(response));
                }
            }
        };

        if (isLiked){
            DATA_MANAGER.unlikeUser(userId).enqueue(callback);
        }  else {
            DATA_MANAGER.likeUser(userId).enqueue(callback);
        }
    }
    //endregion
}
