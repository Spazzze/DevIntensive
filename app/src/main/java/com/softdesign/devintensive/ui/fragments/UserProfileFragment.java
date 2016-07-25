package com.softdesign.devintensive.ui.fragments;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.redmadrobot.chronos.gui.fragment.ChronosFragment;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.operations.FullUserDataOperation;
import com.softdesign.devintensive.data.storage.viewmodels.ProfileViewModel;
import com.softdesign.devintensive.databinding.FragmentProfileBinding;
import com.softdesign.devintensive.ui.callbacks.MainActivityCallback;
import com.softdesign.devintensive.utils.Const;

import java.io.File;

import de.greenrobot.event.EventBus;

public class UserProfileFragment extends ChronosFragment {

    private static final String TAG = Const.TAG_PREFIX + "UserProfileFragment";
    private static final DataManager DATA_MANAGER = DataManager.getInstance();
    private static final EventBus BUS = EventBus.getDefault();

    private ProfileViewModel mProfileViewModel;
    private FragmentProfileBinding mProfileBinding;
    private MainActivityCallback mCallbacks;

    private File mPhotoFile = null;

    //region onCreate
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null && mProfileViewModel == null) {
            mProfileViewModel = savedInstanceState.getParcelable(Const.PARCELABLE_KEY_PROFILE);
        }

        mProfileBinding = DataBindingUtil.bind(getView());
        initFields();
/*        activityCallback.setActionBar(toolbar);*/ //// TODO: 25.07.2016
    }
    //endregion

    //region Life Cycle
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState ");
        outState.putParcelable(Const.PARCELABLE_KEY_PROFILE, mProfileViewModel);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.d(TAG, "onAttach: "); //проверить срабатывает ли при повороте экрана
        super.onAttach(activity);
        if (activity instanceof MainActivityCallback) {
            mCallbacks = (MainActivityCallback) activity;
        } else {
            throw new IllegalStateException("Parent activity must implement MainActivityCallback");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BUS.registerSticky(this);
        Log.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        saveUserInfoData();
        BUS.unregister(this);
        super.onPause();
    }
    //endregion

    //region UI
    private void initFields() {
        if (mProfileBinding != null) {
            if (mProfileViewModel != null) mProfileBinding.setProfile(mProfileViewModel);
            else loadFullUserData();
        } else {
            Log.e(TAG, "initFields: Binding is null");
            mCallbacks.logout(1);
        }
    }

    private void setProfileView(@NonNull ProfileViewModel profileViewModel) {
        mProfileViewModel = profileViewModel;
        mProfileBinding.setProfile(mProfileViewModel);
    }
    //endregion

    //region Save and Load/Update
    @SuppressWarnings("unused")
    public void onEvent(ProfileViewModel event) {
        if (event != null) {
            mProfileViewModel = event;
        }
    }

    @SuppressWarnings("unused")
    public void onOperationFinished(final FullUserDataOperation.Result result) {
        if (result.isSuccessful()) {
            if (result.getOutput() != null) { //only Loading
                setProfileView(result.getOutput());
            }
        } else {
            Log.e(TAG, "onOperationFinished: Данные из памяти не были загружены");
            if (mProfileViewModel == null) mCallbacks.logout(0);
        }
    }

    @SuppressWarnings("unchecked")
/*    public void updateUserData(BaseModel<?> newData) {
        if (newData.getData().getClass().isAssignableFrom(UserPhotoRes.class)) {
            BaseModel<UserPhotoRes> res = (BaseModel<UserPhotoRes>) newData;
            mProfileViewModel.mUserData.get().getPublicInfo().setUpdated(res.getData().getUpdated());
        }
        if (newData.getData().getClass().isAssignableFrom(EditProfileRes.class)) {
            BaseModel<EditProfileRes> res = (BaseModel<EditProfileRes>) newData;
            mProfileViewModel.mUserData.set(res.getData().getUser());
        }
    }*/

    private void loadFullUserData() {
        runOperation(new FullUserDataOperation());
    }

    private void saveUserInfoData() {
        runOperation(new FullUserDataOperation(mProfileViewModel));
        mCallbacks.uploadUserPhoto(mProfileViewModel.mUserPhotoUri.get());
        mCallbacks.uploadUserAvatar((mProfileViewModel.mUserAvatarUri.get()));
        mCallbacks.uploadUserData(mProfileViewModel);
    }
    //endregion
}
