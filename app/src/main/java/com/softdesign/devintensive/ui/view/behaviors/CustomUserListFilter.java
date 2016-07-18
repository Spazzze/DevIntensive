package com.softdesign.devintensive.ui.view.behaviors;

import android.widget.Filter;

import com.softdesign.devintensive.data.network.api.res.UserListRes;
import com.softdesign.devintensive.ui.adapters.UsersAdapter;

import java.util.ArrayList;
import java.util.List;

public class CustomUserListFilter extends Filter {

    private UsersAdapter mAdapter;
    private List<UserListRes> mList;

    public CustomUserListFilter(UsersAdapter mAdapter) {
        super();
        this.mAdapter = mAdapter;
        mList = mAdapter.getUsers();
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results = new FilterResults();
        List<UserListRes> tempList = new ArrayList<>();

        if (constraint.length() != 0) {
            final String filterPattern = constraint.toString().toLowerCase().trim();
            for (final UserListRes s : mList) {
                if (s.getFullName().toLowerCase().contains(filterPattern) ||
                        s.getProfileValues().getHomeTask().contains(filterPattern)) {
                    tempList.add(s);
                }
            }
        } else {
            tempList = mList;
        }
        mAdapter.setUsers(tempList);
        results.values = tempList;
        results.count = tempList.size();
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        this.mAdapter.notifyDataSetChanged();
    }
}