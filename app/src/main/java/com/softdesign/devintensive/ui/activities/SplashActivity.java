package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.NetworkRequest;
import com.softdesign.devintensive.ui.fragments.retain.AuthNetworkFragment;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import java.util.Date;

public class SplashActivity extends AppCompatActivity implements AuthNetworkFragment.AuthNetworkTaskCallbacks {

    private static final String TAG = Const.TAG_PREFIX + "Splash Activity";
    private final FragmentManager mManager = getSupportFragmentManager();
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
    public void onNetworkRequestStarted(@NonNull NetworkRequest request) {

    }

    @Override
    public void onNetworkRequestFinished(@NonNull NetworkRequest request) {
        Log.d(TAG, "onNetworkRequestFinished: ");
        new Handler().postDelayed(this::startMainActivity, AppConfig.SPLASH_FADE_DELAY - (new Date().getTime() - mTime));
    }

    @Override
    public void onNetworkRequestFailed(@NonNull NetworkRequest request) {
        Log.e(TAG, "onNetworkRequestFailed: " + request.getError());
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
