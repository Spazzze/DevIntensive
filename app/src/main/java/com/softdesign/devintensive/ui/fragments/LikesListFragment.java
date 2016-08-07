package com.softdesign.devintensive.ui.fragments;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.databinding.library.baseAdapters.BR;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.operations.DBSelectOperation;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.FragmentLikesListBinding;
import com.softdesign.devintensive.ui.adapters.RecyclerBindingAdapter;
import com.softdesign.devintensive.ui.callbacks.ListFragmentCallback;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;

import java.util.ArrayList;
import java.util.List;

import static com.softdesign.devintensive.data.network.NetworkRequest.ID;
import static com.softdesign.devintensive.data.network.NetworkRequest.Status;

@SuppressWarnings({"unchecked", "deprecation"})
public class LikesListFragment extends BaseViewFragment implements ListFragmentCallback {
    @StringRes
    private static final int LIST_LOADING_ERROR = R.string.error_cannot_load_likes_list;
    private static final String AUTH_USER_ID = DataManager.getInstance().getPreferencesManager().loadBuiltInAuthId();

    private FragmentLikesListBinding mListBinding;

    private String mUserId;

    private Status mRequestDataFromDBStatus = Status.PENDING;
    private ObservableInt likesCount = new ObservableInt();
    private List<ProfileViewModel> mUsers = new ArrayList<>();

