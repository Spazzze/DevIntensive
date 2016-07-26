package com.softdesign.devintensive.data.binding;

import android.databinding.BindingAdapter;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.CustomGlideModule;
import com.softdesign.devintensive.utils.UserInfoTextWatcher;

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

    @SuppressWarnings("unchecked")
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
}
