package com.softdesign.devintensive.ui.adapters;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.databinding.ItemUserListBinding;
import com.softdesign.devintensive.ui.callbacks.OnStartDragListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements Filterable, ItemTouchHelperAdapter {

    public interface OnItemCLickListener {
        void onItemClick(int position);
    }

    private static final EventBus BUS = EventBus.getDefault();
    private final CustomUserListFilter mFilter;
    private final OnStartDragListener mDragStartListener;

    private List<UserDTO> mUsers;
    private OnItemCLickListener mViewClickListener;
    private OnItemCLickListener mLikesClickListener;

    //region Adapter
    @Override
    public UsersAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);

        return new UserViewHolder(convertView, mViewClickListener, mLikesClickListener);
    }

    public UsersAdapter(List<UserEntity> users, OnStartDragListener dragStartListener) {
        mUsers = new ArrayList<UserDTO>() {{
            for (UserEntity u : users) {
                add(new UserDTO(u));
            }
        }};
        mDragStartListener = dragStartListener;
        mFilter = new CustomUserListFilter(UsersAdapter.this);
    }

    public UsersAdapter(List<UserEntity> users, OnStartDragListener dragStartListener, OnItemCLickListener viewClickListener, OnItemCLickListener likesClickListener) {
        mUsers = new ArrayList<UserDTO>() {{
            for (UserEntity u : users) {
                add(new UserDTO(u));
            }
        }};
        mDragStartListener = dragStartListener;
        mFilter = new CustomUserListFilter(UsersAdapter.this);
        mViewClickListener = viewClickListener;
        mLikesClickListener = likesClickListener;
    }

    public void setOnViewBtnCLickListener(@Nullable OnItemCLickListener itemCLickListener) {
        mViewClickListener = itemCLickListener;
    }

    public void setOnLikeBtnCLickListener(@Nullable OnItemCLickListener itemCLickListener) {
        mLikesClickListener = itemCLickListener;
    }

    @Override
    public void onBindViewHolder(final UsersAdapter.UserViewHolder holder, int position) {

        holder.getBinding().setProfile(mUsers.get(position));

        holder.getBinding().handle.setOnTouchListener((v, event) -> {
            if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                mDragStartListener.onStartDrag(holder);
            }
            return false;
        });
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
        UserDTO u = mUsers.get(position);
        BUS.post(new ChangeUserInternalId(u.getRemoteId(), null));
        mFilter.getList().remove(u);
        mUsers.remove(u);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        BUS.post(new ChangeUserInternalId(mUsers.get(fromPosition).getRemoteId(), mUsers.get(toPosition).getRemoteId()));
        Collections.swap(mUsers, fromPosition, toPosition);
        Collections.swap(mFilter.getList(), fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public List<UserDTO> getUsers() {
        return mUsers;
    }

    private void setUsers(List<UserDTO> users) {
        mUsers = users;
    }

    public class ChangeUserInternalId {
        final String firstUserRemoteId;
        final String secondUserRemoteId;

        public ChangeUserInternalId(String firstUserRemoteId, String secondUserRemoteId) {
            this.firstUserRemoteId = firstUserRemoteId;
            this.secondUserRemoteId = secondUserRemoteId;
        }

        public String getFirstUserRemoteId() {
            return firstUserRemoteId;
        }

        public String getSecondUserRemoteId() {
            return secondUserRemoteId;
        }
    }
    //endregion

    //region ViewHolder
    public static class UserViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {

        private ItemUserListBinding mBinding;

        @SuppressWarnings("deprecation")
        public UserViewHolder(View itemView, OnItemCLickListener viewClickListener, OnItemCLickListener likesClickListener) {
            super(itemView);
            mBinding = DataBindingUtil.bind(itemView);

            mBinding.listMoreInfoBtn.setOnClickListener(v -> {
                if (viewClickListener != null) {
                    viewClickListener.onItemClick(getAdapterPosition());
                }
            });

            mBinding.listLikeBtn.setOnClickListener(v -> {
                if (likesClickListener != null) {
                    likesClickListener.onItemClick(getAdapterPosition());
                }
            });

            mBinding.listUnlikeBtn.setOnClickListener(v -> {
                if (likesClickListener != null) {
                    likesClickListener.onItemClick(getAdapterPosition());
                }
            });
        }

        public ItemUserListBinding getBinding() {
            return mBinding;
        }

        @Override
        public void onItemSelected() {
            getBinding().getProfile().setMoving(true);
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            getBinding().getProfile().setMoving(false);
            itemView.setBackgroundColor(0);
        }
    }
    //endregion

    //region Filter
    public static class CustomUserListFilter extends Filter {

        private final UsersAdapter mAdapter;
        private final List<UserDTO> mList;

        public List<UserDTO> getList() {
            return mList;
        }

        public CustomUserListFilter(UsersAdapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
            this.mList = mAdapter.getUsers();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<UserDTO> tempList = new ArrayList<>();

            if (constraint.length() != 0) {
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (final UserDTO s : mList) {
                    if (s.getFullName().toLowerCase().contains(filterPattern) ||
                            s.getHometask().contains(filterPattern)) {
                        tempList.add(s);
                    }
                }
            } else {
                tempList = mList;
            }
            mAdapter.setUsers(tempList);
            return null;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            this.mAdapter.notifyDataSetChanged();
        }
    }
    //endregion
}

