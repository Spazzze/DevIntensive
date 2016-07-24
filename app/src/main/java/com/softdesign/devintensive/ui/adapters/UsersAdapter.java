package com.softdesign.devintensive.ui.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.CustomGlideModule;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.ui.callbacks.OnStartDragListener;
import com.softdesign.devintensive.ui.view.elements.AspectRatioImageView;
import com.softdesign.devintensive.utils.UiHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements Filterable, ItemTouchHelperAdapter {

    private List<UserEntity> mUsers;
    private final CustomUserListFilter mFilter;
    private final UserViewHolder.CustomClickListener mCustomClickListener;
    private final OnStartDragListener mDragStartListener;
    private static final EventBus BUS = EventBus.getDefault();

    //region Adapter
    @Override
    public UsersAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_list, parent, false);

        return new UserViewHolder(convertView, mCustomClickListener);
    }

    public UsersAdapter(List<UserEntity> users, OnStartDragListener dragStartListener, UserViewHolder.CustomClickListener customClickListener) {
        mUsers = users;
        mDragStartListener = dragStartListener;
        mCustomClickListener = customClickListener;
        mFilter = new CustomUserListFilter(UsersAdapter.this);
    }

    @Override
    public void onBindViewHolder(final UsersAdapter.UserViewHolder holder, int position) {

        final UserEntity user = mUsers.get(position);

        CustomGlideModule.loadImage(user.getPhoto(), holder.mPlaceHolder, holder.mPlaceHolder, holder.mUserPhoto);

        holder.mFullName.setText(user.getFullName());
        holder.mRating.setText(String.valueOf(user.getRating()));
        holder.mCodeLines.setText(String.valueOf(user.getCodeLines()));
        holder.mProjects.setText(String.valueOf(user.getProjects()));
        holder.mHomeTask.setText(user.getHomeTask());

        if (UiHelper.isEmptyOrNull(user.getBio())) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getBio().trim());
        }

        holder.mHandleView.setOnTouchListener((v, event) -> {
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
        UserEntity u = mUsers.get(position);
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

    public List<UserEntity> getUsers() {
        return mUsers;
    }

    private void setUsers(List<UserEntity> users) {
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
    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ItemTouchHelperViewHolder {

        private final AspectRatioImageView mUserPhoto;
        private final TextView mFullName;
        private final TextView mRating;
        private final TextView mCodeLines;
        private final TextView mProjects;
        private final TextView mBio;
        private final TextView mHomeTask;
        private final Button mButton;
        private final CustomClickListener mClickListener;
        private final ImageView mHandleView;
        private Drawable mPlaceHolder;

        @SuppressWarnings("deprecation")
        public UserViewHolder(View itemView, CustomClickListener customClickListener) {
            super(itemView);

            mClickListener = customClickListener;
            mUserPhoto = (AspectRatioImageView) itemView.findViewById(R.id.list_userPhoto);
            mFullName = (TextView) itemView.findViewById(R.id.list_user_full_name_text);
            mRating = (TextView) itemView.findViewById(R.id.list_rating_count);
            mCodeLines = (TextView) itemView.findViewById(R.id.list_codeLines_count);
            mProjects = (TextView) itemView.findViewById(R.id.list_projects_count);
            mBio = (TextView) itemView.findViewById(R.id.list_bio_txt);
            mHomeTask = (TextView) itemView.findViewById(R.id.cur_homeTask_txt);
            mButton = (Button) itemView.findViewById(R.id.list_more_info_btn);
            mHandleView = (ImageView) itemView.findViewById(R.id.handle);

            mPlaceHolder = mUserPhoto.getContext().getResources().getDrawable(R.drawable.user_bg);
            if (mPlaceHolder == null) {
                mPlaceHolder = mUserPhoto.getContext().getResources().getDrawable(android.R.drawable.screen_background_dark);
            }

            mButton.setOnClickListener(this);
        }

        @Override
        public String toString() {
            return String.format("%s '%s'", super.toString(), mFullName.getText());
        }

        @Override
        public void onClick(View v) {

            if (mClickListener != null) {
                mClickListener.onUserItemClickListener(getAdapterPosition());
            }
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }

        public interface CustomClickListener {
            void onUserItemClickListener(int position);
        }
    }
    //endregion

    //region Filter
    public static class CustomUserListFilter extends Filter {

        private final UsersAdapter mAdapter;
        private final List<UserEntity> mList;

        public List<UserEntity> getList() {
            return mList;
        }

        public CustomUserListFilter(UsersAdapter mAdapter) {
            super();
            this.mAdapter = mAdapter;
            this.mList = mAdapter.getUsers();
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<UserEntity> tempList = new ArrayList<>();

            if (constraint.length() != 0) {
                final String filterPattern = constraint.toString().toLowerCase().trim();
                for (final UserEntity s : mList) {
                    if (s.getFullName().toLowerCase().contains(filterPattern) ||
                            s.getHomeTask().contains(filterPattern)) {
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

