package com.softdesign.devintensive.ui.activities;

import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.softdesign.devintensive.data.operations.DatabaseOperation;
import com.softdesign.devintensive.data.operations.FullUserDataOperation;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.ui.callbacks.BaseTaskCallbacks;
import com.softdesign.devintensive.ui.callbacks.ItemTouchHelperCallback;
import com.softdesign.devintensive.ui.callbacks.OnStartDragListener;
import com.softdesign.devintensive.ui.events.UpdateDBEvent;
import com.softdesign.devintensive.ui.fragments.BaseNetworkFragment;
import com.softdesign.devintensive.ui.fragments.LoadUsersIntoDBFragment;
import com.softdesign.devintensive.ui.view.elements.GlideTargetIntoBitmap;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import java.util.List;

import butterknife.BindView;

public class UserListActivity extends BaseActivity implements BaseTaskCallbacks, OnStartDragListener {

    private static final String TAG = Const.TAG_PREFIX + "UserListActivity";

    @BindView(R.id.navigation_drawerLayout) DrawerLayout mDrawerLayout;
    @BindView(R.id.main_coordinatorLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.appbar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.user_list) RecyclerView mRecyclerView;
    @BindView(R.id.swipeRefresh) SwipeRefreshLayout mSwipeRefreshLayout;

    private DataManager mDataManager;
    private UsersAdapter mUsersAdapter;
    private LoadUsersIntoDBFragment mDbNetworkFragment;
    private final FragmentManager mFragmentManager = getFragmentManager();
    private User mUserData;
    private ItemTouchHelper mItemTouchHelper;
    private ItemTouchHelperCallback mItemTouchHelperCallback;
    private DatabaseOperation.Sort mSort = DatabaseOperation.Sort.CUSTOM;

