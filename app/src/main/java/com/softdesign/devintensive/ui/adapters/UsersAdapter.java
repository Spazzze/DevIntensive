package com.softdesign.devintensive.ui.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.redmadrobot.chronos.gui.fragment.ChronosSupportFragment;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.data.storage.operations.DBSwapOperation;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.ItemUserListBinding;
import com.softdesign.devintensive.ui.callbacks.ItemTouchHelperAdapter;
import com.softdesign.devintensive.ui.callbacks.ItemTouchHelperViewHolder;
import com.softdesign.devintensive.ui.callbacks.OnStartDragListener;
import com.softdesign.devintensive.utils.AppUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.softdesign.devintensive.data.storage.operations.DatabaseOperation.Sort;

@SuppressWarnings({"unused", "all"})
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements Filterable, ItemTouchHelperAdapter {

    public interface OnItemClickListener {
        void onLikeClick(UserViewHolder userViewHolder, int position);

        void onMoreClick(int position);

        void onViewProfileClick(int position);
    }

    private CustomUserListFilter mFilter;
    private final OnStartDragListener mDragStartListener;
    private ChronosSupportFragment mFragment;

    private List<ProfileViewModel> mUsers = new ArrayList<>();
    private OnItemClickListener mClickListener;
    private Sort mSort = Sort.CUSTOM;
    private boolean isMovable = false;

    //region :::::::::::::::::::::::::::::::::::::::::: Adapter

    public UsersAdapter(ChronosSupportFragment fragment, OnStartDragListener dragListener, OnItemClickListener cLickListener, Sort sort) {
        this.mDragStartListener = dragListener;
        this.mFilter = new CustomUserListFilter(UsersAdapter.this);
        this.mClickListener = cLickListener;
        this.mSort = sort;
        this.mFragment = fragment;
    }

    @Override
    public UsersAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);

        return new UserViewHolder(convertView, mDragStartListener, mClickListener);
    }

    @Override
    public void onBindViewHolder(final UsersAdapter.UserViewHolder holder, int position) {
        holder.getBinding().setProfile(mUsers.get(position));
        holder.getBinding().getProfile().setAnimateTextChange(false);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    @Override
    public void onItemDismiss(int position) {
        ProfileViewModel u = removeItem(position);
        mFragment.runOperation(new DBSwapOperation(u.getRemoteId(), null));
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        mFragment.runOperation(new DBSwapOperation(mUsers.get(fromPosition).getRemoteId(), mUsers.get(toPosition).getRemoteId()));
        Collections.swap(mUsers, fromPosition, toPosition);
        Collections.swap(mFilter.getList(), fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<ProfileViewModel> getItems() {
        return mUsers;
    }

    public void setUsersFromSavedData(List<ProfileViewModel> users, Sort sort) {
        if (AppUtils.compareLists(users, mUsers)) return;
        synchronized (this) {
            mSort = sort;
            mUsers = users;
            this.mFilter = new CustomUserListFilter(UsersAdapter.this);
        }
        notifyDataSetChanged();
    }

    public void setUsersFromDB(final List<UserEntity> users, Sort sort) {
        synchronized (this) {
            mSort = sort;
            mUsers = new ArrayList<ProfileViewModel>() {{
                for (UserEntity u : users) {
                    add(new ProfileViewModel(u));
                }
            }};
            this.mFilter = new CustomUserListFilter(UsersAdapter.this);
        }
        notifyDataSetChanged();
    }

    public boolean updateUserList(UserEntity u) {
        ProfileViewModel updatedUser = new ProfileViewModel(u);
        synchronized (mUsers) {
            for (int i = 0; i < mUsers.size(); i++) {
                ProfileViewModel m = mUsers.get(i);
                if (AppUtils.equals(m.getRemoteId(), updatedUser.getRemoteId())) {
                    if (mSort == Sort.FAVOURITES && !updatedUser.isLiked()) {
                        removeItem(i);
                    } else if (!AppUtils.equals(m, updatedUser)) {
                        m.updateProfileValues(updatedUser);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void removeItem(ProfileViewModel model, int position) {
        synchronized (this) {
            mFilter.getList().remove(model);
            mUsers.remove(model);
            notifyItemRemoved(position);
        }
    }

    public ProfileViewModel removeItem(int position) {
        synchronized (this) {
            ProfileViewModel u = mUsers.get(position);
            mFilter.getList().remove(u);
            mUsers.remove(u);
            notifyItemRemoved(position);
            return u;
        }
    }

    private void setUsersFromFilter(List<ProfileViewModel> users) {   //only for filter use
        if (AppUtils.compareLists(users, mUsers)) return;
        synchronized (this) {
            mUsers = users;
        }
    }

    public void setSort(Sort sort) {
        mSort = sort;
    }

    public Sort getSort() {
        return mSort;
    }

    public void setOnItemClickListener(OnItemClickListener cLickListener) {
        this.mClickListener = cLickListener;
    }

    public boolean isMovable() {
        return isMovable;
    }

    public void setMovable(boolean movable) {
        isMovable = movable;
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: ViewHolder
    public static class UserViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private ItemUserListBinding mBinding;

        public UserViewHolder(View itemView, OnStartDragListener dragListener, OnItemClickListener listener) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);

/*
            mBinding.handle.setOnTouchListener((v, event) -> {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    dragListener.onStartDrag(this);
                }
                return false;
            });
*/

            mBinding.btnViewProfile.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProfileClick(getAdapterPosition());
                }
            });

            mBinding.btnMoreInfo.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMoreClick(getAdapterPosition());
                }
            });

            mBinding.btnLike.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onLikeClick(this, getAdapterPosition());
                }
            });
        }

        public ItemUserListBinding getBinding() {
            return mBinding;
        }

        @Override
        public void onItemSelected() {
        }

        @Override
        public void onItemClear() {
            getBinding().getProfile().setMoving(false);
        }
    }
//endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Filter
    public static class CustomUserListFilter extends CustomFilter<UsersAdapter> {

        private final List<ProfileViewModel> mList;

        public List<ProfileViewModel> getList() {
            return mList;
        }

        public CustomUserListFilter(UsersAdapter mAdapter) {
            super(mAdapter);
            this.mList = mAdapter.getItems();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<ProfileViewModel> tempList = new ArrayList<>();

            if (constraint.length() != 0) {
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (final ProfileViewModel s : mList) {
                    if (s.getFullName().toLowerCase().contains(filterPattern) ||
                            s.getHometask().contains(filterPattern)) {
                        tempList.add(s);
                    }
                }
            } else {
                tempList = mList;
            }
            mAdapter.setUsersFromFilter(tempList);
            return null;
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}

