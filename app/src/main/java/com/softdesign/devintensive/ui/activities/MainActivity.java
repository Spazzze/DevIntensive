package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.CustomGlideModule;
import com.softdesign.devintensive.data.network.api.res.EditProfileRes;
import com.softdesign.devintensive.data.network.api.res.UserPhotoRes;
import com.softdesign.devintensive.data.network.restmodels.BaseModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.operations.BaseChronosOperation;
import com.softdesign.devintensive.data.operations.DatabaseOperation;
import com.softdesign.devintensive.data.operations.FullUserDataOperation;
import com.softdesign.devintensive.ui.callbacks.MainActivityCallback;
import com.softdesign.devintensive.ui.events.UpdateDBEvent;
import com.softdesign.devintensive.ui.fragments.LoadUsersIntoDBFragment;
import com.softdesign.devintensive.ui.fragments.UpdateServerDataFragment;
import com.softdesign.devintensive.ui.view.elements.GlideTargetIntoBitmap;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.NetworkUtils;
import com.softdesign.devintensive.utils.UiHelper;
import com.softdesign.devintensive.utils.UserInfoTextWatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.softdesign.devintensive.utils.UiHelper.createImageFile;
import static com.softdesign.devintensive.utils.UiHelper.openApplicationSetting;
import static com.softdesign.devintensive.utils.UiHelper.queryIntentActivities;

public class MainActivity extends BaseActivity implements MainActivityCallback, UpdateServerDataFragment.UploadToServerCallbacks {

    private static final String TAG = Const.TAG_PREFIX + "Main Activity";

    @BindViews({R.id.scoreBox_rating, R.id.scoreBox_codeLines, R.id.scoreBox_projects}) List<TextView> mTextViews_userProfileValues;

    @BindViews({R.id.phone_EditText, R.id.email_EditText, R.id.vk_EditText, R.id.gitHub_EditText, R.id.about_EditText})
    List<EditText> mEditTexts_userInfoList;

    @BindViews({R.id.phone_TextInputLayout, R.id.email_TextInputLayout, R.id.vk_TextInputLayout, R.id.gitHub_TextInputLayout})
    List<TextInputLayout> mTextInputLayouts_userInfoList;

