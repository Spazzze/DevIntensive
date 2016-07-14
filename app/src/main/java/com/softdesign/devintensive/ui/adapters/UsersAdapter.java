package com.softdesign.devintensive.ui.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.ui.view.AspectRatioImageView;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    Context mContext;
    List<UserListRes> mUsers;

    @Override
    public UsersAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.item_user_list, parent, false);
        return new UserViewHolder(convertView);
    }

    public UsersAdapter(List<UserListRes> users) {
        mUsers = users;
    }

    @Override
    public void onBindViewHolder(UsersAdapter.UserViewHolder holder, int position) {
        UserListRes user = mUsers.get(position);
        Drawable ph = mContext.getResources().getDrawable(R.drawable.user_bg);
        if (ph != null)
            Picasso.with(mContext)
                    .load(user.getPublicInfo().getPhoto())
                    .placeholder(ph)
                    .error(ph)
                    .fit()
                    .centerCrop()
                    .into(holder.mUserPhoto);
        else
            Picasso.with(mContext)
                    .load(user.getPublicInfo().getPhoto())
                    .fit()
                    .centerCrop()
                    .into(holder.mUserPhoto);

        holder.mFullName.setText(user.getFullName());
        holder.mRating.setText(user.getProfileValues().getRating());
        holder.mCodeLines.setText(user.getProfileValues().getLinesCode());
        holder.mProjects.setText(user.getProfileValues().getProjects());

        if (user.getPublicInfo().getBio() == null || user.getPublicInfo().getBio().isEmpty()) {
            holder.mBio.setVisibility(View.GONE);
        } else {
            holder.mBio.setVisibility(View.VISIBLE);
            holder.mBio.setText(user.getPublicInfo().getBio());
        }
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        protected AspectRatioImageView mUserPhoto;
        protected TextView mFullName, mRating, mCodeLines, mProjects, mBio;
        protected Button mButton;

        public UserViewHolder(View itemView) {
            super(itemView);

            mUserPhoto = (AspectRatioImageView) itemView.findViewById(R.id.list_userPhoto);
            mFullName = (TextView) itemView.findViewById(R.id.list_user_full_name_text);
            mRating = (TextView) itemView.findViewById(R.id.list_rating_count);
            mCodeLines = (TextView) itemView.findViewById(R.id.list_codeLines_count);
            mProjects = (TextView) itemView.findViewById(R.id.list_projects_count);
            mBio = (TextView) itemView.findViewById(R.id.list_bio_txt);
            mButton = (Button) itemView.findViewById(R.id.list_more_info_btn);
        }
    }
}
