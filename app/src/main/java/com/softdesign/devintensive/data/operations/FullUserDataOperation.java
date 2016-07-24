package com.softdesign.devintensive.data.operations;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.data.network.restmodels.User;
import com.softdesign.devintensive.utils.Const;

import static com.softdesign.devintensive.utils.UiHelper.getJsonFromObject;
import static com.softdesign.devintensive.utils.UiHelper.getObjectFromJson;

public class FullUserDataOperation extends BaseChronosOperation<User> {

    private User mUser;

    public FullUserDataOperation() {
        this.mAction = Action.LOAD;
    }

    public FullUserDataOperation(User user) {
        this.mUser = user;
        this.mAction = Action.SAVE;
    }

    @Nullable
    @Override
    public User run() {
        switch (this.mAction) {
            case SAVE:
                SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
                editor.putString(Const.USER_JSON_OBJ, getJsonFromObject(mUser, User.class));
                editor.apply();
                return null;
            case LOAD:
                String json = SHARED_PREFERENCES.getString(Const.USER_JSON_OBJ, null);
                if (json != null) return (User) getObjectFromJson(json, User.class);
                else return null;
        }
        return null;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<User>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<User> {
    }
}
