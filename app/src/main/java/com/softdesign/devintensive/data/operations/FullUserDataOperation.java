package com.softdesign.devintensive.data.operations;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.network.api.res.UserPhotoRes;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.utils.Const;

import static com.softdesign.devintensive.utils.UiHelper.getJsonFromObject;
import static com.softdesign.devintensive.utils.UiHelper.getObjectFromJson;

public class FullUserDataOperation extends BaseChronosOperation<ProfileViewModel> {

    private User mUser;
    private Uri mPhotoUri;
    private String mAvatarUri;
    private String mPublicInfoUpdated;

    public FullUserDataOperation() {
        this.mAction = Action.LOAD;
    }

    public FullUserDataOperation(ProfileViewModel model) {
        this.mUser = model.updateUserData(getUser());
        this.mPhotoUri = model.mUserPhotoUri.get();
        this.mAvatarUri = model.mUserAvatarUri.get();
        this.mAction = Action.SAVE;
    }

    public FullUserDataOperation(User user) {
        this.mUser = user;
        this.mAction = Action.SAVE;
    }

    public FullUserDataOperation(UserPhotoRes data) {
        this.mPublicInfoUpdated = data.getUpdated();
        this.mAction = Action.SAVE;
    }

    public FullUserDataOperation(Uri photoUri) {
        this.mPhotoUri = photoUri;
        this.mAction = Action.SAVE;
    }

    public FullUserDataOperation(String avatarUri) {
        this.mAvatarUri = avatarUri;
        this.mAction = Action.SAVE;
    }

    @Nullable
    @Override
    public ProfileViewModel run() {
        switch (this.mAction) {
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
                Uri photoUri = Uri.parse(SHARED_PREFERENCES.getString(Const.USER_PROFILE_PHOTO_URI, ""));
                String avatarUri = SHARED_PREFERENCES.getString(Const.USER_PROFILE_AVATAR_URI, "");
                return new ProfileViewModel(userData, photoUri, avatarUri);
        }
        return null;
    }

    @Nullable
    private User getUser() {
        String json = SHARED_PREFERENCES.getString(Const.USER_JSON_OBJ, null);
        if (json != null) return  (User) getObjectFromJson(json, User.class);
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
