package com.softdesign.devintensive.ui.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import com.softdesign.devintensive.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "unchecked"})
public class RecyclerBindingAdapter<T> extends RecyclerView.Adapter<RecyclerBindingAdapter.BindingHolder> implements Filterable {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private int holderLayout, variableId;
    private List<T> items = new ArrayList<>();
    private OnItemClickListener mItemClickListener;
    private CustomBindingFilter mFilter;

    private int lastAnimatedPosition = -1;

    private boolean delayEnterAnimation = true;

    //region :::::::::::::::::::::::::::::::::::::::::: Adapter
    public RecyclerBindingAdapter(int holderLayout, int variableId, List<T> items) {
        this.holderLayout = holderLayout;
        this.variableId = variableId;
        this.items = items;
    }

    public RecyclerBindingAdapter(int holderLayout, int variableId, List<T> items, OnItemClickListener onItemClickListener) {
        this.mItemClickListener = onItemClickListener;
        this.holderLayout = holderLayout;
        this.variableId = variableId;
        this.items = items;
    }

    @Override
    public RecyclerBindingAdapter.BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(holderLayout, parent, false);
        return new BindingHolder(v, mItemClickListener);
    }

    @Override
    public void onBindViewHolder(RecyclerBindingAdapter.BindingHolder holder, int position) {
        final T item = items.get(position);
        holder.getBinding().setVariable(variableId, item);
    }

    //endregion :::::::::::::::::::::::::::::::::::::::::: Adapter

    //region :::::::::::::::::::::::::::::::::::::::::: Base Methods
    @Override
    public int getItemCount() {
        return items.size();
    }

    public List<T> getItems() {
        return items;
    }

    @Override
    public Filter getFilter() {
        return mFilter;
    }
    //endregion :::::::::::::::::::::::::::::::::::::::::: Base Methods

    //region :::::::::::::::::::::::::::::::::::::::::: Adapter Utils
    public void setUsersFromDB(List<T> userEntities) {
        synchronized (this) {
            if (items.size() > 0) {
                List<String> newItems = new ArrayList<String>() {{
                    for (T u : userEntities) add(u.toString());
                }};
                List<String> currentItems = new ArrayList<String>() {{
                    for (T u : items) add(u.toString());
                }};

                for (int i = 0; i < currentItems.size(); i++) {
                    if (!newItems.contains(currentItems.get(i))) {
                        T model = items.get(i);
                        if (items.remove(model)) notifyItemRemoved(i);
                    }
                }

                for (int i = 0; i < newItems.size(); i++) {
                    if (!currentItems.contains(newItems.get(i))) {
                        items.add(userEntities.get(i));
                        notifyItemInserted(items.size() - 1);
                    }
                }
            } else {
                items.addAll(userEntities);
                notifyItemRangeInserted(0, items.size());
            }
            if (mFilter != null) mFilter.setList(userEntities);
        }
    }

    public void setListFromFilter(List<T> newList) {   //only for mFilter use
        if (AppUtils.compareLists(newList, items)) return;
        synchronized (this) {
            items = newList;
        }
    }

    public void setFilter(CustomBindingFilter filter) {
        mFilter = filter;
    }

    public void setOnItemClickListener(OnItemClickListener cLickListener) {
        this.mItemClickListener = cLickListener;
    }
    //endregion :::::::::::::::::::::::::::::::::::::::::: Adapter Utils

    //region :::::::::::::::::::::::::::::::::::::::::: ViewHolder
    public static class BindingHolder extends RecyclerView.ViewHolder {
        private ViewDataBinding binding;

        public BindingHolder(View view, OnItemClickListener clickListener) {
            super(view);
            binding = DataBindingUtil.bind(view);
            binding.getRoot().setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(getAdapterPosition());
                }
            });
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }
    //endregion :::::::::::::::::::::::::::::::::::::::::: ViewHolder

    //region :::::::::::::::::::::::::::::::::::::::::: Filter
    public static abstract class CustomBindingFilter<S> extends CustomFilter<RecyclerBindingAdapter> {

        private final List<S> mList;

        public CustomBindingFilter(RecyclerBindingAdapter mAdapter) {
            super(mAdapter);
            mList = mAdapter.getItems();
        }

        public List<S> getList() {
            return mList;
        }

        public void setList(List<S> list) {
            mList.clear();
            for (S o : list) {
                mList.add(o);
            }
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
