package com.softdesign.devintensive.data.storage.viewmodels;

import android.databinding.Bindable;

public class MainViewModel extends BaseViewModel {

    private String mFullName;
    private String mEmail;

    //region <<<<<<<<<<<<<<<GETTERS>>>>>>>>>>>>>>>
    @Bindable
    public String getFullName() {
        return mFullName;
    }

    @Bindable
    public String getEmail() {
        return mEmail;
    }
    //endregion

    //region <<<<<<<<<<<<<<<<<<<<SETTERS>>>>>>>>>>>>>>>>>>>>
    public void setFullName(String fullName) {
        mFullName = fullName;
    }

    public void setEmail(String email) {
        mEmail = email;
    }
    //endregion
}
