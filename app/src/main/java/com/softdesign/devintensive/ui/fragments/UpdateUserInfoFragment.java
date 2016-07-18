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

public class UpdateUserInfoFragment extends Fragment {

    private static final String TAG = ConstantManager.TAG_PREFIX + "UpdUserInfoFrag";

    private TaskCallbacks mCallbacks;
    private boolean isRequestSuccessful = false;
    private String mError = null;
    private DataManager mDataManager;
    private User mUser;

    @SuppressWarnings("EmptyMethod")
    public interface TaskCallbacks {
        void onAuthRequestFinished();

        void onAuthRequestCancelled(String error);
    }

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
        if (mDataManager.isUserAuthenticated()) silentLogin();
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

    /**
     * The Activity can call this when it wants to start the task
     */
    private void silentLogin() {
        isRequestSuccessful = false;
        mError = null;

        Call<BaseModel<User>> call = mDataManager.getUserData(mDataManager.getPreferencesManager().loadBuiltInAuthId());

        call.enqueue(new Callback<BaseModel<User>>() {
            @Override
            public void onResponse(Call<BaseModel<User>> call,
                                   Response<BaseModel<User>> response) {
                if (response.isSuccessful()) {

                    if (!isEmptyOrNull(response.body().getData())) {
                        isRequestSuccessful = true;
                        updateUserInfo(response.body().getData());
                    } else {
                        mError = getString(R.string.error_response_is_empty);
                        if (mCallbacks != null)
                            mCallbacks.onAuthRequestCancelled(mError);
                    }
                } else {
                    mError = ErrorUtils.parseHttpError(response).getErrorMessage();
                    Log.e(TAG, "onResponse: " + mError);
                    if (mCallbacks != null) mCallbacks.onAuthRequestCancelled(mError);
                }
            }

            @Override
            public void onFailure(Call<BaseModel<User>> call, Throwable t) {
                mError = String.format("%s: %s", getString(R.string.error_unknown_response_error), t.getMessage());
                Log.e(TAG, "onFailure: " + mError);
                if (mCallbacks != null) mCallbacks.onAuthRequestCancelled(mError);
            }
        });
    }

    public void updateUserInfo(User user) {
        if (mUser != null && mUser.getUpdated().equals(user.getUpdated())) {
            finishedAuth();
        } else {
            mDataManager.getPreferencesManager().saveAllUserData(user);
            if (mUser == null || !mUser.getPublicInfo().getUpdated().equals(user.getPublicInfo().getUpdated())) {
                String pathToPhoto = user.getPublicInfo().getPhoto();
                if (!isEmptyOrNull(pathToPhoto)) updateUserPhoto(pathToPhoto);
            } else {
                finishedAuth();
            }
        }
    }

    public void updateUserPhoto(@NonNull final String pathToPhoto) {
        Log.d(TAG, "updateUserPhoto: ");
        int photoWidth = getScreenWidth();
        int photoHeight = (int) (photoWidth / ConstantManager.ASPECT_RATIO_3_2);

        final GlideTargetIntoBitmap photoTarget = new GlideTargetIntoBitmap(photoWidth, photoHeight, "photo") {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                super.onResourceReady(bitmap, anim);
                Log.d(TAG, "onResourceReady: Success");
                mDataManager.getPreferencesManager().saveUserPhoto(Uri.fromFile(getFile()));
                finishedAuth();
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                Log.e(TAG, "updateUserPhoto onLoadFailed: " + e.getMessage());
                mDataManager.getPreferencesManager().saveUserPhoto(Uri.parse(pathToPhoto));
                finishedAuth();
            }
        };
        Glide.with(this)
                .load(pathToPhoto)
                .asBitmap()
                .into(photoTarget);
    }

    private void finishedAuth() {
        if (mCallbacks != null) mCallbacks.onAuthRequestFinished();
    }
}
