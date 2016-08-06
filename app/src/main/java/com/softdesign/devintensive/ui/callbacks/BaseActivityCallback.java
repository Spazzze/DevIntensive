package com.softdesign.devintensive.ui.callbacks;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

public interface BaseActivityCallback {

    String getCurrentFragmentTag();

    Fragment getCurrentFragment();

    void showProgressDialog();

    void hideProgressDialog();

    void showError(@StringRes int messageId);

    void showError(String message);

    void showToast(String message);

    void errorAlertExitToMain(String error);

    void errorAlertExitToAuth(String error);

    void logout(int mode);

    void startAuthActivity();

    void startMainActivity();

    void openAppSettings();

    void openAppSettingsForResult(int flag);

    void onBackPressed();
}
