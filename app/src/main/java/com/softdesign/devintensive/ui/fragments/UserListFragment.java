package com.softdesign.devintensive.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
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
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.NetworkRequest.Status;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.operations.DatabaseOperation;
import com.softdesign.devintensive.data.storage.operations.DatabaseOperation.Sort;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.FragmentUserListBinding;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;
import com.softdesign.devintensive.ui.callbacks.ItemTouchHelperCallback;
import com.softdesign.devintensive.ui.callbacks.ListFragmentCallback;
import com.softdesign.devintensive.ui.callbacks.OnStartDragListener;
import com.softdesign.devintensive.ui.view.behaviors.Animations;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.yalantis.contextmenu.lib.ContextMenuDialogFragment;
import com.yalantis.contextmenu.lib.MenuObject;
import com.yalantis.contextmenu.lib.MenuParams;
import com.yalantis.contextmenu.lib.interfaces.OnMenuItemClickListener;

import java.util.ArrayList;
import java.util.List;

import static com.softdesign.devintensive.data.network.NetworkRequest.ID;
import static com.softdesign.devintensive.data.storage.operations.BaseChronosOperation.Action;

@SuppressWarnings("deprecation")
public class UserListFragment extends BaseViewFragment implements OnStartDragListener, OnMenuItemClickListener, UsersAdapter.OnItemClickListener, ListFragmentCallback {
    @StringRes
    private static final int LIST_LOADING_ERROR = R.string.error_cannot_load_user_list;

    private FragmentUserListBinding mListBinding;
    private ContextMenuDialogFragment mMenuDialogFragment;
    private ItemTouchHelper mItemTouchHelper;

    private Status mRequestDataFromDBStatus = Status.PENDING;
    private Sort mSort = Sort.CUSTOM;