    //region OnCreate
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);

        attachLoadIntoDBFragment();

        mDataManager = DataManager.getInstance();

        this.setTitle(getString(R.string.header_menu_myTeam));
        runOperation(new FullUserDataOperation());
        setupToolbar();
        setupDrawer();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (mUsersAdapter == null && mDbNetworkFragment != null && mDbNetworkFragment.getStatus() != BaseNetworkFragment.Status.RUNNING) {
            showProgressDialog();
            mDbNetworkFragment.downloadUserListIntoDB();
        } else {
            onRequestFailed(getString(R.string.error_data_from_db_is_not_loaded));
        }

        mSwipeRefreshLayout.setOnRefreshListener(this::refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.color_accent);
    }

    private void refresh() {
        showProgressDialog();
        if (mDbNetworkFragment != null) mDbNetworkFragment.forceRefreshUserListIntoDB();
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
                if (!AppUtils.isEmptyOrNull(newText)) mUsersAdapter.getFilter().filter(newText);
                return false;
            }
        });
        return true;
    }
    //endregion

    //region Fragments
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
        Log.d(TAG, "onRequestFinished: ");
        hideProgressDialog();
        runOperation(new DatabaseOperation(mSort));
    }

    @Override
    public void onRequestFailed(String error) {
        Log.e(TAG, "onLoadIntoDBFailed: " + error);
        hideProgressDialog();
        if (mUsersAdapter == null) {
            showError(Const.DIALOG_SHOW_ERROR_RETURN_TO_MAIN, R.string.error_cannot_load_user_list);
        } else {
            showError(Const.DIALOG_SHOW_ERROR, R.string.error_cannot_load_user_list);
        }
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
            updateDrawerItems();
            navigationView.setNavigationItemSelectedListener(item -> {
                switch (item.getItemId()) {
                    case R.id.navMenu_userProfile:
                        startMainActivity();
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
        if (navigationView != null && mUserData != null) {
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

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    @Override
    public void hideProgressDialog() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showProgressDialog() {
        mSwipeRefreshLayout.setRefreshing(true);
    }

    //endregion

    //region onClick
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.toolbar_refresh:
                refresh();
                return true;
            case R.id.toolbar_sortByRating:
                mSort = DatabaseOperation.Sort.RATING;
                runOperation(new DatabaseOperation(mSort));
                return true;
            case R.id.toolbar_sortByCode:
                mSort = DatabaseOperation.Sort.CODE;
                runOperation(new DatabaseOperation(mSort));
                return true;
            case R.id.toolbar_customSort:
                mSort = DatabaseOperation.Sort.CUSTOM;
                runOperation(new DatabaseOperation(mSort));
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

    //region Life cycle
    @Override
    protected void onResume() {
        super.onResume();                //// TODO: 23.07.2016 переделать когда будут фрагменты
        BUS.registerSticky(this);
    }

    @Override
    protected void onPause() {
        BUS.unregister(this);
        super.onPause();
    }
    //endregion

    //region Events
    @SuppressWarnings("unused")
    public void onEvent(UsersAdapter.ChangeUserInternalId event) {
        if (event != null && !AppUtils.isEmptyOrNull(event.getFirstUserRemoteId())) {
            runOperation(new DatabaseOperation(event.getFirstUserRemoteId(), event.getSecondUserRemoteId()));
        }
    }

    @SuppressWarnings("unused")
    public void onEvent(UpdateDBEvent event) {
        if (mDbNetworkFragment != null) mDbNetworkFragment.downloadUserListIntoDB();
    }
    //endregion

    //region Background Operation Results
    @SuppressWarnings("unused")
    public void onOperationFinished(final DatabaseOperation.Result result) {
        hideProgressDialog();

        if (result.isSuccessful()) {
            if (result.getOutput() != null && result.getOutput().size() > 0) {
                if (mUsersAdapter == null) {
                    initUserListAdapter(result.getOutput());
                } else {
                    updateUserListAdapter(result.getOutput());
                }
            }
        } else {
            onRequestFailed(getString(R.string.error_data_from_db_is_not_loaded));
        }
    }

    public void onOperationFinished(final FullUserDataOperation.Result result) {
       /* if (result.isSuccessful()) {
            if (result.getOutput() != null) {//only Loading
                mUserData = result.getOutput();
            } else {
                logout(0);
            }
        } else {
            Log.e(TAG, "onOperationFinished: Данные из памяти не были загружены");
            if (mUserData == null) logout(0);
        }*/
    }

    //endregion

    private void initUserListAdapter(List<UserEntity> userEntities) {
        mUsersAdapter = new UsersAdapter(userEntities, this, position -> {
            Intent profileUserIntent = new Intent(UserListActivity.this, UserProfileActivity.class);
            profileUserIntent.putExtra(Const.PARCELABLE_KEY, mUsersAdapter.getUsers().get(position));
            startActivity(profileUserIntent);
        }, position -> mDbNetworkFragment.likeUser(
                mUsersAdapter.getUsers().get(position).getRemoteId(),
                mUsersAdapter.getUsers().get(position).isLiked()));
        mRecyclerView.swapAdapter(mUsersAdapter, false);
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        mItemTouchHelperCallback = new ItemTouchHelperCallback(mUsersAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mItemTouchHelper = new ItemTouchHelper(mItemTouchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void updateUserListAdapter(List<UserEntity> userEntities) {
        mUsersAdapter = new UsersAdapter(userEntities, this, position -> {
            Intent profileUserIntent = new Intent(UserListActivity.this, UserProfileActivity.class);
            profileUserIntent.putExtra(Const.PARCELABLE_KEY, mUsersAdapter.getUsers().get(position));
            startActivity(profileUserIntent);
        }, position -> mDbNetworkFragment.likeUser(
                mUsersAdapter.getUsers().get(position).getRemoteId(),
                mUsersAdapter.getUsers().get(position).isLiked()));
        mRecyclerView.swapAdapter(mUsersAdapter, false);
        mItemTouchHelperCallback.swapAdapter(mUsersAdapter);
    }

    @SuppressWarnings("all")
    private void loadUserAvatarFromServer() {

        if (!AppUtils.isNetworkAvailable()) return;

        final String pathToAvatar = mUserData.getPublicInfo().getAvatar();

        int photoWidth = getResources().getDimensionPixelSize(R.dimen.size_medium_64);

        final GlideTargetIntoBitmap avatarTarget = new GlideTargetIntoBitmap(photoWidth, photoWidth, "avatar") {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                super.onResourceReady(bitmap, anim);
                runOperation(new FullUserDataOperation(getFile().getAbsolutePath()));

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
}
