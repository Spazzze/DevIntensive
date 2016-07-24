package com.softdesign.devintensive.ui.fragments;

import android.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.redmadrobot.chronos.gui.fragment.ChronosFragment;
import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.network.restmodels.User;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;

public class LocalUserProfileFragment extends ChronosFragment {

    @BindViews({R.id.phone_EditText, R.id.email_EditText, R.id.vk_EditText, R.id.gitHub_EditText, R.id.about_EditText})
    List<EditText> mEditTexts_userInfoList;

    @BindViews({R.id.phone_TextInputLayout, R.id.email_TextInputLayout, R.id.vk_TextInputLayout, R.id.gitHub_TextInputLayout})
    List<TextInputLayout> mTextInputLayouts_userInfoList;

    @BindView(R.id.main_coordinatorLayout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.floating_action_button) FloatingActionButton mFloatingActionButton;
    @BindView(R.id.placeholder_profilePhoto) RelativeLayout mPlaceholder_profilePhoto;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.appbar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.user_photo_img) ImageView mImageView_profilePhoto;

    private Boolean mCurrentEditMode = false;
    private Boolean mNotSavingUserValues = false;
    private File mPhotoFile = null;
    private Uri mUri_SelectedProfileImage = null;
    private String mUri_SelectedAvatarImage = null;
    private User mUserData = null;
    private final FragmentManager mFragmentManager = getFragmentManager();
    private LoadUsersIntoDBFragment mDbNetworkFragment;
    private UpdateServerDataFragment mDataFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
/*        init();
        activityCallback.setActionBar(toolbar);
        presenter.onCreate(savedInstanceState);*/
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