    public final ObservableInt mScrollPosition = new ObservableInt();
    private List<ProfileViewModel> mUsers = new ArrayList<>();
    private boolean isStartAnimationFinished = false;

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
        loadUserList(savedInstanceState);
        initRecycleView();
        initFields();
        startIntroAnimation();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu_user_list, menu);

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
                UsersAdapter adapter = getUsersAdapter();
                if (adapter != null) adapter.getFilter().filter(newText);
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
            outState.putParcelableArrayList(Const.PARCELABLE_KEY_USER_LIST,
                    (ArrayList<? extends Parcelable>) getUsersAdapter().getItems());
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
        UsersAdapter adapter;
        if ((adapter = getUsersAdapter()) != null) mUsers = adapter.getItems();
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: UI
    private void initFields() {
        setupMenu();
        mCallbacks.setupToolbar(mListBinding.toolbar, R.menu.toolbar_menu_user_list, true);
        mListBinding.setScrollPos(mScrollPosition);
        mListBinding.swipeRefresh.setOnRefreshListener(this::forceRequestDataFromServer);
        mListBinding.swipeRefresh.setColorSchemeResources(R.color.color_accent);
        mListBinding.fab.setOnClickListener(this::onFABClick);
    }

    public void hideProgressDialog() {
        if (mListBinding != null) mListBinding.swipeRefresh.setRefreshing(false);
    }

    public void showProgressDialog() {
        if (mListBinding != null) mListBinding.swipeRefresh.setRefreshing(true);
    }

    private void setupMenu() {

        MenuObject close = new MenuObject();
        close.setResource(R.drawable.ic_menu_up_white);

        MenuObject favourites = new MenuObject(R.string.header_favourites);
        favourites.setResource(R.drawable.ic_favourites_accent);
        favourites.setId(R.id.favourites_menu);

        MenuObject sortByRating = new MenuObject(R.string.header_sortByRating);
        sortByRating.setResource(R.drawable.ic_sort_descending_colored);
        sortByRating.setId(R.id.sortByRating_menu);

        MenuObject sortByCode = new MenuObject(R.string.header_sortByCode);
        sortByCode.setResource(R.drawable.ic_sort_descending_colored);
        sortByCode.setId(R.id.sortByCode_menu);

        MenuObject sortByCustom = new MenuObject(R.string.header_sortByCustom);
        sortByCustom.setResource(R.drawable.ic_custom_sort_colored);
        sortByCustom.setId(R.id.sortByCustom_menu);

        MenuObject resetCustomSort = new MenuObject(R.string.header_resetCustomSort);
        resetCustomSort.setResource(R.drawable.ic_reset_custom_sort_colored);
        resetCustomSort.setId(R.id.resetCustomSort_menu);

        MenuObject refresh = new MenuObject(R.string.header_refresh);
        refresh.setResource(R.drawable.ic_refresh_white);
        refresh.setId(R.id.refresh_menu);

        List<MenuObject> menuObjects = new ArrayList<>();

        menuObjects.add(close);
        menuObjects.add(favourites);
        menuObjects.add(sortByRating);
        menuObjects.add(sortByCode);
        menuObjects.add(sortByCustom);
        menuObjects.add(resetCustomSort);
        menuObjects.add(refresh);

        for (MenuObject o : menuObjects) {
            o.setBgColor(R.color.color_primary_dark);
            o.setMenuTextAppearanceStyle(R.style.UserListMenuText);
        }

        MenuParams menuParams = new MenuParams();
        menuParams.setActionBarSize((int) AppUtils.getAppBarSize());
        menuParams.setMenuObjects(menuObjects);
        menuParams.setClosableOutside(true);
        menuParams.setFitsSystemWindow(true);
        menuParams.setClipToPadding(false);
        menuParams.setShowAnimationDuration(50);
        menuParams.setHideAnimationDuration(50);
        menuParams.setTextClickable(true);

        mMenuDialogFragment = ContextMenuDialogFragment.newInstance(menuParams);
        mMenuDialogFragment.setItemClickListener(this);
    }

    private void startIntroAnimation() {
        Log.d(TAG, "startIntroAnimation: ");
        mListBinding.fab.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.size_medium_56));

        int actionbarSize = (int) AppUtils.getAppBarSize();
        mListBinding.appbarLayout.setTranslationY(-actionbarSize);
        mListBinding.appbarLayout.animate()
                .translationY(0)
                .setDuration(AppConfig.ANIM_DURATION_TOOLBAR)
                .setStartDelay(AppConfig.ANIM_START_DELAY_TOOLBAR)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animateFab();
                    }
                })
                .start();
    }

    private void animateFab() {
        mListBinding.fab.animate()
                .translationY(0)
                .setInterpolator(new OvershootInterpolator(1.f))
                .setStartDelay(AppConfig.ANIM_START_DELAY_FAB)
                .setDuration(AppConfig.ANIM_DURATION_FAB)
                .start();
        loadItemsIntoAdapter();
        isStartAnimationFinished = true;
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

    private void onFABClick(View view) {
        LinearLayoutManager llm = (LinearLayoutManager) mListBinding.userList.getLayoutManager();
        if (llm == null) return;
        if (llm.findFirstVisibleItemPosition() + llm.getChildCount() >= 3) {
            llm.scrollToPosition(0);
        } else {
            llm.scrollToPosition(llm.getItemCount() - 1);
        }
    }

    @Override
    public void onMenuItemClick(View v, int position) {
        switch (v.getId()) {
            case R.id.favourites_menu:
                requestDataFromDB(Sort.FAVOURITES);
                break;
            case R.id.sortByRating_menu:
                requestDataFromDB(Sort.RATING);
                break;
            case R.id.sortByCode_menu:
                requestDataFromDB(Sort.CODE);
                break;
            case R.id.sortByCustom_menu:
                requestDataFromDB(Sort.CUSTOM);
                break;
            case R.id.resetCustomSort_menu:
                resetCustomSorting();
                break;
            case R.id.refresh_menu:
                forceRequestDataFromServer();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mCallbacks.openDrawer();
                return true;
            case R.id.toolbar_menu:
                if (mMenuDialogFragment != null)
                    mMenuDialogFragment.show(getFragmentManager(), mMenuDialogFragment.getClass().getSimpleName());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        if (mItemTouchHelper != null) mItemTouchHelper.startDrag(viewHolder);
        UsersAdapter adapter = getUsersAdapter();
        int position = viewHolder.getAdapterPosition();
        if (adapter == null || position == -1) return;
        adapter.getItems().get(position).setMoving(true);
    }

    @Override
    public void onLikeClick(UsersAdapter.UserViewHolder userViewHolder, int position) {
        UsersAdapter adapter = getUsersAdapter();
        if (adapter == null || adapter.getItems().size() <= position) return;

        ProfileViewModel model = adapter.getItems().get(position);

        boolean isLiked = !model.isLiked();
        mCallbacks.likeUser(model.getRemoteId(), isLiked);
        model.changeLiked(!isLiked);

        if (adapter.getSort() == Sort.FAVOURITES && !isLiked) {
            adapter.removeItem(model, position);
        } else {
            Animations.animateLikeButton(userViewHolder);
        }
    }

    @Override
    public void onMoreClick(int position) {
        Log.d(TAG, "onLikesListClick: ");
        UsersAdapter adapter = getUsersAdapter();
        if (adapter == null || adapter.getItems().size() <= position) return;

        ProfileViewModel model = adapter.getItems().get(position);
        Bundle b = new Bundle();
        b.putString(Const.PARCELABLE_KEY_USER_ID, model.getRemoteId());
        b.putStringArrayList(Const.PARCELABLE_KEY_LIKES, new ArrayList<>(model.getLikesBy()));
        mCallbacks.attachLikesListFragment(b);
    }

    @Override
    public void onViewProfileClick(int position) {
        Log.d(TAG, "onViewProfileClick: ");
        UsersAdapter adapter = getUsersAdapter();
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
    public void onOperationFinished(final DatabaseOperation.Result result) {
        hideProgressDialog();
        mRequestDataFromDBStatus = Status.FINISHED;
        if (result.isSuccessful()) {
            if (!AppUtils.isEmptyOrNull(result.getOutput())) {
                if (mUsers.size() == 0 && !isStartAnimationFinished) {
                    initUserList(result.getOutput());
                } else {
                    updateUserListAdapter(result.getOutput());
                }
            } else {
                if (isAdapterEmpty()) mCallbacks.forceRefreshUserListFromServer();
            }
        } else {
            listLoadingError();
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Data

    private void initUserList(List<UserEntity> userEntities) {
        this.mUsers = new ArrayList<ProfileViewModel>() {{
            for (UserEntity u : userEntities) {
                add(new ProfileViewModel(u));
            }
        }};
    }

    private void loadUserList(Bundle savedInstanceState) {
        Log.d(TAG, "loadUserList: ");
        List<ProfileViewModel> savedList = null;
        if (mUsers != null && mUsers.size() > 0) {
            return;
        }
        if (savedInstanceState != null) {
            savedList = savedInstanceState.getParcelableArrayList(Const.PARCELABLE_KEY_USER_LIST);
        }
        if (savedList != null) {
            this.mUsers = savedList;
        } else {
            requestDataFromDB(mSort);
        }
    }

    private void requestDataFromDB(Sort sort) {
        mSort = sort;
        mRequestDataFromDBStatus = Status.RUNNING;
        runOperation(new DatabaseOperation(sort));
    }

    public void forceRequestDataFromServer() {
        mCallbacks.forceRefreshUserListFromServer();
    }

    public void resetCustomSorting() {
        mSort = Sort.CUSTOM;
        mRequestDataFromDBStatus = Status.RUNNING;
        runOperation(new DatabaseOperation(Action.RESET));
    }

    public void requestDataFromDB(List list) {
        requestDataFromDB(mSort);
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: RecyclerView
    private UsersAdapter getUsersAdapter() {
        if (mListBinding == null || mListBinding.userList == null) {
            mCallbacks.errorAlertExitToMain(getString(LIST_LOADING_ERROR));
            return null;
        } else
            return (UsersAdapter) mListBinding.userList.getAdapter();
    }

    @Override
    public boolean isAdapterEmpty() {
        UsersAdapter adapter = getUsersAdapter();
        return adapter == null || adapter.getItems().size() == 0;
    }

    private void initRecycleView() {
        UsersAdapter adapter = new UsersAdapter(this, this, this, mSort);
        mListBinding.userList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListBinding.userList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                mScrollPosition.set(llm.findFirstVisibleItemPosition());
            }
        });
        mListBinding.userList.swapAdapter(adapter, false);

        RecyclerView.ItemAnimator animator = mListBinding.userList.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }

        ItemTouchHelperCallback touchHelperCallback = new ItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(mListBinding.userList);
    }

    private void updateUserListAdapter(List<UserEntity> userEntities) {
        Log.d(TAG, "updateUserListAdapter: ");
        UsersAdapter adapter = getUsersAdapter();
        if (adapter != null) adapter.setUsersFromDB(userEntities, mSort);
    }

    public void updateUserList(UserEntity u) {
        UsersAdapter adapter = getUsersAdapter();
        if (adapter != null && adapter.updateUserList(u) && mSort == Sort.FAVOURITES) {
            mListBinding.userList.swapAdapter(getUsersAdapter(), true);
        }
    }

    public void loadItemsIntoAdapter() {
        Log.d(TAG, "loadItemsIntoAdapter: ");
        if (mUsers.size() != 0) {
            UsersAdapter adapter = getUsersAdapter();
            if (adapter != null) adapter.setUsersFromSavedData(mUsers, adapter.getSort());
        } else if (mCallbacks.isNetworkRequestRunning(ID.LOAD_DB) || mRequestDataFromDBStatus == Status.RUNNING) {
            showProgressDialog();
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
