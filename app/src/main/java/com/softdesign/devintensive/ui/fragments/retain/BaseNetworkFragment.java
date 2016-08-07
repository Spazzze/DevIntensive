package com.softdesign.devintensive.ui.fragments.retain;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.Log;

import com.redmadrobot.chronos.gui.fragment.ChronosSupportFragment;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.NetworkRequest;
import com.softdesign.devintensive.data.network.api.res.BaseResponse;
import com.softdesign.devintensive.ui.callbacks.BaseNetworkTaskCallbacks;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.softdesign.devintensive.data.network.NetworkRequest.ID;
import static com.softdesign.devintensive.data.network.NetworkRequest.Status;

@SuppressWarnings("unused")
public class BaseNetworkFragment extends ChronosSupportFragment {

    public final String TAG = Const.TAG_PREFIX + getClass().getSimpleName();

    public static final DataManager DATA_MANAGER = DataManager.getInstance();

    public static volatile List<NetworkRequest> mNetworkRequests = new ArrayList<>();

    public BaseNetworkTaskCallbacks mCallbacks;

    //region :::::::::::::::::::::::::::::::::::::::::: Utils
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isExecutePossible(ID reqId, Object additionalInfo) {
        if (!isExecutePossible(reqId)) return false;
        NetworkRequest request = findRequest(reqId);
        return !(request != null && request.getStatus() == Status.RUNNING && AppUtils.equals(request.getAdditionalInfo(), additionalInfo));
    }

    public boolean isAuthExecutePossible(ID reqId) {
        if (!AppUtils.isNetworkAvailable()) {
            onRequestCriticalError(R.string.error_connection_failed);
            return false;
        } else if (getStatus(reqId) == Status.RUNNING) {
            return false;
        }
        return true;
    }

    public boolean isExecutePossible(ID reqId) {
        if (!AppUtils.isNetworkAvailable()) {
            if (mCallbacks != null)
                mCallbacks.onNetworkRequestFailed(new NetworkRequest().failed(R.string.error_connection_failed, true, false));
            return false;
        } else if (!DATA_MANAGER.isUserAuthenticated()) {
            onRequestCriticalError(R.string.error_user_not_authenticated);
            return false;
        } else if (getStatus(reqId) == Status.RUNNING) {
            return false;
        }
        return true;
    }

    @NonNull
    public NetworkRequest findOrAddRequest(NetworkRequest request) {
        NetworkRequest req = findRequest(request.getId(), request.getAdditionalInfo());
        if (req != null) return req;
        else mNetworkRequests.add(request);
        return mNetworkRequests.get(mNetworkRequests.size() - 1);
    }

    @NonNull
    public NetworkRequest findOrAddRequest(ID reqId) {
        for (NetworkRequest n : mNetworkRequests) {
            if (n.getId() == reqId)
                return n;
        }
        mNetworkRequests.add(new NetworkRequest(reqId));
        return mNetworkRequests.get(mNetworkRequests.size() - 1);
    }

    @Nullable
    public NetworkRequest findRequest(ID reqId) {
        for (NetworkRequest n : mNetworkRequests) {
            if (n.getId() == reqId)
                return n;
        }
        return null;
    }

    @Nullable
    public NetworkRequest findRequest(ID reqId, Object additionalInfo) {
        for (NetworkRequest n : mNetworkRequests) {
            if (n.getId() == reqId && AppUtils.equals(n.getAdditionalInfo(), additionalInfo))
                return n;
        }
        return null;
    }

    public void removeRequest(@Nullable NetworkRequest request) {
        if (request == null) return;
        NetworkRequest req = findRequest(request.getId(), request.getAdditionalInfo());
        if (req != null) mNetworkRequests.remove(req);
    }

    public void removeRequest(ID reqId) {
        NetworkRequest req = findRequest(reqId);
        if (req != null) mNetworkRequests.remove(req);
    }

