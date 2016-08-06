package com.softdesign.devintensive.ui.callbacks;

import android.os.Bundle;
import android.support.annotation.MenuRes;
import android.support.v7.widget.Toolbar;

import com.softdesign.devintensive.data.network.NetworkRequest;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;

public interface MainActivityCallback extends BaseActivityCallback {

    void loadImageFromGallery(int intentId);

    void takeSnapshotFromCamera(int intentId);

    void uploadUserData(ProfileViewModel model);

    void uploadUserPhoto(String uri);

    void uploadUserAvatar(String uri);

    void updateNavViewModel(ProfileViewModel model);

    void forceRefreshUserListFromServer();

    void likeUser(String remoteId, boolean liked);

    void attachOtherUserFragment(Bundle args);

    void attachLikesListFragment(Bundle b);

    boolean isNetworkRequestRunning(NetworkRequest.ID id);

    void setupToolbar(Toolbar toolbar, @MenuRes int id, boolean drawerOpening);

    void openDrawer();

    void forceRefreshLikesListFromServer(String userId, boolean isLikedByMe);

    void showDialogFragment(int dialogId);
}
