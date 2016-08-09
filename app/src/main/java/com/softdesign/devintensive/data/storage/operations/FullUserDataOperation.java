package com.softdesign.devintensive.data.storage.operations;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.network.api.res.UserAvatarRes;
import com.softdesign.devintensive.data.network.api.res.UserPhotoRes;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.utils.AppUtils;
import com.softdesign.devintensive.utils.Const;
import com.vk.sdk.VKSdk;

import java.io.File;

import static com.softdesign.devintensive.utils.AppUtils.getJsonFromObject;
import static com.softdesign.devintensive.utils.AppUtils.getObjectFromJson;
import static com.softdesign.devintensive.utils.AppUtils.isEmptyOrNull;
import static com.softdesign.devintensive.utils.DevIntensiveApplication.getContext;

public class FullUserDataOperation extends BaseChronosOperation<ProfileViewModel> {

    private User mUser;
    private Uri mPhotoUri;
    private String mAvatarUri;
    private String mPublicInfoUpdated;

    public FullUserDataOperation() {
        this.mAction = Action.LOAD;
    }

    public FullUserDataOperation(Action action) {
        if (action == Action.SAVE) return; //only CLEAR and LOAD allowed this way
        this.mAction = action;
    }

    public FullUserDataOperation(ProfileViewModel model) {
        User savedUser = getUser();
        if (savedUser != null) this.mUser = model.updateUserFromModel(savedUser);
        String photoUri = model.getUserPhotoUri();
        if (!AppUtils.isEmptyOrNull(photoUri)) this.mPhotoUri = Uri.parse(photoUri);
        String avatarUri = model.getUserAvatarUri();
        if (!AppUtils.isEmptyOrNull(avatarUri)) this.mAvatarUri = avatarUri;
        this.mAction = Action.SAVE;
    }

    public FullUserDataOperation(User user) {
        this.mUser = user;
        this.mAction = Action.SAVE;
    }

    public FullUserDataOperation(UserPhotoRes data) {
        this.mPublicInfoUpdated = data.getUpdated();
        this.mPhotoUri = Uri.parse(data.getPhoto());
        this.mAction = Action.SAVE;
    }

    public FullUserDataOperation(UserAvatarRes data) {
        this.mPublicInfoUpdated = data.getUpdated();
        this.mAvatarUri = data.getAvatar();
        this.mAction = Action.SAVE;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    @Override
    public ProfileViewModel run() {
        switch (this.mAction) {
            case CLEAR:
                SharedPreferences.Editor editor1 = SHARED_PREFERENCES.edit();
                editor1.clear().apply();
                VKSdk.logout();
                File dir = getContext().getFilesDir();
                try {
                    new File(dir, "photo.png").delete();
                    new File(dir, "avatar.png").delete();
                } catch (Exception ignored) {
                }
                Glide.get(getContext()).clearDiskCache();
                Glide.get(getContext()).clearMemory();
                break;
            case SAVE:
                SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();

                if (mPublicInfoUpdated != null) {
                    User userData = getUser();
                    if (userData != null) {
                        userData.getPublicInfo().setUpdated(mPublicInfoUpdated);
                        editor.putString(Const.USER_JSON_OBJ, getJsonFromObject(userData, User.class));
                    }
                }
                if (mUser != null) {
                    editor.putString(Const.USER_JSON_OBJ, getJsonFromObject(mUser, User.class));
                    if (mPhotoUri == null) {
                        String photoUri = SHARED_PREFERENCES.getString(Const.USER_PROFILE_PHOTO_URI, "");
                        if (isEmptyOrNull(photoUri))
                            editor.putString(Const.USER_PROFILE_PHOTO_URI, mUser.getPublicInfo().getPhoto());
                    }
                    if (mAvatarUri == null) {
                        String avatarUri = SHARED_PREFERENCES.getString(Const.USER_PROFILE_AVATAR_URI, "");
                        if (isEmptyOrNull(avatarUri))
                            editor.putString(Const.USER_PROFILE_AVATAR_URI, mUser.getPublicInfo().getAvatar());
                    }
                }
                if (mPhotoUri != null) {
                    editor.putString(Const.USER_PROFILE_PHOTO_URI, mPhotoUri.toString());
                }
                if (mAvatarUri != null) {
                    editor.putString(Const.USER_PROFILE_AVATAR_URI, mAvatarUri);
                }

                editor.apply();
                return null;
            case LOAD:
                User userData = getUser();
                if (userData == null) return null;
                String photoUri = SHARED_PREFERENCES.getString(Const.USER_PROFILE_PHOTO_URI, "");
                String avatarUri = SHARED_PREFERENCES.getString(Const.USER_PROFILE_AVATAR_URI, "");
                return new ProfileViewModel(userData, photoUri, avatarUri);
        }
        return null;
    }

    @Nullable
    private User getUser() {
        String json = SHARED_PREFERENCES.getString(Const.USER_JSON_OBJ, null);
        if (json != null) return (User) getObjectFromJson(json, User.class);
        else return null;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<ProfileViewModel>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<ProfileViewModel> {
    }
}
