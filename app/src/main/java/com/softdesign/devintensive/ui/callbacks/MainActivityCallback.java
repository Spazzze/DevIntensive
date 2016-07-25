package com.softdesign.devintensive.ui.callbacks;

import android.net.Uri;

import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;

public interface MainActivityCallback extends BaseActivityCallback {

    void loadPhotoFromGallery();

    void loadPhotoFromCamera();

    void uploadUserData(ProfileViewModel model);

    void uploadUserPhoto(Uri uri);

    void uploadUserAvatar(String uri);
}
