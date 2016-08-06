package com.softdesign.devintensive.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.widget.Filter;

public abstract class CustomFilter<S extends RecyclerView.Adapter> extends Filter {

    public final S mAdapter;

    public CustomFilter(S mAdapter) {
        super();
        this.mAdapter = mAdapter;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        this.mAdapter.notifyDataSetChanged();
    }
}