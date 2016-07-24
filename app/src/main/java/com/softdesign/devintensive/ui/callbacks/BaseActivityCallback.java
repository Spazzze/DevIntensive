package com.softdesign.devintensive.ui.callbacks;

public interface BaseActivityCallback {

    void logout(int mode);

    void startAuthActivity();

    void startMainActivity();

    void startUserListActivity();

    void openAppSettings();

    void openAppSettingsForResult(int flag);
}
