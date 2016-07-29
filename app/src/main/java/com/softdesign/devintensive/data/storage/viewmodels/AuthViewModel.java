package com.softdesign.devintensive.data.storage.viewmodels;

import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.data.storage.operations.UserLoginDataOperation;
import com.softdesign.devintensive.ui.activities.BaseActivity;
import com.softdesign.devintensive.utils.AppUtils;

public class AuthViewModel extends BaseViewModel implements Parcelable {

    private BaseActivity mActivity;

    private String mLoginName = "";
    private String mPassword;
    private boolean isSavingLogin = false;
    private boolean isWrongPassword = false;

    public AuthViewModel(BaseActivity activity) {
        mActivity = activity;
    }

    public void saveLoginName() {
        String loginName = null;
        if (isSavingLogin) loginName = mLoginName;
        if (mActivity != null) {
            mActivity.runOperation(new UserLoginDataOperation(loginName));
        } else {
            new UserLoginDataOperation(loginName).run();
        }
    }

    public void updateValues(AuthViewModel model){
        if (model == null) return;

        if (!AppUtils.equals(this.mLoginName, model.getLoginName())) {
            setLoginName(model.getLoginName());
        }

        if (!AppUtils.equals(this.mPassword, model.getPassword())) {
            setPassword(model.getPassword());
        }

        if (isSavingLogin() != model.isSavingLogin()) {
            setSavingLogin(model.isSavingLogin());
        }
    }

    //region --------------GETTERS-------------
    @Bindable
    public String getPassword() {
        return mPassword;
    }

    @Bindable
    public boolean isSavingLogin() {
        return isSavingLogin;
    }

    @Bindable
    public String getLoginName() {
        return mLoginName;
    }

    @Bindable
    public boolean isWrongPassword() {
        return isWrongPassword;
    }

    public BaseActivity getActivity() {
        return mActivity;
    }
    //endregion

    //region -------------SETTERS----------------
    public void clearData() {
        setPassword(null);
        setLoginName(null);
    }

    public void setPassword(String password) {
        mPassword = password;
        notifyPropertyChanged(BR.password);
    }

    public void setSavingLogin(boolean savingLogin) {
        isSavingLogin = savingLogin;
        notifyPropertyChanged(BR.savingLogin);
    }

    public void setWrongPassword(boolean wrongPassword) {
        isWrongPassword = wrongPassword;
        notifyPropertyChanged(BR.wrongPassword);
    }

    public void setLoginName(String loginName) {
        mLoginName = loginName;
        notifyPropertyChanged(BR.loginName);
    }

    public void setActivity(BaseActivity activity) {
        mActivity = activity;
    }

    //endregion

    //region <<<<<<<<<<<PARCEL>>>>>>>>>>>
    protected AuthViewModel(Parcel in) {
        mLoginName = in.readString();
        mPassword = in.readString();
        isSavingLogin = in.readByte() != 0x00;
        isWrongPassword = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLoginName);
        dest.writeString(mPassword);
        dest.writeByte((byte) (isSavingLogin ? 0x01 : 0x00));
        dest.writeByte((byte) (isWrongPassword ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AuthViewModel> CREATOR = new Parcelable.Creator<AuthViewModel>() {
        @Override
        public AuthViewModel createFromParcel(Parcel in) {
            return new AuthViewModel(in);
        }

        @Override
        public AuthViewModel[] newArray(int size) {
            return new AuthViewModel[size];
        }
    };
    //endregion
}
