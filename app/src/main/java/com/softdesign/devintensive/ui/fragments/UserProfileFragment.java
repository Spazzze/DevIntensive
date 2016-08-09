package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.softdesign.devintensive.ui.view.elements.CustomGridLayoutManager;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.softdesign.devintensive.data.network.NetworkRequest.Status;
import static com.softdesign.devintensive.ui.view.animations.Animations.animateLikeButton;
import static com.softdesign.devintensive.utils.AppUtils.getScreenWidth;
import static com.softdesign.devintensive.utils.AppUtils.isEmptyOrNull;

@SuppressWarnings("unchecked")
public class UserProfileFragment extends BaseViewFragment implements View.OnClickListener {

    private ProfileViewModel mProfileViewModel = null;
    private FragmentProfileBinding mProfileBinding;
    private String mUserId;
    private File mPhotoFile = null;
    private List<ProfileViewModel> mSavedList = new ArrayList<>();

    private Status mRequestDataFromDBStatus = Status.PENDING;
    private Status mLoading = Status.PENDING;
    private static final Handler ITEM_LOADING_HANDLER = new Handler();

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
        outState.putParcelableArrayList(Const.PARCELABLE_KEY_LIKES,
                (ArrayList<? extends Parcelable>) mSavedList);
    }

    @Override
    public void onPause() {
        saveUserData();
        if (mProfileViewModel != null) mProfileViewModel.setAnimateTextChange(false);
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

        animateLikeButton(
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
        Log.d(TAG, "setProfileView: ");
        if (mProfileBinding.getProfile() == null) {
            mUserId = model.getRemoteId();
            mProfileViewModel = model;
            mProfileViewModel.setList(false);
            mCallbacks.setupToolbar(mProfileBinding.toolbar, R.menu.toolbar_menu_main, mProfileViewModel.isAuthorizedUser());
            mProfileBinding.setProfile(model);
            if (mProfileViewModel.isAuthorizedUser()) mCallbacks.setItemMenuChecked(0);
        } else {
            mProfileViewModel.updateValues(model);
        }
        updateLikesList();
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
            mProfileBinding.mainProfileLayout.phoneEditText.requestFocus();
        } else {    //stop edit mode
            saveUserData();
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Events
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

    @SuppressWarnings("unused")
    public void onOperationFinished(final DBSelectOperation.Result result) {
        mRequestDataFromDBStatus = Status.FINISHED;
        if (result.isSuccessful()) {
            updateAdapterFromDb(result.getOutput());
        } else {
            if (!isEmptyOrNull(mSavedList)) {
                initLoadItemsIntoAdapter();
            }
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::   Events

    //region :::::::::::::::::::::::::::::::::::::::::: Data
    private void initFields(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mProfileViewModel = savedInstanceState.getParcelable(Const.PARCELABLE_KEY_PROFILE);
            mSavedList = savedInstanceState.getParcelableArrayList(Const.PARCELABLE_KEY_LIKES);
        }
        if (mProfileViewModel == null && getArguments() != null) {
            mUserId = getArguments().getParcelable(Const.PARCELABLE_KEY_USER_ID);
            mProfileViewModel = getArguments().getParcelable(Const.PARCELABLE_KEY_PROFILE);
            if (mProfileViewModel == null && isEmptyOrNull(mUserId)) {
                mCallbacks.errorAlertExitToMain(getString(R.string.error_cannot_load_user_profile));
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
        mProfileBinding.mainProfileLayout.btnLike.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.makeCallImg.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.sendEmailImg.setOnClickListener(this);
        mProfileBinding.mainProfileLayout.openVKImg.setOnClickListener(this);
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
        if (!AppUtils.equals(savedPhoto, mProfileViewModel.getUserPhotoUri())) {
            hasChanges = true;
            mCallbacks.uploadUserPhoto(mProfileViewModel.getUserPhotoUri());   //// TODO: 29.07.2016 в очередь на выполнение
        }
        String savedAvatar = DATA_MANAGER.getPreferencesManager().loadUserAvatar();
        if (!AppUtils.equals(savedAvatar, mProfileViewModel.getUserAvatarUri())) {
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
    private void initRecycleView() {
        Log.d(TAG, "initRecycleView: ");
        int footerItemsCount = (getScreenWidth() - getResources().getDimensionPixelSize(R.dimen.profile_likesBox_size)) /
                getResources().getDimensionPixelSize(R.dimen.size_normal_40);
        if (footerItemsCount == 0) footerItemsCount = 7;

        mProfileBinding.mainProfileLayout.likesRV.setItemAnimator(AppConfig.LF_ANIMATION.getAnimator());
        mProfileBinding.mainProfileLayout.likesRV.getItemAnimator().setAddDuration(AppConfig.LF_ANIM_DURATION_ITEM_ADD);
        mProfileBinding.mainProfileLayout.likesRV.getItemAnimator().setRemoveDuration(AppConfig.LF_ANIM_DURATION_ITEM_REMOVE);

        mProfileBinding.mainProfileLayout.likesRV.setLayoutManager(new CustomGridLayoutManager(getActivity(), footerItemsCount, false));

        RecyclerBindingAdapter<ProfileViewModel> bindingAdapter = createAdapter(mSavedList);
        mProfileBinding.mainProfileLayout.likesRV.swapAdapter(bindingAdapter, false);
    }

    private RecyclerBindingAdapter<ProfileViewModel> createAdapter(List<ProfileViewModel> list) {
        return new RecyclerBindingAdapter<>(
                R.layout.item_likes_footer,
                BR.profile,
                list,
                this::onLikesListClick);
    }

    private void initLoadItemsIntoAdapter() {
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        if (adapter != null && mLoading != Status.RUNNING)
            loadItemsIntoAdapter(adapter, mSavedList, true);
    }

    private void loadItemsIntoAdapter(@NonNull RecyclerBindingAdapter<ProfileViewModel> adapter,
                                      List<ProfileViewModel> savedList, boolean animated) {
        if (savedList != null && mLoading != Status.RUNNING) {
            mLoading = Status.RUNNING;
            mSavedList = savedList;
            if (animated) {
                adapter.getItems().clear();
                ITEM_LOADING_HANDLER.removeCallbacksAndMessages(null);
                ITEM_LOADING_HANDLER.postDelayed(() -> {
                    adapter.setUsersFromDB(mSavedList);
                    mLoading = Status.FINISHED;
                }, AppConfig.RECYCLER_ANIM_DELAY);
            } else {
                adapter.setUsersFromDB(mSavedList);
                mLoading = Status.FINISHED;
            }
        }
    }

    private void updateAdapterFromDb(List<UserEntity> userEntities) {
        Log.d(TAG, "updateAdapterFromDb: ");

        mSavedList = new ArrayList<ProfileViewModel>() {{
            for (UserEntity u : userEntities) {
                add(new ProfileViewModel(u));
            }
        }};

        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        if (adapter != null && mLoading != Status.RUNNING)
            loadItemsIntoAdapter(adapter, mSavedList, false);
    }

    public boolean isAdapterEmpty() {
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        return adapter == null || adapter.getItems().size() == 0;
    }

    private RecyclerBindingAdapter<ProfileViewModel> getUsersAdapter() {
        if (mProfileBinding == null || mProfileBinding.mainProfileLayout.likesRV == null) {
            mCallbacks.errorAlertExitToMain(getString(R.string.error_ui));
            return null;
        } else
            return (RecyclerBindingAdapter<ProfileViewModel>) mProfileBinding.mainProfileLayout.likesRV.getAdapter();
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::  RecyclerView

    //region :::::::::::::::::::::::::::::::::::::::::: Communication with Activity
    public ProfileViewModel getViewModel() {
        return mProfileViewModel;
    }

    public void forceUpdateLikesList() {
        Log.d(TAG, "updateLikesList: ");
        if (mProfileViewModel != null)
            requestLikeListFromDB(new ArrayList<>(mProfileViewModel.getLikesBy()));
    }

    public void updateLikesList() {
        Log.d(TAG, "updateLikesList: ");
        if (mProfileViewModel != null && mRequestDataFromDBStatus != Status.RUNNING)
            requestLikeListFromDB(new ArrayList<>(mProfileViewModel.getLikesBy()));
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

    public Status getRequestDataFromDBStatus() {
        return mRequestDataFromDBStatus;
    }

    //endregion :::::::::::::::::::::::::::::::::::::::::: Communication with Activity
}
