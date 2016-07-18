package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.network.restmodels.BaseListModel;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ErrorUtils;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This Fragment manages a single background task and retains
 * itself across configuration changes.
 */
public class GetUsersNetworkTaskFragment extends Fragment {
    private static final String TAG = ConstantManager.TAG_PREFIX + "GetUsersNTFragment";

    private TaskCallbacks mCallbacks;
    private List<UserListRes> mResult;
    private ProgressDialog mDialog;

    public interface TaskCallbacks {

        void onRequestStarted();

        void onRequestFinished(List<UserListRes> result);

        void onRequestCancelled(String error);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ((mResult != null) && (mCallbacks != null)) {
            mCallbacks.onRequestFinished(mResult);
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
        getUserList();
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
    public void getUserList() {
        if (mCallbacks != null) mCallbacks.onRequestStarted();
        mDialog = new ProgressDialog(getActivity());
        mDialog.setMessage(getString(R.string.hint_loading));
        mDialog.setIndeterminate(true);
        mDialog.setCancelable(false);
        mDialog.show();
        Call<BaseListModel<UserListRes>> call = DataManager.getInstance().getUserList();
        call.enqueue(new Callback<BaseListModel<UserListRes>>() {
                         @Override
                         public void onResponse(Call<BaseListModel<UserListRes>> call, Response<BaseListModel<UserListRes>> response) {
                             mDialog.dismiss();
                             if (response.isSuccessful()) {
                                 mResult = response.body().getData();
                                 if (mCallbacks != null) mCallbacks.onRequestFinished(mResult);
                             } else {
                                 ErrorUtils.BackendHttpError error = ErrorUtils.parseHttpError(response);
                                 if (mCallbacks != null) mCallbacks.onRequestCancelled(error.getErrMessage());
                             }
                         }

                         @Override
                         public void onFailure(Call<BaseListModel<UserListRes>> call, Throwable t) {
                             mDialog.dismiss();
                             if (mCallbacks != null) mCallbacks.onRequestCancelled(String.format("%s: %s", getString(R.string.error_unknown_auth_error), t.getMessage()));
                         }
                     }
        );
    }
}
