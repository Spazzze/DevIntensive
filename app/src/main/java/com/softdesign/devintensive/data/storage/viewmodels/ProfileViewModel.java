package com.softdesign.devintensive.data.storage.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableArrayList;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.data.network.restmodels.Repo;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.utils.UiHelper;

import java.util.List;

@SuppressWarnings("unused")
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

    private boolean isEditMode = false;
    private boolean isAuthorizedUser = false;

    public ProfileViewModel() {
    }

    public ProfileViewModel(User user, String photoUri, String avatarUri) {

        isAuthorizedUser = true;

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

        isAuthorizedUser = false;

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

    public void updateValues(@Nullable ProfileViewModel profileViewModel) {
        if (profileViewModel == null) return;

        if (!UiHelper.equals(this.mUserPhoto, profileViewModel.getUserPhoto())) {
            this.mUserPhoto = profileViewModel.getUserPhoto();
            notifyPropertyChanged(BR.userPhoto);
        }

        if (!UiHelper.equals(this.mFullName, profileViewModel.getFullName())) {
            this.mFullName = profileViewModel.getFullName();
            notifyPropertyChanged(BR.fullName);
        }

        if (!UiHelper.equals(this.mRating, profileViewModel.getRating())) {
            this.mRating = profileViewModel.getRating();
            notifyPropertyChanged(BR.rating);
        }

        if (!UiHelper.equals(this.mCodeLines, profileViewModel.getCodeLines())) {
            this.mCodeLines = profileViewModel.getCodeLines();
            notifyPropertyChanged(BR.codeLines);
        }

        if (!UiHelper.equals(this.mProjects, profileViewModel.getProjects())) {
            this.mProjects = profileViewModel.getProjects();
            notifyPropertyChanged(BR.projects);
        }

        if (!UiHelper.equals(this.mPhone, profileViewModel.getPhone())) {
            this.mPhone = profileViewModel.getPhone();
            notifyPropertyChanged(BR.phone);
        }

        if (!UiHelper.equals(this.mEmail, profileViewModel.getEmail())) {
            this.mEmail = profileViewModel.getEmail();
            notifyPropertyChanged(BR.email);
        }

        if (!UiHelper.equals(this.mVK, profileViewModel.getVK())) {
            this.mVK = profileViewModel.getVK();
            notifyPropertyChanged(BR.vK);
        }

        if (!UiHelper.equals(this.mBio, profileViewModel.getBio())) {
            this.mBio = profileViewModel.getBio();
            notifyPropertyChanged(BR.bio);
        }

        if (!UiHelper.equals(this.mUserAvatarUri, profileViewModel.getUserAvatarUri())) {
            this.mUserAvatarUri = profileViewModel.getUserAvatarUri();
            notifyPropertyChanged(BR.userAvatarUri);
        }

        if (!UiHelper.equals(this.mUserPhotoUri, profileViewModel.getUserPhotoUri())) {
            this.mUserPhotoUri = profileViewModel.getUserPhotoUri();
            notifyPropertyChanged(BR.userPhotoUri);
        }
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
        isEditMode = in.readByte() != 0x00;
        isAuthorizedUser = in.readByte() != 0x00;
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
        dest.writeByte((byte) (isEditMode ? 0x01 : 0x00));
        dest.writeByte((byte) (isAuthorizedUser ? 0x01 : 0x00));
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

    @Bindable
    public boolean isEditMode() {
        return isEditMode;
    }

    @Bindable
    public boolean isAuthorizedUser() {
        return isAuthorizedUser;
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

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyPropertyChanged(BR.editMode);
    }

    public void setAuthorizedUser(boolean authorizedUser) {
        isAuthorizedUser = authorizedUser;
        notifyPropertyChanged(BR.authorizedUser);
    }
    //endregion
}
