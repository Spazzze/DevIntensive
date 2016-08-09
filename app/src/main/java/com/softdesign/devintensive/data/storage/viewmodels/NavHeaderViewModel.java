package com.softdesign.devintensive.data.storage.viewmodels;

import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.utils.AppUtils;

public class NavHeaderViewModel extends BaseViewModel implements Parcelable {

    private String mNavFullName;
    private String mNavEmail;
    private String mUserAvatarUri;
    private boolean isEditing = false;

    public NavHeaderViewModel(ProfileViewModel model) {
        mNavFullName = model.getFullName();
        mNavEmail = model.getEmail();
        mUserAvatarUri = model.getUserAvatarUri();
        isEditing = model.isEditMode();
    }

    public void updateValues(ProfileViewModel model) {
        if (model == null) return;

        if (!AppUtils.equals(this.mNavFullName, model.getFullName())) {
            setNavFullName(model.getFullName());
        }

        if (!AppUtils.equals(this.mNavEmail, model.getEmail())) {
            setNavEmail(model.getEmail());
        }

        if (!AppUtils.equals(this.mUserAvatarUri, model.getUserAvatarUri())) {
            setUserAvatarUri(model.getUserAvatarUri());
        }

        if (isEditing() != model.isEditMode()) {
            setEditing(model.isEditMode());
        }
    }

    //region :::::::::::::::::::::::::::::::::::::::::: Getters
    @Bindable
    public boolean isEditing() {
        return isEditing;
    }

    @Bindable
    public String getNavFullName() {
        return mNavFullName;
    }

    @Bindable
    public String getNavEmail() {
        return mNavEmail;
    }

    @Bindable
    public String getUserAvatarUri() {
        return mUserAvatarUri;
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Setters

    public void setEditing(boolean editing) {
        isEditing = editing;
        notifyPropertyChanged(BR.editing);
    }

    public void setNavFullName(String navFullName) {
        mNavFullName = navFullName;
        notifyPropertyChanged(BR.navFullName);
    }

    public void setNavEmail(String navEmail) {
        mNavEmail = navEmail;
        notifyPropertyChanged(BR.navEmail);
    }

    public void setUserAvatarUri(String userAvatarUri) {
        mUserAvatarUri = userAvatarUri;
        notifyPropertyChanged(BR.userAvatarUri);
    }

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region :::::::::::::::::::::::::::::::::::::::::: Parcel
    protected NavHeaderViewModel(Parcel in) {
        mNavFullName = in.readString();
        mNavEmail = in.readString();
        mUserAvatarUri = in.readString();
        isEditing = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mNavFullName);
        dest.writeString(mNavEmail);
        dest.writeString(mUserAvatarUri);
        dest.writeByte((byte) (isEditing ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<NavHeaderViewModel> CREATOR = new Parcelable.Creator<NavHeaderViewModel>() {
        @Override
        public NavHeaderViewModel createFromParcel(Parcel in) {
            return new NavHeaderViewModel(in);
        }

        @Override
        public NavHeaderViewModel[] newArray(int size) {
            return new NavHeaderViewModel[size];
        }
    };
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
