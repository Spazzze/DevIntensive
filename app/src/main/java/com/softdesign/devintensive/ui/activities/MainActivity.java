package com.softdesign.devintensive.ui.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.UserInfoTextWatcher;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.softdesign.devintensive.utils.UiHelper.createImageFile;
import static com.softdesign.devintensive.utils.UiHelper.openApplicationSetting;
import static com.softdesign.devintensive.utils.UiHelper.queryIntentActivities;

public class MainActivity extends BaseActivity {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Main Activity";

    @BindViews({R.id.scoreBox_rating, R.id.scoreBox_codeLines, R.id.scoreBox_projects}) List<TextView> mTextViews_userProfileValues;

    @BindViews({R.id.phone_EditText, R.id.email_EditText, R.id.vk_EditText, R.id.gitHub_EditText,
                       R.id.gitHub_EditText1, R.id.gitHub_EditText2, R.id.about_EditText})
    List<EditText> mEditTexts_userInfoList;

    @BindViews({R.id.phone_TextInputLayout, R.id.email_TextInputLayout, R.id.vk_TextInputLayout,
                       R.id.gitHub_TextInputLayout, R.id.gitHub_TextInputLayout1, R.id.gitHub_TextInputLayout2})
    List<TextInputLayout> mTextInputLayouts_userInfoList;

    @BindViews({R.id.gitHub_LL1, R.id.gitHub_LL2}) List<LinearLayout> mLinearLayouts_gitHubAdditional;

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
    private Uri mUri_SelectedImage = null;


    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        setupUserInfoLayout();
        initUserProfileInfo();
        setupDrawer();
        setupToolbar();
        refresh();

