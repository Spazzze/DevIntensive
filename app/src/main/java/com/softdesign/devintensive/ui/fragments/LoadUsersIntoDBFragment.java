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
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.softdesign.devintensive.utils.ErrorUtils;
import com.softdesign.devintensive.utils.NetworkUtils;
import com.softdesign.devintensive.utils.UiHelper;

import java.util.List;

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
    private boolean isRequestSuccessful = false;
    private String mError = null;
    private DataManager mDataManager;
    private boolean isRequestStarted = false;

    @SuppressWarnings("EmptyMethod")
    public interface TaskCallbacks {

        void onRequestStarted();

        void onRequestFinished();

        void onRequestCancelled(String error);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCallbacks != null) {
            if (isRequestSuccessful)
                mCallbacks.onRequestFinished();
            else {
                if (!UiHelper.isEmptyOrNull(mError)) {
                    mCallbacks.onRequestCancelled(mError);
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
        if (mDataManager.isUserAuthenticated()
                && mDataManager.getPreferencesManager().isDBNeedsUpdate())
            downloadUserListIntoDB();
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

    public boolean isRequestStarted() {
        return isRequestStarted;
    }

    /**
     * The Activity can call this when it wants to start the task
     */
    public void downloadUserListIntoDB() {
        Log.d(TAG, "downloadUserListIntoDB: ");
        if (!NetworkUtils.isNetworkAvailable(DevIntensiveApplication.getContext())) {
            if (mCallbacks != null)
                mCallbacks.onRequestCancelled(getString(R.string.error_no_network_connection));
            return;
        }

        isRequestSuccessful = false;
        mError = null;
        isRequestStarted = true;

        if (mCallbacks != null) mCallbacks.onRequestStarted();
        Call<BaseListModel<UserListRes>> call = DataManager.getInstance().getUserListFromNetwork();
        call.enqueue(new Callback<BaseListModel<UserListRes>>() {
                         @Override
                         public void onResponse(Call<BaseListModel<UserListRes>> call, final Response<BaseListModel<UserListRes>> response) {
                             if (response.isSuccessful()) {

                                 List<UserListRes> responseData = response.body().getData();

                                 if (UiHelper.isEmptyOrNull(responseData)) {
                                     mError = getString(R.string.error_response_is_empty);
                                     if (mCallbacks != null)
                                         mCallbacks.onRequestCancelled(mError);
                                     return;
                                 }

                                 isRequestSuccessful = true;
                                 mDataManager.fillDataBase(responseData);
                                 if (mCallbacks != null) mCallbacks.onRequestFinished();
                             } else {
                                 mError = ErrorUtils.parseHttpError(response).getErrorMessage();
                                 if (mCallbacks != null) mCallbacks.onRequestCancelled(mError);
                             }
                         }

                         @Override
                         public void onFailure(Call<BaseListModel<UserListRes>> call, Throwable t) {
                             mError = String.format("%s: %s", getString(R.string.error_unknown_response_error), t.getMessage());
                             if (mCallbacks != null)
                                 mCallbacks.onRequestCancelled(mError);
                         }
                     }
        );
    }
}
