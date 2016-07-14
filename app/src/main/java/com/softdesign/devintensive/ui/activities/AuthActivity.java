package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.api.req.UserLoginReq;
import com.softdesign.devintensive.data.network.api.res.UserAuthRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.ui.adapters.PicassoTargetByName;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ErrorUtils;
import com.softdesign.devintensive.utils.NetworkUtils;
import com.squareup.picasso.Picasso;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.softdesign.devintensive.utils.UiHelper.getScreenWidth;

public class AuthActivity extends BaseActivity {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Auth Activity";
    private static final String GOOGLE_SCOPES = ConstantManager.G_PLUS_SCOPE + " " + ConstantManager.USER_INFO_SCOPE + " " + ConstantManager.EMAIL_SCOPE;

    @BindView(R.id.auth_CoordinatorL) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.auth_email_editText) EditText mEditText_login_email;
    @BindView(R.id.auth_password_editText) EditText mEditText_login_password;
    @BindView(R.id.save_login_checkbox) CheckBox mCheckBox_saveLogin;
    @BindView(R.id.signIn_vk_icon) ImageView mImageView_vk;

    private DataManager mDataManager;
    private CallbackManager mCallbackManager;
    private int mWrongPasswordCount;
    private Boolean mUserDataEmpty;

    //region onCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();
        mUserDataEmpty = mDataManager.getPreferencesManager().isEmpty();
        if (!mUserDataEmpty && mDataManager.getPreferencesManager().isLoginNameSavingEnabled()) {
            Log.d(TAG, "onCreate: " + mUserDataEmpty);
            mCheckBox_saveLogin.setChecked(true);
            mEditText_login_email.setText(mDataManager.getPreferencesManager().loadLoginName());
            mEditText_login_email.setSelection(mEditText_login_email.length());
            //try silent login
            if (NetworkUtils.isNetworkAvailable(this)) {
                silentLogin();
            }
        }

        //fb
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        showToast(getString(R.string.notify_auth_by_Facebook));
                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });
    }
    //endregion

    //region onClick
    @OnClick({R.id.login_button, R.id.forgot_pass_button, R.id.signIn_vk_icon,
                     R.id.singIn_fb_icon, R.id.signIn_google_icon})
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
            case R.id.singIn_fb_icon:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
                break;
            case R.id.signIn_google_icon:
                googleSignIn();
                break;
        }
    }
    //endregion

    //region Activity Results
    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //google sign in
        if (requestCode == ConstantManager.REQUEST_GOOGLE_SIGN_IN && resultCode == RESULT_OK) {
            final String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            final String accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
            AsyncTask<Void, Void, String> getToken = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String token = "";
                    try {
                        token = GoogleAuthUtil.getToken(AuthActivity.this, accountName,
                                GOOGLE_SCOPES);
                    } catch (UserRecoverableAuthException userAuthEx) {
                        startActivityForResult(userAuthEx.getIntent(), ConstantManager.REQUEST_GOOGLE_SIGN_IN);
                    } catch (IOException ioEx) {
                        Log.e(TAG, "IOException");
                    } catch (GoogleAuthException fatalAuthEx) {
                        Log.e(TAG, "Fatal Authorization Exception" + fatalAuthEx.getLocalizedMessage());
                    }
                    return token;
                }

                @Override
                protected void onPostExecute(String token) {
                    mDataManager.getPreferencesManager().saveGoogleAuthorizationInfo(accountName, accountType, token);
                    showToast(getString(R.string.notify_auth_by_Google));
                }
            };
            getToken.execute(null, null, null);
        } else if //vk sign in
                (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                    @Override
                    public void onResult(VKAccessToken res) {
                        showToast(getString(R.string.notify_auth_by_VK));
                        mDataManager.getPreferencesManager().saveVKAuthorizationInfo(res);
                    }

                    @Override
                    public void onError(VKError error) {
                        // Произошла ошибка авторизации (например, пользователь запретил авторизацию)
                    }
                })) {
            //facebook sign in
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
                            showToast(error.getErrMessage());
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
                    showSnackBar(String.format("%s: %s", getString(R.string.error_unknown_auth_error), t.getMessage()));
            }
        });
    }

    private void silentLogin() {
        Log.d(TAG, "silentLogin: ");

        String userId = mDataManager.getPreferencesManager().loadBuiltInAuthId();

        if (userId == null || userId.isEmpty()) return;

        showProgressDialog();

        Call<BaseModel<User>> call = mDataManager.getUserData(userId);

        call.enqueue(new Callback<BaseModel<User>>() {
            @Override
            public void onResponse(Call<BaseModel<User>> call,
                                   Response<BaseModel<User>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "onResponse: " + response.body().getData().getPublicInfo().getAvatar());
                    onSilentLoginSuccess(response.body());
                } else {
                    hideProgressDialog();
                }
            }

            @Override
            public void onFailure(Call<BaseModel<User>> call, Throwable t) {
                hideProgressDialog();
                showSnackBar(String.format("%s: %s", getString(R.string.error_unknown_auth_error), t.getMessage()));
                Log.d(TAG, "onFailure: " + String.format("%s: %s", getString(R.string.error_unknown_auth_error), t.getMessage()));
            }
        });
    }

    private void forgotPassword() {  //// TODO: 10.07.2016 переделать в отдельную форму
        Intent forgotPassIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.FORGOT_PASS_URL));
        startActivity(forgotPassIntent);
    }

    private void googleSignIn() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    ConstantManager.REQUEST_PERMISSIONS_GET_ACCOUNTS);
            //// TODO: 08.07.2016 лучше бы делать кастомный диалог а не снекбар
            showSnackBar(getString(R.string.notify_google_silent_auth_request_permission));
        }
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                false, null, null, null, null);
        startActivityForResult(intent, ConstantManager.REQUEST_GOOGLE_SIGN_IN);
    }

    //endregion

    //region Ui methods
    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
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

    //region Other functional methods
    private void onLoginSuccess(BaseModel<UserAuthRes> userModelRes) {
        Log.d(TAG, "onLoginSuccess: ");
        if (mCheckBox_saveLogin.isChecked()) {
            mDataManager.getPreferencesManager().saveLoginName(mEditText_login_email.getText().toString());
        } else {
            mEditText_login_email.setText("");
        }
        mEditText_login_password.setText("");
        saveUserInfoFromServer(userModelRes);
    }

    private void onSilentLoginSuccess(BaseModel<User> res) {
        Log.d(TAG, "onSilentLoginSuccess: ");
        mEditText_login_password.setText("");
        User user = res.getData();
        Log.d(TAG, "onSilentLoginSuccess: " + user.getFirstName());
        Log.d(TAG, "onSilentLoginSuccess: " + (user.getPublicInfo() == null));
        mDataManager.getPreferencesManager().saveAllUserData(user);
        saveUserPhotosFromServer(user);
    }

    private void saveUserInfoFromServer(@NonNull BaseModel<UserAuthRes> userModelRes) {

        saveUserAuthData(userModelRes);
        User user = userModelRes.getData().getUser();
        mDataManager.getPreferencesManager().saveAllUserData(user);
        saveUserPhotosFromServer(user);
    }

    private void saveUserAuthData(@NonNull BaseModel<UserAuthRes> userModelRes) {
        mDataManager.getPreferencesManager().saveBuiltInAuthInfo(
                userModelRes.getData().getUser().getId(),
                userModelRes.getData().getToken()
        );
    }

    private void saveUserPhotosFromServer(@NonNull User user) {

        String pathToAvatar = user.getPublicInfo().getAvatar();
        String pathToPhoto = user.getPublicInfo().getPhoto();

        PicassoTargetByName avatarTarget = new PicassoTargetByName("avatar");
        PicassoTargetByName photoTarget = new PicassoTargetByName("photo") {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                super.onBitmapLoaded(bitmap, from);
                hideProgressDialog();
                showToast(getString(R.string.notify_auth_successful));
                startActivity(new Intent(AuthActivity.this, MainActivity.class));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                hideProgressDialog();
                showToast(getString(R.string.error_connection_failed));
            }
        };
        mImageView_vk.setTag(photoTarget);

        int photoWidth = getScreenWidth();
        int photoHeight = (int) (photoWidth / ConstantManager.ASPECT_RATIO_3_2);

        Picasso.with(this)
                .load(Uri.parse(pathToPhoto))
                .resize(photoWidth,
                        photoHeight)
                .centerCrop()
                .into(photoTarget);

        Picasso.with(this)
                .load(Uri.parse(pathToAvatar))
                .resize(getResources().getDimensionPixelSize(R.dimen.size_medium_64),
                        getResources().getDimensionPixelSize(R.dimen.size_medium_64))
                .centerCrop()
                .into(avatarTarget);

        mDataManager.getPreferencesManager().saveUserPhoto(Uri.fromFile(photoTarget.getFile()));
        mDataManager.getPreferencesManager().saveUserAvatar(avatarTarget.getFile().getAbsolutePath());
    }
    //endregion
}