package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.ui.view.behaviors.CustomUserListFilter;
import com.softdesign.devintensive.ui.view.elements.AspectRatioImageView;
import com.softdesign.devintensive.utils.ConstantManager;
import com.squareup.picasso.Picasso;

import java.util.List;

import static com.softdesign.devintensive.utils.UiHelper.getScreenWidth;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements Filterable {

    private static final String TAG = ConstantManager.TAG_PREFIX + "UsersAdapter";

    private Context mContext;
    private List<UserListRes> mUsers;
    private UserViewHolder.CustomClickListener mCustomClickListener;
    private int mHeight;
    private int mWidth;
    private Drawable mPlaceHolder;
    private CustomUserListFilter mFilter;

    @SuppressWarnings("deprecation")
    @Override
    public UsersAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        mContext = parent.getContext();
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user_list, parent, false);

        mWidth = getScreenWidth();
        mHeight = (int) (mWidth / ConstantManager.ASPECT_RATIO_16_9);
        //инициализирую плейсхолдер при создании, заранее избавляясь от возможного NullPointerException
        // и от ненужной операции загрузки плейсхолдера в onBindViewHolder, стало работать побыстрее
        mPlaceHolder = mContext.getResources().getDrawable(R.drawable.user_bg);
        if (mPlaceHolder == null) {
            mPlaceHolder = mContext.getResources().getDrawable(android.R.drawable.screen_background_dark);
        }

        return new UserViewHolder(convertView, mCustomClickListener);
    }

    public UsersAdapter(List<UserListRes> users, UserViewHolder.CustomClickListener customClickListener) {
        mUsers = users;
        mCustomClickListener = customClickListener;
        mFilter = new CustomUserListFilter(UsersAdapter.this);
    }

    @Override
    public void onBindViewHolder(UsersAdapter.UserViewHolder holder, int position) {

        UserListRes user = mUsers.get(position);

        if (user.getPublicInfo().getPhoto() != null && !user.getPublicInfo().getPhoto().isEmpty()) {
            Picasso.with(mContext)
                    .load(user.getPublicInfo().getPhoto())
                    .placeholder(mPlaceHolder)
                    .error(mPlaceHolder)
                    .resize(mWidth, mHeight)
                    .onlyScaleDown()
                    .centerCrop()
                    .into(holder.mUserPhoto);
        }

        holder.mFullName.setText(user.getFullName());
        holder.mRating.setText(user.getProfileValues().getRating());
        holder.mCodeLines.setText(user.getProfileValues().getLinesCode());
        holder.mProjects.setText(user.getProfileValues().getProjects());
        holder.mHomeTask.setText(user.getProfileValues().getHomeTask());

        if (user.getPublicInfo().getBio() == null && user.getPublicInfo().getBio().trim().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getPublicInfo().getBio().trim());
        }
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }

    public List<UserListRes> getUsers() {
        return mUsers;
    }

    public void setUsers(List<UserListRes> users) {
        mUsers = users;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private AspectRatioImageView mUserPhoto;
        private TextView mFullName, mRating, mCodeLines, mProjects, mBio, mHomeTask;
        private Button mButton;
        private CustomClickListener mClickListener;

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

        public interface CustomClickListener {
            void onUserItemClickListener(int position);
        }
    }
}

