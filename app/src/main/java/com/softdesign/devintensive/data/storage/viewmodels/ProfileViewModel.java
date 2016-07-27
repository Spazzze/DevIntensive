package com.softdesign.devintensive.data.storage.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.data.network.restmodels.Repo;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;
import com.softdesign.devintensive.utils.AppUtils;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class ProfileViewModel extends BaseObservable implements Parcelable {

    public static final String IGNORED_STR = "IGNORED_STR";

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

    private List<RepoViewModel> mRepoViewModels = new ArrayList<>();

    private boolean isEditMode = false;
    private boolean isAuthorizedUser = false;

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
        mRepoViewModels = createRepoViewModelList(user.getRepositories().getRepo());
    }

    public ProfileViewModel(UserDTO user) {

        isAuthorizedUser = false;

        mUserPhoto = user.getUserPhoto();
        mFullName = user.getFullName();
        mRating = user.getRating();
        mCodeLines = user.getCodeLines();
        mProjects = user.getProjects();
        mBio = user.getBio();

        for (String s : user.getRepositories()) {
            if (!s.isEmpty()) mRepoViewModels.add(new RepoViewModel(s, false, false));
        }
    }

    public void updateValues(@Nullable ProfileViewModel model) {
        if (model == null) return;

        if (!AppUtils.equals(this.mUserPhoto, model.getUserPhoto())) {
            setUserPhoto(model.getUserPhoto());
        }

        if (!AppUtils.equals(this.mFullName, model.getFullName())) {
            setFullName(model.getFullName());
        }

        if (!AppUtils.equals(this.mRating, model.getRating())) {
            setRating(model.getRating());
        }

        if (!AppUtils.equals(this.mCodeLines, model.getCodeLines())) {
            setCodeLines(model.getCodeLines());
        }

        if (!AppUtils.equals(this.mProjects, model.getProjects())) {
            setProjects(model.getProjects());
        }

        if (!AppUtils.equals(this.mPhone, model.getPhone())) {
            setPhone(model.getPhone());
        }

        if (!AppUtils.equals(this.mEmail, model.getEmail())) {
            setEmail(model.getEmail());
        }

        if (!AppUtils.equals(this.mVK, model.getVK())) {
            setVK(model.getVK());
        }

        if (!AppUtils.equals(this.mBio, model.getBio())) {
            setBio(model.getBio());
        }

        if (!AppUtils.equals(this.mUserAvatarUri, model.getUserAvatarUri())) {
            setUserAvatarUri(model.getUserAvatarUri());
        }

        if (!AppUtils.equals(this.mUserPhotoUri, model.getUserPhotoUri())) {
            setUserPhotoUri(model.getUserPhotoUri());
        }

        if (isEditMode() != model.isEditMode()) {
            setEditMode(model.isEditMode());
        }

        if (isAuthorizedUser() != model.isAuthorizedUser()) {
            setAuthorizedUser(model.isAuthorizedUser());
        }

        if (!AppUtils.compareRepoModelLists(this.mRepoViewModels, model.getRepoViewModels())) {
            setRepoViewModels(model.getRepoViewModels());
        }
    }

    //region Utils
    public void addEmptyRepo() {
        if (isEditMode()) {
            mRepoViewModels.add(new RepoViewModel("", isEditMode(), isAuthorizedUser()));
            notifyPropertyChanged(BR.repoViewModels);
        }
    }

    public User updateUserFromModel(User user) {

        String[] name = mFullName.split("\\s");
        user.setFirstName(name[0]);
        user.setSecondName(name[1]);

        user.getContacts().setPhone(mPhone);
        user.getContacts().setEmail(mEmail);
        user.getContacts().setVk(mVK);
        user.getPublicInfo().setBio(mBio);
        user.getRepositories().setRepo(repoListFromModel());

        return user;
    }

    public Boolean compareUserData(User user) {
        updateLists();
        return user.getContacts().getPhone().equals(mPhone) &&
                user.getContacts().getVk().equals(mVK) &&
                user.getPublicInfo().getBio().equals(mBio) &&
                AppUtils.compareRepoModelLists(mRepoViewModels, createRepoViewModelList(user.getRepositories().getRepo())) &&
                mFullName.equals(String.format("%s %s", user.getFirstName(), user.getSecondName()));
    }

    private void updateLists() {
        List<RepoViewModel> newList = new ArrayList<RepoViewModel>() {{
            for (int i = 0; i < mRepoViewModels.size(); i++) {
                RepoViewModel r = mRepoViewModels.get(i);
                if ((i != 0 && !r.getRepoUri().isEmpty()) || i == 0) add(r);
            }
        }};
        setRepoViewModels(newList);
    }

    private List<RepoViewModel> createRepoViewModelList(final List<Repo> list) {
        if (list.size() > 0) {
            return new ArrayList<RepoViewModel>() {{
                for (Repo r : list) {
                    add(new RepoViewModel(r.getId(), r.getGit(), isEditMode(), isAuthorizedUser()));
                }
            }};
        } else {
            return new ArrayList<RepoViewModel>() {{
                add(new RepoViewModel("", isEditMode(), isAuthorizedUser()));
            }};
        }
    }

    public List<Repo> repoListFromModel() {
        return new ArrayList<Repo>() {{
            for (RepoViewModel r : mRepoViewModels) {
                add(new Repo(r.getId(), r.getRepoUri()));
            }
        }};
    }
    //endregion

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
            mRepoViewModels.clear();
            in.readList(mRepoViewModels, RepoViewModel.class.getClassLoader());
        } else {
            mRepoViewModels.clear();
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
        if (mRepoViewModels == null || mRepoViewModels.size() == 0) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mRepoViewModels);
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

    @Bindable
    public List<RepoViewModel> getRepoViewModels() {
        return mRepoViewModels;
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
        if (this.isEditMode != editMode) {
            isEditMode = editMode;
            if (mRepoViewModels != null && !mRepoViewModels.isEmpty()) {
                for (RepoViewModel model : mRepoViewModels) {
                    model.setEnabled(editMode);
                }
            }
            notifyPropertyChanged(BR.repoViewModels);
            notifyPropertyChanged(BR.editMode);
        }
    }

    public void setAuthorizedUser(boolean authorizedUser) {
        if (this.isAuthorizedUser != authorizedUser) {
            isAuthorizedUser = authorizedUser;
            if (mRepoViewModels != null && !mRepoViewModels.isEmpty()) {
                for (RepoViewModel model : mRepoViewModels) {
                    model.setCanBeEdit(authorizedUser);
                }
            }
            notifyPropertyChanged(BR.repoViewModels);
            notifyPropertyChanged(BR.authorizedUser);
        }
    }

    public void setRepoViewModels(List<RepoViewModel> repoViewModels) {
        if (!AppUtils.compareRepoModelLists(this.mRepoViewModels, repoViewModels)) {
            mRepoViewModels = repoViewModels;
            notifyPropertyChanged(BR.repoViewModels);
        }
    }
    //endregion
}
