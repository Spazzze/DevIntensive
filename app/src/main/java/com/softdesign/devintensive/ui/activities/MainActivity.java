package com.softdesign.devintensive.ui.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.PhoneNumberTextWatcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    public static final String TAG = ConstantManager.TAG_PREFIX + "Main Activity";

    private int mCurrentEditMode = 0;
    private ArrayList<EditText> mUserInfoList;

    private DataManager mDataManager;
    private EditText mEditText_userPhone, mEditText_userEmail, mEditText_userGitHub, mEditText_userVk, mEditText_userAbout;
    private ImageView mMakeCall_img;
    private CoordinatorLayout mCoordinatorLayout;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private FloatingActionButton mFloatingActionButton;

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

        mDataManager = DataManager.getInstance();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.navigation_drawerLayout);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator_container);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mEditText_userPhone = (EditText) findViewById(R.id.phone_EditText);
        mEditText_userEmail = (EditText) findViewById(R.id.email_EditText);
        mEditText_userVk = (EditText) findViewById(R.id.vk_EditText);
        mEditText_userGitHub = (EditText) findViewById(R.id.gitHub_EditText);
        mEditText_userAbout = (EditText) findViewById(R.id.about_EditText);
        mMakeCall_img = (ImageView) findViewById(R.id.makeCall_img);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);

        mUserInfoList = new ArrayList<>();
        Collections.addAll(mUserInfoList, mEditText_userPhone, mEditText_userEmail, mEditText_userVk, mEditText_userGitHub, mEditText_userAbout);

        mEditText_userPhone.addTextChangedListener(new PhoneNumberTextWatcher(mEditText_userPhone));
        mFloatingActionButton.setOnClickListener(this);
        setupToolbar();
        setupDrawer();
        loadUserInfoValue();

        if (savedInstanceState == null) {
            showToast("activity запущено впервые");
            //activity запущено впервые
        } else {
            //activity уже запускалось
            mCurrentEditMode = savedInstanceState.getInt(ConstantManager.EDIT_MODE_KEY);
            changeEditMode(mCurrentEditMode);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floating_action_button:
                if (mCurrentEditMode == 1) {
                    changeEditMode(0);
                    mCurrentEditMode = 0;
                } else {
                    changeEditMode(1);
                    mCurrentEditMode = 1;
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");

        outState.putInt(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
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

    public void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    public void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setupDrawer() {
        Log.d(TAG, "setupDrawer");

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    showSnackbar(item.getTitle().toString());
                    item.setChecked(true);
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    return false;
                }
            });
        }
    }

    public void updateDrawerItems(){  //redraw navigation view items
        Log.d(TAG, "updateDrawerItems");

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            TextView mTextView_menuUserEmail = (TextView) navigationView.getHeaderView(0).findViewById(R.id.menu_userEmail_txt);
            mTextView_menuUserEmail.setText(mEditText_userEmail.getText()); //drawer menu email change
            setupMenuAvatar();
        }
    }

    public void setupMenuAvatar() {    //setup menu avatar with rounded corners from picture
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
        if (mode == 1) {
            mFloatingActionButton.setImageResource(R.drawable.ic_done_black_24dp);
            for (EditText e : mUserInfoList) {
                e.setEnabled(true);
                e.setFocusable(true);
                e.setFocusableInTouchMode(true);
            }
        } else {
            mFloatingActionButton.setImageResource(R.drawable.ic_edit_black_24dp);
            for (EditText e : mUserInfoList) {
                e.setEnabled(false);
                e.setFocusable(false);
                e.setFocusableInTouchMode(false);
            }
            saveUserInfoValue();
        }
    }

    private void loadUserInfoValue() {
        Log.d(TAG, "loadUserInfoValue");

        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();

        for (int i = 0; i < userData.size(); i++) {
            mUserInfoList.get(i).setText(userData.get(i));
        }
        updateDrawerItems();
    }

    private void saveUserInfoValue() {
        Log.d(TAG, "saveUserInfoValue");

        List<String> userData = new ArrayList<>();
        for (int i = 0; i < mUserInfoList.size(); i++) {
            userData.add(mUserInfoList.get(i).getText().toString());
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userData);
        updateDrawerItems();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
