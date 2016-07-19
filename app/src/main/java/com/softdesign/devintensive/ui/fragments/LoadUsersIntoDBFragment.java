package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.network.restmodels.BaseListModel;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ErrorUtils;
import com.softdesign.devintensive.utils.UiHelper;

import java.util.List;

import lombok.Getter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This Fragment manages a single background task and retains
 * itself across configuration changes.
 */
public class LoadUsersIntoDBFragment extends Fragment {
    private static final String TAG = ConstantManager.TAG_PREFIX + "getUsersIntoDBFrag";

    private TaskCallbacks mCallbacks;

    @Getter private boolean isRequestSuccessful = false;
    @Getter private boolean isRequestStarted = false;
    @Getter private String mError = null;
    private DataManager mDataManager;

    @SuppressWarnings("EmptyMethod")
    public interface TaskCallbacks {

        void onLoadIntoDBStarted();

        void onLoadIntoDBCompleted();

        void onLoadIntoDBFailed(String error);
    }

    //region Fragment Life Cycle
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCallbacks != null) {
            if (isRequestSuccessful)
                mCallbacks.onLoadIntoDBCompleted();
            else {
                if (!UiHelper.isEmptyOrNull(mError)) {
                    mCallbacks.onLoadIntoDBFailed(mError);
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

    /**
     * The Activity can call this when it wants to start the task
     */
    public void downloadUserListIntoDB() {
        Log.d(TAG, "downloadUserListIntoDB: ");
        if (!mDataManager.isUserAuthenticated() ||
                !mDataManager.getPreferencesManager().isDBNeedsUpdate() || isRequestStarted)
            return;

        onRequestStarted();

        Call<BaseListModel<UserListRes>> call = DataManager.getInstance().getUserListFromNetwork();
        call.enqueue(new Callback<BaseListModel<UserListRes>>() {
                         @Override
                         public void onResponse(Call<BaseListModel<UserListRes>> call, final Response<BaseListModel<UserListRes>> response) {
                             if (response.isSuccessful()) {
                                 List<UserListRes> responseData = response.body().getData();
                                 if (UiHelper.isEmptyOrNull(responseData)) {
                                     onRequestEmpty();
                                 } else {
                                     onDownloadComplete(responseData);
                                 }
                             } else {
                                 onRequestHttpError(ErrorUtils.parseHttpError(response));
                             }
                         }

                         @Override
                         public void onFailure(Call<BaseListModel<UserListRes>> call, Throwable t) {
                             onRequestFailure(t);
                         }
                     }
        );
    }
    //endregion

    //region Handling Request's status

    private void onRequestStarted() {
        isRequestSuccessful = false;
        isRequestStarted = true;
        mError = null;
        if (mCallbacks != null) mCallbacks.onLoadIntoDBStarted();
    }

    private void onRequestEmpty() {
        isRequestStarted = false;
        mError = getString(R.string.error_response_is_empty);
        if (mCallbacks != null)
            mCallbacks.onLoadIntoDBFailed(mError);
    }

    private void onRequestHttpError(ErrorUtils.BackendHttpError error) {
        isRequestStarted = false;
        mError = error.getErrorMessage();
        Log.e(TAG, "onResponse: " + mError);
        if (mCallbacks != null) mCallbacks.onLoadIntoDBFailed(mError);
    }

    private void onRequestFailure(Throwable t) {
        isRequestStarted = false;
        mError = String.format("%s: %s", getString(R.string.error_unknown_response), t.getMessage());
        Log.e(TAG, "onFailure: " + mError);
        if (mCallbacks != null) mCallbacks.onLoadIntoDBFailed(mError);
    }

    private void onDownloadComplete(List<UserListRes> responseData) {
        isRequestStarted = false;
        isRequestSuccessful = true;
        mDataManager.fillDataBase(responseData);
        if (mCallbacks != null) mCallbacks.onLoadIntoDBCompleted();
    }

    //endregion
}
