package com.softdesign.devintensive.ui.fragments.retain;

import android.app.Activity;
import android.os.Bundle;

import com.redmadrobot.chronos.gui.fragment.ChronosSupportFragment;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.ui.callbacks.BaseTaskCallbacks;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import de.greenrobot.event.EventBus;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BaseNetworkFragment extends ChronosSupportFragment {

    public final String TAG = Const.TAG_PREFIX + getClass().getSimpleName();

    public static final EventBus BUS = EventBus.getDefault();
    public static final DataManager DATA_MANAGER = DataManager.getInstance();

    public BaseTaskCallbacks mCallbacks;
    public String mError = null;
    public volatile boolean mCancelled = false;
    public volatile Status mStatus = Status.PENDING;

    public enum Status {
        PENDING,
        RUNNING,
        FINISHED,
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Utils
    public Status getStatus() {
        return mStatus;
    }

    public boolean isCancelled() {
        return mCancelled;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isExecutePossible() {
        if (!AppUtils.isNetworkAvailable()) {
            return false;
        } else if (!DATA_MANAGER.isUserAuthenticated()) {
            this.mStatus = Status.FINISHED;
            mCancelled = true;
            return false;
        }
        return true;
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Fragment Life Cycle
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCallbacks != null && mStatus == Status.FINISHED) {
            if (!mCancelled) {
                mCallbacks.onRequestFinished();
            } else {
                mCallbacks.onRequestFailed(mError);
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
        if (activity instanceof BaseTaskCallbacks) {
            mCallbacks = (BaseTaskCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement BaseTaskCallbacks");
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

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Request status
    public void onRequestStarted() {
        mStatus = Status.RUNNING;
        mCancelled = false;
        synchronized (this) {
            mError = null;
        }
        if (mCallbacks != null) mCallbacks.onRequestStarted();
    }

    public void onRequestResponseEmpty() {
        mStatus = Status.FINISHED;
        mCancelled = true;
        synchronized (this) {
            mError = getString(R.string.error_response_is_empty);
        }
        if (mCallbacks != null) mCallbacks.onRequestFailed(mError);
    }

    public void onRequestHttpError(AppUtils.BackendHttpError error) {
        mStatus = Status.FINISHED;
        mCancelled = true;
        synchronized (this) {
            mError = error.getErrorMessage();
        }
        if (mCallbacks != null) mCallbacks.onRequestFailed(mError);
    }

    public void onRequestFailure(Throwable t) {
        mStatus = Status.FINISHED;
        mCancelled = true;
        synchronized (this) {
            if (t.getMessage() != null) {
                mError = String.format("%s: %s", getString(R.string.error_unknown_response), t.getMessage());
            } else {
                mError = getString(R.string.error_connection_failed);
            }
        }
        if (mCallbacks != null) mCallbacks.onRequestFailed(mError);
    }

    public void onRequestComplete(Response response) {
        mStatus = Status.FINISHED;
        if (mCallbacks != null) mCallbacks.onRequestFinished();
    }
//endregion ::::::::::::::::::::::::::::::::::::::::::

    public class NetworkCallback<T> implements Callback<T> {

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (response.isSuccessful()) {
                if (AppUtils.isEmptyOrNull(response.body())) {
                    onRequestResponseEmpty();
                } else {
                    onRequestComplete(response);
                }
            } else {
                onRequestHttpError(AppUtils.parseHttpError(response));
            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            onRequestFailure(t);
        }
    }
}
