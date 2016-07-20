package com.softdesign.devintensive.ui.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserAuthRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.ui.adapters.GlideTargetIntoBitmap;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ErrorUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.softdesign.devintensive.utils.UiHelper.getScreenWidth;
import static com.softdesign.devintensive.utils.UiHelper.isEmptyOrNull;

public class AuthNetworkFragment extends BaseNetworkFragment {

    private static final String TAG = ConstantManager.TAG_PREFIX + "AuthNetworkFragment";

    private int mWrongPasswordCount;
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = mDataManager.getPreferencesManager().loadAllUserData();
        if (mDataManager.isUserAuthenticated()) silentSignIn();
    }

    private void onWrongCredentials() {
        mError = getString(R.string.error_wrong_credentials);
        if (mUser != null) {
            mWrongPasswordCount++;
            if (mWrongPasswordCount == AppConfig.MAX_LOGIN_TRIES)
                mDataManager.getPreferencesManager().totalLogout();
            if (mCallbacks != null) mCallbacks.onErrorCount(mWrongPasswordCount);
        }
        onRequestHttpError(new ErrorUtils.BackendHttpError(0, mError));
    }

    //region Network Requests

    public void signIn(String id, String pass) {

        if (getStatus() == Status.RUNNING) return;

        onRequestStarted();

        Call<BaseModel<UserAuthRes>> call = mDataManager.loginUser(new UserLoginReq(id, pass));

        call.enqueue(new Callback<BaseModel<UserAuthRes>>() {
            @Override
            public void onResponse(Call<BaseModel<UserAuthRes>> call, Response<BaseModel<UserAuthRes>> response) {
                if (response.isSuccessful()) {
                    mWrongPasswordCount = 0;
                    UserAuthRes data = response.body().getData();
                    if (!isEmptyOrNull(data)) {
                        saveUserAuthData(response.body());
                        updateUserInfoFromServer(data.getUser());
                    } else {
                        onRequestResponseEmpty();
                    }
                } else {
                    switch (response.code()) {
                        case ConstantManager.HTTP_RESPONSE_NOT_FOUND:
                            onWrongCredentials();
                            break;
                        default:
                            onRequestHttpError(ErrorUtils.parseHttpError(response));
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseModel<UserAuthRes>> call, Throwable t) {
                onRequestFailure(t);
            }
        });
    }

    private void silentSignIn() {
        if (getStatus() == Status.RUNNING) return;

        onRequestStarted();

        Call<BaseModel<User>> call = mDataManager.getUserData(mDataManager.getPreferencesManager().loadBuiltInAuthId());

        call.enqueue(new NetworkCallback<BaseModel<User>>() {
            @Override
            public void onResponse(Call<BaseModel<User>> call,
                                   Response<BaseModel<User>> response) {
                if (response.isSuccessful()) {
                    if (!isEmptyOrNull(response.body().getData())) {
                        updateUserInfoFromServer(response.body().getData());
                    }
                }
            }
        });
    }

    //endregion

    private void saveUserAuthData(@NonNull BaseModel<UserAuthRes> userModelRes) {
        mDataManager.getPreferencesManager().saveBuiltInAuthInfo(
                userModelRes.getData().getUser().getId(),
                userModelRes.getData().getToken()
        );
    }

    private void updateUserInfoFromServer(User user) {
        if (mUser != null && mUser.getUpdated().equals(user.getUpdated())) {
            onRequestComplete(null);
        } else {
            mDataManager.getPreferencesManager().saveAllUserData(user);
            if (mUser == null || !mUser.getPublicInfo().getUpdated().equals(user.getPublicInfo().getUpdated())) {
                String pathToPhoto = user.getPublicInfo().getPhoto();
                if (!isEmptyOrNull(pathToPhoto)) downloadUserPhoto(pathToPhoto);
            } else {
                onRequestComplete(null);
            }
        }
    }

    private void downloadUserPhoto(@NonNull final String pathToPhoto) {
        Log.d(TAG, "downloadUserPhoto: ");
        int photoWidth = getScreenWidth();
        int photoHeight = (int) (photoWidth / ConstantManager.ASPECT_RATIO_3_2);

        final GlideTargetIntoBitmap photoTarget = new GlideTargetIntoBitmap(photoWidth, photoHeight, "photo") {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                super.onResourceReady(bitmap, anim);
                Log.d(TAG, "onResourceReady: Success");
                mDataManager.getPreferencesManager().saveUserPhoto(Uri.fromFile(getFile()));
                onRequestComplete(null);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                Log.e(TAG, "downloadUserPhoto onLoadFailed: " + e.getMessage());
                mDataManager.getPreferencesManager().saveUserPhoto(Uri.parse(pathToPhoto));
                onRequestComplete(null);
            }
        };
        Glide.with(this)
                .load(pathToPhoto)
                .asBitmap()
                .into(photoTarget);
    }
}


