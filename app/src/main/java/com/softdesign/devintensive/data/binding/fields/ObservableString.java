package com.softdesign.devintensive.data.binding.fields;

import android.databinding.BaseObservable;
import android.databinding.BindingConversion;

public class ObservableString extends BaseObservable {

    private String value = "";

    public ObservableString(String value) {
        this.value = value;
    }

    public ObservableString() {
    }

    public String get() {
        return value != null ? value : "";
    }

    public void set(String value) {
        if (!equals(this.value, value)) {
            this.value = value;
            notifyChange();
        }
    }

    public boolean isEmpty() {
        return value == null || value.isEmpty();
    }

    public void clear() {
        set(null);
    }

    @BindingConversion
    public static String convertToString(
            ObservableString observableString) {
        return observableString.get();
    }

    public static boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}