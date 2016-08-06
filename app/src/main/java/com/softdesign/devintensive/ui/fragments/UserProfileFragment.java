package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.operations.DBSelectOperation;
import com.softdesign.devintensive.data.storage.operations.FullUserDataOperation;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.FragmentProfileBinding;
import com.softdesign.devintensive.ui.adapters.RecyclerBindingAdapter;
import com.softdesign.devintensive.ui.view.behaviors.Animations;
import com.softdesign.devintensive.ui.view.elements.CustomGridLayoutManager;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.softdesign.devintensive.data.network.NetworkRequest.Status;
import static com.softdesign.devintensive.utils.AppUtils.isEmptyOrNull;

@SuppressWarnings("unchecked")
public class UserProfileFragment extends BaseViewFragment implements View.OnClickListener {

    private static final int FOOTER_ITEMS_COUNT = 7; //// TODO: 03.08.2016 высчитывать и менять.

    private ProfileViewModel mProfileViewModel = null;
    private FragmentProfileBinding mProfileBinding;
    private String mUserId;
    private File mPhotoFile = null;

    private Status mRequestDataFromDBStatus = Status.PENDING;
    private Status mReqListFromNetworkStatus = Status.PENDING;

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
        loadLikesList(savedInstanceState);
        initRecycleView();
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
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        if (adapter == null) return;
        outState.putParcelableArrayList(Const.PARCELABLE_KEY_LIKES,
                (ArrayList<? extends Parcelable>) adapter.getItems());
    }

    @Override
    public void onPause() {
        saveUserData();
        super.onPause();
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: onClick

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.likes_RV:
                onLikesListClick(0);
                break;
            case R.id.floating_action_button:
                if (mProfileViewModel.isAuthorizedUser()) {
                    changeEditMode(!mProfileViewModel.isEditMode());
                }
                break;
            case R.id.btn_like:
                onLikeClick();
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
                AppUtils.openWebPage(getActivity(), mProfileViewModel.getVK());
                break;
        }
    }

    private void onLikeClick() {
        boolean isLiked = !mProfileViewModel.isLiked();

        mCallbacks.likeUser(mProfileViewModel.getRemoteId(), isLiked);

        mProfileViewModel.changeLiked(!isLiked);

        Animations.animateLikeButton(
                mProfileBinding.mainProfileLayout.buttonLikesLayout.btnLikeImgL,
                mProfileBinding.mainProfileLayout.buttonLikesLayout.btnLikeImgR,
                !isLiked);
    }

    private void onLikesListClick(int pos) {
        Log.d(TAG, "onLikesListClick: ");
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        if (adapter == null) return;
        Bundle b = new Bundle();
        b.putString(Const.PARCELABLE_KEY_USER_ID, mUserId);
        b.putParcelableArrayList(Const.PARCELABLE_KEY_LIKES_MODEL,
                (ArrayList<? extends Parcelable>) adapter.getItems());
        b.putStringArrayList(Const.PARCELABLE_KEY_LIKES, new ArrayList<>(mProfileViewModel.getLikesBy()));
        mCallbacks.attachLikesListFragment(b);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: UI
    private void setProfileView(@NonNull ProfileViewModel model) {
        if (mProfileBinding.getProfile() == null) {
            mUserId = model.getRemoteId();
            mProfileViewModel = model;
            mProfileViewModel.setList(false);
            mCallbacks.setupToolbar(mProfileBinding.toolbar, R.menu.toolbar_menu_main, mProfileViewModel.isAuthorizedUser());
            mProfileBinding.setProfile(model);
        } else {
            mProfileViewModel.updateValues(model);
        }
        if (mReqListFromNetworkStatus != Status.RUNNING) {
            updateLikesList();
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
/*        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (displayMetrics.densityDpi < DisplayMetrics.DENSITY_XXHIGH) {
            mProfileBinding.appBarLayout.setExpanded(false, true);
        }*/
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Events
    @SuppressWarnings("unused")
    public void onOperationFinished(final FullUserDataOperation.Result result) {
        mReqListFromNetworkStatus = Status.FINISHED;
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

    @SuppressWarnings("unused")
    public void onOperationFinished(final DBSelectOperation.Result result) {
        mRequestDataFromDBStatus = Status.FINISHED;
        if (result.isSuccessful()) {
            updateUserListAdapter(result.getOutput());
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::   Events

    //region :::::::::::::::::::::::::::::::::::::::::: Data
    private void initFields(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mProfileViewModel = savedInstanceState.getParcelable(Const.PARCELABLE_KEY_PROFILE);
        }
        if (mProfileViewModel == null && getArguments() != null) {
            mUserId = getArguments().getParcelable(Const.PARCELABLE_KEY_USER_ID);
            mProfileViewModel = getArguments().getParcelable(Const.PARCELABLE_KEY_PROFILE);
            if (mProfileViewModel == null && isEmptyOrNull(mUserId)) {
                mCallbacks.errorAlertExitToMain(getString(R.string.error_cannot_load_user_profile));
                return;
            }  //// TODO: 04.08.2016 else if !isEmptyOrNull(mUserId) запросить инфо с сервера
        }
        if (mProfileViewModel == null) {
            loadFullUserData();
        } else {
            setProfileView(mProfileViewModel);
        }
        mProfileBinding.floatingActionButton.setOnClickListener(this);
        mProfileBinding.profilePhotoLayout.placeholderProfilePhoto.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.btnLike.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.makeCallImg.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.sendEmailImg.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.openVKImg.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.likesRV.setOnClickListener(this);
    }

    private void loadLikesList(Bundle savedInstanceState) {
        Log.d(TAG, "loadLikesList: ");
        List<ProfileViewModel> savedList = null;
        if (savedInstanceState != null) {
            savedList = savedInstanceState.getParcelableArrayList(Const.PARCELABLE_KEY_LIKES);
        }
        if (savedList != null) {
            if (savedList.size() > 0) {
                RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
                if (adapter != null) adapter.setUsersFromDB(savedList);
            }
        } else if (mRequestDataFromDBStatus != Status.RUNNING) {
            updateLikesList();
        }
    }

    private void requestLikeListFromDB(List<String> list) {
        Log.d(TAG, "requestLikeListFromDB: ");
        mRequestDataFromDBStatus = Status.RUNNING;
        runOperation(new DBSelectOperation(list));
    }

    private void loadFullUserData() {
        runOperation(new FullUserDataOperation());
    }

    private void saveUserData() {

        if (mProfileViewModel == null || !mProfileViewModel.isAuthorizedUser()) return;

        boolean hasChanges = false;
        Log.d(TAG, "saveUserData: ");

        String savedPhoto = DATA_MANAGER.getPreferencesManager().loadUserPhoto();
        if (!AppUtils.isEmptyOrNull(savedPhoto) && !AppUtils.equals(savedPhoto, mProfileViewModel.getUserPhotoUri())) {
            hasChanges = true;
            mCallbacks.uploadUserPhoto(mProfileViewModel.getUserPhotoUri());   //// TODO: 29.07.2016 в очередь на выполнение
        }
        String savedAvatar = DATA_MANAGER.getPreferencesManager().loadUserAvatar();
        if (!AppUtils.isEmptyOrNull(savedAvatar) && !AppUtils.equals(savedAvatar, mProfileViewModel.getUserAvatarUri())) {
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
    //endregion ::::::::::::::::::::::::::::::::::::::::::  Data

    //region :::::::::::::::::::::::::::::::::::::::::: Activity Results
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ____ " + requestCode + " " + resultCode + " " + data);
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

    //region :::::::::::::::::::::::::::::::::::::::::: RecyclerView
    private RecyclerBindingAdapter<ProfileViewModel> getUsersAdapter() {
        if (mProfileBinding == null || mProfileBinding.mainProfileLayout.likesRV == null) {
            return null;
        } else
            return (RecyclerBindingAdapter<ProfileViewModel>) mProfileBinding.mainProfileLayout.likesRV.getAdapter();
    }

    private void initRecycleView() {
        RecyclerBindingAdapter<ProfileViewModel> bindingAdapter = new RecyclerBindingAdapter<>(
                R.layout.item_likes_footer,
                BR.profile,
                new ArrayList<>(),
                this::onLikesListClick);
        RecyclerView.ItemAnimator animator = mProfileBinding.mainProfileLayout.likesRV.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        CustomGridLayoutManager glm = new CustomGridLayoutManager(getActivity(), FOOTER_ITEMS_COUNT);
        glm.setScrollEnabled(false);
        mProfileBinding.mainProfileLayout.likesRV.setLayoutManager(glm);
        mProfileBinding.mainProfileLayout.likesRV.swapAdapter(bindingAdapter, false);
    }

    private void updateUserListAdapter(List<UserEntity> userEntities) {
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        if (adapter != null) adapter.setUsersFromDB(new ArrayList<ProfileViewModel>() {{
            for (UserEntity u : userEntities) {
                add(new ProfileViewModel(u));
            }
        }});
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::  RecyclerView

    //region :::::::::::::::::::::::::::::::::::::::::: Communication with Activity
    public ProfileViewModel getViewModel() {
        return mProfileViewModel;
    }

    public void updateLikesList() {
        Log.d(TAG, "updateLikesList: ");
        if (mProfileViewModel != null)
            requestLikeListFromDB(new ArrayList<>(mProfileViewModel.getLikesBy()));
    }

    public boolean isAdapterEmptyOrNull() {
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        return adapter == null || adapter.getItems().size() == 0;
    }

    public void updateUserProfile(UserEntity u) {
        this.mUserId = u.getRemoteId();
        ProfileViewModel updated = new ProfileViewModel(u);
        mProfileViewModel.updateProfileValues(updated);
        requestLikeListFromDB(u.getLikesList());
    }

    public void denySaving() {
        mProfileViewModel = null;
    }
    //endregion :::::::::::::::::::::::::::::::::::::::::: Communication with Activity
}
