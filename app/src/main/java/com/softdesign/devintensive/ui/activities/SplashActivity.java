package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.ui.fragments.AuthNetworkFragment;
import com.softdesign.devintensive.ui.fragments.LoadUsersIntoDBFragment;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.softdesign.devintensive.utils.NetworkUtils;

public class SplashActivity extends BaseActivity implements LoadUsersIntoDBFragment.TaskCallbacks, AuthNetworkFragment.TaskCallbacks {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Splash Activity";
    private FragmentManager mFragmentManager = getFragmentManager();
    private LoadUsersIntoDBFragment mDbNetworkFragment;
    private AuthNetworkFragment mAuthNetworkFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataManager dataManager = DataManager.getInstance();

        if (!NetworkUtils.isNetworkAvailable(DevIntensiveApplication.getContext()) || !dataManager.isUserAuthenticated())
            startAuthActivity();

        attachAuthFragment();
        attachLoadIntoDBFragment();
    }

    //region Fragments
    private void attachAuthFragment() {
        mAuthNetworkFragment = (AuthNetworkFragment) mFragmentManager.findFragmentByTag(AuthNetworkFragment.class.getName());
        if (mAuthNetworkFragment == null) {
            mAuthNetworkFragment = new AuthNetworkFragment();
            mFragmentManager.beginTransaction().add(mAuthNetworkFragment, AuthNetworkFragment.class.getName()).commit();
        }
    }

    private void attachLoadIntoDBFragment() {
        mDbNetworkFragment = (LoadUsersIntoDBFragment) mFragmentManager.findFragmentByTag(LoadUsersIntoDBFragment.class.getName());
        if (mDbNetworkFragment == null) {
            mDbNetworkFragment = new LoadUsersIntoDBFragment();
            mFragmentManager.beginTransaction().add(mDbNetworkFragment, LoadUsersIntoDBFragment.class.getName()).commit();
        }
    }
    //endregion

    //region TaskCallbacks

    @Override
    public void onAuthRequestCancelled(String error) {
        Log.e(TAG, "onAuthRequestCancelled: " + error);
        startAuthActivity();
    }

    @Override
    public void onAuthRequestStarted() {

    }

    @Override
    public void onAuthRequestFinished() {
        Log.d(TAG, "onAuthRequestFinished: " + getString(R.string.notify_auth_successful));
        mDbNetworkFragment.downloadUserListIntoDB();
        startMainActivity();
    }

    @Override
    public void onAuthRequestFailed(int wrongPasswordCount) {

    }

    @Override
    public void onLoadIntoDBStarted() {
    }

    @Override
    public void onLoadIntoDBCompleted() {
        Log.d(TAG, "onLoadIntoDBCompleted: Запрос по сети и запись в БД выполнены успешно");
    }

    @Override
    public void onLoadIntoDBFailed(String error) {
        Log.d(TAG, "onLoadIntoDBFailed: " + error);
    }
    //endregion

    private void startAuthActivity() {
        startActivity(new Intent(this, AuthActivity.class));
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
