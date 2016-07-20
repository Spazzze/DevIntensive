package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.ui.fragments.AuthNetworkFragment;
import com.softdesign.devintensive.ui.fragments.LoadUsersIntoDBFragment;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkUtils;
import com.softdesign.devintensive.utils.UiHelper;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthActivity extends BaseActivity implements AuthNetworkFragment.TaskCallbacks {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Auth Activity";

    @BindView(R.id.auth_CoordinatorL) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.auth_email_editText) EditText mEditText_login_email;
    @BindView(R.id.auth_password_editText) EditText mEditText_login_password;
    @BindView(R.id.save_login_checkbox) CheckBox mCheckBox_saveLogin;
    @BindView(R.id.signIn_vk_icon) ImageView mImageView_vk;

    private DataManager mDataManager;
    private FragmentManager mFragmentManager = getFragmentManager();
    private LoadUsersIntoDBFragment mDbNetworkFragment;
    private AuthNetworkFragment mAuthNetworkFragment;

    //region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        attachAuthFragment();
        attachLoadIntoDBFragment();

        mDataManager = DataManager.getInstance();
        loadLoginName();
        mCheckBox_saveLogin.setOnClickListener(this::saveLoginName);
    }
    //endregion

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
    public void onRequestStarted() {
        showProgressDialog();
    }

    @Override
    public void onRequestCompleted() {
        mDbNetworkFragment.downloadUserListIntoDB();
        finishSignIn();
    }

    @Override
    public void onRequestFailed(String error) {
        hideProgressDialog();
        if (!UiHelper.isEmptyOrNull(error)) {
            Log.e(TAG, "onAuthRequestCancelled: " + error);
            errorAnnounce(error);
        }
    }

    @Override
    public void onErrorCount(int count) {
        hideProgressDialog();
        actionDependsOnFailTriesCount(count);
    }
    //endregion

    //region onClick
    private void saveLoginName(View v) {  //// TODO: 19.07.2016 eventbus 
        CheckBox checkBox = (CheckBox) v;
        if (checkBox.isChecked()) {
            mDataManager.getPreferencesManager().saveLoginName(mEditText_login_email.getText().toString());
        } else {
            mDataManager.getPreferencesManager().saveLoginName(null);
        }
    }

    @OnClick({R.id.login_button, R.id.forgot_pass_button, R.id.signIn_vk_icon})
    void submitAuthButton(View view) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
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
    //endregion

    //region Activity Results
    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                showToast(getString(R.string.notify_auth_by_VK));
                mDataManager.getPreferencesManager().saveVKAuthorizationInfo(res);
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
    //endregion

    //region Login methods

    private void startSignIn() {
        mAuthNetworkFragment.signIn(mEditText_login_email.getText().toString(), mEditText_login_password.getText().toString());
        mEditText_login_password.setText("");
    }

    private void forgotPassword() {  //// TODO: 10.07.2016 переделать в отдельную форму
        Intent forgotPassIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.FORGOT_PASS_URL));
        startActivity(forgotPassIntent);
    }

    private void finishSignIn() {
        hideProgressDialog();
        showToast(getString(R.string.notify_auth_successful));
        startActivity(new Intent(AuthActivity.this, MainActivity.class));
        AuthActivity.this.finish();
    }

    //endregion

    //region Ui methods

    private void loadLoginName() {
        if (mDataManager.getPreferencesManager().isLoginNameSavingEnabled()) {
            mCheckBox_saveLogin.setChecked(true);
            mEditText_login_email.setText(mDataManager.getPreferencesManager().loadLoginName());
            mEditText_login_email.setSelection(mEditText_login_email.length());
        }
    }

    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void showSnackBar(int id) {
        Snackbar.make(mCoordinatorLayout, getString(id), Snackbar.LENGTH_LONG).show();
    }

    private void actionDependsOnFailTriesCount(int failsCount) {
        if (failsCount == AppConfig.MAX_LOGIN_TRIES) {
            mEditText_login_email.setText("");
            showError(R.string.error_current_user_data_erased);
        } else if (failsCount < AppConfig.MAX_LOGIN_TRIES) {
            String s = MessageFormat.format("{0}: {1}",
                    getString(R.string.error_tries_before_erase),
                    AppConfig.MAX_LOGIN_TRIES - failsCount);
            showSnackBar(s);
        }
    }

    private void errorAnnounce(String error) {
        mEditText_login_password.setText("");
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(AppConfig.ERROR_VIBRATE_TIME);
        showToast(error);
    }
    //endregion
}