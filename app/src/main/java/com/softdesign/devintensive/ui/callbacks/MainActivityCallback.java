package com.softdesign.devintensive.ui.callbacks;

import android.os.Bundle;

import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;

public interface MainActivityCallback extends BaseActivityCallback {

    void loadPhotoFromGallery();

    void loadPhotoFromCamera();

    void loadAvatarFromCamera();

    void loadAvatarFromGallery();

    void uploadUserData(ProfileViewModel model);

    void uploadUserPhoto(String uri);

    void uploadUserAvatar(String uri);

    void updateNavViewModel(ProfileViewModel model);

    void forceRefreshUserListFromServer();

    void closeCurrentFragment();

    void attachOtherUserFragment(Bundle args);

    void likeUser(String remoteId, boolean liked);

    void openDrawer();

    void lockDrawer();

    void unlockDrawer();
}
