package com.softdesign.devintensive.ui.fragments.retain;

import android.os.Bundle;
import android.util.Log;

import com.softdesign.devintensive.data.network.NetworkRequest;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.network.restmodels.BaseListModel;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.ProfileValues;
import com.softdesign.devintensive.data.storage.operations.DBUpdateProfileValuesOperation;
import com.softdesign.devintensive.data.storage.operations.DatabaseOperation;
import com.softdesign.devintensive.ui.activities.MainActivity;
import com.softdesign.devintensive.utils.AppUtils;

import retrofit2.Call;
import retrofit2.Response;

import static com.softdesign.devintensive.data.network.NetworkRequest.ID;

/**
 * This Fragment manages a single background task and retains
 * itself across configuration changes.
 */
public class LoadUsersInfoFragment extends BaseNetworkFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        downloadUserListIntoDB();
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Requests
    public void downloadUserListIntoDB() {

        final ID reqId = ID.LOAD_DB;

        if (!DATA_MANAGER.getPreferencesManager().isDBNeedsUpdate()) {
            return;
        } else if (!isExecutePossible(reqId)) return;

        final NetworkRequest request = onRequestStarted(reqId);

        Log.d(TAG, "downloadUserListIntoDB: ");

        getUserListFromServer(request);
    }

    public void forceRefreshUserListIntoDB() {

        final ID reqId = ID.LOAD_DB;

        if (!isExecutePossible(reqId)) return;

        final NetworkRequest request = onRequestStarted(reqId);

        Log.d(TAG, "forceRefreshUserListIntoDB: ");

        getUserListFromServer(request);
    }

    public void likeUser(final String userId, final boolean isLiked) {

        ID reqId;
        if (isLiked) {
            reqId = ID.LIKE;
        } else {
            reqId = ID.UNLIKE;
        }

        if (!isExecutePossible(reqId, userId)) return;

        final NetworkRequest request = onRequestStarted(reqId, userId);

        NetworkCallback<BaseModel<ProfileValues>> callback = new NetworkCallback<BaseModel<ProfileValues>>(request) {
            @Override
            public void onResponse(Call<BaseModel<ProfileValues>> call, Response<BaseModel<ProfileValues>> response) {
                if (response.isSuccessful()) {
                    if (AppUtils.isEmptyOrNull(response.body())) {
                        onRequestResponseEmpty(request);
                    } else {
                        ProfileValues res = response.body().getData();
                        MainActivity mainActivity = (MainActivity) getActivity();
                        if (mainActivity != null) {
                            mainActivity.runOperation(new DBUpdateProfileValuesOperation(userId, res));
                            removeRequest(request);
                        } else {
                            onRequestComplete(request, response);
                        }
                    }
                } else {
                    onRequestHttpError(request, AppUtils.parseHttpError(response));
                }
            }
        };

        if (!isLiked) {
            DATA_MANAGER.unlikeUser(userId).enqueue(callback);
        } else {
            DATA_MANAGER.likeUser(userId).enqueue(callback);
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Utils
    private void getUserListFromServer(NetworkRequest request) {
        DATA_MANAGER.getUserListFromNetwork().enqueue(new NetworkCallback<BaseListModel<UserListRes>>(request) {
            @Override
            public void onResponse(Call<BaseListModel<UserListRes>> call, Response<BaseListModel<UserListRes>> response) {
                if (response.isSuccessful()) {
                    if (AppUtils.isEmptyOrNull(response.body())) {
                        onRequestResponseEmpty(request);
                    } else {
                        runOperation(new DatabaseOperation(response.body().getData()));
                        onRequestComplete(request, response);
                    }
                } else {
                    onRequestHttpError(request, AppUtils.parseHttpError(response));
                }
            }
        });
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
