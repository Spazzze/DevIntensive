package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

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
import com.softdesign.devintensive.utils.ConstantManager;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AuthActivity extends BaseActivity {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Auth Activity";
    private static final String GOOGLE_SCOPES = ConstantManager.G_PLUS_SCOPE + " " + ConstantManager.USER_INFO_SCOPE + " " + ConstantManager.EMAIL_SCOPE;

    @BindView(R.id.auth_screen) FrameLayout mFrameLayout;

    private DataManager mDataManager;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();
        //try silent re-auth with google
        if (mDataManager.getPreferencesManager().getAuthorizationSystem().equals(ConstantManager.AUTH_GOOGLE)) {
            googleSilentSignIn();
        }

        //fb
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        showToast(getString(R.string.notify_auth_by_Facebook));
                        mDataManager.getPreferencesManager().saveAuthorizationSystem(ConstantManager.AUTH_FACEBOOK);
                        AuthActivity.this.finish();
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

    @OnClick({R.id.auth_enter_button, R.id.forgot_pass_button, R.id.login_with_vk_icon,
                     R.id.login_with_fb_icon, R.id.login_with_google_icon})
    void submitAuthButton(View view) {
        switch (view.getId()) {
            case R.id.auth_enter_button:

                break;
            case R.id.forgot_pass_button:
                break;
            case R.id.login_with_vk_icon:
                VKSdk.login(this, VKScope.PHOTOS, VKScope.NOTIFY);
                break;
            case R.id.login_with_fb_icon:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));
                break;
            case R.id.login_with_google_icon:
                googleSignIn();
                break;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //google sign in
        if (requestCode == ConstantManager.REQUEST_GOOGLE_SIGN_IN && resultCode == RESULT_OK) {
            showToast(getString(R.string.notify_auth_by_Google));
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
                    AuthActivity.this.finish();
                }
            };
            getToken.execute(null, null, null);
        } else if //vk sign in
                (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                    @Override
                    public void onResult(VKAccessToken res) {
                        showToast(getString(R.string.notify_auth_by_VK));
                        mDataManager.getPreferencesManager().saveVKAuthorizationInfo(res);
                        AuthActivity.this.finish();
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

    private void googleSignIn() {
        Log.d(TAG, "googleSignIn: ");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    ConstantManager.REQUEST_PERMISSIONS_GET_ACCOUNTS);
            //// TODO: 08.07.2016 лучше бы делать кастомный диалог а не снекбар
            Snackbar.make(mFrameLayout, R.string.notify_google_silent_auth_request_permission, Snackbar.LENGTH_LONG).show();
        }
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"},
                false, null, null, null, null);
        startActivityForResult(intent, ConstantManager.REQUEST_GOOGLE_SIGN_IN);
    }

    /**
     * implemented silent login but "android.permission.GET_ACCOUNTS" is required
     */
    private void googleSilentSignIn() {
        Log.d(TAG, "googleSilentSignIn: ");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED) {
            List<String> currentGoogleAuthData = mDataManager.getPreferencesManager().loadGoogleAuthorizationInfo();
            if (!(currentGoogleAuthData.get(0)).isEmpty() && !(currentGoogleAuthData.get(1)).isEmpty()) {
                Account selectedAccount = new Account(currentGoogleAuthData.get(0), currentGoogleAuthData.get(1));
                Intent intent = AccountPicker.newChooseAccountIntent(selectedAccount, null, new String[]{"com.google"},
                        false, null, null, null, null);
                startActivityForResult(intent, ConstantManager.REQUEST_GOOGLE_SIGN_IN);
            }
        }
    }
}