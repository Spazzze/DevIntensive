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
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

import static com.softdesign.devintensive.utils.UiHelper.createImageFile;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Main Activity";

    @BindViews({R.id.phone_EditText, R.id.email_EditText, R.id.vk_EditText, R.id.gitHub_EditText, R.id.about_EditText})
    List<EditText> mUserInfoList;

    @BindView(R.id.phone_EditText) EditText mEditText_userPhone;
    @BindView(R.id.email_EditText) EditText mEditText_userEmail;
    @BindView(R.id.vk_EditText) EditText mEditText_userVk;
    @BindView(R.id.gitHub_EditText) EditText mEditText_userGitHub;
    @BindView(R.id.about_EditText) EditText mEditText_userAbout;
    @BindView(R.id.navigation_drawerLayout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_coordinatorLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.floating_action_button) FloatingActionButton mFloatingActionButton;
    @BindView(R.id.placeholder_profilePhoto) RelativeLayout mPlaceholder_profilePhoto;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.appbar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.user_photo_img) ImageView mImageView_profilePhoto;
    @BindView(R.id.makeCall_img) ImageView mImageView_makeCall;
    @BindView(R.id.sendEmail_img) ImageView mImageView_sendEmail;
    @BindView(R.id.openVK_img) ImageView mImageView_openVK;
    @BindView(R.id.openGitHub_img) ImageView mImageView_openGitHub;
    @BindView(R.id.phone_LL) LinearLayout mLinearLayout_phone;
    @BindView(R.id.email_LL) LinearLayout mLinearLayout_email;
    @BindView(R.id.vk_LL) LinearLayout mLinearLayout_vk;
    @BindView(R.id.gitHub_LL) LinearLayout mLinearLayout_gitHub;
    @BindView(R.id.phone_TextInputLayout) TextInputLayout mTextInputLayout_phone;
    @BindView(R.id.email_TextInputLayout) TextInputLayout mTextInputLayout_email;
    @BindView(R.id.vk_TextInputLayout) TextInputLayout mTextInputLayout_vk;
    @BindView(R.id.gitHub_TextInputLayout) TextInputLayout mTextInputLayout_gitHub;

    private int mCurrentEditMode;
    private int mToolBarScrollFlag;

    private DataManager mDataManager;
    private File mPhotoFile = null;
    private Uri mUri_SelectedImage = null;

    /**
     * метод вызывается при создании активити (после изменения/возврата к текущей
     * активности после ее уничтожения)
     * <p/>
     * в данном методе инициализируется/производится:
     * - UI statics;
     * - init activity's static data;
     * - link data to lists (init adapters);
     * <p/>
     * DO NOT EXECUTE LONGTIME OPERATIONS IN THIS METHOD!!!
     *
     * @param savedInstanceState - объект со значениями, сохраненными в Bundle - состояние UI
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        mFloatingActionButton.setOnClickListener(this);
        mPlaceholder_profilePhoto.setOnClickListener(this);

        setupUserInfoLayout();
        setupToolbar();
        setupDrawer();
        loadUserInfoValue();

        if (savedInstanceState == null) {
            showToast("activity запущено впервые");
            AppBarLayout.LayoutParams appBarLayoutParams = (AppBarLayout.LayoutParams) mCollapsingToolbarLayout.getLayoutParams();
            mToolBarScrollFlag = appBarLayoutParams.getScrollFlags();
        } else {
            mCurrentEditMode = savedInstanceState.getInt(ConstantManager.EDIT_MODE_KEY);
            mToolBarScrollFlag = savedInstanceState.getInt(ConstantManager.TOOLBAR_SCROLL_KEY);
            changeEditMode(mCurrentEditMode);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        return true;
    }

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floating_action_button:
                if (mCurrentEditMode == 1) {
                    changeEditMode(0);
                } else {
                    changeEditMode(1);
                }
                break;
            case R.id.placeholder_profilePhoto:
                showDialog(ConstantManager.LOAD_PROFILE_PHOTO);
                break;
            case R.id.makeCall_img:
                Intent makeCall = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mEditText_userPhone.getText().toString(), null));
                startActivity(makeCall);
                break;
            case R.id.sendEmail_img:
                Intent sendEmail = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", mEditText_userEmail.getText().toString(), null));
                startActivity(sendEmail);
                break;
            case R.id.openVK_img:
                Intent openVK = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mEditText_userVk.getText().toString()));
                startActivity(openVK);
                break;
            case R.id.openGitHub_img:
                Intent openGitHub = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" + mEditText_userGitHub.getText().toString()));
                startActivity(openGitHub);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");

        outState.putInt(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
        outState.putInt(ConstantManager.TOOLBAR_SCROLL_KEY, mToolBarScrollFlag);
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
        if (!mDataManager.getPreferencesManager().checkAuthorizationStatus()) {
            logout();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        saveUserInfoValue();
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
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mCurrentEditMode == 1) {
            changeEditMode(0);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
            case ConstantManager.REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {
                    mUri_SelectedImage = data.getData();
                    Log.d(TAG, "onActivityResult: 1" + mUri_SelectedImage.toString());
                    placeProfilePicture(mUri_SelectedImage);
                    mDataManager.getPreferencesManager().saveUserPhoto(mUri_SelectedImage);
                }
                break;
            case ConstantManager.REQUEST_CAMERA_PICTURE:
                if (resultCode == RESULT_OK && mPhotoFile != null) {
                    mUri_SelectedImage = Uri.fromFile(mPhotoFile);
                    Log.d(TAG, "onActivityResult: 2" + mUri_SelectedImage.toString());
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
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case ConstantManager.LOAD_PROFILE_PHOTO:
                String[] selectedItems = getResources().getStringArray(R.array.profile_placeHolder_loadPhotoDialog);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.hint_profile_placeHolder_loadPhotoDialog_title));
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

    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
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
            TextView mTextView_menuUserEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userEmail_txt);
            mTextView_menuUserEmail.setText(mEditText_userEmail.getEditableText().toString()); //drawer menu email change
            setupMenuAvatar();
        }
    }

    private void setupMenuAvatar() {    //setup menu avatar with rounded corners from picture
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            ImageView mRoundedAvatar_img = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.rounded_avatar);
            Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
            RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(getResources(), src);
            dr.setCornerRadius(Math.max(src.getWidth(), src.getHeight()) / 2.0f);
            mRoundedAvatar_img.setImageDrawable(dr);
        }
    }

    private void changeEditMode(int mode) {
        Log.d(TAG, "changeEditMode: " + mode);
        if (mode == 1) {  //editing
            mFloatingActionButton.setImageResource(R.drawable.ic_done_black_24dp);
            for (EditText e : mUserInfoList) {
                e.setEnabled(true);
                e.setFocusable(true);
                e.setFocusableInTouchMode(true);
            }
            mEditText_userPhone.requestFocus();
            changeToolBarScrollFlags(0);
            showProfilePhotoPlaceholder();
            mCollapsingToolbarLayout.setExpandedTitleColor(Color.TRANSPARENT);
            mCurrentEditMode = 1;
        } else {    //stop edit mode
            mFloatingActionButton.setImageResource(R.drawable.ic_edit_black_24dp);
            for (int i = mUserInfoList.size() - 1; i >= 0; i--) {    //don't change, magic
                mUserInfoList.get(i).setEnabled(false);
                mUserInfoList.get(i).setFocusable(false);
                mUserInfoList.get(i).setFocusableInTouchMode(false);
            }
            hideProfilePhotoPlaceholder();
            changeToolBarScrollFlags(mToolBarScrollFlag);
            saveUserInfoValue();
            mCollapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.color_white));
            mCurrentEditMode = 0;
        }
    }

    private void loadUserInfoValue() {
        Log.d(TAG, "loadUserInfoValue");
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();

        for (int i = 0; i < userData.size(); i++) {
            mUserInfoList.get(i).setText(userData.get(i));
        }
        placeProfilePicture(mDataManager.getPreferencesManager().loadUserPhoto());
    }

    private void saveUserInfoValue() {
        Log.d(TAG, "saveUserInfoValue");

        List<String> userData = new ArrayList<>();
        for (int i = 0; i < mUserInfoList.size(); i++) {
            userData.add(mUserInfoList.get(i).getText().toString());
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userData);
        mDataManager.getPreferencesManager().saveUserPhoto(mUri_SelectedImage);
        updateDrawerItems();
    }

    private void loadPhotoFromGallery() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeFromGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            takeFromGalleryIntent.setType("image/*");
            startActivityForResult(Intent.createChooser(takeFromGalleryIntent, getString(R.string.hint_choosePhotoFromGallery)), ConstantManager.REQUEST_GALLERY_PICTURE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD);
            Snackbar.make(mCoordinatorLayout, R.string.error_access_permissions_needed, Snackbar.LENGTH_LONG)
                    .setAction(R.string.header_allow, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSetting(ConstantManager.REQUEST_PERMISSIONS_READ_SDCARD_SETTINGS);
                        }
                    }).show();
        }
    }

    private void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                mPhotoFile = createImageFile(this);
            } catch (IOException e) {
                showSnackBar(getString(R.string.error_cannot_create_file) + e.getMessage());
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
                            openApplicationSetting(ConstantManager.REQUEST_PERMISSIONS_CAMERA_SETTINGS);
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

    private void changeToolBarScrollFlags(int scrollFlag) {
        /*AppBarLayout.LayoutParams appBarLayoutParams = (AppBarLayout.LayoutParams) mCollapsingToolbarLayout.getLayoutParams();*/
        /*appBarLayoutParams.setScrollFlags(scrollFlag);*/
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        if (displayMetrics.density < 3 && scrollFlag == 0) {
            mAppBarLayout.setExpanded(false, true);
        }
        /*mCollapsingToolbarLayout.setLayoutParams(appBarLayoutParams);*/
    }

    private void openApplicationSetting(int flag) {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        startActivityForResult(appSettingsIntent, flag);
    }

    private void placeProfilePicture(Uri selectedImage) {
        Picasso.with(this)
                .load(selectedImage)
                .resize(getResources().getDimensionPixelSize(R.dimen.profileImage_size_256), getResources().getDimensionPixelSize(R.dimen.profileImage_size_256))
                .centerInside()
                .placeholder(R.drawable.user_bg)
                .into(mImageView_profilePhoto);
    }

    private void logout() {
        mDataManager.getPreferencesManager().removeCurrentAuthorization();
        startActivity(new Intent(this, AuthorizationActivity.class));
    }

    private void setupUserInfoLayout() {
        mImageView_makeCall.setOnClickListener(this);
        mImageView_sendEmail.setOnClickListener(this);
        mImageView_openVK.setOnClickListener(this);
        mImageView_openGitHub.setOnClickListener(this);

        mEditText_userPhone.addTextChangedListener(
                new UserInfoTextWatcher(this, mEditText_userPhone, mTextInputLayout_phone, mLinearLayout_phone));
        mEditText_userEmail.addTextChangedListener(
                new UserInfoTextWatcher(this, mEditText_userEmail, mTextInputLayout_email, mLinearLayout_email));
        mEditText_userVk.addTextChangedListener(
                new UserInfoTextWatcher(this, mEditText_userVk, mTextInputLayout_vk, mLinearLayout_vk));
        mEditText_userGitHub.addTextChangedListener(
                new UserInfoTextWatcher(this, mEditText_userGitHub, mTextInputLayout_gitHub, mLinearLayout_gitHub));

        final View.OnFocusChangeListener focusListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (v instanceof EditText) {
                        EditText et = (EditText) v;
                        if (!et.isEnabled() && !et.isFocusable()) return;
                        et.setSelection(et.getText().length());
                        /*((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(et, 0);*/
                    }
                } else {
                    ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        };

        mEditText_userPhone.setOnFocusChangeListener(focusListener);
        mEditText_userEmail.setOnFocusChangeListener(focusListener);
        mEditText_userVk.setOnFocusChangeListener(focusListener);
        mEditText_userGitHub.setOnFocusChangeListener(focusListener);
    }
}