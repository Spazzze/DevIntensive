package com.softdesign.devintensive.data.binding;

import android.databinding.BindingAdapter;
import android.net.Uri;
import android.widget.ImageView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.CustomGlideModule;

public class BindingAdapters {
    private BindingAdapters() {
        throw new AssertionError();
    }

    @BindingAdapter("imageUrl")
    public static void loadImage(ImageView view, Uri url) {
        CustomGlideModule.loadImage(url.toString(), R.drawable.user_bg, R.drawable.user_bg, view);
    }

 /*   @SuppressWarnings("unchecked")
    @BindingAdapter("android:text")
    public static void bindEditText(EditText view,
                                    final ObservableString observableString) {
        Pair<ObservableString, TextChangeListener> pair = (Pair) view.getTag(R.id.bound_observable);

        if (pair == null || pair.first != observableString) {
            if (pair != null) view.removeTextChangedListener(pair.second);

            TextChangeListener watcher = new TextChangeListener(
                    (s, start, before, count) -> observableString.set(s.toString()));

            view.setTag(R.id.bound_observable, new Pair<>(observableString, watcher));
            view.addTextChangedListener(watcher);
        }
        String newValue = observableString.get();
        if (!view.getText().toString().equals(newValue))
            view.setText(newValue);
    }*/

/*    @BindingAdapter("app:onClick")
    public static void bindOnClick(View view, final Runnable runnable) {
        view.setOnClickListener(v -> runnable.run());
    }*/
}
