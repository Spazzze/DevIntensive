package com.softdesign.devintensive.ui.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.data.network.restmodels.BaseListModel;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.ErrorUtils;
import com.softdesign.devintensive.utils.NetworkUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends BaseActivity {

    private static final String TAG = ConstantManager.TAG_PREFIX + "UserListActivity";

    @BindView(R.id.navigation_drawerLayout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_coordinatorLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.appbar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.user_list) RecyclerView mRecyclerView;

    private DataManager mDataManager;
    private UsersAdapter mUsersAdapter;
    private List<UserListRes> mUsers;
    private User mUserData;

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        ButterKnife.bind(this);

        mDataManager = DataManager.getInstance();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);

        initUserProfileInfo();
        setupDrawer();
        setupToolbar();
        loadUsers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_main, menu);
        return true;
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
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            updateDrawerItems();
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
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
            ImageView mRoundedAvatar_img = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.rounded_avatar);

            mTextView_menuUserName.setText(this.getTitle());
            mTextView_menuUserEmail.setText(mUserData.getContacts().getEmail());

            Bitmap src = BitmapFactory.decodeFile(mDataManager.getPreferencesManager().loadUserAvatar());
            if (src != null) {
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

    private void initUserProfileInfo() {
        Log.d(TAG, "initUserProfileInfo");

        mUserData = mDataManager.getPreferencesManager().loadAllUserData();
        if (mUserData != null)
            this.setTitle(String.format("%s %s", mUserData.getSecondName(), mUserData.getFirstName()));
    }

    private void logout(int mode) {
        Log.d(TAG, "logout: ");
        if (mode == 1)
            mDataManager.getPreferencesManager().totalLogout();
        else mDataManager.getPreferencesManager().softLogout();
        startActivity(new Intent(this, AuthActivity.class));
    }

    private void loadUsers() {

        Call<BaseListModel<UserListRes>> call = mDataManager.getUserList();
        showProgressDialog();
        call.enqueue(new Callback<BaseListModel<UserListRes>>() {
                         @Override
                         public void onResponse(Call<BaseListModel<UserListRes>> call, Response<BaseListModel<UserListRes>> response) {
                             hideProgressDialog();
                             if (response.isSuccessful()) {
                                 mUsers = response.body().getData();
                                 mUsersAdapter = new UsersAdapter(mUsers, new UsersAdapter.UserViewHolder.CustomClickListener() {
                                     @Override
                                     public void onUserItemClickListener(int position) {
                                         UserDTO userDTO = new UserDTO(mUsers.get(position));
                                         Intent profileUserIntent = new Intent(UserListActivity.this, UserProfileActivity.class);
                                         profileUserIntent.putExtra(ConstantManager.PARCELABLE_KEY, userDTO);
                                         startActivity(profileUserIntent);
                                     }
                                 });
                                 mRecyclerView.setAdapter(mUsersAdapter);
                             } else {
                                 hideProgressDialog();
                                 ErrorUtils.BackendHttpError error = ErrorUtils.parseHttpError(response);
                                 showToast(error.getErrMessage());
                             }
                         }

                         @Override
                         public void onFailure(Call<BaseListModel<UserListRes>> call, Throwable t) {
                             hideProgressDialog();
                             if (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {
                                 showSnackBar(getString(R.string.error_no_network_connection));
                             } else
                                 showSnackBar(String.format("%s: %s", getString(R.string.error_unknown_auth_error), t.getMessage()));
                         }
                     }

        );
    }
}
