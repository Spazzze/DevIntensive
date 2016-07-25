package com.softdesign.devintensive.data.storage.viewmodels;

import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.softdesign.devintensive.data.binding.fields.ObservableString;
import com.softdesign.devintensive.data.network.restmodels.Repo;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.models.UserDTO;

import java.util.List;

public class ProfileViewModel extends BaseObservable implements Parcelable {

    public final ObservableString mUserPhoto = new ObservableString();
    public final ObservableString mFullName = new ObservableString();
    public final ObservableString mRating = new ObservableString();
    public final ObservableString mCodeLines = new ObservableString();
    public final ObservableString mProjects = new ObservableString();
    public final ObservableString mPhone = new ObservableString();
    public final ObservableString mEmail = new ObservableString();
    public final ObservableString mVK = new ObservableString();
    public final ObservableString mBio = new ObservableString();

    public final ObservableArrayList<Repo> mRepositories = new ObservableArrayList<>();

    public final ObservableBoolean isEditMode = new ObservableBoolean(false);
    public final ObservableBoolean isAuthorizedUser = new ObservableBoolean(false);

    public final ObservableField<Uri> mUserPhotoUri = new ObservableField<>();
    public final ObservableString mUserAvatarUri = new ObservableString();

    public ProfileViewModel(User user, Uri photoUri, String avatarUri) {

        isAuthorizedUser.set(true);

        mUserPhotoUri.set(photoUri);
        mUserAvatarUri.set(avatarUri);

        mFullName.set(String.format("%s %s", user.getFirstName(), user.getSecondName()));
        mRating.set(String.valueOf(user.getProfileValues().getRating()));
        mCodeLines.set(String.valueOf(user.getProfileValues().getCodeLines()));
        mProjects.set(String.valueOf(user.getProfileValues().getProjects()));
        mPhone.set(user.getContacts().getPhone());
        mEmail.set(user.getContacts().getEmail());
        mVK.set(user.getContacts().getVk());
        mBio.set(user.getPublicInfo().getBio());

        List<Repo> repo = user.getRepositories().getRepo();
        for (int i = 0; i < repo.size(); i++) {
            mRepositories.set(i, repo.get(i));
        }
    }

    public ProfileViewModel(UserDTO user) {

        isAuthorizedUser.set(false);

        mUserPhoto.set(user.getUserPhoto());
        mFullName.set(user.getFullName());
        mRating.set(user.getRating());
        mCodeLines.set(user.getCodeLines());
        mProjects.set(user.getProjects());
        mBio.set(user.getBio());

        List<String> repo = user.getRepositories();
        for (int i = 0; i < repo.size(); i++) {
            mRepositories.set(i, new Repo(i, repo.get(i)));
        }
    }

    public User updateUserData(User user) {

        String[] name = mFullName.get().split("\\s");
        user.setFirstName(name[0]);
        user.setSecondName(name[1]);

        user.getContacts().setPhone(mPhone.get());
        user.getContacts().setEmail(mEmail.get());
        user.getContacts().setVk(mVK.get());
        user.getPublicInfo().setBio(mBio.get());
        user.getRepositories().setRepo(mRepositories);

        return user;
    }

    public Boolean compareUserData(User user) {

        return  user.getContacts().getPhone().equals(mPhone.get()) &&
                user.getContacts().getVk().equals(mVK.get()) &&
                user.getPublicInfo().getBio().equals(mBio.get()) &&
                user.getRepositories().getRepo().equals(mRepositories) &&
                mFullName.get().equals(String.format("%s %s", user.getFirstName(), user.getSecondName()));
    }

    //region Parcel
    protected ProfileViewModel(Parcel in) {
        mUserPhoto.set(in.readString());
        mFullName.set(in.readString());
        mRating.set(in.readString());
        mCodeLines.set(in.readString());
        mProjects.set(in.readString());
        mPhone.set(in.readString());
        mEmail.set(in.readString());
        mVK.set(in.readString());
        mBio.set(in.readString());
        if (in.readByte() == 0x01) {
            mRepositories.clear();
            in.readList(mRepositories, Repo.class.getClassLoader());
        } else {
            mRepositories.clear();
        }
        isEditMode.set(in.readByte() != 0x00);
        isAuthorizedUser.set(in.readByte() != 0x00);
        mUserPhotoUri.set((Uri) in.readValue(Uri.class.getClassLoader()));
        mUserAvatarUri.set(in.readString());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserPhoto.get());
        dest.writeString(mFullName.get());
        dest.writeString(mRating.get());
        dest.writeString(mCodeLines.get());
        dest.writeString(mProjects.get());
        dest.writeString(mPhone.get());
        dest.writeString(mEmail.get());
        dest.writeString(mVK.get());
        dest.writeString(mBio.get());
        if (mRepositories == null || mRepositories.size() == 0) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(mRepositories);
        }
        dest.writeByte((byte) (isEditMode.get() ? 0x01 : 0x00));
        dest.writeByte((byte) (isAuthorizedUser.get() ? 0x01 : 0x00));
        dest.writeValue(mUserPhotoUri.get());
        dest.writeString(mUserAvatarUri.get());
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
}
