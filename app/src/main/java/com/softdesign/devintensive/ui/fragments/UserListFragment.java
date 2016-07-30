package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SimpleItemAnimator;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.operations.DatabaseOperation;
import com.softdesign.devintensive.data.storage.operations.DatabaseOperation.Sort;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.FragmentUserListBinding;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.ui.callbacks.ItemTouchHelperCallback;
import com.softdesign.devintensive.ui.callbacks.OnStartDragListener;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import java.util.ArrayList;
import java.util.List;

public class UserListFragment extends BaseViewFragment implements OnStartDragListener {

    private FragmentUserListBinding mListBinding;

    private UsersAdapter mUsersAdapter;
    private ItemTouchHelper mItemTouchHelper;
    private Sort mSort = Sort.CUSTOM;

    //region :::::::::::::::::::::::::::::::::::::::::: onCreate
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_list, container, false);
        return mListBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFields(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint(getString(R.string.hint_search));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mUsersAdapter.getFilter().filter(newText);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Life Cycle

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mUsersAdapter != null) {
            if (outState == null) outState = new Bundle();
            outState.putParcelableArrayList(Const.PARCELABLE_KEY_USER_LIST,
                    (ArrayList<? extends Parcelable>) mUsersAdapter.getUsers());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BUS.registerSticky(this);
    }

    @Override
    public void onDetach() {
        BUS.unregister(this);
        super.onDetach();
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: UI
    private void initFields(Bundle savedInstanceState) {
        Log.d(TAG, "initFields: ");
        mCallbacks.setupToolbar(mListBinding.toolbar, R.menu.toolbar_menu_search);

        mListBinding.userList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListBinding.swipeRefresh.setOnRefreshListener(this::forceRefreshUserListFromServer);
        mListBinding.swipeRefresh.setColorSchemeResources(R.color.color_accent);

        List<ProfileViewModel> savedList = null;
        if (mUsersAdapter != null && mUsersAdapter.getUsers().size() > 0) {
            resumeRecycleView();
            return;
        }
        if (savedInstanceState != null) {
            savedList = savedInstanceState.getParcelableArrayList(Const.PARCELABLE_KEY_USER_LIST);
        }
        if (savedList != null) {
            resumeUserListAdapter(savedList);
        } else {
            requestDataFromDB();
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }

    public void hideProgressDialog() {
        if (mListBinding != null) mListBinding.swipeRefresh.setRefreshing(false);
    }

    public void showProgressDialog() {
        if (mListBinding != null) mListBinding.swipeRefresh.setRefreshing(true);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: onClick
    private void callUserProfileFragment(int pos) {
        Bundle b = new Bundle();
        b.putBoolean(Const.OTHER_USER_PROFILE_KEY, true);
        b.putParcelable(Const.PARCELABLE_KEY_PROFILE, new ProfileViewModel().updateValues(mUsersAdapter.getUsers().get(pos)));
        mCallbacks.attachOtherUserFragment(b);
    }

    private void likeUser(int position) {
        mCallbacks.likeUser(mUsersAdapter.getUsers().get(position).getRemoteId(),
                mUsersAdapter.getUsers().get(position).isLiked());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mCallbacks.openDrawer();
                return true;
            case R.id.toolbar_refresh:
                forceRefreshUserListFromServer();
                return true;
            case R.id.toolbar_sortByRating:
                mSort = Sort.RATING;
                requestDataFromDB();
                return true;
            case R.id.toolbar_sortByCode:
                mSort = Sort.CODE;
                requestDataFromDB();
                return true;
            case R.id.toolbar_customSort:
                mSort = Sort.CUSTOM;
                requestDataFromDB();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Events
    @SuppressWarnings("unused")
    public void onEvent(UsersAdapter.ChangeUserInternalId event) {
        if (event != null && !AppUtils.isEmptyOrNull(event.getFirstUserRemoteId())) {
            runOperation(new DatabaseOperation(event.getFirstUserRemoteId(), event.getSecondUserRemoteId()));
            BUS.removeStickyEvent(UsersAdapter.ChangeUserInternalId.class);
        }
    }

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
            } else {
                if (mUsersAdapter == null) mCallbacks.forceRefreshUserListFromServer();
            }
        } else {
            if (mUsersAdapter == null) {
                mCallbacks.showError(Const.DIALOG_SHOW_ERROR_RETURN_TO_MAIN, R.string.error_cannot_load_user_list);
            } else {
                mCallbacks.showError(Const.DIALOG_SHOW_ERROR, R.string.error_cannot_load_user_list);
            }
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Data

    public void requestDataFromDB() {
        runOperation(new DatabaseOperation(mSort));
    }

    private void forceRefreshUserListFromServer() {
        Log.d(TAG, "forceRefreshUserListFromServer: ");
        mCallbacks.forceRefreshUserListFromServer();
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Adapter
    public UsersAdapter getUsersAdapter() {
        return mUsersAdapter;
    }

    private void resumeUserListAdapter(List<ProfileViewModel> userEntities) {
        mUsersAdapter = new UsersAdapter(userEntities, this, this::callUserProfileFragment, this::likeUser);

        initRecycleView();
    }

    private void initUserListAdapter(List<UserEntity> userEntities) {
        mUsersAdapter = new UsersAdapter(userEntities, this, this::callUserProfileFragment, this::likeUser);

        initRecycleView();
    }

    private void resumeRecycleView() {
        Log.d(TAG, "resumeRecycleView: ");
        mListBinding.userList.swapAdapter(mUsersAdapter, false);
    }

    private void initRecycleView() {
        mListBinding.userList.swapAdapter(mUsersAdapter, false);

        RecyclerView.ItemAnimator animator = mListBinding.userList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback(mUsersAdapter);
        mItemTouchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(mListBinding.userList);
    }

    private void updateUserListAdapter(List<UserEntity> userEntities) {
        Log.d(TAG, "updateUserListAdapter: ");
        mUsersAdapter.setUsersFromDB(userEntities);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