    public Status getStatus(ID reqId) {
        NetworkRequest req = findRequest(reqId);
        if (req != null) return req.getStatus();
        else return Status.PENDING;
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Fragment Life Cycle
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mCallbacks == null) return;
        for (NetworkRequest n : mNetworkRequests) {
            if (mCallbacks != null && n.getStatus() == Status.FINISHED) {
                if (!n.isCancelled()) {
                    mCallbacks.onNetworkRequestFinished(n);
                    removeRequest(n);
                } else {
                    mCallbacks.onNetworkRequestFailed(n);
                    removeRequest(n);
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
        if (activity instanceof BaseNetworkTaskCallbacks) {
            mCallbacks = (BaseNetworkTaskCallbacks) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement BaseNetworkTaskCallbacks");
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
    public NetworkRequest onRequestStarted(ID requestId, Object additionalInfo) {
        NetworkRequest request = findOrAddRequest(new NetworkRequest(requestId, additionalInfo));
        request.started();
        if (mCallbacks != null) mCallbacks.onNetworkRequestStarted(request);
        return request;
    }

    public NetworkRequest onRequestStarted(ID requestId) {
        NetworkRequest request = findOrAddRequest(requestId);
        request.started();
        if (mCallbacks != null) mCallbacks.onNetworkRequestStarted(request);
        return request;
    }

    public void onRequestResponseEmpty(@NonNull NetworkRequest request) {
        request.failed(R.string.error_response_is_empty, false, false);
        if (mCallbacks != null) {
            mCallbacks.onNetworkRequestFailed(request);
            removeRequest(request);
        } else {
            findOrAddRequest(request);
        }
    }

    public void onRequestHttpError(@NonNull NetworkRequest request, AppUtils.BackendHttpError error) {
        switch (error.getStatusCode()) {
            case Const.HTTP_FORBIDDEN:
            case Const.HTTP_UNAUTHORIZED:
                request.failed(error.getErrorMessage(), true, true);
                break;
            default:
                request.failed(error.getErrorMessage(), true, false);
                break;
        }

        if (mCallbacks != null) {
            mCallbacks.onNetworkRequestFailed(request);
            removeRequest(request);
        } else {
            findOrAddRequest(request);
        }
    }

    public void onRequestCriticalError(@StringRes int id) {
        if (mCallbacks != null)
            mCallbacks.onNetworkRequestFailed(new NetworkRequest().critical(id));
    }

    public void onRequestFailure(@NonNull NetworkRequest request, Throwable t) {
        if (!AppUtils.isEmptyOrNull(t.getMessage())) {
            request.failed(String.format("%s: %s", getString(R.string.error_unknown_response), t.getMessage()), true, false);
        } else {
            request.failed(R.string.error_connection_failed, true, false);
        }
        if (mCallbacks != null) {
            mCallbacks.onNetworkRequestFailed(request);
            removeRequest(request);
        } else {
            findOrAddRequest(request);
        }
    }

    public void onRequestComplete(@NonNull NetworkRequest request, Response response) {
        request.successful();
        if (mCallbacks != null) {
            mCallbacks.onNetworkRequestFinished(request);
            removeRequest(request);
        } else {
            findOrAddRequest(request);
        }
    }

//endregion ::::::::::::::::::::::::::::::::::::::::::

    public class NetworkCallback<T extends BaseResponse> implements Callback<T> {

        private NetworkRequest mNetworkRequest;

        public NetworkCallback(@NonNull NetworkRequest networkRequest) {
            mNetworkRequest = networkRequest;
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (response.isSuccessful()) {
                if (AppUtils.isEmptyOrNull(response.body())) {
                    onRequestResponseEmpty(mNetworkRequest);
                } else {
                    mNetworkRequest.setAdditionalInfo(response.body());
                    onRequestComplete(mNetworkRequest, response);
                }
            } else {
                onRequestHttpError(mNetworkRequest, AppUtils.parseHttpError(response));
            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            if (call.isCanceled()) {
                Log.e(TAG, "request was cancelled");
            } else
                onRequestFailure(mNetworkRequest, t);
        }
    }

    //debug
    public void printRequestTable() {
        for (NetworkRequest n : mNetworkRequests) {
            Log.d(TAG, "==================================== " + n.getId());
            Log.d(TAG, n.toString());
        }
    }
}
