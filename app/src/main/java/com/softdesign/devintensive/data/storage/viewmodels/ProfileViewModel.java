package com.softdesign.devintensive.data.storage.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.os.Parcel;
import android.os.Parcelable;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.data.network.restmodels.Repo;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;

import java.util.List;

public class ProfileViewModel extends BaseObservable implements Parcelable {

    private String mUserPhoto;
    private String mFullName;
    private String mRating;
    private String mCodeLines;
    private String mProjects;
    private String mPhone;
    private String mEmail;
    private String mVK;
    private String mBio;
    private String mUserAvatarUri;
    private String mUserPhotoUri;

    public final ObservableArrayList<Repo> mRepositories = new ObservableArrayList<>();
    public final ObservableBoolean isEditMode = new ObservableBoolean(false);
    public final ObservableBoolean isAuthorizedUser = new ObservableBoolean(false);

    public ProfileViewModel(User user, String photoUri, String avatarUri) {

        isAuthorizedUser.set(true);

        mUserPhotoUri = photoUri;
        mUserAvatarUri = avatarUri;

        mFullName = String.format("%s %s", user.getFirstName(), user.getSecondName());
        mRating = String.valueOf(user.getProfileValues().getRating());
        mCodeLines = String.valueOf(user.getProfileValues().getCodeLines());
        mProjects = String.valueOf(user.getProfileValues().getProjects());
        mPhone = user.getContacts().getPhone();
        mEmail = user.getContacts().getEmail();
        mVK = user.getContacts().getVk();
        mBio = user.getPublicInfo().getBio();

        List<Repo> repo = user.getRepositories().getRepo();
        for (Repo r : repo) {
            mRepositories.add(r);
        }
    }

    public ProfileViewModel(UserDTO user) {

        isAuthorizedUser.set(false);

        mUserPhoto = user.getUserPhoto();
        mFullName = user.getFullName();
        mRating = user.getRating();
        mCodeLines = user.getCodeLines();
        mProjects = user.getProjects();
        mBio = user.getBio();

        List<String> repo = user.getRepositories();
        for (int i = 0; i < repo.size(); i++) {
            mRepositories.add(new Repo(i, repo.get(i)));
        }
    }

    public User updateUserData(User user) {

        String[] name = mFullName.split("\\s");
        user.setFirstName(name[0]);
        user.setSecondName(name[1]);

        user.getContacts().setPhone(mPhone);
        user.getContacts().setEmail(mEmail);
        user.getContacts().setVk(mVK);
        user.getPublicInfo().setBio(mBio);
        user.getRepositories().setRepo(mRepositories);

        return user;
    }

    public Boolean compareUserData(User user) {

        return user.getContacts().getPhone().equals(mPhone) &&
                user.getContacts().getVk().equals(mVK) &&
                user.getPublicInfo().getBio().equals(mBio) &&
                user.getRepositories().getRepo().equals(mRepositories) &&
                mFullName.equals(String.format("%s %s", user.getFirstName(), user.getSecondName()));
    }

    //region Parcel
    protected ProfileViewModel(Parcel in) {
        mUserPhoto = in.readString();
        mFullName = in.readString();
        mRating = in.readString();
        mCodeLines = in.readString();
        mProjects = in.readString();
        mPhone = in.readString();
        mEmail = in.readString();
        mVK = in.readString();
        mBio = in.readString();
        mUserAvatarUri = in.readString();
        mUserPhotoUri = in.readString();
        if (in.readByte() == 0x01) {
            mRepositories.clear();
            in.readList(mRepositories, Repo.class.getClassLoader());
        } else {
            mRepositories.clear();
        }
        isEditMode.set(in.readByte() != 0x00);
        isAuthorizedUser.set(in.readByte() != 0x00);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserPhoto);
        dest.writeString(mFullName);
        dest.writeString(mRating);
        dest.writeString(mCodeLines);
        dest.writeString(mProjects);
        dest.writeString(mPhone);
        dest.writeString(mEmail);
        dest.writeString(mVK);
        dest.writeString(mBio);
        dest.writeString(mUserAvatarUri);
        dest.writeString(mUserPhotoUri);
        if (mRepositories == null || mRepositories.size() == 0) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mRepositories);
        }
        dest.writeByte((byte) (isEditMode.get() ? 0x01 : 0x00));
        dest.writeByte((byte) (isAuthorizedUser.get() ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ProfileViewModel> CREATOR = new Parcelable.Creator<ProfileViewModel>() {
        @Override
        public ProfileViewModel createFromParcel(Parcel in) {
            return new ProfileViewModel(in);
        }

        @Override
        public ProfileViewModel[] newArray(int size) {
            return new ProfileViewModel[size];
        }
    };
    //endregion

    //region Getters
    @Bindable
    public String getUserPhoto() {
        return mUserPhoto;
    }

    @Bindable
    public String getFullName() {
        return mFullName;
    }

    @Bindable
    public String getRating() {
        return mRating;
    }

    @Bindable
    public String getCodeLines() {
        return mCodeLines;
    }

    @Bindable
    public String getProjects() {
        return mProjects;
    }

    @Bindable
    public String getPhone() {
        return mPhone;
    }

    @Bindable
    public String getEmail() {
        return mEmail;
    }

    @Bindable
    public String getVK() {
        return mVK;
    }

    @Bindable
    public String getBio() {
        return mBio;
    }

    @Bindable
    public String getUserAvatarUri() {
        return mUserAvatarUri;
    }

    @Bindable
    public String getUserPhotoUri() {
        return mUserPhotoUri;
    }
    //endregion

    //region Setters
    public void setUserPhoto(String userPhoto) {
        mUserPhoto = userPhoto;
        notifyPropertyChanged(BR.userPhoto);
    }

    public void setFullName(String fullName) {
        mFullName = fullName;
        notifyPropertyChanged(BR.fullName);
    }

    public void setRating(String rating) {
        mRating = rating;
        notifyPropertyChanged(BR.rating);
    }

    public void setCodeLines(String codeLines) {
        mCodeLines = codeLines;
        notifyPropertyChanged(BR.codeLines);
    }

    public void setProjects(String projects) {
        mProjects = projects;
        notifyPropertyChanged(BR.projects);
    }

    public void setPhone(String phone) {
        mPhone = phone;
        notifyPropertyChanged(BR.phone);
    }

    public void setEmail(String email) {
        mEmail = email;
        notifyPropertyChanged(BR.email);
    }

    public void setVK(String VK) {
        mVK = VK;
        notifyPropertyChanged(BR.vK);
    }

    public void setBio(String bio) {
        mBio = bio;
        notifyPropertyChanged(BR.bio);
    }

    public void setUserAvatarUri(String userAvatarUri) {
        mUserAvatarUri = userAvatarUri;
        notifyPropertyChanged(BR.userAvatarUri);
    }

    public void setUserPhotoUri(String userPhotoUri) {
        mUserPhotoUri = userPhotoUri;
        notifyPropertyChanged(BR.userPhotoUri);
    }
    //endregion
}
