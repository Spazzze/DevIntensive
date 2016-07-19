package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserAuthRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.ui.adapters.GlideTargetIntoBitmap;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ErrorUtils;
import com.softdesign.devintensive.utils.UiHelper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.softdesign.devintensive.utils.UiHelper.getScreenWidth;
import static com.softdesign.devintensive.utils.UiHelper.isEmptyOrNull;

public class AuthNetworkFragment extends Fragment {

    private static final String TAG = ConstantManager.TAG_PREFIX + "AuthNetworkFragment";

    private TaskCallbacks mCallbacks;
    private boolean isRequestSuccessful = false;
    private int mWrongPasswordCount;
    private String mError = null;
    private DataManager mDataManager;
    private User mUser;

    @SuppressWarnings("EmptyMethod")
    public interface TaskCallbacks {
        void onAuthRequestStarted();

        void onAuthRequestFinished();

        void onAuthRequestFailed(int wrongPasswordCount);

        void onAuthRequestCancelled(String error);
    }

    //region Life cycle
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCallbacks != null) {
            if (isRequestSuccessful)
                mCallbacks.onAuthRequestFinished();
            else {
                if (!UiHelper.isEmptyOrNull(mError)) {
                    mCallbacks.onAuthRequestCancelled(mError);
                }
            }
        }
    }

    /**
     * Hold a reference to the parent Activity so we can report the
     * task's current progress and results. The Android framework
     * will pass us a reference to the newly created Activity after
     * each configuration change.
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof TaskCallbacks) {
            mCallbacks = (TaskCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement TaskCallbacks");
        }
    }

    /**
     * This method will only be called once when the retained
     * Fragment is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mDataManager = DataManager.getInstance();
        mUser = mDataManager.getPreferencesManager().loadAllUserData();

        if (mDataManager.isUserAuthenticated()) silentSignIn();
    }

    /**
     * Set the callback to null so we don't accidentally leak the
     * Activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }
    //endregion

    //region Network Requests

    public void signIn(String id, String pass) {
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
                        onRequestEmpty();
                    }
                } else {
                    Log.d(TAG, "onResponse: " + response.code());
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

    /**
     * The Activity can call this when it wants to start the task
     */
    private void silentSignIn() {
        onRequestStarted();

        Call<BaseModel<User>> call = mDataManager.getUserData(mDataManager.getPreferencesManager().loadBuiltInAuthId());

        call.enqueue(new Callback<BaseModel<User>>() {
            @Override
            public void onResponse(Call<BaseModel<User>> call,
                                   Response<BaseModel<User>> response) {
                if (response.isSuccessful()) {
                    if (!isEmptyOrNull(response.body().getData())) {
                        updateUserInfoFromServer(response.body().getData());
                    } else {
                        onRequestEmpty();
                    }
                } else {
                    onRequestHttpError(ErrorUtils.parseHttpError(response));
                }
            }

            @Override
            public void onFailure(Call<BaseModel<User>> call, Throwable t) {
                onRequestFailure(t);
            }
        });
    }

    //region Handling Request's status

    private void onRequestStarted() {
        isRequestSuccessful = false;
        mError = null;
        if (mCallbacks != null) mCallbacks.onAuthRequestStarted();
    }

    private void onWrongCredentials() {
        mWrongPasswordCount++;
        mError = getString(R.string.error_wrong_credentials);
        if (mCallbacks != null) {
            mCallbacks.onAuthRequestCancelled(mError);
            if (mUser != null)
                mCallbacks.onAuthRequestFailed(mWrongPasswordCount);
        }
    }

    private void onRequestEmpty() {
        mError = getString(R.string.error_response_is_empty);
        if (mCallbacks != null)
            mCallbacks.onAuthRequestCancelled(mError);
    }

    private void onRequestHttpError(ErrorUtils.BackendHttpError error) {
        mError = error.getErrorMessage();
        Log.e(TAG, "onResponse: " + mError);
        if (mCallbacks != null) mCallbacks.onAuthRequestCancelled(mError);
    }

    private void onRequestFailure(Throwable t) {
        mError = String.format("%s: %s", getString(R.string.error_unknown_response), t.getMessage());
        Log.e(TAG, "onFailure: " + mError);
        if (mCallbacks != null) mCallbacks.onAuthRequestCancelled(mError);
    }

    private void onAuthComplete() {
        isRequestSuccessful = true;
        if (mCallbacks != null) mCallbacks.onAuthRequestFinished();
    }

    //endregion

    //endregion

    //region Data methods

    private void saveUserAuthData(@NonNull BaseModel<UserAuthRes> userModelRes) {
        mDataManager.getPreferencesManager().saveBuiltInAuthInfo(
                userModelRes.getData().getUser().getId(),
                userModelRes.getData().getToken()
        );
    }

    private void updateUserInfoFromServer(User user) {
        if (mUser != null && mUser.getUpdated().equals(user.getUpdated())) {
            onAuthComplete();
        } else {
            mDataManager.getPreferencesManager().saveAllUserData(user);
            if (mUser == null || !mUser.getPublicInfo().getUpdated().equals(user.getPublicInfo().getUpdated())) {
                String pathToPhoto = user.getPublicInfo().getPhoto();
                if (!isEmptyOrNull(pathToPhoto)) downloadUserPhoto(pathToPhoto);
            } else {
                onAuthComplete();
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
                onAuthComplete();
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                Log.e(TAG, "downloadUserPhoto onLoadFailed: " + e.getMessage());
                mDataManager.getPreferencesManager().saveUserPhoto(Uri.parse(pathToPhoto));
                onAuthComplete();
            }
        };
        Glide.with(this)
                .load(pathToPhoto)
                .asBitmap()
                .into(photoTarget);
    }
    //endregion
}


