package com.softdesign.devintensive.ui.callbacks;

import android.support.annotation.NonNull;

import com.softdesign.devintensive.data.network.NetworkRequest;

public interface BaseNetworkTaskCallbacks {

    void onNetworkRequestStarted(@NonNull NetworkRequest request);

    void onNetworkRequestFinished(@NonNull NetworkRequest request);

    void onNetworkRequestFailed(@NonNull NetworkRequest request);
}
