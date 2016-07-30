package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.ui.fragments.retain.AuthNetworkFragment;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.AppUtils;

import java.util.Date;

public class SplashActivity extends AppCompatActivity implements AuthNetworkFragment.AuthTaskCallbacks {

    private static final String TAG = Const.TAG_PREFIX + "Splash Activity";
    private final FragmentManager mManager = getFragmentManager();
    private final long mTime = new Date().getTime();

    //region :::::::::::::::::::::::::::::::::::::::::: onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AppUtils.isNetworkAvailable() ||
                !DataManager.getInstance().isUserAuthenticated()) {
            startAuthActivity();
        } else {
            attachAuthFragment();
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Fragments
    private void attachAuthFragment() {
        AuthNetworkFragment authNetworkFragment = (AuthNetworkFragment) mManager.findFragmentByTag(AuthNetworkFragment.class.getName());
        if (authNetworkFragment == null) {
            authNetworkFragment = new AuthNetworkFragment();
            mManager.beginTransaction().add(authNetworkFragment, AuthNetworkFragment.class.getName()).commit();
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: TaskCallbacks
    @Override
    public void onRequestStarted() {

    }

    @Override
    public void onRequestFinished() {
        Log.d(TAG, "onRequestFinished: ");
        new Handler().postDelayed(this::startMainActivity, AppConfig.SPLASH_FADE_DELAY - (new Date().getTime() - mTime));
    }

    @Override
    public void onRequestFailed(String error) {
        Log.e(TAG, "onRequestFailed: " + error);
        new Handler().postDelayed(this::startAuthActivity, AppConfig.SPLASH_FADE_DELAY - (new Date().getTime() - mTime));
    }

    @Override
    public void onErrorCount(int count) {

    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    private void startAuthActivity() {
        startActivity(new Intent(this, AuthActivity.class));
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
