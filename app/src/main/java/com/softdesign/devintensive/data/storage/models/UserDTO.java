package com.softdesign.devintensive.data.storage.models;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.softdesign.devintensive.BR;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({"unused", "unchecked"})
public class UserDTO extends BaseObservable implements Parcelable {

    private static final Context CONTEXT = DevIntensiveApplication.getContext();
    private static final String AUTH_USER_ID = DataManager.getInstance().getPreferencesManager().loadBuiltInAuthId();

    private String mUserPhoto;
    private String mFullName;
    private String mRating;
    private String mCodeLines;
    private String mProjects;
    private String mBio;
    private String mHometask;
    private List<String> mRepositories = new ArrayList<>();
    private Set<String> mLikesBy = new HashSet<>();
    private boolean isLiked = false;
    private boolean isMoving = false;
    private int mInternalId;
    private String mRemoteId;
    private String mLikesString;

    public UserDTO(UserEntity user) {
        mUserPhoto = user.getPhoto();
        mFullName = user.getFullName();
        mRating = String.valueOf(user.getRating());
        mCodeLines = String.valueOf(user.getCodeLines());
        mProjects = String.valueOf(user.getProjects());
        mBio = user.getBio();
        mHometask = user.getHomeTask();
        mRepositories = user.getRepoList();
        mLikesBy = new HashSet<>(user.getLikesList());
        isLiked = mLikesBy.contains(AUTH_USER_ID);
        mInternalId = user.getInternalId();
        mRemoteId = user.getRemoteId();
        mLikesString = getLikesString();
    }

    public void updateValues(@Nullable UserDTO model) {
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

        if (!AppUtils.equals(this.mBio, model.getBio())) {
            setBio(model.getBio());
        }

        if (!AppUtils.equals(this.mHometask, model.getHometask())) {
            setHometask(model.getHometask());
        }

        if (isLiked() != model.isLiked()) {
            setLiked(model.isLiked());
        }

        if (!AppUtils.compareLists(this.mRepositories, model.getRepositories())) {
            setRepositories(model.getRepositories());
        }

        if (!AppUtils.compareLists(this.mLikesBy, model.getLikesBy())) {
            setLikesBy(model.getLikesBy());
        }

        if (mInternalId != model.getInternalId()) {
            setInternalId(model.getInternalId());
        }

        if (!AppUtils.equals(this.mRemoteId, model.getRemoteId())) {
            setRemoteId(model.getRemoteId());
        }
    }

    //region Parcel
    protected UserDTO(Parcel in) {
        mUserPhoto = in.readString();
        mFullName = in.readString();
        mRating = in.readString();
        mCodeLines = in.readString();
        mProjects = in.readString();
        mBio = in.readString();
        mHometask = in.readString();
        if (in.readByte() == 0x01) {
            mRepositories = new ArrayList<>();
            in.readList(mRepositories, String.class.getClassLoader());
        } else {
            mRepositories.clear();
        }
        mLikesBy = (Set) in.readValue(Set.class.getClassLoader());
        isLiked = in.readByte() != 0x00;
        isMoving = in.readByte() != 0x00;
        mInternalId = in.readInt();
        mRemoteId = in.readString();
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
        dest.writeString(mBio);
        dest.writeString(mHometask);
        if (mRepositories == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mRepositories);
        }
        dest.writeValue(mLikesBy);
        dest.writeByte((byte) (isLiked ? 0x01 : 0x00));
        dest.writeByte((byte) (isMoving ? 0x01 : 0x00));
        dest.writeInt(mInternalId);
        dest.writeString(mRemoteId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<UserDTO> CREATOR = new Parcelable.Creator<UserDTO>() {
        @Override
        public UserDTO createFromParcel(Parcel in) {
            return new UserDTO(in);
        }

        @Override
        public UserDTO[] newArray(int size) {
            return new UserDTO[size];
        }
    };
    //endregion

    //region Standard Getters
    @Bindable
    public String getRemoteId() {
        return mRemoteId;
    }

    @Bindable
    public int getInternalId() {
        return mInternalId;
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
    public String getCodeLines() {
        return mCodeLines;
    }

    @Bindable
    public String getProjects() {
        return mProjects;
    }

    @Bindable
    public String getBio() {
        return mBio;
    }

    @Bindable
    public List<String> getRepositories() {
        return mRepositories;
    }

    @Bindable
    public String getHometask() {
        return mHometask;
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
    public String getLikesString() {
        return MessageFormat.format("{0} {1}", CONTEXT.getResources().getString(R.string.header_like), mLikesBy.size());
    }

    //endregion

    //region Setters

    public void setRemoteId(String remoteId) {
        mRemoteId = remoteId;
        notifyPropertyChanged(BR.remoteId);
    }

    public void setInternalId(int internalId) {
        mInternalId = internalId;
        notifyPropertyChanged(BR.internalId);
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

    public void setCodeLines(String codeLines) {
        mCodeLines = codeLines;
        notifyPropertyChanged(BR.codeLines);
    }

    public void setProjects(String projects) {
        mProjects = projects;
        notifyPropertyChanged(BR.projects);
    }

    public void setBio(String bio) {
        mBio = bio;
        notifyPropertyChanged(BR.bio);
    }

    public void setHometask(String hometask) {
        mHometask = hometask;
        notifyPropertyChanged(BR.hometask);
    }

    public void setRepositories(List<String> repositories) {
        mRepositories = repositories;
        notifyPropertyChanged(BR.repositories);
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
        setLikesString(getLikesString());
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

    public void setLikesString(String likesString) {
        this.mLikesString = likesString;
        notifyPropertyChanged(BR.likesString);
    }

    //endregion
}
