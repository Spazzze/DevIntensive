package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.ui.adapters.GlideTargetIntoBitmap;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.ui.fragments.LoadUsersIntoDBFragment;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.NetworkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserListActivity extends BaseActivity implements LoadUsersIntoDBFragment.TaskCallbacks {

    private static final String TAG = ConstantManager.TAG_PREFIX + "UserListActivity";

    @BindView(R.id.navigation_drawerLayout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_coordinatorLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.appbar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.user_list) RecyclerView mRecyclerView;

    private DataManager mDataManager;
    private UsersAdapter mUsersAdapter;
    private User mUserData;

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        //region Fragment
        FragmentManager fm = getFragmentManager();
        LoadUsersIntoDBFragment networkFragment = (LoadUsersIntoDBFragment) fm.findFragmentByTag(ConstantManager.TAG_USER_LIST_TASK_FRAGMENT);

        if (networkFragment == null) {
            networkFragment = new LoadUsersIntoDBFragment();
            fm.beginTransaction().add(networkFragment, ConstantManager.TAG_USER_LIST_TASK_FRAGMENT).commit();
        }
        //endregion

        initUserProfileInfo();
        setupDrawer();
        setupToolbar();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);
        //// TODO: 18.07.2016 переделать
        if ((mDataManager.getUsersDBSize() == 0 || mDataManager.getRepoDBSize() == 0)) {
            showProgressDialog();
            networkFragment.downloadUserListIntoDB();
        } else
            initUserListAdapter(mDataManager.getUserListFromDb());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.hint_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mUsersAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mUsersAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
    //endregion

    //region LoadUsersIntoDBFragment.TaskCallbacks
    @Override
    public void onLoadIntoDBStarted() {
    }

    @Override
    public void onLoadIntoDBCompleted() {
        Log.d(TAG, "onLoadIntoDBCompleted: Запрос по сети и запись в БД выполнены успешно");
        hideProgressDialog();
        initUserListAdapter(mDataManager.getUserListFromDb());
    }

    @Override
    public void onLoadIntoDBFailed(String error) {
        Log.e(TAG, "onLoadIntoDBFailed: " + error);
        hideProgressDialog();
        showSnackBar(getString(R.string.error_cannot_load_user_list));
    }
    //endregion

    //region UI
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
                    case R.id.navMenu_userProfile:
                        startActivity(new Intent(UserListActivity.this, MainActivity.class));
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

    private void showSnackBar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }
    //endregion

    //region onClick
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

    @Override
    public void onBackPressed() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    //endregion

    private void initUserListAdapter(List<UserEntity> userEntities) {
        if (userEntities == null || userEntities.size() == 0) {
            showSnackBar(getString(R.string.error_cannot_load_user_list));
            return;
        }
        mUsersAdapter = new UsersAdapter(userEntities, position -> {
            UserDTO userDTO = new UserDTO(mUsersAdapter.getUsers().get(position));
            Intent profileUserIntent = new Intent(UserListActivity.this, UserProfileActivity.class);
            profileUserIntent.putExtra(ConstantManager.PARCELABLE_KEY, userDTO);
            startActivity(profileUserIntent);
        });
        mRecyclerView.setAdapter(mUsersAdapter);
    }

    @SuppressWarnings("all")
    private void loadUserAvatarFromServer() {

        if (!NetworkUtils.isNetworkAvailable(this)) return;

        final String pathToAvatar = mUserData.getPublicInfo().getAvatar();

        int photoWidth = getResources().getDimensionPixelSize(R.dimen.size_medium_64);

        final GlideTargetIntoBitmap avatarTarget = new GlideTargetIntoBitmap(photoWidth, photoWidth, "avatar") {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                super.onResourceReady(bitmap, anim);
                mDataManager.getPreferencesManager().saveUserAvatar((getFile().getAbsolutePath()));

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
            }
        };

        mToolbar.setTag(avatarTarget);

        Glide.with(this)
                .load(pathToAvatar)
                .asBitmap()
                .into(avatarTarget);
    }

    private void initUserProfileInfo() {
        Log.d(TAG, "initUserProfileInfo");

        mUserData = mDataManager.getPreferencesManager().loadAllUserData();
        if (mUserData == null) logout(0);
        this.setTitle(getString(R.string.header_menu_myTeam));
    }

    private void logout(int mode) {
        Log.d(TAG, "logout: ");
        if (mode == 1)
            mDataManager.getPreferencesManager().totalLogout();
        startActivity(new Intent(this, AuthActivity.class));
    }
}
