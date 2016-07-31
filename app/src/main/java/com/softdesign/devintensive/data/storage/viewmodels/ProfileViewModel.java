package com.softdesign.devintensive.data.storage.viewmodels;

import android.content.Context;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.restmodels.Repo;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.models.UserEntity;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings("unused")
public class ProfileViewModel extends BaseViewModel implements Parcelable {

    private static final Context CONTEXT = DevIntensiveApplication.getContext();
    private static final String AUTH_USER_ID = DataManager.getInstance().getPreferencesManager().loadBuiltInAuthId();

    private String mUserPhoto;
    private String mFullName;
    private String mRating = "0";
    private int mCodeLines = 0;
    private int mProjects = 0;
    private String mPhone;
    private String mEmail;
    private String mVK;
    private String mBio = "";
    private String mUserAvatarUri;
    private String mUserPhotoUri;
    private int mInternalId = 0;
    private String mRemoteId;
    private String mHometask;

    private List<RepoViewModel> mRepoViewModels = new ArrayList<>();
    private Set<String> mLikesBy = new HashSet<>();

    private boolean isEditMode = false;
    private boolean isAuthorizedUser = false;
    private boolean isLiked = false;
    private boolean isMoving = false;
    private boolean isList = false;

    //region ::::::::::::::::::::::::::::::::::: Constructors

    public ProfileViewModel() {
    }

    public ProfileViewModel(User user, String photoUri, String avatarUri) {

        isAuthorizedUser = true;

        mUserPhoto = user.getPublicInfo().getPhoto();
        mFullName = String.format("%s %s", user.getFirstName(), user.getSecondName());
        mRating = user.getProfileValues().getRating();
        mCodeLines = user.getProfileValues().getIntСodeLines();
        mProjects = user.getProfileValues().getIntProjects();
        mPhone = user.getContacts().getPhone();
        mEmail = user.getContacts().getEmail();
        mVK = user.getContacts().getVk();
        String bio = user.getPublicInfo().getBio();
        mBio = AppUtils.isEmptyOrNull(bio) ? "" : bio.trim();
        mUserPhotoUri = AppUtils.isEmptyOrNull(photoUri) ? mUserPhoto : photoUri;
        mUserAvatarUri = AppUtils.isEmptyOrNull(avatarUri) ? user.getPublicInfo().getAvatar() : avatarUri;
        mRemoteId = user.getId();
        mHometask = user.getProfileValues().getHomeTask();

        mRepoViewModels = createRepoViewModelList(user.getRepositories().getRepo());
        mLikesBy = new HashSet<>(user.getProfileValues().getLikesBy());

        isLiked = mLikesBy.contains(AUTH_USER_ID);
    }

    public ProfileViewModel(UserEntity user) {
        mUserPhoto = user.getPhoto();
        mFullName = user.getFullName();
        mRating = String.valueOf(user.getRating());
        mCodeLines = user.getCodeLines();
        mProjects = user.getProjects();
        String bio = user.getBio();
        mBio = AppUtils.isEmptyOrNull(bio) ? "" : bio.trim();
        mInternalId = user.getInternalId();
        mRemoteId = user.getRemoteId();
        mHometask = user.getHomeTask();
        mUserPhotoUri = mUserPhoto;

        mRepoViewModels = user.getRepoViewModelsList();
        mLikesBy = new HashSet<>(user.getLikesList());

        isLiked = mLikesBy.contains(AUTH_USER_ID);
        isList = true;
    }
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region ::::::::::::::::::::::::::::::::::: Utils
    public int set_16_9_AspectHeight(){
        int width = AppUtils.getScreenWidth();
        return (int) (width / Const.ASPECT_RATIO_16_9);
    }

    public ProfileViewModel updateValues(@Nullable ProfileViewModel model) {
        if (model == null) return null;

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

        if (!AppUtils.equals(this.mInternalId, model.getInternalId())) {
            setInternalId(model.getInternalId());
        }

        if (!AppUtils.equals(this.mRemoteId, model.getRemoteId())) {
            setRemoteId(model.getRemoteId());
        }

        if (!AppUtils.equals(this.mHometask, model.getHometask())) {
            setHometask(model.getHometask());
        }

        if (!AppUtils.compareRepoModelLists(this.mRepoViewModels, model.getRepoViewModels())) {
            setRepoViewModels(model.getRepoViewModels());
        }

        if (!AppUtils.compareLists(this.mLikesBy, model.getLikesBy())) {
            setLikesBy(model.getLikesBy());
        }

        if (isEditMode() != model.isEditMode()) {
            setEditMode(model.isEditMode());
        }

        if (isAuthorizedUser() != model.isAuthorizedUser()) {
            setAuthorizedUser(model.isAuthorizedUser());
        }

        if (isLiked() != model.isLiked()) {
            setLiked(model.isLiked());
        }

        return this;
    }

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

    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region ::::::::::::::::::::::::::::::::::: Getters
    @Bindable
    public boolean isList() {
        return isList;
    }

