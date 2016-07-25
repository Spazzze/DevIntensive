package com.softdesign.devintensive.ui.activities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.ui.callbacks.BaseTaskCallbacks;
import com.softdesign.devintensive.ui.callbacks.MainActivityCallback;
import com.softdesign.devintensive.ui.fragments.LoadUsersIntoDBFragment;
import com.softdesign.devintensive.ui.fragments.UpdateServerDataFragment;
import com.softdesign.devintensive.ui.fragments.UserProfileFragment;
import com.softdesign.devintensive.utils.Const;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends BaseActivity implements MainActivityCallback, BaseTaskCallbacks {

    private static final String TAG = Const.TAG_PREFIX + "Main Activity";

/*
    @BindViews({R.id.scoreBox_rating, R.id.scoreBox_codeLines, R.id.scoreBox_projects}) List<TextView> mTextViews_userProfileValues;

    @BindViews({R.id.phone_EditText, R.id.email_EditText, R.id.vk_EditText, R.id.gitHub_EditText, R.id.about_EditText})
    List<EditText> mEditTexts_userInfoList;

    @BindViews({R.id.phone_TextInputLayout, R.id.email_TextInputLayout, R.id.vk_TextInputLayout, R.id.gitHub_TextInputLayout})
    List<TextInputLayout> mTextInputLayouts_userInfoList;
*/

    /*@BindView(R.id.navigation_drawerLayout) DrawerLayout mDrawerLayout;*/
   /* @BindView(R.id.main_coordinatorLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.floating_action_button) FloatingActionButton mFloatingActionButton;
    @BindView(R.id.placeholder_profilePhoto) RelativeLayout mPlaceholder_profilePhoto;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.appbar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.user_photo_img) ImageView mImageView_profilePhoto;*/

    /*    private Boolean mCurrentEditMode = false;
        private Boolean mNotSavingUserValues = false;
        private File mPhotoFile = null;
        private Uri mUri_SelectedProfileImage = null;

        private User mUserData = null;*/

    private final FragmentManager mFragmentManager = getFragmentManager();
    private LoadUsersIntoDBFragment mDbNetworkFragment;
    private UpdateServerDataFragment mDataFragment;
    private UserProfileFragment mProfileFragment;
    private DrawerLayout mDrawerLayout;
    private Map<String, String> mAuthorizedUserData = new HashMap<>();

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        mDrawerLayout = $(R.id.navigation_drawerLayout);

        attachDataFragment();
        attachLoadIntoDBFragment();
        attachProfileFragment();

        initUI();

        if (savedInstanceState != null) {    //// TODO: 25.07.2016 parcelable model
            mAuthorizedUserData.put(Const.PARCELABLE_USER_NAME_KEY, savedInstanceState.getString(Const.PARCELABLE_USER_NAME_KEY));
            mAuthorizedUserData.put(Const.PARCELABLE_USER_EMAIL_KEY, savedInstanceState.getString(Const.PARCELABLE_USER_EMAIL_KEY));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putString(Const.PARCELABLE_USER_NAME_KEY, mAuthorizedUserData.get(Const.PARCELABLE_USER_NAME_KEY));
        outState.putString(Const.PARCELABLE_USER_EMAIL_KEY, mAuthorizedUserData.get(Const.PARCELABLE_USER_EMAIL_KEY));
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        return true;
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
/*    @OnClick({R.id.floating_action_button, R.id.placeholder_profilePhoto, R.id.makeCall_img,
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
    }*/

    @Override
    public void onBackPressed() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } /*else if (mCurrentEditMode) {   //// TODO: 25.07.2016  
            changeEditMode(false);
        }*/ else {
            super.onBackPressed();
        }
    }
    //endregion

    //region Activity's LifeCycle

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();                //// TODO: 23.07.2016 переделать когда будут фрагменты
        BUS.registerSticky(this);
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        BUS.unregister(this);
        super.onPause();
    }

    //endregion

    //region Setup Ui Items

    private void initUI() {
        Log.d(TAG, "initUI");
        setupPhoto();
        setupUserInfoLayout();
        setupDrawer();
    }

    private void setupPhoto() {
        /*mUri_SelectedAvatarImage = DATA_MANAGER.getPreferencesManager().loadUserAvatar();       //// TODO: 25.07.2016*/
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
            updateDrawerItems();
            navigationView.setNavigationItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.navMenu_team:
                        startUserListActivity();
                        break;
                    case R.id.navMenu_options:
                        openAppSettings();
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

            updateAuthorizedUserData();

            TextView mTextView_menuUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userName_txt);
            TextView mTextView_menuUserEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userEmail_txt);

            mTextView_menuUserName.setText(mAuthorizedUserData.get(Const.PARCELABLE_USER_NAME_KEY));
            mTextView_menuUserEmail.setText(mAuthorizedUserData.get(Const.PARCELABLE_USER_EMAIL_KEY));

            ImageView mRoundedAvatar_img = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.rounded_avatar);
            Bitmap src = BitmapFactory.decodeFile(DATA_MANAGER.getPreferencesManager().loadUserAvatar());
            if (src == null) {
                loadUserAvatarFromServer();
            } else {
                RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), src);
                dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);
                mRoundedAvatar_img.setImageDrawable(dr);
            }
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

       /* for (int i = 0; i < mEditTexts_userInfoList.size() - 1; i++) {
            mEditTexts_userInfoList.get(i).addTextChangedListener(
                    new UserInfoTextWatcher(mEditTexts_userInfoList.get(i), mTextInputLayouts_userInfoList.get(i)));
            mEditTexts_userInfoList.get(i).setOnFocusChangeListener(focusListener);
        }*/
    }

    //endregion

    //region Network
    @SuppressWarnings("all")
    private void loadUserAvatarFromServer() {

/*        if (!NetworkUtils.isNetworkAvailable()) return;

        String avatar = DATA_MANAGER.getPreferencesManager().loadUserAvatar();

        final String pathToAvatar = mUserData.getPublicInfo().getAvatar();

        int photoWidth = getResources().getDimensionPixelSize(R.dimen.size_medium_64);

        final GlideTargetIntoBitmap avatarTarget = new GlideTargetIntoBitmap(photoWidth, photoWidth, "avatar") {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                super.onResourceReady(bitmap, anim);
                DATA_MANAGER.getPreferencesManager().saveUserAvatar((getFile().getAbsolutePath()));
                mUserAvatarUri = getFile().getAbsolutePath();

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
                mUserAvatarUri = null;
            }
        };

        mToolbar.setTag(avatarTarget);

        Glide.with(this)
                .load(pathToAvatar)
                .asBitmap()
                .into(avatarTarget);*/
    }
    //endregion

    //region  <<<<<<<<<< Fragments >>>>>>>>>>

    private void attachProfileFragment() {
        mProfileFragment = (UserProfileFragment) mFragmentManager.findFragmentByTag(UserProfileFragment.class.getName());
        if (mProfileFragment == null) {
            mProfileFragment = new UserProfileFragment();
            replaceMainFragment(mProfileFragment, true);
        }
    }

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

    private void replaceMainFragment(Fragment fragment, boolean addToBackStack) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.container, fragment, fragment.getClass().getName());
        if (addToBackStack) {
            transaction.addToBackStack(fragment.getClass().getName());
        }
        transaction.commit();
    }
    //endregion

    //region <<<<<<<<<< Task Callbacks >>>>>>>>>>
    @Override
    public void onRequestStarted() {

    }

    @Override
    public void onRequestFinished() {

    }

    @Override
    public void onRequestFailed(String error) {
        Log.e(TAG, "onRequestFailed: " + error);
    }
    //endregion

    //region <<<<<<<<<< Fragments Callbacks >>>>>>>>>>

    @Override
    public void loadPhotoFromGallery() {
        if (mProfileFragment != null) mProfileFragment.loadPhotoFromGallery();
    }

    @Override
    public void loadPhotoFromCamera() {
        if (mProfileFragment != null) mProfileFragment.loadPhotoFromCamera();
    }

    @Override
    public void uploadUserData(ProfileViewModel model) {
        if (mDataFragment != null) mDataFragment.uploadUserData(model);
    }

    @Override
    public void uploadUserPhoto(Uri uri) {
        if (mDataFragment != null) mDataFragment.uploadUserPhoto(uri);
    }

    @Override
    public void uploadUserAvatar(String uri) {
        if (mDataFragment != null) mDataFragment.uploadUserAvatar(uri);
    }

    @Override
    public void updateAuthorizedUserData() {
        if (mProfileFragment != null) {
            mAuthorizedUserData = mProfileFragment.getAuthorizedUserInfo();
        }
        if (mAuthorizedUserData.isEmpty()) {
            User user = DATA_MANAGER.getPreferencesManager().loadAllUserData();
            mAuthorizedUserData.put(Const.PARCELABLE_USER_NAME_KEY, String.format("%s %s", user.getFirstName(), user.getSecondName()));
            mAuthorizedUserData.put(Const.PARCELABLE_USER_EMAIL_KEY, user.getContacts().getEmail());
        }
    }

    //endregion
}