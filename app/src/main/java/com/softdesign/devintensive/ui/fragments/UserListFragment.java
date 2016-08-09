package com.softdesign.devintensive.ui.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableInt;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import static com.softdesign.devintensive.ui.view.animations.Animations.animateFabAppearance;
import static com.softdesign.devintensive.ui.view.animations.Animations.animateLikeButton;

@SuppressWarnings("deprecation")
public class UserListFragment extends BaseViewFragment implements OnStartDragListener, OnMenuItemClickListener, UsersAdapter.OnItemClickListener, ListFragmentCallback, View.OnLongClickListener {
    @StringRes
    private static final int LIST_LOADING_ERROR = R.string.error_cannot_load_user_list;
    private static final int ITEM_COUNT_DELAY = 1000;
    private static final Handler ITEM_COUNT_HANDLER = new Handler();
    private static final Handler ITEM_LOADING_HANDLER = new Handler();

    private FragmentUserListBinding mListBinding;
    private ContextMenuDialogFragment mMenuDialogFragment;
    private ItemTouchHelper mItemTouchHelper;

    private Status mRequestDataFromDBStatus = Status.PENDING;
    private Sort mSort = Sort.CUSTOM;
    private Status mLoading = Status.PENDING;
    private boolean isConfiguring = false;

    public final ObservableInt mScrollPosition = new ObservableInt();
    public final ObservableInt mItemsOnList = new ObservableInt(2);

