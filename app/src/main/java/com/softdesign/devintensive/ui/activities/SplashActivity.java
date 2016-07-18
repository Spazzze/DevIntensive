package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.ui.fragments.LoadUsersIntoDBFragment;
import com.softdesign.devintensive.ui.fragments.UpdateUserInfoFragment;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.softdesign.devintensive.utils.NetworkUtils;

public class SplashActivity extends BaseActivity implements LoadUsersIntoDBFragment.TaskCallbacks, UpdateUserInfoFragment.TaskCallbacks {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Splash Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataManager dataManager = DataManager.getInstance();

        if (!NetworkUtils.isNetworkAvailable(DevIntensiveApplication.getContext()) || !dataManager.isUserAuthenticated())
            startAuthActivity();

        //region Fragment
        FragmentManager fm = getFragmentManager();
        LoadUsersIntoDBFragment dbNetworkFragment = (LoadUsersIntoDBFragment) fm.findFragmentByTag(ConstantManager.TAG_USER_LIST_TASK_FRAGMENT);
        UpdateUserInfoFragment updateUserInfoFragment = (UpdateUserInfoFragment) fm.findFragmentByTag(ConstantManager.TAG_USER_UPDATE_TASK_FRAGMENT);

        if (dbNetworkFragment == null) {
            dbNetworkFragment = new LoadUsersIntoDBFragment();
            fm.beginTransaction().add(dbNetworkFragment, ConstantManager.TAG_USER_LIST_TASK_FRAGMENT).commit();
        }
        if (updateUserInfoFragment == null) {
            updateUserInfoFragment = new UpdateUserInfoFragment();
            fm.beginTransaction().add(updateUserInfoFragment, ConstantManager.TAG_USER_UPDATE_TASK_FRAGMENT).commit();
        }
        //endregion
    }

    //region TaskCallbacks

    @Override
    public void onAuthRequestCancelled(String error) {
        Log.e(TAG, "onAuthRequestCancelled: " + error);
        startAuthActivity();
    }

    @Override
    public void onAuthRequestFinished() {
        Log.d(TAG, "onAuthRequestFinished: " + getString(R.string.notify_auth_successful));
        startMainActivity();
    }

    @Override
    public void onRequestStarted() {
    }

    @Override
    public void onRequestFinished() {
        Log.d(TAG, "onRequestFinished: Запрос по сети и запись в БД выполнены успешно");
    }

    @Override
    public void onRequestCancelled(String error) {
        Log.d(TAG, "onRequestCancelled: " + error);
    }
    //endregion

    private void startAuthActivity() {
        startActivity(new Intent(this, AuthActivity.class));
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
