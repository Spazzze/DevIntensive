package com.softdesign.devintensive.data.binding;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class RecyclerBindingAdapter<T> extends RecyclerView.Adapter<RecyclerBindingAdapter.BindingHolder> {

    private int holderLayout, variableId;
    private List<T> items = new ArrayList<>();
    private BindingHolder.OnItemClickListener mItemClickListener;

    public RecyclerBindingAdapter(int holderLayout, int variableId, List<T> items, BindingHolder.OnItemClickListener onItemClickListener) {
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

    @Override
    public int getItemCount() {
        return items.size();
    }

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

        public interface OnItemClickListener {
            void onItemClick(int position);
        }
    }
}
