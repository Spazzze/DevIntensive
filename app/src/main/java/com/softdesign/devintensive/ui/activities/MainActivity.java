package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.MenuRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.NetworkRequest;
import com.softdesign.devintensive.data.network.api.res.EditProfileRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.operations.DBUpdateProfileValuesOperation;
import com.softdesign.devintensive.data.storage.operations.FullUserDataOperation;
import com.softdesign.devintensive.data.storage.viewmodels.NavHeaderViewModel;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.ActivityMainBinding;
import com.softdesign.devintensive.databinding.ItemNavHeaderMainBinding;
import com.softdesign.devintensive.ui.callbacks.BaseNetworkTaskCallbacks;
import com.softdesign.devintensive.ui.callbacks.MainActivityCallback;
import com.softdesign.devintensive.ui.fragments.LikesListFragment;
import com.softdesign.devintensive.ui.fragments.UserListFragment;
import com.softdesign.devintensive.ui.fragments.UserProfileFragment;
import com.softdesign.devintensive.ui.fragments.retain.BaseNetworkFragment;
import com.softdesign.devintensive.ui.fragments.retain.LoadUsersInfoFragment;
import com.softdesign.devintensive.ui.fragments.retain.UpdateServerDataFragment;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import java.io.File;
import java.io.IOException;

import static com.softdesign.devintensive.data.network.NetworkRequest.ID;
import static com.softdesign.devintensive.data.network.NetworkRequest.Status;

