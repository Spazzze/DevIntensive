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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

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

public class AuthorizationActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = ConstantManager.TAG_PREFIX + "Auth Activity";
    private final String GOOGLE_SCOPES = ConstantManager.G_PLUS_SCOPE + " " + ConstantManager.USER_INFO_SCOPE + " " + ConstantManager.EMAIL_SCOPE;

    @BindView(R.id.auth_enter_button) Button mButton_authenticate;
    @BindView(R.id.forgot_pass_button) TextView mButton_forgot_pass;
    @BindView(R.id.login_with_vk_icon) ImageView mButton_vkLogin;
    @BindView(R.id.login_with_fb_icon) ImageView mButton_fbLogin;
    @BindView(R.id.login_with_google_icon) ImageView mButton_googleLogin;
    @BindView(R.id.auth_screen) FrameLayout mFrameLayout;

    private DataManager mDataManager;
    private CallbackManager mCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autorization_screen);
        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();
        //try silent re-auth with google
        if (mDataManager.getPreferencesManager().getAuthorizationSystem().equals(ConstantManager.AUTH_GOOGLE)) {
            googleSilentSignIn();
        }

        mButton_authenticate.setOnClickListener(this);
        mButton_forgot_pass.setOnClickListener(this);
        mButton_vkLogin.setOnClickListener(this);
        mButton_fbLogin.setOnClickListener(this);
        mButton_googleLogin.setOnClickListener(this);

        //fb
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        showToast("Authorized with Facebook");
                        mDataManager.getPreferencesManager().saveAuthorizationSystem(ConstantManager.AUTH_FACEBOOK);
                        AuthorizationActivity.this.finish();
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

    @Override
    public void onClick(View view) {
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
            showToast("Authorized with Google");
            final String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            final String accountType = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
            AsyncTask<Void, Void, String> getToken = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... params) {
                    String token = "";
                    try {
                        token = GoogleAuthUtil.getToken(AuthorizationActivity.this, accountName,
                                GOOGLE_SCOPES);
                        return token;
                    } catch (UserRecoverableAuthException userAuthEx) {
                        startActivityForResult(userAuthEx.getIntent(), ConstantManager.REQUEST_GOOGLE_SIGN_IN);
                    } catch (IOException ioEx) {
                        Log.d(TAG, "IOException");
                    } catch (GoogleAuthException fatalAuthEx) {
                        Log.d(TAG, "Fatal Authorization Exception" + fatalAuthEx.getLocalizedMessage());
                    }
                    return token;
                }

                @Override
                protected void onPostExecute(String token) {
                    Log.d(TAG, "onPostExecute: " + token);
                    mDataManager.getPreferencesManager().saveGoogleAuthorizationInfo(accountName, accountType, token);
                    AuthorizationActivity.this.finish();
                }
            };
            getToken.execute(null, null, null);
        } else if //vk sign in
                (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
                    @Override
                    public void onResult(VKAccessToken res) {
                        showToast("Authorized with VK");
                        mDataManager.getPreferencesManager().saveVKAuthorizationInfo(res);
                        AuthorizationActivity.this.finish();
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