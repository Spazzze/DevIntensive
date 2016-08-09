package com.softdesign.devintensive.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.redmadrobot.chronos.gui.activity.ChronosAppCompatActivity;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.operations.BaseChronosOperation;
import com.softdesign.devintensive.data.storage.operations.DatabaseOperation;
import com.softdesign.devintensive.data.storage.operations.FullUserDataOperation;
import com.softdesign.devintensive.ui.callbacks.BaseActivityCallback;
import com.softdesign.devintensive.ui.fragments.DialogsFragment;
import com.softdesign.devintensive.ui.fragments.UserProfileFragment;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import de.greenrobot.event.EventBus;

@SuppressWarnings("unused")
public class BaseActivity extends ChronosAppCompatActivity implements BaseActivityCallback {

    public final String TAG = Const.TAG_PREFIX + getClass().getSimpleName();

    public static final DataManager DATA_MANAGER = DataManager.getInstance();
    public static final EventBus BUS = EventBus.getDefault();
    public final FragmentManager mManager = getSupportFragmentManager();
    private ProgressDialog mProgressDialog;

    //region :::::::::::::::::::::::::::::::::: Activity's LifeCycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        DevIntensiveApplication.setCurrentActivity(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onSaveInstanceState(@Nullable Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
    }

    //endregion

    //region :::::::::::::::::::::::::::::::::: Announce
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this, R.style.custom_dialog);
            mProgressDialog.setCancelable(false);
            mProgressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        mProgressDialog.show();
        mProgressDialog.setContentView(R.layout.item_progress_splash);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void showError(@StringRes int messageId) {
        try {
            showDialogFragment(Const.DIALOG_SHOW_ERROR, getString(messageId));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showError(String message) {
        showDialogFragment(Const.DIALOG_SHOW_ERROR, message);
    }

    public void showToast(@StringRes int messageId) {
        Toast.makeText(this, getString(messageId), Toast.LENGTH_LONG).show();
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void showDialogFragment(int dialogId) {
        DialogFragment dialog = DialogsFragment.newInstance(dialogId);
        FragmentTransaction ft = mManager.beginTransaction();
        ft.add(dialog, dialog.getClass().toString() + dialogId);
        ft.commitAllowingStateLoss();
    }

    public void showDialogFragment(int dialogId, String message) {
        DialogFragment dialog = DialogsFragment.newInstance(dialogId, message);
        FragmentTransaction ft = mManager.beginTransaction();
        ft.add(dialog, dialog.getClass().toString() + dialogId);
        ft.commitAllowingStateLoss();
    }

    public void errorAlertExitToMain(String error) {
        showDialogFragment(Const.DIALOG_SHOW_ERROR_RETURN_TO_MAIN, error);
    }

    public void errorAlertExitToAuth(String error) {
        showDialogFragment(Const.DIALOG_SHOW_ERROR_RETURN_TO_AUTH, error);
    }
    //endregion

    //region :::::::::::::::::::::::::::::::::: UTIL
    public <T extends View> T $(@IdRes int id) {
        //noinspection unchecked
        return (T) findViewById(id);
    } //(штырила у Сереги Куприна, на память)

    @Nullable
    public <T extends BaseActivity> T as(Class<T> clazz) {
        //noinspection unchecked
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public <T extends Fragment> T findFragment(Class<T> clazz) {
        return (T) getSupportFragmentManager().findFragmentByTag(clazz.getName());
    }
    //endregion

    //region :::::::::::::::::::::::::::::::::: Callbacks
    public void logout(int mode) {
        Log.d(TAG, "logout: ");
        if (mode == 1) {
            UserProfileFragment mainProfileFragment = findFragment(UserProfileFragment.class);
            if (mainProfileFragment != null) mainProfileFragment.denySaving();
            runOperation(new DatabaseOperation(BaseChronosOperation.Action.CLEAR));
            runOperation(new FullUserDataOperation(BaseChronosOperation.Action.CLEAR));
        } else {
            showToast(R.string.error_ui);
        }
        startAuthActivity();
    }

    public void startAuthActivity() {
        Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void startMainActivity() {
        if (!this.getClass().getName().contains(MainActivity.class.getSimpleName())) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            this.finish();
        } else {
            for (int i = 0; i < mManager.getBackStackEntryCount(); i++) {
                mManager.popBackStackImmediate();
            }
        }
    }

    public void openAppSettings() {
        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
    }

    public void openAppSettingsForResult(int flag) {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, flag);
    }

    public String getCurrentFragmentTag() {
        int count = mManager.getBackStackEntryCount();
        if (count > 0) return mManager.getBackStackEntryAt(count - 1).getName();
        else return null;
    }

    public Fragment getCurrentFragment() {
        String tag = getCurrentFragmentTag();
        if (!AppUtils.isEmptyOrNull(tag)) return mManager.findFragmentByTag(tag);
        else return findFragment(UserProfileFragment.class);
    }

    //endregion
}
