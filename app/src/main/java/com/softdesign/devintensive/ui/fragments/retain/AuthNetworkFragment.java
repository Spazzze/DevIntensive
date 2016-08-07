package com.softdesign.devintensive.ui.fragments.retain;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.NetworkRequest;
import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserAuthRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.operations.DatabaseOperation;
import com.softdesign.devintensive.data.storage.operations.FullUserDataOperation;
import com.softdesign.devintensive.data.storage.operations.UserLoginDataOperation;
import com.softdesign.devintensive.ui.callbacks.BaseNetworkTaskCallbacks;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import retrofit2.Call;
import retrofit2.Response;

import static com.softdesign.devintensive.data.network.NetworkRequest.ID;
import static com.softdesign.devintensive.data.storage.operations.BaseChronosOperation.Action;
import static com.softdesign.devintensive.utils.AppUtils.isEmptyOrNull;

public class AuthNetworkFragment extends BaseNetworkFragment {

    private AuthNetworkTaskCallbacks mCallbacks;
    private int mWrongPasswordCount;
    private User mUser;

    public interface AuthNetworkTaskCallbacks extends BaseNetworkTaskCallbacks {

        void onErrorCount(int count);
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Life cycle
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUser = DATA_MANAGER.getPreferencesManager().loadAllUserData();
        if (DATA_MANAGER.isUserAuthenticated()) silentSignIn();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AuthNetworkTaskCallbacks) {
            mCallbacks = (AuthNetworkTaskCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement AuthNetworkTaskCallbacks");
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Requests Status
    @Override
    public void onRequestHttpError(@NonNull NetworkRequest request, AppUtils.BackendHttpError error) {
        switch (error.getStatusCode()) {
            case Const.HTTP_RESPONSE_NOT_FOUND:
                error.setErrorMessage(getString(R.string.error_wrong_credentials));
                synchronized (this) {
                    mWrongPasswordCount++;
                }
                if (mUser != null) {
                    if (mWrongPasswordCount == AppConfig.MAX_LOGIN_TRIES) {
                        runOperation(new DatabaseOperation(Action.CLEAR));
                        runOperation(new FullUserDataOperation(Action.CLEAR));
                    }
                    if (mCallbacks != null) mCallbacks.onErrorCount(mWrongPasswordCount);
                }
                break;
        }
        super.onRequestHttpError(request, error);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Network Requests

    public void signIn(@NonNull String id, @NonNull String pass) {

        final ID reqId = ID.SILENT_AUTH;

        if (!isAuthExecutePossible(reqId)) return;

        final NetworkRequest request = onRequestStarted(reqId);

        Call<BaseModel<UserAuthRes>> call = DATA_MANAGER.loginUser(new UserLoginReq(id, pass));

        call.enqueue(new NetworkCallback<BaseModel<UserAuthRes>>(request) {
            @Override
            public void onResponse(Call<BaseModel<UserAuthRes>> call, Response<BaseModel<UserAuthRes>> response) {
                if (response.isSuccessful()) {
                    mWrongPasswordCount = 0;
                    UserAuthRes data = response.body().getData();
                    if (!isEmptyOrNull(data)) {
                        saveUserAuthData(response.body());
                        updateUserInfoFromServer(request, data.getUser());
                    } else {
                        onRequestResponseEmpty(request);
                    }
                } else {
                    onRequestHttpError(request, AppUtils.parseHttpError(response));
                }
            }
        });
    }

    private void silentSignIn() {

        final ID reqId = ID.AUTH;

        if (!isAuthExecutePossible(reqId)) return;

        final NetworkRequest request = onRequestStarted(reqId);

        Call<BaseModel<User>> call = DATA_MANAGER.getUserData(DATA_MANAGER.getPreferencesManager().loadBuiltInAuthId());

        call.enqueue(new NetworkCallback<BaseModel<User>>(request) {
            @Override
            public void onResponse(Call<BaseModel<User>> call,
                                   Response<BaseModel<User>> response) {
                if (response.isSuccessful()) {
                    if (AppUtils.isEmptyOrNull(response.body())) {
                        onRequestResponseEmpty(request);
                    } else {
                        updateUserInfoFromServer(request, response.body().getData());
                    }
                } else {
                    switch (AppUtils.parseHttpError(response).getStatusCode()) {
                        case Const.HTTP_RESPONSE_NOT_FOUND:
                        case Const.HTTP_FORBIDDEN:
                        case Const.HTTP_UNAUTHORIZED:
                            runOperation(new UserLoginDataOperation(Action.CLEAR));
                            break;
                    }
                    if (mCallbacks != null)
                        mCallbacks.onNetworkRequestFailed(request);
                }
            }
        });
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Utils
    private void saveUserAuthData(@NonNull BaseModel<UserAuthRes> userModelRes) {
        DATA_MANAGER.getPreferencesManager().saveBuiltInAuthInfo(
                userModelRes.getData().getUser().getId(),
                userModelRes.getData().getToken()
        );
    }

    private void updateUserInfoFromServer(final @NonNull NetworkRequest request, @NonNull final User user) {
        runOperation(new FullUserDataOperation(user));
        onRequestComplete(request, null);
    }
}