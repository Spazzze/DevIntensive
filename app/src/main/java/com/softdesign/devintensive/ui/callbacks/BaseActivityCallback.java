package com.softdesign.devintensive.ui.callbacks;

import android.support.annotation.MenuRes;
import android.support.v7.widget.Toolbar;

public interface BaseActivityCallback {

    void showProgressDialog();

    void hideProgressDialog();

    void showError(int messageId);

    void showError(int dialogId, int messageId);

    void showError(String message);

    void showDialogFragment(int dialogId);

    void showDialogFragment(int dialogId, String message);

    void showToast(String message);

    void logout(int mode);

    void startAuthActivity();

    void startMainActivity();

    void startUserListActivity();

    void openAppSettings();

    void openAppSettingsForResult(int flag);

    void setupToolbar(Toolbar toolbar, @MenuRes int id);
}
