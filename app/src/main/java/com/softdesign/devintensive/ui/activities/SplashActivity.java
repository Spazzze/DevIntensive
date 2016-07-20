package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.ui.fragments.AuthNetworkFragment;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.softdesign.devintensive.utils.NetworkUtils;

public class SplashActivity extends AppCompatActivity implements AuthNetworkFragment.AuthTaskCallbacks {

    private static final String TAG = Const.TAG_PREFIX + "Splash Activity";
    private FragmentManager mFragmentManager = getFragmentManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataManager dataManager = DataManager.getInstance();

        if (!NetworkUtils.isNetworkAvailable(DevIntensiveApplication.getContext()) || !dataManager.isUserAuthenticated())
            startAuthActivity();

        attachAuthFragment();
    }

    //region Fragments
    private void attachAuthFragment() {
        AuthNetworkFragment authNetworkFragment = (AuthNetworkFragment) mFragmentManager.findFragmentByTag(AuthNetworkFragment.class.getName());
        if (authNetworkFragment == null) {
            authNetworkFragment = new AuthNetworkFragment();
            mFragmentManager.beginTransaction().add(authNetworkFragment, AuthNetworkFragment.class.getName()).commit();
        }
    }
    //endregion

    //region TaskCallbacks
    @Override
    public void onRequestStarted() {

    }

    @Override
    public void onRequestFinished() {
        Log.d(TAG, "onRequestFinished: ");
        startMainActivity();
    }

    @Override
    public void onRequestFailed(String error) {
        Log.e(TAG, "onRequestFailed: " + error);
        startAuthActivity();
    }

    @Override
    public void onErrorCount(int count) {

    }
    //endregion

    private void startAuthActivity() {
        startActivity(new Intent(this, AuthActivity.class));
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
