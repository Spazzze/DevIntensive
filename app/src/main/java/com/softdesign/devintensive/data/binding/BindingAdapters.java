package com.softdesign.devintensive.data.binding;

import android.content.Context;
import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.providers.CustomGlideModule;
import com.softdesign.devintensive.data.storage.viewmodels.RepoViewModel;
import com.softdesign.devintensive.ui.adapters.RecyclerBindingAdapter;
import com.softdesign.devintensive.ui.view.animations.Animations;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;
import com.softdesign.devintensive.utils.UserInfoTextWatcher;

import java.util.List;

@SuppressWarnings({"unchecked", "unused"})
public class BindingAdapters {

    private static final Context CONTEXT = DevIntensiveApplication.getContext();
    public static final String TAG = Const.TAG_PREFIX + "BindingAdapters";

    private BindingAdapters() {
        throw new AssertionError();
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Overridden defaults
    @BindingAdapter("android:layout_height")
    public static void setLayoutHeight(View view, float height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("layout_behavior")
    public static void setLayoutBehavior(View view, CoordinatorLayout.Behavior behavior) {
        if (behavior == null) return;
        CoordinatorLayout.Behavior tag = (CoordinatorLayout.Behavior) view.getTag(R.id.behavior_tag);
        if (tag == null || !AppUtils.equals(behavior, tag)) {
            CoordinatorLayout.LayoutParams cl = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
            if (cl == null) return;
            cl.setBehavior(behavior);
            view.setLayoutParams(cl);
            view.setTag(R.id.behavior_tag, behavior);
        }
    }

    @BindingAdapter("android:enabled")
    public static void removeError(EditText editText, boolean isEnabled) {
        Boolean tag = (Boolean) editText.getTag(R.id.et_errorHandler_tag);
        if (tag == null || tag != isEnabled) {
            if (!isEnabled) {
                TextInputLayout parent = (TextInputLayout) editText.getParent();
                if (parent != null) {
                    parent.setErrorEnabled(false);
                    parent.setError(null);
                }
            }
            editText.setTag(R.id.et_errorHandler_tag, isEnabled);
            editText.setEnabled(isEnabled);
        }
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Custom
    @BindingAdapter("imageMasked")
    public static void imageMasked(ImageView view, String url) {

        String tag = (String) view.getTag(R.id.imageUrl_tag);

        if (tag == null || !AppUtils.equals(url, tag)) {
            CustomGlideModule.loadMaskedImage(url, view, R.drawable.dw_mask_list);
            view.setTag(R.id.imageUrl_tag, url);
        }
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, String url) {

        String tag = (String) view.getTag(R.id.imageUrl_tag);

        if (tag == null || !AppUtils.equals(url, tag)) {
            CustomGlideModule.loadImage(url, view);
            view.setTag(R.id.imageUrl_tag, url);
        }
    }

    @BindingAdapter({"imageUrl", "placeholder"})
    public static void loadImage(ImageView view, String url, Drawable placeholder) {

        Pair<String, Drawable> pair = (Pair) view.getTag(R.id.imageUrl_tag);

        if (pair == null || !AppUtils.equals(url, pair.first) || !AppUtils.equals(pair.second, placeholder)) {
            CustomGlideModule.loadImage(url, placeholder, view);
            view.setTag(R.id.imageUrl_tag, new Pair<>(url, placeholder));
        }
    }

    @BindingAdapter("resize_by_aspect")
    public static void resizeImage(ImageView view, float aspectRatio) {
        int width = view.getMeasuredWidth();
        int height = (int) (width / aspectRatio);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("roundedImage")
    public static void setRoundedImage(ImageView view, String url) {
        String tag = (String) view.getTag(R.id.avatar_tag);
        if (tag != null && AppUtils.isEmptyOrNull(url)) return;
        if (tag == null || !AppUtils.equals(url, tag)) {
            CustomGlideModule.loadRoundedImage(url, R.drawable.ic_account_circle_white, R.drawable.ic_account_circle_white, view);
            view.setTag(R.id.avatar_tag, url);
        }
    }

    @BindingAdapter("onClick")
    public static void bindOnClick(View view, final Runnable runnable) {
        view.setOnClickListener(v -> runnable.run());
    }

    @BindingAdapter("userInfoTextWatcher")
    public static void addUserInfoTextWatcher(EditText editText, boolean isCanBeEdit) {
        if (!isCanBeEdit) return;
        UserInfoTextWatcher watcher = (UserInfoTextWatcher) editText.getTag(R.id.et_TextWatcher_tag);
        if (watcher == null) {
            TextInputLayout parent = (TextInputLayout) editText.getParent();
            if (parent != null) {
                watcher = new UserInfoTextWatcher(editText, parent);
                editText.setTag(R.id.et_TextWatcher_tag, watcher);
                editText.addTextChangedListener(watcher);
            } else {
                throw new IllegalArgumentException("Parent of this editText should be TextInputLayout");
            }
        }
    }

    @BindingAdapter("entries")
    public static void loadRepositories(RecyclerView recyclerView, List<RepoViewModel> list) {
        if (AppUtils.isEmptyOrNull(list)) return;
        Integer savedSize = (Integer) recyclerView.getTag(R.id.repo_recycleView_tag);
        if (savedSize == null || savedSize != list.size()) {
            if (savedSize == null) {
                LinearLayoutManager manager = new LinearLayoutManager(recyclerView.getContext());
                recyclerView.setLayoutManager(manager);
                RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
                if (animator instanceof SimpleItemAnimator) {
                    ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
                }
            }
            RecyclerBindingAdapter<RepoViewModel> adapter = new RecyclerBindingAdapter<>(
                    R.layout.item_repositories_list,
                    BR.repoItem,
                    list,
                    (position -> {
                        if (!list.get(position).isEnabled()) {
                            AppUtils.openWebPage(CONTEXT, list.get(position).getRepoUri());
                        }
                    }));
            recyclerView.setTag(R.id.repo_recycleView_tag, list.size());
            recyclerView.swapAdapter(adapter, false);
        }
    }

    @BindingAdapter("changeFABColor")
    public static void changeFABColor(FloatingActionButton fab, @ColorInt int id) {
        if (id == 0) return;

        Integer tag = (Integer) fab.getTag(R.id.fab_color_tag);

        if (tag == null || id != tag) {
            fab.setBackgroundTintList(ColorStateList.valueOf(id));
            fab.setTag(R.id.fab_color_tag, tag);
        }
    }

    //endregion :::::::::::::::::::::::::::::::::::::::::: Custom

    //region :::::::::::::::::::::::::::::::::::::::::: Animations
    @BindingAdapter("switchText")
    public static void switchText(TextSwitcher switcher, String text) {
        if (text == null) return;

        String tag = (String) switcher.getTag(R.id.animText_tag);

        if (tag == null || !AppUtils.equals(tag, text)) {
            switcher.setText(text);
            switcher.setTag(R.id.animText_tag, tag);
        }
    }

    @BindingAdapter("animateFabAppearance")
    public static void animateFabAppearance(FloatingActionButton fab, boolean isNeeded) {
        Pair<Boolean, Float> pair = (Pair) fab.getTag(R.id.fab_appearance_tag);

        if (pair == null || pair.first != isNeeded) {
            float neededTranslation = isNeeded ? 0.0f : 2 * CONTEXT.getResources().getDimensionPixelOffset(R.dimen.size_medium_56);
            Animations.animateFabAppearance(fab, neededTranslation);
            fab.setTag(R.id.fab_appearance_tag, new Pair<>(isNeeded, neededTranslation));
        }
    }
    //endregion :::::::::::::::::::::::::::::::::::::::::: Animations
}


