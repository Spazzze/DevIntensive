package com.softdesign.devintensive.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.operations.FullUserDataOperation;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.FragmentProfileBinding;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.io.File;
import java.io.IOException;

import static com.softdesign.devintensive.utils.AppUtils.createImageFile;

public class UserProfileFragment extends BaseViewFragment implements View.OnClickListener {

    private ProfileViewModel mProfileViewModel;
    private FragmentProfileBinding mProfileBinding;
    private File mPhotoFile = null;

    //region :::::::::::::::::::::::::::::::::::::::::: onCreate
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mProfileViewModel = savedInstanceState.getParcelable(Const.PARCELABLE_KEY_PROFILE);
        }
        mProfileBinding = DataBindingUtil.bind(getView());
        initFields();
        if (mCallbacks != null)
            mCallbacks.setupToolbar(mProfileBinding.toolbar, R.menu.toolbar_menu_main);
    }
    //endregion

    //region :::::::::::::::::::::::::::::::::::::::::: Life Cycle
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (outState == null) outState = new Bundle();
        outState.putParcelable(Const.PARCELABLE_KEY_PROFILE, mProfileViewModel);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        saveUserTextData();
        super.onPause();
    }

    @Override
    public void onDetach() {
        BUS.unregister(this);
        super.onDetach();
    }

    //endregion

    //region :::::::::::::::::::::::::::::::::::::::::: onClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floating_action_button:
                changeEditMode(!mProfileViewModel.isEditMode());
                break;
            case R.id.placeholder_profilePhoto:
                if (mCallbacks != null)
                    mCallbacks.showDialogFragment(Const.DIALOG_LOAD_PROFILE_PHOTO);
                break;
            case R.id.makeCall_img:
                startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.fromParts("tel", mProfileViewModel.getPhone(), null)));
                break;
            case R.id.sendEmail_img:
                Intent sendEmail = new Intent(Intent.ACTION_SENDTO,
                        Uri.fromParts("mailto", mProfileViewModel.getEmail(), null));
                if (AppUtils.queryIntentActivities(getActivity(), sendEmail)) {
                    startActivity(sendEmail);
                } else {
                    if (mCallbacks != null)
                        mCallbacks.showError(R.string.error_email_client_not_configured);
                }
                break;
            case R.id.openVK_img:
                AppUtils.openWebPage(getActivity(), "https://" + mProfileViewModel.getVK());
                break;
        }
    }
    //endregion

    //region :::::::::::::::::::::::::::::::::::::::::: UI

    private void initFields() {
        Log.d(TAG, "initFields: ");
        if (mProfileBinding != null) {
            if (mProfileViewModel != null) mProfileBinding.setProfile(mProfileViewModel);
            else loadFullUserData();
            mProfileBinding.floatingActionButton.setOnClickListener(this);
            mProfileBinding.profilePhotoLayout.placeholderProfilePhoto.setOnClickListener(this);
            mProfileBinding.mainProfileLayout.makeCallImg.setOnClickListener(this);
            mProfileBinding.mainProfileLayout.sendEmailImg.setOnClickListener(this);
            mProfileBinding.mainProfileLayout.openVKImg.setOnClickListener(this);
        } else {
            Log.e(TAG, "initFields: Binding is null");
            if (mCallbacks != null) mCallbacks.logout(1);
        }
    }

    private void setProfileView(@NonNull ProfileViewModel model) {
        if (mProfileViewModel == null) {
            mProfileViewModel = model;
            mProfileBinding.setProfile(mProfileViewModel);
        } else mProfileViewModel.updateValues(model);
    }

    public void loadImageFromGallery(int intentId) {
        if (ContextCompat.checkSelfPermission(APPCONTEXT, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeFromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            takeFromGalleryIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(takeFromGalleryIntent, getString(R.string.header_choosePhotoFromGallery)), intentId);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Const.REQUEST_PERMISSIONS_READ_SDCARD);
        }
    }

    public void takeSnapshotFromCamera(int intentId) {
        if (ContextCompat.checkSelfPermission(APPCONTEXT, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(APPCONTEXT, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
                if (mCallbacks != null)
                    mCallbacks.showError(getString(R.string.error_cannot_save_file) + e.getMessage());
            }
            if (mPhotoFile != null) {
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, intentId);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Const.REQUEST_PERMISSIONS_CAMERA);
        }
    }

    public boolean isEditing() {
        return mProfileViewModel.isEditMode();
    }

    /**
     * enables or disables editing profile info
     *
     * @param mode if true - editing mode will be enabled
     */
    @SuppressWarnings("deprecation")
    public void changeEditMode(boolean mode) {
        mProfileViewModel.setEditMode(mode);
        mCallbacks.updateNavViewModel(mProfileViewModel);
        if (mode) {  //editing
            collapseAppBar();
            mProfileBinding.mainProfileLayout.phoneEditText.requestFocus();
        } else {    //stop edit mode
            saveUserData();
        }
    }

    private void collapseAppBar() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (displayMetrics.densityDpi < DisplayMetrics.DENSITY_XXHIGH) {
            mProfileBinding.appbarLayout.setExpanded(false, true);
        }
    }

    //endregion

    //region :::::::::::::::::::::::::::::::::::::::::: Save && Load/Update
    @SuppressWarnings("unused")
    public void onEvent(ProfileViewModel event) {
        Log.d(TAG, "onEvent: ");
        if (event != null && mProfileViewModel == null) {
            mProfileViewModel = event;
        }
    }

    @SuppressWarnings("unused")
    public void onOperationFinished(final FullUserDataOperation.Result result) {
        if (result.isSuccessful()) {
            if (result.getOutput() != null) { //only Loading
                Log.d(TAG, "onOperationFinished: ");
                setProfileView(result.getOutput());
            } else {
                if (mProfileViewModel == null && mCallbacks != null) mCallbacks.logout(0);
            }
        } else {
            Log.e(TAG, "onOperationFinished: Данные из памяти не были загружены");
            if (mProfileViewModel == null && mCallbacks != null) mCallbacks.logout(0);
        }
    }

    private void loadFullUserData() {
        runOperation(new FullUserDataOperation());
    }

    private void saveUserData() {

        if (!mProfileViewModel.isAuthorizedUser()) return;

        Log.d(TAG, "saveUserData: ");
        if (mCallbacks != null) {
            if (!DATA_MANAGER.getPreferencesManager().loadUserPhoto().equals(mProfileViewModel.getUserPhotoUri())) {
                mCallbacks.uploadUserPhoto(mProfileViewModel.getUserPhotoUri());
            }
            if (!DATA_MANAGER.getPreferencesManager().loadUserAvatar().equals((mProfileViewModel.getUserAvatarUri()))) {
                mCallbacks.uploadUserAvatar((mProfileViewModel.getUserAvatarUri()));
            }
            if (isUserDataChanged()) mCallbacks.uploadUserData(mProfileViewModel);
        }
        saveUserTextData();
    }

    private void saveUserTextData() {
        runOperation(new FullUserDataOperation(mProfileViewModel));
    }

    private boolean isUserDataChanged() {

        User savedUser;
        String jsonSavedUser = DevIntensiveApplication.getSharedPreferences().getString(Const.USER_JSON_OBJ, null);
        if (jsonSavedUser == null) return true;
        savedUser = (User) AppUtils.getObjectFromJson(jsonSavedUser, User.class);

        return !mProfileViewModel.compareUserData(savedUser);
    }
    //endregion

    //region :::::::::::::::::::::::::::::::::::::::::: Activity Results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Const.REQUEST_PHOTO_FROM_GALLERY:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    mProfileViewModel.setUserPhotoUri(data.getData().toString());
                }
                break;
            case Const.REQUEST_PHOTO_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK && mPhotoFile != null) {
                    mProfileViewModel.setUserPhotoUri(Uri.fromFile(mPhotoFile).toString());
                }
                break;
            case Const.REQUEST_AVATAR_FROM_GALLERY:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    mProfileViewModel.setUserAvatarUri(data.getData().toString());
                    mCallbacks.updateNavViewModel(mProfileViewModel);
                }
                break;
            case Const.REQUEST_AVATAR_FROM_CAMERA:
                if (resultCode == Activity.RESULT_OK && mPhotoFile != null) {
                    mProfileViewModel.setUserAvatarUri(Uri.fromFile(mPhotoFile).toString());
                    mCallbacks.updateNavViewModel(mProfileViewModel);
                }
                break;
        }
    }

    //endregion
}
