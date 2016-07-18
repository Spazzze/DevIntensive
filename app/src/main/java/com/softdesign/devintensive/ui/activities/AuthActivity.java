package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserAuthRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.ui.fragments.LoadUsersIntoDBFragment;
import com.softdesign.devintensive.ui.fragments.UpdateUserInfoFragment;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ErrorUtils;
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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends BaseActivity implements LoadUsersIntoDBFragment.TaskCallbacks, UpdateUserInfoFragment.TaskCallbacks {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Auth Activity";

    @BindView(R.id.auth_CoordinatorL) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.auth_email_editText) EditText mEditText_login_email;
    @BindView(R.id.auth_password_editText) EditText mEditText_login_password;
    @BindView(R.id.save_login_checkbox) CheckBox mCheckBox_saveLogin;
    @BindView(R.id.signIn_vk_icon) ImageView mImageView_vk;

    private DataManager mDataManager;
    private int mWrongPasswordCount;
    private Boolean mUserDataEmpty;
    private LoadUsersIntoDBFragment dbNetworkFragment;
    private UpdateUserInfoFragment updateUserNetwFragment;

    //region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();
        mUserDataEmpty = mDataManager.getPreferencesManager().isEmpty();

        //region Fragment
        FragmentManager fm = getFragmentManager();
        dbNetworkFragment = (LoadUsersIntoDBFragment) fm.findFragmentByTag(ConstantManager.TAG_USER_LIST_TASK_FRAGMENT);
        updateUserNetwFragment = (UpdateUserInfoFragment) fm.findFragmentByTag(ConstantManager.TAG_USER_UPDATE_TASK_FRAGMENT);

        if (dbNetworkFragment == null) {
            dbNetworkFragment = new LoadUsersIntoDBFragment();
            fm.beginTransaction().add(dbNetworkFragment, ConstantManager.TAG_USER_LIST_TASK_FRAGMENT).commit();
        }
        if (updateUserNetwFragment == null) {
            updateUserNetwFragment = new UpdateUserInfoFragment();
            fm.beginTransaction().add(updateUserNetwFragment, ConstantManager.TAG_USER_UPDATE_TASK_FRAGMENT).commit();
        }
        //endregion

        if (!mUserDataEmpty) {
            loadLoginName();
        }
    }

    private void loadLoginName() {
        if (mDataManager.getPreferencesManager().isLoginNameSavingEnabled()) {
            mCheckBox_saveLogin.setChecked(true);
            mEditText_login_email.setText(mDataManager.getPreferencesManager().loadLoginName());
            mEditText_login_email.setSelection(mEditText_login_email.length());
        }
    }
    //endregion

    //region LoadUsersIntoDBFragment.TaskCallbacks
    @Override
    public void onAuthRequestCancelled(String error) {
        if (!UiHelper.isEmptyOrNull(error)) {
            Log.e(TAG, "onRequestCancelled: " + error);
            showSnackBar(error);
        }
    }

    @Override
    public void onAuthRequestFinished() {
        finishSignIn();
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

    //region onClick
    @OnClick({R.id.login_button, R.id.forgot_pass_button, R.id.signIn_vk_icon})
    void submitAuthButton(View view) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showSnackBar(getString(R.string.error_no_network_connection));
            return;
        }
        switch (view.getId()) {
            case R.id.login_button:
                login();
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

    private void login() {
        showProgressDialog();
        Call<BaseModel<UserAuthRes>> call = mDataManager.loginUser(new UserLoginReq(
                mEditText_login_email.getText().toString(),
                mEditText_login_password.getText().toString()));
        call.enqueue(new Callback<BaseModel<UserAuthRes>>() {
            @Override
            public void onResponse(Call<BaseModel<UserAuthRes>> call, Response<BaseModel<UserAuthRes>> response) {
                if (response.isSuccessful()) {
                    mWrongPasswordCount = 0;
                    onLoginSuccess(response.body());
                } else {
                    hideProgressDialog();
                    switch (response.code()) {
                        case ConstantManager.HTTP_RESPONSE_NOT_FOUND:
                            wrongPasswordAnnounce();
                            if (!mUserDataEmpty) {
                                mWrongPasswordCount++;
                                actionDependsOnFailTriesCount(mWrongPasswordCount);
                            }
                            break;
                        default:
                            ErrorUtils.BackendHttpError error = ErrorUtils.parseHttpError(response);
                            showToast(error.getErrorMessage());
                            break;
                    }
                }
            }

            @Override
            public void onFailure(Call<BaseModel<UserAuthRes>> call, Throwable t) {
                // there is more than just a failing request (like: no internet connection)
                hideProgressDialog();
                if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                    showSnackBar(getString(R.string.error_no_network_connection));
                } else
                    showSnackBar(String.format("%s: %s", getString(R.string.error_unknown_response_error), t.getMessage()));
            }
        });
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

    //region After Success Login
    private void onLoginSuccess(BaseModel<UserAuthRes> userModelRes) {
        if (mCheckBox_saveLogin.isChecked()) {
            mDataManager.getPreferencesManager().saveLoginName(mEditText_login_email.getText().toString());
        } else {
            mEditText_login_email.setText("");
        }
        mEditText_login_password.setText("");
        dbNetworkFragment.downloadUserListIntoDB();
        saveUserAuthData(userModelRes);
        updateUserNetwFragment.updateUserInfo(userModelRes.getData().getUser());
    }

    private void saveUserAuthData(@NonNull BaseModel<UserAuthRes> userModelRes) {
        mDataManager.getPreferencesManager().saveBuiltInAuthInfo(
                userModelRes.getData().getUser().getId(),
                userModelRes.getData().getToken()
        );
    }

    //endregion

    //region Ui methods
    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void showSnackBar(int id) {
        Snackbar.make(mCoordinatorLayout, getString(id), Snackbar.LENGTH_LONG).show();
    }

    private void actionDependsOnFailTriesCount(int failsCount) {
        if (failsCount == AppConfig.MAX_LOGIN_TRIES) {
            mDataManager.getPreferencesManager().totalLogout();
            mEditText_login_email.setText("");
            showSnackBar(getString(R.string.error_current_user_data_erased));
        } else if (failsCount < AppConfig.MAX_LOGIN_TRIES) {
            String s = MessageFormat.format("{0}: {1}",
                    getString(R.string.error_tries_before_erase),
                    AppConfig.MAX_LOGIN_TRIES - mWrongPasswordCount);
            showSnackBar(s);
        }
    }

    private void wrongPasswordAnnounce() {
        mEditText_login_password.setText("");
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(AppConfig.ERROR_VIBRATE_TIME);
        showToast(getString(R.string.error_wrong_credentials));
    }
    //endregion
}