    //region :::::::::::::::::::::::::::::::::::::::::: onCreate
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        mListBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_likes_list, container, false);
        return mListBinding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadUserList(savedInstanceState);
        initRecycleView();
        initFields();
        loadItemsIntoAdapter();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu_likes_list, menu);

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
                getUsersAdapter().getFilter().filter(newText);
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
        if (getUsersAdapter() != null) {
            if (outState == null) outState = new Bundle();
            outState.putParcelableArrayList(Const.PARCELABLE_KEY_LIKES,
                    (ArrayList<? extends Parcelable>) getUsersAdapter().getItems());
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: UI
    private void initFields() {
        mListBinding.setLikesCount(likesCount);
        mCallbacks.setupToolbar(mListBinding.toolbar, R.menu.toolbar_menu_likes_list, false);
        mListBinding.swipeRefresh.setOnRefreshListener(this::forceRequestDataFromServer);
        mListBinding.swipeRefresh.setColorSchemeResources(R.color.color_accent);
    }

    public void hideProgressDialog() {
        if (mListBinding != null) mListBinding.swipeRefresh.setRefreshing(false);
    }

    public void showProgressDialog() {
        if (mListBinding != null) mListBinding.swipeRefresh.setRefreshing(true);
    }

    public void listLoadingError() {
        if (isAdapterEmpty()) {
            mCallbacks.errorAlertExitToMain(getString(LIST_LOADING_ERROR));
        } else {
            mCallbacks.showError(LIST_LOADING_ERROR);
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: onClick

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mCallbacks.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void onViewProfileClick(int position) {
        Log.d(TAG, "onViewProfileClick: ");
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        if (adapter == null || adapter.getItems().size() <= position) return;

        ProfileViewModel model = adapter.getItems().get(position);
        Bundle b = new Bundle();
        b.putString(Const.PARCELABLE_KEY_USER_ID, model.getRemoteId());
        b.putParcelable(Const.PARCELABLE_KEY_PROFILE, new ProfileViewModel().updateValues(model));
        mCallbacks.attachOtherUserFragment(b);
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Events

    @SuppressWarnings("unused")
    public void onOperationFinished(final DBSelectOperation.Result result) {
        hideProgressDialog();
        mRequestDataFromDBStatus = Status.FINISHED;
        if (result.isSuccessful()) {
            updateUserListAdapter(result.getOutput());
        } else {
            listLoadingError();
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::   Events

    //region :::::::::::::::::::::::::::::::::::::::::: Data

    private void loadUserList(Bundle savedInstanceState) {
        Log.d(TAG, "loadUserList: ");
        List<ProfileViewModel> savedList = null;
        if (savedInstanceState != null) {
            savedList = savedInstanceState.getParcelableArrayList(Const.PARCELABLE_KEY_LIKES);
        }
        if (savedList != null) {
            this.mUsers = savedList;
        } else if (getArguments() != null) {
            mUserId = getArguments().getString(Const.PARCELABLE_KEY_USER_ID);
            savedList = getArguments().getParcelableArrayList(Const.PARCELABLE_KEY_LIKES_MODEL);
            if (savedList == null) {
                List<String> usersIds = getArguments().getStringArrayList(Const.PARCELABLE_KEY_LIKES);
                requestDataFromDB(usersIds);
            } else {
                Log.d(TAG, "loadUserList: 2");
                this.mUsers = savedList;
            }
        } else if (!AppUtils.isEmptyOrNull(mUserId)) {
            requestDataFromDB(mUserId);
        } else {
            mCallbacks.errorAlertExitToMain(getString(LIST_LOADING_ERROR));
        }
    }

    public void requestDataFromDB(String id) {
        mRequestDataFromDBStatus = Status.RUNNING;
        runOperation(new DBSelectOperation(id));
    }

    @Override
    public void forceRequestDataFromServer() {
        if (AppUtils.isEmptyOrNull(mUserId)) return;
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        if (adapter == null) return;
        boolean isLikedByMe = false;
        mUsers = adapter.getItems();
        for (ProfileViewModel p : mUsers) {
            if (AppUtils.equals(p.getRemoteId(), AUTH_USER_ID)) {
                isLikedByMe = true;
                break;
            }
        }
        mCallbacks.forceRefreshLikesListFromServer(mUserId, isLikedByMe);
    }

    @Override
    public void requestDataFromDB(@Nullable List list) {
        if (!AppUtils.isEmptyOrNull(list)) {
            mRequestDataFromDBStatus = Status.RUNNING;
            runOperation(new DBSelectOperation(list));
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::  Data

    //region :::::::::::::::::::::::::::::::::::::::::: RecyclerView
    public RecyclerBindingAdapter<ProfileViewModel> getUsersAdapter() {
        if (mListBinding == null || mListBinding.userList == null) {
            mCallbacks.errorAlertExitToMain(getString(LIST_LOADING_ERROR));
            return null;
        } else
            return (RecyclerBindingAdapter<ProfileViewModel>) mListBinding.userList.getAdapter();
    }

    @Override
    public boolean isAdapterEmpty() {
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        return adapter == null || adapter.getItems().size() == 0;
    }

    private void initRecycleView() {
        RecyclerBindingAdapter<ProfileViewModel> bindingAdapter = new RecyclerBindingAdapter<>(
                R.layout.item_likes_list,
                BR.profile,
                new ArrayList<>(),
                this::onViewProfileClick);

        RecyclerView.ItemAnimator animator = mListBinding.userList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        bindingAdapter.setFilter(new RecyclerBindingAdapter.CustomBindingFilter<ProfileViewModel>(bindingAdapter) {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<ProfileViewModel> tempList = new ArrayList<>();
                if (constraint.length() != 0) {
                    final String filterPattern = constraint.toString().toLowerCase().trim();
                    for (final ProfileViewModel s : this.getList()) {
                        if (s.getFullName().toLowerCase().contains(filterPattern) ||
                                s.getHometask().contains(filterPattern)) {
                            tempList.add(s);
                        }
                    }
                } else {
                    tempList = this.getList();
                }
                Log.d(TAG, "performFiltering: " + tempList.size());
                this.mAdapter.setListFromFilter(tempList);
                return null;
            }
        });
        mListBinding.userList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListBinding.userList.swapAdapter(bindingAdapter, false);
    }

    private void updateUserListAdapter(List<UserEntity> userEntities) {
        Log.d(TAG, "updateUserListAdapter: ");
        RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
        if (adapter != null) {
            adapter.setUsersFromDB(new ArrayList<ProfileViewModel>() {{
                for (UserEntity u : userEntities) {
                    add(new ProfileViewModel(u));
                }
            }});
            likesCount.set(adapter.getItems().size());
        }
    }

    public void loadItemsIntoAdapter() {
        Log.d(TAG, "loadItemsIntoAdapter: ");
        if (mUsers.size() != 0) {
            RecyclerBindingAdapter<ProfileViewModel> adapter = getUsersAdapter();
            if (adapter != null) {
                adapter.setUsersFromDB(mUsers);
                likesCount.set(mUsers.size());
            }
        } else if (mCallbacks.isNetworkRequestRunning(ID.LOAD_DB) || mRequestDataFromDBStatus == Status.RUNNING) {
            showProgressDialog();
        }
    }

    public void updateLikesList(UserEntity u) {
        requestDataFromDB(u.getLikesList());
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::  RecyclerView
}