@SuppressWarnings({"unused", "deprecation"})
public class MainActivity extends BaseActivity implements MainActivityCallback, BaseNetworkTaskCallbacks,
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private ActivityMainBinding mMainBinding;
    private ItemNavHeaderMainBinding mNavHeaderBinding;
    private NavHeaderViewModel mNavHeaderViewModel;

    private LoadUsersInfoFragment mLoadUsersInfoFragment;
    private UpdateServerDataFragment mDataFragment;
    private UserProfileFragment mMainProfileFragment;
    private UserListFragment mUserListFragment;

    private File mPhotoFile = null;
    private int mIntentId;

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
                if (mNavHeaderViewModel.isEditing()) {
                    showDialogFragment(Const.DIALOG_LOAD_PROFILE_AVATAR);
                }
                break;
        }
    }

    //Back button
    @Override
    public void onBackPressed() {
        if (mMainBinding.navView != null && mMainBinding.drawer.isDrawerOpen(GravityCompat.START)) {
            mMainBinding.drawer.closeDrawer(GravityCompat.START);
        } else if (findMainProfileFragment() != null && mMainProfileFragment.isEditing()) {
            mMainProfileFragment.changeEditMode(false);
        } else {
            super.onBackPressed();
            if (mManager.getBackStackEntryCount() == 0) {
                mMainBinding.navView.getMenu().getItem(0).setChecked(true);
            }
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
                showToast(R.string.notify_not_implemented);
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
    }

    @Override
    public void showProgressDialog() {
        Fragment fragment = getCurrentFragment();
        if (findUserListFragment() == fragment) {
            mUserListFragment.showProgressDialog();
        } else if (fragment instanceof LikesListFragment) {
            ((LikesListFragment) fragment).showProgressDialog();
        } else {
            super.showProgressDialog();
        }
    }

    @Override
    public void hideProgressDialog() {
        if (findUserListFragment() != null) mUserListFragment.hideProgressDialog();
        super.hideProgressDialog();
    }

    private void lockDrawer(boolean b) {
        if (b) mMainBinding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        else mMainBinding.drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public void loadImageFromGallery(int intentId) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeFromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            takeFromGalleryIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(takeFromGalleryIntent, getString(R.string.header_choosePhotoFromGallery)), intentId);
        } else {
            mIntentId = intentId;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Const.REQUEST_PERMISSIONS_READ_SDCARD);
        }
    }

    public void takeSnapshotFromCamera(int intentId) {
        Log.d(TAG, "takeSnapshotFromCamera: " + intentId);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                mPhotoFile = AppUtils.createImageFile();
            } catch (IOException e) {
                showError(getString(R.string.error_cannot_save_file) + e.getMessage());
            }
            if (mPhotoFile != null) {
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, intentId);
            }
        } else {
            mIntentId = intentId;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Const.REQUEST_PERMISSIONS_CAMERA);
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Events

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

    @SuppressWarnings("unused")
    public void onOperationFinished(final DBUpdateProfileValuesOperation.Result result) {  //like response
        if (result.isSuccessful() && result.getOutput() != null) {
            updateProfileValuesFromServer(result.getOutput());
        } else {
            Log.e(TAG, "onOperationFinished: DBUpdateProfileValuesOperation.Result Данные из памяти не были загружены");
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::  Events

    //region :::::::::::::::::::::::::::::::::: Data

    private void loadFullUserData() {
        runOperation(new FullUserDataOperation());
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Activity Results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Const.REQUEST_PERMISSIONS_CAMERA_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (mIntentId != 0) takeSnapshotFromCamera(mIntentId);
                }
                break;
            case Const.REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    if (mIntentId != 0) loadImageFromGallery(mIntentId);
                }
                break;
            case Const.REQUEST_PHOTO_FROM_GALLERY:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (findMainProfileFragment() != null)
                        mMainProfileFragment.getViewModel().setUserPhotoUri(data.getData().toString());
                }
                break;
            case Const.REQUEST_PHOTO_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK && mPhotoFile != null) {
                    if (findMainProfileFragment() != null)
                        mMainProfileFragment.getViewModel().setUserPhotoUri(Uri.fromFile(mPhotoFile).toString());
                }
                break;
            case Const.REQUEST_AVATAR_FROM_GALLERY:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (findMainProfileFragment() != null)
                        mMainProfileFragment.getViewModel().setUserAvatarUri(data.getData().toString());
                    mNavHeaderViewModel.setUserAvatarUri(data.getData().toString());
                }
                break;
            case Const.REQUEST_AVATAR_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK && mPhotoFile != null) {
                    if (findMainProfileFragment() != null)
                        mMainProfileFragment.getViewModel().setUserAvatarUri(Uri.fromFile(mPhotoFile).toString());
                    mNavHeaderViewModel.setUserAvatarUri(Uri.fromFile(mPhotoFile).toString());
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
                    if (mIntentId != 0) takeSnapshotFromCamera(mIntentId);
                } else
                    onPermissionsDenied(permissions, grantResults, Const.REQUEST_PERMISSIONS_CAMERA_SETTINGS);
                break;
            case Const.REQUEST_PERMISSIONS_READ_SDCARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (mIntentId != 0) loadImageFromGallery(mIntentId);
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
    public UserProfileFragment findMainProfileFragment() {
        if (mMainProfileFragment == null)
            mMainProfileFragment = findFragment(UserProfileFragment.class);
        return mMainProfileFragment;
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

    public BaseNetworkFragment findNetworkFragment() {
        for (Fragment f : mManager.getFragments()) {
            if (f instanceof BaseNetworkFragment) return (BaseNetworkFragment) f;
        }
        return null;
    }

    public UserProfileFragment findOtherProfileFragment(String userId) {
        return (UserProfileFragment) mManager.findFragmentByTag(UserProfileFragment.class.getSimpleName() + userId);
    }

    public LikesListFragment findLikesListFragment(String userId) {
        return (LikesListFragment) mManager.findFragmentByTag(LikesListFragment.class.getName() + userId);
    }

    @Override
    public void attachLikesListFragment(Bundle args) {
        String userId;
        if (args == null || (userId = args.getString(Const.PARCELABLE_KEY_USER_ID)) == null) return;

        LikesListFragment likesListFragment = findLikesListFragment(userId);
        if (likesListFragment == null) {
            likesListFragment = new LikesListFragment();
            likesListFragment.setArguments(args);
            replaceFragment(likesListFragment, true, LikesListFragment.class.getName() + userId);
        } else {
            mManager.popBackStack(LikesListFragment.class.getName() + userId, 0);
        }
    }

    @Override
    public void attachOtherUserFragment(Bundle args) {
        String userId;
        if (args == null || (userId = args.getString(Const.PARCELABLE_KEY_USER_ID)) == null) return;

        UserProfileFragment userProfileFragment = findOtherProfileFragment(userId);
        if (userProfileFragment == null) {
            userProfileFragment = new UserProfileFragment();
            userProfileFragment.setArguments(args);
            replaceFragment(userProfileFragment, true, UserProfileFragment.class.getSimpleName() + userId);
        } else {
            mManager.popBackStack(UserProfileFragment.class.getSimpleName() + userId, 0);
        }
    }

    private void attachMainProfileFragment() {
        String authId = DATA_MANAGER.getPreferencesManager().loadBuiltInAuthId();
        if (findMainProfileFragment() == null) {
            mMainProfileFragment = new UserProfileFragment();
            replaceFragment(mMainProfileFragment, false, UserProfileFragment.class.getName());
        } else {
            mManager.popBackStack(UserProfileFragment.class.getName(), 0);
        }
    }

    private void attachUserListFragment() {
        if (findUserListFragment() == null) {
            mUserListFragment = new UserListFragment();
            replaceFragment(mUserListFragment, true, UserListFragment.class.getName());
        } else {
            mManager.popBackStackImmediate(UserListFragment.class.getName(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            replaceFragment(mUserListFragment, true, UserListFragment.class.getName());
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

    private void removeFragment(Fragment fragment) {
        FragmentTransaction transaction = mManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.remove(fragment);
        transaction.commit();
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack, String tag) {
        FragmentTransaction transaction = mManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commit();
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Task Callbacks

    @Override
    public void onNetworkRequestFailed(@NonNull NetworkRequest request) {
        Log.e(TAG, "onNetworkRequestFailed: " + request.getId() + " " + request.getError());
        hideProgressDialog();
        if (request.isErrorCritical()) {         //// TODO: 06.08.2016 обработать критикал ошибки
            showError(request.getError());
            return;
        }
        switch (request.getId()) {
            case LOAD_DB:
                if (findUserListFragment() != null && request.isAnnounceError())
                    mUserListFragment.listLoadingError();
                break;
            default:
                if (request.isAnnounceError()) showError(request.getError());
                break;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onNetworkRequestFinished(@NonNull NetworkRequest request) {
        Log.d(TAG, "onNetworkRequestFinished: " + request.getId());
        hideProgressDialog();
        switch (request.getId()) {
            case LOAD_DB:
                if (findUserListFragment() != null) mUserListFragment.requestDataFromDB(null);
                if (findMainProfileFragment() != null && mMainProfileFragment.isVisible() &&
                        mMainProfileFragment.isAdapterEmptyOrNull()) {
                    new Handler().postDelayed(() -> mMainProfileFragment.updateLikesList(), 1000);
                }
                break;
            case UPLOAD_DATA:
                BaseModel bm = (BaseModel) request.getAdditionalInfo();
                if (bm.getData().getClass().isAssignableFrom(EditProfileRes.class)) {
                    BaseModel<EditProfileRes> res = (BaseModel<EditProfileRes>) bm;
                    if (findMainProfileFragment() != null){
                        mMainProfileFragment.getViewModel().updateValues(new ProfileViewModel(res.getData().getUser()));
                    }
                }
                break;
        }
    }

    @Override
    public void onNetworkRequestStarted(@NonNull NetworkRequest request) {
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Fragments Callbacks

    @Override
    public void setupToolbar(Toolbar toolbar, @MenuRes int id, boolean drawerOpening) {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (drawerOpening) actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.inflateMenu(id);
        }
        lockDrawer(!drawerOpening);
    }

    @Override
    public void openDrawer() {
        mMainBinding.drawer.openDrawer(GravityCompat.START);
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
    public void forceRefreshLikesListFromServer(String userId, boolean isLikedByMe) {
        showProgressDialog();
        likeUser(userId, isLikedByMe);
    }

    @Override
    public void likeUser(String remoteId, boolean liked) {
        if (findLoadUsersInfoFragment() != null) mLoadUsersInfoFragment.likeUser(remoteId, liked);
    }

    @Override
    public boolean isNetworkRequestRunning(ID id) {
        BaseNetworkFragment fragment = findNetworkFragment();
        return fragment != null && fragment.getStatus(id) == Status.RUNNING;
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::: Data Communication to Fragments

    private void updateProfileValuesFromServer(UserEntity output) {
        Fragment fragment = getCurrentFragment();
        if (fragment == null) return;
        String userId = output.getRemoteId();

        if (fragment instanceof LikesListFragment && fragment.getTag().contains(userId)) {
            ((LikesListFragment) fragment).updateLikesList(output);
        } else if (fragment instanceof UserListFragment) {
            ((UserListFragment) fragment).updateUserList(output);
        } else if (fragment instanceof UserProfileFragment &&
                (fragment.getTag().contains(userId) ||
                        (AppUtils.equals(fragment.getTag(), UserProfileFragment.class.getName()) &&
                                AppUtils.equals(userId, DATA_MANAGER.getPreferencesManager().loadBuiltInAuthId())))) {
            ((UserProfileFragment) fragment).updateUserProfile(output);
        }
    }

    //endregion :::::::::::::::::::::::::::::::::: Data Communication to Fragments
}