        if (savedInstanceState != null) {
            mCurrentEditMode = savedInstanceState.getBoolean(ConstantManager.EDIT_MODE_KEY);
            changeEditMode(mCurrentEditMode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String[] selectedItems = getResources().getStringArray(R.array.profile_placeHolder_loadPhotoDialog);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.header_profile_placeHolder_loadPhotoDialog_title));
                builder.setItems(selectedItems, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int chosenItem) {
                        switch (chosenItem) {
                            case 0:
                                loadPhotoFromCamera();
                                break;
                            case 1:
                                loadPhotoFromGallery();
                                break;
                            case 2:
                                dialog.cancel();
                                break;
                        }
                    }
                });
                return builder.create();
            default:
                return null;
        }
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
                logout();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("deprecation")
    @OnClick({R.id.floating_action_button, R.id.placeholder_profilePhoto, R.id.makeCall_img,
                     R.id.sendEmail_img, R.id.openVK_img, R.id.openGitHub_img, R.id.openGitHub_img1, R.id.openGitHub_img2})
    void submitButton(View view) {
        switch (view.getId()) {
            case R.id.floating_action_button:
                changeEditMode(!mCurrentEditMode);
                break;
            case R.id.placeholder_profilePhoto:
                showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                break;
            case R.id.makeCall_img:
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mEditTexts_userInfoList.get(0).getText().toString(), null)));
                break;
            case R.id.sendEmail_img:
                Intent sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mEditTexts_userInfoList.get(1).getText().toString(), null));
                if (queryIntentActivities(this, sendEmail)) {
                    startActivity(sendEmail);
                } else {
                    showSnackBar(getString(R.string.error_email_client_not_configured));
                }
                break;
            case R.id.openVK_img:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mEditTexts_userInfoList.get(2).getText().toString())));
                break;
            case R.id.openGitHub_img:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mEditTexts_userInfoList.get(3).getText().toString())));
                break;
            case R.id.openGitHub_img1:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mEditTexts_userInfoList.get(4).getText().toString())));
                break;
            case R.id.openGitHub_img2:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mEditTexts_userInfoList.get(5).getText().toString())));
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
        if (!mDataManager.getPreferencesManager().checkAuthorizationStatus()) {
            mDataManager.getPreferencesManager().softLogout();
            startActivity(new Intent(this, AuthActivity.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        saveUserInfoData();
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
        mDataManager.getPreferencesManager().removeGoogleAuthorizationOnDestroy();
    }
    //endregion

    //region Setup Ui Items
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
        Log.d(TAG, "setupDrawer");
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            updateDrawerItems();
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navMenu_options:
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName())));
                            break;
                        case R.id.navMenu_logout:
                            logout();
                            break;
                        default:
                            showSnackBar(item.getTitle().toString());
                            item.setChecked(true);
                            break;
                    }
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    return false;
                }
            });
        }
    }

    private void updateDrawerItems() {  //redraw navigation view items
        Log.d(TAG, "updateDrawerItems");

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            TextView mTextView_menuUserName = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userName_txt);
            TextView mTextView_menuUserEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userEmail_txt);
            mTextView_menuUserName.setText(MainActivity.this.getTitle());
            mTextView_menuUserEmail.setText(mEditTexts_userInfoList.get(1).getText().toString()); //drawer menu email change
            setupMenuAvatar();
        }
    }

    private void updateGitHubFields() {
        //// TODO: 12.07.2016 переделать на ListView
        for (int i = 0; i < mEditTexts_userInfoList.size(); i++) {
            String s = mEditTexts_userInfoList.get(i).getText().toString();
            switch (mEditTexts_userInfoList.get(i).getId()) {
                case R.id.gitHub_EditText1:
                    if (!s.isEmpty()) {
                        mLinearLayouts_gitHubAdditional.get(0).setVisibility(View.VISIBLE);
                    } else {
                        mLinearLayouts_gitHubAdditional.get(0).setVisibility(View.GONE);
                    }
                    break;
                case R.id.gitHub_EditText2:
                    if (!s.isEmpty()) {
                        mLinearLayouts_gitHubAdditional.get(1).setVisibility(View.VISIBLE);
                    } else {
                        mLinearLayouts_gitHubAdditional.get(1).setVisibility(View.GONE);
                    }
            }
        }
    }

    private void setupMenuAvatar() {    //setup menu avatar with rounded corners from picture
        //// TODO: 08.07.2016 get avatars from social info
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            ImageView mRoundedAvatar_img = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.rounded_avatar);
            Bitmap src = BitmapFactory.decodeFile(mDataManager.getPreferencesManager().loadUserAvatar());
            if (src != null) {
                RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), src);
                dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);
                mRoundedAvatar_img.setImageDrawable(dr);
            }
            /*Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);*/
        }
    }

    private void setupUserInfoLayout() {

        final View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
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
        Picasso.with(this)
                .load(selectedImage)
                .resize(getResources().getDimensionPixelSize(R.dimen.profileImage_size_256), getResources().getDimensionPixelSize(R.dimen.profileImage_size_256))
                .centerInside()
                .placeholder(R.drawable.user_bg)
                .into(mImageView_profilePhoto);
    }

    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private void refresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                placeProfilePicture(mUri_SelectedImage);
                updateGitHubFields();
                updateDrawerItems();
            }
        }, 500);
    }

    static final ButterKnife.Setter<TextView, String[]> setTextViews = new ButterKnife.Setter<TextView, String[]>() {
        @Override
        public void set(@NonNull TextView view, String[] value, int index) {
            view.setText(value[index]);
        }
    };

    static final ButterKnife.Setter<View, Boolean> setEnabledViews = new ButterKnife.Setter<View, Boolean>() {
        @Override
        public void set(@NonNull View view, Boolean value, int index) {
            view.setEnabled(value);
            view.setFocusable(value);
            view.setFocusableInTouchMode(value);
        }
    };

    //endregion

    //region Save and Load preferences and current state
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");

        outState.putBoolean(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
    }

    private void initUserProfileInfo() {
        Log.d(TAG, "initUserProfileInfo");
        mUri_SelectedImage = mDataManager.getPreferencesManager().loadUserPhoto();
        ArrayList<EditText> mainEditTexts = new ArrayList<>(mEditTexts_userInfoList.subList(0, 4));
        mainEditTexts.add(mEditTexts_userInfoList.get(mEditTexts_userInfoList.size() - 1));

        List<String> userProfileDataList = mDataManager.getPreferencesManager().loadUserProfileData();
        ButterKnife.apply(mainEditTexts, setTextViews, userProfileDataList.toArray(new String[userProfileDataList.size()]));

        List<String> userProfileValuesList = mDataManager.getPreferencesManager().loadUserProfileValues();
        ButterKnife.apply(mTextViews_userProfileValues, setTextViews, userProfileValuesList.toArray(new String[userProfileValuesList.size()]));

        List<String> additionalGitHubList = mDataManager.getPreferencesManager().loadUserAdditionalGitHubRepo();
        for (int i = 0; i < additionalGitHubList.size(); i++) {
            mEditTexts_userInfoList.get(4 + i).setText(additionalGitHubList.get(i));
            if (i == 1) break;
        }

        String userFullName = mDataManager.getPreferencesManager().loadUserName().get(ConstantManager.USER_FULL_NAME_KEY);
        MainActivity.this.setTitle(userFullName);
    }

    private void saveUserInfoData() {

        if (mNotSavingUserValues) return;

        Log.d(TAG, "saveUserInfoData");

        Map<String, String> userInfo = new HashMap<>();

        userInfo.put(ConstantManager.USER_PHONE_KEY, mEditTexts_userInfoList.get(0).getText().toString());
        userInfo.put(ConstantManager.USER_EMAIL_KEY, mEditTexts_userInfoList.get(1).getText().toString());
        userInfo.put(ConstantManager.USER_VK_KEY, mEditTexts_userInfoList.get(2).getText().toString());
        userInfo.put(ConstantManager.USER_GITHUB_KEY, mEditTexts_userInfoList.get(3).getText().toString());
        userInfo.put(ConstantManager.USER_ABOUT_KEY, mEditTexts_userInfoList.get(mEditTexts_userInfoList.size() - 1).getText().toString());

        int m = 1;
        //indexes of additional gitHub repo;
        for (int i = 4; i <= 5; i++) {
            String s = mEditTexts_userInfoList.get(i).getText().toString();
            String key = ConstantManager.USER_GITHUB_KEY + m;
            userInfo.put(key, s);
            m++;
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userInfo);

        mDataManager.getPreferencesManager().saveUserPhoto(mUri_SelectedImage);
        updateDrawerItems();
        updateGitHubFields();
    }
    //endregion

    //region Activity Results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case ConstantManager.REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {
                    mUri_SelectedImage = data.getData();
                    placeProfilePicture(mUri_SelectedImage);
                    mDataManager.getPreferencesManager().saveUserPhoto(mUri_SelectedImage);
                }
                break;
            case ConstantManager.REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mUri_SelectedImage = Uri.fromFile(mPhotoFile);
                    placeProfilePicture(mUri_SelectedImage);
                    mDataManager.getPreferencesManager().saveUserPhoto(mUri_SelectedImage);
                }
                break;
            case ConstantManager.REQUEST_PERMISSIONS_CAMERA_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromCamera();
                }
                break;
            case ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromGallery();
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ConstantManager.REQUEST_PERMISSIONS_CAMERA:
                if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromCamera();
                }
                break;
            case ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadPhotoFromGallery();
                }
                break;
        }
    }
    //endregion

    //region functional methods

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

    private void loadPhotoFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeFromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            takeFromGalleryIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(takeFromGalleryIntent, getString(R.string.header_choosePhotoFromGallery)), ConstantManager.REQUEST_GALLERY_PICTURE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD);
            Snackbar.make(mCoordinatorLayout, R.string.error_access_permissions_needed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.header_allow, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSetting(MainActivity.this, ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS);
                        }
                    }).show();
        }
    }

    private void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                mPhotoFile = createImageFile();
            } catch (IOException e) {
                showSnackBar(getString(R.string.error_cannot_save_file) + e.getMessage());
            }
            if (mPhotoFile != null) {
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile));
                startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_PICTURE);
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    ConstantManager.REQUEST_PERMISSIONS_CAMERA);
            Snackbar.make(mCoordinatorLayout, R.string.error_access_permissions_needed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.header_allow, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSetting(MainActivity.this, ConstantManager.REQUEST_PERMISSIONS_CAMERA_SETTINGS);
                        }
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

    private void logout() {
        Log.d(TAG, "logout: ");
        mNotSavingUserValues = true;
        mDataManager.getPreferencesManager().totalLogout();
        startActivity(new Intent(this, AuthActivity.class));
    }
    //endregion
}