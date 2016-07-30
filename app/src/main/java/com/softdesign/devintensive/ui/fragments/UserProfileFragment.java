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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

    private ProfileViewModel mProfileViewModel = null;
    private FragmentProfileBinding mProfileBinding;
    private File mPhotoFile = null;

    //region :::::::::::::::::::::::::::::::::::::::::: onCreate
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mProfileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false);
        return mProfileBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFields(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Life Cycle
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState == null) outState = new Bundle();
        outState.putParcelable(Const.PARCELABLE_KEY_PROFILE, mProfileViewModel);
    }

    @Override
    public void onPause() {
        saveUserData();
        super.onPause();
    }

    @Override
    public void onDetach() {
        BUS.unregister(this);
        super.onDetach();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BUS.registerSticky(this);
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: onClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floating_action_button:
                changeEditMode(!mProfileViewModel.isEditMode());
                break;
            case R.id.placeholder_profilePhoto:
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
                    mCallbacks.showError(R.string.error_email_client_not_configured);
                }
                break;
            case R.id.openVK_img:
                AppUtils.openWebPage(getActivity(), "https://" + mProfileViewModel.getVK());
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mProfileViewModel != null && mProfileViewModel.isAuthorizedUser()) {
                    mCallbacks.openDrawer();
                } else mCallbacks.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: UI

    private void initFields(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mProfileViewModel = savedInstanceState.getParcelable(Const.PARCELABLE_KEY_PROFILE);
        }
        if (mProfileViewModel == null && getArguments() != null) {
            mProfileViewModel = getArguments().getParcelable(Const.PARCELABLE_KEY_PROFILE);
            if (mProfileViewModel == null) {
                mCallbacks.showError(Const.DIALOG_SHOW_ERROR_RETURN_TO_MAIN, R.string.error_cannot_load_user_profile);
                return;
            }
        }
        if (mProfileViewModel == null) {
            loadFullUserData();
        } else {
            setProfileView(mProfileViewModel);
        }
        mProfileBinding.floatingActionButton.setOnClickListener(this);
        mProfileBinding.profilePhotoLayout.placeholderProfilePhoto.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.makeCallImg.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.sendEmailImg.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.openVKImg.setOnClickListener(this);
    }

    private void setProfileView(@NonNull ProfileViewModel model) {
        mProfileViewModel = model;
        mProfileViewModel.setList(false);
        if (mProfileViewModel.isAuthorizedUser()) {
            mCallbacks.setupToolbar(mProfileBinding.toolbar, R.menu.toolbar_menu_main);
        } else {
            mCallbacks.lockDrawer();
            mCallbacks.setupToolbarWithoutNavMenu(mProfileBinding.toolbar, R.menu.toolbar_menu_main);
        }
        if (mProfileBinding.getProfile() == null) {
            mProfileBinding.setProfile(model);
        } else {
            mProfileViewModel.updateValues(model);
        }
    }

    public void loadImageFromGallery(int intentId) {
        if (ContextCompat.checkSelfPermission(CONTEXT, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
        if (ContextCompat.checkSelfPermission(CONTEXT, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(CONTEXT, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
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
        return mProfileViewModel != null && mProfileViewModel.isEditMode();
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

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Save && Load/Update
    @SuppressWarnings("unused")
    public void onEvent(ProfileViewModel event) {
        Log.d(TAG, "onEvent: ");
        if (event != null && mProfileViewModel == null) {
            mProfileViewModel = event;
            BUS.removeStickyEvent(ProfileViewModel.class);
        }
    }

    @SuppressWarnings("unused")
    public void onOperationFinished(final FullUserDataOperation.Result result) {
        if (result.isSuccessful()) {
            if (result.getOutput() != null) { //only Loading
                setProfileView(result.getOutput());
            } else {
                if (mProfileViewModel == null) mCallbacks.logout(0);
            }
        } else {
            Log.e(TAG, "onOperationFinished: Данные из памяти не были загружены");
            if (mProfileViewModel == null) mCallbacks.logout(0);
        }
    }

    private void loadFullUserData() {
        runOperation(new FullUserDataOperation());
    }

    private void saveUserData() {

        if (mProfileViewModel == null || !mProfileViewModel.isAuthorizedUser()) return;

        boolean hasChanges = false;
        Log.d(TAG, "saveUserData: ");

        if (!DATA_MANAGER.getPreferencesManager().loadUserPhoto().equals(mProfileViewModel.getUserPhotoUri())) {
            hasChanges = true;
            mCallbacks.uploadUserPhoto(mProfileViewModel.getUserPhotoUri());   //// TODO: 29.07.2016 в очередь на выполнение
        }
        if (!DATA_MANAGER.getPreferencesManager().loadUserAvatar().equals((mProfileViewModel.getUserAvatarUri()))) {
            hasChanges = true;
            mCallbacks.uploadUserAvatar((mProfileViewModel.getUserAvatarUri()));
        }
        if (isUserDataChanged()) {
            hasChanges = true;
            mCallbacks.uploadUserData(mProfileViewModel);
        }

        if (hasChanges) runOperation(new FullUserDataOperation(mProfileViewModel));
    }

    private boolean isUserDataChanged() {

        User savedUser;
        String jsonSavedUser = DevIntensiveApplication.getSharedPreferences().getString(Const.USER_JSON_OBJ, null);
        if (jsonSavedUser == null) return true;
        savedUser = (User) AppUtils.getObjectFromJson(jsonSavedUser, User.class);

        return !mProfileViewModel.compareUserData(savedUser);
    }

    public void denySaving() {
        mProfileViewModel = null;
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

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

    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