    private List<ProfileViewModel> mSavedList = new ArrayList<>();
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
        UsersAdapter adapter;
        if ((adapter = getUsersAdapter()) != null) {
            if (outState == null) outState = new Bundle();
            outState.putParcelableArrayList(Const.PARCELABLE_KEY_USER_LIST,
                    (ArrayList<? extends Parcelable>) adapter.getItems());
            outState.putSerializable(Const.PARCELABLE_KEY_USER_LIST_SORT, mSort);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        UsersAdapter adapter;
        if ((adapter = getUsersAdapter()) != null) mSavedList = adapter.getItems();
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: UI
    private void initFields() {
        setupMenu();
        mCallbacks.setupToolbar(mListBinding.toolbar, R.menu.toolbar_menu_user_list, true);
        mListBinding.setScrollPos(mScrollPosition);
        mListBinding.setItemsOnList(mItemsOnList);
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

    public void listLoadingError() {
        if (isAdapterEmpty()) {
            mCallbacks.errorAlertExitToMain(getString(LIST_LOADING_ERROR));
        } else {
            mCallbacks.showError(LIST_LOADING_ERROR);
        }
    }

    private void startIntroAnimation() {
        Log.d(TAG, "startIntroAnimation: ");
        mListBinding.fab.setTranslationY(2 * getResources().getDimensionPixelOffset(R.dimen.size_medium_56));

        int actionbarSize = (int) AppUtils.getAppBarSize();
        mListBinding.toolbar.setTranslationY(-actionbarSize);
        mListBinding.toolbar.animate()
                .translationY(0)
                .setDuration(AppConfig.UL_ANIM_DURATION_TOOLBAR)
                .setStartDelay(AppConfig.UL_ANIM_START_DELAY_TOOLBAR)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (!AppUtils.isEmptyOrNull(mSavedList))
                            initLoadItemsIntoAdapter(mSavedList);
                        animateFabAppearance(mListBinding.fab, 2 * getResources().getDimensionPixelOffset(R.dimen.size_medium_56));
                        isStartAnimationFinished = true;
                    }
                })
                .start();
    }

    private void setMainRecyclerAnimation() {
        mListBinding.userList.setItemAnimator(AppConfig.UL_MAIN_ANIMATION.getAnimator());
        mListBinding.userList.getItemAnimator().setAddDuration(AppConfig.UL_ANIM_DURATION_ITEM_ADD);
        mListBinding.userList.getItemAnimator().setRemoveDuration(AppConfig.UL_ANIM_DURATION_ITEM_REMOVE);
    }

    private void setConfigRecyclerAnimation() {
        mListBinding.userList.setItemAnimator(AppConfig.UL_CONFIG_ANIMATION.getAnimator());
        mListBinding.userList.getItemAnimator().setAddDuration(AppConfig.UL_ANIM_DURATION_CONF_ITEM_ADD);
        mListBinding.userList.getItemAnimator().setRemoveDuration(AppConfig.UL_ANIM_DURATION_CONF_ITEM_REMOVE);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: onClick
    @Override
    public boolean onLongClick(View v) {
        if (mSort == Sort.CUSTOM) configureMode(true);
        return true;
    }

    private void onFABClick(View view) {
        LinearLayoutManager llm = (LinearLayoutManager) mListBinding.userList.getLayoutManager();
        if (llm == null) return;
        if (llm.findFirstVisibleItemPosition() >= llm.getChildCount()) {
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
                if (isConfiguring) configureMode(false);
                else requestDataFromDB(Sort.CUSTOM);
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
            animateLikeButton(userViewHolder);
        }
    }

    @Override
    public void onMoreClick(int position) {
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
            updateAdapterFromDb(result.getOutput());
        } else {
            listLoadingError();
        }
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Data
    private void loadUserList(Bundle savedInstanceState) {
        List<ProfileViewModel> savedList = null;
        if (mSavedList != null && mSavedList.size() > 0) {
            return;
        }
        if (savedInstanceState != null) {
            savedList = savedInstanceState.getParcelableArrayList(Const.PARCELABLE_KEY_USER_LIST);
            mSort = (Sort) savedInstanceState.get(Const.PARCELABLE_KEY_USER_LIST_SORT);
        }
        if (savedList != null) {
            this.mSavedList = savedList;
        } else {
            requestDataFromDB(mSort);
        }
    }

    private void requestDataFromDB(Sort sort) {
        mSort = sort;
        mRequestDataFromDBStatus = Status.RUNNING;
        runOperation(new DatabaseOperation(sort));
    }

    private void resetCustomSorting() {
        mSort = Sort.CUSTOM;
        mRequestDataFromDBStatus = Status.RUNNING;
        runOperation(new DatabaseOperation(Action.RESET));
    }

    @Override
    public void forceRequestDataFromServer() {
        mCallbacks.forceRefreshUserListFromServer();
    }

    @Override
    public void requestDataFromDB(List list) {
        requestDataFromDB(mSort);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: RecyclerView
    private void initRecycleView() {
        UsersAdapter adapter = new UsersAdapter(this, this, this, this, mSort);
        mListBinding.userList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListBinding.userList.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
                mScrollPosition.set(llm.findFirstVisibleItemPosition());
            }
        });
        mListBinding.userList.swapAdapter(adapter, false);

        setMainRecyclerAnimation();

        ItemTouchHelperCallback touchHelperCallback = new ItemTouchHelperCallback(adapter);
        mItemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        mItemTouchHelper.attachToRecyclerView(mListBinding.userList);
    }

    private void initLoadItemsIntoAdapter(List<ProfileViewModel> savedList) {
        UsersAdapter adapter = getUsersAdapter();
        if (adapter != null) configureMode(isConfiguring);
    }

    private void loadItemsIntoAdapter(@NonNull UsersAdapter adapter, final List<ProfileViewModel> savedList, boolean animated) {
        Log.d(TAG, "loadItemsIntoAdapter: " + (savedList == null ? "null" : savedList.size()) + " " + mLoading);
        if (savedList != null && mLoading != Status.RUNNING) {
            mLoading = Status.RUNNING;
            if (animated) {
                adapter.getItems().clear();
                ITEM_LOADING_HANDLER.removeCallbacksAndMessages(null);
                ITEM_LOADING_HANDLER.postDelayed(() -> {
                    adapter.setUsersList(savedList, mSort);
                    mLoading = Status.FINISHED;
                }, AppConfig.RECYCLER_ANIM_DELAY);
            } else {
                adapter.setUsersList(savedList, mSort);
                mLoading = Status.FINISHED;
            }
            mSavedList = savedList;
            countItemsAtList();
        } else if (mCallbacks.isNetworkRequestRunning(ID.LOAD_DB) || mRequestDataFromDBStatus == Status.RUNNING) {
            showProgressDialog();
        }
    }

    public void updateUserListEntity(UserEntity u) {
        UsersAdapter adapter = getUsersAdapter();
        if (adapter != null && adapter.updateUserList(u) && mSort == Sort.FAVOURITES) {
            mListBinding.userList.swapAdapter(adapter, true);
            mSavedList = adapter.getItems();
        }
    }

    private void updateAdapterFromDb(List<UserEntity> userEntities) {
        Log.d(TAG, "updateAdapterFromDb: ");

        mSavedList = new ArrayList<ProfileViewModel>() {{
            for (UserEntity u : userEntities) {
                add(new ProfileViewModel(u));
            }
        }};

        UsersAdapter adapter = getUsersAdapter();
        if (adapter == null) return;

        if (mSort != Sort.CUSTOM && adapter.isConfigure()) {
            configureMode(false);
        } else {
            if (isStartAnimationFinished) loadItemsIntoAdapter(adapter, mSavedList, false);
        }
    }

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

    private void countItemsAtList() {
        ITEM_COUNT_HANDLER.removeCallbacksAndMessages(null);
        ITEM_COUNT_HANDLER.postDelayed(() -> {
            LinearLayoutManager llm = (LinearLayoutManager) mListBinding.userList.getLayoutManager();
            UsersAdapter adapter = getUsersAdapter();
            if (adapter == null || llm == null) return;
            mItemsOnList.set(llm.getChildCount());
        }, ITEM_COUNT_DELAY);
    }

    public boolean isConfiguring() {
        return isConfiguring;
    }

    public void configureMode(boolean mode) {
        Log.d(TAG, "configureMode: " + mode);
        UsersAdapter adapter = getUsersAdapter();
        if (adapter == null) return;
        isConfiguring = mode;
        if (mode) {
            adapter.showConfigureView(true);
            setConfigRecyclerAnimation();
        } else {
            adapter.showConfigureView(false);
            setMainRecyclerAnimation();
        }
        loadItemsIntoAdapter(adapter, new ArrayList<>(mSavedList), true);
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
