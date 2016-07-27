package com.softdesign.devintensive.data.binding;

import android.databinding.BindingAdapter;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.Pair;
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

    @SuppressWarnings("unchecked")
    @BindingAdapter("entries")
    public static void loadRepositories(RecyclerView recyclerView, List<RepoViewModel> list) {

        Pair<List<RepoViewModel>, RecyclerBindingAdapter<RepoViewModel>> pair = (Pair) recyclerView.getTag(R.id.repo_recycleView);

        if (pair == null || pair.first.size() != list.size()) {
            if (pair == null) {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
                linearLayoutManager.setAutoMeasureEnabled(true);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
            }
            RecyclerBindingAdapter<RepoViewModel> adapter = new RecyclerBindingAdapter<>(
                    R.layout.item_repositories_list,
                    BR.repoItem,
                    list,
                    (position -> {
                        String uri = list.get(position).getRepoUri();
                        AppUtils.openWebPage(DevIntensiveApplication.getContext(), "https://" + uri);
                    }));
            recyclerView.setTag(R.id.repo_recycleView, new Pair<>(list, adapter));
            recyclerView.swapAdapter(adapter, false);
        }
    }
}
