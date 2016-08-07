package com.softdesign.devintensive.ui.callbacks;

import android.support.annotation.Nullable;

import java.util.List;

public interface ListFragmentCallback {

    void listLoadingError();

    void requestDataFromDB(@Nullable List list);

    void hideProgressDialog();

    void showProgressDialog();

    void forceRequestDataFromServer();

    boolean isAdapterEmpty();
}
