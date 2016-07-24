package com.softdesign.devintensive.ui.fragments;

import android.os.Bundle;
import android.util.Log;

import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.network.restmodels.BaseListModel;
import com.softdesign.devintensive.data.operations.DatabaseOperation;
import com.softdesign.devintensive.utils.Const;

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

        DATA_MANAGER.getUserListFromNetwork().enqueue(new NetworkCallback<>());
    }

    public void forceRefreshUserListIntoDB() {
        if (this.mStatus == Status.RUNNING) {
            return;
        } else if (!isExecutePossible()) return;

        onRequestStarted();

        Log.d(TAG, "forceRefreshUserListIntoDB: ");

        DATA_MANAGER.getUserListFromNetwork().enqueue(new NetworkCallback<>());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onRequestComplete(Response response) {
        super.onRequestComplete(response);
        Log.d(TAG, "onRequestComplete: ");
        Response<BaseListModel<UserListRes>> res = (Response<BaseListModel<UserListRes>>) response;
        runOperation(new DatabaseOperation(res.body().getData()));
    }
}
