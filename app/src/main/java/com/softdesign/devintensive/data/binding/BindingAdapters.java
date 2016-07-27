package com.softdesign.devintensive.data.binding;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.CustomGlideModule;
import com.softdesign.devintensive.data.storage.viewmodels.RepoViewModel;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.softdesign.devintensive.utils.UserInfoTextWatcher;

import java.util.List;

public class BindingAdapters {
    private static final Context CONTEXT = DevIntensiveApplication.getContext();

    private BindingAdapters() {
        throw new AssertionError();
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {
        CustomGlideModule.loadImage(url, R.drawable.user_bg, R.drawable.user_bg, view);
    }

    @BindingAdapter("onClick")
    public static void bindOnClick(View view, final Runnable runnable) {
        view.setOnClickListener(v -> runnable.run());
    }

    @BindingAdapter("android:enabled")
    public static void removeError(EditText editText, boolean isEnabled) {
        Boolean tag = (Boolean) editText.getTag(R.id.et_errorHandler);
        if (tag == null || tag != isEnabled) {
            if (!isEnabled) {
                TextInputLayout parent = (TextInputLayout) editText.getParent();
                if (parent != null) {
                    parent.setErrorEnabled(false);
                    parent.setError(null);
                }
            }
            editText.setTag(R.id.et_errorHandler, isEnabled);
            editText.setEnabled(isEnabled);
        }
    }

    @BindingAdapter("userInfoTextWatcher")
    public static void addUserInfoTextWatcher(EditText editText, boolean isCanBeEdit) {
        if (!isCanBeEdit) return;
        UserInfoTextWatcher watcher = (UserInfoTextWatcher) editText.getTag(R.id.et_TextWatcher);
        if (watcher == null) {
            TextInputLayout parent = (TextInputLayout) editText.getParent();
            if (parent != null) {
                watcher = new UserInfoTextWatcher(editText, parent);
                editText.setTag(R.id.et_TextWatcher, watcher);
                editText.addTextChangedListener(watcher);
            } else {
                throw new IllegalArgumentException("Parent of this editText should be TextInputLayout");
            }
        }
    }

    @BindingAdapter("entries")
    public static void loadRepositories(RecyclerView recyclerView, List<RepoViewModel> list) {
        Integer savedSize = (Integer) recyclerView.getTag(R.id.repo_recycleView);
        if (savedSize == null || savedSize != list.size()) {
            if (savedSize == null) {
                LinearLayoutManager manager = new LinearLayoutManager(recyclerView.getContext());
                manager.setAutoMeasureEnabled(true);
                recyclerView.setLayoutManager(manager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            }
            RecyclerBindingAdapter<RepoViewModel> adapter = new RecyclerBindingAdapter<>(
                    R.layout.item_repositories_list,
                    BR.repoItem,
                    list,
                    (position -> {
                        if (!list.get(position).isEnabled()) {
                            AppUtils.openWebPage(CONTEXT, "https://" + list.get(position).getRepoUri());
                        }
                    }));
            recyclerView.setTag(R.id.repo_recycleView, list.size());
            recyclerView.swapAdapter(adapter, true);
        }
    }
}