    @BindView(R.id.navigation_drawerLayout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_coordinatorLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.floating_action_button) FloatingActionButton mFloatingActionButton;
    @BindView(R.id.placeholder_profilePhoto) RelativeLayout mPlaceholder_profilePhoto;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.appbar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.user_photo_img) ImageView mImageView_profilePhoto;

    private Boolean mCurrentEditMode = false;
    private Boolean mNotSavingUserValues = false;
    private DataManager mDataManager;
    private File mPhotoFile = null;
    private Uri mUri_SelectedProfileImage = null;
    private String mUri_SelectedAvatarImage = null;
    private User mUserData = null;
    private FragmentManager mFragmentManager = getFragmentManager();
    private LoadUsersIntoDBFragment mDbNetworkFragment;
    private UpdateServerDataFragment mDataFragment;

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        attachDataFragment();
        attachLoadIntoDBFragment();

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        if (savedInstanceState != null && mUserData != null) {
            mCurrentEditMode = savedInstanceState.getBoolean(Const.EDIT_MODE_KEY);
            changeEditMode(mCurrentEditMode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        return true;
    }

    //endregion

    //region Fragments
    private void attachDataFragment() {
        mDataFragment = (UpdateServerDataFragment) mFragmentManager.findFragmentByTag(UpdateServerDataFragment.class.getName());
        if (mDataFragment == null) {
            mDataFragment = new UpdateServerDataFragment();
            mFragmentManager.beginTransaction().add(mDataFragment, UpdateServerDataFragment.class.getName()).commit();
        }
    }

    private void attachLoadIntoDBFragment() {
        mDbNetworkFragment = (LoadUsersIntoDBFragment) mFragmentManager.findFragmentByTag(LoadUsersIntoDBFragment.class.getName());
        if (mDbNetworkFragment == null) {
            mDbNetworkFragment = new LoadUsersIntoDBFragment();
            mFragmentManager.beginTransaction().add(mDbNetworkFragment, LoadUsersIntoDBFragment.class.getName()).commit();
        }
    }
    //endregion

    //region TaskCallbacks
    @Override
    public void onRequestStarted() {

    }

    @Override
    public void onRequestFinished() {

    }

    @Override
    public void onRequestFailed(String error) {

    }

    @Override
    @SuppressWarnings("unchecked")
    public void onRequestFinished(BaseModel<?> result) {
        if (result.getData().getClass().isAssignableFrom(UserPhotoRes.class)) {
            BaseModel<UserPhotoRes> res = (BaseModel<UserPhotoRes>) result;
            mUserData.getPublicInfo().setUpdated(res.getData().getUpdated());
            saveFullUserData();
        }
        if (result.getData().getClass().isAssignableFrom(EditProfileRes.class)) {
            BaseModel<EditProfileRes> res = (BaseModel<EditProfileRes>) result;
            mUserData = res.getData().getUser();
            saveFullUserData();
        }
    }
    //endregion

    //region Events
    @SuppressWarnings("unused")
    public void onEvent(User event) {
        if (event != null) {
            if (mUserData == null) {
                mUserData = event;
                initUI();
            } else mUserData = event;
        } else {
            loadFullUserData();
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(UpdateDBEvent event) {
        if (mDbNetworkFragment != null) mDbNetworkFragment.downloadUserListIntoDB();
    }
    //endregion

    //region OnClick
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.toolbar_logout:
                logout(1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("deprecation")
    @OnClick({R.id.floating_action_button, R.id.placeholder_profilePhoto, R.id.makeCall_img,
                     R.id.sendEmail_img, R.id.openVK_img, R.id.openGitHub_img})
    void submitButton(View view) {
        switch (view.getId()) {
            case R.id.floating_action_button:
                changeEditMode(!mCurrentEditMode);
                break;
            case R.id.placeholder_profilePhoto:
                showDialogFragment(Const.DIALOG_LOAD_PROFILE_PHOTO);
                break;
            case R.id.makeCall_img:
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mEditTexts_userInfoList.get(0).getText().toString(), null)));
                break;
            case R.id.sendEmail_img:
                Intent sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mEditTexts_userInfoList.get(1).getText().toString(), null));
                if (queryIntentActivities(this, sendEmail)) {
                    startActivity(sendEmail);
                } else {
                    showError(getString(R.string.error_email_client_not_configured));
                }
                break;
            case R.id.openVK_img:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mEditTexts_userInfoList.get(2).getText().toString())));
                break;
            case R.id.openGitHub_img:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mEditTexts_userInfoList.get(3).getText().toString())));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mCurrentEditMode) {
            changeEditMode(false);
        } else {
            super.onBackPressed();
        }
    }
    //endregion

    //region Activity's LifeCycle

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState " + mCurrentEditMode);

        outState.putBoolean(Const.EDIT_MODE_KEY, mCurrentEditMode);
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
        BUS.registerSticky(this);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        BUS.unregister(this);
        saveUserInfoData();
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
    //endregion

    //region Setup Ui Items

    private void initUI() {
        Log.d(TAG, "initUI");
        setupTitle();
        setupEditTexts();
        setupPhoto();
        setupProfileValues();
        setupToolbar();
        setupUserInfoLayout();
        setupDrawer();
    }

    private void setupTitle() {
        String userFullName = String.format("%s %s", mUserData.getSecondName(), mUserData.getFirstName());
        MainActivity.this.setTitle(userFullName);
    }

    private void setupProfileValues() {
        String[] userProfileValuesList = {
                mUserData.getProfileValues().getRating(),
                mUserData.getProfileValues().getCodeLines(),
                mUserData.getProfileValues().getProjects()};

        ButterKnife.apply(mTextViews_userProfileValues, setTextViews, userProfileValuesList);
    }

    private void setupEditTexts() {
        List<String> userProfileDataList = new ArrayList<>();

        userProfileDataList.add(mUserData.getContacts().getPhone());
        userProfileDataList.add(mUserData.getContacts().getEmail());
        userProfileDataList.add(mUserData.getContacts().getVk());
        userProfileDataList.add(mUserData.getRepositories().getRepo().get(0).getGit());
        userProfileDataList.add(mUserData.getPublicInfo().getBio());

        ButterKnife.apply(mEditTexts_userInfoList, setTextViews, userProfileDataList.toArray(new String[userProfileDataList.size()]));
    }

    private void setupPhoto() {
        mUri_SelectedAvatarImage = mDataManager.getPreferencesManager().loadUserAvatar();
        mUri_SelectedProfileImage = mDataManager.getPreferencesManager().loadUserPhoto();
        placeProfilePicture(mUri_SelectedProfileImage);
    }

    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
            mToolbar.inflateMenu(R.menu.toolbar_menu_main);
        }
    }

    private void setupDrawer() {
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                updateDrawerItems();
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            setupDrawerItems(navigationView);
            navigationView.setNavigationItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.navMenu_team:
                        startActivity(new Intent(MainActivity.this, UserListActivity.class));
                        break;
                    case R.id.navMenu_options:
                        startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
                        break;
                    case R.id.navMenu_logout:
                        logout(1);
                        break;
                    default:
                        showToast(item.getTitle().toString());
                        item.setChecked(true);
                        break;
                }
                mDrawerLayout.closeDrawer(GravityCompat.START);
                return false;
            });
        }
    }

    private void updateDrawerItems() {  //redraw navigation view items

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            TextView mTextView_menuUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userName_txt);
            TextView mTextView_menuUserEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userEmail_txt);
            ImageView mRoundedAvatar_img = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.rounded_avatar);

            mTextView_menuUserName.setText(this.getTitle());

            mTextView_menuUserEmail.setText(mUserData.getContacts().getEmail());

            Bitmap src = BitmapFactory.decodeFile(mDataManager.getPreferencesManager().loadUserAvatar());
            if (src == null) {
                loadUserAvatarFromServer();
            } else {
                RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), src);
                dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);
                mRoundedAvatar_img.setImageDrawable(dr);
            }
        }
    }

    private void setupDrawerItems(@NonNull NavigationView navigationView) {  //draw navigation view items

        TextView mTextView_menuUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userName_txt);
        TextView mTextView_menuUserEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userEmail_txt);
        ImageView mRoundedAvatar_img = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.rounded_avatar);

        mTextView_menuUserName.setText(this.getTitle());

        mTextView_menuUserEmail.setText(mUserData.getContacts().getEmail());

        Bitmap src = BitmapFactory.decodeFile(mDataManager.getPreferencesManager().loadUserAvatar());
        if (src == null) {
            loadUserAvatarFromServer();
        } else {
            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), src);
            dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);
            mRoundedAvatar_img.setImageDrawable(dr);
        }
    }

    private void setupUserInfoLayout() {

        final View.OnFocusChangeListener focusListener = (v, hasFocus) -> {
            if (hasFocus) {
                if (v instanceof EditText) {
                    EditText et = (EditText) v;
                    if (!et.isEnabled() && !et.isFocusable()) return;
                    et.setSelection(et.getText().length());
                }
            } else {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)) //this is needed to fix bug with sometimes appearing soft keyboard after onStop() is called
                        .hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        };

        for (int i = 0; i < mEditTexts_userInfoList.size() - 1; i++) {
            mEditTexts_userInfoList.get(i).addTextChangedListener(
                    new UserInfoTextWatcher(mEditTexts_userInfoList.get(i), mTextInputLayouts_userInfoList.get(i)));
            mEditTexts_userInfoList.get(i).setOnFocusChangeListener(focusListener);
        }
    }

    private void placeProfilePicture(Uri selectedImage) {
        Log.d(TAG, "placeProfilePicture: " + selectedImage);
        CustomGlideModule.loadImage(selectedImage.toString(), R.drawable.user_bg, R.drawable.user_bg, mImageView_profilePhoto);
    }

    static final ButterKnife.Setter<TextView, String[]> setTextViews = (view, value, index) -> view.setText(value[index]);

    static final ButterKnife.Setter<View, Boolean> setEnabledViews = (view, value, index) -> {
        view.setEnabled(value);
        view.setFocusable(value);
        view.setFocusableInTouchMode(value);
    };

    //endregion

    //region Save and Load preferences and current state

    private void loadFullUserData() {
        Log.d(TAG, "loadFullUserData: ");
        runOperation(new FullUserDataOperation());
    }

    private void saveFullUserData() {
        Log.d(TAG, "saveFullUserData: ");
        runOperation(new FullUserDataOperation(mUserData));
    }

    private void onUserDataChanged(User savedUser) {
        Log.d(TAG, "onUserDataChanged: ");
        String jsonSavedUser = UiHelper.getJsonFromObject(savedUser, User.class);
        String currentData = UiHelper.getJsonFromObject(mUserData, User.class);
        if (!jsonSavedUser.equals(currentData)) {
            mDataFragment.uploadUserData(mUserData);
        }
    }

    private void saveUserInfoData() {

        if (mNotSavingUserValues) return;

        Log.d(TAG, "saveUserInfoData");
        saveAvatar();
        savePhoto();
        updateUserInfo();
    }

    private void updateUserInfo() {
        readUserInfoFromViews();
        loadFullUserData(); //compare data in SP with current, if data was changed, it will be initiated upload to server
    }

    private void readUserInfoFromViews() {
        mUserData.getContacts().setPhone(mEditTexts_userInfoList.get(0).getText().toString());
        mUserData.getContacts().setEmail(mEditTexts_userInfoList.get(1).getText().toString());
        mUserData.getContacts().setVk(mEditTexts_userInfoList.get(2).getText().toString());
        mUserData.getRepositories().getRepo().get(0).setGit(mEditTexts_userInfoList.get(3).getText().toString());
        mUserData.getPublicInfo().setBio(mEditTexts_userInfoList.get(mEditTexts_userInfoList.size() - 1).getText().toString());
    }

    private void savePhoto() {
        if (mUri_SelectedProfileImage != null &&
                !mDataManager.getPreferencesManager().loadUserPhoto().equals(mUri_SelectedProfileImage)) {
            mDataFragment.uploadUserPhoto(mUri_SelectedProfileImage);
            mDataManager.getPreferencesManager().saveUserPhoto(mUri_SelectedProfileImage);
        }
    }

    private void saveAvatar() {
        if (mUri_SelectedAvatarImage != null &&
                !mDataManager.getPreferencesManager().loadUserAvatar().equals(mUri_SelectedAvatarImage)) {
            mDataFragment.uploadUserAvatar(mUri_SelectedAvatarImage);
            mDataManager.getPreferencesManager().saveUserAvatar(mUri_SelectedAvatarImage);
        }
    }
    //endregion

    //region Activity Results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case Const.REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {
                    mUri_SelectedProfileImage = data.getData();
                    placeProfilePicture(mUri_SelectedProfileImage);
                }
                break;
            case Const.REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mUri_SelectedProfileImage = Uri.fromFile(mPhotoFile);
                    placeProfilePicture(mUri_SelectedProfileImage);
                }
                break;
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
        switch (requestCode) {
            case Const.REQUEST_PERMISSIONS_CAMERA:
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromCamera();
                }
                break;
            case Const.REQUEST_PERMISSIONS_READ_SDCARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromGallery();
                }
                break;
        }
    }
    //endregion

    //region Background Operation Results
    @SuppressWarnings("unused")
    public void onOperationFinished(final FullUserDataOperation.Result result) {
        if (result.isSuccessful()) {
            if (result.getOutput() != null) {//only Loading
                if (mUserData == null) { //init info onCreate
                    mUserData = result.getOutput();
                    initUI();
                } else {
                    onUserDataChanged(result.getOutput());
                }
            }
        } else {
            Log.e(TAG, "onOperationFinished: Данные из памяти не были загружены");
            if (mUserData == null) logout(0);
        }
    }

    //endregion

    //region functional methods

    //region Network
    @SuppressWarnings("all")
    private void loadUserAvatarFromServer() {        //// TODO: 20.07.2016  

        if (!NetworkUtils.isNetworkAvailable(this)) return;

        final String pathToAvatar = mUserData.getPublicInfo().getAvatar();

        int photoWidth = getResources().getDimensionPixelSize(R.dimen.size_medium_64);

        final GlideTargetIntoBitmap avatarTarget = new GlideTargetIntoBitmap(photoWidth, photoWidth, "avatar") {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                super.onResourceReady(bitmap, anim);
                mDataManager.getPreferencesManager().saveUserAvatar((getFile().getAbsolutePath()));
                mUri_SelectedAvatarImage = getFile().getAbsolutePath();

                NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
                if (navigationView != null) {
                    RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                    dr.setCornerRadius(Math.max(bitmap.getWidth(), bitmap.getHeight()) / 2.0f);
                    ImageView mRoundedAvatar_img = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.rounded_avatar);
                    mRoundedAvatar_img.setImageDrawable(dr);
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                Log.e(TAG, "updateUserPhoto onLoadFailed: " + e.getMessage());
                mUri_SelectedAvatarImage = null;
            }
        };

        mToolbar.setTag(avatarTarget);

        Glide.with(this)
                .load(pathToAvatar)
                .asBitmap()
                .into(avatarTarget);
    }
    //endregion

    /**
     * enables or disables editing profile info
     *
     * @param mode if true - editing mode will be enabled
     */
    @SuppressWarnings("deprecation")
    private void changeEditMode(boolean mode) {
        Log.d(TAG, "changeEditMode: " + mode);
        mCurrentEditMode = mode;
        if (mode) {  //editing
            mFloatingActionButton.setImageResource(R.drawable.ic_done_black_24dp);
            collapseAppBar();
            showProfilePhotoPlaceholder();
            mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);

            ButterKnife.apply(mEditTexts_userInfoList, setEnabledViews, true);
            mEditTexts_userInfoList.get(0).requestFocus();
        } else {    //stop edit mode
            saveUserInfoData();
            mFloatingActionButton.setImageResource(R.drawable.ic_edit_black_24dp);
            hideProfilePhotoPlaceholder();
            mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.color_white));
            for (int i = mEditTexts_userInfoList.size() - 1; i >= 0; i--) {    //don't change, magic
                mEditTexts_userInfoList.get(i).setEnabled(false);
                mEditTexts_userInfoList.get(i).setFocusable(false);
                mEditTexts_userInfoList.get(i).setFocusableInTouchMode(false);
                if (i != mEditTexts_userInfoList.size() - 1) {
                    mTextInputLayouts_userInfoList.get(i).setError(null);
                    mTextInputLayouts_userInfoList.get(i).setErrorEnabled(false);
                }
            }
        }
    }

    public void loadPhotoFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeFromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            takeFromGalleryIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(takeFromGalleryIntent, getString(R.string.header_choosePhotoFromGallery)), Const.REQUEST_GALLERY_PICTURE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    Const.REQUEST_PERMISSIONS_READ_SDCARD);
            Snackbar.make(mCoordinatorLayout, R.string.error_access_permissions_needed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.header_allow, v -> {
                        openApplicationSetting(MainActivity.this, Const.REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS);
                    }).show();
        }
    }

    public void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
                showError(getString(R.string.error_cannot_save_file) + e.getMessage());
            }
            if (mPhotoFile != null) {
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, Const.REQUEST_CAMERA_PICTURE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Const.REQUEST_PERMISSIONS_CAMERA);
            Snackbar.make(mCoordinatorLayout, R.string.error_access_permissions_needed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.header_allow, v -> {
                        openApplicationSetting(MainActivity.this, Const.REQUEST_PERMISSIONS_CAMERA_SETTINGS);
                    }).show();
        }
    }

    private void hideProfilePhotoPlaceholder() {
        mPlaceholder_profilePhoto.setVisibility(View.GONE);
    }

    private void showProfilePhotoPlaceholder() {
        mPlaceholder_profilePhoto.setVisibility(View.VISIBLE);
    }

    private void collapseAppBar() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (displayMetrics.densityDpi < DisplayMetrics.DENSITY_XXHIGH) {
            mAppBarLayout.setExpanded(false, true);
        }
    }

    private void logout(int mode) {
        Log.d(TAG, "logout: ");
        mNotSavingUserValues = true;
        if (mode == 1) {
            runOperation(new DatabaseOperation(BaseChronosOperation.Action.CLEAR));
            mDataManager.getPreferencesManager().totalLogout();
        }
        startActivity(new Intent(this, AuthActivity.class));
    }
    //endregion
}