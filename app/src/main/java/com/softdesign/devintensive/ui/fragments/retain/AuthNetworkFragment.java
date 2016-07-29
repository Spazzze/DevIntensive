package com.softdesign.devintensive.ui.fragments.retain;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.GlideTargetIntoBitmap;
import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserAuthRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.operations.BaseChronosOperation;
import com.softdesign.devintensive.data.storage.operations.DatabaseOperation;
import com.softdesign.devintensive.data.storage.operations.FullUserDataOperation;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.ui.callbacks.BaseTaskCallbacks;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.softdesign.devintensive.utils.AppUtils.getScreenWidth;
import static com.softdesign.devintensive.utils.AppUtils.isEmptyOrNull;

public class AuthNetworkFragment extends BaseNetworkFragment {

    private AuthTaskCallbacks mCallbacks;
    private volatile int mWrongPasswordCount;
    private volatile User mUser;

    public interface AuthTaskCallbacks extends BaseTaskCallbacks {

        void onErrorCount(int count);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUser = DATA_MANAGER.getPreferencesManager().loadAllUserData();
        if (DATA_MANAGER.isUserAuthenticated()) silentSignIn();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof AuthTaskCallbacks) {
            mCallbacks = (AuthTaskCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement AuthTaskCallbacks");
        }
    }

    @Override
    public void onRequestHttpError(AppUtils.BackendHttpError error) {

        mStatus = Status.FINISHED;
        mCancelled = true;

        switch (error.getStatusCode()) {
            case Const.HTTP_RESPONSE_NOT_FOUND:
                synchronized (this) {
                    mError = getString(R.string.error_wrong_credentials);
                    mWrongPasswordCount++;
                }
                if (mUser != null) {
                    if (mWrongPasswordCount == AppConfig.MAX_LOGIN_TRIES) {
                        runOperation(new DatabaseOperation(BaseChronosOperation.Action.CLEAR));
                        runOperation(new FullUserDataOperation(BaseChronosOperation.Action.CLEAR));
                    }
                    if (mCallbacks != null) mCallbacks.onErrorCount(mWrongPasswordCount);
                }
                break;
            default:
                synchronized (this) {
                    mError = error.getErrorMessage();
                }
                break;
        }

        if (mCallbacks != null) mCallbacks.onRequestFailed(mError);
    }

    public void onRequestComplete(User user) {
        String photo = DATA_MANAGER.getPreferencesManager().loadUserPhoto();
        String avatar = DATA_MANAGER.getPreferencesManager().loadUserAvatar();
        BUS.postSticky(new ProfileViewModel(user,
                !AppUtils.isEmptyOrNull(photo) ? photo : user.getPublicInfo().getPhoto(),
                !AppUtils.isEmptyOrNull(avatar) ? avatar : user.getPublicInfo().getAvatar()));
        super.onRequestComplete(null);
    }

    public void onRequestComplete(User user, Uri photoUri) {
        String avatar = DATA_MANAGER.getPreferencesManager().loadUserAvatar();
        BUS.postSticky(new ProfileViewModel(user, photoUri.toString(),
                !AppUtils.isEmptyOrNull(avatar) ? avatar : user.getPublicInfo().getAvatar()));
        super.onRequestComplete(null);
    }

    //region Network Requests

    public void signIn(String id, String pass) {

        if (getStatus() == Status.RUNNING) return;

        onRequestStarted();

        Call<BaseModel<UserAuthRes>> call = DATA_MANAGER.loginUser(new UserLoginReq(id, pass));

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
                    onRequestHttpError(AppUtils.parseHttpError(response));
                }
            }

            @Override
            public void onFailure(Call<BaseModel<UserAuthRes>> call, Throwable t) {
                onRequestFailure(t);
            }
        });
    }

    private void silentSignIn() {
        if (getStatus() == Status.RUNNING || !isExecutePossible()) return;

        onRequestStarted();

        Call<BaseModel<User>> call = DATA_MANAGER.getUserData(DATA_MANAGER.getPreferencesManager().loadBuiltInAuthId());

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
        DATA_MANAGER.getPreferencesManager().saveBuiltInAuthInfo(
                userModelRes.getData().getUser().getId(),
                userModelRes.getData().getToken()
        );
    }

    private void updateUserInfoFromServer(User user) {
        if (mUser != null && mUser.getUpdated().equals(user.getUpdated())) {
            onRequestComplete(user);
        } else {
            runOperation(new FullUserDataOperation(user));
            if (mUser == null || !mUser.getPublicInfo().getUpdated().equals(user.getPublicInfo().getUpdated())) {
                if (!isEmptyOrNull(user.getPublicInfo().getPhoto()))
                    downloadUserPhoto(user);
            } else {
                onRequestComplete(user);
            }
        }
    }

    private void downloadUserPhoto(@NonNull final User user) {
        Log.d(TAG, "downloadUserPhoto: ");
        String pathToPhoto = user.getPublicInfo().getPhoto();

        int photoWidth = getScreenWidth();
        int photoHeight = (int) (photoWidth / Const.ASPECT_RATIO_3_2);

        final GlideTargetIntoBitmap photoTarget = new GlideTargetIntoBitmap(photoWidth, photoHeight, "photo") {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                super.onResourceReady(bitmap, anim);
                Log.d(TAG, "onResourceReady: Success");
                runOperation(new FullUserDataOperation(Uri.fromFile(getFile())));
                onRequestComplete(user, Uri.fromFile(getFile()));
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                Log.e(TAG, "downloadUserPhoto onLoadFailed: " + e.getMessage());
                runOperation(new FullUserDataOperation(Uri.parse(pathToPhoto)));
                onRequestComplete(user, Uri.parse(pathToPhoto));
            }
        };
        Glide.with(this)
                .load(pathToPhoto)
                .asBitmap()
                .into(photoTarget);
    }
}


