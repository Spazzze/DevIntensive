package com.softdesign.devintensive.data.operations;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.redmadrobot.chronos.ChronosOperationResult;
import com.softdesign.devintensive.utils.Const;

public class UserLoginDataOperation extends BaseChronosOperation<String> {

    private String mLogin;

    public UserLoginDataOperation() {
        this.mAction = Action.LOAD;
    }

    public UserLoginDataOperation(String s) {
        this.mLogin = s;
        this.mAction = Action.SAVE;
    }

    @Nullable
    @Override
    public String run() {
        switch (this.mAction) {
            case SAVE:
                SharedPreferences.Editor editor = SHARED_PREFERENCES.edit();
                if (mLogin == null) {
                    editor.putBoolean(Const.SAVE_LOGIN, false);
                    editor.putString(Const.SAVED_LOGIN_NAME, "");
                } else {
                    editor.putBoolean(Const.SAVE_LOGIN, true);
                    editor.putString(Const.SAVED_LOGIN_NAME, mLogin);
                }
                editor.apply();
                return null;
            case LOAD:
                if (SHARED_PREFERENCES.getBoolean(Const.SAVE_LOGIN, false))
                    return SHARED_PREFERENCES.getString(Const.SAVED_LOGIN_NAME, "");
                else return null;
        }
        return null;
    }

    @NonNull
    @Override
    public Class<? extends ChronosOperationResult<String>> getResultClass() {
        return Result.class;
    }

    public final static class Result extends ChronosOperationResult<String> {
    }
}

