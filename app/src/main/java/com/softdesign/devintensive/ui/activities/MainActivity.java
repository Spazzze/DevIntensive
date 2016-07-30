package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.storage.operations.FullUserDataOperation;
import com.softdesign.devintensive.data.storage.viewmodels.NavHeaderViewModel;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.ActivityMainBinding;
import com.softdesign.devintensive.databinding.ItemNavHeaderMainBinding;
import com.softdesign.devintensive.ui.callbacks.BaseTaskCallbacks;
import com.softdesign.devintensive.ui.callbacks.MainActivityCallback;
import com.softdesign.devintensive.ui.fragments.UserListFragment;
import com.softdesign.devintensive.ui.fragments.UserProfileFragment;
import com.softdesign.devintensive.ui.fragments.retain.LoadUsersInfoFragment;
import com.softdesign.devintensive.ui.fragments.retain.UpdateServerDataFragment;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

public class MainActivity extends BaseActivity implements MainActivityCallback, BaseTaskCallbacks,
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private ActivityMainBinding mMainBinding;
    private ItemNavHeaderMainBinding mNavHeaderBinding;
    private NavHeaderViewModel mNavHeaderViewModel;

    private LoadUsersInfoFragment mLoadUsersInfoFragment;
    private UpdateServerDataFragment mDataFragment;
    private UserProfileFragment mProfileFragment;
    private UserListFragment mUserListFragment;

    //region :::::::::::::::::::::::::::::::::: OnCreate 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mNavHeaderBinding = DataBindingUtil.bind(mMainBinding.navView.getHeaderView(0));

        init(savedInstanceState);

        attachDataFragment();
        attachLoadIntoDBFragment();
        attachMainProfileFragment();
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Life Cycle 
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (outState == null) outState = new Bundle();
        outState.putParcelable(Const.PARCELABLE_KEY_NAV_VIEW, mNavHeaderViewModel);
        super.onSaveInstanceState(outState);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: OnClick

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.avatar_edit:
                if (mNavHeaderViewModel.isEditing())
                    showDialogFragment(Const.DIALOG_LOAD_PROFILE_AVATAR);
                break;
        }
    }

    //Menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_logout:
                logout(1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Back button
    @Override
    public void onBackPressed() {
        if (mMainBinding.navView != null && mMainBinding.drawer.isDrawerOpen(GravityCompat.START)) {
            mMainBinding.drawer.closeDrawer(GravityCompat.START);
        } else if (findProfileFragment() != null && mProfileFragment.isEditing()) {
            mProfileFragment.changeEditMode(false);
        } else {
            unlockDrawer();
            super.onBackPressed();
        }
    }

    //Nav menu
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navMenu_userProfile:
                startMainActivity();
                break;
            case R.id.navMenu_team:
                attachUserListFragment();
                break;
            case R.id.navMenu_options:
                openAppSettings();
                break;
            case R.id.navMenu_logout:
                logout(1);
                break;
            default:
                showToast(item.getTitle().toString());
                break;
        }
        item.setChecked(true);
        mMainBinding.drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: UI

    private void init(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mNavHeaderViewModel = savedInstanceState.getParcelable(Const.PARCELABLE_KEY_NAV_VIEW);
        }
        if (mNavHeaderViewModel == null) {
            loadFullUserData();
        } else {
            mNavHeaderBinding.setNavProfile(mNavHeaderViewModel);
        }
        mMainBinding.navView.setNavigationItemSelectedListener(this);
        mNavHeaderBinding.avatarEdit.setOnClickListener(this);
    }

    public void setNavView(@NonNull ProfileViewModel model) {
        if (mNavHeaderViewModel == null) {
            mNavHeaderViewModel = new NavHeaderViewModel(model);
            mNavHeaderBinding.setNavProfile(mNavHeaderViewModel);
        } else mNavHeaderViewModel.updateValues(model);
    } //// TODO: 30.07.2016 нав меню лочить и нормально прописать для всех фрагментов

    @Override
    public void showProgressDialog() {
        if (findUserListFragment() != null) {
            Log.d(TAG, "showProgressDialog: 1");
            mUserListFragment.showProgressDialog();
        } else {
            Log.d(TAG, "showProgressDialog: 2");
            super.showProgressDialog();
        }
    }

    @Override
    public void hideProgressDialog() {
        if (findUserListFragment() != null) mUserListFragment.hideProgressDialog();
        else super.hideProgressDialog();
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Load data && Events

    private void loadFullUserData() {
        runOperation(new FullUserDataOperation());
    }

    @SuppressWarnings("unused")
    public void onOperationFinished(final FullUserDataOperation.Result result) {
        if (result.isSuccessful()) {
            if (result.getOutput() != null) { //only Loading
                setNavView(result.getOutput());
            } else {
                if (mNavHeaderViewModel == null) logout(0);
            }
        } else {
            Log.e(TAG, "onOperationFinished: Данные из памяти не были загружены");
            if (mNavHeaderViewModel == null) logout(0);
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Activity Results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Const.REQUEST_PERMISSIONS_CAMERA_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromCamera();
                }
                break;
            case Const.REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromGallery();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0) return; //cancelled
        switch (requestCode) {
            case Const.REQUEST_PERMISSIONS_CAMERA:
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromCamera();
                } else
                    onPermissionsDenied(permissions, grantResults, Const.REQUEST_PERMISSIONS_CAMERA_SETTINGS);
                break;
            case Const.REQUEST_PERMISSIONS_READ_SDCARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromGallery();
                } else
                    onPermissionsDenied(permissions, grantResults, Const.REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS);
                break;
        }
    }

    private void onPermissionsDenied(@NonNull String[] permissions, @NonNull int[] grantResults, int permissionFlag) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_DENIED) continue;
            if (!shouldShowRequestPermissionRationale(permissions[i])) {
                AppUtils.showSnackbar(mMainBinding.container, R.string.error_access_permissions_needed,
                        true, R.string.header_allow,
                        (v) -> openAppSettingsForResult(permissionFlag));
            } else {
                switch (permissions[i]) {
                    case Manifest.permission.CAMERA:
                        showError(R.string.error_permission_denied_camera);
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        showError(R.string.error_permission_denied_write_storage);
                        break;
                    case Manifest.permission.READ_EXTERNAL_STORAGE:
                        showError(R.string.error_permission_denied_read_storage);
                        break;
                }
            }
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region  :::::::::::::::::::::::::::::::::: Fragments
    public UserProfileFragment findProfileFragment() {
        if (mProfileFragment == null) mProfileFragment = findFragment(UserProfileFragment.class);
        return mProfileFragment;
    }

    public UpdateServerDataFragment findDataFragment() {
        if (mDataFragment == null) mDataFragment = findFragment(UpdateServerDataFragment.class);
        return mDataFragment;
    }

    public LoadUsersInfoFragment findLoadUsersInfoFragment() {
        if (mLoadUsersInfoFragment == null)
            mLoadUsersInfoFragment = findFragment(LoadUsersInfoFragment.class);
        return mLoadUsersInfoFragment;
    }

    public UserListFragment findUserListFragment() {
        if (mUserListFragment == null) mUserListFragment = findFragment(UserListFragment.class);
        return mUserListFragment;
    }

    public void attachOtherUserFragment(Bundle args) {
        UserProfileFragment userProfileFragment = (UserProfileFragment) mManager.findFragmentByTag(Const.OTHER_USER_PROFILE_KEY);
        if (userProfileFragment == null) {
            userProfileFragment = new UserProfileFragment();
            userProfileFragment.setArguments(args);
            replaceMainFragment(userProfileFragment, true, Const.OTHER_USER_PROFILE_KEY);
        }  else {
            userProfileFragment.setArguments(args);
            mManager.popBackStack(Const.OTHER_USER_PROFILE_KEY, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void attachMainProfileFragment() {
        if (findProfileFragment() == null) {
            mProfileFragment = new UserProfileFragment();
            replaceMainFragment(mProfileFragment, false, UserProfileFragment.class.getName());
        }  else {
            mManager.popBackStack(UserProfileFragment.class.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    private void attachUserListFragment() {
        if (findUserListFragment() == null) {
            mUserListFragment = new UserListFragment();
            replaceMainFragment(mUserListFragment, true, UserListFragment.class.getName());
        } else {
            mManager.popBackStack(UserListFragment.class.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            replaceMainFragment(mUserListFragment, true, UserListFragment.class.getName());
        }
    }

    private void attachDataFragment() {
        if (findDataFragment() == null) {
            mDataFragment = new UpdateServerDataFragment();
            mManager.beginTransaction().add(mDataFragment, UpdateServerDataFragment.class.getName()).commit();
        }
    }

    private void attachLoadIntoDBFragment() {
        if (findLoadUsersInfoFragment() == null) {
            mLoadUsersInfoFragment = new LoadUsersInfoFragment();
            mManager.beginTransaction().add(mLoadUsersInfoFragment, LoadUsersInfoFragment.class.getName()).commit();
        }
    }

    private void replaceMainFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = mManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.replace(R.id.container, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }

    @Override
    public void logout(int mode) {
        if (mode == 1 && findProfileFragment() != null) mProfileFragment.denySaving();
        super.logout(mode);
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Task Callbacks
    @Override
    public void onRequestStarted() {

    }

    @Override
    public void onRequestFinished() {
        Log.d(TAG, "onRequestFinished: ");
        hideProgressDialog();
        if (findUserListFragment() != null) mUserListFragment.requestDataFromDB();
    }

    @Override
    public void onRequestFailed(String error) {
        Log.e(TAG, "onRequestFailed: " + error);
        hideProgressDialog();
        if (findUserListFragment() != null) {
            if (mUserListFragment.getUsersAdapter() == null) {
                showError(Const.DIALOG_SHOW_ERROR_RETURN_TO_MAIN, R.string.error_cannot_load_user_list);
            } else {
                showError(Const.DIALOG_SHOW_ERROR, R.string.error_cannot_load_user_list);
            }
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Fragments Callbacks

    @Override
    public void unlockDrawer() {
        mMainBinding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void lockDrawer() {
        mMainBinding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void openDrawer() {
        mMainBinding.drawer.openDrawer(GravityCompat.START);
    }

    @Override
    public void closeCurrentFragment() {
        mManager.popBackStackImmediate();
    }

    @Override
    public void loadAvatarFromGallery() {
        if (findProfileFragment() != null)
            mProfileFragment.loadImageFromGallery(Const.REQUEST_AVATAR_FROM_GALLERY);
    }

    @Override
    public void loadAvatarFromCamera() {
        if (findProfileFragment() != null)
            mProfileFragment.takeSnapshotFromCamera(Const.REQUEST_AVATAR_FROM_CAMERA);
    }

    @Override
    public void loadPhotoFromGallery() {
        if (findProfileFragment() != null)
            mProfileFragment.loadImageFromGallery(Const.REQUEST_PHOTO_FROM_GALLERY);
    }

    @Override
    public void loadPhotoFromCamera() {
        if (findProfileFragment() != null)
            mProfileFragment.takeSnapshotFromCamera(Const.REQUEST_PHOTO_FROM_CAMERA);
    }

    @Override
    public void uploadUserData(ProfileViewModel model) {
        if (model == null) return;
        mNavHeaderViewModel.updateValues(model);
        if (findDataFragment() != null) mDataFragment.uploadUserData(model);
    }

    @Override
    public void uploadUserPhoto(String uri) {
        if (findDataFragment() != null) mDataFragment.uploadUserPhoto(uri);
    }

    @Override
    public void uploadUserAvatar(String uri) {
        mNavHeaderViewModel.setUserAvatarUri(uri);
        if (findDataFragment() != null) mDataFragment.uploadUserAvatar(uri);
    }

    @Override
    public void updateNavViewModel(ProfileViewModel model) {
        mNavHeaderViewModel.updateValues(model);
    }

    @Override
    public void forceRefreshUserListFromServer() {
        showProgressDialog();
        if (findLoadUsersInfoFragment() != null)
            mLoadUsersInfoFragment.forceRefreshUserListIntoDB();
    }

    @Override
    public void likeUser(String remoteId, boolean liked) {
        if (findLoadUsersInfoFragment() != null) mLoadUsersInfoFragment.likeUser(remoteId, liked);
    }

//endregion ::::::::::::::::::::::::::::::::::::::::::
}