    @Bindable
    public String getHometask() {
        return mHometask;
    }

    @Bindable
    public int getInternalId() {
        return mInternalId;
    }

    @Bindable
    public String getRemoteId() {
        return mRemoteId;
    }

    @Bindable
    public Set<String> getLikesBy() {
        return mLikesBy;
    }

    @Bindable
    public boolean isLiked() {
        return isLiked;
    }

    @Bindable
    public boolean isMoving() {
        return isMoving;
    }

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
    public int getCodeLines() {
        return mCodeLines;
    }

    @Bindable
    public int getProjects() {
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
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region ::::::::::::::::::::::::::::::::::: Setters
    public void setList(boolean list) {
        isList = list;
        notifyPropertyChanged(BR.list);
    }

    public void setHometask(String hometask) {
        mHometask = hometask;
        notifyPropertyChanged(BR.hometask);
    }

    public void setRemoteId(String remoteId) {
        mRemoteId = remoteId;
        notifyPropertyChanged(BR.remoteId);
    }

    public void setInternalId(int internalId) {
        mInternalId = internalId;
        notifyPropertyChanged(BR.internalId);
    }

    public void setLikesBy(Set<String> likesBy) {
        mLikesBy = likesBy;
        notifyPropertyChanged(BR.likesBy);
    }

    public void setLikesBy(boolean isLiked) {
        if (isLiked) {
            getLikesBy().add(AUTH_USER_ID);
        } else getLikesBy().remove(AUTH_USER_ID);
        notifyPropertyChanged(BR.likesBy);
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
        notifyPropertyChanged(BR.liked);
        setLikesBy(liked);
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
        notifyPropertyChanged(BR.moving);
    }

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

    public void setCodeLines(int codeLines) {
        mCodeLines = codeLines;
        notifyPropertyChanged(BR.codeLines);
    }

    public void setProjects(int projects) {
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
    //endregion ::::::::::::::::::::::::::::::::::::::::::

    //region ::::::::::::::::::::::::::::::::::: Parcel
    protected ProfileViewModel(Parcel in) {
        mUserPhoto = in.readString();
        mFullName = in.readString();
        mRating = in.readString();
        mCodeLines = in.readInt();
        mProjects = in.readInt();
        mPhone = in.readString();
        mEmail = in.readString();
        mVK = in.readString();
        mBio = in.readString();
        mUserAvatarUri = in.readString();
        mUserPhotoUri = in.readString();
        mInternalId = in.readInt();
        mRemoteId = in.readString();
        mHometask = in.readString();
        mRepoViewModels = in.createTypedArrayList(RepoViewModel.CREATOR);
        List<String> tempList = in.createStringArrayList();
        mLikesBy = new HashSet<>(tempList);
        isEditMode = in.readByte() != 0x00;
        isAuthorizedUser = in.readByte() != 0x00;
        isLiked = in.readByte() != 0x00;
        isMoving = in.readByte() != 0x00;
        isList = in.readByte() != 0x00;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserPhoto);
        dest.writeString(mFullName);
        dest.writeString(mRating);
        dest.writeInt(mCodeLines);
        dest.writeInt(mProjects);
        dest.writeString(mPhone);
        dest.writeString(mEmail);
        dest.writeString(mVK);
        dest.writeString(mBio);
        dest.writeString(mUserAvatarUri);
        dest.writeString(mUserPhotoUri);
        dest.writeInt(mInternalId);
        dest.writeString(mRemoteId);
        dest.writeString(mHometask);
        dest.writeTypedList(mRepoViewModels);
        dest.writeStringList(new ArrayList<>(mLikesBy));
        dest.writeValue(mLikesBy);
        dest.writeByte((byte) (isEditMode ? 0x01 : 0x00));
        dest.writeByte((byte) (isAuthorizedUser ? 0x01 : 0x00));
        dest.writeByte((byte) (isLiked ? 0x01 : 0x00));
        dest.writeByte((byte) (isMoving ? 0x01 : 0x00));
        dest.writeByte((byte) (isList ? 0x01 : 0x00));
    }

    @Override
    public int describeContents() {
        return 0;
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
    //endregion ::::::::::::::::::::::::::::::::::::::::::
}
