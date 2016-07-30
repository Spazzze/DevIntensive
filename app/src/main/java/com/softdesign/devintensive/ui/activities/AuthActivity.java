package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.storage.operations.UserLoginDataOperation;
import com.softdesign.devintensive.data.storage.viewmodels.AuthViewModel;
import com.softdesign.devintensive.databinding.ActivityAuthBinding;
import com.softdesign.devintensive.ui.fragments.retain.AuthNetworkFragment;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.text.MessageFormat;

public class AuthActivity extends BaseActivity implements AuthNetworkFragment.AuthTaskCallbacks, View.OnClickListener {

    private static final Handler ERROR_STOP_HANDLER = new Handler();

    private final FragmentManager mFragmentManager = getFragmentManager();
    private ActivityAuthBinding mAuthBinding;
    private AuthNetworkFragment mAuthNetworkFragment;
    private final AuthViewModel mAuthViewModel = new AuthViewModel(AuthActivity.this);

    //region :::::::::::::::::::::::::::::::::::::::::: onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mAuthViewModel.updateValues(savedInstanceState.getParcelable(Const.PARCELABLE_KEY_AUTH));
        } else {
            runOperation(new UserLoginDataOperation());
        }

        attachAuthFragment();

        init();
    }

    private void init() {
        mAuthBinding = DataBindingUtil.setContentView(this, R.layout.activity_auth);
        if (mAuthBinding != null) {
            mAuthBinding.setAuth(mAuthViewModel);
            mAuthBinding.signInVkIcon.setOnClickListener(this);
            mAuthBinding.loginButton.setOnClickListener(this);
            mAuthBinding.forgotPassButton.setOnClickListener(this);
        } else throw new IllegalArgumentException("View binging is null");
    }

    @Override
    protected void onSaveInstanceState(@Nullable Bundle outState) {
        if (outState == null) outState = new Bundle();
        outState.putParcelable(Const.PARCELABLE_KEY_AUTH, mAuthViewModel);
        super.onSaveInstanceState(outState);
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Fragments
    private void attachAuthFragment() {
        mAuthNetworkFragment = (AuthNetworkFragment) mFragmentManager.findFragmentByTag(AuthNetworkFragment.class.getName());
        if (mAuthNetworkFragment == null) {
            mAuthNetworkFragment = new AuthNetworkFragment();
            mFragmentManager.beginTransaction().add(mAuthNetworkFragment, AuthNetworkFragment.class.getName()).commit();
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: TaskCallbacks

    @Override
    public void onRequestStarted() {
        showProgressDialog();
    }

    @Override
    public void onRequestFinished() {
        finishSignIn();
    }

    @Override
    public void onRequestFailed(String error) {
        hideProgressDialog();
        if (!AppUtils.isEmptyOrNull(error)) {
            Log.e(TAG, "onAuthRequestCancelled: " + error);
            errorAnnounce(error);
        }
    }

    @Override
    public void onErrorCount(int count) {
        hideProgressDialog();
        actionDependsOnFailTriesCount(count);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: onClick

    @Override
    public void onClick(View view) {
        if (!AppUtils.isNetworkAvailable()) {
            showError(R.string.error_no_network_connection);
            return;
        }
        switch (view.getId()) {
            case R.id.login_button:
                startSignIn();
                break;
            case R.id.forgot_pass_button:
                forgotPassword();
                break;
            case R.id.signIn_vk_icon:
                VKSdk.login(this, VKScope.PHOTOS, VKScope.NOTIFY);
                break;
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Activity Results
    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                showToast(getString(R.string.notify_auth_by_VK));
                DATA_MANAGER.getPreferencesManager().saveVKAuthorizationInfo(res);
            }

            @Override
            public void onError(VKError error) {
                showToast(error.toString());
                // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Background Operation Results
    @SuppressWarnings("unused")
    public void onOperationFinished(final UserLoginDataOperation.Result result) {
        if (result.isSuccessful() && result.getOutput() != null) {
            mAuthViewModel.setSavingLogin(true);
            mAuthViewModel.setLoginName(result.getOutput());
            mAuthBinding.authEmail.setSelection(mAuthViewModel.getLoginName().length());
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Login methods

    private void startSignIn() {
        if (mAuthNetworkFragment != null)
            mAuthNetworkFragment.signIn(mAuthViewModel.getLoginName(), mAuthViewModel.getPassword());
        mAuthViewModel.saveLoginName();
        mAuthViewModel.setPassword("");
    }

    private void forgotPassword() {
        Intent forgotPassIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.FORGOT_PASS_URL));
        startActivity(forgotPassIntent);
    }

    private void finishSignIn() {
        hideProgressDialog();
        showToast(getString(R.string.notify_auth_successful));
        startMainActivity();
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Ui methods

    private void showSnackBar(String message) {
        Snackbar.make(mAuthBinding.coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void actionDependsOnFailTriesCount(int failsCount) {
        if (failsCount == AppConfig.MAX_LOGIN_TRIES) {
            mAuthViewModel.clearData();
            showError(R.string.error_current_user_data_erased);
        } else if (failsCount < AppConfig.MAX_LOGIN_TRIES) {
            String s = MessageFormat.format("{0}: {1}",
                    getString(R.string.error_tries_before_erase),
                    AppConfig.MAX_LOGIN_TRIES - failsCount);
            showSnackBar(s);
        }
    }

    private void errorAnnounce(String error) {
        mAuthViewModel.setWrongPassword(true);
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(AppConfig.ERROR_VIBRATE_TIME);
        showToast(error);
        ERROR_STOP_HANDLER.removeCallbacksAndMessages(null);
        ERROR_STOP_HANDLER.postDelayed(() -> mAuthViewModel.setWrongPassword(false), AppConfig.ERROR_FADE_TIME);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}